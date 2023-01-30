package com.informatica.mdm.bes.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.service.CustomLogicService;
import com.informatica.mdm.cs.client.CompositeServiceClient;

/**
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class CompositeServiceClientFactoryImpl implements CompositeServiceClientFactory {
	
	private static Logger logger = Logger.getLogger(CompositeServiceClientFactoryImpl.class.getName());
		
	private String besClientFilepath;
	private InputStream besClientFile;
	
	public CompositeServiceClientFactoryImpl(String besClientFilepath) {
		this.besClientFilepath = besClientFilepath;
		this.besClientFile = CustomLogicService.class.getResourceAsStream(this.besClientFilepath);
	}
	
	public CompositeServiceClientFactoryImpl() { }
	
	public CompositeServiceClient createCompositeServiceClient() {
        logger.info("************CREATING COMPOSITE SERVICE CLIENT****************");
        
        Properties config = new Properties();
        try {
            config.load(besClientFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return CompositeServiceClient.newCompositeServiceClient(config);
	}
	
}
