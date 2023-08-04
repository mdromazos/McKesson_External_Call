package com.informatica.mdm.bes.automate;

import java.io.IOException;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.persistence.sdo.SDOChangeSummary;
import org.eclipse.persistence.sdo.SDODataObject;
import org.eclipse.persistence.sdo.SDOProperty;
import org.eclipse.persistence.sdo.SDOType;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.eclipse.persistence.sdo.helper.SDOTypeHelper;
import org.eclipse.persistence.sdo.helper.SDOXSDHelper;
import org.junit.Test;

import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.helper.ListHelper;
import com.informatica.mdm.bes.test_utils.FileUtil;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.Property;
import commonj.sdo.helper.XMLDocument;
import commonj.sdo.helper.HelperContext;

public class SequenceNumberAutomateTest {
	DataObjectHelperContext dataObjectHelperContext = new DataObjectHelperContext();

	@Test
	public void Test_Split() throws UnsupportedEncodingException, IOException, URISyntaxException {
		String str = "ExTradeSupplier.TaxInformation.taxNum";
		System.out.println(str.split("\\.", 2)[0]);
		System.out.println(str.split("\\.", 2)[1]);
	}
	
	@Test
	public void WhenCreateAddress_ThenSetSequenceNumber() throws UnsupportedEncodingException, IOException, URISyntaxException {
		SequenceNumberAutomate sequenceNumberAutomate = new SequenceNumberAutomate();
		sequenceNumberAutomate.setDataObjectHelperContext(new DataObjectHelperContext());
		DataObjectHelperContext dataObjectHelperContext = new DataObjectHelperContext();
		SDOHelperContext helperContext = new SDOHelperContext();
		SDOTypeHelper typeHelper = (SDOTypeHelper) helperContext.getTypeHelper();
		SDOXSDHelper xsdHelper = (SDOXSDHelper) helperContext.getXSDHelper();
		InputStream inputStream = FileUtil.loadFileIS("xsd/tradeSupplier.xsd");
	    xsdHelper.define(inputStream, "urn:bes-external-call.informatica.mdm");
	    inputStream.close();

	    Type objectType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "object");

		Type tradeSupplierType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier");
		Type addressPagerType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Address.Pager");
		Type contactPagerType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Contact.Pager");
		Type locationPagerType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Location.Pager");

		Type postalAddressType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Address.PostalAddress");

		Type contactContactType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Contact.contact");
		Type locationSupplierLocationType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Location.SupplierLocation");


		
//		DataObject inputSDO = helperContext.getDataFactory().create(objectType);
		DataObject inputSDO = createSDO(helperContext, 2, 1, 1, false);
		DataObject promoteSDO = createSDO(helperContext, 2, 1, 1, true);
		SDOChangeSummary sdoChangeSummary = ((SDOChangeSummary)inputSDO.getChangeSummary());
//		inputSDO.getDataObject("ExTradeSupplier").setString("fullNm", "TEST");
		
//		System.out.println("testtest".split("/").length);
		
		
		boolean isDataType = inputSDO.getInstanceProperty("ExTradeSupplier").getType().isDataType();
		boolean isAbstract = inputSDO.getInstanceProperty("ExTradeSupplier").getType().isAbstract();
//		System.out.println("IS CONTAINTMENT: " + inputSDO.getInstanceProperty("ExTradeSupplier").isContainment());
//		System.out.println("IS CONTAINTMENT: " + inputSDO.getDataObject("ExTradeSupplier").getInstanceProperty("fullNm").isContainment());
//		System.out.println("IS CONTAINTMENT: " + inputSDO.getDataObject("ExTradeSupplier").getInstanceProperty("Address").isContainment());
		
		DataObject address1 = (DataObject) inputSDO.getDataObject("ExTradeSupplier").getList("Address/item").get(0);
		System.out.println("CONTAINTMENT: " + address1.getContainmentProperty());

//		System.out.println("IS OPEN: " + inputSDO.getProperty("ExTradeSupplier").getType().isOpen());
//		System.out.println(inputSDO.getProperty("ExTradeSupplier").getType().getProperties().stream().map(p -> { return ((Property)p).getName(); } ).collect(Collectors.toList()));
//		System.out.println("IS DATA TYPE: " + isDataType);
//		System.out.println("IS ABSTRACT: " + isAbstract);

//		DataObject inputSDOBe = helperContext.getDataFactory().create(tradeSupplierType);
//		DataObject inputSDOBe = inputSDO.createDataObject("ExTradeSupplier");
//		System.out.println(sdoChangeSummary.isCreated(inputSDOBe));
//		System.out.println("HERE3");
		
//		System.out.println("BEFORE - PROMOTE SDO");
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, "ExTradeSupplier", promoteSDO.getDataObject("ExTradeSupplier"));
//		System.out.println("BEFORE - INPUT SDO");
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, "ExTradeSupplier", inputSDO.getDataObject("ExTradeSupplier"));
//
//		
//		sequenceNumberAutomate.doAutomate(inputSDO, helperContext, null, null, "ExTradeSupplier", promoteSDO, null, null);
//
//		
//		System.out.println("AFTER - PROMOTE SDO");
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, "ExTradeSupplier", promoteSDO.getDataObject("ExTradeSupplier"));
//		System.out.println("AFTER -INPUT SDO");
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, "ExTradeSupplier", inputSDO.getDataObject("ExTradeSupplier"));

//		System.out.println("HERE4");
//		DataObject addr = inputSDO.getDataObject("ExTradeSupplier/Address/item");
//		List<DataObject> postalAddrList = inputSDO.getList("ExTradeSupplier/Address/item");
//		DataObject postalAddr = postalAddrList.get(0);
//		postalAddr.setInt("seqNum", 1);
//		postalAddr.setString("city", "TEST CITY");
//		postalAddr.setString("addrLn1", "TEST ADDR LINE 1");
//		System.out.println("PATH: " + postalAddr.getDataObject("PostalAddress").getType().getName());
//		postalAddr.getType().getProperties();
//		List<Property> properties = postalAddr.getInstanceProperties();
//		List<Property> properties = postalAddr.getType().getProperties();
//		for (Property property : properties) {
//			System.out.println("PROPERTY: " + property.getName());
//		}
		
//		inputSDO.getDataObject("ExTradeSupplier").getContainmentProperty().getType().
//		System.out.println("CONTAINMENT PROPERTY: " + inputSDO.getDataObject("ExTradeSupplier").getContainmentProperty().getName());
		
		
		
//		SDOChangeSummary sdoChangeSummary = null;
//		Map<String, List<FieldRule>> ccFieldRules = createCompanyCodeFieldRules();
//		Map<String, List<FieldRule>> purchaseOrgFieldRules = createPurchaseOrgRules();
		
//		businessRulesValidate.processBusinessRules(inputSDOBe, promoteSDOBe, ccFieldRules, purchaseOrgFieldRules, sdoChangeSummary);
		
	}
	
	public DataObject createSDO(SDOHelperContext helperContext, int numAddress, int numContact, int numLocation, boolean setSequenceNumber) {
	    Type objectType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "object");

		Type tradeSupplierType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier");
		Type addressPagerType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Address.Pager");
		Type contactPagerType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Contact.Pager");
		Type locationPagerType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Location.Pager");
		Type bankPagerType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.BankDetails.Pager");

		Type addressType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Address");
		
		Type postalAddressType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Address.PostalAddress");
		Type contactContactType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Contact.contact");
		Type locationSupplierLocationType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "ExTradeSupplier.Location.SupplierLocation");

		DataObject inputSDO = helperContext.getDataFactory().create(objectType);
		SDOChangeSummary sdoChangeSummary = ((SDOChangeSummary)inputSDO.getChangeSummary());
		sdoChangeSummary.beginLogging();
//		((SDOChangeSummary)inputSDO.getChangeSummary()).beginLogging();

		DataObject inputSDOBe = inputSDO.createDataObject("ExTradeSupplier");
		System.out.println(sdoChangeSummary.isCreated(inputSDOBe));

//		DataObject addressPager = createChildList(inputSDOBe, numAddress, "Address", helperContext, postalAddressType, setSequenceNumber, addressPagerType);
//		DataObject contactPager = createChildList(inputSDOBe, numContact, "Contact", helperContext, contactContactType, setSequenceNumber, contactPagerType);
//		DataObject locationPager = createChildList(inputSDOBe, numLocation, "Location", helperContext, locationSupplierLocationType, setSequenceNumber, locationPagerType);

//		DataObject addressPager = createChildList(inputSDO, inputSDOBe, numAddress, 
//				"Address", "PostalAddress", helperContext, 
//				addressType, setSequenceNumber, addressPagerType, postalAddressType);
		
		DataObject addressPager = createChildList(inputSDO, inputSDOBe, helperContext, 
				numAddress, "Address", "item", 
				"PostalAddress", setSequenceNumber, false, 
				null);
		DataObject contactPager = createChildList(inputSDO, inputSDOBe, helperContext, numContact,
				"Contacts", "item", "contacts", setSequenceNumber, false, null);
		DataObject locationPager = createChildList(inputSDO, inputSDOBe, helperContext, 
				numLocation, "SupplierLocation", "item", 
				"Location", setSequenceNumber, true,
				"LocationBank");
//		DataObject contactPager = createChildList(inputSDOBe, numContact, "Contact", "contact", helperContext, contactContactType, setSequenceNumber, contactPagerType);
//		DataObject locationPager = createChildList(inputSDOBe, numLocation, "Location", "SupplierLocation", helperContext, locationSupplierLocationType, setSequenceNumber, locationPagerType);
		DataObject bankPager = createChildListNoGrandChild(inputSDO, inputSDOBe, helperContext, numAddress,
				"BankDetails", "item", setSequenceNumber);
		
		
//		inputSDO.setDataObject("ExTradeSupplier", inputSDOBe);
//		inputSDOBe.setDataObject("Address", addressPager);
//		inputSDOBe.setDataObject("Contact", contactPager);
//		inputSDOBe.setDataObject("Location", locationPager);
		return inputSDO;
	}
	
	public DataObject createChildListNoGrandChild(DataObject inputSDO, DataObject inputSDOBe, 
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
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, "ExTradeSupplier", inputSDO.getDataObject("ExTradeSupplier"));

		return childPager;
	}
	
	
	public DataObject createChildList(DataObject inputSDO, DataObject inputSDOBe, HelperContext helperContext, 
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
	
	@Test
	public void testPadding() {
		String length = "5";
		int inputString = 5;
		String returnValue = String.format("%1$" + length + "s", inputString).replace(' ', '0');
		System.out.println(returnValue);
	}
	
//	List<DataObject> listCombined = new ArrayList();
//    
//	if (ListHelper.listExists(listSDO) && !ListHelper.listExists(listORS)) {
//		logger.debug("Returning listSDO ");
//		return listSDO;
//	}
//	
//	if (!ListHelper.listExists(listSDO) && ListHelper.listExists(listORS)) {
//		Object itemPager[] = new DataObject[] {};
//
//	        for (DataObject dataObject: listORS) {
//	        	listCombined.add(dataObject);
//	        	itemPager = appendObjectArray(itemPager, dataObject);
//	        }
//	        parentSdo.createDataObject(objectListName);
//	        parentSdo.setList(objectListName + "/item", listCombined);
	
//    public Object[] appendObjectArray(Object[] obj, Object newObj) {
//		ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(obj));
//		temp.add(newObj);
//		return (Object[]) temp.toArray();
//    }
}
