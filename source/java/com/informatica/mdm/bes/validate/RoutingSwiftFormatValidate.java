package com.informatica.mdm.bes.validate;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.helper.RegexHelper;
import com.informatica.mdm.bes.helper.SupplierSDOHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * If the bank is US then make sure the Swift code field is formatted as a routing number.  If not then throw an error.
 * If the bank is non-us then make sure the swift code has a swift code format.  If not throw an error to the user.
 * 
 * @author mdromazo
 *
 */
public class RoutingSwiftFormatValidate extends Validate {
	// Regex to check valid ISIN Code
    private static final String SWIFT_REGEX = "^[A-Z]{4}[-]{0,1}[A-Z]{2}[-]{0,1}[A-Z0-9]{2}[-]{0,1}[0-9]{3}$";

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);
		
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = null;
		List<DataObject> inputBankList = inputSDOBe.getList(BusinessEntityConstants.BANK + "/item");
		List<DataObject> promoteBankList = null;
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		
		if (promotePreviewSDO != null) {
			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
			promoteBankList = promoteSDOBe.getList(BusinessEntityConstants.BANK + "/item");
		}
		
		if (!shouldRun(inputSDO, inputSDOBe, promoteSDOBe, inputBankList, businessEntity, helperContext))
			return null;
		
		
		for (DataObject inputBank : inputBankList) {
			if (inputBank == null)
				continue;
			
			String inputBankRowid = inputBank.getString(Constants.ROWID_OBJECT);
			DataObject promoteBank = null;
			if (inputBankRowid != null && promoteBankList != null) {
				promoteBank = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteBankList, inputBankRowid);
			}
			
			String country = vendorSDOHelper.getString(inputBank, promoteBank, BusinessEntityConstants.BANK_COUNTRY_CODE);
			if (country == null)
				continue;
			
			if (country.equals("US")) {
				String routingNum = vendorSDOHelper.getString(inputBank, promoteBank, BusinessEntityConstants.BANK_ROUTING_NUMBER);
				if (routingNum == null) {
					validationErrors.add(createError(ErrorConstants.DOMESTIC_BANK_REQUIRE_ROUTING_ERROR_CODE,
							ErrorConstants.DOMESTIC_BANK_REQUIRE_ROUTING_ERROR_MESSAGE, 
							businessEntity + "." + BusinessEntityConstants.BANK, 
							helperContext.getDataFactory()));
				} else if(routingNum.length() != 9) {
					validationErrors.add(createError(ErrorConstants.DOMESTIC_BANK_ROUTING_FORMAT_ERROR_CODE,
							ErrorConstants.DOMESTIC_BANK_ROUTING_FORMAT_ERROR_MESSAGE, 
							businessEntity + "." + BusinessEntityConstants.BANK, 
							helperContext.getDataFactory()));
				}
				
			} else  {
				String swift = vendorSDOHelper.getString(inputBank, promoteBank, BusinessEntityConstants.BANK_SWIFT);
				if (swift == null) {
					validationErrors.add(createError(ErrorConstants.INT_BANK_REQUIRE_SWIFT_ERROR_CODE,
							ErrorConstants.INT_BANK_REQUIRE_SWIFT_ERROR_MESSAGE, 
							businessEntity + "." + BusinessEntityConstants.BANK, 
							helperContext.getDataFactory()));				
				} else if (!RegexHelper.validateSwift(swift)){
					validationErrors.add(createError(ErrorConstants.INTERNATIONAL_BANK_SWIFT_FORMAT_ERROR_CODE,
							ErrorConstants.INTERNATIONAL_BANK_SWIFT_FORMAT_ERROR_MESSAGE, 
							businessEntity + "." + BusinessEntityConstants.BANK, 
							helperContext.getDataFactory()));
				}
			}
			
			
		}
		
		return validationErrors;
	}
	
	private boolean shouldRun(DataObject inputSDO, DataObject inputSDOBe, DataObject promoteSDOBe, 
			List<DataObject> inputBankList, String businessEntity, HelperContext helperContext) {
		if (inputBankList == null)
			return false;
		
		boolean shouldRun = false;
		List<DataObject> inputPromoteCompanyCodeList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.COMPANY_CODE, inputSDO, helperContext);
		List<DataObject> promoteCompanyCodeList = promoteSDOBe.getList(BusinessEntityConstants.COMPANY_CODE + "/item");
		
		List<String> businessUnits = SupplierSDOHelper.getBusinessUnits(inputPromoteCompanyCodeList, promoteCompanyCodeList, dataObjectHelperContext);
		if (businessEntity.equals(Constants.BE_TRADE_SUPPLIER)) {
			if (businessUnits != null && (businessUnits.contains("PSAS") || businessUnits.contains("MSH")))
				shouldRun = true;
		} else if (businessEntity.equals(Constants.BE_NON_TRADE_SUPPLIER)) {
			List<DataObject> inputPromotePrimaryAddress = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.PRIMARY_ADDRESS, inputSDO, helperContext);
			String cntryCd = inputPromotePrimaryAddress.get(0)
					.getString(BusinessEntityConstants.PRIMARY_ADDRESS_POSTAL_ADDRESS + "/" + BusinessEntityConstants.PRIMARY_ADDRESS_CNTRY_CD);
			if (cntryCd.equals("US"))
				shouldRun = true;
		}
		return shouldRun;
	}
}
