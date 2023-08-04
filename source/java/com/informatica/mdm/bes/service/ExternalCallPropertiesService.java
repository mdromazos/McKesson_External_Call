package com.informatica.mdm.bes.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.factory.CompositeServiceClientFactoryImpl;
import com.informatica.mdm.cs.client.CompositeServiceClient;

public class ExternalCallPropertiesService {
	private static Logger logger = Logger.getLogger(ExternalCallPropertiesService.class.getName());
	
	private static final String PROPERTIES_NAME = "/externalcall.properties";
	
	private static final String DATABASE_JDBC_URL = "database.jdbc.url";
	private static final String DATABASE_JDBC_USERNAME = "database.jdbc.username";
	private static final String DATABASE_JDBC_PASSWORD = "database.jdbc.password";
	private static final String AVS_REST_URL = "avs.rest.url";
	
	private InputStream exernalCallProperties;
	
	private Properties config;
	
	public ExternalCallPropertiesService() {
		loadProperties();
	}
	
	public String getDatabaseJdbcUrl() {
		if (this.config == null)
			return null;
		String property = this.config.getProperty(DATABASE_JDBC_URL);
		return property;
	}
	
	public String getDatabaseJdbcUsername() {
		if (this.config == null)
			return null;
		String property = this.config.getProperty(DATABASE_JDBC_USERNAME);
		return property;
	}
	
	public String getDatabaseJdbcPassword() {
		if (this.config == null)
			return null;
		String property = this.config.getProperty(DATABASE_JDBC_PASSWORD);
		return property;
	}
	
	public String getAvsUrl() {
		if (this.config == null)
			return null;
		String property = this.config.getProperty(AVS_REST_URL);
		return property;
	}
	
	
	
	private void loadProperties() {
        try {
    		this.exernalCallProperties = ExternalCallPropertiesService.class.getResourceAsStream(PROPERTIES_NAME);
    		config = new Properties();
            config.load(exernalCallProperties);
        } catch (IOException e) {
        	logger.info("IO EXCEPTION WHILE LOADING CONFIG OF EXTERNAL CALL PROPERTIES");
            throw new RuntimeException(e);
        } catch (Exception e) {
        	logger.info("EXPETION WHILE LOADING EXTERNAL CALL PROPERTIES");
        	e.printStackTrace();
        	throw new RuntimeException(e);
        }
	}
}
