package com.informatica.mdm.bes.dataobjecthelper;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.helper.ListHelper;
import com.informatica.mdm.bes.helper.StringHelper;
import com.informatica.mdm.sdo.cs.base.CoFilter;
import com.informatica.mdm.sdo.cs.base.CoFilterNode;

import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

public class CoFilterNodeHelper {
	protected static Logger logger = Logger.getLogger(StringHelper.class.getClass());
	
    public ArrayList<CoFilterNode> convertListStringToListCoNode(HelperContext helperContext, ArrayList<String> coNodeListString) {
    	try { 
	    	ArrayList<CoFilterNode> coNodeList = new ArrayList<>();	    		    	
	    	if (ListHelper.listExists(coNodeListString)) {
	    		for (String coNodeString: coNodeListString) {
	    	    	DataFactory dataFactory  = helperContext.getDataFactory();
	    	    	CoFilterNode coNode = (CoFilterNode) dataFactory.create(CoFilterNode.class);
	    	    	logger.debug("coNodeString: " + coNodeString); 
	
	    			coNode.setName(coNodeString);
	    			coNode.setRecordState(Arrays.asList("ACTIVE","PENDING"));
	    			coNodeList.add(coNode);	    			
	    		}
	    	}
	    	return coNodeList;
    	} catch (Exception e) {
    		logger.error(" convertListStringToListCoNode had an error: " + e.getMessage(),e);
    	}
		return null;
    }
    
    public CoFilter buildDynamicCoFilter () {
 		try {
 			
 		} catch (Exception e) {
      		logger.error(" buildDynamicCoFilter had an error: " + e.getMessage(),e);
      	}
		return null;
 		
 	}
}
