package com.informatica.mdm.bes.domain;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.LookupConstants;

import commonj.sdo.DataObject;

import com.informatica.mdm.bes.config.BusinessEntityConstants;

/**
 * This is a utility class used to map fields from a source to a target.  It is used by copy automations to group together a list of fields that need to by copied.
 * For example the CopyAddressAutomate uses this to map fields from VN to PI and VN to OA.
 * It contains properties that tell whether or not the field is a lookup and what the lookup code is for that field.
 * It also contains a property for the Trigger Field Name and what the Trigger value should be to detect whether or not that field should be populated
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class FieldMapping {
	public String sourceName;
	public String sourceCleansedName;
	public String targetName;
	public String targetCleansedName;
	public boolean lookup;
	public String lookupCd;
	public String targetValue;
	public String triggerName;
	public Object triggerValue;
	
	public FieldMapping(String sourceName, String targetName, boolean lookup) {
		this.sourceName = sourceName;
		this.targetName = targetName;
		this.lookup = lookup;
	}
	
	public FieldMapping(String sourceName, String targetName, boolean lookup, String lookupCd) {
		this.sourceName = sourceName;
		this.targetName = targetName;
		this.lookup = lookup;
		this.lookupCd = lookupCd;
	}
	
	public FieldMapping(String sourceName, String targetName, boolean lookup, String lookupCd, String triggerName, String triggerValue) {
		this.sourceName = sourceName;
		this.targetName = targetName;
		this.lookup = lookup;
		this.lookupCd = lookupCd;
		this.triggerName = triggerName;
		this.triggerValue = triggerValue;
	}
	
	public FieldMapping(String sourceName, String sourceCleansedName, String targetName, String targetCleansedName, boolean lookup, String lookupCd) {
		this.sourceName = sourceName;
		this.sourceCleansedName = sourceCleansedName;
		this.targetName = targetName;
		this.targetCleansedName = targetCleansedName;
		this.lookup = lookup;
		this.lookupCd = lookupCd;
	}
	
	public FieldMapping(String sourceName, String targetName, String targetValue, boolean lookup, String lookupCd) {
		this.sourceName = sourceName;
		this.targetName = targetName;
		this.targetValue = targetValue;
		this.lookup = lookup;
		this.lookupCd = lookupCd;
	}
}
