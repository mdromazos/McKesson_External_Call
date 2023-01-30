package com.informatica.mdm.bes.customlogic;

import java.time.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.automate.BackOrderIndAutomate;
import com.informatica.mdm.bes.automate.DefaultsByCompanyCodeAutomate;
import com.informatica.mdm.bes.automate.PSTRegisteredAutomate;
import com.informatica.mdm.bes.automate.PaymentTermsAutomate;
import com.informatica.mdm.bes.automate.PostingBlockAutomate;
import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.dataobjecthelper.ErrorHelper;
import com.informatica.mdm.bes.helper.VendorSDOHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;
import com.informatica.mdm.spi.cs.StepException;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.Parameter;
import com.informatica.mdm.spi.externalcall.ServicePhase;
import com.siperian.sif.client.EjbSiperianClient;
import com.siperian.sif.client.SiperianCommunicationException;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * This class is the CustomLogic to be run when the request is for a Business Entity. it runs all of the Automations and a couple Validations.
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class BeCustomLogicImpl extends CustomLogicImpl {

	private static Logger logger = Logger.getLogger(ViewCustomLogicImpl.class.getName());
		
	public BeCustomLogicImpl(String businessEntity, ServicePhase phase,
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

		startTime = Instant.now();
		try {
			dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, inputSDO);
			boolean wasAbleToDecideBusinessRules = decideBusinessRules(helperContext, inputSDO, inParams, outParams);
			if (wasAbleToDecideBusinessRules) {
				processBusinessRules(inputSDO, helperContext, inParams, outParams);	
			}
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
	 * This method decides what Automations and Validations need to be run depending on the record state and what request is coming from MDM UI, 
	 * or if it is a call coming from ActiveVOS.
	 */
	protected boolean decideBusinessRules(HelperContext helperContext, DataObject inputSDO, Map<String, Object> inParams, Map<String, Object> outParams) {
		logger.info("INSIDE DECIDE BUSINESS RULES");
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		String rowidObject = inputSDOBe.getString(Constants.ROWID_OBJECT);
		boolean validateOnly = (boolean) inParams.get(Constants.VALIDATE_ONLY);
		String interactionId = (String) inParams.get(Constants.INTERACTION_ID);
		if (interactionId == null) {
			interactionId = inputSDOBe.getString(Constants.INTERACTION_ID);	
		}
		
		
		boolean newVendor = inputSDO.getChangeSummary().isCreated(inputSDOBe) && (interactionId == null || interactionId.isEmpty());
		parallelBusinessRules.addAutomation(new BackOrderIndAutomate(), 0);
		parallelBusinessRules.addAutomation(new DefaultsByCompanyCodeAutomate(), 0);
		parallelBusinessRules.addAutomation(new PaymentTermsAutomate(), 0);
		parallelBusinessRules.addAutomation(new PostingBlockAutomate(), 0);
		parallelBusinessRules.addAutomation(new PSTRegisteredAutomate(), 0);

		return true;
	}
	
	/**
	 * Determines if the user sending the request is AVOS.  It determines it by seeing if the user is part of the internal AVOS role.
	 * 
	 * @param inputSDO
	 * @param businessEntity
	 * @return whether or not the request is coming from avos.
	 */
	public boolean isRequestComingFromAvos(DataObject inputSDO, String businessEntity) {
		if (roles.contains(Constants.ROLE_AVOS_SERVICE_ADMIN))
			return true;
		return false;
	}
	
	
	/**
	 * This method grabs the record data from MDM.  It grabs what the active record is in the orsSDO, and it grabs the 
	 * Promote Preview version of the record in the promotePreviewSDO.
	 * 
	 * @param helperContext
	 * @param interactionId
	 * @param rowidObject
	 */
	private boolean getRecordFromDatabase(HelperContext helperContext, String interactionId, String rowidObject) {
		ValidationError validationError = null;
		try {
			logger.info("GRABBING RECORDS FROM THE DATABASE");
			
			promotePreviewSDO = businessEntityServiceClient.readPromotePreview(interactionId, callContext, compositeServiceClient, helperContext, businessEntity, rowidObject);
			logger.info("PRINTING OUT SDO GRABBED FROM DATABASE");
			dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, orsSDO);
			logger.info("PRINTING OUT PROMOTE PREVIEW");
			dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, promotePreviewSDO);
		} catch (SiperianCommunicationException siperianCommunicationException) {
			validationError = createError(ErrorConstants.CANT_COMMUNICATE_WITH_MDM_ERROR_CODE, ErrorConstants.CANT_COMMUNICATE_WITH_MDM_ERROR_MESSAGE, 
					businessEntity, helperContext.getDataFactory());
		} catch (Exception e) {
			validationError = createError(ErrorConstants.GENERAL_ERROR_CODE, ErrorConstants.GENERAL_ERROR_MESSAGE, 
					businessEntity, helperContext.getDataFactory());
		} finally {
			if (validationError != null) {
				try {
					appendError(validationError, helperContext);
				} catch (StepException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}
		}
		return true;
	}

}
