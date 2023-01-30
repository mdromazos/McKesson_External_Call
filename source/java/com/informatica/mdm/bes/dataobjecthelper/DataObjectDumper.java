package com.informatica.mdm.bes.dataobjecthelper;

import java.util.List;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.helper.ListHelper;
import com.informatica.mdm.bes.helper.StringHelper;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class DataObjectDumper {
	protected static Logger logger = Logger.getLogger(StringHelper.class.getName());
	
 	public void dumpList(HelperContext helperContext, String title, List<DataObject> dataObjectList) {
    	if (ListHelper.getListSize(dataObjectList)>0) {
	 		for (DataObject dataObject: dataObjectList) {
				dump(helperContext,title, dataObject);
			}
    	}
    }
        
 	public void dump(HelperContext helperContext, String title, DataObject dataObject) {
 		logger.debug(">>> Entering dump");
 		if (dataObject != null) {
 			String xml = helperContext.getXMLHelper().save(
 	                dataObject,
 	                dataObject.getType().getURI(),
 	                dataObject.getType().getName());

 	        logger.debug(title);
 	        logger.debug(xml);
 	        logger.debug("");

 		}   
    }
}
