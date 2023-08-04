package com.informatica.mdm.bes.factory;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.informatica.mdm.bes.domain.BusinessRuleReturn;

public class BusinessRuleReturnFactory {

	public static BusinessRuleReturn createBusinessRuleReturn(ResultSet rs) {
		BusinessRuleReturn businessRuleReturn = null;
		
		try {
			String node = rs.getString(3);
			String field = rs.getString(4);
			String companyCode = rs.getString(5);
			String purchaseOrg = rs.getString(6);
			String mandatoryStr = rs.getString(7);
			String displayOnlyStr = rs.getString(8);
			boolean mandatory = mandatoryStr != null && mandatoryStr.equals("Y");
			boolean displayOnly = displayOnlyStr != null && displayOnlyStr.equals("Y");
			businessRuleReturn = new BusinessRuleReturn(node, field, companyCode, purchaseOrg, mandatory, displayOnly);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return businessRuleReturn;
	}
	
}
