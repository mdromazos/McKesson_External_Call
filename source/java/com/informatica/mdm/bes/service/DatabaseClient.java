package com.informatica.mdm.bes.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class DatabaseClient {
	protected static Logger logger = Logger.getLogger(DatabaseClient.class.getName());

   private ExternalCallPropertiesService externalCallPropertiesService;
   
   public DatabaseClient() {
	   externalCallPropertiesService = new ExternalCallPropertiesService();
   }
   
   public Connection getConnection() {
	   logger.info("JDBC CONNECTION: " + externalCallPropertiesService.getDatabaseJdbcUrl());
	   Connection result = null;
	   try {
		   Context initialContext = new InitialContext();
		   DataSource datasource = (DataSource)initialContext.lookup(externalCallPropertiesService.getDatabaseJdbcUrl());
		   if (datasource == null) {
			   // TODO LOG
			   logger.info("DATASOURCE NULL");
		   } else {
			   result = datasource.getConnection();
		   }
		   
	   } catch (Exception e) {
		   // TODO PRINT EXCEPTION
		   logger.info("EXCEPTION: " + e.getStackTrace());
	   }
	   return result;
   }
   
   public Connection getConnectionSimple() {
	   logger.info("JDBC CONNECTION: " + externalCallPropertiesService.getDatabaseJdbcUrl());
	   logger.info("JDBC USER: " + externalCallPropertiesService.getDatabaseJdbcUsername());
	   logger.info("JDBC PASS: " + externalCallPropertiesService.getDatabaseJdbcPassword());
	   String mdmDbUrl = externalCallPropertiesService.getDatabaseJdbcUrl();
	   String user = externalCallPropertiesService.getDatabaseJdbcUsername();
	   String password = externalCallPropertiesService.getDatabaseJdbcPassword();
	   
	   try {
		   return DriverManager.getConnection(mdmDbUrl, user, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("ERROR GETTING CONNECTION: " + e.getMessage());
			
			logger.info(e.toString());
		}
	   return null;
   }
	
//	public static void main(String[] args) {
//		// Open a connection
//		try (Connection conn = DriverManager.getConnection(MDM_DB_URL, USER, PASS);
//			Statement stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery(QUERY);) {
//			while (rs.next()) {
//				 System.out.print("ID: " + rs);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}		
//	}
	
	
		
		
}
