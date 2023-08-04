package com.informatica.mdm.bes.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;

import commonj.sdo.DataObject;

public class SupplierSDOHelper {
	
	
	/**
	 * Get a list of all business units onboarded to the supplier.
	 * @param inputPromoteCompanyCodeList
	 * @param promoteCompanyCodeList
	 * @return List<String> of all Business Unit Codes
	 */
	public static List<String> getBusinessUnits(List<DataObject> inputPromoteCompanyCodeList, 
			List<DataObject> promoteCompanyCodeList, DataObjectHelperContext dataObjectHelperContext) {
		List<String> businessUnits = new ArrayList<String>();
		if (inputPromoteCompanyCodeList == null || inputPromoteCompanyCodeList.isEmpty())
			return businessUnits;
		
		for (DataObject inputPromoteCompanyCode : inputPromoteCompanyCodeList) {
			String inputPromoteCompanyCodeRowid = inputPromoteCompanyCode.getString(BusinessEntityConstants.ROWID_OBJECT);
			DataObject promoteCompanyCode = null;
			if (inputPromoteCompanyCodeRowid != null && promoteCompanyCodeList != null)
				promoteCompanyCode = dataObjectHelperContext.getDataObjectSearcher()
					.searchDataObjectList(promoteCompanyCodeList, inputPromoteCompanyCodeRowid);
			
			String businessUnit = SDOHelper.getString(inputPromoteCompanyCode, promoteCompanyCode, 
						BusinessEntityConstants.COMPANY_CODE_BSNS_UNIT + "/" + BusinessEntityConstants.COMPANY_CODE_BSNS_UNIT_CD);
			if (businessUnit != null)
				businessUnits.add(businessUnit);
			
		}
		
		return businessUnits;
	}
	
	/**
	 * Get a list of all business units onboarded to the supplier.
	 * @param inputPromoteCompanyCodeList
	 * @param promoteCompanyCodeList
	 * @return List<String> of all Business Unit Codes
	 */
	public static List<String> getSubTeams(List<DataObject> inputPromoteCompanyCodeList,
			List<DataObject> promoteCompanyCodeList, DataObjectHelperContext dataObjectHelperContext) {
		if (inputPromoteCompanyCodeList == null || inputPromoteCompanyCodeList.isEmpty())
			return null;
		
		List<String> subTeams = inputPromoteCompanyCodeList.parallelStream()
		.map(inputPromoteCompanyCode -> {
			if (inputPromoteCompanyCode == null)
				return null;
			
			String subTeam = null;
			String inputPromoteCompanyCodeRowid = inputPromoteCompanyCode.getString(Constants.ROWID_OBJECT);
			DataObject promoteCompanyCode = null;
			if (inputPromoteCompanyCodeRowid != null) {
				promoteCompanyCode = dataObjectHelperContext.getDataObjectSearcher()
					.searchDataObjectList(promoteCompanyCodeList, inputPromoteCompanyCodeRowid);
			}
			subTeam = SDOHelper.getString(inputPromoteCompanyCode, promoteCompanyCode, 
					BusinessEntityConstants.COMPANY_CODE_SUB_TEAM + "/" + BusinessEntityConstants.COMPANY_CODE_SUB_TEAM_CD);
			if (subTeam == null)
				return null;
			return subTeam;
						
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toList());
		
		return subTeams;
	}
	
}
