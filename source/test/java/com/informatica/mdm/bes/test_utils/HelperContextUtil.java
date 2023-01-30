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
    	String orsId = "infaorcl-TCR_HUB";
    	String user = "e360/admin";
    	String pass = "admin";
        Properties config = new Properties();
        config.put(SiperianClient.SIPERIANCLIENT_PROTOCOL, EjbSiperianClient.PROTOCOL_NAME);
        config.put("siperian-client.orsId","infaorcl-TCR_HUB");

        config.put("java.naming.provider.url","remote://192.168.1.6:4447");
        config.put("java.naming.factory.initial","org.wildfly.naming.client.WildFlyInitialContextFactory");
        config.put("java.naming.factory.url.pkgs","org.jboss.ejb.client.naming");
//        config.put("jboss.naming.client.ejb.context","true");
        config.put("bes-client.http.url", "http://192.168.1.6:8080/cmx");
        CompositeServiceClient client = CompositeServiceClient.newCompositeServiceClient(config);
        CallContext callContext = new CallContext("infaorcl-TCR_HUB", "e360/admin", "admin");
        
        HelperContext helperContext = client.getHelperContext(callContext);
        return helperContext;
    	
    }
    
    public static CompositeServiceClient createCompositeServiceClient() {
    	String orsId = "infaorcl-TCR_HUB";
    	String user = "e360/admin";
    	String pass = "admin";
        Properties config = new Properties();
        config.put(SiperianClient.SIPERIANCLIENT_PROTOCOL, EjbSiperianClient.PROTOCOL_NAME);
        config.put("siperian-client.orsId","infaorcl-TCR_HUB");

        config.put("java.naming.provider.url","remote://192.168.1.10:4447");
        config.put("java.naming.factory.initial","org.wildfly.naming.client.WildFlyInitialContextFactory");
        config.put("java.naming.factory.url.pkgs","org.jboss.ejb.client.naming");
        config.put("bes-client.http.url", "http://192.168.1.10:8080/cmx");
        CompositeServiceClient client = CompositeServiceClient.newCompositeServiceClient(config);
        return client;
    }
    
    public static CallContext createCallContext() {
    	return new CallContext("infaorcl-TCR_HUB", "e360/admin", "admin");
    }
    
    public static ExternalCallRequest createExternalCallRequest() {
    	ExternalCallRequest externalCallRequest = new ExternalCallRequest();
    	Header header = new Header();
    	header.setOrsId("infaorcl-TCR_HUB");
    	externalCallRequest.setHeader(header);
    	return externalCallRequest;
    }
}
