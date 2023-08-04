package com.informatica.mdm.bes.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.domain.BusinessRuleReturn;
import com.informatica.mdm.bes.factory.BusinessRuleReturnFactory;
import com.informatica.mdm.bes.validate.Validate;


public class BusinessRulesService {
	private DatabaseClient databaseClient;
	public static final String QUERY = "SELECT * FROM SUPPLIER_HUB.C_L_BSNS_RULES";
	
	protected static Logger logger = Logger.getLogger(BusinessRulesService.class.getName());
	
	public BusinessRulesService() {
		databaseClient = new DatabaseClient();
	}
	
	public List<BusinessRuleReturn> getBusinessRulesReturn() {
		ResultSet resultSet = callDB();
		return createList(resultSet);		
	}
	
	
	public ResultSet callDB()
	{
		ResultSet rs = null;
		Connection connection = databaseClient.getConnectionSimple();
		Statement statement;
		try {
			statement = connection.createStatement();
			rs = statement.executeQuery(QUERY);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	public List<BusinessRuleReturn> createList(ResultSet rs) {
		List<BusinessRuleReturn> businessRuleReturnList = new ArrayList<BusinessRuleReturn>();
		try {
			while (rs.next()) {
				logger.info("RESULT SET: " + rs.toString());
				BusinessRuleReturn businessRuleReturn = BusinessRuleReturnFactory.createBusinessRuleReturn(rs);
				if (businessRuleReturn != null) {
					businessRuleReturnList.add(businessRuleReturn);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return businessRuleReturnList;
	}
	
}
