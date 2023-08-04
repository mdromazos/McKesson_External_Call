package com.informatica.mdm.bes.validate;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.domain.ExternalCallProcess;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;


/**
 * @author Matthew Dromazos
 *
 */
public abstract class Validate extends ExternalCallProcess {
	
	protected static Logger logger = Logger.getLogger(Validate.class.getName());

	protected String validateName;
	
	protected String errorCode;
	protected String errorMessage;
	protected String errorField; 
	
	public Validate() { }
	
	public Validate(String validateName, String errorCode, String errorMessage, String errorField) {
		this.validateName = validateName;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.errorField = errorField;
	}
	
	public abstract List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity, DataObject promotePreviewSDO, CallContext callContext
 			,CompositeServiceClient besClient, DataObject dbSDO);
		
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
    public List<ValidationError> createErrors(String errorCode, String errorMessage, String errorField, DataFactory dataFactory,String errorLevel) {
    	List<ValidationError> validationErrors = new ArrayList<ValidationError>();
    	try {
    		
	    	ValidationError error = createError(errorCode, errorMessage, errorField, dataFactory,errorLevel);
	    	validationErrors.add(error);
	        
	        return validationErrors;
    	} catch (Exception e) {
			logger.error("createError had an error: " + e.getMessage(), e);
 		}
		return null;
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
    public ValidationError createError (String errorCode, String errorMessage, String errorField, DataFactory dataFactory,String errorLevel) {
    	try {
	    	ValidationError error = (ValidationError) dataFactory.create(ValidationError.class);
	        error.setCode(errorCode);
	        error.setMessage(errorMessage);
	        error.setLevel(errorLevel);
	        error.setField(Collections.singletonList(errorField));
	        
	        return error;
    	} catch (Exception e) {
			logger.error("createError had an error: " + e.getMessage(), e);
 		}
		return null;
    }
    
    /**
   	* createError - Function used to create a ValidationError Object
   	*   
	* @param  {DataFactory} dataFactory - DataFactory for creating ValidationError Objects 
	* 
   	* @returns {ValidationError} - Validation Error if error condition(s) found
   	*/
    public ValidationError createError (DataFactory dataFactory) {
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
    
    protected void pauseLogging(DataObject inputSDO) {
		SDOChangeSummary sdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		sdoChangeSummary.pauseLogging();
    }
    
    public String getValidateName() {
    	return validateName;
    }
    
    public void setValidateName(String validateName) {
    	this.validateName = validateName;
    }
    
	public String getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorField() {
		return errorField;
	}
	
	public void setErrorField(String errorField) {
		this.errorField = errorField;
	}
    
	public String toString() {
		return "Validation {" +
				"validateName=" + validateName +
				"ErrorCode=" + errorCode + 
				"ErrorMessage=" + errorMessage + 
				"ErrorField=" + errorField +
				"}";
	}

}
