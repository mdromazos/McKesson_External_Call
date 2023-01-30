package com.informatica.mdm.bes.validate;

import java.util.List;
import java.util.Map;

import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * If Vendor is doing business with PSAS and the Product Coverage is under 10 million than require a COI to be entered.  
 * If not throw an error.
 * 
 * If the Vendor is Non-Trade and ProductCoverage is under 5 Million then COI must be uploaded.  If not throw an error.
 * 
 * @author mdromazo
 *
 */
public class COIValidate extends Validate {

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		// TODO Auto-generated method stub
		return null;
	}
}
