package com.informatica.mdm.bes.automate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.service.BusinessEntityServiceClient;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class ProfileAutomate extends Automate {
	private static Logger logger = Logger.getLogger(ProfileAutomate.class.getName());

	
	public static final String MAINTENANCE_LEVEL = "mntnceLvl";
	public static final String VENDOR_PROFILE_CO_CD = "cmpnyCd/coCd";
	public static final String VENDOR_PROFILE_PORG = "prchOrg/prchOrgCd";
	public static final String VENDOR_PROFILE_BSNS_UNIT = "bsnsUnit/bsnsUnitCd";
	public static final String VENDOR_PROFILE_SUB_TEAM = "subTeam/subTeamCd";
	public static final String VENDOR_PROFILE_CO_CD_PYMNT_TRMS = "pymntTrms/pymntTrmsCd";
	public static final String VENDOR_PROFILE_CO_CD_CRDT_MEMO_PYMNT_TRMS = "creditMemoPymntTrm/pymntTrmsCd";
	public static final String VENDOR_PROFILE_CO_CD_CO_CD_CNTRY = "cmpnyCdCntry/countryCode";
	public static final String VENDOR_PROFILE_CO_CD_PYMNT_MTHD = "pymntMthd/ExPymntMthdCd";

	public static final String BE_VENDOR_MAINTENANCE_PROFILE = "ExVendorMaintenanceProfile";
	public static final String BE_VNDR_MNT_PRFL_ATTR_ = "vndrMntncPrfl";
	
	public static final String CC_RULE = "CompanyCodeRule";
	public static final String PORG_RULE = "PurchaseOrgRule";
	
	private BusinessEntityServiceClient businessEntityServiceClient;
	
	public ProfileAutomate() {
		businessEntityServiceClient = new BusinessEntityServiceClient();
	}

	@Override
	public List<ValidationError> doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO, CallContext callContext,
			CompositeServiceClient besClient) {
		
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		SDOChangeSummary sdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		sdoChangeSummary.pauseLogging();
		
		String profileRowid = null;
		profileRowid = inputSDOBe.getString(BE_VNDR_MNT_PRFL_ATTR_ + "/" + Constants.ROWID_OBJECT);
		
		if (profileRowid == null)
			return null;
		
		DataObject vendorProfile = getVendorTypeMaintenance(profileRowid, callContext, besClient, helperContext, businessEntity);

		List<DataObject> ccVendorProfiles = vendorProfile.getList(CC_RULE + "/item");
		//vendorProfiles.stream().filter(vendorProfile -> vendorProfile.getString(MAINTENANCE_LEVEL).equals("2")).collect(Collectors.toList());
		List<DataObject> porgVendorProfiles = vendorProfile.getList(PORG_RULE + "/item");
		//vendorProfiles.stream().filter(vendorProfile -> vendorProfile.getString(MAINTENANCE_LEVEL).equals("1")).collect(Collectors.toList());
		
		
		
		addVendorProfiles(ccVendorProfiles, inputSDOBe, BusinessEntityConstants.COMPANY_CODE, 
				sdoChangeSummary, BusinessEntityConstants.COMPANY_CODE_BSNS_UNIT, BusinessEntityConstants.COMPANY_CODE_BSNS_UNIT_CD,
				BusinessEntityConstants.COMPANY_CODE_COMPANY_CODE, BusinessEntityConstants.COMPANY_CODE_COMPANY_CODE_CD, VENDOR_PROFILE_CO_CD, 
					true, BusinessEntityConstants.COMPANY_CODE_SUB_TEAM, BusinessEntityConstants.COMPANY_CODE_SUB_TEAM_CD);
		
		addVendorProfiles(porgVendorProfiles, inputSDOBe, BusinessEntityConstants.PURCHASE_ORG, 
				sdoChangeSummary, BusinessEntityConstants.PURCHASE_ORG_BSNS_UNIT, BusinessEntityConstants.PURCHASE_ORG_BSNS_UNIT_CD,
				BusinessEntityConstants.PURCHASE_ORG_PORG, BusinessEntityConstants.PURCHASE_ORG_PORG_CD, VENDOR_PROFILE_PORG,
				false, null, null);
		
		return null;
	}
	
	public void addVendorProfiles(List<DataObject> vendorProfiles, DataObject inputSDOBe, String childName, 
			SDOChangeSummary sdoChangeSummary, String bsnsUnitLkpNm, String bsnsUnitLkpFldNm,
			String mntncLkpNm, String mntncLkpFldNm, String mntncLvLValFldNm, 
			boolean setSubTeam, String subTeamLkpNm, String subTeamValNm) {
		
		try {
			sdoChangeSummary.resumeLogging();
			List<DataObject> childList = inputSDOBe.getList(childName + "/item");
			
			List<String> childMaintenanceValues = new ArrayList<String>();
			
			DataObject childPager = inputSDOBe.getDataObject(childName);
			String childPagerName = childName;
			String childPagerItemName = "item";
			if (childPager == null) {
				childPager = inputSDOBe.createDataObject(childPagerName);
			} else {
				childMaintenanceValues = childList.stream().map(child -> {return child.getString(mntncLkpNm + "/" + mntncLkpFldNm);}).collect(Collectors.toList());
			}
			
			
			for (DataObject vendorProfile : vendorProfiles) {
				String mntncLvlVal = vendorProfile.getString(mntncLvLValFldNm);
				String bsnsUnitVal = vendorProfile.getString(VENDOR_PROFILE_BSNS_UNIT);
				String subTeamVal = null;
				String pymntTrmsVal = null;
				String crdtMemoPymntTrmsVal = null;
				String cmpnyCdCntryVal = null;
				String pymntMthdVal = null;	
				if (setSubTeam) {
					logger.info("SET SUB TEAM TRUE");
					subTeamVal = vendorProfile.getString(VENDOR_PROFILE_SUB_TEAM);
					pymntTrmsVal = vendorProfile.getString(VENDOR_PROFILE_CO_CD_PYMNT_TRMS);
					crdtMemoPymntTrmsVal = vendorProfile.getString(VENDOR_PROFILE_CO_CD_CRDT_MEMO_PYMNT_TRMS);
					cmpnyCdCntryVal = vendorProfile.getString(VENDOR_PROFILE_CO_CD_CO_CD_CNTRY);
					pymntMthdVal = vendorProfile.getString(VENDOR_PROFILE_CO_CD_PYMNT_MTHD);	
					logger.info("PAYMENT TERMS VAL: " + pymntTrmsVal);
					logger.info("CREDIT MEMO PAYMENT TERMS VAL: " + crdtMemoPymntTrmsVal);
					logger.info("Company Code COUNTRY VAL: " + cmpnyCdCntryVal);
					logger.info("PAYMENT METHOD VALUE: " + pymntMthdVal);
				}

				if (childMaintenanceValues.contains(mntncLvlVal)) {
					continue;
				}
				
				DataObject childItem = childPager.createDataObject(childPagerItemName);
				
				setupChild(childItem, mntncLvlVal, bsnsUnitLkpNm, bsnsUnitLkpFldNm,
						bsnsUnitVal, mntncLkpNm, mntncLkpFldNm,
						setSubTeam, subTeamLkpNm, subTeamValNm, subTeamVal, 
						pymntTrmsVal, crdtMemoPymntTrmsVal, cmpnyCdCntryVal, pymntMthdVal);
			}
			sdoChangeSummary.pauseLogging();	
		} catch (Exception e) {
			logger.info("ERROR ADDING PROFILES: " + e.getMessage());
			sdoChangeSummary.pauseLogging();
		}

	}
	
//	private DataObject setupChildNew(DataObject child, String childName, DataObject vendorProfile, boolean setSubTeam) {
//		String bsnsUnitLkpNm = null;
//		String bsnsUnitLkpFldNm = null;
//		String mntncLkpNm = null;
//		String mntncLkpFldNm = null;
//		String subTeamLkpFieldNm = BusinessEntityConstants.COMPANY_CODE_SUB_TEAM;
//		
//
//		String subTeamValNm = BusinessEntityConstants.COMPANY_CODE_SUB_TEAM_CD;
//
//		if(childName.equals(BusinessEntityConstants.COMPANY_CODE)) {
//			bsnsUnitLkpNm = BusinessEntityConstants.COMPANY_CODE_BSNS_UNIT;
//			bsnsUnitLkpFldNm = BusinessEntityConstants.COMPANY_CODE_BSNS_UNIT_CD;
//			mntncLkpNm = BusinessEntityConstants.COMPANY_CODE_COMPANY_CODE;
//
//		} else if (childName.equals(BusinessEntityConstants.PURCHASE_ORG)) {
//			bsnsUnitLkpNm = BusinessEntityConstants.PURCHASE_ORG_BSNS_UNIT;
//			bsnsUnitLkpFldNm = BusinessEntityConstants.PURCHASE_ORG_BSNS_UNIT;
//			mntncLkpNm = BusinessEntityConstants.PURCHASE_ORG_PORG;
//		}
//		
//		String mntncLvlVal = vendorProfile.getString(mntncLkpFldNm);
//		String bsnsUnitVal = vendorProfile.getString(VENDOR_PROFILE_BSNS_UNIT);
//		String subTeamVal = vendorProfile.getString(VENDOR_PROFILE_SUB_TEAM);
//		
//		DataObject lkpMntncLvlDataObject = child.createDataObject(mntncLkpNm);
//
//		if (lkpMntncLvlDataObject == null)
//			return null;
//		
//		lkpMntncLvlDataObject.set(mntncLkpFldNm, mntncLvlVal);
//		
//		DataObject lkpBsnsUnit = child.createDataObject(bsnsUnitLkpNm);
//		if (lkpBsnsUnit == null)
//			return null;
//		lkpBsnsUnit.set(bsnsUnitLkpFldNm, bsnsUnitVal);
//		
//		if (setSubTeam && subTeamVal != null) {
//			DataObject lkpSubTeam = child.createDataObject(subTeamLkpFieldNm);
//			if (lkpSubTeam == null)
//				return null;
//			lkpSubTeam.set(subTeamValNm, subTeamVal);
//		}
//		
//		return child;
//	}
	
	private DataObject setupChild(DataObject child, String mntncLvlVal, String bsnsUnitLkpNm, String bsnsUnitLkpFldNm,
			String bsnsUnitVal, String mntncLkpNm, String mntncLkpFldNm,
			boolean setSubTeam, String subTeamLkpFieldNm, String subTeamValNm, String subTeamVal,
			String pymntTrmsVal, String crdtMemoPymntTrmsVal, String cmpnyCdCntryVal,
			String pymntMthdVal) {
		DataObject lkpMntncLvlDataObject = child.createDataObject(mntncLkpNm);

		if (lkpMntncLvlDataObject == null)
			return null;
		
		lkpMntncLvlDataObject.set(mntncLkpFldNm, mntncLvlVal);
		
		DataObject lkpBsnsUnit = child.createDataObject(bsnsUnitLkpNm);
		if (lkpBsnsUnit == null)
			return null;
		lkpBsnsUnit.set(bsnsUnitLkpFldNm, bsnsUnitVal);
		
		if (setSubTeam && subTeamVal != null) {
			//Set Sub Team
			DataObject lkpSubTeam = child.createDataObject(subTeamLkpFieldNm);
			if (lkpSubTeam == null)
				return null;
			lkpSubTeam.set(subTeamValNm, subTeamVal);
			
		}
		
		if (setSubTeam && pymntTrmsVal != null) {
			//Set Payment Terms
			DataObject lkpPymntTrms = child.createDataObject(BusinessEntityConstants.COMPANY_CODE_PAYMENT_TERMS_LKP);
			if (lkpPymntTrms == null)
				return null;
			lkpPymntTrms.set(BusinessEntityConstants.COMPANY_CODE_PAYMENT_TERMS_CD, pymntTrmsVal);
		}
		
		if (setSubTeam && crdtMemoPymntTrmsVal != null) {
			//Set Credit Memo Payment Terms
			DataObject lkpCrdtMemoPymntTrms = child.createDataObject(BusinessEntityConstants.COMPANY_CODE_CREDIT_MEMO_PAYMENT_TERMS_LKP);
			if (lkpCrdtMemoPymntTrms == null)
				return null;
			lkpCrdtMemoPymntTrms.set(BusinessEntityConstants.COMPANY_CODE_CREDIT_MEMO_PAYMENT_TERMS_CD, crdtMemoPymntTrmsVal);
		}
		
		if (setSubTeam && cmpnyCdCntryVal != null) {
			//Set Company Code Country
			DataObject lkpCmpnyCdCntry = child.createDataObject(BusinessEntityConstants.COMPANY_CODE_COMPANY_CODE_COUNTRY);
			if (lkpCmpnyCdCntry == null)
				return null;
			lkpCmpnyCdCntry.set(BusinessEntityConstants.COMPANY_CODE_COMPANY_CODE_COUNTRY_CD, cmpnyCdCntryVal);
		}
		
		if (setSubTeam && pymntMthdVal != null) {
			//Set Payment Method
			DataObject lkpPymntMthd = child.createDataObject(BusinessEntityConstants.COMPANY_CODE_PAYMENT_METHOD);
			if (lkpPymntMthd == null)
				return null;
			lkpPymntMthd.set(BusinessEntityConstants.COMPANY_CODE_PAYMENT_METHOD_CD, pymntMthdVal);
		}
		
		return child;
	}
	
	
	private DataObject getVendorTypeMaintenance(String profileRowid, CallContext callContext, CompositeServiceClient compositeServiceClient, 
			HelperContext helperContext, String businessEntity) {
		DataObject vendorTypeReturn = null;
		String splrBsnsTyp = "";
		if (businessEntity.contains("Trade")) {
			splrBsnsTyp = "Trade";
		} else if (businessEntity.contains("NonTrade")) {
			splrBsnsTyp = "NonTrade";
		}
		String filter = "rowidObject=" + profileRowid;
		
		vendorTypeReturn = businessEntityServiceClient.searchBE(callContext, compositeServiceClient, helperContext, BE_VENDOR_MAINTENANCE_PROFILE, filter, 100);
//		dataObjectHelperContext.getDataObjectDumper().dump(helperContext, BE_VENDOR_MAINTENANCE_PROFILE, vendorTypeReturn);
		List<DataObject> vendorTypeReturns = vendorTypeReturn.getList("object/item");
		if (vendorTypeReturns == null || vendorTypeReturns.isEmpty())
			return null;
		return vendorTypeReturns.get(0).getDataObject(BE_VENDOR_MAINTENANCE_PROFILE);
	}
	
}
