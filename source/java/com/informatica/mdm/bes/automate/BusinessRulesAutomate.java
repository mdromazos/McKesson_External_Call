package com.informatica.mdm.bes.automate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.service.BusinessEntityServiceClient;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.ChangeSummary.Setting;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author Matthew Dromazos
 *
 ** Maintenance Levels
 *	1 - Purchase Org
 *	2 - Company Code
 *	3 - Supplier
 */
public class BusinessRulesAutomate extends Automate {
	private static Logger logger = Logger.getLogger(ProfileAutomate.class.getName());

	BusinessEntityServiceClient businessEntityServiceClient = new BusinessEntityServiceClient();
	public static final String PROPERTY = "prop";
	public static final String DEFAULT = "dflt";
	public static final String GLOBAL_PROPERTY = "glblProp";
	public static final String GLOBAL_DEFAULT = "glblDflt";
	public static final String ATTR_NM = "attrNm";
	public static final String REQUIRED = "Required";
	public static final String DISPLAY = "Display";
	public static final String DEPENDS_ON = "dependsOn";
	
	private static final boolean ONLY_ON_CREATE = false;

	
	@SuppressWarnings("unchecked")
	@Override
	public List<ValidationError> doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO, CallContext callContext,
			CompositeServiceClient besClient) {
		List<ValidationError> validationErrors = null;
		try {
			DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
			DataObject promoteSDOBe = promoteSDO != null ? promoteSDO.getDataObject(businessEntity) : null;
			SDOChangeSummary sdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
			
			List<DataObject> inputPromoteCompanyCodeList = null,  promoteCompanyCodeList = null;			
			List<DataObject> inputPromotePurchaseOrgList = null, promotePurchaseOrgList = null;
			
			if (ONLY_ON_CREATE) {
				if (inputSDOBe.isSet(BusinessEntityConstants.COMPANY_CODE + "/item")) {
					if (sdoChangeSummary.isCreated(inputSDOBe)) {
						inputPromoteCompanyCodeList = (List<DataObject>) inputSDOBe.getList(BusinessEntityConstants.COMPANY_CODE + "/item");
					} else {
						inputPromoteCompanyCodeList = (List<DataObject>) inputSDOBe.getList(BusinessEntityConstants.COMPANY_CODE + "/item")
								.stream().filter(cmpnyCd -> {
									return cmpnyCd != null && sdoChangeSummary.isCreated((DataObject) cmpnyCd);
								}).collect(Collectors.toList());		
					}
				}
				if (inputSDOBe.isSet(BusinessEntityConstants.PURCHASE_ORG + "/item")) {
					if (sdoChangeSummary.isCreated(inputSDOBe)) {
						inputPromotePurchaseOrgList = (List<DataObject>) inputSDOBe.getList(BusinessEntityConstants.PURCHASE_ORG + "/item");
					} else {
						inputPromotePurchaseOrgList = (List<DataObject>) inputSDOBe.getList(BusinessEntityConstants.PURCHASE_ORG + "/item")
								.stream().filter(prchOrg -> {
									return prchOrg != null && sdoChangeSummary.isCreated((DataObject) prchOrg);
								}).collect(Collectors.toList());
					}
				}
			} else {
				inputPromoteCompanyCodeList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.COMPANY_CODE, inputSDO, helperContext);
				promoteCompanyCodeList = promoteSDOBe != null ? promoteSDOBe.getList(BusinessEntityConstants.COMPANY_CODE + "/item") : null;
				
				inputPromotePurchaseOrgList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.PURCHASE_ORG, inputSDO, helperContext);
				promotePurchaseOrgList = promoteSDOBe != null ? promoteSDOBe.getList(BusinessEntityConstants.PURCHASE_ORG + "/item") : null;			
			}

			String accntGrp = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, BusinessEntityConstants.ACCNT_GRP_CD);
			String splrBsnsTyp = businessEntity.contains("NonTrade") ? "Non-Trade" : "Trade";

			List<DataObject> businessRules = getBusinessRules(callContext, besClient, helperContext, accntGrp, splrBsnsTyp);
			if (businessRules == null || businessRules.isEmpty())
				return null;
			
			List<DataObject> companyCodeRuleAttributes = getCompanyCodeRules(businessRules);
			List<DataObject> prchOrgRuleAttributes = getPurchaseOrgRules(businessRules);
			List<DataObject> rootRules = getRootRules(businessRules, inputPromoteCompanyCodeList, promoteCompanyCodeList);

			validationErrors = new ArrayList<ValidationError>();
			List<ValidationError> cmpnyCdValidationErrors = validateChild(inputSDOBe, inputPromoteCompanyCodeList, promoteCompanyCodeList, companyCodeRuleAttributes,
					helperContext.getDataFactory(), sdoChangeSummary, businessEntity, "cmpnyCd/coCd", BusinessEntityConstants.COMPANY_CODE);
			
			List<ValidationError> prchOrgValidationErrors = validateChild(inputSDOBe, inputPromotePurchaseOrgList, promotePurchaseOrgList, prchOrgRuleAttributes,
					helperContext.getDataFactory(), sdoChangeSummary, businessEntity, BusinessEntityConstants.PURCHASE_ORG_PORG_CD, BusinessEntityConstants.PURCHASE_ORG);
			
			List<ValidationError> rootErrors = validateRoot(inputSDOBe, promoteSDOBe, rootRules, helperContext.getDataFactory(), sdoChangeSummary, businessEntity);
			
			validationErrors.addAll(cmpnyCdValidationErrors);
			validationErrors.addAll(prchOrgValidationErrors);
			validationErrors.addAll(rootErrors);
		} catch (Exception e) {
			logger.error("Error processing Business Rules: " + e.getStackTrace());
		}
		
		return validationErrors;
	}
	
	public List<ValidationError> validateRoot(DataObject inputSDOBe, DataObject promoteSDOBe, List<DataObject> businessRulesAttributeList, 
			DataFactory dataFactory, SDOChangeSummary sdoChangeSummary, String businessEntity) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		
		if (businessRulesAttributeList == null || businessRulesAttributeList.isEmpty())
			return validationErrors;
		
		for (DataObject businessRuleAttribute : businessRulesAttributeList) {
			if (businessRuleAttribute == null)
				continue;
			
			String attrNm = businessRuleAttribute.getString(ATTR_NM);
			String property = null;
			String dflt = null;
			property = businessRuleAttribute.getString(GLOBAL_PROPERTY);
			dflt = businessRuleAttribute.getString(GLOBAL_DEFAULT);
			
			// Only run default rules on a newly created vendor
			if (sdoChangeSummary.isCreated(inputSDOBe) && dflt != null) {
				if (attrNm.split("/").length > 1) {
					String lkpDataObjectNm = attrNm.split("/")[0];
					String lkpCdNm = attrNm.split("/")[1];
					if (inputSDOBe.getInstanceProperty(lkpDataObjectNm).isContainment()) {
						setDataObjectDefault(inputSDOBe, lkpDataObjectNm, lkpCdNm, dflt, sdoChangeSummary);
					}
				} else {
					sdoChangeSummary.resumeLogging();
					inputSDOBe.set(attrNm, dflt);
					sdoChangeSummary.pauseLogging();
				}
			}
			
			if (property != null && property.equals(REQUIRED)) {
				if (vendorSDOHelper.get(inputSDOBe, promoteSDOBe, attrNm) == null) {
					validationErrors.add(createError("CUSTOM-ERROR", "Attribute: " + attrNm + " is required to be populated.", businessEntity + "." + attrNm, dataFactory));
				}
			}
		}
		
		return validationErrors;
	}
	
	public List<ValidationError> validateChild(DataObject inputSDOBe, List<DataObject> inputPromoteChildList, List<DataObject> promoteChildList, List<DataObject> businessRulesAttributeList,
			DataFactory dataFactory, SDOChangeSummary sdoChangeSummary, String businessEntity, String mntncLvlFieldNm, String childName) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();

		if (inputPromoteChildList == null || inputPromoteChildList.isEmpty())
			return validationErrors;

		for (DataObject inputPromoteChild : inputPromoteChildList) {
			String inputPromoteChildRowid = inputPromoteChild.getString(BusinessEntityConstants.ROWID_OBJECT);
			DataObject promoteChild = null;
			if (inputPromoteChildRowid != null)
				promoteChild = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteChildList, inputPromoteChildRowid);
			List<ValidationError> brErrors = processBusinessRules(inputSDOBe, inputPromoteChild, promoteChild, businessRulesAttributeList,
					dataFactory, sdoChangeSummary, businessEntity, mntncLvlFieldNm, childName);
			if (brErrors != null)
				validationErrors.addAll(brErrors);
		}
		
		return validationErrors;
	}
	
	public List<ValidationError> processBusinessRules(DataObject inputSDOBe, DataObject inputChild, DataObject promoteChild, List<DataObject> businessRulesAttributeList, 
			DataFactory dataFactory, SDOChangeSummary sdoChangeSummary, String businessEntity, String mntncLvlFieldNm, String childName) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();

		if (businessRulesAttributeList == null || businessRulesAttributeList.isEmpty())
			return validationErrors;

		List<String> changedAttributes = getChangedAttributes(inputChild, sdoChangeSummary);
		for (DataObject businessRuleAttribute : businessRulesAttributeList) {
			String attrNm = businessRuleAttribute.getString(ATTR_NM);
			String property = null;
			String dflt = null;
			DataObject attributeRule = getAttributeRule(inputChild, promoteChild, businessRuleAttribute, mntncLvlFieldNm);
			if (attributeRule != null) {
				property = attributeRule.getString(PROPERTY);
				dflt = attributeRule.getString(DEFAULT);
			} else {
				// If there is no maintenance level rule then use the global
				property = businessRuleAttribute.getString(GLOBAL_PROPERTY);
				dflt = businessRuleAttribute.getString(GLOBAL_DEFAULT);
			}
			
			String dependsOn = businessRuleAttribute.getString(DEPENDS_ON);
			
			if (property != null && property.equals(REQUIRED)) {
				if (inputChild.get(attrNm) == null) {
					validationErrors.add(createError("CUSTOM-ERROR", "Attribute: " + attrNm + " is required to be populated.", businessEntity + "." + childName, dataFactory));
				}
			} else if (property != null && property.equals(DISPLAY)) {
				if (changedAttributes.contains(attrNm)) {
					validationErrors.add(createError("CUSTOM-ERROR", "Attribute: " + attrNm + " is display only for this Maintenance Level.", businessEntity + "." + childName, dataFactory));
				}
			}
			
			// Only run default rules on newly created Data Objects
			if ((sdoChangeSummary.isCreated(inputSDOBe) || sdoChangeSummary.isCreated(inputChild)) && dflt != null) {
				String dependsOnLkpVal = null;
				if (dependsOn != null) {
					dependsOnLkpVal = vendorSDOHelper.getString(inputChild, promoteChild, dependsOn);
					dflt = dependsOnLkpVal != null ? dependsOnLkpVal + "|" + dflt : dflt;
				}
				
				if (attrNm.split("/").length > 1) {
					String lkpDataObjectNm = attrNm.split("/")[0];
					String lkpCdNm = attrNm.split("/")[1];
					if (inputChild.getInstanceProperty(lkpDataObjectNm).isContainment()) {
						setDataObjectDefault(inputChild, lkpDataObjectNm, lkpCdNm, dflt, sdoChangeSummary);
					}
				} else {
					sdoChangeSummary.resumeLogging();
					inputChild.set(attrNm, dflt);
					sdoChangeSummary.pauseLogging();
				}
			}

		}
		return validationErrors;
	}
	
	public void setDataObjectDefault(DataObject inputPromoteChild, String lkpDataObjectNm, String lkpCdNm, String dflt, SDOChangeSummary sdoChangeSummary) {
		try {
			sdoChangeSummary.resumeLogging();
			DataObject lkpDataObject = inputPromoteChild.createDataObject(lkpDataObjectNm);
			lkpDataObject.set(lkpCdNm, dflt);
			sdoChangeSummary.pauseLogging();
		} catch (Exception e) {
			sdoChangeSummary.pauseLogging();
		}
	}
	
	public DataObject getAttributeRule(DataObject inputPromoteChild, DataObject promoteChild, DataObject businessRuleAttribute, String mntncLvlFieldNm) {
		String mntncLvlFieldVal = vendorSDOHelper.getString(inputPromoteChild, promoteChild, mntncLvlFieldNm);
		List<DataObject> attributeRules = businessRuleAttribute.getList("BusinessRuleAttributeRule/item");
		if (attributeRules == null || attributeRules.isEmpty() || mntncLvlFieldVal == null)
			return null;
		
		for (DataObject attributeRule : attributeRules) {
			String mntncLvlVal = attributeRule.getString("mntnceLvlValue");
			if (mntncLvlFieldVal.contains(mntncLvlVal)) {
				return attributeRule;
			}
		}
		
		return null;
	}
	
	
	public List<DataObject> getBusinessRules(CallContext callContext, CompositeServiceClient besClient, HelperContext helperContext, 
			String accntGrp, String splrBsnsTyp) {
		
		String filter = "BusinessRuleAssignment.accntGrp=" + accntGrp + " AND splrBsnsTyp=" + splrBsnsTyp;

		DataObject businessRulesReturn = businessEntityServiceClient.searchBE(callContext, besClient, helperContext, "ExBusinessRule", filter,100);
		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, "ExBusinessRule", businessRulesReturn);
		return businessRulesReturn.getList("object/item");		
	}
	
	public List<DataObject> getCompanyCodeRules(List<DataObject> businessRules) {
		List<DataObject> companyCodeRules = new ArrayList<DataObject>();
		if (businessRules == null || businessRules.isEmpty())
			return companyCodeRules;
		for (DataObject businessRule : businessRules) {
			List<DataObject> businessRuleAttributes = businessRule.getList("ExBusinessRule/BusinessRuleAttribute/item");
			for (DataObject businessRuleAttribute : businessRuleAttributes) {
				String mntncLvl = businessRuleAttribute.getString("mntncLvl");
				if (mntncLvl.equals("2")) {
					companyCodeRules.add(businessRuleAttribute);
				}
			}
		}
		return companyCodeRules;
	}
	
	public List<DataObject> getPurchaseOrgRules(List<DataObject> businessRules) {
		List<DataObject> prchOrgRules = new ArrayList<DataObject>();
		if (businessRules == null || businessRules.isEmpty())
			return prchOrgRules;
		for (DataObject businessRule : businessRules) {
			List<DataObject> businessRuleAttributes = businessRule.getList("ExBusinessRule/BusinessRuleAttribute/item");
			for (DataObject businessRuleAttribute : businessRuleAttributes) {
				String mntncLvl = businessRuleAttribute.getString("mntncLvl");
				if (mntncLvl.equals("1")) {
					prchOrgRules.add(businessRuleAttribute);
				}
			}
		}
		return prchOrgRules;
	}
	
	/**
	 * 
	 * @param businessRules
	 * @param inputPromoteCompanyCodeList
	 * @param promoteCompanyCodeList
	 * @return
	 */
	public List<DataObject> getRootRules(List<DataObject> businessRules, List<DataObject> inputPromoteCompanyCodeList, List<DataObject> promoteCompanyCodeList) {
		List<DataObject> rootRules = new ArrayList<DataObject>();
		if (businessRules == null || businessRules.isEmpty())
			return rootRules;
		
		// Get all company codes into a list
		
		List<String> companyCodes = new ArrayList<String>();
		for (DataObject inputPromoteCompanyCode : inputPromoteCompanyCodeList) {
			if (inputPromoteCompanyCode == null)
				continue;
			String companyCodeRowid = inputPromoteCompanyCode.getString(Constants.ROWID_OBJECT);
			DataObject promoteCompanyCode = null;
			if (companyCodeRowid != null && promoteCompanyCodeList != null)
				promoteCompanyCode = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteCompanyCodeList, companyCodeRowid);
			String companyCode = vendorSDOHelper.getString(inputPromoteCompanyCode, promoteCompanyCode, BusinessEntityConstants.COMPANY_CODE_COMPANY_CODE_CD_LKP);
			companyCode = companyCode != null ? companyCode.split("\\|")[1] : null;
			if (companyCode != null)
				companyCodes.add(companyCode);
		}
		
		for (DataObject businessRule : businessRules) {
			List<DataObject> businessRuleAttributes = businessRule.getList("ExBusinessRule/BusinessRuleAttribute/item");
			for (DataObject businessRuleAttribute : businessRuleAttributes) {
				String mntncLvl = businessRuleAttribute.getString("mntncLvl");
				if (mntncLvl.equals("3")) {
					
					List<DataObject> attributeRules = businessRuleAttribute.getList("BusinessRuleAttributeRule/item");
					
					
					if (attributeRules == null || attributeRules.isEmpty()) {
						rootRules.add(businessRuleAttribute);
					} else {
						for (DataObject attributeRule : attributeRules) {
							String mntncLvlVal = attributeRule.getString("mntnceLvlValue");
							if (companyCodes.contains(mntncLvlVal)) {
								rootRules.add(businessRuleAttribute);
							}
						}
					}
				}
			}
		}
		return rootRules;
	}
	
	public List<String> getChangedAttributes(SDOChangeSummary inputSdoChangeSummary, SDOChangeSummary promoteSdoChangeSummary) {
		List<String> changedAttributes = new ArrayList<String>();
		
		
		for (DataObject dataObject : (List<DataObject>)inputSdoChangeSummary.getChangedDataObjects()) {
			changedAttributes.addAll(getChangedAttributes(dataObject, inputSdoChangeSummary));
		}
		
		if (promoteSdoChangeSummary != null) {
			for (DataObject dataObject : (List<DataObject>)promoteSdoChangeSummary.getChangedDataObjects()) {
				changedAttributes.addAll(getChangedAttributes(dataObject, promoteSdoChangeSummary));
			}
		}
		
		return changedAttributes;
	}
	
	public List<String> getChangedAttributes(DataObject dataObject, SDOChangeSummary sdoChangeSummary) {
		List<String> changedAttributes = sdoChangeSummary.getOldValues(dataObject) == null ? new ArrayList<String>() 
				: (List<String>) sdoChangeSummary.getOldValues(dataObject).stream().map(oldValue -> {
					return ((Setting)oldValue).getProperty().getName();
				}).collect(Collectors.toList());
		changedAttributes.addAll(sdoChangeSummary.getUnsetProps(dataObject));
		return changedAttributes;
	}
	
	private boolean onboardedWith(List<DataObject> inputPromoteCompanyCodeList, List<DataObject> promoteCompanyCodeList, String companyCodeCompare) {
		for (DataObject inputPromoteCompanyCode : inputPromoteCompanyCodeList) {
			if (inputPromoteCompanyCode == null)
				continue;
			String companyCodeRowid = inputPromoteCompanyCode.getString(Constants.ROWID_OBJECT);
			DataObject promoteCompanyCode = null;
			if (companyCodeRowid != null && promoteCompanyCodeList != null)
				promoteCompanyCode = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteCompanyCodeList, companyCodeRowid);
			String companyCode = vendorSDOHelper.getString(inputPromoteCompanyCode, promoteCompanyCode, BusinessEntityConstants.COMPANY_CODE_COMPANY_CODE_CD);
			if (companyCodeCompare.equals(companyCode))
				return true;
		}
		return false;
	}

}
