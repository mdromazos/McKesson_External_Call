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
	
	private static final String RUN_MATCHBOOK_PROP_NAME = "externalcall.run.matchbook";
	private static final String IDQ_DEFAULTS_URL_NAME = "idq.defaults.url";
	
	private InputStream exernalCallProperties;
	
	private Properties config;
	
	public ExternalCallPropertiesService() {
		loadProperties();
	}
	
	public boolean getRunMatchbook() {
		if (this.config == null)
			return true;
		String property = this.config.getProperty(RUN_MATCHBOOK_PROP_NAME);
		if (property == null)
			return true;
		return property.equals("true");
	}
	
	public String getIdqDefaultsUrl() {
		if (this.config == null)
			return null;
		return this.config.getProperty(IDQ_DEFAULTS_URL_NAME);
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
