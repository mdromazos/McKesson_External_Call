package com.informatica.mdm.bes.dataobjecthelper;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.helper.ListHelper;
import com.informatica.mdm.bes.helper.StringHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.helper.DataFactory;

public class DataObjectCombiner {
	private static Logger logger = Logger.getLogger(DataObjectCombiner.class.getClass());
	
	private DataObjectReader dataObjectReader;
	private DataObjectFieldHelper dataObjectFieldHelper;
	private DataObjectSearcher dataObjectSearcher;
	
	public DataObjectCombiner(DataObjectReader dataObjectReader,
							  DataObjectFieldHelper dataObjectFieldHelper,
							  DataObjectSearcher dataObjectSearcher) {
		this.dataObjectReader = dataObjectReader;
		this.dataObjectFieldHelper = dataObjectFieldHelper;
		this.dataObjectSearcher = dataObjectSearcher;
	}
	
	public List<DataObject> combineDataObjectList(List<DataObject> listSDO, List<DataObject> listORS) {

		if ((listSDO == null && listORS == null)) {
			return null;
		}

    	try {
    		List<DataObject> listCombined = new ArrayList<DataObject>();
    		
    		if (ListHelper.listExists(listSDO) && !ListHelper.listExists(listORS)) {
    			return listSDO;
    		}
    		
    		if (!ListHelper.listExists(listSDO) && ListHelper.listExists(listORS)) {
    			return listORS;
    		}
    		
    		if (!ListHelper.listExists(listSDO) && !ListHelper.listExists(listORS)) {
    			return null;
    		}

			for (DataObject sdoObject: listSDO) {
				listCombined.add(sdoObject);
			}
			
    		for (DataObject orsObject: listORS) {
    			
    			String rowidOrs = dataObjectFieldHelper.getDataObjectRowid(orsObject);
    			Boolean matched = false;
    			
    			for (DataObject sdoObject : listSDO) {
    				
    				String rowidSdo = dataObjectFieldHelper.getDataObjectRowid(sdoObject);
    				if (StringHelper.stringExists(rowidSdo) && rowidSdo.equals(rowidOrs)) {
    					matched = true;
    				}
    			}

    			if (!matched) {
    				listCombined.add(orsObject);
    			}
    		}	
    		return listCombined;
    	} catch (Exception e) {
			logger.error("combineDataObjectList had an error: " + e.getMessage(), e);
 		}
    	return null;
	}
	
    public List<DataObject> combineDataObjectListAutomation(List<DataObject> listSDO, List<DataObject> listORS, 
    		DataObject parentSdo, String objectListName, DataObject parentORS) {
    	try {

    		List<DataObject> listCombined = new ArrayList();
			    
    		if (ListHelper.listExists(listSDO) && !ListHelper.listExists(listORS)) {
    			logger.debug("Returning listSDO ");
    			return listSDO;
    		}
    		
    		if (!ListHelper.listExists(listSDO) && ListHelper.listExists(listORS)) {
				Object itemPager[] = new DataObject[] {};

   		        for (DataObject dataObject: listORS) {
   		        	listCombined.add(dataObject);
   		        	itemPager = appendObjectArray(itemPager, dataObject);
   		        }
   		        parentSdo.createDataObject(objectListName);
   		        parentSdo.setList(objectListName + "/item", listCombined);
//   		        parentSdo.setDataObject(objectListName, parentORS.getDataObject(objectListName));
    			logger.debug(" Arrays.asList(itemPager) size: " + ListHelper.getListSize( Arrays.asList(itemPager)));
    			return listCombined;
    		}
    		
    		if (!ListHelper.listExists(listSDO) && !ListHelper.listExists(listORS)) {
    			return null;
    		}
    		

			if (ListHelper.listExists(listORS)) {
				Object itemPager[] = new DataObject[] {};
				DataObject childPager = (parentSdo.getDataObject(objectListName)==null) ? parentORS.getDataObject(objectListName) : parentSdo.getDataObject(objectListName);
				Property item = childPager.getInstanceProperty("item");
				
				if (ListHelper.listExists(listSDO)) {
	    			for (DataObject sdoObject : listSDO) {
	    				logger.debug("Adding SDO Child Item to List: ");
       		        	itemPager = appendObjectArray(itemPager, sdoObject);
       		        	listCombined.add(sdoObject);
	    			}
    			}
				
	    		for (DataObject orsObject: listORS) {
	    			
	    			String rowidOrs = dataObjectFieldHelper.getDataObjectRowid(orsObject);
	    			boolean matched = false;
	    			
	    			if (ListHelper.listExists(listSDO)) {
		    			for (DataObject sdoObject : listSDO) {	       		        	
		    				String rowidSdo = dataObjectFieldHelper.getDataObjectRowid(sdoObject);
		    				if (StringHelper.stringExists(rowidSdo) && rowidSdo.equals(rowidOrs)) {
		    					matched = true;
		    				}
		    			}
	    			}
	    			
	    			if (!matched) {
	    				logger.debug("Adding ORS Child Item to List: ");
       		        	itemPager = appendObjectArray(itemPager, orsObject);
       		        	listCombined.add(orsObject);
	    			}
	    		}
	    		childPager.setList(item, Arrays.asList(itemPager));
	    		logger.debug(" Arrays.asList(itemPager) size: " + ListHelper.getListSize( Arrays.asList(itemPager)));
	    		return listCombined;

			}
			logger.debug("Combined List Count: " + ListHelper.getListSize(listCombined));
			return listCombined; //childPager.getList(objectListName);
    	} catch (Exception e) {
			logger.error("combineDataObjectListAutomation had an error: " + e.getMessage(), e);
 		}
    	return null;
    }  

    public List<DataObject> combineDataObjectList(List<DataObject> listSDO, List<DataObject> listORS, Boolean isRootPending) {
    	logger.info("Entering combineDataObjectList ");
		logger.debug("Entering combineDataObjectList ");
		if ((listSDO == null && listORS == null) || isRootPending == null) {
			logger.error("listSDO, listORS, or isRootPending were sent as null");
			return null;
		}

    	try {
    		
			logger.debug("listSDO Count: " + ListHelper.getListSize(listSDO));
			logger.debug("listORS Count: " + ListHelper.getListSize(listORS));

    		List<DataObject> listCombined = new ArrayList<DataObject>();
    		
    		if (ListHelper.listExists(listSDO) && !ListHelper.listExists(listORS)) {
    			logger.debug("Returning listSDO ");

    			return listSDO;
    		}
    		if (!ListHelper.listExists(listSDO) && ListHelper.listExists(listORS) && isRootPending) {
    			logger.debug("Returning listORS for pending ");

    			return listORS;
    		}
    		if (!ListHelper.listExists(listSDO) && !ListHelper.listExists(listORS)) {
    			logger.debug("Returning null ");
    			return null;
    		}
			logger.debug("isRootPending : "+ isRootPending);

			for (DataObject sdoObject: listSDO) {
				listCombined.add(sdoObject);
			}
			
    		for (DataObject orsObject: listORS) {
    			
    			String rowidOrs = dataObjectFieldHelper.getDataObjectRowid(orsObject);
    			Boolean matched = false;
    			
    			for (DataObject sdoObject : listSDO) {
    				
    				String rowidSdo = dataObjectFieldHelper.getDataObjectRowid(sdoObject);
    				if (StringHelper.stringExists(rowidSdo) && rowidSdo.equals(rowidOrs)) {
    					matched = true;
    				}
    			}

    			logger.debug("matched : "+ matched);
	    		logger.debug("isRootPending : "+ isRootPending);

    			if (!matched && isRootPending) {
    				logger.debug("Adding ORS Child Item to List: ");
    				listCombined.add(orsObject);
    			}
    		}	
			logger.debug("Combined List Count: " + ListHelper.getListSize(listCombined));
    		return listCombined;
    	} catch (Exception e) {
			logger.error("combineDataObjectList had an error: " + e.getMessage(), e);
 		}
    	return null;
    }

    public Object[] appendObjectArray(Object[] obj, Object newObj) {
		ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(obj));
		temp.add(newObj);
		return (Object[]) temp.toArray();
    }
    
    public List<DataObject> combineDataObjectListAutomation(List<DataObject> listSDO, List<DataObject> listORS, Boolean isRootPending
    		, DataObject parentSdo, DataFactory dataFactory, String objectListName, DataObject parentORS) {
		logger.debug("Entering combineDataObjectList ");
    	try {
			logger.debug("listSDO Count: " + ListHelper.getListSize(listSDO));
			logger.debug("listORS Count: " + ListHelper.getListSize(listORS));

    		List<DataObject> listCombined = new ArrayList();
			    
    		if (ListHelper.listExists(listSDO) && !ListHelper.listExists(listORS)) {
    			logger.debug("Returning listSDO ");
    			return listSDO;
    		}
    		
    		if (!ListHelper.listExists(listSDO) && ListHelper.listExists(listORS) && isRootPending) {
    			logger.debug("Returning listORS for pending ");
				Object itemPager[] = new DataObject[] {};

   		        for (DataObject dataObject: listORS) {
   		        	listCombined.add(dataObject);
   		        	itemPager = appendObjectArray(itemPager, dataObject);
   		        }
   		        parentSdo.setDataObject(objectListName, parentORS.getDataObject(objectListName));
    			logger.debug(" Arrays.asList(itemPager) size: " + ListHelper.getListSize( Arrays.asList(itemPager)));
    			return listCombined;
    		}
    		
    		if (!ListHelper.listExists(listSDO) && !ListHelper.listExists(listORS)) {
    			logger.debug("Returning null ");
    			return null;
    		}
    		
			logger.debug("isRootPending : "+ isRootPending);

			if (ListHelper.listExists(listORS)) {
				Object itemPager[] = new DataObject[] {};
				DataObject childPager = (parentSdo.getDataObject(objectListName)==null) ? parentORS.getDataObject(objectListName) : parentSdo.getDataObject(objectListName);
				Property item = childPager.getInstanceProperty("item");
				
				if (ListHelper.listExists(listSDO)) {
	    			for (DataObject sdoObject : listSDO) {
	    				logger.debug("Adding SDO Child Item to List: ");
       		        	itemPager = appendObjectArray(itemPager, sdoObject);
       		        	listCombined.add(sdoObject);
	    			}
    			}
				
	    		for (DataObject orsObject: listORS) {
	    			
	    			String rowidOrs = dataObjectFieldHelper.getDataObjectRowid(orsObject);
	    			boolean matched = false;
	    			
	    			if (ListHelper.listExists(listSDO)) {
		    			for (DataObject sdoObject : listSDO) {	       		        	
		    				String rowidSdo = dataObjectFieldHelper.getDataObjectRowid(sdoObject);
		    				if (StringHelper.stringExists(rowidSdo) && rowidSdo.equals(rowidOrs)) {
		    					matched = true;
		    				}
		    			}
	    			}
	    			
	    			logger.debug("isRootPending : "+ isRootPending);
	    			logger.debug("matched : "+ matched);

	    			if (!matched && isRootPending) {
	    				logger.debug("Adding ORS Child Item to List: ");
       		        	itemPager = appendObjectArray(itemPager, orsObject);
       		        	listCombined.add(orsObject);
	    			}
	    		}
	    		childPager.setList(item, Arrays.asList(itemPager));
	    		logger.debug(" Arrays.asList(itemPager) size: " + ListHelper.getListSize( Arrays.asList(itemPager)));
	    		return listCombined;

			}
			logger.debug("Combined List Count: " + ListHelper.getListSize(listCombined));
			return listCombined; //childPager.getList(objectListName);
    	} catch (Exception e) {
			logger.error("combineDataObjectListAutomation had an error: " + e.getMessage(), e);
 		}
    	return null;
    }  
    
 	public List<DataObject> appendPendingChildDataObjects (List<DataObject> dataObjectListSDO, String path, Boolean isRootPending, String businessEntity, String rootRowid, String interactionId, CallContext callContext
 			,CompositeServiceClient besClient,List<String> deletedKeys ) {
 		try {
 			logger.debug("ENTERING appendPendingChildDataObjects for: " + path);
 			logger.debug("Input List Size: " + ListHelper.getListSize(dataObjectListSDO));
 			List<DataObject> dataObjectList = dataObjectListSDO;
 			
	 		if (isRootPending) {
		        DataObject readPendingChildReturn = dataObjectReader.readPendingChild(callContext, besClient, businessEntity, rootRowid, businessEntity+"."+path, interactionId);		    			        
		        if (readPendingChildReturn != null) {
		        	String listPath = path.replaceAll("\\.", "/item[1]/");
		        	listPath = listPath.replaceAll("ExSite\\/item[1]]", "ExSite");
		        	logger.debug("listPath: " + listPath);
		        	List<DataObject> readPendingChildReturnList = readPendingChildReturn.getList(listPath + "/item");
		        	
		        	if (ListHelper.listExists(readPendingChildReturnList)) {
		        		logger.debug("Found pending children object to append in changelist");
			            logger.debug("Combining " + path);
				        
			            dataObjectList  = combineDataObjectList(dataObjectList, dataObjectSearcher.filterDeleted(dataObjectSearcher.filterByInteractionId(readPendingChildReturnList,interactionId),deletedKeys), isRootPending);
			            		            		    				     	
				        logger.debug("readPendingChildReturnList size: " + ListHelper.getListSize(readPendingChildReturnList));
				        logger.debug("dataObjectList size: " + ListHelper.getListSize(dataObjectList));
				        logger.debug("");
		        	} else {
		        		logger.debug("No pending child objects found for appending to changelist");
		        	}
		        } else {
		        	logger.debug("appendPendingChildDataObjects ORS NOT FOUND");
		        }
		    }
	 		logger.debug("Output List Size: " + ListHelper.getListSize(dataObjectList));
	 		return dataObjectList;
 		} catch (Exception e) {
 			logger.error("appendPendingChildDataObjects had an error: " + e.getMessage(),e);
 		}
 		logger.debug("appendPendingChildDataObjects returning null");
 		return dataObjectListSDO;
 	}
 	
 	public List<DataObject> appendPendingChildDataObjectsAutomation (List<DataObject> dataObjectListSDO, String path, Boolean isRootPending, String businessEntity, String rootRowid, String interactionId, CallContext callContext
 			,CompositeServiceClient besClient, DataObject parentSdo, DataFactory dataFactory, DataObject parentORS, String additionalPath) {
 		try {
 			logger.debug("ENTERING appendPendingChildDataObjectsAutomation for: " + path);
 			logger.debug("Input List Size: " + ListHelper.getListSize(dataObjectListSDO));
 			List<DataObject> dataObjectList = dataObjectListSDO;
	 		if (isRootPending) {
	 			String fullPath = null;
	 			if (additionalPath != null)
	 				fullPath = businessEntity + "." + path + "." + additionalPath;
	 			else
	 				fullPath = businessEntity + "." + path;
		        DataObject readPendingChildReturn = dataObjectReader.readPendingChild(callContext, besClient, businessEntity, rootRowid, fullPath, interactionId);		    			        
		        if (readPendingChildReturn != null) {
		        	logger.info("HERE");
//		        	String listPath = path.replaceAll("\\.", "/item[1]/");
//		        	logger.debug("listPath: " + listPath);
		        	List<DataObject> readPendingChildReturnList = readPendingChildReturn.getList(path + "/item");
		        	
		        	if (ListHelper.listExists(readPendingChildReturnList)) {
		        		logger.info("Found pending children object to append in changelist");
			            logger.info("Combining " + path);
				        
			            String objectListName = path.substring(path.lastIndexOf("\\.") + 1);
			            logger.info(" objectListName: " + objectListName);
			            dataObjectList  = combineDataObjectListAutomation(dataObjectListSDO, readPendingChildReturnList, parentSdo, objectListName, parentORS);			            
			            		    				     	
				        logger.info("readPendingChildReturnList size: " + ListHelper.getListSize(readPendingChildReturnList));
				        logger.info("dataObjectList size: " + ListHelper.getListSize(dataObjectList));
				        logger.info("");
		        	} else {
		        		logger.debug("No pending child objects found for appending to changelist");
		        	}
		        } else {
		        	logger.debug("appendPendingChildDataObjectsAutomation ORS NOT FOUND");
		        }
		    }
	 		logger.debug(path + " output List Size: " + ListHelper.getListSize(dataObjectList));
	 		return dataObjectList;
 		} catch (Exception e) {
 			logger.error("appendPendingChildDataObjectsAutomation had an error: " + e.getMessage(),e);
 		}
 		logger.debug("appendPendingChildDataObjectsAutomation returning null");
 		return dataObjectListSDO;
 	}

}
