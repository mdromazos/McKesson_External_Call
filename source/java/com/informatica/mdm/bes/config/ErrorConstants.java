package com.informatica.mdm.bes.config;

public class ErrorConstants {
	/*
	 * PeopleSoftIDRequired 
	 */
	public static final String PEOPLESOFT_ID_REQUIRED_ERROR_CODE = "ERROR-01";
	public static final String PEOPLESOFT_ID_REQUIRED_ERROR_MESSAGE = "ERROR-01 : PeopleSoft ID must be populated.";
	
	public static final String COMPANY_CODE_MISSING_ERROR_CODE = "ERROR-02";
	public static final String COMPANY_CODE_MISSING_ERROR_MESSAGE = "ERROR-02 : Supplier must have one Company Code";
	
	public static final String ANID_VALIDATE_ERROR_CODE = "ERROR-03";
	public static final String ANID_VALIDATE_ERROR_MESSAGE = "ERROR-03 : There must be an ANID Identifier Type, if Ariba Check Request is 'No'";
	
	public static final String CONTACT_VALIDATE_ERROR_CODE = "ERROR-04";
	public static final String CONTACT_VALIDATE_ERROR_MESSAGE = "ERROR-04 : There must be atleast one Phone and Email child for a Contact";
	
	public static final String PSAS_COI_INSURANCE_VALIDATE_ERROR_CODE = "ERROR-05";
	public static final String PSAS_COI_INSURANCE_VALIDATE_ERROR_MESSAGE = "ERROR-05 : Supplier doing Business with PSAS with Insurance less than 10 Million must upload a Document of Type 'COI Approval'";
	
	public static final String BACK_ORDER_IND_N_VALIDATE_ERROR_CODE = "ERROR-06";
	public static final String BACK_ORDER_IND_N_VALIDATE_ERROR_MESSAGE = "ERROR-06 : Back Order Days cannot be modifed with Back Order Indicator as N";
	
	public static final String BACK_ORDER_IND_Y_VALIDATE_ERROR_CODE = "ERROR-07";
	public static final String BACK_ORDER_IND_Y_VALIDATE_ERROR_MESSAGE = "ERROR-07 : Back Order Days range should be in 10-16 only with Back Order Indicator as Y";
	
	// Maintenance Required Doc Types
	public static final String REQ_DOC_UPLOAD_ERROR_CODE = "ERROR-REQ-%s";
	public static final String REQ_DOC_UPLOAD_ERROR_MESSAGE = "ERROR-REQ-%s : Fields that were changed require a %s Document to be uploaded.";
	public static final String INTERNAL_REQ_DOC_UPLOAD_ERROR_MESSAGE = "ERROR-07 : A document of type: %s is required to be uploaded for changed fields: %s";
	public static final String SUPPLIER_REQ_DOC_MSG = "New Documents Required: %s";
	public static final String SUPPLIER_REQ_DOC_UPLOAD_ERROR_MESSAGE = "ERROR-REQ-%s";
	
	public static final String NARCOTICS_MUST_HAVE_ESIG_ERROR_CODE = "ERROR-09";
	public static final String NARCOTICS_MUST_HAVE_ESIG_ERROR_MESSAGE = "ERROR-09 : Suppliers who sell narcotics must provide an ESIG email.";

	public static final String DOMESTIC_BANK_REQUIRE_ROUTING_ERROR_CODE = "ERROR-10";
	public static final String DOMESTIC_BANK_REQUIRE_ROUTING_ERROR_MESSAGE = "ERROR-10 : Banks within the United States require the Routing Number field to be populated.";

	public static final String INT_BANK_REQUIRE_SWIFT_ERROR_CODE = "ERROR-11";
	public static final String INT_BANK_REQUIRE_SWIFT_ERROR_MESSAGE = "ERROR-11 :  International banks require the Swift field to be populated.";

	public static final String NONTRADE_COI_INSURANCE_VALIDATE_ERROR_CODE = "ERROR-12";
	public static final String NONTRADE_COI_INSURANCE_VALIDATE_ERROR_MESSAGE = "ERROR-12 : Non-Trade Supplier with Insurance less than 5 Million must upload a Document of Type 'COI Approval'";
	
	public static final String MBA_DELAY_DAYS_REQUIRED_ERROR_CODE = "ERROR-13";
	public static final String MBA_DELAY_DAYS_REQUIRED_ERROR_MESSAGE = "ERROR-12 : Net Payment Terms requires field 'MBA Delay Days' to be populated.";
	
	public static final String CANT_COMMUNICATE_WITH_MDM_ERROR_CODE = "CUSTOM-INT01";
	public static final String CANT_COMMUNICATE_WITH_MDM_ERROR_MESSAGE = "CUSTOM-INT01 : The External Calls were unable to communicate with the MDM Server";
	
	public static final String GENERAL_ERROR_CODE = "CUSTOM-INT02";
	public static final String GENERAL_ERROR_MESSAGE = "CUSTOM-INT02 : The External Calls had an error. Please try again or contact the System Administrator.";
	
	public static final String CANT_MODIFY_PREVIOUS_REQUEST_ERROR_CODE = "CUSTOM-CHGVENDOR28";
	public static final String CANT_MODIFY_PREVIOUS_REQUEST_ERROR_MESSAGE = "CUSTOM-CHGVENDOR28 : Cannot modify a previous Request. Please create a new one";
	
	public static final String NEW_REQUEST_MANDATORY_FOR_CHANGE_REQUESTS_ERROR_CODE = "CUSTOM-CHGVENDOR29";
	public static final String NEW_REQUEST_MANDATORY_FOR_CHANGE_REQUESTS_ERROR_MESSAGE = "CUSTOM-CHGVENDOR29 : A request must be created when updating a vendor.  Please create a request and fill in the specific selections";
	
	public static final String CANT_HAVE_MULTIPLE_ACTIVE_REQUESTS_ERROR_CODE = "CUSTOM-CHGVENDOR30";
	public static final String CANT_HAVE_MULTIPLE_ACTIVE_REQUESTS_ERROR_MESSAGE = "CUSTOM-CHGVENDOR30 : Cannot have more than one request created";
	
	public static final String SHORT_FULL_NAME_LENGTH_ERROR_CODE = "CUSTOM-CHGVENDOR31";
	public static final String SHORT_FULL_NAME_LENGTH_ERROR_MESSAGE = "CUSTOM-CHGVENDOR31 : Short Full Name cannot be more than 30 characters when Account Group is set to 'VICO' or 'VHOM'";
	
	public static final String DOMESTIC_BANK_ROUTING_FORMAT_ERROR_CODE = "CUSTOM-CHGVENDOR32";
	public static final String DOMESTIC_BANK_ROUTING_FORMAT_ERROR_MESSAGE = "CUSTOM-CHGVENDOR32 : Routing numbers must be 9 characters long";

	public static final String INTERNATIONAL_BANK_SWIFT_FORMAT_LENGTH_ERROR_CODE = "CUSTOM-CHGVENDOR33";
	public static final String INTERNATIONAL_BANK_SWIFT_FORMAT_LENGTH_ERROR_MESSAGE = "CUSTOM-CHGVENDOR33 : Swift numbers must be 8-11 characters long";

	
	public static final String INTERNATIONAL_BANK_SWIFT_FORMAT_ERROR_CODE = "CUSTOM-CHGVENDOR33";
	public static final String INTERNATIONAL_BANK_SWIFT_FORMAT_ERROR_MESSAGE = "CUSTOM-CHGVENDOR33 : Swift numbers must have a format of AAAA-BB-CC-123";

}

