package com.informatica.mdm.bes.customlogic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.automate.Automate;
import com.informatica.mdm.bes.automate.BackOrderIndAutomate;
import com.informatica.mdm.bes.automate.PSTRegisteredAutomate;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.dataobjecthelper.ErrorHelper;
import com.informatica.mdm.bes.validate.ANIDValidate;
import com.informatica.mdm.bes.validate.BackOrderIndValidate;
import com.informatica.mdm.bes.validate.COIValidate;
import com.informatica.mdm.bes.validate.ContactPhoneEmailValidate;
import com.informatica.mdm.bes.validate.ESIGEmailValidate;
import com.informatica.mdm.bes.validate.MaintenanceRequiredDocumentsValidate;
import com.informatica.mdm.bes.validate.RequestValidate;
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

		if (isRequestComingFromAvos())
			return inputSDO;
		
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

		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		String rowidObject = inputSDOBe.getString(Constants.ROWID_OBJECT);
//		Boolean draftSts = getDraftStsView(inputSDO, businessEntity, helperContext, rowidObject);  
//		boolean validateOnly = (boolean) inParams.get(Constants.VALIDATE_ONLY);
		boolean newVendor = inputSDO.getChangeSummary().isCreated(inputSDOBe);
		String interactionId = (String) inParams.get(Constants.INTERACTION_ID);
		
		boolean gotRecords = getRecordFromDatabase(inputSDOBe, helperContext, interactionId, rowidObject);
//		List<String> buList = getBus(inputSDO, helperContext);
		
		if (!newVendor) {
			parallelBusinessRules.addValidation(new ANIDValidate(), 0);
			parallelBusinessRules.addValidation(new COIValidate(), 0);
			parallelBusinessRules.addValidation(new BackOrderIndValidate(), 1);
			parallelBusinessRules.addAutomation(new BackOrderIndAutomate(), 1);
			
			
//			parallelBusinessRules.addValidation(new ContactPhoneEmailValidate(), 0);
		}

		return true;
	}
	
	public List<String> getBus(DataObject inputSDO, HelperContext helperContext) {

		DataObject promoteSDOBe = promotePreviewSDO != null ? promotePreviewSDO.getDataObject(businessEntity) : null;
		List<DataObject> inputPromoteCompanyCodeList = vendorSDOHelper.getCombinedList(inputSDO, promoteSDOBe, VendorMainConstants.COMPANY_CODE, inputSDO, helperContext);
		return inputPromoteCompanyCodeList.parallelStream().map((DataObject inputPromoteCoCd) -> {
			return inputPromoteCoCd.getString(VendorMainConstants.COMPANY_CODE_BUSINESS_UNIT);
		}).collect(Collectors.toList());
	}
	
	/**
	 * Determines if the user sending the request is AVOS.  It determines it by seeing if the user is part of the internal AVOS role.
	 * 
	 * @param inputSDO
	 * @param businessEntity
	 * @return whether or not the request is coming from avos.
	 */
	public boolean isRequestComingFromAvos() {
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
	private boolean getRecordFromDatabase(DataObject inputSDOBe, HelperContext helperContext, String interactionId, String rowidObject) {
//		logger.info("GRABBING RECORDS FROM THE DATABASE");
		if (interactionId != null) {
			promotePreviewSDO = businessEntityServiceClient.readPromotePreview(interactionId, callContext, compositeServiceClient, helperContext, businessEntity, rowidObject);
		} else {
			promotePreviewSDO = businessEntityServiceClient.readExistingRecord(callContext, compositeServiceClient, helperContext, businessEntity, rowidObject);
		}
//		dbSDO = businessEntityServiceClient.readDBExistingRecord(callContext, compositeServiceClient, externalCallRequest, helperContext, businessEntity, rowidObject);
		
//		logger.info("PRINTING OUT PROMOTE PREVIEW");
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, businessEntity, promotePreviewSDO);
		
		return true;
	}
}
