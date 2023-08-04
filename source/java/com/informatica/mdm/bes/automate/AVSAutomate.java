package com.informatica.mdm.bes.automate;

import java.io.IOException;

import java.io.InputStream;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.domain.AVSResponse;
import com.informatica.mdm.bes.service.ExternalCallPropertiesService;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class AVSAutomate extends Automate {
	
	private ExternalCallPropertiesService externalCallPropertiesService;

	@Override
	public List<ValidationError> doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO, CallContext callContext,
			CompositeServiceClient besClient) {
		SDOChangeSummary sdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		// TODO Auto-generated method stub
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		
		List<DataObject> inputBankList = inputSDOBe.getList(BusinessEntityConstants.BANK + "/item");
		
		for (DataObject inputBank : inputBankList) {
			if (inputBank == null)
				continue;
			
			AVSResponse avsResponse = callAVS(inputBank);
			sdoChangeSummary.resumeLogging();
			setAVSResponse(avsResponse, inputBank);
			sdoChangeSummary.resumeLogging();
			sdoChangeSummary.pauseLogging();
		}
		
		return null;
	}
	
	private AVSResponse callAVS(DataObject inputBank) {
		AVSResponse avsResponse = null;
		
		String routingNumber = inputBank.getString(BusinessEntityConstants.BANK_ROUTING_NUMBER);
		
		try {
			URL url = new URL(externalCallPropertiesService.getAvsUrl());
			
			// Open a connection(?) on the URL(??) and cast the response(???)
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Now it's "open", we can set the request method, headers etc.
			connection.setRequestProperty("accept", "application/json");

			// This line makes the request
			InputStream responseStream = connection.getInputStream();

			// Manually converting the response body InputStream to APOD using Jackson
			ObjectMapper mapper = new ObjectMapper();
			avsResponse = mapper.readValue(responseStream, AVSResponse.class);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return avsResponse;	
	}
	
	public void setAVSResponse(AVSResponse avsResponse, DataObject bankDataObject) {
		if (avsResponse.getVerificationCode() != null) { bankDataObject.setString(avsResponse.getVerificationCode(), BusinessEntityConstants.BANK_VERIFICATION_CODE); } 
		if (avsResponse.getVerificationMessage() != null) { bankDataObject.setString(avsResponse.getVerificationMessage(), BusinessEntityConstants.BANK_VERIFICATION_MESSAGE); } 
		if (avsResponse.getAuthenticationCode() != null) { bankDataObject.setString(avsResponse.getAuthenticationCode(), BusinessEntityConstants.BANK_AUTHENTICATION_CODE); } 
		if (avsResponse.getAuthenticationMessage() != null) { bankDataObject.setString(avsResponse.getAuthenticationMessage(), BusinessEntityConstants.BANK_AUTHENTICATION_MESSAGE); } 
	}

}


//# AVS Integration
//curl --location 'https://apimulewest-dev.nonprod.az.mckesson.com/inteng-jpm-avs-sapi-dev/accounts/inquiry' \
//--header 'client_id: 0oa7fvu33q2gZSeHy1d7' \
//--header 'client_secret: vUHTxLiS8FdVa4UsQZfl6DcGTlbpN1Wh6cTC3AY7' \
//--header 'Content-Type: application/json' \
//--data '[
//    {
//        "transactionId": "123e4567-e89b-12d3-a456-426614174000",
//        "account": {
//            "accountNumber": "200001",
//            "financialInstitutionId": "122199983",
//            "financialInstitutionIdType": "ABA"
//        },
//        "entity": {
//            "personalAccountHolderFirstName": "Kara",
//            "personalAccountHolderLastName": "Aanemone",
//            "address": {
//                "addressLine1": "9384 Poodle Ln",
//                "addressLine2": "",
//                "city": "Kailau",
//                "state": "HI",
//                "postalCode": "96734"
//            },
//            "personalIds": [
//                {
//                    "idType": "SSN",
//                    "idNumber": "666648368"
//                },
//                {
//                    "idType": "DRIVERS_LICENSE_USA",
//                    "idNumber": "324648368",
//                    "idIssuer": "HI"
//                }
//            ],
//            "dateOfBirth": "05-06-1950",
//            "phoneNumbers": {
//                "home": "8087531234"
//            }
//        },
//        "transaction": {
//            "amount": 24400.90,
//            "currency": "USD",
//            "checkSerialNo": "203618"
//        }
//    }
//]'

