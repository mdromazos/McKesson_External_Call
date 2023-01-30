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
 * Check the Back Order Indicator, if set to no then default Back Order Days to 10.
 **/
public class BackOrderIndAutomate extends Automate {

	@Override
	public ValidationError doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO,
			CallContext callContext, CompositeServiceClient besClient) {
		SDOChangeSummary inputSDOChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		
		// Grab Business Entity Data Object
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = promoteSDO.getDataObject(businessEntity);
		
		// Grab the list of taxes from both inputSDO and promoteSDO so we have the complete lists
		List<DataObject> inputPromotePurchOrgList = vendorSDOHelper.getCombinedListAutomation(inputSDOBe, promoteSDOBe, 
				BusinessEntityConstants.PURCHASE_ORG, inputSDO, helperContext);
		
		// Grab just the promote tax list for reference
		List<DataObject> promotePurchOrgList = promoteSDOBe.getList(BusinessEntityConstants.PURCHASE_ORG + "/item");
		
		if (inputPromotePurchOrgList == null)
			return null;
		
		// Loop through each Purchase Org and set Back Order Ind

		inputPromotePurchOrgList.parallelStream().forEach((inputPromotePurchOrg) -> {
			if (inputPromotePurchOrg != null) {
				String inputPromotePurchOrgRowid = inputPromotePurchOrg.getString(BusinessEntityConstants.ROWID_OBJECT);
				DataObject promotePurchOrg = null;
				if (inputPromotePurchOrgRowid != null && promotePurchOrgList != null)
					promotePurchOrg = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promotePurchOrgList, inputPromotePurchOrgRowid);

				
				String backOrderDays = vendorSDOHelper.getString(inputPromotePurchOrg, promotePurchOrg, BusinessEntityConstants.PURCH_ORG_BACK_ORDER_DAYS);
				boolean backOrderInd = backOrderDays != null && backOrderDays.equals("10");
						
				if (backOrderInd) {
					inputSDOChangeSummary.resumeLogging();
					inputSDOBe.setString(BusinessEntityConstants.PURCH_ORG_BACK_ORDER_DAYS, "Y");
					inputSDOChangeSummary.pauseLogging();
				}
			}
		});

		return null;
	}

}
