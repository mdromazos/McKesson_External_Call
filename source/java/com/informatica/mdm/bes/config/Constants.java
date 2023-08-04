package com.informatica.mdm.bes.config;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.api.CompositeServiceException;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.CoFilterNode;
import com.informatica.mdm.sdo.cs.base.Key;
import com.informatica.mdm.sdo.cs.base.ValidationError;
import com.informatica.mdm.spi.cs.StepException;


import commonj.sdo.ChangeSummary;
import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;
import mdm.informatica.cs_ors.*;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.ChangeSummary.Setting;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public class Constants {

	protected Logger logger = Logger.getLogger(this.getClass());
	public static final String BE_REL_TO = "to";
	public static final String BE_REL_FROM = "from";
	public static final String UPDATE = "UPDATE";
	public static final String INSERT = "INSERT";
	public static final String DELETE = "DELETE";
	public static final String NO_ACTION = "NO_ACTION";
	public static final String DIRECT = "DIRECT";
	public static final String INDIRECT = "INDIRECT";
	public static final String YES = "Y";
	public static final String TRUE = "TRUE";
	public static final String FALSE = "FALSE";
	public static final String INACTIVE = "I";
	public static final String NO = "N";
	public static final Integer maxErrorThreshold = 20;
	
	public static final String BES_CLIENT_FILEPATH = "/bes-client.properties";
	public static final String APP_NAME = "e360";
	public static final String USERNAME = "admin";
	public static final String APP_USERNAME = APP_NAME + "/" + USERNAME;
	public static final boolean DEBUG = true;

    public static final String PARAMETERS = "parameters";
    public static final String READ_ENTITY_PARAMETERS = "ReadEntityParameters";
    
    public static final String CO_FILTER = "coFilter";
    public static final String OBJECT = "object";
    public static final String ACTIVE = "ACTIVE";
    public static final String PENDING = "PENDING";
    public static final String VALIDATE_ONLY = "validateOnly";
    public static final String INTERACTION_ID = "interactionId";
    public static final String RECORD_STATE = "recordState";
    public static final String ROWID_OBJECT = "rowidObject";

 	public static final String ROLE_APP_ADMIN = "ApplicationAdministrator";
 	public static final String ROLE_AVOS_SERVICE_ADMIN = "AvosServiceAdmin";
 	public static final String ROLE_MULESOFT_SERVICE_ADMIN = "MuleSoftServiceAdmin";
 	
 	public static final String BE_TRADE_SUPPLIER = "ExTradeSupplier";
 	public static final String BE_NON_TRADE_SUPPLIER = "ExNonTradeSupplier";
 	
 	public static final String BE_TRADE_SUPPLIER_VIEW = "ExTradeSupplierAvosView";
 	public static final String BE_NON_TRADE_SUPPLIER_VIEW = "ExNonTradeSupplierView";
 	
 	public static final String BE_TRADE_REGIST_VIEW = "ExTradeSupplierRegistrationView";
 	public static final String BE_NONTRADE_REGIST_VIEW = "ExNonTradeSupplierRegistrationView";

 	
 	public static final String BE_TRADE_PORTAL_VIEW = "ExTradeSupplierPortalView";
 	public static final String BE_NON_TRADE_PORTAL_VIEW = "ExNonTradeSupplierPortalView";
 	
 	public static final String BE_SUPPLIER_ATTRIBUTE_APPROVER = "ExSupplierAttributeApprover";
	public static final String BE_SUPPLIER_BU_APPROVER = "ExSupplierBusinessUnitApprover";

 	
 	public static final String BU_PSAS = "PSAS";
 	public static final String BU_USON = "USON";
 	public static final String BU_MMS = "MMS";
 	public static final String BU_WELLCA = "WELLCA";
 	public static final String BU_PHARMA = "PHARMA";
 	public static final String BU_RETAIL = "RETAIL";
 	public static final String BU_MSD = "MSD";
 	public static final String BU_CMM = "CMM";
 	public static final String BU_TECH_SOLUTIONS = "TECH SOLUTIONS";
 	
 	public static final String DOC_TYPE_COI_APPROVAL = "COI Approval";

//	if (businessUnitList.contains("PSAS") || businessUnitList.contains("Non-Trade")) {
 }
