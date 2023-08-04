package com.informatica.mdm.bes.validate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * If the vendor is a Canadian vendor and revenue is above 30k/year then make sure there is a tax with type GST.  If not then throw an error.
 * 
 * @author mdromazo
 *
 */
public class GSTValidate extends Validate {
	
	public static final String[] SMALL_CDS = {"",""};
	public static final String GST = "GST";
	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		
		pauseLogging(inputSDO);
		
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = null;
		if (promotePreviewSDO != null)
			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
		
		String cntry = getCountry(inputSDOBe, promoteSDOBe, inputSDO, helperContext);
		if (cntry == null == cntry.equals("CA"))
			return null;
		
		
		
		String mnrInd = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, VendorMainConstants.MNR_IND);
		if (Arrays.asList(SMALL_CDS).contains(mnrInd))
			return null;
		
		List<DataObject> inputPromoteAlternateIdList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, VendorMainConstants.ALTERNATE_ID, inputSDO, helperContext);
		List<DataObject> promoteAlternateIdList = promoteSDOBe.getList(VendorMainConstants.ALTERNATE_ID + "/item");
		
		boolean hasGST = inputPromoteAlternateIdList.parallelStream().anyMatch(inputPromoteAlternateId -> {
			String inputPromoteAltIdRowid = inputPromoteAlternateId.getString(Constants.ROWID_OBJECT);
			DataObject promoteAlternateId = null;
			if (promoteAlternateIdList != null) {
				promoteAlternateId = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteAlternateIdList, inputPromoteAltIdRowid);
			}
			String altIdTyp = vendorSDOHelper.getString(inputPromoteAlternateId, promoteAlternateId, VendorMainConstants.ALTERNATE_ID_TYPE);
			if (altIdTyp != null && altIdTyp.equals(GST))
				return true;
			return false;
		});
		
		if (hasGST)
			return createErrors("ERROR",
					"Canada vendors must have a GST", 
					businessEntity + "." + VendorMainConstants.ALTERNATE_ID, 
					helperContext.getDataFactory());
		
		return null;
	}
	
	public String getCountry(DataObject inputSDOBe, DataObject promoteSDOBe, DataObject inputSDO, HelperContext helperContext) {
		List<DataObject> inputPromotePrimaryAddressList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, VendorMainConstants.PRIMARY_ADDR, inputSDO, helperContext);
		List<DataObject> promotePrimaryAddressList = promoteSDOBe != null ? promoteSDOBe.getList(VendorMainConstants.PRIMARY_ADDR + "/item") : null;
		
		if (inputPromotePrimaryAddressList == null || inputPromotePrimaryAddressList.isEmpty())
			return null;
		
		
		DataObject inputPromotePrimaryAddress = inputPromotePrimaryAddressList.get(0);
		DataObject promotePrimaryAddress = promotePrimaryAddressList != null ? promotePrimaryAddressList.get(0) : null;
		
		return vendorSDOHelper.getString(inputPromotePrimaryAddress, promotePrimaryAddress, 
				VendorMainConstants.PRIMARY_ADDR_POSTAL_ADDR + "/" + VendorMainConstants.PRIMARY_ADDR_POSTAL_ADDR_CNTRY);		
	}

}
