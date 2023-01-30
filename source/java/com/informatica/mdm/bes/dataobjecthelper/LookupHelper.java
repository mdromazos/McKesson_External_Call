package com.informatica.mdm.bes.dataobjecthelper;

import java.util.ArrayList;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class LookupHelper {
	protected static Logger logger = Logger.getLogger(LookupHelper.class.getClass());

	private DataObjectGeneralHelper dataObjectGeneralHelper;
	
	public LookupHelper(DataObjectGeneralHelper dataObjectGeneralHelper) {
		this.dataObjectGeneralHelper = dataObjectGeneralHelper;
	}
	
	public List<String> getDeletedLookups(DataObject rootSdo, HelperContext helperContext) {
 		SDOChangeSummary cs = (SDOChangeSummary) rootSdo.getChangeSummary();
        List<DataObject>  csdl      = cs.getDeleted();
        List<DataObject>  csCreated = cs.getCreated();
        List<String> createdLookups = new ArrayList();
        List<String> deletedLookups = new ArrayList();
        
        if (csCreated != null && csCreated.size()>0) {
	        for(DataObject csc:csCreated) {
	        	String name = (String) csc.getType().getName();
	        	if (name != null) {
	        		createdLookups.add(name.trim());
		       	 }
	        }
        }        
        
        if (csdl != null && csdl.size()>0) {
	        for(DataObject csd:csdl) {
	        	 // dump(helperContext, "csd", csd);
	        	 String name = (String) csd.getType().getName();
	        	 String value = csd.getType().getURI();
		       	 if (name != null && !createdLookups.contains(name)) {
		       		deletedLookups.add(name.trim());
		        	 logger.debug("Deleted Lookup name: " + name + " URI: " + value);

		       	 }
	        }
        }
		return deletedLookups;
 	}
	
	public String getLookupStringFilterDeleted(DataObject sdo, DataObject ors, String path,List<String>  deletedLookups) {
 		try {
 			String lookupName = path.substring(0, path.indexOf("/"));
 			logger.debug("lookupName "+ lookupName);
 			
 			DataObject lookupDoORS = (ors != null) ? ors.getDataObject(path.substring(0, path.indexOf("/"))) : null;
			String typeName = (lookupDoORS != null)? lookupDoORS.getType().getName(): null;
			String lookupString = dataObjectGeneralHelper.chooseOrsSdoString(sdo, ors, path);
			logger.debug("typeName: " + typeName);

			logger.debug(" typeName != null && deletedLookups.contains(typeName)   " + (typeName != null && deletedLookups.contains(typeName)));
			Boolean isDeleted = (typeName != null && deletedLookups.contains(typeName)) 
					? true  
					: false;
			
			if (isDeleted) {
				logger.debug("Deleted Lookup Found, Returning NULL: ");
				return null;
			} 
			logger.debug("Deleted Lookup Not found, Returning: " + lookupString);
			return lookupString;		

 		} catch (Exception e) {
			logger.error("getSupplierType had an error: " + e.getMessage(), e);
 		}
		return null;
 	}	
}
