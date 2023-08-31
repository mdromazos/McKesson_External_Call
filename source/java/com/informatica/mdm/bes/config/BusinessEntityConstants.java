package com.informatica.mdm.bes.config;

/**
 * This class contains constants values for Field Names at the Business Entity level for the HFC and HFLS Business Entity
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 *
 */
public class BusinessEntityConstants {
	
	public static final String ALTERNATE_ID 		   = "AlternateId";
	public static final String REQUEST 				   = "Requests";
	public static final String PORTAL_USERS 		   = "Contacts";
	public static final String PORTAL_STATE 		   = "";
	public static final String TAX 					   = "TaxInformation";
	public static final String PURCHASE_ORG 		   = "PurchaseOrg";
	public static final String DOCUMENTS 			   = "Document";
	public static final String SUPPLIER_DOCUMENTS 	   = "SupplierDocuments";
	public static final String REQUEST_APPROVAL_STATUS = "RequestApprovalStatus";
	public static final String BANK 				   = "BankDetails";
	public static final String ADDRESS 				   = "Address";
	public static final String LOCATION 			   = "SupplierLocation";
	public static final String CONTACT 				   = "Contacts";
	public static final String COMPANY_CODE 		   = "CompanyCode";
	public static final String ELECTRONIC_ADDR 		   = "ElectronicAddress";
	public static final String PRIMARY_ADDRESS 		   = "PrimaryAddress";
	
	public static final String ROWID_OBJECT   		   = "rowidObject";
	public static final String INTERACTION_ID 		   = "interactionId";
	public static final String HUB_STATE_IND  		   = "hubStateInd";
	public static final String DRAFT_STS 			   = "";
	public static final String PST_REGISTERED	 	   = "pstReg";
	public static final String ACCOUNT_GRP	 	   	   = "accntGrp/accntGrpCd";
	public static final String VENDOR_STATUS	 	   = "vndrSts/vndrStsCd";
	public static final String POSTING_BLOCK	 	   = "pstngBlk";
	public static final String CENTRAL_POSTING_BLOCK   = "cntrlPstngBlck";
	public static final String PURCHASING_BLOCK	 	   = "prchBlck";
	public static final String CSOS_CNTRL_SYS_ORDR	   = "csosOrdering";
	public static final String ACCNT_GRP_CD = "accntGrp/accntGrpCd";
	public static final String SHORT_FULL_NAME = "shrtFullNm";
	public static final String SPLR_BSNS_TYP 		   = "splrBsnsTyp";
	
	public static final String VENDOR_ITEM_TYPE_1	 	   	   = "vndrItemTypOne/vndrItemTypCd";
	public static final String VENDOR_ITEM_TYPE_3	 	   	   = "vndrItemTypThr/vndrItemTypThreeCd";
	
	public static final String REQ_DOC_MSG			   = "reqDocMsg"; 
	
	public static final String TAX_TAX_NUM 			 = "taxNum";			
	public static final String TAX_TAX_TYPE = "taxNumTyp";
	public static final String TAX_TAX_TYPE_CD = "taxNumTyp/taxNumberTypeCode";
	public static final String TAX_PYR_TYP = "taxpyrTyp/taxpayerTypeCode";
	public static final String TAX_NUM_TYP = "taxNumTyp/taxNumberTypeCode";
	
	
	public static final String BANK_ROUTING_NUMBER  			= "routingNum";
	public static final String BANK_SWIFT	  					= "swift";
	public static final String BANK_VERIFICATION_CODE 			= "validationCd";
	public static final String BANK_VERIFICATION_MESSAGE  		= "validationMsg";
	public static final String BANK_AUTHENTICATION_CODE 		= "automationCd";
	public static final String BANK_AUTHENTICATION_MESSAGE  	= "automationMsg";
	
	public static final String DOCUMENT_DOC_TYPE = "docType/docTypCd";
	
	public static final String ADDRESS_POSTAL_ADDRESS  = "PostalAddress";
	public static final String ADDRESS_SEQUENCE_NUMBER = "seqNum";

	public static final String LOCATION_LOCATION = "Location";
	public static final String LOCATION_SEQUENCE_NUMBER = "seqNum";
	public static final String LOCATION_LOCATION_BANK = "LocationBank";
	
	public static final String CONTACT_CONTACT = "contacts";
	public static final String CONTACT_SEQUENCE_NUMBER = "seqNum";
	
	public static final String BANK_SEQUENCE_NUMBER = "seqNum";
	public static final String BANK_COUNTRY_CODE = "cntry/countryCode";
	
	public static final String COMPANY_CODE_BSNS_UNIT 		   	   = "bsnsUnit";
	public static final String COMPANY_CODE_BSNS_UNIT_CD 		   = "bsnsUnitCd";

	public static final String COMPANY_CODE_COMPANY_CODE 		   = "cmpnyCd";
	public static final String COMPANY_CODE_COMPANY_CODE_CD 	   = "coCd";
	public static final String COMPANY_CODE_COMPANY_CODE_CD_LKP    = "cmpnyCd/coCd";
	
	public static final String COMPANY_CODE_SUB_TEAM 		   	   = "subTeam";
	public static final String COMPANY_CODE_SUB_TEAM_CD 		   = "subTeamCd";
	public static final String COMPANY_CODE_PAYMENT_BLOCK 		   = "pymntBlck";
	public static final String COMPANY_CODE_PAYMENT_TERMS		   = "pymntTrms/pymntTrmsCd";
	public static final String COMPANY_CODE_PAYMENT_TERMS_LKP	   = "pymntTrms";
	public static final String COMPANY_CODE_PAYMENT_TERMS_CD	   = "pymntTrmsCd";
	
	public static final String COMPANY_CODE_CREDIT_MEMO_PAYMENT_TERMS_LKP	   = "creditMemoPymntTrm";
	public static final String COMPANY_CODE_CREDIT_MEMO_PAYMENT_TERMS_CD	   = "pymntTrmsCd";
	
	public static final String COMPANY_CODE_COMPANY_CODE_COUNTRY	   = "cmpnyCdCntry";
	public static final String COMPANY_CODE_COMPANY_CODE_COUNTRY_CD	   = "countryCode";
	
	public static final String COMPANY_CODE_PAYMENT_METHOD	   	   = "pymntMtdh";
	public static final String COMPANY_CODE_PAYMENT_METHOD_CD	   = "ExPymntMthdCd";

	public static final String COMPANY_CODE_MBA_DELAY_DAYS		   = "mbaDlyDays";

	public static final String COMPANY_CODE_POSTING_BLOCK		   = "pstngBlck";
	
	
	public static final String PURCHASE_ORG_BSNS_UNIT 		   	   = "bsnsUnit";
	public static final String PURCHASE_ORG_BSNS_UNIT_CD 		   = "bsnsUnitCd";

	public static final String PURCHASE_ORG_PORG 		   	   	   = "purchOrg";
	public static final String PURCHASE_ORG_PORG_CD 		       = "prchOrgCd";

	
	public static final String PURCH_ORG_BACK_ORDER_DAYS	       = "backOrderDays";	
	public static final String PURCH_ORG_BACK_ORDER_IND		       = "backOrderInd";
	public static final String PURCH_ORG_PURCH_BLOCK		       = "purchasingBlock";
	
	public static final String SUPPLIER_BU_APPR_DOCUMENT_DOC_TYP   = "docTyp/docTypCd";
	public static final String SUPPLIER_BU_APPR_ATTRIBUTE_APPR	   = "SupplierAttributeApprover";
	
	public static final String ATTR_APRVR_ATTR_LABEL 			   = "attributeLabel";
	
	public static final String DOCUMENT_DOC_TYP 				   = "docType/docTypCd";
	public static final String DOCUMENT_DOC_TYP_LKP 			   = "docType";
	public static final String DOCUMENT_DOC_TYP_LKP_CD 			   = "docTypCd";
	
	public static final String SUPPLIER_DOCUMENTS_DOC_TYP 		   = "docTyp/docTypCd";

	
	
	// BU Maintenance Attributes
	public static final String BU_MNTNC_DOCUMENT_DOC_TYP 		   = "docTyp/docTypCd";
	
	public static final String PRIMARY_ADDRESS_CNTRY_CD 		   = "cntryCd/countryCode";
	public static final String PRIMARY_ADDRESS_POSTAL_ADDRESS 	   = "PostalAddress";

	public static final String REQUEST_SEND_TO_PORTAL 			   = "sendToPrtl";

	
}
