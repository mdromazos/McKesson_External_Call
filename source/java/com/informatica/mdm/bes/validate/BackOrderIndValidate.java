package com.informatica.mdm.bes.validate;

import java.util.List;
import java.util.Map;

import com.informatica.mdm.bes.automate.Automate;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
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
		// TODO Auto-generated method stub
		return null;
	}
}
