package com.informatica.mdm.bes.helper;

import java.util.function.Predicate;

import org.apache.log4j.Logger;

/**
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class PredicateUtil {
	protected static Logger logger = Logger.getLogger(PredicateUtil.class.getName());

	
	public static <T> Predicate<T> andLogFilteredOutValues(Predicate<T> predicate) {
	    return value -> {
	        if (predicate.test(value)) {
	            return true;
	        } else {
	            try {
					logger.debug("filtered value "+ value.toString()+ " " + value.getClass().getField("rowidObject"));
				} catch (NoSuchFieldException | SecurityException e) {
					e.printStackTrace();
				}
	            return false;
	        }
	    };
	}
}
