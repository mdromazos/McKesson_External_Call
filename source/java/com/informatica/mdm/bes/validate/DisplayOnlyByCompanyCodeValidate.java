package com.informatica.mdm.bes.validate;

import java.util.List;
import java.util.Map;

import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;


/**
 * This class will look at a properties field and see which fields are display only based on the company codes the supplier is with.  
 * If the user tries to change the values then the class will throw an error.
 * 
 * @author mdromazo
 *
 */
public class DisplayOnlyByCompanyCodeValidate extends Validate {

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		// TODO Auto-generated method stub
		return null;
	}

}
