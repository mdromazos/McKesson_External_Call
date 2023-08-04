package com.informatica.mdm.bes.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.PortalViewConstants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.helper.SupplierSDOHelper;
import com.informatica.mdm.bes.service.BusinessEntityServiceClient;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

public class RequiredDocumentValidate extends Validate {
	BusinessEntityServiceClient businessEntityServiceClient = new BusinessEntityServiceClient();
	private static Logger logger = Logger.getLogger(StateLicensingValidate.class.getName());
	
	// This list indicates which BU's we should include sub team in the filter.
	public static Map<String, Boolean> MAINTENANCE_BUS_USING_SUB_TEAMS;
	static {
		MAINTENANCE_BUS_USING_SUB_TEAMS = new HashMap<>();
		MAINTENANCE_BUS_USING_SUB_TEAMS.put("PSAS", true);
	}

	
	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);
		
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		
		if (businessEntity.equals(Constants.BE_TRADE_SUPPLIER_VIEW) || businessEntity.equals(Constants.BE_NON_TRADE_SUPPLIER_VIEW))
			return validateSupplier360View();
		
		List<DataObject> inputDocuments = inputSDOBe.getList(PortalViewConstants.DOCUMENT + "/item");
		if (inputDocuments == null)
			return null;
		
		DataObject promoteSDOBe = null;
		if (promotePreviewSDO != null)
			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
		
		String rowidObject = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, Constants.ROWID_OBJECT);
		
		List<DataObject> inputPromoteCompanyCodeList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, PortalViewConstants.COMPANY_CODE, inputSDO, helperContext);
		List<DataObject> promoteCompanyCodeList = promoteSDOBe.getList(PortalViewConstants.COMPANY_CODE + "/item");

		List<String> bsnsUnits = SupplierSDOHelper.getBusinessUnits(inputPromoteCompanyCodeList, promoteCompanyCodeList, dataObjectHelperContext);
		List<String> subTeams = null;
		boolean filterOnSubTeam = filterOnSubTeam(businessEntity, bsnsUnits);
		if (filterOnSubTeam) {
			subTeams = SupplierSDOHelper.getSubTeams(inputPromoteCompanyCodeList, promoteCompanyCodeList, dataObjectHelperContext);
		}
		
		List<DataObject> promoteAddressList = promoteSDOBe.getList(PortalViewConstants.ADDRESS + "/item");
		String cntryCd = getPrimaryCountryCode(promoteAddressList);
		
		List<DataObject> buMaintenanceDocs = getBuMaintenanceDocs(bsnsUnits, callContext, besClient, helperContext, businessEntity, filterOnSubTeam, subTeams, cntryCd);
//		logger.info("REQ DOC - BU MAINTENANCE DOCS : " + buMaintenanceDocs);
		
		List<DataObject> inputPromoteDocumentList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, PortalViewConstants.DOCUMENT, inputSDO, helperContext);
		List<DataObject> promoteDocumentList = promoteSDOBe.getList(PortalViewConstants.DOCUMENT + "/item");
		List<String> reqDocTypes = getReqDocTypes(buMaintenanceDocs, callContext, besClient, helperContext, businessEntity, rowidObject);
//		logger.info("REQ DOC TYPES: " + reqDocTypes  + " REQ DOC SIZE : " + reqDocTypes.size());
		
		
		return checkRequiredDocuments(reqDocTypes, inputPromoteDocumentList, promoteDocumentList, 
				helperContext.getDataFactory(), businessEntity, helperContext);
	}
	
	/*
	 * TODO:
	 */
	public List<ValidationError> validateSupplier360View() {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		
		
		return validationErrors;
	}
	
	public List<String> getReqDocTypes(List<DataObject> buMaintenanceDocList, CallContext callContext, CompositeServiceClient besClient, HelperContext helperContext, 
			String businessEntityView, String rowidObject) {
		List<String> reqDocTypes = new ArrayList<String>();
		if (buMaintenanceDocList == null || buMaintenanceDocList.isEmpty())
			return reqDocTypes;
		
		List<DataObject> documents = getDocuments(callContext, besClient, helperContext, businessEntityView, rowidObject);
		List<String> exceptionDocs = new ArrayList<String>();
		
		// get list of documents not required to be uploaded by supplier
		if (documents != null && !documents.isEmpty()) {
			for (DataObject document : documents) {
				String splrReqExcpInd = document.getString("splrReqExcpInd");
				String docTyp = document.getString(BusinessEntityConstants.DOCUMENT_DOC_TYP);
				if (splrReqExcpInd != null && docTyp != null && splrReqExcpInd.equals("Y")) {
					exceptionDocs.add(docTyp);
				}
			}
		}
		
		reqDocTypes = buMaintenanceDocList.stream().map(doc -> {
			return doc.getString("docTyp/docTypCd");
		}).filter(doc -> {
			return !exceptionDocs.contains(doc);
		})
		.collect(Collectors.toList());
		
		return reqDocTypes;
	}
	
	public List<DataObject> getDocuments(CallContext callContext, CompositeServiceClient besClient, HelperContext helperContext, 
			String businessEntityView, String rowidObject) {
		String businessEntity = businessEntityView.contains("NonTrade") ? "ExNonTradeSupplier" : "ExTradeSupplier";
		String filter = "rowidObject=" + rowidObject;
		String childName = "Document";
		DataObject supplierReturn = businessEntityServiceClient
				.searchBEList(callContext, besClient, helperContext, businessEntity, filter, 10, childName);
		
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, "ExTradeSupplier", supplierReturn);

		List<DataObject> documents = null;
		
		List<DataObject> supplierReturnList = supplierReturn.getList("object/item");
		if (supplierReturnList != null && !supplierReturnList.isEmpty()) {
			documents = supplierReturnList.get(0).getList(businessEntity + "/" + BusinessEntityConstants.DOCUMENTS + "/item");
		}
		
		return documents;
		
	}
	
	/**
	 * Call the BES API to retreive the BU Maintenance based on Supplier Business Type, Sub Teams and Business Units
	 * @param bsnsUnits
	 * @param subTeams
	 * @param callContext
	 * @param besClient
	 * @param helperContext
	 * @param businessEntity
	 * @return
	 */
	public List<DataObject> getBuMaintenanceDocs(List<String> bsnsUnits, CallContext callContext,
			CompositeServiceClient besClient, HelperContext helperContext, String businessEntity,
			boolean filterOnSubTeam, List<String> subTeams, String cntryCd) {
		if (bsnsUnits == null || bsnsUnits.isEmpty() || 
				(filterOnSubTeam && (subTeams == null || subTeams.isEmpty())))
			return null;
		
		String splrBsnsTyp = businessEntity.contains("NonTrade") ? "NonTrade" : "Trade";
		String filter = null;
		
		if (splrBsnsTyp.equals("NonTrade")) {
			filter = "splrBsnsTyp=" + splrBsnsTyp + " AND cntryCd=" + cntryCd;
		} else {
			filter = "splrBsnsTyp=" + splrBsnsTyp + " AND bsnsUnit IN [" + String.join(",", bsnsUnits) + "]";
			filter = filterOnSubTeam ? filter + " AND subTeam IN [" +  String.join(",", subTeams) + "]" : filter;
		}
		
		DataObject buMaintenanceListReturn = businessEntityServiceClient.searchBE(callContext, besClient, helperContext, "ExBuMaintenance", filter);
		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, "ExBuMaintenance", buMaintenanceListReturn);

		List<DataObject> buMaintenanceList = buMaintenanceListReturn.getList("object/item");
		
		List<DataObject> buMaintenancsDocs = new ArrayList<DataObject>();
		for (DataObject buMaintenance : buMaintenanceList) {
			if (buMaintenance == null || buMaintenance.getList("ExBuMaintenance/Document/item") == null)
				continue;
			
			buMaintenancsDocs.addAll(buMaintenance.getList("ExBuMaintenance/Document/item"));
		}
		
		return buMaintenancsDocs;
	}	
	
	public List<ValidationError> checkRequiredDocuments(List<String> reqDocTypes, List<DataObject> inputPromoteDocumentList, 
			List<DataObject> promoteDocumentList, DataFactory dataFactory, String businessEntity,
			HelperContext helperContext) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		if (reqDocTypes == null || reqDocTypes.isEmpty())
			return validationErrors;
		
		List<String> currentDocs = new ArrayList<String>();
		try {
			int i = 0;
			if (inputPromoteDocumentList != null && !inputPromoteDocumentList.isEmpty()) {
				for (DataObject inputPromoteDocument : inputPromoteDocumentList) {
					if (inputPromoteDocument == null)
						continue;
					String inputPromoteDocumentRowid = inputPromoteDocument.getString(BusinessEntityConstants.ROWID_OBJECT);
					DataObject promoteDocument = null;
					if (inputPromoteDocumentRowid != null && promoteDocumentList != null)
						promoteDocument = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteDocumentList, inputPromoteDocumentRowid);
					
					String docType = vendorSDOHelper.getString(inputPromoteDocument, promoteDocument, PortalViewConstants.DOCUMENT_DOC_TYPE);

					if (docType == null)
						continue;
					
					Boolean overrideIndicator = vendorSDOHelper.getBoolean(inputPromoteDocument, promoteDocument, PortalViewConstants.DOCUMENT_SPLR_OVRDE_REQ_IND);
					String overrideReason = vendorSDOHelper.getString(inputPromoteDocument, promoteDocument, PortalViewConstants.DOCUMENT_SPLR_OVRDE_REQ_RSN);
					String fileName = vendorSDOHelper.getString(inputPromoteDocument, promoteDocument, PortalViewConstants.DOCUMENT_FILE_NM);
					String docName = vendorSDOHelper.getString(inputPromoteDocument, promoteDocument, PortalViewConstants.DOCUMENT_DOC_NM);
					
					if ((overrideIndicator == null || !overrideIndicator) &&
							(fileName == null || docName == null)) {
						if (fileName == null) {
							validationErrors.add(createError("ERROR-PORTAL-1", 
									"ERROR-PORTAL-1 : Documents require a File Name", 
									businessEntity + "." + PortalViewConstants.DOCUMENT + "[" + i + "]." + PortalViewConstants.DOCUMENT_FILE_NM,
									helperContext.getDataFactory()));
						} 
						if (docName == null) {
							validationErrors.add(createError("ERROR-PORTAL-2", 
									"ERROR-PORTAL-2 : Documents require a Document Name", 
									businessEntity + "." + PortalViewConstants.DOCUMENT + "[" + i + "]." + PortalViewConstants.DOCUMENT_DOC_NM,
									helperContext.getDataFactory()));
						}
					} else if ((overrideIndicator != null && overrideIndicator) &&
								(overrideReason == null)) {
						validationErrors.add(createError("ERROR-PORTAL-3", 
								"ERROR-PORTAL-3 : Documents with field 'Override Required' turn on must populate the'Override Reason' field", 
								businessEntity + "." + PortalViewConstants.DOCUMENT + "[" + i + "]." + PortalViewConstants.DOCUMENT_SPLR_OVRDE_REQ_RSN,
								helperContext.getDataFactory()));
					}
					currentDocs.add(docType);
					i++;
				}
			}
			
//			logger.info("CURRENT DOCS: " + currentDocs + " SIZE : " + currentDocs.size());
			
			for (String reqDocType : reqDocTypes) {
				if (!currentDocs.contains(reqDocType)) {
					validationErrors.add(createError("ERROR-PORTAL-4", 
							"ERROR-PORTAL-4 : A Document with type: " + reqDocType + " is required to be uploaded.", 
							businessEntity + "." + PortalViewConstants.DOCUMENT, dataFactory));
				}
			}
			
		} catch (Exception e) {
			logger.error("Error Validating Required Documents: " + e.getMessage());
		}
		
		return validationErrors;
	}
	
	private boolean filterOnSubTeam(String businessEntity, List<String> businessUnits) {
		if (businessEntity == null || businessEntity.contains("NonTrade") || businessUnits == null || businessUnits.isEmpty())
			return false;
		
		return businessUnits.parallelStream().anyMatch(bu -> {
			if (bu == null)
				return false;
			return MAINTENANCE_BUS_USING_SUB_TEAMS.containsKey(bu);
		});
	}
	
	
	private String getPrimaryCountryCode(List<DataObject> promoteAddressList) {
		if (promoteAddressList == null || promoteAddressList.isEmpty())
			return null;
		
		String countryCode = null;
		
		for (DataObject promoteAddress : promoteAddressList) {
			Boolean primaryAddr = promoteAddress.getBoolean(PortalViewConstants.ADDRESS_PRIMARY_IND);
			if (primaryAddr) {
				countryCode = promoteAddress.getString(PortalViewConstants.ADDRESS_COUNTRY);
				break;
			}
		}
		
		return countryCode;
	}

}
