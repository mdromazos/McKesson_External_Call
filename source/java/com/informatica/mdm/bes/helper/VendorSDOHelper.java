package com.informatica.mdm.bes.helper;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;
import org.eclipse.persistence.sdo.SDODataObject;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;

import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.LookupConstants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;

import commonj.sdo.ChangeSummary.Setting;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class VendorSDOHelper {
	DataObjectHelperContext dataObjectHelperContext = new DataObjectHelperContext();
	private static Logger logger = Logger.getLogger(VendorSDOHelper.class.getName());
	
	public boolean getIsRootPending(Map<String, Object> inParams) {
		String recordState = (String)inParams.get("recordState");
		Boolean isRootPending = (recordState != null && recordState.equalsIgnoreCase("PENDING"))? true: false;
		return isRootPending;
	}
	
	public synchronized List<DataObject> getCombinedListAutomation(DataObject inputParentSDO, DataObject orsParentSDO, String childPath, DataObject inputSDO, HelperContext helperContext) {
		List<DataObject> combinedList;
		List<DataObject> inputList = inputParentSDO.getList(childPath + "/item");
		if (orsParentSDO != null && orsParentSDO.getList(childPath + "/item") != null) {
			List<DataObject> orsListCopied = new ArrayList<DataObject>();
			List<DataObject> orsList = orsParentSDO.getList(childPath + "/item");
			for (DataObject ors : orsList)
				orsListCopied.add(helperContext.getCopyHelper().copy(ors));
			
			List<String> deletedRowids = dataObjectHelperContext.getDataObjectSearcher().getDeletedRowids(inputSDO);
			for (String deletedRowid : deletedRowids)
				logger.info("DELETED ROWID: " + deletedRowid);
			
			List<DataObject> orsListFiltered = dataObjectHelperContext.getDataObjectSearcher().filterDeleted(orsListCopied, deletedRowids);
			combinedList = dataObjectHelperContext.getDataObjectCombiner().combineDataObjectListAutomation(inputList, orsListFiltered, inputParentSDO, childPath, orsParentSDO);
		} else {
			combinedList = inputList;
		}
		return combinedList;
	}
	
	public List<DataObject> getCombinedList(DataObject inputParentSDO, DataObject orsParentSDO, String childPath, DataObject inputSDO, HelperContext helperContext) {
		List<DataObject> combinedList;
		List<DataObject> inputList = inputParentSDO.getList(childPath + "/item");
		if (orsParentSDO != null && orsParentSDO.getList(childPath + "/item") != null) {
			List<DataObject> orsListCopied = new ArrayList<DataObject>();
			List<DataObject> orsList = orsParentSDO.getList(childPath + "/item");
			for (DataObject ors : orsList)
				orsListCopied.add(helperContext.getCopyHelper().copy(ors));
			
			List<String> deletedRowids = dataObjectHelperContext.getDataObjectSearcher().getDeletedRowids(inputSDO);
			for (String deletedRowid : deletedRowids)
				logger.info("DELETED ROWID: " + deletedRowid);
			
			List<DataObject> orsListFiltered = dataObjectHelperContext.getDataObjectSearcher().filterDeleted(orsListCopied, deletedRowids);
			combinedList = dataObjectHelperContext.getDataObjectCombiner().combineDataObjectList(inputList, orsListFiltered);
		} else {
			combinedList = inputList;
		}
		return combinedList;
	}
	
	public List<DataObject> getCombinedList(DataObject inputParentSDO, DataObject orsParentSDO, String childPath, DataObject inputSDO, boolean isRootPending) {
		List<DataObject> combinedList;
		List<DataObject> inputList = inputParentSDO.getList(childPath + "/item");
		if (orsParentSDO != null) {
			List<DataObject> orsList = orsParentSDO.getList(childPath + "/item");
			List<String> deletedRowids = dataObjectHelperContext.getDataObjectSearcher().getDeletedRowids(inputSDO);
			for (String deletedRowid : deletedRowids) {
				logger.info("DELETED ROWID: " + deletedRowid);
			}
			List<DataObject> orsListFiltered = dataObjectHelperContext.getDataObjectSearcher().filterDeleted(orsList, deletedRowids);
			combinedList = dataObjectHelperContext.getDataObjectCombiner().combineDataObjectListAutomation(inputList, orsListFiltered, isRootPending, inputParentSDO, null, childPath, orsParentSDO);
		} else {
			combinedList = inputList;
		}
		return combinedList;
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
	
	public float getFloat(DataObject parentInputSDO, DataObject parentOrsSDO, String path) {
		//String stringValue = parentInputSDO.getString(path);
		float value = parentInputSDO.getFloat(path);
		if(Float.compare(value, 0)==0) 
			value = parentOrsSDO.getFloat(path);
		return value;
	}
	
	public double getDouble(DataObject parentInputSDO, DataObject parentOrsSDO, String path) {
		//String stringValue = parentInputSDO.getString(path);
		double value = parentInputSDO.getDouble(path);
		if (parentInputSDO != null && !parentInputSDO.isSet(path))
			value = parentOrsSDO.getDouble(path);

		return value;
	}
	
	public int getInt(DataObject parentInputSDO, DataObject parentOrsSDO, String path) {
		//String stringValue = parentInputSDO.getString(path);
		int value = parentInputSDO.getInt(path);
		if(Float.compare(value, 0)==0) 
			value = parentOrsSDO.getInt(path);
		return value;
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
	
	public DataObject getRequestByInteractionId(DataObject dataObject, String interactionId) {
		if (dataObject == null)
			return null;
		
		List<DataObject> requests = dataObject.getList(VendorMainConstants.REQUEST + "/item");
		if (requests == null || requests.isEmpty())
			return null;
		
		List<DataObject> requestsByInteractionId = dataObjectHelperContext.getDataObjectSearcher().filterByInteractionId(requests, interactionId);
		if (requestsByInteractionId == null || requestsByInteractionId.isEmpty())
			return null;
		
		return requestsByInteractionId.get(0);
	}
	
	/**
	 * This method gets the names all of the old values in the change summary put into one single ArrayList.  
	 * @param promotePreviewChangeSummary
	 * @param inputSDOChangeSummary
	 * @return
	 */
	public Map<String, List<SDODataObject>> getDataObjectsByType(List<SDODataObject> promoteDataObjects, List<SDODataObject> inputDataObjects) {
		Map<String, List<SDODataObject>> dataObjectsByType = new HashMap<String, List<SDODataObject>>();
		
		if (promoteDataObjects != null) {
			for (SDODataObject promoteDataObject : promoteDataObjects) {
				String[] nameSplit = promoteDataObject.getType().getName().split("\\.");
				String key = nameSplit[nameSplit.length-1];
				dataObjectsByType.putIfAbsent(key, new ArrayList<SDODataObject>());
				dataObjectsByType.get(key).add(promoteDataObject);
			}
		}
		
		for (SDODataObject inputDataObject : inputDataObjects) {
			String[] nameSplit = inputDataObject.getType().getName().split("\\.");
			String key = nameSplit[nameSplit.length-1];
			dataObjectsByType.putIfAbsent(key, new ArrayList<SDODataObject>());
			dataObjectsByType.get(key).add(inputDataObject);
		}		
		return dataObjectsByType;
	}
	
	public boolean didFieldChangeToNull(DataObject inputSdoParent, SDOChangeSummary inputSdoChangeSummary, String fieldName) {
		boolean didFieldChangeToNull = false;
		Object fieldValue = inputSdoParent.get(fieldName);
		if (fieldValue == null) {
			Property fieldProperty = inputSdoParent.getInstanceProperty(fieldName);
			Setting oldValue = inputSdoChangeSummary.getOldValue(inputSdoParent, fieldProperty);
			if (oldValue != null && oldValue.getValue() != null) {
				didFieldChangeToNull = true;
			}
		}
		
		return didFieldChangeToNull;
	}
	
	public boolean didFieldChangeFromYToN(DataObject inputSdoParent, SDOChangeSummary inputSdoChangeSummary, String fieldName) {
		boolean didFieldChangeFromYToNo = false;
		Object fieldValue = inputSdoParent.get(fieldName);
		if (fieldValue != null && fieldValue.equals("N")) {
			Property fieldProperty = inputSdoParent.getInstanceProperty(fieldName);
			Setting oldValue = inputSdoChangeSummary.getOldValue(inputSdoParent, fieldProperty);
			if (oldValue != null && oldValue.getValue().equals("Y")) {
				didFieldChangeFromYToNo = true;
			}
		}
		
		return didFieldChangeFromYToNo;
	}
	
	public boolean didFieldChangeFrom(DataObject inputSdoParent, SDOChangeSummary inputSdoChangeSummary, String fieldName, Object didFieldChangeFrom, Object didFieldChangeTo) {
		boolean didFieldChangeFromYToNo = false;
		Object fieldValue = inputSdoParent.get(fieldName);
		if (fieldValue != null && fieldValue.equals(didFieldChangeTo)) {
			Property fieldProperty = inputSdoParent.getInstanceProperty(fieldName);
			Setting oldValue = inputSdoChangeSummary.getOldValue(inputSdoParent, fieldProperty);
			if (oldValue != null && oldValue.getValue().equals(didFieldChangeFrom)) {
				didFieldChangeFromYToNo = true;
			}
		}
		
		return didFieldChangeFromYToNo;
	}
	
	public synchronized void setString(DataObject inputSDO, String fieldName, String fieldValue, SDOChangeSummary sdoChangeSummary) {
		sdoChangeSummary.resumeLogging();
		inputSDO.setString(fieldName, fieldValue);
		sdoChangeSummary.pauseLogging();
	}
	
	public synchronized void set(DataObject inputSDO, String fieldName, Object fieldValue, SDOChangeSummary sdoChangeSummary) {
		sdoChangeSummary.resumeLogging();
		inputSDO.set(fieldName, fieldValue);
		sdoChangeSummary.pauseLogging();
	}
	
	public synchronized void setDataObject(DataObject inputSDO, String fieldName, DataObject fieldValue, SDOChangeSummary sdoChangeSummary) {
		sdoChangeSummary.resumeLogging();
		inputSDO.setDataObject(fieldName, fieldValue);
		sdoChangeSummary.pauseLogging();
	}
	
	public synchronized void setBoolean(DataObject inputSDO, String fieldName, Boolean fieldValue, SDOChangeSummary sdoChangeSummary) {
		sdoChangeSummary.resumeLogging();
		inputSDO.setBoolean(fieldName, fieldValue);
		sdoChangeSummary.pauseLogging();
	}
	
	public synchronized void setInt(DataObject inputSDO, String fieldName, Integer fieldValue, SDOChangeSummary sdoChangeSummary) {
		sdoChangeSummary.resumeLogging();
		inputSDO.setInt(fieldName, fieldValue);
		sdoChangeSummary.pauseLogging();
	}
	
	public synchronized void setDouble(DataObject inputSDO, String fieldName, Double fieldValue, SDOChangeSummary sdoChangeSummary) {
		sdoChangeSummary.resumeLogging();
		inputSDO.setDouble(fieldName, fieldValue);
		sdoChangeSummary.pauseLogging();
	}
	
	public synchronized DataObject createDataObject(DataObject inputSDO, String fieldName, SDOChangeSummary sdoChangeSummary) {
		sdoChangeSummary.resumeLogging();
		DataObject result = inputSDO.createDataObject(fieldName);
		sdoChangeSummary.pauseLogging();
		return result;
	}
	
	public synchronized void addItemToList(List<DataObject> inputSDOList, DataObject itemToAdd, SDOChangeSummary sdoChangeSummary) {
		sdoChangeSummary.resumeLogging();
		inputSDOList.add(itemToAdd);
		sdoChangeSummary.pauseLogging();
	}
	
	public String getInteractionId(Map<String, Object> inParams, DataObject inputSDOBe, DataObject orsSDOBe) {
		String interactionId = (String) inParams.get(Constants.INTERACTION_ID);
		if (interactionId == null)
			interactionId = inputSDOBe.getString(Constants.INTERACTION_ID);
		if (interactionId == null)
			interactionId = orsSDOBe.getString(Constants.INTERACTION_ID);
		return interactionId;
	}
	
	public List<DataObject> getListByHubStateInd(DataObject inputParent, DataObject promoteParent, String hubStateInd, 
			HelperContext helperContext, String childName) {
		List<DataObject> result = null;
		if ("1".equals(hubStateInd)) {
			return inputParent.getList(childName + "/item");
		} else {
			return getCombinedList(inputParent, 
					promoteParent, childName, inputParent.getRootObject(), helperContext);
		}
	}
}
