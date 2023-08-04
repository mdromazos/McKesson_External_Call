/**
 * 
 */
package com.informatica.mdm.bes.domain;

import java.util.Collections;
import java.util.Map;

import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.helper.VendorSDOHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public abstract class ExternalCallProcess {
	protected VendorSDOHelper vendorSDOHelper;
	protected DataObjectHelperContext dataObjectHelperContext;

	
	public void setVendorSDOHelper(VendorSDOHelper vendorSDOHelper) {
		this.vendorSDOHelper = vendorSDOHelper;
	}
	
	public VendorSDOHelper getVendorSDOHelper() {
		return vendorSDOHelper;
	}
	
	public void setDataObjectHelperContext(DataObjectHelperContext dataObjectHelperContext) {
		this.dataObjectHelperContext = dataObjectHelperContext;
	}
	
	public DataObjectHelperContext getDataObjectHelperContext() {
		return dataObjectHelperContext;
	}
	
    /**
   	* createError - Function used to create a ValidationError Object
   	*   
	* @param  {String} errorCode - String containing the error code
	* @param  {String} errorMessage - String containing the error message
	* @param  {String} errorField - String containing view location to point the error 
	* @param  {DataFactory} dataFactory - DataFactory for creating ValidationError Objects 
	* 
   	* @returns {ValidationError} - Validation Error if error condition(s) found
   	*/
    public ValidationError createError (String errorCode, String errorMessage, String errorField, DataFactory dataFactory) {
    	try {
	    	ValidationError error = (ValidationError) dataFactory.create(ValidationError.class);
	        error.setCode(errorCode);
	        error.setMessage(errorMessage);
	        error.setField(Collections.singletonList(errorField));
	        
	        return error;
    	} catch (Exception e) {
    		return null;
    	}
    }
}
