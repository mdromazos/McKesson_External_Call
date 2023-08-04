package com.informatica.mdm.bes.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class SDOHelper {	
	
	public static List<DataObject> getCombinedList(DataObject inputParentSDO, DataObject orsParentSDO, 
			String childPath, DataObject inputSDO, HelperContext helperContext, DataObjectHelperContext dataObjectHelperContext) {
		List<DataObject> combinedList;
		List<DataObject> inputList = inputParentSDO.getList(childPath + "/item");
		if (orsParentSDO != null && orsParentSDO.getList(childPath + "/item") != null) {
			List<DataObject> orsListCopied = new ArrayList<DataObject>();
			List<DataObject> orsList = orsParentSDO.getList(childPath + "/item");
			for (DataObject ors : orsList)
				orsListCopied.add(helperContext.getCopyHelper().copy(ors));
			
			List<String> deletedRowids = dataObjectHelperContext.getDataObjectSearcher().getDeletedRowids(inputSDO);
			
			List<DataObject> orsListFiltered = dataObjectHelperContext.getDataObjectSearcher().filterDeleted(orsListCopied, deletedRowids);
			combinedList = dataObjectHelperContext.getDataObjectCombiner().combineDataObjectList(inputList, orsListFiltered);
		} else {
			combinedList = inputList;
		}
		return combinedList;
	}
	
	public static DataObject getDataObject(DataObject inputSDO, DataObject orsSDO, String path) {
		DataObject dataObject = null;
		if (inputSDO != null)
			dataObject = inputSDO.getDataObject(path);
		if (dataObject == null && orsSDO != null)
			dataObject = orsSDO.getDataObject(path);
		return dataObject;
	}
	
	public static DataObject getDataObject(DataObject inputSDO, DataObject promoteSDO, DataObject orsSDO, String path) {
		DataObject dataObject = null;
		if (inputSDO != null)
			dataObject = inputSDO.getDataObject(path);
		if (dataObject == null && promoteSDO != null)
			dataObject = promoteSDO.getDataObject(path);
		if (dataObject == null && orsSDO != null)
			dataObject = orsSDO.getDataObject(path);
		return dataObject;
	}
	
	public static Date getDate(DataObject inputSDO, DataObject orsSDO, String path) {
		Date date = null;
		if (inputSDO != null)
			date = inputSDO.getDate(path);
		if (date == null && orsSDO != null)
			date = orsSDO.getDate(path);
		return date;
	}
	
	public static String getString(DataObject parentInputSDO, DataObject parentOrsSDO, String path) {
		String stringValue = parentInputSDO.getString(path);
		if (stringValue == null && parentOrsSDO != null)
			stringValue = parentOrsSDO.getString(path);
		return stringValue;
	}
	
	public static float getFloat(DataObject parentInputSDO, DataObject parentOrsSDO, String path) {
		//String stringValue = parentInputSDO.getString(path);
		float value = parentInputSDO.getFloat(path);
		if(Float.compare(value, 0)==0) 
			value = parentOrsSDO.getFloat(path);
		return value;
	}
	
	public static int getInt(DataObject parentInputSDO, DataObject parentOrsSDO, String path) {
		//String stringValue = parentInputSDO.getString(path);
		int value = parentInputSDO.getInt(path);
		if(Float.compare(value, 0)==0) 
			value = parentOrsSDO.getInt(path);
		return value;
	}
	
	public static String getString(DataObject parentInputSDO, DataObject promoteSDO, DataObject parentOrsSDO, String path) {
		String stringValue = null;
		if (parentInputSDO != null)
			stringValue = parentInputSDO.getString(path);
		if (stringValue == null && promoteSDO != null)
			stringValue = promoteSDO.getString(path);
		if (stringValue == null && parentOrsSDO != null)
			stringValue = parentOrsSDO.getString(path);
		return stringValue;
	}
	
	public static Boolean getBoolean(DataObject inputSDO, DataObject orsSDO, String path) {
		Boolean booleanValue = null;
		if (inputSDO != null)
			booleanValue = inputSDO.getBoolean(path);
		if (booleanValue == null && orsSDO != null)
			booleanValue = orsSDO.getBoolean(path);
		return booleanValue;
	}
	
	public static Boolean getBoolean(DataObject parentInputSDO, DataObject parentOrsSDO, DataObject parentPromoteSDO, String path) {
		Boolean booleanValue = parentInputSDO.getBoolean(path);
		// if the inputSDO does not contain the dataobject - try the promotePreview
		if (booleanValue == null  && parentPromoteSDO != null) 
			booleanValue = parentPromoteSDO.getBoolean(path);
		// If the promote preview does not contain the data object, last resort try the orsSDO
		if (booleanValue == null && parentOrsSDO != null)
			booleanValue = parentOrsSDO.getBoolean(path);
		
		return booleanValue;
	}
	
	public static Object get(DataObject inputSDO, DataObject orsSDO, String path) {
		Object value = inputSDO.get(path);
		if (value == null && orsSDO != null)
			value = orsSDO.get(path);
		return value;
	}
}
