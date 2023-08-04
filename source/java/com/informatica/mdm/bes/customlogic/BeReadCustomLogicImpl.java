package com.informatica.mdm.bes.customlogic;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.automate.MaskAutomate;
import com.informatica.mdm.bes.dataobjecthelper.ErrorHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.spi.cs.StepException;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.ServicePhase;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class BeReadCustomLogicImpl extends CustomLogicImpl {
	
	private static Logger logger = Logger.getLogger(BeReadCustomLogicImpl.class.getName());
	private static final String[] MASK_FOR_USER_ROLES = {"SupplierLiaisonMMS"};
	
	public BeReadCustomLogicImpl(String businessEntity, ServicePhase phase,
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

	@Override
	protected boolean decideBusinessRules(HelperContext helperContext, DataObject inputSDO,
			Map<String, Object> inParams, Map<String, Object> outParams) {
		if (maskForUser()) parallelBusinessRules.addAutomation(new MaskAutomate(roles), 0);

		return true;
	}
	
	private boolean maskForUser() {
		for (int i = 0; i < MASK_FOR_USER_ROLES.length; i++) {
			if (roles.contains(MASK_FOR_USER_ROLES[i]))
				return true;
		}
		return false;
	}
}
