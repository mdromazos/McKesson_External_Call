/**
 * 
 */
package com.informatica.mdm.bes.customlogic;

import java.time.Instant;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.dataobjecthelper.ErrorHelper;
import com.informatica.mdm.bes.factory.CompositeServiceClientFactoryImpl;
import com.informatica.mdm.bes.validate.RequiredDocumentValidate;
import com.informatica.mdm.bes.validate.StateLicensingValidate;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.spi.cs.StepException;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.ServicePhase;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * This is the CustomLogic class to be run when the Business Entity is the Portal View.
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class PortalViewCustomLogicImpl extends CustomLogicImpl {	
	private static Logger logger = Logger.getLogger(PortalViewCustomLogicImpl.class.getName());

	public PortalViewCustomLogicImpl(String businessEntity, 
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
		logger.info("STARTING PROCESS OF PortalViewCustomLogicImpl for be + " + businessEntity);

		startTime = Instant.now();
		try {
			DataObjectHelperContext dataObjectHelperContext = new DataObjectHelperContext();
			dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, inputSDO);
			boolean wasAbleToDecide = decideBusinessRules(helperContext, inputSDO, inParams, outParams);
			if (wasAbleToDecide)
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
	 * This method decides what Validations and Automations need to be run depending on the record state and what request is coming from MDM UI, 
	 * or if it is a call coming from ActiveVOS.
	 */
	@Override
	protected boolean decideBusinessRules(HelperContext helperContext, DataObject inputSDO, Map<String, Object> inParams, Map<String, Object> outParams) {
		logger.info("INSIDE DECIDE BUSINESS RULES");
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		boolean newVendor = inputSDO.getChangeSummary().isCreated(inputSDOBe);
		String interactionId = (String) inParams.get(Constants.INTERACTION_ID);
		String rowidObject = inputSDOBe.getString(Constants.ROWID_OBJECT);
		boolean gotRecords = getRecordFromDatabase(inputSDOBe, helperContext, interactionId, rowidObject);
		if (!gotRecords)
			return false;
		
		parallelBusinessRules.addValidation(new StateLicensingValidate(), 0);
		parallelBusinessRules.addValidation(new RequiredDocumentValidate(), 0);
		
//		if (!newVendor) {
//			boolean gotRecords = getRecordFromDatabase(inputSDOBe, helperContext, interactionId, rowidObject);
//			if (!gotRecords)
//				return false;
//			
//			if (inputSDOBe.getList(VendorMainConstants.PI + "/item") != null) {
//				parallelBusinessRules.addValidation(new RemitToBankValidate(), 0);
//			} else if (inputSDOBe.getList(VendorMainConstants.TAX + "/item") != null) {
//				parallelBusinessRules.addValidation(new W8W9Validate(), 0);
//			}
//			
//			parallelBusinessRules.addValidation(new VNPIOAAddressValidate(), 0);
//			parallelBusinessRules.addValidation(new VNPIOAStateRegionValidate(), 0);			
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
		if (interactionId != null) {
			promotePreviewSDO = businessEntityServiceClient.readPromotePreview(interactionId, callContext, compositeServiceClient, helperContext, businessEntity, rowidObject);			
		} else {
			promotePreviewSDO = businessEntityServiceClient.readExistingRecord(callContext, compositeServiceClient, helperContext, businessEntity, rowidObject);
		}
//		logger.info("PRINTING OUT SDO GRABBED FROM DATABASE");
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, orsSDO);
//		logger.info("PRINTING OUT PROMOTE PREVIEW");
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, promotePreviewSDO);
		return true;
	}

}
