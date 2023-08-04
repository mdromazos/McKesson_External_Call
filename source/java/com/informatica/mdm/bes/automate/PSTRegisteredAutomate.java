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
 * When a new Tax is entered, look at the Tax Type, if it is set to PST, then set the PST Registered at the Supplier level
 * 
 * @author Matthew Dromazos
 *
 */
public class PSTRegisteredAutomate extends Automate {
	
	public static final String PST = "PST";

	@Override
	public List<ValidationError> doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO,
			CallContext callContext, CompositeServiceClient besClient) {

		SDOChangeSummary inputSDOChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		inputSDOChangeSummary.pauseLogging();
		
		// Grab Business Entity Data Object
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = promoteSDO.getDataObject(businessEntity);
		
		// Grab the list of taxes from both inputSDO and promoteSDO so we have the complete lists
		List<DataObject> inputPromoteTaxList = vendorSDOHelper.getCombinedListAutomation(inputSDOBe, promoteSDOBe, 
				BusinessEntityConstants.TAX, inputSDO, helperContext);
		
		// Grab just the promote tax list for reference
		List<DataObject> promoteTaxList = promoteSDOBe.getList(BusinessEntityConstants.TAX + "/item");
		
		if (inputPromoteTaxList == null)
			return null;
		
		// Loop through each tax to see if the tax type is PST
		boolean foundPSTTax = 
			inputPromoteTaxList.parallelStream().anyMatch((inputPromoteTax) -> {
				if (inputPromoteTax != null) {
					String inputPromoteTaxRowid = inputPromoteTax.getString(BusinessEntityConstants.ROWID_OBJECT);
					DataObject promoteTax = null;
					if (inputPromoteTaxRowid != null && promoteTaxList != null)
						promoteTax = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteTaxList, inputPromoteTaxRowid);
					
					String taxNumTyp = vendorSDOHelper.getString(inputPromoteTax, promoteTax, BusinessEntityConstants.TAX_TAX_TYPE_CD);
					if (taxNumTyp != null && taxNumTyp.equals(PST))
						return true;
					
				}
				return false;
			});
		
		// If the supplier has a PST tax, then set the PSTRegistered field
		if (foundPSTTax) {
			inputSDOChangeSummary.resumeLogging();
			inputSDOBe.setString(BusinessEntityConstants.PST_REGISTERED, "Y");
			inputSDOChangeSummary.pauseLogging();
		}
		
		return null;
	}

}