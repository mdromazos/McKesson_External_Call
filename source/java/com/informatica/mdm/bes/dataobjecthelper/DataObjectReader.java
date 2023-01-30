package com.informatica.mdm.bes.dataobjecthelper;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.helper.ListHelper;
import com.informatica.mdm.bes.helper.StringHelper;
import com.informatica.mdm.bes.helper.TimeHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.BusinessEntityKey;
import com.informatica.mdm.sdo.cs.base.CoFilter;
import com.informatica.mdm.sdo.cs.base.CoFilterNode;
import com.informatica.mdm.sdo.cs.base.Key;

import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;
import mdm.informatica.cs_ors.PromotePreview;
import mdm.informatica.cs_ors.PromotePreviewParameters;
import mdm.informatica.cs_ors.ReadEntity;
import mdm.informatica.cs_ors.ReadEntityParameters;

public class DataObjectReader {
	private static Logger logger = Logger.getLogger(StringHelper.class.getName());
	
	private DataObjectDumper dataObjectDumper;
	private CoFilterNodeHelper coFilterNodeHelper;
	
	public DataObjectReader(DataObjectDumper dataObjectDumper,
			CoFilterNodeHelper coFilterNodeHelper) {
		this.dataObjectDumper = dataObjectDumper;
		this.coFilterNodeHelper = coFilterNodeHelper;
		
	}

	
	public DataObject readDirectChild (CallContext callContext, CompositeServiceClient besClient, HelperContext helperContext,
 		String businessEntity, String rootRowid,String rootPkey, String systemName, String path,Integer depth) {	
 
      	try {
  	    	logger.debug("INSIDE readDirectChild: " + path);
  	    	logger.debug("rootRowid: " + rootRowid); 	    	  	        
  	    	logger.debug("businessEntity: " + businessEntity); 
  	    	logger.debug("depth: " + depth); 	
  	    	
  	    	if (StringHelper.stringExists(rootRowid) || StringHelper.stringExists(rootPkey)) { 
	  	    	String lastChildName = null;
	  	    	
	  	    	helperContext = besClient.getHelperContext(callContext);
	
	  	    	depth = (depth != null) ? depth : 1;
	  	    	    	  	        
	  	    	ArrayList<String> coNodeListString = new ArrayList<>(Arrays.asList(path.split("\\."))); 
	  	    	logger.debug("coNodeListString Size: " + ListHelper.getListSize(coNodeListString));
	  	    	
	  	    	DataFactory dataFactory  = helperContext.getDataFactory();
	  	    	
	  	    	Key rootKey = (Key) dataFactory.create(Key.class);
	  	    	
	  	    	if(StringHelper.stringExists(rootRowid)) {
	  	    		rootKey.setRowid(rootRowid);
	  	    	} else if (StringHelper.stringExists(rootPkey)) {
	  	    		rootKey.setSystemName(systemName);
	  	    		rootKey.setSourceKey(rootPkey);
	  	    	}
	  	    		   	    		    	
	  	    	CoFilterNode coFilterNodeParent = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	  	    	CoFilterNode coFilterNodeChild = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	  	    	CoFilterNode coFilterNodeHeader = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	  
	  	    	ArrayList<CoFilterNode> coNodeList = coFilterNodeHelper.convertListStringToListCoNode(helperContext, coNodeListString);
	  	        logger.debug("coNodeList Size: " + ListHelper.getListSize(coNodeList));
	  	        Integer nodeListCount = ListHelper.getListSize(coNodeList);	  	        
	  	        
	  	        for (int x = 0; x<nodeListCount;x++) {
	      	        logger.debug("x: " + x);
	      	        
	      	        // Header Filter Node Level (MANDATORY)
	  	        	if (x<=0) {	    	        	    	     
	  	    	        coFilterNodeHeader = coNodeList.get(x);  	    	        
	  	        		coFilterNodeHeader.setSuppress(true);  	        		
	  	        		coFilterNodeHeader.setKey(rootKey);  	        		
	  	    	        
	  	        	} else {
	  	        		// Middle Filter Node configuration (Optional. Only on levels > 2)
	  	        		if ((x+1)<nodeListCount.intValue()){
		  	        		coFilterNodeParent = coNodeList.get(x-1);	  	        		
		  	        		coFilterNodeChild = coNodeList.get(x);	  	        		
		  	        		//coFilterNodeChild.setFilter(pathStringFilter);	  	        		
		  	        		coFilterNodeParent.setObject(Arrays.asList(coFilterNodeChild));	  	        		
		  	        		coFilterNodeChild.setSuppress(true);
		  	        		
		  	        	// Trailer filter configuration (MANDATORY)
	  	        		} else if (nodeListCount.intValue() == (x+1)){
		  	        		
	  	        			coFilterNodeParent = coNodeList.get(x-1);
		  	        		coFilterNodeChild = coNodeList.get(x);
		  	        		coFilterNodeChild.setSuppress(false);
		  	        		coFilterNodeChild.setDepth(depth);
		  	        		lastChildName = coFilterNodeChild.getName();
		  	        		coFilterNodeParent.setObject(Arrays.asList(coFilterNodeChild));	  	        		
		  	        	}
	  	        	}  	        	
	  	        } 	        
	  	        
	  	        //Composite Object Filter 
	  	    	CoFilter coFilter = (CoFilter) dataFactory.create(CoFilter.class);
	  	    	coFilter.setObject(coFilterNodeHeader);
	  
	  	    	ReadEntityParameters readEntityParameters = (ReadEntityParameters) dataFactory.create(ReadEntityParameters.class);
	  	    	readEntityParameters.setCoFilter(coFilter);  	    	
	  	    	readEntityParameters.setReadSystemFields(true);
	  	    	ReadEntity readEntity = (ReadEntity) dataFactory.create(ReadEntity.class);
	  	    	readEntity.setParameters(readEntityParameters);
	  	
	  	    	logger.debug("Execute readDirectChild for " + path); 
	  	    	dataObjectDumper.dump(helperContext, "readSupplierChildList for " + path, (DataObject) readEntity);
	  	    	
	  	    	Instant start = Instant.now();
	  	        DataObject readSupplierReturn = besClient.process(callContext, (DataObject) readEntity);
	  	        TimeHelper.getDuration(start, "readDirectChild Duration: " + lastChildName);
	  	        if (readSupplierReturn != null ) {
	  	        	DataObject readSupplierChild = (DataObject) readSupplierReturn.getDataObject("object").get(1);
	  	        	dataObjectDumper.dump(helperContext, "readSupplierChildReturn for " + path,  readSupplierChild);	          	         	        
	
	  	        	return readSupplierChild; //.getDataObject(lastChildName);
	  	        } else {
	  	        	logger.debug("NULL returned for readDirectChild " + path);
	  	        }   
  	    	}  else {
    			logger.error("rootRowid or rootPkey is required for function readDirectChild " + path);
    		}
      	} catch (Exception e) {
      		logger.error(" readDirectChild had an error: " + e.getMessage());
      	}
  		return null;   
 	}
 	
    public DataObject readPendingChild (CallContext callContext, CompositeServiceClient besClient, String businessEntity, String rowid ,
			String path,String interactionId) {	
      	
      	try {
  	    	logger.debug("INSIDE readPendingChild: " + path);
  	    	logger.debug("rootRowid: " + rowid); 	    	  	        
  	    	logger.debug("businessEntity: " + businessEntity); 
  	    	logger.debug("interactionId: " + interactionId); 
  	    	if (StringHelper.stringExists(interactionId)) {
	  	    	HelperContext helperContext = besClient.getHelperContext(callContext);
	  	    	DataFactory dataFactory  = helperContext.getDataFactory();
		    	DataObject readSupplierReturn = null;
		    	
		    	Key beKey = (Key) dataFactory.create(Key.class);
		    	BusinessEntityKey businessEntityKey = (BusinessEntityKey) dataFactory.create(BusinessEntityKey.class);
		    	PromotePreviewParameters promotePreviewParameters = (PromotePreviewParameters) dataFactory.create(PromotePreviewParameters.class);	    			       
	  	    	
	  	    	String lastChildName = null;	  	       	  	        
	  	    	ArrayList<String> coNodeListString = new ArrayList<>(Arrays.asList(path.split("\\."))); 	  	    
	  	    	
	  	    	Key rootKey = (Key) dataFactory.create(Key.class);
	  	    	rootKey.setRowid(rowid);	   	    		    		  	    	
	
	  	    	CoFilterNode coFilterNodeParent = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	  	    	CoFilterNode coFilterNodeChild = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	  	    	CoFilterNode coFilterNodeHeader = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	  
	  	    	ArrayList<CoFilterNode> coNodeList = coFilterNodeHelper.convertListStringToListCoNode(helperContext, coNodeListString);
	  	        logger.debug(path + "coNodeList Size: " + ListHelper.getListSize(coNodeList));
	  	        Integer nodeListCount = ListHelper.getListSize(coNodeList);
	  	         	        
	  	        for (int x = 0; x<nodeListCount;x++) {
	      	        
	      	        
	      	        // Header Filter Node Level (MANDATORY)
	  	        	if (x<=0) {	    	        	    	     
	  	    	        coFilterNodeHeader = coNodeList.get(x);  	    	        
	  	        		coFilterNodeHeader.setSuppress(true);  	        		
	  	        		coFilterNodeHeader.setKey(rootKey);  		  	        		
	  	    	        
	  	        	} else {
	  	        		// Middle Filter Node configuration (Optional. Only on levels > 2)
	  	        		if ((x+1)<nodeListCount.intValue()){
		  	        		coFilterNodeParent = coNodeList.get(x-1);	  	        		
		  	        		coFilterNodeChild = coNodeList.get(x);	  	        		
		  	        		coFilterNodeParent.setObject(Arrays.asList(coFilterNodeChild));	  	        		
		  	        		coFilterNodeChild.setSuppress(true);
		  	        		
		  	        	// Trailer filter configuration (MANDATORY)
	  	        		} else if (nodeListCount.intValue() == (x+1)){
		  	        		
	  	        			coFilterNodeParent = coNodeList.get(x-1);
		  	        		coFilterNodeChild = coNodeList.get(x);
		  	        		coFilterNodeChild.setSuppress(false);
		  	        		//coFilterNodeChild.setDepth(depth);
		  	        		lastChildName = coFilterNodeChild.getName();
		  	        		coFilterNodeParent.setObject(Arrays.asList(coFilterNodeChild));	  	        		
		  	        	}
	  	        	}  	        	
	  	        } 	        
	  	        
	  	        //Composite Object Filter 
	  	    	CoFilter coFilter = (CoFilter) dataFactory.create(CoFilter.class);
	  	    	coFilter.setObject(coFilterNodeHeader);
  	
	  	    	beKey.setRowid(rowid);
		    	
		    	businessEntityKey.setName(businessEntity);
		    	businessEntityKey.setKey(beKey);		    	
		    	
		    	promotePreviewParameters.setBusinessEntityKey(businessEntityKey);
		    	promotePreviewParameters.setInteractionId(interactionId);
		    	promotePreviewParameters.setCoFilter(coFilter);  	
	  	    	
		    	PromotePreview readPromotePreview = (PromotePreview) dataFactory.create(PromotePreview.class);
	  	    	readPromotePreview.setParameters(promotePreviewParameters);
	  	
	  	    	logger.debug("Execute readPendingChild for " + path); 
	  	    	// dump(helperContext, "readPendingChild " + path, (DataObject) readPromotePreview);	
	  	    	
	  	    	Instant startReadPromotePreviewTime = Instant.now();   	
	  	        readSupplierReturn = besClient.process(callContext, (DataObject) readPromotePreview);	  	        
	  	        TimeHelper.getDuration(startReadPromotePreviewTime, "startReadPendingChildTime for " + path);
	  	        	  	        
	  	        if (readSupplierReturn != null ) {
	  	        	DataObject readPromotePreviewReturn = (DataObject) readSupplierReturn.getDataObject("object").getDataObject(businessEntity);
		  	        dataObjectDumper.dump(helperContext, "readPendingChildReturn for " + path, readPromotePreviewReturn);

	  	        	return readPromotePreviewReturn;
	  	        } else {
	  	        	logger.debug("NULL returned for readPromotePreview " + path);
	  	        }   
  	    	}
      	} catch (Exception e) {
      		logger.error(" readPromotePreview had an error: " + e.getMessage());
      	}
  		return null;   
 	}
 	
 	public HashMap<String, String> getDeletedKeys(DataObject rootSdo) {
 		SDOChangeSummary cs = (SDOChangeSummary) rootSdo.getChangeSummary();
		cs.pauseLogging();

        List<DataObject>  csdl = cs.getDeleted();
        List<String> deletedRowids = new ArrayList();
        HashMap<String, String> deletedRowidsHM = new HashMap<String, String>();

        if (ListHelper.listExists(csdl)) {
        	for (Iterator iter = csdl.iterator(); iter.hasNext(); ) {
        		 
                DataObject csd = (DataObject) iter.next();
                String name = (String) csd.getType().getName();
                String value = csd.getString("rowidObject");
                logger.debug("Got Deleted List name=" + value + ", type=" + name);      
                if (StringHelper.stringExists(value)) {
                	deletedRowidsHM.put(name, value);               
                }
            }
        }
              
		return deletedRowidsHM;
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
	  	        	DataObject readRootResponse = (DataObject) readRootRequest.getDataObject("object");
	  	        	
	  	        	logger.debug("Found readRootORS");  
	  	        	 //dump(helperContext, "*** readRootResponse return :", readRootResponse);	     
	  	        	
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
	
 	public DataObject readFilteredEntity (CallContext callContext, CompositeServiceClient besClient, HelperContext helperContext,
      		String businessEntity, String rootRowid,ArrayList<String> coNodeListString, String filter) {	
      	
      	try {
  	    	logger.debug("INSIDE readFilteredEntity");
  	    	logger.debug("rootRowid: " + rootRowid); 	    	
  	        logger.debug("coNodeListString Size: " + ListHelper.getListSize(coNodeListString));
  	    	logger.debug("businessEntity: " + businessEntity); 
  	    	
  	    	if (StringHelper.stringExists(rootRowid)) {    	  	    	
	  	    	DataFactory dataFactory  = helperContext.getDataFactory();
	  	    	
	  	    	Key rootKey = (Key) dataFactory.create(Key.class);
	  	    	rootKey.setRowid(rootRowid);	   	    		    	
	  	    	
	  	    	String pathStringFilter = "";
	  	    	CoFilterNode coFilterNodeParent = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	  	    	CoFilterNode coFilterNodeChild = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	  	    	CoFilterNode coFilterNodeHeader = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	  
	  	    	ArrayList<CoFilterNode> coNodeList = coFilterNodeHelper.convertListStringToListCoNode(helperContext, coNodeListString);
	  	        logger.debug("coNodeList Size: " + ListHelper.getListSize(coNodeList));
	  	        Integer nodeListCount = ListHelper.getListSize(coNodeList);
	  	        
	  	        ArrayList<String> tmpcoNodeListString = (ListHelper.listExists(coNodeListString))? coNodeListString:null;	    	
	  	        if (ListHelper.listExists(tmpcoNodeListString)) {
	  	        	tmpcoNodeListString.remove(0);
	  	        }
	  	        
	  	        for (int x = 0; x<nodeListCount;x++) {
	      	        logger.debug("x: " + x);
	      	        
	      	        // Header Filter Node Level (MANDATORY)
	  	        	if (x<=0) {	    	        	    	     
	  	    	        coFilterNodeHeader = coNodeList.get(x);  	    	        
	  	        		coFilterNodeHeader.setSuppress(true);  	        		
	  	        		coFilterNodeHeader.setKey(rootKey);  	        		
	  	    	        
	  	        	} else {
	  	        		// Child Filter Node Level
	  	        		int childCounter=1;
	  	        		
	  	        		//Configure filter property dynamically based on depth location 
	  	        		if (nodeListCount>0 && StringHelper.stringExists(filter) && ListHelper.listExists(tmpcoNodeListString)) {
	  	        			
	  	        	    	tmpcoNodeListString.remove(0);
	  	        	    	pathStringFilter = tmpcoNodeListString.stream().collect(Collectors.joining("."));
	  	        	    	pathStringFilter = pathStringFilter + "."+ filter;
		  	      	        logger.debug("pathStringFilter: " + pathStringFilter);
	  	        		}
	  	        		
	  	        		// Middle Filter Node configuration (Optional. Only on levels > 2)
	  	        		if ((x+1)<nodeListCount.intValue()){
		  	        		coFilterNodeParent = coNodeList.get(x-1);	  	        		
		  	        		coFilterNodeChild = coNodeList.get(x);	  	        		
		  	        		coFilterNodeChild.setFilter(pathStringFilter);	  	        		
		  	        		coFilterNodeParent.setObject(Arrays.asList(coFilterNodeChild));	  	        		
		  	        		coFilterNodeChild.setSuppress(true);	    	        	  	        		
		  	        		
		  	        	// Trailer filter configuration (MANDATORY)
	  	        		} else if (nodeListCount.intValue() == (x+1)){
		  	        		
	  	        			coFilterNodeParent = coNodeList.get(x-1);
		  	        		coFilterNodeChild = coNodeList.get(x);
		  	        		coFilterNodeChild.setSuppress(false);
		  	        		
		          			if (StringHelper.stringExists(filter)) {	          				 
		          				 coFilterNodeChild.setFilter(filter);
		          				coFilterNodeChild.setFields("interactionId");
		          			}
		  	        		coFilterNodeParent.setObject(Arrays.asList(coFilterNodeChild));	  	        		
		  	        	}
	  	        		childCounter++;
	  	        	}  	        	
	  	        } 	        
	  	        
	  	        //Composite Object Filter 
	  	    	CoFilter coFilter = (CoFilter) dataFactory.create(CoFilter.class);
	  	    	coFilter.setObject(coFilterNodeHeader);
	  
	  	    	ReadEntityParameters readEntityParameters = (ReadEntityParameters) dataFactory.create(ReadEntityParameters.class);
	  	    	readEntityParameters.setCoFilter(coFilter);  	    	
	  	    	
	  	    	ReadEntity readEntity = (ReadEntity) dataFactory.create(ReadEntity.class);
	  	    	readEntity.setParameters(readEntityParameters);
	  	
	  	    	logger.debug("Execute readSupplierChildren"); 
	  	    	// dump(helperContext, "readSupplierChildren", (DataObject) readEntity);	          	         	        
	  	        DataObject readSupplierReturn = besClient.process(callContext, (DataObject) readEntity);
	  	        
	  	        if (readSupplierReturn != null ) {
	  	        	DataObject readSupplierChild = (DataObject) readSupplierReturn.getDataObject("object").get(1);
	  	        	
	  	        	logger.debug("Found readSupplierChildren");  	        	
	  	        	// dump(helperContext, "*** readSupplierChildren return :", readSupplierChild);	  
	  	        	
	  	        	return readSupplierChild;	
	  	        } else {
	  	        	logger.debug("NULL returned for readSupplierChildren");
	  	        } 
  	    	} else {
  	    		logger.error("rootRowid is required for function readSupplierChild");
  	    	}
      	} catch (Exception e) {
      		logger.error(" readSupplierChildren had an error: " + e.getMessage());
      	}
  		return null;   
    }

    
 	public DataObject readPromotePreview (String interactionId, CallContext callContext, CompositeServiceClient besClient, HelperContext helperContext,String businessEntity,String rootRowid) {	
    	
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
		    	CoFilterNode supplierFilter = (CoFilterNode) dataFactory.create(CoFilterNode.class);
		    	PromotePreview readPromotePreview = (PromotePreview) dataFactory.create(PromotePreview.class);
		    	           
		        key.setRowid(rootRowid);
		        beKey.setRowid(rootRowid);
		    	
		    	businessEntityKey.setName(businessEntity);
		    	businessEntityKey.setKey(beKey);
		    	supplierFilter.setName(businessEntity);
		    	supplierFilter.setSuppress(false);
		    	
		    	supplierFilter.setRecordState(Arrays.asList("ACTIVE","PENDING"));
		    	supplierFilter.setKey(key);
		    	coFilter.setObject(supplierFilter);
		    	
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
		        	
		        	return (DataObject) readSupplierReturn.getDataObject("object").get(1);	
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
}
