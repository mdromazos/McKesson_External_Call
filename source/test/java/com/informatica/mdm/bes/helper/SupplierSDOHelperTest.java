package com.informatica.mdm.bes.helper;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.test_utils.CreateSDOUtil;
import com.informatica.mdm.bes.test_utils.FileUtil;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

import org.eclipse.persistence.sdo.SDOChangeSummary;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.eclipse.persistence.sdo.helper.SDOTypeHelper;
import org.eclipse.persistence.sdo.helper.SDOXSDHelper;
import org.junit.Before;

public class SupplierSDOHelperTest {
	
	private DataObject inputSDO;
	private DataObject inputSDOBe;
	private DataObject promoteSDO;
	private DataObject promoteSDOBe;
	private SDOHelperContext helperContext;
	private SDOChangeSummary sdoChangeSummary;
	private DataObjectHelperContext dataObjectHelperContext;
	private VendorSDOHelper vendorSDOHelper;
	
	@Before
	public void Setup() throws UnsupportedEncodingException, IOException, URISyntaxException {
		dataObjectHelperContext = new DataObjectHelperContext();
		vendorSDOHelper = new VendorSDOHelper();
		helperContext = new SDOHelperContext();
		SDOTypeHelper typeHelper = (SDOTypeHelper) helperContext.getTypeHelper();
		SDOXSDHelper xsdHelper = (SDOXSDHelper) helperContext.getXSDHelper();
		InputStream inputStream = FileUtil.loadFileIS("xsd/tradeSupplier.xsd");
	    xsdHelper.define(inputStream, "urn:bes-external-call.informatica.mdm");
	    inputStream.close();

		inputSDO = CreateSDOUtil.createSDO(helperContext);
		inputSDOBe = inputSDO.getDataObject("ExTradeSupplier");

		promoteSDO = CreateSDOUtil.createSDO(helperContext);
		promoteSDOBe = promoteSDO.getDataObject("ExTradeSupplier");
		
		sdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();

			
	}
	
	@Test
	public void givenInputPromoteCoCdPromoteCoCdWithOneEntry_WhenGetBU_ThenStringArray() {
	
		List<DataObject> inputCompanyCodeList = CreateSDOUtil.createChildListNoGrandChild(inputSDO, inputSDOBe, helperContext, 1, "CompanyCode", "item", false);
		List<DataObject> promoteCompanyCodeList = CreateSDOUtil.createChildListNoGrandChild(promoteSDO, promoteSDOBe, helperContext, 2, "CompanyCode", "item", false);
		
		String[] inputBsnsUnitToSet = {"PSAS"};
		String[] promoteBsnsUnitToSet = {"PSAS", "MSH"};
		String[] inputRowidObjectToSet = {"1"};
		String[] promoteRowidObjectToSet = {"1", "2"};
		setupChildListWithLookup(inputCompanyCodeList, Arrays.asList(inputBsnsUnitToSet), "bsnsUnit", "bsnsUnitCd");
		setupChildListWithLookup(promoteCompanyCodeList, Arrays.asList(promoteBsnsUnitToSet), "bsnsUnit", "bsnsUnitCd");

		setupChildListWithField(inputCompanyCodeList, Arrays.asList(inputRowidObjectToSet), "rowidObject");
		setupChildListWithField(promoteCompanyCodeList, Arrays.asList(promoteRowidObjectToSet), "rowidObject");
		
		List<DataObject> inputPromoteCompanyCodes = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.COMPANY_CODE, inputSDO, helperContext);
		
		List<String> businessUnits = SupplierSDOHelper.getBusinessUnits(inputPromoteCompanyCodes, promoteCompanyCodeList, dataObjectHelperContext);
		List<String> expectedBusinessUnits = new ArrayList<String>();
		expectedBusinessUnits.add("PSAS");
		expectedBusinessUnits.add("MSH");
		assertEquals(expectedBusinessUnits, businessUnits);
	}
	
	@Test
	public void givenInputPromoteCoCdPromoteCoCdWithOneEntry_WhenGetSubTeam_ThenStringArray() {
	
		List<DataObject> inputCompanyCodeList = CreateSDOUtil.createChildListNoGrandChild(inputSDO, inputSDOBe, helperContext, 1, "CompanyCode", "item", false);
		List<DataObject> promoteCompanyCodeList = CreateSDOUtil.createChildListNoGrandChild(promoteSDO, promoteSDOBe, helperContext, 2, "CompanyCode", "item", false);
		
		String[] inputSubTeamsToSet = {"PSAS|GX"};
		String[] promoteSubTeamsToSet = {"PSAS|GX", "PSAS|OTC_CPG"};
		String[] inputRowidObjectToSet = {"1"};
		String[] promoteRowidObjectToSet = {"1", "2"};
		setupChildListWithLookup(inputCompanyCodeList, Arrays.asList(inputSubTeamsToSet), "subTeam", "subTeamCd");
		setupChildListWithLookup(promoteCompanyCodeList, Arrays.asList(promoteSubTeamsToSet), "subTeam", "subTeamCd");

		setupChildListWithField(inputCompanyCodeList, Arrays.asList(inputRowidObjectToSet), "rowidObject");
		setupChildListWithField(promoteCompanyCodeList, Arrays.asList(promoteRowidObjectToSet), "rowidObject");
		
		List<DataObject> inputPromoteCompanyCodes = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe, BusinessEntityConstants.COMPANY_CODE, inputSDO, helperContext);
		
		List<String> subTeams = SupplierSDOHelper.getSubTeams(inputPromoteCompanyCodes, promoteCompanyCodeList, dataObjectHelperContext);
		String[] expectedSubTeams = {"PSAS|GX", "PSAS|OTC_CPG"};
		
		assertEquals(Arrays.asList(expectedSubTeams), subTeams);
	}
	
	
	private void setupChildListWithField(List<DataObject> childList, List<String> valuesToSet, String fieldToSet) {
		if (childList.size() != valuesToSet.size())
			return;
	
		sdoChangeSummary.resumeLogging();
		for (int i = 0; i < childList.size(); i++) {
			childList.get(i).set(fieldToSet, valuesToSet.get(i));
		}
		sdoChangeSummary.pauseLogging();
	}
	
	private void setupChildListWithLookup(List<DataObject> childList, List<String> valuesToSet, String lookupToSet, String fieldToSet) {
		if (childList.size() != valuesToSet.size())
			return;
	
//		sdoChangeSummary.resumeLogging();
		for (int i = 0; i < childList.size(); i++) {
			DataObject lookup = childList.get(i).createDataObject(lookupToSet);
			lookup.set(fieldToSet, valuesToSet.get(i));
		}
//		sdoChangeSummary.pauseLogging();
	}
	
}
