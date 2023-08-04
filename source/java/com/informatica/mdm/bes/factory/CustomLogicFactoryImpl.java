package com.informatica.mdm.bes.factory;

import java.util.List;







import java.util.Map;

import org.apache.log4j.Logger;
import com.informatica.mdm.spi.cs.StepException;
import com.informatica.mdm.spi.externalcall.CustomLogic;
import com.informatica.mdm.spi.externalcall.CustomLogicFactory;
import com.informatica.mdm.spi.externalcall.ServicePhase;
import com.informatica.mdm.spi.externalcall.Trigger;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.customlogic.BeCustomLogicImpl;
import com.informatica.mdm.bes.customlogic.BeReadCustomLogicImpl;
import com.informatica.mdm.bes.customlogic.PortalViewCustomLogicImpl;
import com.informatica.mdm.bes.customlogic.RegistViewCustomLogicImpl;
import com.informatica.mdm.bes.customlogic.ViewCustomLogicImpl;
import com.informatica.mdm.bes.domain.BusinessRules;

/**
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class CustomLogicFactoryImpl implements CustomLogicFactory {
    public static final Logger log = Logger.getLogger(CustomLogicFactoryImpl.class); 

    private static final CustomLogic EMPTY_LOGIC = new EmptyLogic();

    private CompositeServiceClient besClient;

    public CustomLogicFactoryImpl(CompositeServiceClient besClient) {
        this.besClient = besClient;
    }
    
    @Override
    public CustomLogic create(ExternalCallRequest externalCallRequest) throws StepException {
    	throw new StepException("call context is req");
    }
    
    @Override
    public CustomLogic create(ExternalCallRequest externalCallRequest, CallContext callContext) throws StepException {
    	Trigger trigger = externalCallRequest.getTrigger();
    	
        String businessEntity = trigger.getBusinessEntity();
        ServicePhase phase = trigger.getServicePhase();
        List<String> roles = externalCallRequest.getHeader().getRole();
        log.debug("IN CREATE CUSTOM LOGIC FACTORY IMPL");
        log.debug(phase.toString());
        log.debug(businessEntity);

        switch (phase) {
            case PREVIEW_MERGE_CO_BEFORE_EVERYTHING:
            	break;
            case WRITE_CO_BEFORE_EVERYTHING:
            	break;
            case WRITE_CO_BEFORE_VALIDATE:
                break;
            case WRITE_CO_AFTER_VALIDATE:
            	if (Constants.BE_TRADE_SUPPLIER.equals(businessEntity) || Constants.BE_NON_TRADE_SUPPLIER.equals(businessEntity)) {
            		return new BeCustomLogicImpl(businessEntity, phase, externalCallRequest, callContext, besClient, roles); 
            	}
            case WRITE_CO_AFTER_EVERYTHING:
    			break;
            case WRITE_VIEW_AFTER_VALIDATE:
            	if (Constants.BE_TRADE_SUPPLIER_VIEW.equals(businessEntity) || Constants.BE_NON_TRADE_SUPPLIER_VIEW.equals(businessEntity)) {
            		return new ViewCustomLogicImpl(businessEntity, phase, externalCallRequest, callContext, besClient, roles);
            	} else if (Constants.BE_TRADE_REGIST_VIEW.equals(businessEntity) || Constants.BE_NONTRADE_REGIST_VIEW.equals(businessEntity)) {
            		return new RegistViewCustomLogicImpl(businessEntity, phase, externalCallRequest, callContext, besClient, roles); 
            	} else if (Constants.BE_TRADE_PORTAL_VIEW.equals(businessEntity) || Constants.BE_NON_TRADE_PORTAL_VIEW.equals(businessEntity)) {
            		return new PortalViewCustomLogicImpl(businessEntity, phase, externalCallRequest, callContext, besClient, roles); 
            	}
            case WRITE_VIEW_BEFORE_EVERYTHING:
            	break;
            case WRITE_VIEW_AFTER_EVERYTHING:
            	break;
            case READ_VIEW_AFTER_EVERYTHING:
        		break;
            case READ_CO_AFTER_EVERYTHING:
        		return new BeReadCustomLogicImpl(businessEntity, phase, externalCallRequest, callContext, besClient, roles);
            default:
                
        }
        return EMPTY_LOGIC; // this one will do nothing
    }

    private static class EmptyLogic implements CustomLogic {

        public static final DataObject OBJECT = null;

        @Override
        public DataObject process(HelperContext helperContext, DataObject dataObject, Map<String, Object> map,
                Map<String, Object> map1) throws StepException {
            return OBJECT;
        }
    }
}
