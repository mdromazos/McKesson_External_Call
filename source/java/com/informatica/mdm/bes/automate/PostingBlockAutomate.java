package com.informatica.mdm.bes.automate;

import java.util.Map;

import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * If the Posting Block is set at the Supplier Table level, then loop through all Company Codes and set it there.
 * 
 * If a new Company Code is added and the Posting Block is set then set the new Company Code Posting Block
 * @author mdromazo
 *
 */
public class PostingBlockAutomate extends Automate {

	@Override
	public ValidationError doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO,
			CallContext callContext, CompositeServiceClient besClient) {
		// TODO Auto-generated method stub
		return null;
	}

}
