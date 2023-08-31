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
import com.informatica.mdm.bes.service.BusinessEntityServiceClient;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author Matthew Dromazos
 *
 */
public class StateLicensingValidate extends Validate {
	BusinessEntityServiceClient businessEntityServiceClient = new BusinessEntityServiceClient();
	private static Logger logger = Logger.getLogger(StateLicensingValidate.class.getName());
	
	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);
		
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = null;
		if (promotePreviewSDO != null)
			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
		
		// Get Licenses Lists
//		List<DataObject> inputPromoteLicenseList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, PortalViewConstants.LICENSE, inputSDOBe, false);
//		List<DataObject> promoteLicenseList = promoteSDOBe.getList(PortalViewConstants.LICENSE + "/item");
		
		// Get Distributions Lists
		List<DataObject> inputDistributionList = inputSDOBe.getList(PortalViewConstants.DISTRIBUTION + "/item");
		List<DataObject> inputPromoteDistributionList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, PortalViewConstants.DISTRIBUTION, inputSDOBe, helperContext);
		List<DataObject> promoteDistributionList = promoteSDOBe.getList(PortalViewConstants.DISTRIBUTION + "/item");
		
		// If there are no licenses entered, then we do not need to perform this validation.
		if (inputDistributionList == null || inputDistributionList.isEmpty() || inputPromoteDistributionList == null || promoteDistributionList.isEmpty())
			return null;
		
		
		// Get Document Lists
		List<DataObject> inputPromoteDocumentList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, PortalViewConstants.DOCUMENT, inputSDOBe, helperContext);
		List<DataObject> promoteDocumentList = promoteSDOBe.getList(PortalViewConstants.DOCUMENT + "/item");
		
		// Get List of Distribution and Products
//		List<ProductDistribution> distributionsProducts = getProductsAndDistributionsInUS(inputPromoteLicenseList, promoteLicenseList);
		
		// Call the API to get the list of License Maintenance
		List<DataObject> licenseMaintenanceList = getLicenseMaintenance(inputPromoteDistributionList, promoteDistributionList, callContext, 
				besClient, helperContext, businessEntity);
		
		// Validate the State Licenses required to be uploaded
		List<ValidationError> validationErrors = validateStateLicense(licenseMaintenanceList,inputPromoteDocumentList, 
				promoteDocumentList, helperContext.getDataFactory(), businessEntity);
		return validationErrors;
	}
	
	
	/**
	 * This method gathers the list of product types entered into the Licenses Children
	 * 
	 * @param inputPromoteLicenseList
	 * @param promoteLicenseList
	 * @return
	 */
	public List<String> getProductTypes(List<DataObject> inputPromoteLicenseList, List<DataObject> promoteLicenseList) {
		List<String> productTypes = new ArrayList<String>();
		if (inputPromoteLicenseList == null || inputPromoteLicenseList.isEmpty())
			return productTypes;
		
		for (DataObject inputPromoteLicense : inputPromoteLicenseList) {
			String inputPromoteLicenseRowid = inputPromoteLicense.getString(BusinessEntityConstants.ROWID_OBJECT);
			DataObject promoteLicense = null;
			if (inputPromoteLicenseRowid != null && promoteLicenseList != null)
				promoteLicense = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteLicenseList, inputPromoteLicenseRowid);

			
			String regProductType = vendorSDOHelper.getString(inputPromoteLicense, promoteLicense, PortalViewConstants.LICENSE_REG_PRODUCT_TYPE);
			if (regProductType != null) {
				productTypes.add(regProductType);
			}
		}
		return productTypes;
	}
	
	/**
	 * This method created a List of ProductDistributions that detail what the Product and distribution is for each License.
	 * 
	 * @param inputPromoteLicenseList
	 * @param promoteLicenseList
	 * @return
	 */
	public List<ProductDistribution> getProductsAndDistributionsInUS(List<DataObject> inputPromoteLicenseList, List<DataObject> promoteLicenseList) {
		List<DataObject> licensesInUS = new ArrayList<DataObject>();
		List<ProductDistribution> distributionsProducts = new ArrayList<ProductDistribution>();
		
		// If the input promote Licenses list is null, then we need to return.
		if (inputPromoteLicenseList == null || inputPromoteLicenseList.isEmpty())
			return distributionsProducts;
		
		for (DataObject inputPromoteLicense : inputPromoteLicenseList) {
			String inputPromoteLicenseRowid = inputPromoteLicense.getString(BusinessEntityConstants.ROWID_OBJECT);
			DataObject promoteLicense = null;
			if (inputPromoteLicenseRowid != null && promoteLicenseList != null)
				promoteLicense = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteLicenseList, inputPromoteLicenseRowid);
			String cntryCd = vendorSDOHelper.getString(inputPromoteLicense, promoteLicense, PortalViewConstants.LICENSE_COUNTRY_CODE);
			if (cntryCd == null || !cntryCd.equals("US"))
				continue;
			
			licensesInUS.add(inputPromoteLicense);
			String distributionType = vendorSDOHelper.getString(inputPromoteLicense, promoteLicense, PortalViewConstants.LICENSE_COUNTRY_CODE);
			String productType = vendorSDOHelper.getString(inputPromoteLicense, promoteLicense, PortalViewConstants.LICENSE_COUNTRY_CODE);
			
			if (distributionType == null || productType == null)
				continue;
			distributionsProducts.add(new ProductDistribution(distributionType, productType));
		}
				
		return distributionsProducts;
	}
	
	
	/**
	 * This method calls the BES API LicenseMaintenance to get the list of State Licenses required to be uploaded.
	 * 
	 * @param productDistributions
	 * @param callContext
	 * @param compositeServiceClient
	 * @param helperContext
	 * @param businessEntity
	 * @return
	 */
	public List<DataObject> getLicenseMaintenance(List<DataObject> inputPromoteDistributions, List<DataObject> promoteDistributions, CallContext callContext, 
			CompositeServiceClient compositeServiceClient, HelperContext helperContext, String businessEntity) {
		List<DataObject> licenseMaintenanceList = new ArrayList<DataObject>();

		// Consolidate the list of distribution and product types to use in the filter to the BES API
		List<String> productTypesList = new ArrayList<String>();
		List<String> manTypList = new ArrayList<String>();
		Map<String, List<String>> manufacturerProducts = new HashMap<String, List<String>>();
		
		for (DataObject inputPromoteDistribution: inputPromoteDistributions) {
			String inputPromoteDistributionRowid = inputPromoteDistribution.getString(Constants.ROWID_OBJECT);
			DataObject promoteDistribution = null;
			if (inputPromoteDistributionRowid != null && promoteDistributions != null)
				promoteDistribution = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteDistributions, inputPromoteDistributionRowid);
			String manTyp = vendorSDOHelper.getString(inputPromoteDistribution, promoteDistribution, PortalViewConstants.DISTRIBUTION_MAN_TYP);
			if (manTyp != null)
				manTypList.add(manTyp);
			
			if (!manufacturerProducts.containsKey(manTyp))
				manufacturerProducts.put(manTyp, new ArrayList());
			
			boolean rx = vendorSDOHelper.getBoolean(inputPromoteDistribution, promoteDistribution, PortalViewConstants.DISTRIBUTION_RX);
			if (rx)
				productTypesList.add("RX");
			
			manufacturerProducts.get(manTyp).add("RX");
			
		}

		String filter = "manufacturerType IN '[" + String.join(",", manTypList) + "]' AND productType IN [" + String.join(",", productTypesList) + "]";

		// Call the BES API and get the return.
		DataObject licenseMaintenenceReturn = businessEntityServiceClient.searchBE(callContext, compositeServiceClient, helperContext, "ExLicenseMaintenance", filter);
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, "ExLicenseMaintenance", licenseMaintenenceReturn);
		licenseMaintenanceList = licenseMaintenenceReturn.getList("object/item");
		
		List<DataObject> licenseMaintenanceListFilt = new ArrayList<DataObject>();
		
		for (DataObject licenseMaintenanceReturn : licenseMaintenanceList) {
			String manTyp = licenseMaintenanceReturn.getString("ExLicenseMaintenance/manufacturerType/manufacturerTypCd");
			String prdctTyp = licenseMaintenanceReturn.getString("ExLicenseMaintenance/productType/regulatoryProductTypCd");
			
			if (manufacturerProducts.containsKey(manTyp) && manufacturerProducts.get(manTyp).contains(prdctTyp))
				licenseMaintenanceListFilt.add(licenseMaintenanceReturn);
			
		}
		
		return licenseMaintenanceListFilt;
	}
	
	/**
	 * Validate the State License documents uploaded by the Supplier
	 * 
	 * @param licenseMaintenanceList
	 * @param inputPromoteDocumentList
	 * @param promoteDocumentList
	 * @param dataFactory
	 * @param businessEntity
	 * @return
	 */
	public List<ValidationError> validateStateLicense(List<DataObject> licenseMaintenanceList, List<DataObject> inputPromoteDocumentList, 
			List<DataObject> promoteDocumentList, DataFactory dataFactory, String businessEntity) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		List<String> documentStateLicenceTypeList = getDocumentStatesLicenseType(inputPromoteDocumentList, promoteDocumentList);
		
		for (DataObject licenseMaintenance : licenseMaintenanceList) {
			List<DataObject> stateLicenseMaintenanceList = licenseMaintenance.getList("ExLicenseMaintenance/StateLicenseMaintenance/item");
			for (DataObject stateLicenseMaintenance : stateLicenseMaintenanceList) {
				String state = stateLicenseMaintenance.getString("stateCd/stateCode");
				String licenseType = stateLicenseMaintenance.getString("licenseType/lcnsTypCd");
				if (documentStateLicenceTypeList == null || !documentStateLicenceTypeList.contains(state + licenseType)) {
					validationErrors.add(createError("CUSTOM-08", 
							"CUSTOM-08 STATE LICENSE VALIDATION", 
							businessEntity + "." + PortalViewConstants.DOCUMENT, 
							dataFactory));
				}
			}
		}
		
		return validationErrors;
	}
	
	/**
	 * This method gets all of the document types uploaded into a simple ArrayList.
	 * 
	 * @param inputPromoteDocumentList
	 * @param promoteDocumentList
	 * @return
	 */
	private List<String> getDocumentStatesLicenseType(List<DataObject> inputPromoteDocumentList, List<DataObject> promoteDocumentList) {
		List<String> documentStateLicenceTypeList = new ArrayList<String>();
		if (inputPromoteDocumentList == null || inputPromoteDocumentList.isEmpty())
			return documentStateLicenceTypeList;
		
		for (DataObject inputPromoteDocument: inputPromoteDocumentList) {
			String inputPromoteDocumentRowid = inputPromoteDocument.getString(BusinessEntityConstants.ROWID_OBJECT);
			DataObject promoteDocument = null;
			if (inputPromoteDocumentRowid != null && promoteDocumentList != null)
				promoteDocument = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteDocumentList, inputPromoteDocumentRowid);
			String state = vendorSDOHelper.getString(inputPromoteDocument, promoteDocument, PortalViewConstants.DOCUMENT_STATE);
			String licenseType = vendorSDOHelper.getString(inputPromoteDocument, promoteDocument, PortalViewConstants.DOCUMENT_LICENSE_TYPE);
			if (state == null || licenseType == null)
				continue;

			documentStateLicenceTypeList.add(state + licenseType);
		}
		
		return documentStateLicenceTypeList;
	}
	
	/**
	 * Class used to hold what the ProductType and Distribution type is for a supplier.
	 * Allows the consolidation of the data to use as a reference in other code.
	 * 
	 * @author Matthew Dromazos
	 *
	 */
	protected class ProductDistribution {
		public String productType;
		public String distributionType;
		public ProductDistribution(String productType, String distributionType) {
			this.productType = productType;
			this.distributionType = distributionType;
		}
	}

}
