package com.informatica.mdm.bes.test_utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.sdo.SDOChangeSummary;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;

public class CreateSDOUtil {

	public static DataObject createSDO(SDOHelperContext helperContext) {
	    Type objectType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "object");


		DataObject inputSDO = helperContext.getDataFactory().create(objectType);
		SDOChangeSummary sdoChangeSummary = ((SDOChangeSummary)inputSDO.getChangeSummary());
		sdoChangeSummary.beginLogging();

		DataObject inputSDOBe = inputSDO.createDataObject("ExTradeSupplier");
		
		sdoChangeSummary.pauseLogging();
		
		return inputSDO;

	}
	
	
	public static List<DataObject> createChildListNoGrandChild(DataObject inputSDO, DataObject inputSDOBe, 
			HelperContext helperContext, int numChild, String childPagerName, 
			String childPagerItemName, boolean setSequences) {
		
		DataObject childPager = inputSDOBe.createDataObject(childPagerName);
		List<DataObject> childList = new ArrayList<DataObject>();
		inputSDOBe.setDataObject(childPagerName, childPager);
		
		for (int i = 1; i < numChild+1; i++) {
			DataObject childItem = childPager.createDataObject(childPagerItemName);
			if (setSequences) {
				childItem.setInt("seqNum", i);
			}
			childItem.setString("rowidObject", String.valueOf(i));
			childList.add(childItem);
			List<Property> properties = childItem.getInstanceProperties();
		}
		childPager.setList("item", childList);

		return inputSDOBe.getList(childPagerName + "/item");
	}
	
	
	public static DataObject createChildList(DataObject inputSDO, DataObject inputSDOBe, HelperContext helperContext, 
			int numChild, String childPagerName, String childPagerItemName, 
			String grandChildName, boolean setSequences, boolean createGreatGrandchild, 
			String greatGrandChildPagerName) {
		
		DataObject childPager = inputSDOBe.createDataObject(childPagerName);
		List<DataObject> childList = new ArrayList<DataObject>();
		inputSDOBe.setDataObject(childPagerName, childPager);
		
		for (int i = 1; i < numChild+1; i++) {
			DataObject childItem = childPager.createDataObject(childPagerItemName);
			DataObject grandChild = childItem.createDataObject(grandChildName);
			if (setSequences) {
				grandChild.setInt("seqNum", i);
			}
			childItem.setString("rowidObject", String.valueOf(i));
			grandChild.setString("rowidObject", String.valueOf(i));
			if (createGreatGrandchild) {
				// Create Great Grandchild
				DataObject greatGrandChildPager = grandChild.createDataObject(greatGrandChildPagerName);
				List<DataObject> greatGrandchildList = new ArrayList<DataObject>();
				grandChild.setDataObject(greatGrandChildPagerName, greatGrandChildPager);
			
			
				for (int grandChildIndex = 0; grandChildIndex < numChild; grandChildIndex++) {
					DataObject greatGrandchildItem = greatGrandChildPager.createDataObject(childPagerItemName);
					if (setSequences) {
						greatGrandchildItem.setInt("seqNum", i);
					}
					greatGrandchildItem.setString("rowidObject", String.valueOf(i));
				}
			}
			
		}
		return childPager;
	}
}
