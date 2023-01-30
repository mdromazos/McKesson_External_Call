package com.informatica.mdm.bes.customlogic;

import java.time.Instant;


import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.automate.Automate;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.dataobjecthelper.ErrorHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.api.CompositeServiceException;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;
import com.informatica.mdm.spi.cs.StepException;
import com.informatica.mdm.spi.externalcall.CustomLogic;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.Parameter;
import com.informatica.mdm.spi.externalcall.ServicePhase;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.DataFactory;

/**
 * This is the CustomLogic class to be run when the Business Entity is the Vendor Main View.  This class runs a majority of the Validations for the HFC and HFLS Business Entities.
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class ViewCustomLogicImpl extends CustomLogicImpl {	
	private static Logger logger = Logger.getLogger(ViewCustomLogicImpl.class.getName());

		
	public ViewCustomLogicImpl(String businessEntity, ServicePhase phase,
								  ExternalCallRequest externalCallRequest, 
								  CallContext callContext,
								  CompositeServiceClient compositeServiceClient,
								  List<String> roles) {
		super(businessEntity, phase, externalCallRequest, callContext, compositeServiceClient, roles);
	}

	/**
	 * This method processes the custom logic. It decides what rules to run and then calls its parent method to run all automations and validations needed.
	 */
	@Override
	public DataObject process(HelperContext helperContext, DataObject inputSDO, Map<String, Object> inParams, Map<String, Object> outParams)
			throws StepException {
		logger.info("STARTING PROCESS OF HFCViewCustomerLogicImpl");
		startTime = Instant.now();
		try {
			dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, inputSDO);
			decideBusinessRules(helperContext, inputSDO, inParams, outParams);
			processBusinessRules(inputSDO, helperContext, inParams, outParams);
			dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, inputSDO);
		} catch (Exception e) {
			logger.info("ERROR DURING PROCESSING");
			logger.info(e.getMessage());
			e.printStackTrace();
		}
		ErrorHelper.throwErrorList(errorList, helperContext.getDataFactory(), startTime, null);	

		
		return inputSDO;
	}

	
	/**
	 * This method decides what Validations need to be run depending on the record state and what request is coming from MDM UI, 
	 * or if it is a call coming from ActiveVOS.
	 */
	protected boolean decideBusinessRules(HelperContext helperContext, DataObject inputSDO, Map<String, Object> inParams, Map<String, Object> outParams) {
		logger.info("INSIDE DECIDE BUSINESS RULES");
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		String rowidObject = inputSDOBe.getString(Constants.ROWID_OBJECT);
		Boolean draftSts = getDraftStsView(inputSDO, businessEntity, helperContext, rowidObject);  
		boolean validateOnly = (boolean) inParams.get(Constants.VALIDATE_ONLY);
		boolean newVendor = inputSDO.getChangeSummary().isCreated(inputSDOBe);
		String interactionId = (String) inParams.get(Constants.INTERACTION_ID);
		
		logger.info("INSIDE DECIDE BUSINESS RULES : newVendor : " + newVendor);
		logger.info("DRAFT STATUS IS :" + draftSts);
				
		// Always run when 
//		if (!newVendor && !validateOnly) {
//			boolean gotRecords = getRecordFromDatabase(inputSDOBe, helperContext, interactionId, rowidObject);
//			if (!gotRecords)
//				return false;
//			
//			parallelBusinessRules.addValidation(new SyncRequestDataChangeValidate(), 0);
//			
//			if (!draftSts) {;
//				
//				parallelBusinessRules.addValidation(new PaymentMethodTermValidate(), 0);
//				parallelBusinessRules.addValidation(new APQuestionsValidate(roles), 0);
//				parallelBusinessRules.addValidation(new DuplicateVendorValidate(), 0);
//				parallelBusinessRules.addValidation(new PIExistsValidate(), 0);
//				parallelBusinessRules.addValidation(new RemitToBankValidate(), 0);
//				parallelBusinessRules.addValidation(new RemitToAddressValidate(), 0);
//				parallelBusinessRules.addValidation(new OrderFromAddressValidate(), 0);
//				
//				parallelBusinessRules.addValidation(new RemitToPaymentValidate(roles), 1);
//				parallelBusinessRules.addValidation(new SCACValidate(roles), 1);
//				parallelBusinessRules.addValidation(new SpendAnnualVendorCatValidate(), 1);
//				parallelBusinessRules.addValidation(new TaxExistsValidate(), 1);
//				
//				parallelBusinessRules.addValidation(new TaxQuestionsValidate(roles), 2);
//				parallelBusinessRules.addValidation(new SendToPortalEligibleValidate(), 2);
//				parallelBusinessRules.addValidation(new VNPIOAAddressValidate(), 2);
//				parallelBusinessRules.addValidation(new VNPIOAStateRegionValidate(), 2);
//				parallelBusinessRules.addValidation(new W8W9Validate(), 2);
//			}
//		}
		return true;
	}
	
	/**
	 * This method grabs the record data from MDM.  It grabs what the active record is in the orsSDO, and it grabs the 
	 * Promote Preview version of the record in the promotePreviewSDO.
	 * 
	 * @param helperContext
	 * @param interactionId
	 * @param rowidObject
	 */
	private boolean getRecordFromDatabase(DataObject inputSDOBe, HelperContext helperContext, String interactionId, String rowidObject) {
		logger.info("GRABBING RECORDS FROM THE DATABASE");
		
		promotePreviewSDO = businessEntityServiceClient.readPromotePreview(interactionId, callContext, compositeServiceClient, helperContext, businessEntity, rowidObject);
		dbSDO = businessEntityServiceClient.readDBExistingRecord(callContext, compositeServiceClient, externalCallRequest, helperContext, businessEntity, rowidObject);
		
		logger.info("PRINTING OUT PROMOTE PREVIEW");
		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, promotePreviewSDO);
		
		return true;
	}
}
