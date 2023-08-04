package com.informatica.mdm.bes.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.helper.SupplierSDOHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * If Vendor is doing business with PSAS and the Product Coverage is under 10
 * million than require a COI to be entered. If not throw an error.
 * 
 * If the Vendor is Non-Trade and ProductCoverage is under 5 Million then COI
 * must be uploaded. If not throw an error.
 * 
 * @author mdromazo
 *
 */
public class COIValidate extends Validate {

//	@Override
//	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
//			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
//			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
//		pauseLogging(inputSDO);
//
//		if (inputSDO == null || businessEntity.equals(Constants.BE_TRADE_REGIST_VIEW) || businessEntity.equals(Constants.BE_NONTRADE_REGIST_VIEW))
//			return null;
//		
//		Boolean validateOnly = (Boolean) inParams.get(Constants.VALIDATE_ONLY);
//		Boolean isE360Save = isE360Save(validateOnly);
//		logger.info("Save Button : " + isE360Save);
//		
//		if (!isE360Save)
//			return null;
//		
//		// Run when validations when save button is clicked
//		DataObject promoteSDOBe = null;
//		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
//		if (promotePreviewSDO != null)
//			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
//
//		/* Company Code */
//		String companyCodeChild = null;
//		if (businessEntity.equals(Constants.BE_TRADE_REGIST_VIEW))
//			companyCodeChild = VendorMainConstants.COMPANY_CODE;
//		else
//			companyCodeChild = VendorMainConstants.COMPANY_CODE;
//		
//		logger.info("Company Code Child : " + companyCodeChild);
//
//		List<DataObject> companyCodeList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe,
//				companyCodeChild, inputSDO, helperContext);
//
//		List<DataObject> inputPromoteInsuranceList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe,
//				VendorMainConstants.INSURANCE, inputSDO, helperContext);
//
//		List<DataObject> inputPromoteDocumentList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe,
//				VendorMainConstants.DOCUMENT, inputSDO, helperContext);
//
//		logger.info("AFTER GET COMBINED LIST : InsuranceList : " + companyCodeList + " Insurance List : "
//				+ inputPromoteInsuranceList + " Documents List : " + inputPromoteDocumentList);
//		
//		if (inputPromoteInsuranceList == null || inputPromoteInsuranceList.isEmpty())
//			return null;
//
//		// If Insurance Child has records
//		List<String> businessUnitList = new ArrayList<String>();
//		// Get Business Unit
//		companyCodeList.parallelStream().forEach((inputCompanyCode) -> {
//			
//			if (inputCompanyCode != null) {
//				String inputPromoteContactRowid = inputCompanyCode
//						.getString(BusinessEntityConstants.ROWID_OBJECT);
//				DataObject promoteCompanyCode = null;
//				if (inputPromoteContactRowid != null && companyCodeList != null)
//					promoteCompanyCode = dataObjectHelperContext.getDataObjectSearcher()
//							.searchDataObjectList(companyCodeList, inputPromoteContactRowid);
//				String businessUnit = vendorSDOHelper.getString(inputCompanyCode, promoteCompanyCode,
//						VendorMainConstants.BUSINESS_UNIT);
//				logger.info("Business Unit : " + businessUnit);
//				businessUnitList.add(businessUnit);
//			}
//		});
//		if (businessUnitList.contains(Constants.BU_PSAS) || businessUnitList.contains(Constants.BU_USON) || businessUnitList.contains(Constants.BU_CMM)) {
//			
//			// Read Document Details
//			List<String> documentTypeList = new ArrayList<String>();
//			if (inputPromoteDocumentList == null) {
//				
//				return createErrors(ErrorConstants.COI_INSURANCE_VALIDATE_ERROR_CODE,
//						ErrorConstants.COI_INSURANCE_VALIDATE_ERROR_MESSAGE,
//						businessEntity + "." + VendorMainConstants.DOCUMENT,
//						helperContext.getDataFactory(), "ERROR");
//			}
//			
//			for (DataObject inputPromoteDocument : inputPromoteDocumentList) {
//				
//				if (inputPromoteDocument != null) {
//					
//					String inputPromoteDocumentRowid = inputPromoteDocument
//							.getString(BusinessEntityConstants.ROWID_OBJECT);
//					logger.info("Document RowId : " + inputPromoteDocumentRowid);
//					DataObject promoteDocument = null;
//					if (inputPromoteDocumentRowid != null && inputPromoteDocumentList != null)
//						promoteDocument = dataObjectHelperContext.getDataObjectSearcher()
//								.searchDataObjectList(inputPromoteDocumentList, inputPromoteDocumentRowid);
//					
//					String documentType = vendorSDOHelper.getString(inputPromoteDocument, promoteDocument, VendorMainConstants.DOCUMENT_DOC_TYP);
//					
//					logger.info("Document Type : " + documentType);
//					documentTypeList.add(documentType);
//				}
//			}
//
//			for (DataObject inputPromoteInsurance : inputPromoteInsuranceList) {
//				
//				if (inputPromoteInsurance != null) {
//					
//					String inputPromoteInsuranceRowid = inputPromoteInsurance
//							.getString(BusinessEntityConstants.ROWID_OBJECT);
//					DataObject promoteInsurance = null;
//					if (inputPromoteInsuranceRowid != null && inputPromoteInsuranceList != null)
//						promoteInsurance = dataObjectHelperContext.getDataObjectSearcher()
//								.searchDataObjectList(inputPromoteInsuranceList,
//										inputPromoteInsuranceRowid);
//					float insuranceVal = vendorSDOHelper.getFloat(inputPromoteInsurance, promoteInsurance,
//							VendorMainConstants.INSURANCE_INSURANCE_VAL);
//					logger.info("Insurance Value: " + insuranceVal);
//
//					if ((businessUnitList.contains(Constants.BU_PSAS) && Integer.compare(
//							Float.compare(insuranceVal, VendorMainConstants.PSAS_INSURANCE_VALUE), -1) == 0)
//							|| (businessUnitList.contains("Non-Trade")
//									&& Integer.compare(
//											Float.compare(insuranceVal,
//													VendorMainConstants.NON_TRADE_INSURANCE_VALUE),
//											-1) == 0)) {
//						
//						if (!documentTypeList.contains(Constants.DOC_TYPE_COI_APPROVAL)) {
//							return createErrors(ErrorConstants.COI_INSURANCE_VALIDATE_ERROR_CODE,
//									ErrorConstants.COI_INSURANCE_VALIDATE_ERROR_MESSAGE,
//									businessEntity + "." + VendorMainConstants.DOCUMENT,
//									helperContext.getDataFactory(), "ERROR");
//						}
//					}
//
//				}
//
//			}
//		}
//		return null;
//	}
	
	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);
		
		DataObject promoteSDOBe = null;
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		List<DataObject> promoteInsuranceList = null;
		List<DataObject> promoteDocumentList = null;
		List<DataObject> promoteCompanyCodeList = null;
		if (promotePreviewSDO != null) {
			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
			promoteInsuranceList = promoteSDOBe.getList(VendorMainConstants.INSURANCE + "/item");
			promoteDocumentList = promoteSDOBe.getList(VendorMainConstants.DOCUMENT + "/item");
			promoteCompanyCodeList = promoteSDOBe.getList(VendorMainConstants.COMPANY_CODE + "/item");
		}
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		
		List<DataObject> inputPromoteInsuranceList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe,
				VendorMainConstants.INSURANCE, inputSDO, helperContext);

		List<DataObject> inputPromoteDocumentList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe,
				VendorMainConstants.DOCUMENT, inputSDO, helperContext);
		
		List<DataObject> inputPromoteCompanyCodeList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe,
				VendorMainConstants.COMPANY_CODE, inputSDO, helperContext);
		
		
		
		List<String> businessUnits = SupplierSDOHelper.getBusinessUnits(inputPromoteCompanyCodeList, promoteCompanyCodeList, dataObjectHelperContext);
		
		List<String> docsUploaded = getDocTypesUploaded(inputPromoteDocumentList, promoteDocumentList);
		
		
		// if COI Approval has already been uploaded, no need to validate.
		if (docsUploaded.contains(Constants.DOC_TYPE_COI_APPROVAL) 
				|| inputPromoteInsuranceList == null || inputPromoteInsuranceList.isEmpty())
			return null;
		
		boolean nonTradeVender = businessEntity.contains("NonTrade");
		
		for (DataObject inputPromoteInsurance : inputPromoteInsuranceList) {
			if (inputPromoteInsurance == null)
				continue;
			String inputPromoteInsuranceRowid = inputPromoteInsurance.getString(Constants.ROWID_OBJECT);
			DataObject promoteInsurance = null;
			if (inputPromoteInsuranceRowid != null) 
				promoteInsurance = dataObjectHelperContext.getDataObjectSearcher()
					.searchDataObjectList(promoteInsuranceList, inputPromoteInsuranceRowid);
			
			double insuranceVal = vendorSDOHelper.getDouble(inputPromoteInsurance, promoteInsurance, VendorMainConstants.INSURANCE_INSURANCE_VAL);
			if (nonTradeVender && insuranceVal < VendorMainConstants.NON_TRADE_INSURANCE_VALUE) {
				validationErrors.add(createError(ErrorConstants.NONTRADE_COI_INSURANCE_VALIDATE_ERROR_CODE,
						ErrorConstants.NONTRADE_COI_INSURANCE_VALIDATE_ERROR_MESSAGE,
						businessEntity + "." + VendorMainConstants.DOCUMENT, 
						helperContext.getDataFactory()));				break;
			} else if (!nonTradeVender) {
//				String insuranceBsnsUnit = vendorSDOHelper.getString(inputPromoteInsurance, promoteInsurance, VendorMainConstants.INSURANCE_BSNS_UNIT_CD);

				if (businessUnits != null && businessUnits.contains("PSAS") && insuranceVal < VendorMainConstants.PSAS_INSURANCE_VALUE) {
					validationErrors.add(createError(ErrorConstants.PSAS_COI_INSURANCE_VALIDATE_ERROR_CODE,
							ErrorConstants.PSAS_COI_INSURANCE_VALIDATE_ERROR_MESSAGE,
							businessEntity + "." + VendorMainConstants.DOCUMENT, 
							helperContext.getDataFactory()));
					break;
				}
			}
		}
		
		return validationErrors;
	}
	
	private List<String> getDocTypesUploaded(List<DataObject> inputPromoteDocumentList, List<DataObject> promoteDocumentList) {
		List<String> docTypesUploaded = new ArrayList<String>();
		if (inputPromoteDocumentList == null)
			return docTypesUploaded;
		
		for (DataObject inputPromoteDocument : inputPromoteDocumentList) {
			if (inputPromoteDocument == null)
				continue;
			
				
			String inputPromoteDocumentRowid = inputPromoteDocument
					.getString(BusinessEntityConstants.ROWID_OBJECT);
			logger.info("Document RowId : " + inputPromoteDocumentRowid);
			DataObject promoteDocument = null;
			if (inputPromoteDocumentRowid != null && inputPromoteDocumentList != null)
				promoteDocument = dataObjectHelperContext.getDataObjectSearcher()
						.searchDataObjectList(inputPromoteDocumentList, inputPromoteDocumentRowid);
			
			String documentType = vendorSDOHelper.getString(inputPromoteDocument, promoteDocument, VendorMainConstants.DOCUMENT_DOC_TYP);
			
			logger.info("Document Type : " + documentType);
			docTypesUploaded.add(documentType);
			
		}
		return docTypesUploaded;
	}
	

	protected Boolean isE360Save(Boolean validateOnly) {
		return (validateOnly != null && !validateOnly) ? true : false;
	}
}
