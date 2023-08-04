package com.informatica.mdm.bes.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
    // Regex to check valid ISIN Code
    public static final String SWIFT_REGEX = "^[A-Z]{4}[-]{0,1}[A-Z]{2}[-]{0,1}[A-Z0-9]{2}[-]{0,1}[0-9]{3}$";
	
    // Regex to check valid ISIN Code
    public static final String ROUTING_REGEX = "\\s*(\\d{9})(?:[^\\d]|$)";
    
	public static boolean validateSwift(String swift) {
	    return validateString(swift, SWIFT_REGEX);
	}
	
	public static boolean validateRouting(String routingNum) {
	    return validateString(routingNum, SWIFT_REGEX);
	}
	
	
	private static boolean validateString(String value, String regex) {
	    // Compile the ReGex
	    Pattern p = Pattern.compile(regex);
	 
	    // If the swift_code
	    // is empty return false
	    if (value == null) {
	        return false;
	    }
	 
	    // Pattern class contains matcher() method
	    // to find matching between given
	    // swift_code  using regular expression.
	    Matcher m = p.matcher(value);
	 
	    // Return if the swift_code
	    // matched the ReGex
	    return m.matches();
	}
}
