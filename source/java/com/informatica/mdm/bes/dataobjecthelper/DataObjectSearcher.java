package com.informatica.mdm.bes.dataobjecthelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.helper.ListHelper;
import com.informatica.mdm.bes.helper.PredicateUtil;
import com.informatica.mdm.bes.helper.StringHelper;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class DataObjectSearcher {
	protected static Logger logger = Logger.getLogger(StringHelper.class.getClass());
	
	private DataObjectFieldHelper dataObjectFieldHelper;
	private DataObjectGeneralHelper dataObjectGeneralHelper;
	
	public DataObjectSearcher(DataObjectFieldHelper dataObjectFieldHelper,
								DataObjectGeneralHelper dataObjectGeneralHelper) {
		this.dataObjectFieldHelper = dataObjectFieldHelper;
		this.dataObjectGeneralHelper = dataObjectGeneralHelper;
	}
	
	public List<DataObject> filterGSAROuBuMapReturn(List<DataObject> readGSAROuBuMapReturn, String GSARQuestion, String exOuBuEntity){
		logger.debug("GSARQuestion: "+ GSARQuestion);
		logger.debug("exOuBuEntity: "+ exOuBuEntity);
		if (ListHelper.listExists(readGSAROuBuMapReturn) && readGSAROuBuMapReturn.get(1) != null) {
			logger.debug("exOuBuEntity: "+ readGSAROuBuMapReturn.get(1).getString("ExOUBUMapLookup/ExGSARQuestions"));
			logger.debug("exOuBuEntity: "+ readGSAROuBuMapReturn.get(1).getString("ExOUBUMapLookup/ExOUBUEntity/exImpactedBUEntityCd"));

		}		
		return readGSAROuBuMapReturn.stream()
			.filter(PredicateUtil.andLogFilteredOutValues(data -> 
					(StringHelper.compare(GSARQuestion,data.getString("ExOUBUMapLookup/ExGSARQuestions"))) &&
					(StringHelper.compare(exOuBuEntity,data.getString("ExOUBUMapLookup/ExOUBUEntity/exImpactedBUEntityCd"))) 			
					))			
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
    public DataObject searchDataObjectList (List<DataObject> dataObjectList, String rowid) {
    	rowid = rowid.trim();
		logger.debug("Entering searchDataObjectList");
		if (dataObjectList == null || rowid == null) {
			logger.error("dataObjectList or rowid was sent as null");
			return null;
		}
		
		try {
			for (DataObject dataObject : dataObjectList) {
				logger.debug("getDataObjectRowid(dataObject)" + dataObjectFieldHelper.getDataObjectRowid(dataObject));
				String dataObjectRowid = dataObjectFieldHelper.getDataObjectRowid(dataObject).trim();
				if (StringHelper.stringExists(dataObjectRowid) && dataObjectRowid.equals(rowid)) {
					logger.debug("Search Data Object List by Rowid FOUND: " + rowid + " " + dataObject.getType().getName());
					return dataObject;
				}
			}	
		} catch (Exception e) {
 			logger.error("appendError had an error: " + e.getMessage(), e);
  		}
		logger.debug("Search Data Object List by Rowid not found, returning null: ");
		return null;
	}
    
    public DataObject getChildObject(DataObject dataObjectParent,String childName, 
    										String rowidToSearch, String pkeyToSearch) {
    	logger.debug("ENTERING getChildObject for " + childName);
    	logger.debug(childName + " rowid to search: " + rowidToSearch);
		logger.debug(childName + " pkey to search: " + pkeyToSearch);
		logger.debug("");
    	if (dataObjectParent == null || childName == null || (rowidToSearch == null && pkeyToSearch == null)) {
    		logger.error("Either DataObjectParent, ChildName, RowidToSearch or PkeyToSearch is null");
    		return null;
    	}

    	try {
			List<DataObject> dataObjectList = (childName.equalsIgnoreCase("ExPPASiteRel/ExSite")) 
					? dataObjectParent.getList("ExPPASiteRel/item") 
					: dataObjectParent.getList(childName+"/item");
					
			for(DataObject dataObjectChild: dataObjectList) {
				if (childName.equalsIgnoreCase("ExPPASiteRel/ExSite")) {
					dataObjectChild = dataObjectChild.getDataObject("ExSite");
				}
				String rowid = dataObjectFieldHelper.getDataObjectRowid(dataObjectChild);
				String pkey  = dataObjectFieldHelper.getDataObjectPkey(dataObjectChild);
				logger.debug(childName + " rowid to search: " + rowid);
				logger.debug(childName + " pkey to search: " + pkey);
				if (rowid != null && rowidToSearch != null && rowid.trim().equalsIgnoreCase(rowidToSearch.trim())) {
					logger.debug("Get Child Data Object Found with ROWID for: " + childName);
					return dataObjectChild;
				}
				if (pkey != null && pkeyToSearch != null && pkey.trim().equalsIgnoreCase(pkeyToSearch.trim())) {
					logger.debug("Get Child Data Object Found with PKEY for: " + childName);
					return dataObjectChild;
				}
			}	
    	} catch (Exception e) {
			logger.error("getChildObject had an error: " + e.getMessage(), e);
 		}
		logger.debug("Get Child Data Object Not found, returning null for: " + childName);
    	return null;
    }
    
    
    
    @SuppressWarnings("unchecked")
	public List<DataObject> getChildObjectList(DataObject dataObjectParent,String childName) {
    	if (dataObjectParent == null || childName == null) {
    		logger.error("dataObjectParent or chidName was sent as null. dataObjectParent: " + dataObjectParent + ", childName: " + childName);
    		return null;
    	}
    	List<DataObject> dataObjectList = dataObjectParent.getList(childName+"/item");
    	if (!ListHelper.listExists(dataObjectList)) {
    		logger.debug("Get Child Data Object Not found, returning null for: " + childName);
    		return null;
    	}
    	return dataObjectList;
    }
    
    public String getStringFilterDeleted(DataObject sdo, DataObject ors, String path) {
    	if (sdo == null || ors == null || path == null) {
    		logger.error("SDO, ORS, or Path was sent as null. SDO: " + sdo + " ORS: " + ors + ", Path: " + path);
    		return null;
    	}
    	Boolean isDeleted = sdo.getChangeSummary().isDeleted(sdo.getDataObject(path));
    	String lookupString = dataObjectGeneralHelper.chooseOrsSdoString(sdo, ors, path);
    	return isDeleted != null && isDeleted ? lookupString : null;
 	}
    
	public DataObject getDeepDataObject (Integer depth, DataObject dataObject,ArrayList<String> coNodeListString,HelperContext helperContext) {
		logger.debug("Entering getDeepDataObject");

		if (dataObject == null || coNodeListString == null) {
			logger.error("dataObject or coNodeListString was null");
			return null;
		}
		
		try {
			
			Integer coNodeListStringLength = ListHelper.getListSize(coNodeListString);
			logger.debug("coNodeListStringLength: " + coNodeListStringLength);

			DataObject tmpDO = dataObject;
			for (int x = 1;x<coNodeListStringLength;x++) {	
								
				if (dataObjectFieldHelper.isMany(tmpDO)) {	
					logger.debug("isMany: true" + " - " + coNodeListString.get(x));					
					List<DataObject> tmpDOList =  getChildObjectList(tmpDO, coNodeListString.get(x));
					logger.debug("tmpDOList Count: " + ListHelper.getListSize(tmpDOList));
					if (ListHelper.getListSize(tmpDOList)>1) {
						//TODO check if occurs - should never happen
			    		logger.error("ERROR: getDeepDataObject found multiple children.  Only works for single child data objects.  Returning null");
			    		return null; 
					}else {
						tmpDO = tmpDOList.get(0);
					}
					tmpDO = tmpDOList.get(0);
				} else {
					tmpDO = tmpDO.getDataObject(coNodeListString.get(x));
					// dump(helperContext,"tmpDO: " + x, tmpDO);
				}					
			}
			return tmpDO;
			
		} catch (Exception e) {
    		logger.error("getDeepDataObject had an error: " + e.getMessage(),e);
    	}
		return null; 
	}
	
	public List<DataObject> filterDeleted(List<DataObject> list,List<String> deletedKeys){
 		if (ListHelper.listExists(list) && ListHelper.listExists(deletedKeys)) { 		
 			logger.debug("deletedKeys: " + deletedKeys);
 			logger.debug("list: " + list);
 			return list.stream()
 					.filter(PredicateUtil.andLogFilteredOutValues(data -> !StringHelper.stringExists(dataObjectFieldHelper.getDataObjectRowid(data)) 
 							|| !deletedKeys.contains(dataObjectFieldHelper.getDataObjectRowid(data).trim()+data.getType().getName().trim())))
 					.collect(Collectors.toCollection(ArrayList::new));
 		}
		return list;
	}
 	
 	
	public List<DataObject> filterByInteractionId(List<DataObject> list, String inputInteractionId){		
 		if (ListHelper.listExists(list) && StringHelper.stringExists(inputInteractionId)) {
 			return list.stream()
 					.filter(PredicateUtil.andLogFilteredOutValues(data -> data.getString("interactionId") != null && data.getString("interactionId").trim().equalsIgnoreCase(inputInteractionId.trim())))			
 					.collect(Collectors.toCollection(ArrayList::new));
 		}
 		return list;
	}
 	
	public List<DataObject> filterByString(List<DataObject> list, String searchString,String fieldName){		
 		if (ListHelper.listExists(list) && StringHelper.stringExists(searchString)) {
 			return list.stream()
 					.filter(PredicateUtil.andLogFilteredOutValues(data -> data.getString(fieldName) != null && data.getString(fieldName).trim().equalsIgnoreCase(searchString.trim())))			
 					.collect(Collectors.toCollection(ArrayList::new));
 		}
 		return list;
	}
 	
 	
	public List<DataObject> filterByActiveStatus(List<DataObject> list, String activeStatus){		
 		if (ListHelper.listExists(list)) {
	 		return list.stream()
				.filter(PredicateUtil.andLogFilteredOutValues(data -> data.getString("exSttus/exStatusCd") != null && data.getString("exSttus/exStatusCd").equalsIgnoreCase(activeStatus)))			
				.collect(Collectors.toCollection(ArrayList::new));
 		}
 		return list;
	}
	
	public List<String> getDeletedRowids(DataObject inputSDO, String childPath) {
		List<String> deletedRowids = new ArrayList<>();
		for (DataObject changedDataObject : (List<DataObject>) inputSDO.getChangeSummary().getChangedDataObjects()) {
			if (changedDataObject.isSet(childPath)) {
				List<DataObject> changedPiList = changedDataObject.getList(childPath + "/item");
				for (DataObject changedPi : changedPiList) {
					if (inputSDO.getChangeSummary().isDeleted(changedPi)) {
						String deletedRowid = changedPi.getString("rowidObject");
						if (deletedRowid != null)
							deletedRowids.add(deletedRowid);
					}
				}
			}
		}
		return deletedRowids;
	}
	
    public List<String> getDeletedRowids(DataObject rootSdo) {
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
        
        if (csdl != null && csdl.size()>0) {
	        for(DataObject csd:csdl) {
	        	 String name = (String) csd.getType().getName();
		       	 String deletedRowid = csd.getString("rowidObject");
		       	 logger.debug("Deleted Rowid: " + deletedRowid + " for " + name);
		       	 if (deletedRowid != null) {
		       		 deletedRowids.add(deletedRowid.trim()+name.trim());
		       	 }
	        }
        }
		return deletedRowids;
 	}
}
