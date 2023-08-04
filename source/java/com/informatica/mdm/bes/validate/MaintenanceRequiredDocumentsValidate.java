package com.informatica.mdm.bes.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.config.PortalViewConstants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.helper.RoleHelper;
import com.informatica.mdm.bes.service.BusinessEntityServiceClient;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.ChangeSummary.Setting;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

/**
 * The class will look at a properties file that details the business logic for how documents are required based on the Vendor.
 * 
 * @author Matthew Dromazos
 *
 */
public class MaintenanceRequiredDocumentsValidate extends Validate {
	BusinessEntityServiceClient businessEntityServiceClient = new BusinessEntityServiceClient();
	private static Logger logger = Logger.getLogger(StateLicensingValidate.class.getName());
	List<String> roles;
	
	private enum TYPE_ACTIONS {HAS_TYP, HAS_ASSOCIATED_TYP};
	
	// This list indicates which BU's we should include sub team in the filter.
	public static Map<String, Boolean> MAINTENANCE_BUS_USING_SUB_TEAMS;
	static {
		MAINTENANCE_BUS_USING_SUB_TEAMS = new HashMap<>();
		MAINTENANCE_BUS_USING_SUB_TEAMS.put("PSAS", true);
	}
	
	public MaintenanceRequiredDocumentsValidate(List<String> roles) {
		this.roles = roles;
	}
	
	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = null;
		if (promotePreviewSDO != null)
			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
		
		String hubStateIndPromote = promoteSDOBe != null ? promoteSDOBe.getString(BusinessEntityConstants.HUB_STATE_IND) : null;
		if (hubStateIndPromote != null && hubStateIndPromote.equals("0"))
			return null;
		
		if (isSendBackOnDocSection(inputSDOBe, inParams))
			return null;
		
		// Get Input and Promote SDO Change Summary
		SDOChangeSummary inputSdoChangeSummary = ((SDOChangeSummary)inputSDO.getChangeSummary());
		SDOChangeSummary promoteSdoChangeSummary = ((SDOChangeSummary)promotePreviewSDO.getChangeSummary());

		boolean isSupplier = RoleHelper.isSupplier(roles);
		
		// Gather the Company Code Lists
		List<DataObject> inputPromoteCompanyCodeList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.COMPANY_CODE, inputSDO, helperContext);
		List<DataObject> promoteCompanyCodeList = promoteSDOBe != null ? promoteSDOBe.getList(BusinessEntityConstants.COMPANY_CODE + "/item") : null;
		
		List<DataObject> inputPromotePrimaryAddress = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.PRIMARY_ADDRESS, inputSDO, helperContext);
		String cntryCd = inputPromotePrimaryAddress.get(0)
				.getString(BusinessEntityConstants.PRIMARY_ADDRESS_POSTAL_ADDRESS + "/" + BusinessEntityConstants.PRIMARY_ADDRESS_CNTRY_CD);
		
		// Gather Changed Attributes
		List<String> changedAttributes = getChangedAttributes(inputSdoChangeSummary, promoteSdoChangeSummary);
		
		// Get a condensed list of all of the Business Units
		List<String> businessUnits = getBusinessUnits(inputPromoteCompanyCodeList, promoteCompanyCodeList);
		List<String> subTeams = null;
		boolean filterOnSubTeam = filterOnSubTeam(businessEntity, businessUnits);
		if (filterOnSubTeam) {
			subTeams = getSubTeams(inputPromoteCompanyCodeList, promoteCompanyCodeList);
		}
		
		// Call BES API to gather all AttributeApprovers
		List<DataObject> attributeApprovers = getAttributeApprovers(changedAttributes, callContext, helperContext, 
				besClient, businessUnits, businessEntity,
				filterOnSubTeam, subTeams, cntryCd);
		
		if (attributeApprovers == null || attributeApprovers.isEmpty())
			return null;
		
		List<DataObject> filteredAttributeApprovers = verifyAttrApprsWthType(attributeApprovers, inputSdoChangeSummary, 
				promoteSdoChangeSummary, inputSDO, promotePreviewSDO);
		
		// Gather the Document Lists
		List<DataObject> inputPromoteDocumentList = null;
		List<DataObject> promoteDocumentList = null;
		
		if (isSupplier) {
			inputPromoteDocumentList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.SUPPLIER_DOCUMENTS, inputSDO, helperContext);
			promoteDocumentList = promoteSDOBe != null ? promoteSDOBe.getList(BusinessEntityConstants.SUPPLIER_DOCUMENTS + "/item") : null;
		} else {
			inputPromoteDocumentList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.DOCUMENTS, inputSDO, helperContext);
			promoteDocumentList = promoteSDOBe != null ? promoteSDOBe.getList(BusinessEntityConstants.DOCUMENTS + "/item") : null;
		}
		
		// Get a condensed list of all added Document Types
		List<String> addedDocTypes = getAddedDocTypes(inputPromoteDocumentList, promoteDocumentList, inputSdoChangeSummary, promoteSdoChangeSummary, isSupplier);
		
		logger.info("ADDED DOC TYPES: " + addedDocTypes);
		return validateDocuments(addedDocTypes, 
				filteredAttributeApprovers, helperContext.getDataFactory(), businessEntity,
				changedAttributes, inputSDO,
				inputSdoChangeSummary, promotePreviewSDO);
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
	
	
	/**
	 * This method calls the BES API to get the list of Attribute Approvers based on the Business Unit and Attributes Changed
	 * 
	 * @param changedAttributes
	 * @param callContext
	 * @param helperContext
	 * @param besClient
	 * @param businessUnits
	 * @return
	 */
	public List<DataObject> getAttributeApprovers(List<String> changedAttributes, CallContext callContext, HelperContext helperContext, 
			CompositeServiceClient besClient, List<String> businessUnits, String businessEntity, 
			boolean filterOnSubTeam, List<String> subTeams, String cntryCd) {
		if (changedAttributes == null || changedAttributes.isEmpty())
			return null;
		String splrBsnsTyp = businessEntity.contains("NonTrade") ? "NonTrade" : "Trade";
		String filter = "";
		String rolesSearch = "";
		String subTeam = "";
		
		if (RoleHelper.isSupplier(roles)) {
			rolesSearch = "Supplier";
		} else {
			rolesSearch = String.join(",", roles);
		}
		
		if (splrBsnsTyp.equals("NonTrade")) {
			filter = "splrBsnsTyp= " + splrBsnsTyp + " AND " + "SupplierAttributeApprover.attributePath IN [" + String.join(",", changedAttributes) + "] "
			+ " AND SupplierAttributeApprover.approvedRoles IN [" + String.join(",", rolesSearch) + "] AND SupplierAttributeApprover.reqDocUpload=Y AND cntry=" + cntryCd;
		} else {
			filter = "splrBsnsTyp= " + splrBsnsTyp + " AND " + "SupplierAttributeApprover.attributePath IN [" + String.join(",", changedAttributes) 
			+ "] AND subBsnsUnit IN [" + String.join(",", businessUnits) 
			+ "] AND SupplierAttributeApprover.approvedRoles IN [" + String.join(",", rolesSearch) + "] AND SupplierAttributeApprover.reqDocUpload=Y";
			if (filterOnSubTeam && subTeams != null) {
				filter += " AND subTeam IN [" + String.join(",", subTeams) + "]";
			}
		}
		
//		filter = filter.replace("|", "%7C");
		
		
		logger.info("FILTER: " + filter);
		DataObject businessUnitAttributeApprovers = businessEntityServiceClient.searchBE(callContext, besClient, helperContext, Constants.BE_SUPPLIER_BU_APPROVER, filter);
		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, Constants.BE_SUPPLIER_BU_APPROVER, businessUnitAttributeApprovers);
		List<DataObject> buAttrAprvList = businessUnitAttributeApprovers.getList("object/item");
		if (buAttrAprvList == null || buAttrAprvList.isEmpty())
			return null;
		
		List<String> rowids = buAttrAprvList.stream().map(buAttrAprv -> buAttrAprv.getString(Constants.BE_SUPPLIER_BU_APPROVER + "/" + Constants.ROWID_OBJECT) ).collect(Collectors.toList());
		
		filter = "bsnsUnitAprvId IN [" + String.join(",", rowids)  
					+ "] AND attributePath IN [" + String.join(",", changedAttributes) 
					+ "] AND approvedRoles IN [" + String.join(",", rolesSearch) + "] AND reqDocUpload=Y";
		
		DataObject attributeApprovers = businessEntityServiceClient.searchBE(callContext, besClient, helperContext, Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER, filter);
		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER, attributeApprovers);

		return attributeApprovers.getList("object/item");
	}
	
	
	
	/**
	 * Get all changed attributes
	 * 
	 * @param inputSdoChangeSummary
	 * @param promoteSdoChangeSummary
	 * @return
	 */
	public List<String> getChangedAttributes(SDOChangeSummary inputSdoChangeSummary, SDOChangeSummary promoteSdoChangeSummary) {
		List<String> changedAttributes = new ArrayList<String>();
		
		
		for (DataObject dataObject : (List<DataObject>)inputSdoChangeSummary.getChangedDataObjects()) {
			
			changedAttributes.addAll(getChangedAttributePaths(dataObject, inputSdoChangeSummary));
		}
		
		if (promoteSdoChangeSummary != null) {
			for (DataObject dataObject : (List<DataObject>) promoteSdoChangeSummary.getChangedDataObjects()) {
				changedAttributes.addAll(getChangedAttributePaths(dataObject, promoteSdoChangeSummary));
			}
		}
		
		return changedAttributes;
	}
	
	/**
	 * Get all changed attributes for a specific Data Object
	 * @param dataObject
	 * @param sdoChangeSummary
	 * @return
	 */
	public List<String> getChangedAttributes(DataObject dataObject, SDOChangeSummary sdoChangeSummary) {
		if (dataObject == null || sdoChangeSummary == null)
			return null;
		
		
		for (Setting oldValue : (List<Setting>)sdoChangeSummary.getOldValues(dataObject)) {
			oldValue.getProperty().getContainingType().getName().replaceAll(".Root", "");
		}
		
		List<String> changedAttributes = sdoChangeSummary.getOldValues(dataObject) == null ? new ArrayList<String>() 
				: (List<String>) sdoChangeSummary.getOldValues(dataObject).stream().map(oldValue -> {
					return ((Setting)oldValue).getProperty().getName();
				}).collect(Collectors.toList());
		changedAttributes.addAll(sdoChangeSummary.getUnsetProps(dataObject));

		return changedAttributes;
	}
	
	/**
	 * Get a list of all business units onboarded to the supplier.
	 * @param inputPromoteCompanyCodeList
	 * @param promoteCompanyCodeList
	 * @return List<String> of all Business Unit Codes
	 */
	public List<String> getBusinessUnits(List<DataObject> inputPromoteCompanyCodeList, List<DataObject> promoteCompanyCodeList) {
		List<String> businessUnits = new ArrayList<String>();
		if (inputPromoteCompanyCodeList == null || inputPromoteCompanyCodeList.isEmpty())
			return businessUnits;
		
		for (DataObject inputPromoteCompanyCode : inputPromoteCompanyCodeList) {
			String inputPromoteCompanyCodeRowid = inputPromoteCompanyCode.getString(BusinessEntityConstants.ROWID_OBJECT);
			DataObject promoteCompanyCode = null;
			if (inputPromoteCompanyCodeRowid != null && promoteCompanyCodeList != null)
				promoteCompanyCode = dataObjectHelperContext.getDataObjectSearcher()
					.searchDataObjectList(promoteCompanyCodeList, inputPromoteCompanyCodeRowid);
			
			String businessUnit = vendorSDOHelper.getString(inputPromoteCompanyCode, promoteCompanyCode, 
						BusinessEntityConstants.COMPANY_CODE_BSNS_UNIT + "/" + BusinessEntityConstants.COMPANY_CODE_BSNS_UNIT_CD);
			if (businessUnit != null)
				businessUnits.add(businessUnit);
			
		}
		
		return businessUnits;
	}
	
	/**
	 * Get a list of all business units onboarded to the supplier.
	 * @param inputPromoteCompanyCodeList
	 * @param promoteCompanyCodeList
	 * @return List<String> of all Business Unit Codes
	 */
	public List<String> getSubTeams(List<DataObject> inputPromoteCompanyCodeList, List<DataObject> promoteCompanyCodeList) {
		if (inputPromoteCompanyCodeList == null || inputPromoteCompanyCodeList.isEmpty())
			return null;
		
		List<String> subTeams = inputPromoteCompanyCodeList.parallelStream().map(inputPromoteCompanyCode -> {
			if (inputPromoteCompanyCode == null)
				return null;
			
			String subTeam = null;
			String inputPromoteCompanyCodeRowid = inputPromoteCompanyCode.getString(Constants.ROWID_OBJECT);
			DataObject promoteCompanyCode = null;
			if (inputPromoteCompanyCodeRowid != null) {
				promoteCompanyCode = dataObjectHelperContext.getDataObjectSearcher()
					.searchDataObjectList(promoteCompanyCodeList, inputPromoteCompanyCodeRowid);
			}
			subTeam = vendorSDOHelper.getString(inputPromoteCompanyCode, promoteCompanyCode, 
					BusinessEntityConstants.COMPANY_CODE_SUB_TEAM + "/" + BusinessEntityConstants.COMPANY_CODE_SUB_TEAM_CD);
			if (subTeam == null)
				return null;
			return subTeam;
						
		}).collect(Collectors.toList());
		
		return subTeams;
	}
	
	private boolean isSendBackOnDocSection(DataObject inputSDOBe, Map<String, Object> inParams) {
		String interactionId = (String) inParams.get(Constants.INTERACTION_ID);
		return interactionId != null && RoleHelper.isSupplier(roles) && inputSDOBe.isSet(BusinessEntityConstants.SUPPLIER_DOCUMENTS + "/item");
	}
	
	public List<ValidationError> validateDocuments(List<String> addedDocTypes,
			List<DataObject> attributeApprovers, DataFactory dataFactory, String businessEntity,
			List<String> changedAttributes, DataObject inputSDO,
			SDOChangeSummary inputSdoChangeSummary, DataObject promoteSDO) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		boolean isSupplier = RoleHelper.isSupplier(roles);
		List<String> docsNotUploaded = new ArrayList<String>();
		Map<String, ArrayList<String>> docsNotUploadedMap = new HashMap<String, ArrayList<String>>();
		
		if (attributeApprovers == null || attributeApprovers.isEmpty())
			return validationErrors;
		
		
		// Get a list of all required document types that have not been uploaded by the user.
		for (DataObject attributeApprover : attributeApprovers) {
			String attribute = attributeApprover.getString(Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER + "/attribute");
			String initiator = attributeApprover.getString(Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER + "/approvedRoles");
			String attributeLabel = attributeApprover.getString(Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER + "/attributeLabel");
			
			logger.info("ATTRIBUTE APPROVER: " + attributeApprover);
			List<String> requiredDocTypes = getRequiredDocTypes(attributeApprover, inputSDO, promoteSDO, inputSdoChangeSummary);

			for (String requiredDocType : requiredDocTypes) {
				logger.info("REQUIRED DOC TYPE: " + requiredDocType);
				
				if (addedDocTypes == null || !addedDocTypes.contains(requiredDocType)) {
					docsNotUploaded.add(requiredDocType);
					if (!docsNotUploadedMap.containsKey(requiredDocType)) {
						docsNotUploadedMap.put(requiredDocType, new ArrayList<String>());
					}
					if (!docsNotUploadedMap.get(requiredDocType).contains(attributeLabel)) {
						docsNotUploadedMap.get(requiredDocType).add(attributeLabel);	
					}
				}	
			}			
		}
		
		// If not all documents have been uploaded, set the error message.
		if (!docsNotUploaded.isEmpty()) {
			for (String docNotUploaded : docsNotUploadedMap.keySet()) {
				if (isSupplier) {
					validationErrors.add(
							createError(String.format(ErrorConstants.REQ_DOC_UPLOAD_ERROR_CODE, docNotUploaded), 
											String.format(ErrorConstants.REQ_DOC_UPLOAD_ERROR_MESSAGE, docNotUploaded, docNotUploaded), 
										businessEntity + "." + BusinessEntityConstants.SUPPLIER_DOCUMENTS, 
										dataFactory));
				} else {
					validationErrors.add(createError(ErrorConstants.REQ_DOC_UPLOAD_ERROR_CODE, 
													 String.format(ErrorConstants.INTERNAL_REQ_DOC_UPLOAD_ERROR_MESSAGE,
															 docNotUploaded,
															 String.join(",", docsNotUploadedMap.get(docNotUploaded))), 
													 businessEntity + "." + BusinessEntityConstants.DOCUMENTS, 
													 dataFactory));
				}
			}
		}
		
		return validationErrors;
	}
	
	
	private List<DataObject> verifyAttrApprsWthType(List<DataObject> attributeApprovers, SDOChangeSummary inputSdoChangeSummary,
			SDOChangeSummary promoteSdoChangeSummary, DataObject inputSDO, DataObject promoteSDO) {
		List<DataObject> filteredAttributeApprovers = new ArrayList<DataObject>();
		
		attributeApprovers.parallelStream().forEach(attributeApprover -> {
			if (attributeApprover == null)
				return;
			
			if (attributeApprover.getString("ExSupplierAttributeApprover/hasTyp") == null) {
				filteredAttributeApprovers.add(attributeApprover);	
				return;
			}
			
			boolean hasTyp = attributeApprover.getString("ExSupplierAttributeApprover/hasTyp").equals("Y");
			String attributePath = attributeApprover.getString("ExSupplierAttributeApprover/attributePath");
			String attributeTypeFieldNm = attributeApprover.getString("ExSupplierAttributeApprover/typFieldNm"); // I.E. docType/docTypCd
			String attributeTypeVal = attributeApprover.getString("ExSupplierAttributeApprover/typVal");
			if (!hasTyp || 
					verifyAttrApprWthType(attributePath, attributeTypeFieldNm, attributeTypeVal, inputSDO, promoteSDO))
				filteredAttributeApprovers.add(attributeApprover);	
		});
		
		return filteredAttributeApprovers;
		
	}
	
	private boolean verifyAttrApprWthType(String attributePath, String attributeTypeFieldNm, String attributeTypeVal,
			DataObject inputSDO, DataObject promoteSDO) {
		String[] attributePathSplit = attributePath.split("\\.", 2);
		// I.E ExTradeSupplier.TaxInformation.taxNum

		// TaxInformation.taxNum
		
		if (attributePathSplit.length == 1) {
			String typeValue = vendorSDOHelper.getString(inputSDO, promoteSDO, attributeTypeFieldNm);
			if (typeValue != null && attributeTypeVal.equals(typeValue))
				return true;
			else
				return false;
			
		} else {

			boolean isChildList = inputSDO.getList(attributePathSplit[0] + "/item") != null;

			DataObject inputChild, promoteChild = null;
			String remainingPath = attributePathSplit[1];
			if(isChildList) {
				List<DataObject> inputChildList = inputSDO.getList(attributePathSplit[0] + "/item");
				List<DataObject> promoteChildList = promoteSDO != null ? promoteSDO.getList(attributePathSplit[0] + "/item") : null;
				for (int i = 0; i < inputChildList.size(); i++) {
					inputChild = inputChildList.get(i);
					String inputChildRowid = inputChild.getString(Constants.ROWID_OBJECT);
					
					promoteChild = inputChildRowid != null && promoteChildList != null ? 
							dataObjectHelperContext.getDataObjectSearcher()
							.searchDataObjectList(promoteChildList, inputChildRowid) : null;
					
					boolean valid = verifyAttrApprWthType(remainingPath, attributeTypeFieldNm, attributeTypeVal, 
							inputChild, promoteChild);
					if (valid)
						return true;
				}
				return false;
			} else {
				inputChild = inputSDO.getDataObject(attributePathSplit[0]);
				promoteChild = promoteSDO != null ? inputSDO.getDataObject(attributePathSplit[0]) : null;
				return verifyAttrApprWthType(remainingPath, attributeTypeFieldNm, attributeTypeVal, 
						inputChild, promoteChild);
			}
		}
	}
	
	/**
	 * Get all changed attributes for a specific Data Object
	 * @param dataObject
	 * @param sdoChangeSummary
	 * @return
	 */
	public List<String> getChangedAttributePaths(DataObject dataObject, SDOChangeSummary sdoChangeSummary) {
		if (dataObject == null || sdoChangeSummary == null)
			return null;
		
		String dataObjectTypeName = dataObject.getType().getName();
		
//		for (Setting oldValue : (List<Setting>)sdoChangeSummary.getOldValues(dataObject)) {
//			oldValue.getProperty().getContainingType().getName().replaceAll(".Root", "");
//		}
		
		List<String> changedAttributePaths = sdoChangeSummary.getOldValues(dataObject) == null ? new ArrayList<String>() 
				: (List<String>) sdoChangeSummary.getOldValues(dataObject).stream().map(oldValue -> {
					String path = ((Setting)oldValue).getProperty().getContainingType().getName().replaceAll(".Root", "");
					path += "." + ((Setting)oldValue).getProperty().getName();
					return path;
				}).collect(Collectors.toList());
		
		List<String> unsetPropPaths = (List<String>) sdoChangeSummary.getUnsetProps(dataObject).stream().map(unsetProp -> dataObjectTypeName + "." + unsetProp).collect(Collectors.toList());
		
		logger.info("UNSET PROP PATHS: " + unsetPropPaths);
		changedAttributePaths.addAll(unsetPropPaths);
		
		for (DataObject createdDo : (List<DataObject>)sdoChangeSummary.getCreated()) {
			String createdPath = createdDo.getType().getName();

			for (Property property : (List<Property>) createdDo.getInstanceProperties()) {

				if (property != null && createdDo.isSet(property)) {
					changedAttributePaths.add(createdPath + "." + property.getName());
				}
				
			}
		}
		
		return changedAttributePaths;
	}
	
	/**
	 * This method retreives what the required doc types are in the Attribute Approver Rule
	 * If the Rule does not require an Associated Document type, then it will grabt the document type field in the attribute approver
	 * If the rule has an Associated Document Type, it will retrieve all of the types the user changed, 
	 * then cross reference the changed types with the associated Type Mapping tied to the Attribute Approver Rule
	 * 
	 * For Example, if the user changed the insurance number, then we would want to get the insurance type of the number the user changed, 
	 * then find the associated document types and require the user to upload them
	 * 
	 * @param attributeApprover
	 * @param inputSDO
	 * @param promoteSDO
	 * @param inputSdoChangeSummary
	 * @return
	 */
	public List<String> getRequiredDocTypes(DataObject attributeApprover, DataObject inputSDO, DataObject promoteSDO, 
			SDOChangeSummary inputSdoChangeSummary) {
		logger.info("GET REQUIRED DOC TYPES");
		List<String> requiredDocTypes = new ArrayList<String>();
		if (attributeApprover.getString(Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER + "/" + "hasAssociatedTyp") == null ||
				attributeApprover.getString(Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER + "/" + "hasAssociatedTyp").equals("N")) {
			requiredDocTypes.add(attributeApprover.getString(Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER + "/" + BusinessEntityConstants.SUPPLIER_BU_APPR_DOCUMENT_DOC_TYP));			
		} else if (attributeApprover.getString(Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER + "/" + "hasAssociatedTyp").equals("Y")) {
			logger.info("HAS ASSOCIATED TYPE");
			String typFieldNm = attributeApprover.getString(Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER + "/typFieldNm");
			
			List<String> typesInInputSdo = new ArrayList<String>();
			getTypes(typFieldNm, inputSDO, promoteSDO, typesInInputSdo);
			logger.info("TYPES IN SDO 1: " + typesInInputSdo.get(0));
			
			List<DataObject> typMappingItems = attributeApprover.getList(Constants.BE_SUPPLIER_ATTRIBUTE_APPROVER + "/" + "typMapping" + "/" + "typMappingItms" + "/item");
			logger.info("TYPE MAPPING ITEMS: " + typMappingItems);
			if (typMappingItems == null || typMappingItems.isEmpty())
				return null;
			for (DataObject typMappingItem : typMappingItems) {
				if (typMappingItem == null || typMappingItem.getString("typOneCd") == null || typMappingItem.getString("typTwoCd") == null)
					continue;
				
				logger.info("TYPE MAPPING ONE: " +  typMappingItem.getString("typOneCd"));
				logger.info("TYPE MAPPING TWO: " + typMappingItem.getString("typTwoCd"));
				// See if any of the types added to the supplier record equals the type mapping
				if (typesInInputSdo.contains(typMappingItem.getString("typOneCd"))) {
					requiredDocTypes.add(typMappingItem.getString("typTwoCd"));
				}
			}
		}

		return requiredDocTypes;
	}
	
	
	/**
	 * This method retrieves all of the types in the input sdo. if the type field is in a one to many child object, the returning arraylist will
	 * have multiple items.
	 * @param attributePath
	 * @param inputSDO
	 * @param promoteSDO
	 * @param typesInSdo
	 */
	private void getTypes(String attributePath,
			DataObject inputSDO, DataObject promoteSDO, List<String> typesInSdo) {
		String[] attributePathSplit = attributePath.split("\\.", 2);
		logger.info("ATTRIBUTE PATH:  " + attributePath);
		// I.E ExTradeSupplier.TaxInformation.taxNum

		// TaxInformation.taxNum
		
		if (attributePathSplit.length == 1) {
			logger.info("LENGTH 1");
			logger.info("TYPE CODE: " + vendorSDOHelper.getString(inputSDO, promoteSDO, attributePath));
			typesInSdo.add(vendorSDOHelper.getString(inputSDO, promoteSDO, attributePath));
			return;
		} else {

			boolean isChildList = inputSDO.getList(attributePathSplit[0] + "/item") != null;

			DataObject inputChild, promoteChild = null;
			String remainingPath = attributePathSplit[1];
			if(isChildList) {
				List<DataObject> inputChildList = inputSDO.getList(attributePathSplit[0] + "/item");
				List<DataObject> promoteChildList = promoteSDO != null ? promoteSDO.getList(attributePathSplit[0] + "/item") : null;
				for (int i = 0; i < inputChildList.size(); i++) {
					inputChild = inputChildList.get(i);
					String inputChildRowid = inputChild.getString(Constants.ROWID_OBJECT);
					
					promoteChild = inputChildRowid != null && promoteChildList != null ? 
							dataObjectHelperContext.getDataObjectSearcher()
							.searchDataObjectList(promoteChildList, inputChildRowid) : null;
					
					getTypes(remainingPath, inputChild, promoteChild, typesInSdo);
					
				}
				return;
			} else {
				inputChild = inputSDO.getDataObject(attributePathSplit[0]);
				promoteChild = promoteSDO != null ? inputSDO.getDataObject(attributePathSplit[0]) : null;
				getTypes(remainingPath, inputChild, promoteChild, typesInSdo);
				return;
			}
		}
	}
	
	/**
	 * Get all types of documents uploaded
	 * 
	 * @param inputPromoteDocumentList
	 * @param promoteDocumentList
	 * @param inputSdoChangeSummary
	 * @param promoteSdoChangeSummary
	 * @return
	 */
	public List<String> getAddedDocTypes(List<DataObject> inputPromoteDocumentList, 
			List<DataObject> promoteDocumentList, SDOChangeSummary inputSdoChangeSummary, SDOChangeSummary promoteSdoChangeSummary,
			boolean isSupplier) {
		List<String> addedDocTypes = new ArrayList<String>();
		if (inputPromoteDocumentList == null || inputPromoteDocumentList.isEmpty())
			return addedDocTypes;
		
		String docTyp = isSupplier ? BusinessEntityConstants.SUPPLIER_DOCUMENTS_DOC_TYP : BusinessEntityConstants.DOCUMENT_DOC_TYP;
		
		for (DataObject inputPromoteDocument : inputPromoteDocumentList) {
			
			if (inputSdoChangeSummary.isCreated(inputPromoteDocument)) {
				
				String inputPromoteDocumentRowid = inputPromoteDocument.getString(BusinessEntityConstants.ROWID_OBJECT);
				DataObject promoteDocument = null;
				if (inputPromoteDocumentRowid != null && promoteDocumentList != null)
					promoteDocument = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteDocumentList, inputPromoteDocumentRowid);
				String docType = vendorSDOHelper.getString(inputPromoteDocument, promoteDocument, docTyp);
				if (docType != null)
					addedDocTypes.add(docType);
			} else {
				
				String inputPromoteDocumentRowid = inputPromoteDocument.getString(BusinessEntityConstants.ROWID_OBJECT);
				DataObject promoteDocument = null;
				if (inputPromoteDocumentRowid != null && promoteDocumentList != null)
					promoteDocument = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteDocumentList, inputPromoteDocumentRowid);
				if (promoteSdoChangeSummary.isCreated(promoteDocument)) {
					String docType = promoteDocument.getString(docTyp);
					if (docType != null)
						addedDocTypes.add(docType);
				}
			}
		}
		return addedDocTypes;
	}
}
