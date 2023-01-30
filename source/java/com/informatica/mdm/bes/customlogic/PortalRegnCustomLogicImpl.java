/**
 * 
 */
package com.informatica.mdm.bes.customlogic;

import java.time.Instant;

import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;

import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.automate.PSTRegisteredAutomate;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.dataobjecthelper.ErrorHelper;
import com.informatica.mdm.bes.factory.CompositeServiceClientFactoryImpl;
import com.informatica.mdm.bes.validate.ContactPhoneEmailValidate;
import com.informatica.mdm.bes.validate.RequiredDocumentsValidate;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.spi.cs.StepException;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.ServicePhase;


import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * This is the CustomLogic class to be run when the Business Entity is the Portal Registration.  It runs one main validation to make sure the Send To Portal field and Portal Users are correctly set
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class PortalRegnCustomLogicImpl extends CustomLogicImpl {
	private static Logger logger = Logger.getLogger(PortalRegnCustomLogicImpl.class.getName());
	
	public PortalRegnCustomLogicImpl(String businessEntity, 
										ServicePhase phase,
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
		logger.info("STARTING PROCESS OF HFCPortalRegnCustomLogicImpl");
		startTime = Instant.now();
		try {
			DataObjectHelperContext dataObjectHelperContext = new DataObjectHelperContext();
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

	@Override
	protected boolean decideBusinessRules(HelperContext helperContext, DataObject inputSDO, Map<String, Object> inParams,
			Map<String, Object> outParams) {
		boolean validateOnly = (boolean) inParams.get(Constants.VALIDATE_ONLY);
		if (!validateOnly) {
//			parallelBusinessRules.addValidation(new SendToPortalEligibleValidate(), 0);
		}
		parallelBusinessRules.addValidation(new RequiredDocumentsValidate(), 0);
		parallelBusinessRules.addValidation(new ContactPhoneEmailValidate(), 0);
		
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
		promotePreviewSDO = businessEntityServiceClient.readPromotePreview(interactionId, callContext, compositeServiceClient, helperContext, businessEntity, rowidObject);
		logger.info("PRINTING OUT SDO GRABBED FROM DATABASE");
		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, orsSDO);
		logger.info("PRINTING OUT PROMOTE PREVIEW");
		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, promotePreviewSDO);
		return true;
	}
}
