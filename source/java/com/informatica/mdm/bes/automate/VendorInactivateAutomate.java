package com.informatica.mdm.bes.automate;

import java.util.List;
import java.util.Map;

import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * If the Posting Block is set at the Supplier Table level, then loop through all Company Codes and set it there.
 * 
 * If a new Company Code is added and the Posting Block is set then set the new Company Code Posting Block
 * @author Matthew Dromazos
 *
 */
public class VendorInactivateAutomate extends Automate {
	public static final String VENDOR_STATUS_INACTIVE = "I";

	@Override
	public List<ValidationError> doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO, CallContext callContext,
			CompositeServiceClient besClient) {
		// TODO Auto-generated method stub
		
		SDOChangeSummary inputSdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		inputSdoChangeSummary.pauseLogging();
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = null;
		if (promoteSDO != null)
			promoteSDOBe = promoteSDO.getDataObject(businessEntity);
				
		String vendorStatus = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, BusinessEntityConstants.VENDOR_STATUS);
		
		if (vendorStatus == null || !vendorStatus.equals(VENDOR_STATUS_INACTIVE))
			return null;
		
		String centralPostingBlock = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, BusinessEntityConstants.CENTRAL_POSTING_BLOCK);
		if (centralPostingBlock != null && centralPostingBlock.equals("N")) {
			inputSdoChangeSummary.resumeLogging();
			inputSDOBe.setString(BusinessEntityConstants.CENTRAL_POSTING_BLOCK, "Y");
			inputSdoChangeSummary.pauseLogging();
		}
		
		String purchBlock = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, BusinessEntityConstants.PURCHASING_BLOCK);
		if (purchBlock != null && purchBlock.equals("N")) {
			inputSdoChangeSummary.resumeLogging();
			inputSDOBe.setString(BusinessEntityConstants.PURCHASING_BLOCK, "Y");
			inputSdoChangeSummary.pauseLogging();
		}

		setPOrgPurchasingBlock(inputSDOBe, promoteSDOBe, helperContext, inputSdoChangeSummary, inputSDO);
		setCompanyCodePostingBlock(inputSDOBe, promoteSDOBe, helperContext, inputSdoChangeSummary, inputSDO);
		
		return null;
	}
	
	
	/**
	 * Set the Purchase Block of every Purchase Org to Y.  Only sets if the existing value is N
	 * 
	 * @param inputSDOBe
	 * @param promoteSDOBe
	 * @param helperContext
	 * @param inputSdoChangeSummary
	 */
	public void setPOrgPurchasingBlock(DataObject inputSDOBe, DataObject promoteSDOBe, HelperContext helperContext, SDOChangeSummary inputSdoChangeSummary, DataObject inputSDO) {
		List<DataObject> inputPromotePurchaseOrgList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.PURCHASE_ORG, inputSDO, helperContext);
		List<DataObject> promotePurchaseOrgList = promoteSDOBe != null ? promoteSDOBe.getList(BusinessEntityConstants.PURCHASE_ORG + "/item") : null;
		if (inputPromotePurchaseOrgList == null || inputPromotePurchaseOrgList.isEmpty())
			return;
		
		// Loop through each porg and set PurchaseBlock if it is N
		for (DataObject inputPromotePurchaseOrg : inputPromotePurchaseOrgList) {
			if (inputPromotePurchaseOrg == null)
				continue;
			
			
			String inputPromotePurchaseOrgRowid = inputPromotePurchaseOrg.getString(BusinessEntityConstants.ROWID_OBJECT);
			DataObject promotePurchaseOrg = null;
			if (promotePurchaseOrgList != null && inputPromotePurchaseOrgRowid != null)
				promotePurchaseOrg = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promotePurchaseOrgList, inputPromotePurchaseOrgRowid);
			
			String purchBlock = vendorSDOHelper.getString(inputPromotePurchaseOrg, promotePurchaseOrg, BusinessEntityConstants.PURCH_ORG_PURCH_BLOCK);
			if (purchBlock == null || purchBlock.equals("N")) {
				inputSdoChangeSummary.resumeLogging();
				inputPromotePurchaseOrg.setString(BusinessEntityConstants.PURCH_ORG_PURCH_BLOCK, "Y");
				inputSdoChangeSummary.pauseLogging();
			}
		}
	}
	
	public void setCompanyCodePostingBlock(DataObject inputSDOBe, DataObject promoteSDOBe, HelperContext helperContext, SDOChangeSummary inputSdoChangeSummary, DataObject inputSDO) {
		List<DataObject> inputPromoteCompanyCodeList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.COMPANY_CODE, inputSDO, helperContext);
		List<DataObject> promoteCompanyCodeList = promoteSDOBe.getList(BusinessEntityConstants.COMPANY_CODE + "/item");
		if (inputPromoteCompanyCodeList == null || inputPromoteCompanyCodeList.isEmpty())
			return;
		
		// Loop through each Company Code and set Posting Block to Y if it is currently N
		for (DataObject inputPromoteCompanyCode : inputPromoteCompanyCodeList) {
			if (inputPromoteCompanyCode == null)
				continue;
			
			String inputPromoteCompanyCodeRowid = inputPromoteCompanyCode.getString(BusinessEntityConstants.ROWID_OBJECT);
			DataObject promoteCompanyCode = null;
			if (promoteCompanyCodeList != null && inputPromoteCompanyCodeRowid != null)
				promoteCompanyCode = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteCompanyCodeList, inputPromoteCompanyCodeRowid);
			
			String postingBlock = vendorSDOHelper.getString(inputPromoteCompanyCode, promoteCompanyCode, BusinessEntityConstants.COMPANY_CODE_POSTING_BLOCK);
			if (postingBlock == null || postingBlock.equals("N")) {
				inputSdoChangeSummary.resumeLogging();
				inputPromoteCompanyCode.setString(BusinessEntityConstants.COMPANY_CODE_POSTING_BLOCK, "Y");
				inputSdoChangeSummary.pauseLogging();
			}
		}
	}
}
