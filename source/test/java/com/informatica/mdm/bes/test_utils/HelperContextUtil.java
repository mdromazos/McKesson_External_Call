package com.informatica.mdm.bes.test_utils;

import java.util.Properties;

import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.api.CompositeServiceException;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.Header;
import com.siperian.sif.client.EjbSiperianClient;
import com.siperian.sif.client.HttpSiperianClient;
import com.siperian.sif.client.SiperianClient;

import commonj.sdo.helper.HelperContext;

public class HelperContextUtil {
    public static HelperContext createHelperContext() throws CompositeServiceException {
    	String orsId = "orcl-SUPPLIER_HUB";
    	String user = "e360/admin";
    	String pass = "mckesp@77d5";
        Properties config = new Properties();
        config.put(SiperianClient.SIPERIANCLIENT_PROTOCOL, EjbSiperianClient.PROTOCOL_NAME);
        config.put("siperian-client.orsId","orcl-SUPPLIER_HUB");

//        config.put("java.naming.provider.url","remote://172.27.107.119:4447");
        config.put("java.naming.provider.url","remote://mckesp-devint.mdm.informaticahostednp.com:4447");
        config.put("java.naming.factory.initial","org.wildfly.naming.client.WildFlyInitialContextFactory");
        config.put("java.naming.factory.url.pkgs","org.jboss.ejb.client.naming");
//        config.put("jboss.naming.client.ejb.context","true");
//        config.put("bes-client.http.url", "https://172.27.107.119:8443/cmx");
        config.put("bes-client.http.url", "https://mckesp-devint.mdm.informaticahostednp.com/cmx");
        
        CompositeServiceClient client = CompositeServiceClient.newCompositeServiceClient(config);
        CallContext callContext = new CallContext("orcl-SUPPLIER_HUB", "e360/admin", "admin");
        
        HelperContext helperContext = client.getHelperContext(callContext);
        return helperContext;
    	
    }
    
    public static CompositeServiceClient createCompositeServiceClient() {
    	String orsId = "orcl-SUPPLIER_HUB";
    	String user = "e360/admin";
    	String pass = "mckesp@afa4";
        Properties config = new Properties();
//        config.put(SiperianClient.SIPERIANCLIENT_PROTOCOL, EjbSiperianClient.PROTOCOL_NAME);
        config.put(SiperianClient.SIPERIANCLIENT_PROTOCOL, EjbSiperianClient.PROTOCOL_NAME);
        config.put("siperian-client.orsId","orcl-SUPPLIER_HUB");

        config.put("java.naming.provider.url","remote://172.27.107.119:4447");
        config.put("java.naming.factory.initial","org.wildfly.naming.client.WildFlyInitialContextFactory");
        config.put("java.naming.factory.url.pkgs","org.jboss.ejb.client.naming");
        config.put("bes-client.http.url", "https://172.27.107.119/cmx");
        config.put("siperian-client.username","e360/admin");
        config.put("siperian-client.password","Gv6PqM5QZFlUoJfIT6KSgIkz7h6P+rlRxpomduP+cENVrfU+yPmPDOu+qepDSXljNAk4g7yE8Fq8RpJ0t1o+qaz0kaZ813wf8OHqOM1oHD+nYZjrmDP+MUotTtDj6yisoVvQHXzlDFXhnAl3CAnURc+FeNzVZUbqnGtEvZDrTN0=");
        CompositeServiceClient client = CompositeServiceClient.newCompositeServiceClient(config);
        return client;
    }
    
    public static CallContext createCallContext() {
    	CallContext callContext = new CallContext("orcl-SUPPLIER_HUB", "e360/admin", "Gv6PqM5QZFlUoJfIT6KSgIkz7h6P+rlRxpomduP+cENVrfU+yPmPDOu+qepDSXljNAk4g7yE8Fq8RpJ0t1o+qaz0kaZ813wf8OHqOM1oHD+nYZjrmDP+MUotTtDj6yisoVvQHXzlDFXhnAl3CAnURc+FeNzVZUbqnGtEvZDrTN0=");
    	callContext.getPassword().setEncrypted(true);
    	return callContext;
    }
    
    public static ExternalCallRequest createExternalCallRequest() {
    	ExternalCallRequest externalCallRequest = new ExternalCallRequest();
    	Header header = new Header();
    	header.setOrsId("orcl-SUPPLIER_HUB");
    	externalCallRequest.setHeader(header);
    	return externalCallRequest;
    }
}
