package com.informatica.mdm.bes.validate;

import java.util.List;
import java.util.Map;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.helper.SDOHelper;
import com.informatica.mdm.bes.helper.SupplierSDOHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * If Payment Terms starts with "Nxxx" and the BU is Trade Pharma, then require MBA Delay Days to be entered.
 * 
 * @author Matthew Dromazos
 *
 */
public class MBADelayDaysValidate extends Validate {

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = promotePreviewSDO != null ? promotePreviewSDO.getDataObject(businessEntity) : null;
		List<DataObject> inputPromoteCompanyCodeList = SDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, 
				BusinessEntityConstants.COMPANY_CODE, inputSDO, helperContext, dataObjectHelperContext);
		
		if (inputPromoteCompanyCodeList == null || inputPromoteCompanyCodeList.isEmpty())
			return null;
		
		List<DataObject> promoteCompanyCodeList = promoteSDOBe != null ? promoteSDOBe.getList(BusinessEntityConstants.COMPANY_CODE + "/item") : null;
		
		List<String> businessUnits = SupplierSDOHelper.getBusinessUnits(inputPromoteCompanyCodeList, promoteCompanyCodeList, dataObjectHelperContext);
		if (!businessUnits.contains("PHARMA") || businessEntity.contains("ExTrade")) {
			return null;
		}
		
		boolean hasError = inputPromoteCompanyCodeList.parallelStream().anyMatch(inputPromoteCompanyCode -> {
			if (inputPromoteCompanyCode == null)
				return false;
			
			String inputPromoteCompanyCodeRowid = inputPromoteCompanyCode.getString(Constants.ROWID_OBJECT);
			DataObject promoteCompanyCode = null;
			if (inputPromoteCompanyCodeRowid != null && promoteCompanyCodeList != null) {
				promoteCompanyCode = dataObjectHelperContext.getDataObjectSearcher()
						.searchDataObjectList(promoteCompanyCodeList, inputPromoteCompanyCodeRowid);
			}
			
			String paymentTerms = SDOHelper.getString(inputPromoteCompanyCode, promoteCompanyCode, BusinessEntityConstants.COMPANY_CODE_PAYMENT_TERMS);
			if (paymentTerms != null && paymentTerms.startsWith("N")) {
				String mbaDelayDays = SDOHelper.getString(inputPromoteCompanyCode, promoteCompanyCode, BusinessEntityConstants.COMPANY_CODE_MBA_DELAY_DAYS);
				if (mbaDelayDays == null)
					return true;
			}
			
			return false;
		});
		
		if (hasError) {
			return createErrors(ErrorConstants.MBA_DELAY_DAYS_REQUIRED_ERROR_CODE, 
					ErrorConstants.MBA_DELAY_DAYS_REQUIRED_ERROR_MESSAGE,
					businessEntity + "." + BusinessEntityConstants.COMPANY_CODE,
					helperContext.getDataFactory());
		}
		
		return null;
	}

}
