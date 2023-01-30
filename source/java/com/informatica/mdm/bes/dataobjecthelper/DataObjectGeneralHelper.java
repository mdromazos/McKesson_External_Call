package com.informatica.mdm.bes.dataobjecthelper;

import java.util.List;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.helper.StringHelper;

import commonj.sdo.DataObject;

public class DataObjectGeneralHelper {
	private static Logger logger = Logger.getLogger(StringHelper.class.getName());

	
	public Boolean isE360Save(Boolean validateOnly) {		
		return (validateOnly != null && !validateOnly) ? true : false;
	}
	
	public DataObject getRootSDOObject(DataObject inputSdo) {
		return (inputSdo != null) ? (DataObject) inputSdo.get(1) : null;
	}
	
    public String chooseOrsSdoString(DataObject sdo, DataObject ors, String path) {
    	String value = (sdo != null)? sdo.getString(path):null;   
    	return (value == null && ors != null)? ors.getString(path): value;
    }
	
 	public static Boolean isSystemUserBypass(DataObject sdo,List<String> roles, String path, String roleAppAdmin) {
 		// DEPRECATED CODE
//    	try {
//    		logger.debug("Starting isSystemUserBypass");
//	 		List<DataObject> gsarWorkflowStatusList = (sdo != null) ? sdo.getList(path): null;
//	    	if (gsarWorkflowStatusList != null || UserRoleUtil.isUserRoleMatch(roles,roleAppAdmin)) {
//	    		return true;
//	    	}
//    	} catch (Exception e) {
// 			logger.error("Exception caught in the catch block..isSystemUserBypass..." + e.getMessage(), e); 		
// 		}
//    	return false;
    	
    	return (sdo != null && sdo.getList(path) != null || (roles != null && roles.contains(roleAppAdmin)));
 	}
}


