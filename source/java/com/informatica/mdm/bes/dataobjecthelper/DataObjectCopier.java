package com.informatica.mdm.bes.dataobjecthelper;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.domain.FieldMapping;
import com.informatica.mdm.bes.helper.VendorSDOHelper;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class DataObjectCopier {
	private static Logger logger = Logger.getLogger(DataObjectCombiner.class.getClass());
	DataObjectFieldHelper dataObjectFieldHelper;
	
	
	public DataObjectCopier(DataObjectFieldHelper dataObjectFieldHelper) {
		this.dataObjectFieldHelper = dataObjectFieldHelper;
	}

	/**
	 * This method takes in one FieldMapping and uses it to copy data from the source parents to the sdotoCopyTo
	 * @param inputParent
	 * @param promoteParent
	 * @param orsParent
	 * @param sdoToCopyTo
	 * @param fieldMapping
	 * @param inputSdoChangeSummary
	 * @param helperContext
	 */
	public void copy(DataObject inputParent, DataObject promoteParent, DataObject orsParent, 
			DataObject sdoToCopyTo, FieldMapping fieldMapping, SDOChangeSummary inputSdoChangeSummary, 
			HelperContext helperContext, VendorSDOHelper vendorSDOHelper, boolean overwriteExistingValues) {
		if (inputParent == null || orsParent == null || fieldMapping == null)
			throw new NullPointerException("");
		
		SDOChangeSummary sdoChangeSummary = inputSdoChangeSummary != null ? inputSdoChangeSummary : (SDOChangeSummary) inputParent.getRootObject().getChangeSummary();
		
		Object sourceValue;
		if (fieldMapping.lookup) {
			// If the value is there from the inputSDO, use that, if not use the promote or ors value
			sourceValue = helperContext.getCopyHelper().copy(dataObjectFieldHelper.getDataObject(inputParent, promoteParent, orsParent, fieldMapping.sourceName));
		} else {
			sourceValue = dataObjectFieldHelper.get(inputParent, promoteParent, orsParent, fieldMapping.sourceName);
		}
		if (sourceValue != null) {
			if (overwriteExistingValues) {
				vendorSDOHelper.set(sdoToCopyTo, fieldMapping.targetName, sourceValue, sdoChangeSummary);
			} else {
				boolean didTargetFieldChangeToNull = vendorSDOHelper.didFieldChangeToNull(sdoToCopyTo, inputSdoChangeSummary, fieldMapping.targetName);
//				Object targetValue = vendorSDOHelper.get(sdoToCopyTo, promoteParent,  orsParent, fieldMapping.targetName);
				Object targetValue = sdoToCopyTo.get(fieldMapping.targetName);
				if (targetValue == null || didTargetFieldChangeToNull) {
					vendorSDOHelper.set(sdoToCopyTo, fieldMapping.targetName, sourceValue, sdoChangeSummary);
				}
			}
			
		}
	}
	
	/**
	 * This method takes in a set of fieldMapping's and data objects and uses them to copy data from the source parents to the sdoToCopyTo
	 * @param inputParent
	 * @param promoteParent
	 * @param orsParent
	 * @param sdoToCopyTo
	 * @param fieldMappings
	 * @param helperContext
	 */
	public synchronized void copyAll(DataObject inputParent, DataObject promoteParent, DataObject orsParent, 
			DataObject sdoToCopyTo, FieldMapping[] fieldMappings, HelperContext helperContext, VendorSDOHelper vendorSDOHelper, boolean overwriteExistingValues) {
		if (inputParent == null || orsParent == null || fieldMappings == null)
			throw new NullPointerException("");
		
		SDOChangeSummary inputSdoChangeSummary = (SDOChangeSummary) inputParent.getRootObject().getChangeSummary();
		
		// Loop through each one of the fields to copy and set the child values
		for (FieldMapping fieldMapping : fieldMappings) {
			copy(inputParent, promoteParent, orsParent, sdoToCopyTo, fieldMapping, inputSdoChangeSummary, helperContext, vendorSDOHelper, overwriteExistingValues);
		}
	}
}
