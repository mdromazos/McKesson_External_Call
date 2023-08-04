package com.informatica.mdm.bes.validate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * If supplier Product Category falls under type Narcotics or Controlled Substances then make sure the supplier has an Electronic Address
 * of type ESIG.  If not throw an Error.
 * 
 * @author Matthew Dromazos
 *
 */
public class ESIGEmailValidate extends Validate {
	
	public static final String ITEM_TYPE_ONE_RX = "RX";
	public static final String ITEM_TYPE_THR_CLASS_2 = "CLASS II";
	public static final String ITEM_TYPE_THR_CLASS_3_5 = "CLASS III - V";
	public static final String ETRNC_ADDR_TYP_ESIG = "ESIG";
	
	public static final String[] ASSOCIATED_BUS = {"WELLCA","RETAIL","PHARMA","MSD","TECH SOLUTIONS"};
	public static final Map<String, Boolean> BU_MAP = Stream.of(
			  new AbstractMap.SimpleEntry<>("WELLCA", true), 
			  new AbstractMap.SimpleEntry<>("RETAIL", true),
			  new AbstractMap.SimpleEntry<>("PHARMA", true),
			  new AbstractMap.SimpleEntry<>("TECH SOLUTIONS", true),
			  new AbstractMap.SimpleEntry<>("MSD", true))
			  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);
		
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = null;
		if (promotePreviewSDO != null)
			promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
		
		// Determine if the vendor sells Narcotics
		if (!isNarcoticsSupplier(inputSDOBe, promoteSDOBe))
			return null;
		
		

		return validateHasESIGEmail(inputSDOBe, promoteSDOBe, inputSDO, helperContext, businessEntity);
	}
	
	/**
	 * Determines if the supplier sells Narcotics based on its Item Types
	 * @param inputSDOBe
	 * @param promoteSDOBe
	 * @return
	 */
	public boolean isNarcoticsSupplier(DataObject inputSDOBe, DataObject promoteSDOBe) {
		String vndrItmTyp1 = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, BusinessEntityConstants.VENDOR_ITEM_TYPE_1);
		String vndrItmTyp3 = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, BusinessEntityConstants.VENDOR_ITEM_TYPE_3);
		String csosCntrlSysOrdr = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, BusinessEntityConstants.CSOS_CNTRL_SYS_ORDR);
		
		if (csosCntrlSysOrdr != null && csosCntrlSysOrdr.equals("Y") && 
				vndrItmTyp1 != null && vndrItmTyp1.equals(ITEM_TYPE_ONE_RX) &&
				vndrItmTyp3 != null && vndrItmTyp3.equals(ITEM_TYPE_THR_CLASS_2))
			return true;
		else if (vndrItmTyp1 != null && vndrItmTyp1.equals(ITEM_TYPE_ONE_RX) &&
				vndrItmTyp3 != null && vndrItmTyp1.equals(ITEM_TYPE_THR_CLASS_3_5))
			return true;
		
		return false;
	}
	
	public List<ValidationError> validateHasESIGEmail(DataObject inputSDOBe, DataObject promoteSDOBe, 
			DataObject inputSDO, HelperContext helperContext, String businessEntity) {
		List<DataObject> inputPromoteEtrncAddrList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.ELECTRONIC_ADDR, inputSDO, helperContext);
		if (inputPromoteEtrncAddrList == null || inputPromoteEtrncAddrList.isEmpty())
			return null;
		
		boolean hasESIG = hasESIGEmail(inputSDOBe, promoteSDOBe, inputSDO, helperContext);
		if (!hasESIG) {
			return createErrors(ErrorConstants.NARCOTICS_MUST_HAVE_ESIG_ERROR_CODE, 
					ErrorConstants.NARCOTICS_MUST_HAVE_ESIG_ERROR_MESSAGE, 
					businessEntity + "." + VendorMainConstants.ELECTRONIC_ADDR, helperContext.getDataFactory());
		}
		return null;
	}
	
	
	/**
	 * Returns true if any Electronic Address's have an Electronic Address Type of "ESIG"
	 * @param inputSDOBe
	 * @param promoteSDOBe
	 * @param inputSDO
	 * @param helperContext
	 * @return
	 */
	public boolean hasESIGEmail(DataObject inputSDOBe, DataObject promoteSDOBe, DataObject inputSDO, HelperContext helperContext) {

		List<DataObject> inputPromoteEtrncAddrList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.ELECTRONIC_ADDR, inputSDO, helperContext);
		List<DataObject> promoteEtrncAddrList = promoteSDOBe != null ? 
				promoteSDOBe.getList(BusinessEntityConstants.ELECTRONIC_ADDR + "/item") : null;
		
		// If no Electronic Address exist, return false.
		if (inputPromoteEtrncAddrList == null || inputPromoteEtrncAddrList.isEmpty())
			return false;
		
		// find the first occurrence of an email with a type of "ESIG"
		return inputPromoteEtrncAddrList.parallelStream().anyMatch((inputPromoteEtrncAddr) -> {
			if (inputPromoteEtrncAddr != null) {
				String inputPromoteEtrncAddrRowid = inputPromoteEtrncAddr
						.getString(BusinessEntityConstants.ROWID_OBJECT);
				
				DataObject promoteEtrncAddr = null;
				if (inputPromoteEtrncAddrRowid != null && promoteEtrncAddrList != null)
					promoteEtrncAddr = dataObjectHelperContext.getDataObjectSearcher()
							.searchDataObjectList(promoteEtrncAddrList, inputPromoteEtrncAddrRowid);
				String etrncAddrTyp = vendorSDOHelper.getString(inputPromoteEtrncAddr, promoteEtrncAddr,
						VendorMainConstants.ELECTRONIC_ADDR_ETRNC_TYP);
				logger.info("etrnc Id Type : " + etrncAddrTyp);
				if (etrncAddrTyp != null && etrncAddrTyp.equals(ETRNC_ADDR_TYP_ESIG))
					return true;
			}
			return false;
		});		
	}
}
