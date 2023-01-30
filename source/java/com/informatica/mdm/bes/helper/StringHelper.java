package com.informatica.mdm.bes.helper;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class StringHelper {
	protected static Logger logger = Logger.getLogger(StringHelper.class.getName());
	
	private static final String PARSE_CONTEXT_USERNAME_SEPERATOR = "/";
	
	public static ArrayList<String> convertStringToArrayByPeriod (String string) {
		if (stringExists(string))
			return new ArrayList<>(Arrays.asList(string.split("\\.")));
		else
			logger.error("String sent was null"); return null;		
	}

	public static boolean compare(String str1, String str2) {		
		if (!stringExists(str1) || !stringExists(str2)) {
			logger.error("String 1 or String 2 was null str1: " + str1 + " str2: " + str2); 
			return false;
		}
		return str1.equals(str2);
	}
	
    public static Boolean stringExists(String string) {
    	return (string != null && string != "" && !string.isEmpty()) ? true : false; 
    }

    public static boolean fieldExists (String field) {
    	Boolean fieldExists = (field != null && !field.isEmpty() ? true : false);
    	return fieldExists;    	
    }
    
    public static String parseContextUserName(String user) {
		int sepPos = user.indexOf(PARSE_CONTEXT_USERNAME_SEPERATOR);
		return user.substring(sepPos + PARSE_CONTEXT_USERNAME_SEPERATOR.length());
 	}
}
