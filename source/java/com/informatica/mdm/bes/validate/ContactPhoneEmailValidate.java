package com.informatica.mdm.bes.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * For each Contact make sure there is at least one Phone number and Email
 * Child.
 * 
 * @author Matthew Dromazos
 *
 */
public class ContactPhoneEmailValidate extends Validate {

	
	@SuppressWarnings("unchecked")
	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		DataObject promoteSDOBe = null;
		// TODO Auto-generated method stub
		pauseLogging(inputSDO);
		
		logger.info("ContactPhoneEmail Validation");
		if (inputSDO == null)
			return null;
		Boolean validateOnly = (Boolean) inParams.get(Constants.VALIDATE_ONLY);
		Boolean isE360Save = isE360Save(validateOnly);
		logger.info("Save Button : " + isE360Save);
		
		if (!isE360Save)
			return null;
		
		// Run when validations when save button is clicked			
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);

		if (promotePreviewSDO != null)				
			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
		
		List<DataObject> inputPromoteContactList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe,
				VendorMainConstants.CONTACT, inputSDO, helperContext);
		logger.info("AFTER GET COMBINED LIST : Contact Details : "+inputPromoteContactList);

		if (inputPromoteContactList == null /*|| inputPromoteContactList.isEmpty()*/) {
			logger.info("Into Contact Error");
			return createErrors(ErrorConstants.CONTACT_VALIDATE_ERROR_CODE,
					ErrorConstants.CONTACT_VALIDATE_ERROR_MESSAGE,
					businessEntity + "." + VendorMainConstants.CONTACT, helperContext.getDataFactory(), "ERROR");
		}
		
		logger.info("Into Contact Child Section");
		for(DataObject inputPromoteContactListChild :inputPromoteContactList ) {
			if (inputPromoteContactListChild == null)
				continue;
			
			String inputPromoteContactRowid = inputPromoteContactListChild.getString(BusinessEntityConstants.ROWID_OBJECT);
			logger.info("InputPromoteContactRowId : "+ inputPromoteContactRowid);
			List<DataObject> promoteContactList = new ArrayList<DataObject>();
			if (promotePreviewSDO != null)	
			promoteContactList = promoteSDOBe.getList(VendorMainConstants.CONTACT+"/item");
			logger.info("Promote Contact List : " + promoteContactList);
			DataObject promoteContactChild = null;
			if (inputPromoteContactRowid != null && promoteContactList != null) 
				promoteContactChild = dataObjectHelperContext.getDataObjectSearcher()
						.searchDataObjectList(promoteContactList, inputPromoteContactRowid);
			logger.info("Promote Contact Child : " + promoteContactChild);
			
			// Combine Phone List
			List<DataObject> phoneContactList = vendorSDOHelper.getCombinedList(inputPromoteContactListChild,
					promoteContactChild, VendorMainConstants.CONTACT_PHONE, inputSDO, helperContext);
			// Combine Email List
			List<DataObject> electronicContactList = vendorSDOHelper.getCombinedList(inputPromoteContactListChild,
					promoteContactChild, VendorMainConstants.CONTACT_ELECTRONIC_ADDRESS, inputSDO, helperContext);

			if(phoneContactList == null||electronicContactList == null) {
				return createErrors(ErrorConstants.CONTACT_VALIDATE_ERROR_CODE,
						ErrorConstants.CONTACT_VALIDATE_ERROR_MESSAGE,
						businessEntity + "." + VendorMainConstants.CONTACT, helperContext.getDataFactory(), "ERROR");
			}
				
		}							

		return null;
	}

	protected Boolean isE360Save(Boolean validateOnly) {
		return (validateOnly != null && !validateOnly) ? true : false;
	}
}
