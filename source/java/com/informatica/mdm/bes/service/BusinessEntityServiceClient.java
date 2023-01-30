package com.informatica.mdm.bes.service;

import java.io.BufferedReader;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//import org.apache.commons.httpclient.util.HttpURLConnection;
import com.informatica.mdm.cs.client.HttpCompositeServiceClient;
import org.apache.log4j.Logger;

import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.factory.CompositeServiceClientFactoryImpl;
import com.informatica.mdm.bes.helper.StringHelper;
import com.informatica.mdm.bes.helper.TimeHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.api.CompositeServiceException;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.Pager;
import com.informatica.mdm.sdo.cs.base.BusinessEntityKey;
import com.informatica.mdm.sdo.cs.base.CoFilter;
import com.informatica.mdm.sdo.cs.base.CoFilterNode;
import com.informatica.mdm.sdo.cs.base.FuzzyFilterList;
import com.informatica.mdm.sdo.cs.base.Key;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;
import mdm.informatica.cs_ors.PromotePreview;
import mdm.informatica.cs_ors.PromotePreviewParameters;
import mdm.informatica.cs_ors.ReadEntity;
import mdm.informatica.cs_ors.ReadEntityParameters;
import mdm.informatica.cs_ors.MatchCO;
import mdm.informatica.cs_ors.MatchCOParameters;
import mdm.informatica.cs_ors.MatchCOReturn;
import mdm.informatica.cs_ors.MatchCOReturnImpl;
import com.informatica.mdm.sdo.cs.base.FuzzyFilter;


public class BusinessEntityServiceClient {
	
	private static Logger logger = Logger.getLogger(BusinessEntityServiceClient.class.getName());
	
	public DataObject readExistingRecord(CallContext callContext, CompositeServiceClient client, ExternalCallRequest externalCallRequest,
			HelperContext helperContext, String businessEntity, String rowid) {
		
		if (rowid != null && !rowid.trim().isEmpty()) {
			Type readEntityType = helperContext.getTypeHelper().getType("urn:cs-ors.informatica.mdm", "ReadEntity");
	        Type readParametersType = helperContext.getTypeHelper().getType("urn:cs-ors.informatica.mdm", Constants.PARAMETERS);

			
			DataObject readBERequest = helperContext.getDataFactory().create(readEntityType);
			DataObject readBEParameters = readBERequest.createDataObject(Constants.PARAMETERS);
			DataObject coFilter = readBEParameters.createDataObject(Constants.CO_FILTER);
			DataObject coFilterNode = coFilter.createDataObject(Constants.OBJECT);
			coFilterNode.set("name", businessEntity);
			coFilterNode.set("depth", 4);
			readBEParameters.set("defaultPageSize", 200);
			readBEParameters.set("readSystemFields", true);
			DataObject key = coFilterNode.createDataObject("key");
			key.set("rowid", rowid.trim());
			List<String> recordStates = new ArrayList<String>();
			recordStates.add(Constants.ACTIVE);
			recordStates.add(Constants.PENDING);
			coFilterNode.setList("recordState", recordStates);
			
			try {
				CallContext hardcodedcallContext = new CallContext(externalCallRequest.getHeader().getOrsId(),
						"admin",Base64.getEncoder().encodeToString(("admin:hollyfront@4a4a").getBytes(StandardCharsets.UTF_8)));
				
				DataObject readBEResponse = client.process(callContext, readBERequest);

				return readBEResponse.getDataObject(Constants.OBJECT);

			} catch (CompositeServiceException e) {
				logger.error("Error while reading existing records for Business entity " + e.getMessage());
				e.printStackTrace();
			} catch(Exception e) {
				logger.error("Error while reading existing records for Business entity " + e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
	
 	
 	public DataObject readRootORS (CallContext callContext, CompositeServiceClient besClient, HelperContext helperContext,
      		String businessEntity, String rootRowid, Integer depth) {	
      	
      	try {
      		if (StringHelper.stringExists(rootRowid)) {    	  	    	
	  	    	DataFactory dataFactory  = helperContext.getDataFactory();
	  	    	Integer defaultPageSize = 50;
	  	    	Key rootKey = (Key) dataFactory.create(Key.class);
	  	    	rootKey.setRowid(rootRowid);
	  	    	CoFilterNode coNode = (CoFilterNode) dataFactory.create(CoFilterNode.class);    	    	
	  	    	
    			coNode.setName(businessEntity);   			
    			coNode.setRecordState(Arrays.asList("ACTIVE","PENDING"));
    			coNode.setKey(rootKey);
    			coNode.setDepth(depth);
    			
    			//Composite Object Filter 
	  	    	CoFilter coFilter = (CoFilter) dataFactory.create(CoFilter.class);
	  	    	coFilter.setObject(coNode);
	  	    	
    			ReadEntityParameters readEntityParameters = (ReadEntityParameters) dataFactory.create(ReadEntityParameters.class);
	  	    	readEntityParameters.setCoFilter(coFilter); 
	  	    	readEntityParameters.setReadSystemFields(true);
	  	    	readEntityParameters.setDefaultPageSize(BigInteger.valueOf(defaultPageSize));
	  	    	ReadEntity readEntity = (ReadEntity) dataFactory.create(ReadEntity.class);
	  	    	readEntity.setParameters(readEntityParameters);
	  	
	  	    	logger.debug("Execute readRootORS"); 

	  	    	DataObject readRootRequest = besClient.process(callContext, (DataObject) readEntity);
	  	        
	  	        if (readRootRequest != null ) {
	  	        	DataObject readRootResponse = (DataObject) readRootRequest.getDataObject("object").get(1);
	  	        	
	  	        	logger.debug("Found readRootORS");  
	  	        	
	  	        	return readRootResponse;	
	  	        } else {
	  	        	logger.debug("NULL returned for readRootORS");
	  	        } 
      		}
      		
      	} catch (Exception e) {
      		logger.error(" readRootORS had an error: " + e.getMessage());
      	}
  		return null;   
	}
	
 	public DataObject readPromotePreview (String interactionId, CallContext callContext, CompositeServiceClient besClient, 
 			HelperContext helperContext,String businessEntity,String rootRowid) {
    	try {
	    	logger.debug("INSIDE readPromotePreview");
	    	logger.debug("rootRowid: " + rootRowid); 
	    	logger.debug("businessEntity: " + businessEntity); 
	    	if (StringHelper.stringExists(rootRowid)) {
		    	DataFactory dataFactory  = helperContext.getDataFactory();
		    	DataObject readSupplierReturn = null;
		    	Key key = (Key) dataFactory.create(Key.class);
		    	Key beKey = (Key) dataFactory.create(Key.class);
		    	BusinessEntityKey businessEntityKey = (BusinessEntityKey) dataFactory.create(BusinessEntityKey.class);
		    	PromotePreviewParameters promotePreviewParameters = (PromotePreviewParameters) dataFactory.create(PromotePreviewParameters.class);
		    	CoFilter coFilter = (CoFilter) dataFactory.create(CoFilter.class);
		    	CoFilterNode coFilterNode = (CoFilterNode) dataFactory.create(CoFilterNode.class);
		    	PromotePreview readPromotePreview = (PromotePreview) dataFactory.create(PromotePreview.class);
		    	           
		        key.setRowid(rootRowid);
		        beKey.setRowid(rootRowid);
		        
		    	businessEntityKey.setName(businessEntity);
		    	businessEntityKey.setKey(beKey);
		    	coFilterNode.setName(businessEntity);
		    	coFilterNode.setSuppress(false);
		    	
		    	coFilterNode.setRecordState(Arrays.asList("ACTIVE","PENDING"));
		    	coFilterNode.setKey(key);
		    	coFilterNode.setDepth(4);
		    	coFilter.setObject(coFilterNode);
				HttpCompositeServiceClient h;
		    	
		    	
		    	promotePreviewParameters.setBusinessEntityKey(businessEntityKey);
		    	promotePreviewParameters.setInteractionId(interactionId);
		    	promotePreviewParameters.setCoFilter(coFilter);
		    	
		    	readPromotePreview.setParameters(promotePreviewParameters);
		
		       // dump(helperContext, "readPromotePreview", (DataObject) readPromotePreview);
		    	
		        logger.debug("Execute Read");
		        Instant startreadSupplier = Instant.now(); 
		        
		        readSupplierReturn = besClient.process(callContext, (DataObject) readPromotePreview);
		        
		        TimeHelper.getDuration(startreadSupplier, "Duration for readPromotePreview ");
		        if (readSupplierReturn != null ) {		        	
		        	logger.debug("Found readPromotePreview");
		        	
		        	return (DataObject) readSupplierReturn.getDataObject("object");	
		        } else {
		        	logger.debug("NULL** readPromotePreviewReturn");
		        }  
	    	} else {
  	    		logger.error("rootRowid is required for function readPromotePreview");
	    	}
    	} catch (Exception e) {
    		logger.error(" readPromotePreview had an error: " + e.getMessage(),e);
    	}
		return null;   
    }
 	
 	public DataObject searchMatch(String businessEntity, CallContext callContext, CompositeServiceClient compositeServiceClient, DataFactory dataFactory, Map<String, String> fuzzyFilters, String filter) {
 		try {
 			MatchCO matchCO = (MatchCO) dataFactory.create(MatchCO.class);
 			MatchCOParameters matchCOParameters = (MatchCOParameters) dataFactory.create(MatchCOParameters.class);
 			FuzzyFilterList fuzzyFilterList = (FuzzyFilterList) dataFactory.create(FuzzyFilterList.class);
 			List<FuzzyFilter> fuzzyFilterArrayList = new ArrayList<FuzzyFilter>();
 			MatchCOReturn matchCOReturn = (MatchCOReturn) dataFactory.create(MatchCOReturn.class);
	    	CoFilter coFilter = (CoFilter) dataFactory.create(CoFilter.class);
	    	CoFilterNode coFilterNode = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	    	
	    	coFilterNode.setName("ExHFC_HEP_CustomerOrg");
	    	coFilterNode.setSuppress(false);
	    	coFilterNode.setRecordState(Arrays.asList("ACTIVE","PENDING"));
	    	coFilterNode.setFilter(filter);
	    	coFilter.setObject(coFilterNode);
	    	
 			for (String key : fuzzyFilters.keySet()) {
 	 			FuzzyFilter fuzzyFilter = (FuzzyFilter) dataFactory.create(FuzzyFilter.class);
 	 			fuzzyFilter.setField(key);
 	 			fuzzyFilter.setValue(fuzzyFilters.get(key));
 	 			fuzzyFilterArrayList.add(fuzzyFilter);
 			}
 			fuzzyFilterList.setFilterItem(fuzzyFilterArrayList);
 			matchCOParameters.setFuzzyFilter(fuzzyFilterList);
 			matchCOParameters.setCoFilter(coFilter);
 			matchCO.setParameters(matchCOParameters);
 			
 			matchCOReturn = (MatchCOReturn) compositeServiceClient.process(callContext, (DataObject) matchCO);
 			
 			logger.info("GOT MATCH CO RETURN: " + matchCOReturn);
 			return (DataObject) matchCOReturn;
 			
 		} catch(Exception e) {
 			logger.error("READ MATCH had an error: " + e.getMessage(), e);
 		}
 		return null;
 	}

 	public DataObject readDBExistingRecord(CallContext callContext, CompositeServiceClient client, ExternalCallRequest externalCallRequest,
			HelperContext helperContext, String businessEntity, String rowid) {
		
		if (rowid != null && !rowid.trim().isEmpty()) {
			Type readEntityType = helperContext.getTypeHelper().getType("urn:cs-ors.informatica.mdm", "ReadEntity");
	        Type readParametersType = helperContext.getTypeHelper().getType("urn:cs-ors.informatica.mdm", Constants.PARAMETERS);

			
			DataObject readBERequest = helperContext.getDataFactory().create(readEntityType);
			DataObject readBEParameters = readBERequest.createDataObject(Constants.PARAMETERS);
			DataObject coFilter = readBEParameters.createDataObject(Constants.CO_FILTER);
			DataObject coFilterNode = coFilter.createDataObject(Constants.OBJECT);
//			coFilterNode.set("name", businessEntity);
			coFilterNode.set("name", businessEntity.substring(0, businessEntity.indexOf("View")));
//			coFilterNode.set("name", "ExHFC_HEP_CustomerOrg");
			coFilterNode.set("depth", 20);
			readBEParameters.set("defaultPageSize", 200);
			DataObject key = coFilterNode.createDataObject("key");
			key.set("rowid", rowid.trim());
			List<String> recordStates = new ArrayList<String>();
			recordStates.add(Constants.ACTIVE);
			recordStates.add(Constants.PENDING);
			coFilterNode.setList("recordState", recordStates);
			
			try {
				CallContext hardcodedcallContext = new CallContext(externalCallRequest.getHeader().getOrsId(),
						"admin",Base64.getEncoder().encodeToString(("admin:hollyfront@4a4a").getBytes(StandardCharsets.UTF_8)));
				
				DataObject readBEResponse = client.process(callContext, readBERequest);

				return readBEResponse.getDataObject(Constants.OBJECT);

			} catch (CompositeServiceException e) {
				logger.error("Error while reading existing records for Business entity " + e.getMessage());
				e.printStackTrace();
			} catch(Exception e) {
				logger.error("Error while reading existing records for Business entity " + e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
}
