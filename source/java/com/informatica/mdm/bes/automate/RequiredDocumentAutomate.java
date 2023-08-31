package com.informatica.mdm.bes.automate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.helper.SDOHelper;
import com.informatica.mdm.bes.helper.SupplierSDOHelper;
import com.informatica.mdm.bes.service.BusinessEntityServiceClient;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author Matthew Dromazos
 * 
 * This class creates the "Required Documents" based on the BU given in the Requestor Registration Form
 *
 */
public class RequiredDocumentAutomate extends Automate {
	private static Logger logger = Logger.getLogger(RequiredDocumentAutomate.class.getName());

	BusinessEntityServiceClient businessEntityServiceClient = new BusinessEntityServiceClient();
	
	// This list indicates which BU's we should include sub team in the filter.
	public static Map<String, Boolean> MAINTENANCE_BUS_USING_SUB_TEAMS;
	static {
		MAINTENANCE_BUS_USING_SUB_TEAMS = new HashMap<>();
		MAINTENANCE_BUS_USING_SUB_TEAMS.put("PSAS", true);
	}

	@Override
	public List<ValidationError> doAutomate(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity, DataObject promoteSDO,
			CallContext callContext, CompositeServiceClient besClient) {
		SDOChangeSummary inputSdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		inputSdoChangeSummary.pauseLogging();
		
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		
		try {
			DataObject request = (DataObject) inputSDOBe.getList(BusinessEntityConstants.REQUEST + "/item").get(0);
			String sendToPrtl = request.getString(BusinessEntityConstants.REQUEST_SEND_TO_PORTAL);
			if (sendToPrtl.equals("N"))
				return null;
				
			String filter = null;
			try {
				filter = buildFilter(inputSDOBe, businessEntity);
			} catch (Exception e) {
				logger.error("ERROR CREATING FILTER: " + e.getLocalizedMessage());
				return null;
			}
			
			List<DataObject> buMaintenanceDocs = getBuMaintenanceDocs(callContext, besClient, helperContext,filter);
			
			createDocuments(inputSDOBe, buMaintenanceDocs, inputSdoChangeSummary);
		} catch (Exception e) {
			
		}
	
		
		return null;
	}
	
	public String buildFilter(DataObject inputSDOBe, String businessEntity) throws Exception {
		String filter = null;

		if (businessEntity.contains("NonTrade")) {			
			List<DataObject> primaryAddress = inputSDOBe.getList(BusinessEntityConstants.PRIMARY_ADDRESS + "/item");
			
			String cntryCd = primaryAddress.get(0)
					.getString(BusinessEntityConstants.PRIMARY_ADDRESS_POSTAL_ADDRESS + "/" +BusinessEntityConstants.PRIMARY_ADDRESS_CNTRY_CD);
			
			filter = "splrBsnsTyp=NonTrade AND cntryCd=" + cntryCd;
		} else {
			List<DataObject> inputCompanyCodes = inputSDOBe.getList(BusinessEntityConstants.COMPANY_CODE + "/item");

			if (inputCompanyCodes == null || inputCompanyCodes.isEmpty())
				throw new Exception();
			
			List<String> bsnsUnits = SupplierSDOHelper.getBusinessUnits(inputCompanyCodes, null, dataObjectHelperContext);
			List<String> subTeams = null;
			boolean filterOnSubTeam = filterOnSubTeam(businessEntity, bsnsUnits);
			if (filterOnSubTeam) {
				subTeams = SupplierSDOHelper.getSubTeams(inputCompanyCodes, null, dataObjectHelperContext);
			}
			
			filter = "splrBsnsTyp=Trade AND bsnsUnit IN [" + String.join(",", bsnsUnits) + "]";
			filter = filterOnSubTeam ? filter + " AND subTeam IN [" +  String.join(",", subTeams) + "]" : filter;
		}
		
		return filter;
	}
	
	@SuppressWarnings("unchecked")
	public void createDocuments(DataObject inputSDOBe, List<DataObject> buMaintenanceDocs, SDOChangeSummary inputSdoChangeSummary) {
		if (inputSDOBe == null || buMaintenanceDocs == null || buMaintenanceDocs.isEmpty())
			return;
		
		try {
			inputSdoChangeSummary.resumeLogging();
			DataObject documentPager = inputSDOBe.getDataObject(BusinessEntityConstants.DOCUMENTS);
			String documentPagerItemName = "item";
			List<String> currentDocTypes = new ArrayList<String>();
			//If no documents have been created, create the pager
			if (documentPager == null) {
				documentPager = inputSDOBe.createDataObject(BusinessEntityConstants.DOCUMENTS);
			} else if (inputSDOBe.isSet(BusinessEntityConstants.DOCUMENTS + "/item")) {
				currentDocTypes = (List<String>) inputSDOBe.getList(BusinessEntityConstants.DOCUMENTS + "/item").stream()
					.map(inputDoc -> {
						return inputDoc != null ? ((DataObject) inputDoc).getString(BusinessEntityConstants.DOCUMENT_DOC_TYP) : null;
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			}
			
			for (DataObject buMaintenanceDoc : buMaintenanceDocs) {
				if (buMaintenanceDoc == null)
					continue;
				String buMaintenanceDocTyp = buMaintenanceDoc.getString(BusinessEntityConstants.BU_MNTNC_DOCUMENT_DOC_TYP);
				if (currentDocTypes.contains(buMaintenanceDocTyp))
					continue;
				
				DataObject documentItem = documentPager.createDataObject(documentPagerItemName);
				DataObject docTypSdo = documentItem.createDataObject(BusinessEntityConstants.DOCUMENT_DOC_TYP_LKP);
				docTypSdo.setString(BusinessEntityConstants.DOCUMENT_DOC_TYP_LKP_CD, buMaintenanceDocTyp);
			}	
		} catch (Exception e) {
			logger.error("EXT-CALL-ERROR 1: Error Creating Documents for Supplier Registration");
			logger.error(e.getMessage());
		} finally {
			inputSdoChangeSummary.pauseLogging();
		}
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
	public List<DataObject> getBuMaintenanceDocs(CallContext callContext,
			CompositeServiceClient besClient, HelperContext helperContext, String filter) {
		DataObject buMaintenanceListReturn = businessEntityServiceClient.searchBE(callContext, besClient, helperContext, 
				"ExBuMaintenance", filter);
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
			filter = filterOnSubTeam ? filter + " AND subTeams IN [" +  String.join(",", subTeams) + "]" : filter;
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
	
	private boolean filterOnSubTeam(String businessEntity, List<String> businessUnits) {
		if (businessEntity == null || businessEntity.contains("NonTrade") || businessUnits == null || businessUnits.isEmpty())
			return false;
		
		return businessUnits.parallelStream().anyMatch(bu -> {
			if (bu == null)
				return false;
			return MAINTENANCE_BUS_USING_SUB_TEAMS.containsKey(bu);
		});
	}

}
