package com.informatica.mdm.bes.customlogic;

import java.io.PrintWriter;

import java.io.StringWriter;
import java.time.Instant;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.delos.util.collection.Tree.Node;
import com.informatica.mdm.bes.automate.Automate;
import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.dataobjecthelper.ErrorHelper;
import com.informatica.mdm.bes.domain.BusinessRules;
import com.informatica.mdm.bes.domain.ExternalCallProcess;
import com.informatica.mdm.bes.domain.ParallelBusinessRules;
import com.informatica.mdm.bes.helper.ListHelper;
import com.informatica.mdm.bes.helper.VendorSDOHelper;
import com.informatica.mdm.bes.service.BusinessEntityServiceClient;
import com.informatica.mdm.bes.validate.Validate;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;
import com.informatica.mdm.spi.cs.StepException;
import com.informatica.mdm.spi.externalcall.CustomLogic;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.ServicePhase;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

/**
 * @author Matthew Dromazos
 *
 */
public abstract class CustomLogicImpl implements CustomLogic {
	private static Logger logger = Logger.getLogger(CustomLogicImpl.class.getName());
	
	protected DataObjectHelperContext dataObjectHelperContext = new DataObjectHelperContext();
	protected VendorSDOHelper vendorSDOHelper = new VendorSDOHelper();
	
	protected CompositeServiceClient compositeServiceClient;
	protected String businessEntity;
	protected List<String> roles;
	protected BusinessRules businessRules;
	protected ParallelBusinessRules parallelBusinessRules;
	
	protected List<ValidationError> errorList;
	protected DataFactory dataFactory;
	protected Instant startTime;
	
	protected BusinessEntityServiceClient businessEntityServiceClient;
	protected ExternalCallRequest externalCallRequest;
	protected CallContext callContext;
	
	protected DataObject orsSDOBare;
	protected DataObject promotePreviewSDO;
	protected DataObject dbSDO;
	protected DataObject orsSDO;
	
	private volatile DataObject sdo;
	
	
	protected ServicePhase phase;
	boolean parallel = true;
	
	public CustomLogicImpl() {
		this.businessEntity = "";
		this.businessRules = new BusinessRules();
		this.externalCallRequest = null;
		this.callContext = null;
	}
	
	public CustomLogicImpl(String businessEntity,
						   ServicePhase phase,
						   ExternalCallRequest externalCallRequest, 
						   CallContext callContext,
						   CompositeServiceClient compositeServiceClient,
						   List<String> roles) {
		this.businessEntity = businessEntity;
		this.businessRules = new BusinessRules();
		this.parallelBusinessRules = new ParallelBusinessRules(0,0);
		this.errorList = new ArrayList<>();
		this.callContext = callContext;
		this.externalCallRequest = externalCallRequest;
		this.phase = phase;
		this.compositeServiceClient = compositeServiceClient;
		businessEntityServiceClient = new BusinessEntityServiceClient();
		this.roles = roles;
	}
	
	protected abstract boolean decideBusinessRules(HelperContext helperContext, DataObject inputSDO, 
			Map<String, Object> inParams, Map<String, Object> outParams);
	
	
	protected void appendError(ValidationError error, HelperContext helperContext) throws StepException {
		String locale = null;
 		try {
 			if (error != null && error.getCode() != null ) {
 				errorList.add(error);
 			}
 		} catch (Exception e) {
 			logger.error("appendError had an error: " + e.getMessage(), e);
  		}		

 		if (ErrorHelper.isMaxErrorThresholdHit(ListHelper.getListSize(errorList), Constants.maxErrorThreshold)) {
				ErrorHelper.throwErrorList(errorList, helperContext.getDataFactory(),startTime, null);
		}
 	}
	
	protected void appendErrors(List<ValidationError> validationErrors, HelperContext helperContext) throws StepException {
		try {
			if (validationErrors != null) {
				for (ValidationError validationError : validationErrors) {
					appendError(validationError, helperContext);
				}	
			}
		} catch (Exception e) {
			logger.error("appendErrors had an error: " + e.getMessage(), e);
		}
 	}
	
	protected void processBusinessRulesSequential(DataObject inputSDO, HelperContext helperContext, Map inParams, Map outParams) {
		setHelper();
		processAutomations(inputSDO, helperContext, inParams, outParams);
		processValidations(inputSDO, helperContext, inParams, outParams);
	}
	
	protected void processBusinessRules(DataObject inputSDO, HelperContext helperContext, Map inParams, Map outParams) {
		setHelper();
		if (parallel) {
			procesParallelAutomate(inputSDO, helperContext, inParams, outParams);
			procesParallelValidate(inputSDO, helperContext, inParams, outParams);	
		} else {
			processAutomations(inputSDO, helperContext, inParams, outParams);
			processValidations(inputSDO, helperContext, inParams, outParams);
		}
	}
	
	private void processValidations(DataObject inputSDO, HelperContext helperContext, Map inParams, Map outParams) {
		logger.info("STARTING PROCESSING OF VALIDATIONS");
		for (Validate validation : businessRules.getValidations()) {
			logger.info("STARTING VALIDATION: " + validation.getClass().getName());
			try {
				appendErrors(validation.doValidation(inputSDO, helperContext, 
						inParams, outParams, businessEntity, promotePreviewSDO, callContext, compositeServiceClient, dbSDO), helperContext);
			} catch (Exception e) {
				logger.error("ERROR while running validation " + validation.getClass().getName());
				logger.error(e.toString());
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String sStackTrace = sw.toString(); // stack trace as a string
				logger.error(sStackTrace);
			}
		}
	}
	
	private void procesParallelAutomate(DataObject inputSDO, HelperContext helperContext, Map inParams, Map outParams) {
		logger.info("STARTING PROCESSING OF AUTOMATIONS");
		for (List<Automate> automations : parallelBusinessRules.getParallelAutomations()) {
			List<Thread> automateThreads = new ArrayList<Thread>(automations.size());
			for (Automate automate : automations) {
				logger.info("STARTING AUTOMATION: " + automate.getClass().getName());
				Thread validationThread = new Thread(() -> {
					try {
						appendErrors(automate.doAutomate(inputSDO, helperContext, 
								inParams, outParams, businessEntity, promotePreviewSDO, callContext, compositeServiceClient), helperContext);
					} catch (Exception e) {
						logger.error("ERROR while running automation " + automate.getClass().getName());
						logger.error(e.toString());
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						String sStackTrace = sw.toString(); // stack trace as a string
						logger.error(sStackTrace);
					}         
				});
				automateThreads.add(validationThread);
			}
			try {
				runAll(automateThreads);
				waitFor(automateThreads);
			} catch (InterruptedException e) {
				logger.error("ERROR while running Threads");
				logger.error(e.toString());
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String sStackTrace = sw.toString(); // stack trace as a string
				logger.error(sStackTrace);
			}
		}
	}
	
	private void procesParallelValidate(DataObject inputSDO, HelperContext helperContext, Map inParams, Map outParams) {
		logger.info("STARTING PROCESSING OF VALIDATIONS");
		for (List<Validate> validations : parallelBusinessRules.getParallelValidations()) {
			List<Thread> validationThreads = new ArrayList<Thread>(validations.size());
			for (Validate validation : validations) {
				logger.info("STARTING VALIDATION: " + validation.getClass().getName());
				Thread validationThread = new Thread(() ->{
					try {
						appendErrors(validation.doValidation(inputSDO, helperContext, 
								inParams, outParams, businessEntity, promotePreviewSDO, callContext, compositeServiceClient, dbSDO), helperContext);
					} catch (Exception e) {
						logger.error("ERROR while running validation " + validation.getClass().getName());
						logger.error(e.toString());
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						String sStackTrace = sw.toString(); // stack trace as a string
						logger.error(sStackTrace);
					}         
				});
				validationThreads.add(validationThread);
			}
			try {
				runAll(validationThreads);
				waitFor(validationThreads);
			} catch (InterruptedException e) {
				logger.error("ERROR while running Threads");
				logger.error(e.toString());
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String sStackTrace = sw.toString(); // stack trace as a string
				logger.error(sStackTrace);
			}
		}
	}
	
	private void processAutomations(DataObject inputSDO, HelperContext helperContext, Map inParams, Map outParams) {
		logger.info("STARTING PROCESSING OF AUTOMATIONS");

		for (Automate automate : businessRules.getAutomations()) {
			logger.info("STARTING AUTOMATIONS: " + automate.getClass().getName());

			try {
				automate.doAutomate(inputSDO, helperContext, inParams, outParams, businessEntity, promotePreviewSDO, callContext, compositeServiceClient);
			} catch (Exception e) {
				logger.error("ERROR while running automation " + automate.getClass().getName());
				logger.error(e.toString());
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String sStackTrace = sw.toString(); // stack trace as a string
				logger.error(sStackTrace);
			}
		}
	}
	
	public String getDraftSts(DataObject inputSDO, String businessEntity, HelperContext helperContext, String rowidObject) {
		String draftSts = null;
		draftSts = inputSDO.getString(businessEntity + "/" + BusinessEntityConstants.DRAFT_STS);
		if (draftSts == null) {
			try {
				orsSDOBare = dataObjectHelperContext.getDataObjectReader().readRootORS(callContext, compositeServiceClient, helperContext, businessEntity, rowidObject, 0);
				draftSts = orsSDOBare.getString(businessEntity + "/" + BusinessEntityConstants.DRAFT_STS);
			} catch (Exception e) {
				draftSts = "N";
			}
		}
		return draftSts;
	}
	
	public Boolean getDraftStsView(DataObject inputSDO, String businessEntity, HelperContext helperContext, String rowidObject) {
		Boolean draftSts = null;
		if (inputSDO.isSet(businessEntity + "/" + BusinessEntityConstants.DRAFT_STS)) {
			draftSts = inputSDO.getBoolean(businessEntity + "/" + BusinessEntityConstants.DRAFT_STS);
		} else {
			try {
				orsSDOBare = dataObjectHelperContext.getDataObjectReader().readRootORS(callContext, compositeServiceClient, helperContext, businessEntity, rowidObject, 0);
//				logger.info("PRINTING ROOT ORS");
//				dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, orsSDOBare);
				draftSts = orsSDOBare.getBoolean(businessEntity + "/" + BusinessEntityConstants.DRAFT_STS);
			} catch (Exception e) {
				draftSts = false;
			}
		}
		return draftSts;
	}
	
	public static void waitFor(Collection<? extends Thread> c) throws InterruptedException {
	    for(Thread t : c) t.join();
	}
	
	public static void runAll(Collection<? extends Thread> c) {
	    for(Thread t : c) t.start();
	}
	
	public void setHelper(List<? extends ExternalCallProcess> externalCallProcesses) {
		for (ExternalCallProcess externalCallProcess: externalCallProcesses) {
			externalCallProcess.setDataObjectHelperContext(dataObjectHelperContext);
			externalCallProcess.setVendorSDOHelper(vendorSDOHelper);
		}
	}
	
	public void setHelper() {
		for (List<? extends ExternalCallProcess> automations : parallelBusinessRules.getParallelAutomations()) {
			setHelper(automations);
		}
		for (List<? extends ExternalCallProcess> automations : parallelBusinessRules.getParallelValidations()) {
			setHelper(automations);
		}
		
		setHelper(businessRules.getAutomations());
		setHelper(businessRules.getValidations());
	}
	
	protected void getRecordFromDatabase(HelperContext helperContext, String interactionId, String rowidObject, boolean readExisting, boolean readPromote, boolean readDbSdo) {
		List<Thread> readThreads = new ArrayList<Thread>();
		if (readExisting)
			readThreads.add(readExistingRecord(helperContext, rowidObject));
		if (readPromote)
			readThreads.add(readPromotePreview(helperContext, rowidObject, interactionId));
		if (readDbSdo)
			readThreads.add(readDbSdo(helperContext, rowidObject));
		
		try {
			runAll(readThreads);
			waitFor(readThreads);
		} catch (InterruptedException e) {
			
		} catch (Exception e) {
			try {
				appendErrors(dataObjectHelperContext.getErrorHelper().createErrors("CUSTOM-NEWWVENDOR1000", 
						"Unable to read MDM Database. Please try again later.", businessEntity, helperContext.getDataFactory()), helperContext);
			} catch (StepException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	protected Thread readExistingRecord(HelperContext helperContext, String rowidObject) {
		Thread readThread = new Thread(() -> {
			orsSDO = businessEntityServiceClient.readExistingRecord(callContext, compositeServiceClient, 
					helperContext, businessEntity, rowidObject);
		});
		return readThread;
	}
	
	protected Thread readPromotePreview(HelperContext helperContext, String rowidObject, String interactionId) {
		Thread readThread = new Thread(() -> {
			promotePreviewSDO = businessEntityServiceClient.readPromotePreview(interactionId, callContext, compositeServiceClient, 
					helperContext, businessEntity, rowidObject);

		});
		return readThread;
	}
	
	protected Thread readDbSdo(HelperContext helperContext, String rowidObject) {
		Thread readThread = new Thread(() -> {
			dbSDO = businessEntityServiceClient.readDBExistingRecord(callContext, compositeServiceClient, externalCallRequest, helperContext, 
					businessEntity, rowidObject);

		});
		return readThread;
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
}