package com.informatica.mdm.bes.automate;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.validate.StateLicensingValidate;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class SequenceNumberAutomate extends Automate {
	private static Logger logger = Logger.getLogger(SequenceNumberAutomate.class.getName());
	private static String SEQ_LENGTH = "5";

	@Override
	public List<ValidationError> doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO, CallContext callContext,
			CompositeServiceClient besClient) {
		SDOChangeSummary inputSdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		inputSdoChangeSummary.pauseLogging();

		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = null;
		if (promoteSDO != null)
			promoteSDOBe = promoteSDO.getDataObject(businessEntity);
		
		
		// Gather the Address, Location and Contact List from the inputSDO
		List<DataObject> inputAddressList = inputSDOBe.getList(BusinessEntityConstants.ADDRESS + "/item");
		List<DataObject> inputLocList = inputSDOBe.getList(BusinessEntityConstants.LOCATION + "/item");
		List<DataObject> inputContactList = inputSDOBe.getList(BusinessEntityConstants.CONTACT + "/item");
		List<DataObject> inputBankList = inputSDOBe.getList(BusinessEntityConstants.BANK + "/item");

		List<DataObject> promoteAddressList = null;
		List<DataObject> promoteLocList = null;
		List<DataObject> promoteContactList = null;
		List<DataObject> promoteBankList = null;
		// Gather the Address, Location, and Contact List from the promoteSDO
		if (promoteSDOBe != null) {
			promoteAddressList = promoteSDOBe.getList(BusinessEntityConstants.ADDRESS + "/item");
			promoteLocList = promoteSDOBe.getList(BusinessEntityConstants.LOCATION + "/item");
			promoteContactList = promoteSDOBe.getList(BusinessEntityConstants.CONTACT + "/item");
			promoteBankList = promoteSDOBe.getList(BusinessEntityConstants.BANK + "/item");
		}
		
		// Set Address Sequence Number
		try {
			setSequenceNumber(inputAddressList, promoteAddressList, BusinessEntityConstants.ADDRESS_POSTAL_ADDRESS, 
					inputSdoChangeSummary, BusinessEntityConstants.ADDRESS_SEQUENCE_NUMBER);
		} catch (Exception e) {
			logger.error("ERROR SETTING ADDRESS SEQUENCE NUMBER");
		}
		
		// Set Location Sequence Number
		try {
			setSequenceNumber(inputLocList, promoteLocList, BusinessEntityConstants.LOCATION_LOCATION, 
					inputSdoChangeSummary, BusinessEntityConstants.LOCATION_SEQUENCE_NUMBER);
		} catch (Exception e) {
			logger.error("ERROR SETTING LOCATION SEQUENCE NUMBER");
		}

		// Set Contact Sequence Number
		try {
			setSequenceNumber(inputContactList, promoteContactList, BusinessEntityConstants.CONTACT_CONTACT, 
					inputSdoChangeSummary, BusinessEntityConstants.CONTACT_SEQUENCE_NUMBER);
		} catch (Exception e) {
			logger.error("ERROR SETTING CONTACT SEQUENCE NUMBER");
		}
		
		// Set Supplier Bank Sequence Number
		try {
			setChildSequenceNumber(inputBankList, promoteBankList, 
					inputSdoChangeSummary, BusinessEntityConstants.BANK_SEQUENCE_NUMBER);
		} catch (Exception e) {
			logger.error("ERROR SETTING CONTACT SEQUENCE NUMBER");
		}
		
		// Set Supplier Location Bank Sequence Number
		try {
			setGreatGrandchildSequenceNumber(inputLocList, promoteLocList, BusinessEntityConstants.LOCATION_LOCATION, 
					inputSdoChangeSummary, BusinessEntityConstants.BANK_SEQUENCE_NUMBER, BusinessEntityConstants.LOCATION_LOCATION_BANK);
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			logger.error("ERROR SETTING LOCATION BANK SEQUENCE NUMBER");
		}

		return null;
	}
	
	
	/**
	 * This method sets the Sequence Number for the child list given by inputChildList
	 * 
	 * @param inputChildList - the Child list to set the seqNum
	 * @param promoteChildList - The promote children to reference
	 * @param grandChildName - The grandchild name of the child to set the Sequence Number
	 * @param inputSdoChangeSummary - the Change Summary to see which children were created
	 * @param childSeqNumFieldNm - The name of the Sequence Number field in the grandhild DataObject
	 */
	public void setChildSequenceNumber(List<DataObject> inputChildList, List<DataObject> promoteChildList,
			SDOChangeSummary inputSdoChangeSummary, String childSeqNumFieldNm) {
		// If input child list is null, then we cannot set any sequence numbers
		if (inputChildList == null || inputChildList.isEmpty())
			return;
		
		// Get the next highest sequence Number
		int nextSeqNum = determineNextHighestSequenceChild(promoteChildList, childSeqNumFieldNm);

		for (DataObject inputChild : inputChildList) {
			// The child may be null, if it is then skip 
			// ?? TODO: Do we actually want to skip here?
			if (inputChild == null)
				continue;

			// If the child is not created, then we do not want to set its sequence number as it has already been set.
			if (!inputSdoChangeSummary.isCreated(inputChild))
				continue;
			String seqNumberString = String.format("%1$" + SEQ_LENGTH + "s", nextSeqNum).replace(' ', '0');
			inputSdoChangeSummary.resumeLogging();
			inputChild.setString(childSeqNumFieldNm, seqNumberString);
			inputSdoChangeSummary.pauseLogging();
			nextSeqNum++;
		}
	}
	
	/**
	 * This method sets the Sequence Number for the child list given by inputChildList
	 * 
	 * @param inputChildList - the Child list to set the seqNum
	 * @param promoteChildList - The promote children to reference
	 * @param grandChildName - The grandchild name of the child to set the Sequence Number
	 * @param inputSdoChangeSummary - the Change Summary to see which children were created
	 * @param childSeqNumFieldNm - The name of the Sequence Number field in the grandhild DataObject
	 */
	public void setSequenceNumber(List<DataObject> inputChildList, List<DataObject> promoteChildList, String grandChildName,
			SDOChangeSummary inputSdoChangeSummary, String childSeqNumFieldNm) {
		// If input child list is null, then we cannot set any sequence numbers
		if (inputChildList == null || inputChildList.isEmpty())
			return;
		
		// Get the next highest sequence Number
		int nextSeqNum = determineNextHighestSequence(promoteChildList, childSeqNumFieldNm, grandChildName);
		
		for (DataObject inputChild : inputChildList) {
			// The child may be null, if it is then skip 
			// ?? TODO: Do we actually want to skip here?
			if (inputChild == null)
				continue;

			// If the child is not created, then we do not want to set its sequence number as it has already been set.
			if (!inputSdoChangeSummary.isCreated(inputChild))
				continue;
			
			// The sequence number lives in the grandchild data node
			DataObject grandChild = inputChild.getDataObject(grandChildName);
			
			// Format the sequence number as a 5 character string padded with 0's
			String seqNumberString = String.format("%1$" + SEQ_LENGTH + "s", nextSeqNum).replace(' ', '0');
			
			inputSdoChangeSummary.resumeLogging();
			grandChild.setString(childSeqNumFieldNm, seqNumberString);
			inputSdoChangeSummary.pauseLogging();
			nextSeqNum++;
		}
	}
	
	/**
	 * This method sets the Sequence Number for the child list given by inputChildList
	 * 
	 * @param inputChildList - the Child list to set the seqNum
	 * @param promoteChildList - The promote children to reference
	 * @param grandChildName - The grandchild name of the child to set the Sequence Number
	 * @param inputSdoChangeSummary - the Change Summary to see which children were created
	 * @param childSeqNumFieldNm - The name of the Sequence Number field in the grandhild DataObject
	 */
	public void setGreatGrandchildSequenceNumber(List<DataObject> inputChildList, List<DataObject> promoteChildList, 
			String grandChildName, SDOChangeSummary inputSdoChangeSummary, String greatGrandchildSeqNumFieldNm,
			String greatGrandChildListName) {
		// If input child list is null, then we cannot set any sequence numbers
		if (inputChildList == null || inputChildList.isEmpty())
			return;
		
		for (DataObject inputChild : inputChildList) {
			// The child may be null, if it is then skip 
			// ?? TODO: Do we actually want to skip here?
			if (inputChild == null)
				continue;
			String inputChildRowid = inputChild.getString(Constants.ROWID_OBJECT);
			DataObject grandChild = inputChild.getDataObject(grandChildName);
			List<DataObject> greatGrandchildList = grandChild.getList(greatGrandChildListName + "/item");
			List<DataObject> promoteGreatGrandchildList = null;
			DataObject promoteChild = null;
			DataObject promoteGrandchild = null;
			if (inputChildRowid != null && promoteChildList != null) {
				promoteChild = dataObjectHelperContext.getDataObjectSearcher().searchDataObjectList(promoteChildList, inputChildRowid);
				if (promoteChild != null) {
					promoteGrandchild = promoteChild.getDataObject(grandChildName);
					if (promoteGrandchild != null) {
						promoteGreatGrandchildList = promoteGrandchild.getList(greatGrandChildListName + "/item");
					}
				}
			}
			
			// Get the next highest sequence Number
			int nextSeqNum = determineNextHighestSequenceChild(promoteGreatGrandchildList, greatGrandchildSeqNumFieldNm);
						
			
			if (greatGrandchildList == null || greatGrandchildList.isEmpty())
				continue;
			
			for (DataObject inputGreatGrandchild : greatGrandchildList) {
				if (inputGreatGrandchild == null)
					continue;

				// If the child is not created, then we do not want to set its sequence number as it has already been set.
				if (!inputSdoChangeSummary.isCreated(inputGreatGrandchild))
					continue;
				// Format the sequence number as a 5 character string padded with 0's
				String seqNumberString = String.format("%1$" + SEQ_LENGTH + "s", nextSeqNum).replace(' ', '0');
				
				inputSdoChangeSummary.resumeLogging();
				inputGreatGrandchild.setString(greatGrandchildSeqNumFieldNm, seqNumberString);
				inputSdoChangeSummary.pauseLogging();
				nextSeqNum++;
			}
		}
	}
	
	/**
	 * This method determines what the next highest sequence number should be.
	 * 
	 * @param promoteChildList
	 * @param childSeqNumFieldNm
	 * @param grandChildName
	 * @return
	 */
	public int determineNextHighestSequenceChild(List<DataObject> promoteChildList, String childSeqNumFieldNm) {
		int highestSeqNum = 1;
		
		// If the record doesnt have any children already entered, then return 0.
		if (promoteChildList == null) {
			return highestSeqNum;
		}
		
		for (DataObject promoteChild : promoteChildList) {
			if (promoteChildList == null)
				continue;
			int seqNum = promoteChild.getInt(childSeqNumFieldNm);
			// If the current sequence number is greater than the highest found so far, then set the highestSeqNum
			if (seqNum > highestSeqNum) {
				highestSeqNum = seqNum;
			}
		}
		// Increase the sequence number to be the next highest.
		highestSeqNum++;
		return highestSeqNum;
	}
	
	/**
	 * This method determines what the next highest sequence number should be.
	 * 
	 * @param promoteChildList
	 * @param childSeqNumFieldNm
	 * @param grandChildName
	 * @return
	 */
	public int determineNextHighestSequence(List<DataObject> promoteChildList, String childSeqNumFieldNm, String grandChildName) {
		int highestSeqNum = 1;
		
		// If the record doesnt have any children already entered, then return 0.
		if (promoteChildList == null) {
			return highestSeqNum;
		}
		
		for (DataObject promoteChild : promoteChildList) {
			if (promoteChildList == null)
				continue;
			int seqNum = promoteChild.getInt(grandChildName + "/" + childSeqNumFieldNm);
			// If the current sequence number is greater than the highest found so far, then set the highestSeqNum
			if (seqNum > highestSeqNum) {
				highestSeqNum = seqNum;
			}
		}
		// Increase the sequence number to be the next highest.
		highestSeqNum++;
		return highestSeqNum;
	}
	

}
