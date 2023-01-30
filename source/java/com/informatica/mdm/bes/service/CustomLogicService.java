package com.informatica.mdm.bes.service;

import java.util.Properties;




import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service.Mode;

import org.apache.log4j.Logger;

import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.factory.CompositeServiceClientFactoryImpl;
import com.informatica.mdm.bes.factory.CustomLogicFactoryImpl;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.spi.externalcall.CustomLogicFactory;
import com.informatica.mdm.spi.externalcall.ExternalCallProcessor;
import com.informatica.mdm.spi.externalcall.ExternalCallRequest;
import com.informatica.mdm.spi.externalcall.ObjectFactory;
import com.informatica.mdm.spi.externalcall.ServiceCall;
import com.siperian.sif.client.EjbSiperianClient;
import com.siperian.sif.client.SiperianClient;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

/**
 * Web service implementation.
 * It must accept {urn:bes-external-call.informatica.mdm}ExternalCallRequest as operation input
 * and return {urn:bes-external-call.informatica.mdm}ExternalCallResponse as output
 */
@WebServiceProvider(
        targetNamespace = "http://cs.sample.mdm.informatica.com/",
        serviceName = "CustomLogicService",
        portName = "CustomLogicServicePort",
        wsdlLocation = "WEB-INF/wsdl/custom-logic-service.wsdl"
)
@ServiceMode(Mode.PAYLOAD)
public class CustomLogicService implements Provider<Source> {
    public static final Logger log = Logger.getLogger(CustomLogicService.class); 
    
	private CompositeServiceClientFactoryImpl compositeServiceClientFactoryImpl =
			new CompositeServiceClientFactoryImpl(Constants.BES_CLIENT_FILEPATH);
	private CompositeServiceClient compositeServiceClient;
	private CustomLogicFactory customLogicFactory;
    
	public CustomLogicService() {
		this.compositeServiceClient = compositeServiceClientFactoryImpl.createCompositeServiceClient();
		this.customLogicFactory = new CustomLogicFactoryImpl(compositeServiceClient);
	}

    @Override
    public Source invoke(Source request) {
    	log.info("ENTER CUSTOM LOGIC SERVICE");
        CustomLogicFactory customLogicFactory = new CustomLogicFactoryImpl(compositeServiceClient);
        String appName = Constants.APP_USERNAME; // replace with proper application user name
        // create processor instance ond let it do the job.
        // all we need to provide is a custom logic factory implementation.
        
        ExternalCallProcessor externalCallProcessor =
                 new ExternalCallProcessor(compositeServiceClient,appName,customLogicFactory);

        return externalCallProcessor.invoke(request);
    }
}
