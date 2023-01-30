package com.informatica.mdm.bes.automate;

import java.util.Map;

import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * Will look at a properties file to see which fields are defaulted based on the company code.  
 * 
 * @author Matthew Dromazos
 *
 */
public class DefaultsByCompanyCodeAutomate extends Automate {

	@Override
	public ValidationError doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO,
			CallContext callContext, CompositeServiceClient besClient) {
		// TODO Auto-generated method stub
		return null;
	}

}
