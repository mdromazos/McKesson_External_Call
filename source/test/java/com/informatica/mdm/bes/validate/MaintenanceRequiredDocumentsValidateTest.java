package com.informatica.mdm.bes.validate;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.eclipse.persistence.sdo.helper.SDOTypeHelper;
import org.eclipse.persistence.sdo.helper.SDOXSDHelper;
import org.jcp.xml.dsig.internal.dom.Utils;
import org.junit.Test;

import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.factory.CompositeServiceClientFactoryImpl;
import com.informatica.mdm.bes.service.BusinessEntityServiceClient;
import com.informatica.mdm.bes.service.CustomLogicService;
import com.informatica.mdm.bes.test_utils.FileUtil;
import com.informatica.mdm.bes.test_utils.HelperContextUtil;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.ServicePhase;
import com.siperian.sif.message.Password;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

public class MaintenanceRequiredDocumentsValidateTest {

	DataObjectHelperContext dataObjectHelperContext = new DataObjectHelperContext();

	@Test
	public void Test_Split() throws UnsupportedEncodingException, IOException, URISyntaxException {
		String str = "ExTradeSupplier.TaxInformation.taxNum";
		System.out.println(str.split("\\.", 2)[0]);
		System.out.println(str.split("\\.", 2)[1]);
	}
	
	@Test
	public void givenNonTradeAndPharma_WhenFilterOnSubTeam_ThenFalse() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		MaintenanceRequiredDocumentsValidate maintenanceReqDocValidate = new MaintenanceRequiredDocumentsValidate(null);
		String businessEntity = "ExNonTradeSupplier";
		List<String> businessUnits = new ArrayList<String>();
		businessUnits.add("PHARMA");

		assertEquals(false, getFilterOnSubTeamMethod().invoke(maintenanceReqDocValidate, businessEntity, businessUnits));
	}
	
	@Test
	public void givenNonTradeAndPSAS_WhenFilterOnSubTeam_ThenFalse() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		MaintenanceRequiredDocumentsValidate maintenanceReqDocValidate = new MaintenanceRequiredDocumentsValidate(null);
		String businessEntity = "ExNonTradeSupplier";
		List<String> businessUnits = new ArrayList<String>();
		businessUnits.add("PSAS");

		assertEquals(false, getFilterOnSubTeamMethod().invoke(maintenanceReqDocValidate, businessEntity, businessUnits));
	}
	
	@Test
	public void givenTradeAndPSAS_WhenFilterOnSubTeam_ThenTrue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		MaintenanceRequiredDocumentsValidate maintenanceReqDocValidate = new MaintenanceRequiredDocumentsValidate(null);
		String businessEntity = "ExTradeSupplier";
		List<String> businessUnits = new ArrayList<String>();
		businessUnits.add("PSAS");

		assertEquals(true, getFilterOnSubTeamMethod().invoke(maintenanceReqDocValidate, businessEntity, businessUnits));
	}
	
	
	@Test
	public void givenTradeAndNull_WhenFilterOnSubTeam_ThenTrue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		MaintenanceRequiredDocumentsValidate maintenanceReqDocValidate = new MaintenanceRequiredDocumentsValidate(null);
		String businessEntity = "ExTradeSupplier";
		List<String> businessUnits = null;

		assertEquals(false, getFilterOnSubTeamMethod().invoke(maintenanceReqDocValidate, businessEntity, businessUnits));
	}
	
	@Test
	public void givenTradeAndRetail_WhenFilterOnSubTeam_ThenFalse() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		MaintenanceRequiredDocumentsValidate maintenanceReqDocValidate = new MaintenanceRequiredDocumentsValidate(null);
		String businessEntity = "ExTradeSupplier";
		List<String> businessUnits = new ArrayList<String>();
		businessUnits.add("RETAIL");

		assertEquals(false, getFilterOnSubTeamMethod().invoke(maintenanceReqDocValidate, businessEntity, businessUnits));
	}
	
	@Test
	public void givenTradeAndArrayWithNullEntry_WhenFilterOnSubTeam_ThenTrue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		MaintenanceRequiredDocumentsValidate maintenanceReqDocValidate = new MaintenanceRequiredDocumentsValidate(null);
		String businessEntity = "ExTradeSupplier";
		List<String> businessUnits = new ArrayList<String>();
		businessUnits.add(null);

		assertEquals(false, getFilterOnSubTeamMethod().invoke(maintenanceReqDocValidate, businessEntity, businessUnits));
	}
	
	private Method getFilterOnSubTeamMethod() throws NoSuchMethodException {
	    Method method = MaintenanceRequiredDocumentsValidate.class.getDeclaredMethod("filterOnSubTeam", String.class, List.class);
	    method.setAccessible(true);
	    return method;
	}
	
	
	@Test
	public void testConnection() throws UnsupportedEncodingException, IOException, URISyntaxException {
		InputStream besClientIS = FileUtil.loadFileIS(Constants.BES_CLIENT_FILEPATH);


        Properties config = new Properties();
        try {
            config.load(besClientIS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		
		CompositeServiceClient besClient = CompositeServiceClient.newCompositeServiceClient(config);

		BusinessEntityServiceClient businessEntityServiceClient = new BusinessEntityServiceClient();
		CallContext callContext = new CallContext("orcl-SUPPLIER_HUB", "e360/admin", "Gv6PqM5QZFlUoJfIT6KSgIkz7h6P+rlRxpomduP+cENVrfU+yPmPDOu+qepDSXljNAk4g7yE8Fq8RpJ0t1o+qaz0kaZ813wf8OHqOM1oHD+nYZjrmDP+MUotTtDj6yisoVvQHXzlDFXhnAl3CAnURc+FeNzVZUbqnGtEvZDrTN0=");
		callContext.setPassword(new Password("Gv6PqM5QZFlUoJfIT6KSgIkz7h6P+rlRxpomduP+cENVrfU+yPmPDOu+qepDSXljNAk4g7yE8Fq8RpJ0t1o+qaz0kaZ813wf8OHqOM1oHD+nYZjrmDP+MUotTtDj6yisoVvQHXzlDFXhnAl3CAnURc+FeNzVZUbqnGtEvZDrTN0=", true));
		SDOHelperContext helperContext = new SDOHelperContext();
		SDOTypeHelper typeHelper = (SDOTypeHelper) helperContext.getTypeHelper();
		SDOXSDHelper xsdHelper = (SDOXSDHelper) helperContext.getXSDHelper();
		InputStream tradeSupplierIS = FileUtil.loadFileIS("xsd/tradeSupplier.xsd");
		InputStream coBaseIS = FileUtil.loadFileIS("xsd/co-base.xsd");
		InputStream csOrsIS = FileUtil.loadFileIS("xsd/cs-ors.xsd");
		InputStream csBaseIS = FileUtil.loadFileIS("xsd/cs-base.xsd");
		InputStream csRestIS = FileUtil.loadFileIS("xsd/cs-rest.xsd");
		InputStream taskBaseIS = FileUtil.loadFileIS("xsd/task-base.xsd");
		InputStream coMetaIS = FileUtil.loadFileIS("xsd/co-meta.xsd");
		InputStream coTypesIS = FileUtil.loadFileIS("xsd/co-types.xsd");
		InputStream datagraphIS = FileUtil.loadFileIS("xsd/datagraph.xsd");
		InputStream sdoModelIS = FileUtil.loadFileIS("xsd/sdoModel.xsd");
	    xsdHelper.define(tradeSupplierIS, "urn:bes-external-call.informatica.mdm");
	    xsdHelper.define(csBaseIS, "urn:cs-base.informatica.mdm");
	    xsdHelper.define(csRestIS, "urn:cs-rest.informatica.mdm");
	    xsdHelper.define(coBaseIS, "urn:co-base.informatica.mdm");
	    xsdHelper.define(taskBaseIS, "urn:task-base.informatica.mdm");
	    xsdHelper.define(coMetaIS, "urn:co-meta.informatica.mdm");
	    xsdHelper.define(csOrsIS, "urn:cs-ors.informatica.mdm");
	    xsdHelper.define(coTypesIS, "urn:c0-types.informatica.mdm");
//	    xsdHelper.define(datagraphIS, "urn:.informatica.mdm");
//	    xsdHelper.define(sdoModelIS, "urn:cs-ors.informatica.mdm");

	    tradeSupplierIS.close();
	    csOrsIS.close();

	    Type objectType = helperContext.getTypeHelper().getType("urn:bes-external-call.informatica.mdm", "object");

		//		String filter = "splrBsnsTyp= " + splrBsnsTyp + " AND " + "SupplierAttributeApprover.attributePath IN [" + String.join(",", changedAttributes) 
//		+ "] AND subBsnsUnit IN [" + String.join(",", businessUnits) 
//		+ "] AND SupplierAttributeApprover.approvedRoles IN [" + String.join(",", rolesSearch) + "] AND SupplierAttributeApprover.reqDocUpload=Y";
//		if (filterOnSubTeam && subTeams != null) {
//			filter += " AND subTeam=" + String.join(",", subTeams);
//		}
		
		String filter = "splrBsnsTyp= Trade AND SupplierAttributeApprover.attributePath IN [ExTradeSupplier.Contacts.contacts.cntctTitle] AND "
				+ " subBsnsUnit IN [PSAS] AND SupplierAttributeApprover.approvedRoles IN [SupplierLiaisonPSAS,SupplierLiaison] AND "
				+ " SupplierAttributeApprover.reqDocUpload=Y subTeam IN [PSAS%7CGX,PSAS%7COTC_CPG]";
		String filter2 = "splrBsnsTyp=Trade";
		DataObject businessUnitAttributeApprovers = businessEntityServiceClient.searchBE(callContext, besClient, helperContext, Constants.BE_SUPPLIER_BU_APPROVER, filter2);

	}
	
	
	@Test
	public void testConnection2() throws UnsupportedEncodingException, IOException, URISyntaxException {
		BusinessEntityServiceClient businessEntityServiceClient = new BusinessEntityServiceClient();

//		String businessEntity = "ExHFC_HEP_CustomerOrgView";
//		String rowidObject = "80003";

		SDOHelperContext helperContext = new SDOHelperContext();
		InputStream inputStreamCoBase = FileUtil.loadFileIS("xsd/co-base.xsd");
		InputStream inputStreamCoMeta = FileUtil.loadFileIS("xsd/co-meta.xsd");
		InputStream inputStreamCoTypes = FileUtil.loadFileIS("xsd/co-types.xsd");
		InputStream inputStreamCsBase = FileUtil.loadFileIS("xsd/cs-base.xsd");
		InputStream inputStreamCsOrs = FileUtil.loadFileIS("xsd/cs-ors.xsd");
		InputStream inputStreamCsRest = FileUtil.loadFileIS("xsd/cs-rest.xsd");
		InputStream inputStreamTaskBase = FileUtil.loadFileIS("xsd/task-base.xsd");
		InputStream inputStreamDatagraph = FileUtil.loadFileIS("xsd/datagraph.xsd");
		InputStream inputStreamSdoModel = FileUtil.loadFileIS("xsd/sdoModel.xsd");


        helperContext.getXSDHelper().define(inputStreamCsRest, "com/informatica/mdm/sdo/cs/rest");
        helperContext.getXSDHelper().define(inputStreamCsBase, "com/informatica/mdm/sdo/cs/base");
        helperContext.getXSDHelper().define(inputStreamCoBase, "com/informatica/mdm/sdo/co/base");
        helperContext.getXSDHelper().define(inputStreamCoMeta, "com/informatica/mdm/sdo/co/meta");
        helperContext.getXSDHelper().define(inputStreamCoTypes, "com/informatica/mdm/sdo/co/types");
        helperContext.getXSDHelper().define(inputStreamTaskBase, "com/informatica/mdm/sdo/task.base");
        helperContext.getXSDHelper().define(inputStreamCsOrs, "mdm/informatica/cs_ors");

        
        ServicePhase servicePhase = ServicePhase.WRITE_VIEW_BEFORE_VALIDATE;
        CallContext callContext = HelperContextUtil.createCallContext();
        ExternalCallRequest externalCallRequest = HelperContextUtil.createExternalCallRequest();
        CompositeServiceClient compositeServiceClient = HelperContextUtil.createCompositeServiceClient();
		
		String filter = "splrBsnsTyp= Trade AND SupplierAttributeApprover.attributePath IN [ExTradeSupplier.Contacts.contacts.cntctTitle] AND "
				+ " subBsnsUnit IN [PSAS] AND SupplierAttributeApprover.approvedRoles IN [SupplierLiaisonPSAS,SupplierLiaison] AND "
				+ " SupplierAttributeApprover.reqDocUpload=Y subTeam IN [PSAS%7CGX,PSAS%7COTC_CPG]";
		String filter2 = "splrBsnsTyp=Trade";
		DataObject businessUnitAttributeApprovers = businessEntityServiceClient.searchBE(callContext, compositeServiceClient, helperContext, Constants.BE_SUPPLIER_BU_APPROVER, filter2);

//		DataObject responseSDO = businessEntityServiceClient.readExistingRecord(callContext, compositeServiceClient, externalCallRequest, helperContext, businessEntity, rowidObject); 	

	}
	
	
	@Test
	public void testEncode() throws UnsupportedEncodingException {
		String testStr = "splrBsnsTyp= Trade AND SupplierAttributeApprover.attributePath IN [ExTradeSupplier.fullNm,ExTradeSupplier.avLeadVndr,ExTradeSupplier.Root.avLeadVndr,ExTradeSupplier.Contacts.contacts,ExTradeSupplier.Contacts.Pager.item,ExTradeSupplier.Contacts.contacts,ExTradeSupplier.Contacts.contacts.seqNum,ExTradeSupplier.Contacts.contacts.seqNum,ExTradeSupplier.Contacts.contacts,ExTradeSupplier.Contacts.contacts] AND subBsnsUnit IN [PSAS] AND SupplierAttributeApprover.approvedRoles IN [SupplierLiaisonPSAS,SupplierLiaison] AND SupplierAttributeApprover.reqDocUpload=Y AND subTeam IN [PSAS|BX]";
		System.out.println(encodeValue(testStr));
	}
 	private String encodeValue(String value) throws UnsupportedEncodingException {
 	    return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
 	}
	
 	
	@Test
	public void testCOI() throws UnsupportedEncodingException {
		double min = 5000000;
		double compare = 4999999.9999999;
		System.out.println(compare < min);
	}

	
	
}
