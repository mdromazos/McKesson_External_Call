package com.informatica.mdm.bes.config;

public class ErrorConstants {
	/*
	 * PeopleSoftIDRequired 
	 */
	public static final String PEOPLESOFT_ID_REQUIRED_ERROR_CODE = "CUSTOM-NEWVENDOR01";
	public static final String PEOPLESOFT_ID_REQUIRED_ERROR_MESSAGE = "CUSTOM-NEWVENDOR01 : The W9 Document has exceeded 3 years, Please upload a recent, valid document";
	
	public static final String CANT_COMMUNICATE_WITH_MDM_ERROR_CODE = "CUSTOM-INT01";
	public static final String CANT_COMMUNICATE_WITH_MDM_ERROR_MESSAGE = "CUSTOM-INT01 : The External Calls were unable to communicate with the MDM Server";
	
	public static final String GENERAL_ERROR_CODE = "CUSTOM-INT02";
	public static final String GENERAL_ERROR_MESSAGE = "CUSTOM-INT02 : The External Calls had an error. Please try again or contact the System Administrator.";
	
	
}

