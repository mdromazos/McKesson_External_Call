package com.informatica.mdm.bes.config;

/**
 * This class contains constants values for Field Names of the Vendor Main Business Entity View for HFC and HFLS
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class VendorMainConstants {
	
	// Root Children
	public static final String COMPANY_CODE = "SupplierCompanyCd";
	public static final String DOCUMENT = "SupplierDocuments";
	public static final String PURCHASE_ORG = "SupplierPurchaseOrg";
	public static final String ALTERNATE_ID = "AlternateId";
	public static final String ALTERNATE_ID_TYPE = "altIdTyp/idTyp";
	public static final String BUSINESS_UNIT = "bsnsUnit/bsnsUnitCd";
	public static final String ANID = "ANID";
	public static final String CONTACT ="Contacts";
	public static final String CONTACT_PHONE = "PhoneCommunication";
	public static final String CONTACT_ELECTRONIC_ADDRESS = "ContactElectronicAddress";
	public static final String INSURANCE = "Insurances";
	public static final String REQUEST = "Requests";
	public static final String ELECTRONIC_ADDR = "ElectronicAddress";
	public static final String PRIMARY_ADDR = "PrimaryAddress";
	public static final String PRIMARY_ADDR_POSTAL_ADDR = "PostalAddr";
	
	public static final String PRIMARY_ADDR_POSTAL_ADDR_CNTRY = "cntryCd/countryCode";
	
	// Root Fields
	public static final String ACCNT_GRP_CD = "accntGrp/accntGrpCd";
	public static final String ARIBA_CHECK_REQ = "aribaCheckReq/aribaChkReqCd";
	public static final String MNR_IND = "mnrInd/mndrIndCd";

	// Company Code Fields
	public static final String COMPANY_CODE_COMPANY_CODE = "cmpnyCd/coCd";
	public static final String COMPANY_CODE_BUSINESS_UNIT = "bsnsUnit/bsnsUnitCd";
	
	// Document Fields
	public static final String DOCUMENT_DOC_TYP = "docType/docTypCd";

	// Purchase Org Fields
	public static final String PURCHASE_ORG_PURCHASE_ORG = "PurchaseOrg";
	public static final String PURCHASE_ORG_PURCHASE_ORG_CD = "PurchaseOrg/purchOrgCd";
	
	public static final double PSAS_INSURANCE_VALUE = 10000000;
	public static final double NON_TRADE_INSURANCE_VALUE = 5000000;
	
	public static final String SUPPLIER_BU_APPR_DOCUMENT_DOC_TYP = "docTyp/docTypCd";
	public static final String SUPPLIER_BU_APPR_ATTRIBUTE_APPR = "SupplierAttributeApprover";


	public static final String INSURANCE_INSURANCE_VAL = "insrncVal";
	public static final String INSURANCE_BSNS_UNIT_CD = "bsnsUnit/bsnsUnitCd";
	
	public static final String ELECTRONIC_ADDR_ETRNC_TYP = "etrncTyp/etrncTypCd";

}
