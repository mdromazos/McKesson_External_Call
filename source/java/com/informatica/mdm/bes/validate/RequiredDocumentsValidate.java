package com.informatica.mdm.bes.validate;

import java.util.List;
import java.util.Map;

import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * The class will look at a properties file that details the business logic for how documents are required based on the Vendor.
 * 
 * @author Matthew Dromazos
 *
 */
public class RequiredDocumentsValidate extends Validate {

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		// TODO Auto-generated method stub
		return null;
	}

}
