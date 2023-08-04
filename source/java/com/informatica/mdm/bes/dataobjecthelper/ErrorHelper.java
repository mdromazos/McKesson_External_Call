package com.informatica.mdm.bes.dataobjecthelper;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.helper.ListHelper;
import com.informatica.mdm.bes.helper.TimeHelper;
import com.informatica.mdm.sdo.cs.base.ValidationError;
import com.informatica.mdm.sdo.cs.base.ValidationErrors;
import com.informatica.mdm.spi.cs.StepException;

import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;

public class ErrorHelper {
	protected static Logger logger = Logger.getLogger(ErrorHelper.class.getName());

	public static Boolean isMaxErrorThresholdHit (Integer errorListSize, int maxErrorThreshold) {
		try {
			logger.debug("errorListSize: " + errorListSize);
			return (errorListSize >= maxErrorThreshold) ? true : false;
		} catch (Exception e) {
			logger.error("isMaxErrorThresholdHit had an error: " + e.getMessage(), e);
 		}
		return false;
	}
	
	public static void throwErrorList(List<ValidationError> errorList, DataFactory dataFactory, Instant startTime, Boolean hierarchyFlag) throws StepException {
    	logger.debug("Inside throwErrorList");

    	hierarchyFlag = (hierarchyFlag != null && hierarchyFlag) ? hierarchyFlag : false;
		if (!errorList.isEmpty()) {
			Integer errorListSize = ListHelper.getListSize(errorList);

			String errorCode = (errorListSize==1 && hierarchyFlag)  ? errorList.get(0).getMessage()  : "SIP-50022";
        	ValidationErrors errors = (ValidationErrors) dataFactory.create(ValidationErrors.class);
        	logger.debug("ERROR LIST SIZE: " + ListHelper.getListSize(errorList));
        	logger.debug("errorCode: " +  errorCode);

        	errors.setError(errorList);
        	DataObject inputSDSO;
        	TimeHelper.getDuration(startTime, "Full Duration Till Exception");
        	throw new StepException((DataObject) errors,errorCode);
        } 			
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
			logger.error("createError had an error: " + e.getMessage(), e);
 		}
		return null;
    }
    
    /**
   	* createErrors - Function used to create a a list of ValidationErros's
   	*   
	* @param  {String} errorCode - String containing the error code
	* @param  {String} errorMessage - String containing the error message
	* @param  {String} errorField - String containing view location to point the error 
	* @param  {DataFactory} dataFactory - DataFactory for creating ValidationError Objects 
	* 
   	* @returns {ValidationError} - Validation Error if error condition(s) found
   	*/
    public List<ValidationError> createErrors(String errorCode, String errorMessage, String errorField, DataFactory dataFactory) {
    	List<ValidationError> validationErrors = new ArrayList<ValidationError>();
    	try {
    		
	    	ValidationError error = createError(errorCode, errorMessage, errorField, dataFactory);
	    	validationErrors.add(error);
	        
	        return validationErrors;
    	} catch (Exception e) {
			logger.error("createError had an error: " + e.getMessage(), e);
 		}
		return null;
    }
	
}
