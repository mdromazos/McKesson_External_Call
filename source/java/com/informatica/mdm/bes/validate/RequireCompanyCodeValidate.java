package com.informatica.mdm.bes.validate;

import java.util.List;
import java.util.Map;

import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.helper.SDOHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * Require at least one company code when the Supplier is bring created.
 * 
 * @author Matthew Dromazos
 *
 */
public class RequireCompanyCodeValidate extends Validate {

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
	    pauseLogging(inputSDO);
	    if (inputSDO == null) 
	        return null;
	    
	    DataObject promoteSDOBe = null;
	    DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
	    
	    if (promotePreviewSDO != null)
	    	promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
	    List<DataObject> companyCodeList = SDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, 
	    		VendorMainConstants.COMPANY_CODE, inputSDO, helperContext, dataObjectHelperContext);
	    	    
	    if (companyCodeList == null || companyCodeList.isEmpty()) {
	        return createErrors(ErrorConstants.COMPANY_CODE_MISSING_ERROR_CODE, 
	                ErrorConstants.COMPANY_CODE_MISSING_ERROR_MESSAGE, 
	                businessEntity + "." + VendorMainConstants.COMPANY_CODE, helperContext.getDataFactory());
	    }
	    return null;
	}
}
