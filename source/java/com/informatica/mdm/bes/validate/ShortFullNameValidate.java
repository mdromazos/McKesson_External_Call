package com.informatica.mdm.bes.validate;

import java.util.List;
import java.util.Map;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.config.LookupConstants;
import com.informatica.mdm.bes.helper.SDOHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class ShortFullNameValidate extends Validate {
	
	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		// TODO Auto-generated method stub
		pauseLogging(inputSDO);
		
		DataObject promoteSDOBe = null;
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);

		if (promotePreviewSDO != null)
			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
		
		String accntGrp = SDOHelper.getString(inputSDOBe, promoteSDOBe, BusinessEntityConstants.ACCNT_GRP_CD);
		String shrtFullNm = SDOHelper.getString(inputSDOBe, promoteSDOBe, BusinessEntityConstants.SHORT_FULL_NAME);
		if (accntGrp == null || shrtFullNm == null)
			return null;
		
		if ((accntGrp.equals(LookupConstants.ACCNT_GRP_VICO_K) || accntGrp.equals(LookupConstants.ACCNT_GRP_VHOM)) && shrtFullNm.length() > 30) {
			return createErrors(ErrorConstants.SHORT_FULL_NAME_LENGTH_ERROR_CODE,
					ErrorConstants.SHORT_FULL_NAME_LENGTH_ERROR_MESSAGE,
					businessEntity + "." + BusinessEntityConstants.SHORT_FULL_NAME, 
					helperContext.getDataFactory());
		}
		return null;
	}

}
