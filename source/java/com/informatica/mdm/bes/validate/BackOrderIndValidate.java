package com.informatica.mdm.bes.validate;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.automate.Automate;
import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.ChangeSummary.Setting;
import commonj.sdo.helper.HelperContext;


/**
 * "If Back Order Indicator Set to No, then user cannot modity Back Order Days.  If they try to then throw an error.
 * 
 * If back order indicator set to Yes, then the Back Order Days must be between 10-16."
 * @author Matthew Dromazos
 *
 */
public class BackOrderIndValidate extends Validate {

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);
		SDOChangeSummary inputSDOChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();	
		
		if (inputSDO == null|| businessEntity.equals(Constants.BE_TRADE_REGIST_VIEW))
			return null;
		
		// Grab Business Entity Data Object
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
		
		// Grab the list of purchase org from both inputSDO and promoteSDO so we have the complete lists
		List<DataObject> inputPromotePurchOrgList = vendorSDOHelper.getCombinedListAutomation(inputSDOBe, promoteSDOBe, 
				BusinessEntityConstants.PURCHASE_ORG, inputSDO, helperContext);

		if (inputPromotePurchOrgList == null)
			return null;
		
		List<DataObject> promotePurchOrgList = null;
		if (promoteSDOBe != null)
			promotePurchOrgList = promoteSDOBe.getList(BusinessEntityConstants.PURCHASE_ORG + "/item");
		
		
		// Loop through each Purchase Org and set Back Order Ind
		for(DataObject inputPromotePurchOrg : inputPromotePurchOrgList ) {
			
			if(inputPromotePurchOrg != null) {
				
				String inputPromotePurchOrgRowid = inputPromotePurchOrg.getString(BusinessEntityConstants.ROWID_OBJECT);
				DataObject promotePurchOrg = null;
				
				if (inputPromotePurchOrgRowid != null && promotePurchOrgList != null)
					promotePurchOrg = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promotePurchOrgList, inputPromotePurchOrgRowid);
				
				String backOrderInd = vendorSDOHelper.getString(inputPromotePurchOrg, promotePurchOrg, BusinessEntityConstants.PURCH_ORG_BACK_ORDER_IND);
				String backOrderDays = vendorSDOHelper.getString(inputPromotePurchOrg, promotePurchOrg, BusinessEntityConstants.PURCH_ORG_BACK_ORDER_DAYS);
				
				if(backOrderInd.equals("N")) {		
					
					@SuppressWarnings("unchecked")					
					List<String> purchaseOrgModifiedFieldList = inputSDOChangeSummary.getOldValues(inputPromotePurchOrg) == null ? new ArrayList<String>() 
							: (List<String>) inputSDOChangeSummary.getOldValues(inputPromotePurchOrg).stream().map(oldValue -> {
								return ((Setting) oldValue).getProperty().getName(); }).collect(Collectors.toList());
					
//					if(purchaseOrgModifiedFieldList!=null)
//						for(String purchaseOrgModifiedFieldName : purchaseOrgModifiedFieldList)
//							logger.info("Names : "+purchaseOrgModifiedFieldName);
					
					if(purchaseOrgModifiedFieldList.contains(BusinessEntityConstants.PURCH_ORG_BACK_ORDER_DAYS)) {
						
						if(!backOrderDays.equals("10")) {
//							logger.info("Back Order Ind = N and Back Order days got modified");
							return createErrors(ErrorConstants.BACK_ORDER_IND_N_VALIDATE_ERROR_CODE,
									ErrorConstants.BACK_ORDER_IND_N_VALIDATE_ERROR_MESSAGE,
									businessEntity + "." + BusinessEntityConstants.PURCHASE_ORG, helperContext.getDataFactory(), "ERROR");
						}
					}
				}
				if(backOrderInd.equals("Y")) {
					
					Integer backOrderDaysValue = Integer.valueOf(backOrderDays);
					if(!ValueRange.of(10, 16).isValidIntValue(backOrderDaysValue)) {
						
						return createErrors(ErrorConstants.BACK_ORDER_IND_Y_VALIDATE_ERROR_CODE,
								ErrorConstants.BACK_ORDER_IND_Y_VALIDATE_ERROR_MESSAGE,
								businessEntity + "." + BusinessEntityConstants.PURCHASE_ORG, helperContext.getDataFactory(), "ERROR");
					}
				}
			}
		}		
		return null;
	}
}
