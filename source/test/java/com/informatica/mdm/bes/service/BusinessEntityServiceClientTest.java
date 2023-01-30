/**
 * 
 */
package com.informatica.mdm.bes.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.junit.Test;

import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.customlogic.ViewCustomLogicImpl;
import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.test_utils.FileUtil;
import com.informatica.mdm.bes.test_utils.HelperContextUtil;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.spi.cs.StepException;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.ServicePhase;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

/**
 * @author mdromazo
 *
 */
public class BusinessEntityServiceClientTest {
	BusinessEntityServiceClient businessEntityServiceClient = new BusinessEntityServiceClient();
	DataObjectHelperContext dataObjectHelperContext = new DataObjectHelperContext();
	
	@Test
	public void WhenGetDataObject_ThenOrsSdoCorrect() throws UnsupportedEncodingException, IOException, URISyntaxException {
		String businessEntity = "ExHFC_HEP_CustomerOrgView";
		String rowidObject = "80003";

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
//        Type readParametersType = helperContext.getTypeHelper().getType("urn:cs-ors.informatica.mdm", Constants.READ_ENTITY_PARAMETERS);
//		Type readEntityType = helperContext.getTypeHelper().getType("urn:cs-ors.informatica.mdm", "ReadEntity");
//		DataObject readBERequest = helperContext.getDataFactory().create(readEntityType);
//		System.out.println(readBERequest);
//        System.out.println(readParametersType);
//        XMLDocument xmlDocument = helperContext.getXMLHelper().load(FileUtil.loadMockFile("MockModifyDraftHFC.xml"));
//        DataObject inputSDO = xmlDocument.getRootObject();
        ServicePhase servicePhase = ServicePhase.WRITE_VIEW_BEFORE_VALIDATE;
        CallContext callContext = HelperContextUtil.createCallContext();
        ExternalCallRequest externalCallRequest = HelperContextUtil.createExternalCallRequest();
        CompositeServiceClient compositeServiceClient = HelperContextUtil.createCompositeServiceClient();
        Map<String, Object> inParams = new HashMap();
        Map<String, Object> outParams = new HashMap();
        inParams.put(Constants.VALIDATE_ONLY, true);
        DataObject responseSDO = businessEntityServiceClient.readExistingRecord(callContext, compositeServiceClient, externalCallRequest, helperContext, businessEntity, rowidObject); 	
        
	}
	
	@Test
	public void WhenGetMatchExpectResult_ThenCorrect() throws UnsupportedEncodingException, IOException, URISyntaxException {
		String businessEntity = "ExHFC_HEP_CustomerOrgView";
        ServicePhase servicePhase = ServicePhase.WRITE_VIEW_BEFORE_VALIDATE;
        CallContext callContext = HelperContextUtil.createCallContext();
        ExternalCallRequest externalCallRequest = HelperContextUtil.createExternalCallRequest();
        CompositeServiceClient compositeServiceClient = HelperContextUtil.createCompositeServiceClient();
        
		SDOHelperContext helperContext = new SDOHelperContext();

		
		InputStream inputStreamCoBase = FileUtil.loadFileIS("co-base.xsd");
		InputStream inputStreamCoMeta = FileUtil.loadFileIS("co-meta.xsd");
		InputStream inputStreamCoTypes = FileUtil.loadFileIS("co-types.xsd");
		InputStream inputStreamCsBase = FileUtil.loadFileIS("cs-base.xsd");
		InputStream inputStreamCsOrs = FileUtil.loadFileIS("cs-ors.xsd");
		InputStream inputStreamCsRest = FileUtil.loadFileIS("cs-rest.xsd");
		InputStream inputStreamTaskBase = FileUtil.loadFileIS("task-base.xsd");
		InputStream inputStreamDatagraph = FileUtil.loadFileIS("datagraph.xsd");
		InputStream inputStreamSdoModel = FileUtil.loadFileIS("sdoModel.xsd");

		
//        helperContext.getXSDHelper().define(inputStreamCsBase, "com/informatica/mdm/sdo/cs/base");
//        helperContext.getXSDHelper().define(inputStreamCsRest, "com/informatica/mdm/sdo/cs/rest");
//        helperContext.getXSDHelper().define(inputStreamCoBase, "com/informatica/mdm/sdo/co/base");
//        helperContext.getXSDHelper().define(inputStreamCoTypes, "com/informatica/mdm/sdo/co/types");
//        helperContext.getXSDHelper().define(inputStreamTaskBase, "com/informatica/mdm/sdo/task.base");
//        helperContext.getXSDHelper().define(inputStreamCoMeta, "com/informatica/mdm/sdo/co/meta");
        
		helperContext.getXSDHelper().define(inputStreamCsRest, "com/informatica/mdm/sdo/cs/rest");
		helperContext.getXSDHelper().define(inputStreamCsBase, "com/informatica/mdm/sdo/cs/base");
		helperContext.getXSDHelper().define(inputStreamCoBase, "com/informatica/mdm/sdo/co/base");
		helperContext.getXSDHelper().define(inputStreamTaskBase, "com/informatica/mdm/sdo/task.base");
		helperContext.getXSDHelper().define(inputStreamCoMeta, "com/informatica/mdm/sdo/co/meta");
        System.out.println("STARTING CS ORS");
        helperContext.getXSDHelper().define(inputStreamCsOrs, "mdm/informatica/cs_ors");
        helperContext.getXSDHelper().define(inputStreamCoTypes, "com/informatica/mdm/sdo/co/types");
        
//        helperContext.getXSDHelper().define(inputStreamCoBase, "com/informatica/mdm/sdo/co/base");
//        helperContext.getXSDHelper().define(inputStreamCoMeta, "com/informatica/mdm/sdo/co/meta");
//        helperContext.getXSDHelper().define(inputStreamCoTypes, "com/informatica/mdm/sdo/co/types");
//        helperContext.getXSDHelper().define(inputStreamTaskBase, "com/informatica/mdm/sdo/task.base");

        
        Map<String, String> fuzzyFilters = new HashMap();
        fuzzyFilters.put("ExFullName", "Matt Test");
        
//        DataObject matchCOReturn = businessEntityServiceClient.searchMatch(businessEntity, callContext, compositeServiceClient, helperContext.getDataFactory(), fuzzyFilters);
        
//        System.out.println(matchCOReturn);
        
	}
}
