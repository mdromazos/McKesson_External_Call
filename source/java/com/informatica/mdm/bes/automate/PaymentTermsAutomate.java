package com.informatica.mdm.bes.automate;

import java.util.Map;

import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * Loop through each Purchase Organization and default the Payment Terms based on the POs Associated Company Code Payment Terms
 * 
 * @author mdromazo
 *
 */
public class PaymentTermsAutomate extends Automate {

	@Override
	public ValidationError doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO,
			CallContext callContext, CompositeServiceClient besClient) {
		// TODO Auto-generated method stub
		return null;
	}

}