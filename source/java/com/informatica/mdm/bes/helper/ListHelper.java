package com.informatica.mdm.bes.helper;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class ListHelper {
	protected static Logger logger = Logger.getLogger(ListHelper.class.getName());

    public static boolean listExists (List<?> list) {
    	return (list != null && list.size()>0 && !	list.isEmpty()) ? true : false;
    }
	
    public static Integer getListSize(List<?> list) {
    	return (listExists(list))?list.size():0;
    }
}
