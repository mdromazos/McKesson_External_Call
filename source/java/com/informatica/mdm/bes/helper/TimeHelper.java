package com.informatica.mdm.bes.helper;

import java.time.Duration;

import java.time.Instant;

import org.apache.log4j.Logger;

/**
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class TimeHelper {
	protected static Logger logger = Logger.getLogger(TimeHelper.class.getName());

	
	public static float getDuration (Instant start, String Description) {
		// DEPRECATED
//		final long ms;
//		final float sec;
//		start = (start != null) ? start : Instant.now();
//		Instant end = Instant.now();
//		ms = Duration.between(start, end).toMillis();
//		sec = ms / 1000.0f;
		
		if (start == null) {
			logger.warn("Start was sent as null.  Setting it to current time");
			start = Instant.now();
		}
		Duration dur = Duration.between(start, Instant.now());

		logger.info(Description + ": " + getMillisAsFormattedSeconds(dur.toMillis()) + " seconds");
		return dur.getSeconds();
	}
	
	public static String getMillisAsFormattedSeconds(long millis) {
        long secs = millis / 1000;
        long tenths = (millis - (secs * 1000)) / 100;
        long hundredths = (millis - (secs * 1000) - (tenths * 100)) / 10;

        return secs + "." + tenths + hundredths;
    }
}
