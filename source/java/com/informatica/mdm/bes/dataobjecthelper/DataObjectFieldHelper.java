package com.informatica.mdm.bes.dataobjecthelper;


import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;

import com.informatica.mdm.bes.helper.StringHelper;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.ChangeSummary.Setting;

public class DataObjectFieldHelper {
	private static Logger logger = Logger.getLogger(DataObjectFieldHelper.class.getName());
	
	public DataObjectFieldHelper() { }
	
    public String getDataObjectRowid(DataObject dataObject) {
 		if (dataObject != null) {
 			String rootRowid = dataObject.getString("rowidObject");
 			rootRowid = (rootRowid == null) ? dataObject.getString("key/rowid") : rootRowid.trim();
 			return (rootRowid != null) ? rootRowid.trim() : null ;
 		} 	 		
 		return null;
 	}
 	
    public String getDataObjectPkey(DataObject dataObject) {
 		if (dataObject != null) {
 			String pkey = dataObject.getString("key/sourceKey");
 			return (pkey != null) ? pkey.trim() : null;
 		}
 		return null;
 	}
 	
    public Date getDataObjectLUD(DataObject dataObject) {
 		if (dataObject != null) {
 			Date lud = dataObject.getDate("lastUpdateDate");
 			return (lud != null) ? lud : null;
 		}
 		return null;
 	}
 	
    public String getDataObjectInteractionId(DataObject dataObject) {
 		if (dataObject != null) {
 			String interactionId = dataObject.getString("interactionId");
 			return (interactionId != null) ? interactionId.trim() : null;
 		}
 		return null;
 	}
    
    public String getDataObjectRelRowid(DataObject dataObject, String direction, String relTo, String relFrom) {
    	if (dataObject == null || direction == null) {
    		logger.error("dataObject or direction was sent as null");
    		return null;
    	}
    	DataObject dataObjectRel;
    	
 		try {
 			if (direction.equals(relTo)) {
 				dataObjectRel = dataObject.getDataObject(relTo);
 			} else {
 				dataObjectRel = dataObject.getDataObject(relFrom);
 			}
 			 			
			if (dataObjectRel != null) {
				String rootRowid = dataObject.getString("rowidObject");
	 			rootRowid = (rootRowid == null) ? dataObject.getString("key/rowid") : rootRowid.trim();
	 			return (rootRowid != null) ? rootRowid.trim() : null ;
			}
				
 		} catch (Exception e) {
 			logger.error("getDataObjectRelRowid had an error: " + e.getMessage(), e);
  		}	
 		return null;
 	}
    
    public String getChangeType (DataObject inputSdo, DataObject a, String name, 
    		String insertString, String deleteString, String updateString, String noActionString) {
    	if (inputSdo == null || name == null) {
    		logger.error("inputSdo, a or name is null);");
    		return null;
    	}
		String changeType = null;

 		try {
	 		Boolean isCreated, isModified, isDeleted = null;
			Boolean isRootCreated = inputSdo.getChangeSummary().isCreated(inputSdo.getDataObject(1));
	        
			isCreated = inputSdo.getChangeSummary().isCreated(a);
			isModified = inputSdo.getChangeSummary().isModified(a);
			isDeleted = inputSdo.getChangeSummary().isDeleted(a);
			logger.debug("**** isRootCreated: " + isRootCreated);
			logger.debug("CREATED "+ name + "? " + isCreated);
	        logger.debug("MODIFIED "+ name + "?" + isModified);
	        logger.debug("DELETED " + name + "?" + isDeleted);
			
	        if (isCreated || isRootCreated) 
	        	changeType = insertString;
	        else if (isDeleted)
	        	changeType = deleteString;
	        else if (isModified) 
	        	changeType = updateString;
	        else 
	        	changeType = noActionString;
	        
	        logger.debug(name + "Action Type: " + changeType);
 		} catch (Exception e) {
			logger.error("getSupplierType had an error: " + e.getMessage(), e);
 		}
		return null;
 	}
    
 	public String getSystemName (Map<String, Object> inParams) {		
 		if (inParams != null && !inParams.containsKey("systemName"))
 			return inParams.get("systemName").toString();
 		else
 			logger.error("System name was not in the parameters!"); return null;
    }
    
    public Boolean isMany(DataObject dataObject ) {
    	if (dataObject == null) {
    		logger.error("dataObject was sent as null" );
    		return null;
    	}
    	
    	try {
	        Type objType = dataObject.getType();
	        List propList = objType.getProperties();
   
	        for (int p = 0, size = propList.size(); p < size; p++) {
	            if (dataObject.isSet(p)) {
	                Property property = (Property) propList.get(p);
	                
	                // For many-valued properties, process a list of values
	                return property.isMany() ? true : false;
	            }
	        }
    		
    	} catch (Exception e) {
			logger.error("isMany had an error: " + e.getMessage(), e);
 		}
		return false;
    }
    
 	public Boolean isStringChanged(DataObject dataObject, String fieldType) {
 		logger.debug("*************** ENTERING isStringChanged " + fieldType + " ******************");
 		if (dataObject == null || fieldType == null) {
 			logger.error("dataObject or fieldType is null");
 			return null;
 		}

 		try {
 			ChangeSummary changeSummary = dataObject.getChangeSummary();
			String newValue = dataObject.getString(fieldType);
			if (StringHelper.stringExists(newValue)) {
				logger.debug(fieldType + ": " + newValue);
				List<Setting> oldValueList = changeSummary.getOldValues(dataObject);
				if (oldValueList.size() == 0) {
					logger.debug("	oldValueList Size = 0");
				} else {
					for (Setting obj : oldValueList) {
						String fieldName = obj.getProperty().getName();
						Object fieldValue = obj.getValue();
						logger.debug(" Field Name: " + fieldName);
						logger.debug(" Old Value: " + fieldValue);
						if (StringHelper.stringExists(fieldName) && fieldName.equals(fieldType)) {								
							String oldValue = (fieldValue != null)? fieldValue.toString().trim():null;	
							if (!StringHelper.compare(newValue, oldValue)) {
								logger.debug(fieldType + " has been changed ");
								return true;
							}
						}						
					}
				}
			}
			return false;
 		} catch (Exception e) {
 			logger.error("Exception caught in the catch block..isStringChanged..." + e.getMessage(), e); 			 
 		}
 		return false;
 	}
 	
 	public synchronized DataObject createDataObjectManyChild(DataObject pagerDataObject, DataFactory dataFactory){
 		try {
	 		SDOChangeSummary cs = (SDOChangeSummary) pagerDataObject.getChangeSummary();
	 		if (cs != null) {
		 		cs.resumeLogging();
		 		Property item = pagerDataObject.getInstanceProperty("item");
		 		Type type = item.getType();
		 		DataObject dataObject = dataFactory.create(type);
		 		cs.pauseLogging();
		 		return dataObject;
	 		}

 		} catch (Exception e) {
 			logger.error(" createDataObjectManyChild had an error: " + e.getMessage(),e);
 		}
 		return null;
	}
 	
	public DataObject getDataObject(DataObject inputSDO, DataObject orsSDO, String path) {
		DataObject dataObject = null;
		if (inputSDO != null)
			dataObject = inputSDO.getDataObject(path);
		if (dataObject == null && orsSDO != null)
			dataObject = orsSDO.getDataObject(path);
		return dataObject;
	}
	
	public DataObject getDataObject(DataObject inputSDO, DataObject promoteSDO, DataObject orsSDO, String path) {
		DataObject dataObject = null;
		if (inputSDO != null)
			dataObject = inputSDO.getDataObject(path);
		if (dataObject == null && promoteSDO != null)
			dataObject = promoteSDO.getDataObject(path);
		if (dataObject == null && orsSDO != null)
			dataObject = orsSDO.getDataObject(path);
		return dataObject;
	}
	
	public Date getDate(DataObject inputSDO, DataObject orsSDO, String path) {
		Date date = null;
		if (inputSDO != null)
			date = inputSDO.getDate(path);
		if (date == null && orsSDO != null)
			date = orsSDO.getDate(path);
		return date;
	}
	
	public String getString(DataObject parentInputSDO, DataObject parentOrsSDO, String path) {
		String stringValue = parentInputSDO.getString(path);
		if (stringValue == null && parentOrsSDO != null)
			stringValue = parentOrsSDO.getString(path);
		return stringValue;
	}
	
	public String getString(DataObject parentInputSDO, DataObject promoteSDO, DataObject parentOrsSDO, String path) {
		String stringValue = null;
		if (parentInputSDO != null)
			stringValue = parentInputSDO.getString(path);
		if (stringValue == null && promoteSDO != null)
			stringValue = promoteSDO.getString(path);
		if (stringValue == null && parentOrsSDO != null)
			stringValue = parentOrsSDO.getString(path);
		return stringValue;
	}
	
	public Boolean getBoolean(DataObject inputSDO, DataObject orsSDO, String path) {
		Boolean booleanValue = null;
		if (inputSDO != null)
			booleanValue = inputSDO.getBoolean(path);
		if (booleanValue == null && orsSDO != null)
			booleanValue = orsSDO.getBoolean(path);
		return booleanValue;
	}
	
	public Boolean getBoolean(DataObject parentInputSDO, DataObject parentOrsSDO, DataObject parentPromoteSDO, String path) {
		Boolean booleanValue = parentInputSDO.getBoolean(path);
		// if the inputSDO does not contain the dataobject - try the promotePreview
		if (booleanValue == null  && parentPromoteSDO != null) 
			booleanValue = parentPromoteSDO.getBoolean(path);
		// If the promote preview does not contain the data object, last resort try the orsSDO
		if (booleanValue == null && parentOrsSDO != null)
			booleanValue = parentOrsSDO.getBoolean(path);
		
		return booleanValue;
	}
	
	public Object get(DataObject inputSDO, DataObject orsSDO, String path) {
		Object value = inputSDO.get(path);
		if (value == null && orsSDO != null)
			value = orsSDO.get(path);
		return value;
	}
	
	
	public Object get(DataObject inputSDO, DataObject promoteSDO, DataObject orsSDO, String path) {
		Object value = inputSDO.get(path);
		if (value == null && promoteSDO != null)
			value = promoteSDO.get(path);
		if (value == null && orsSDO != null)
			value = orsSDO.get(path);
		return value;
	}
 

}
