package com.informatica.mdm.bes.validate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.SDOChangeSummary;
import org.eclipse.persistence.sdo.SDODataObject;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.bes.service.BusinessEntityServiceClient;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;

public class RequestValidate extends Validate {
	private static Logger logger = Logger.getLogger(RequestValidate.class.getName());

	BusinessEntityServiceClient businessEntityServiceClient = new BusinessEntityServiceClient();

	DataObject bePromotePreviewSDO;

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);

		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = promotePreviewSDO != null ? promotePreviewSDO.getDataObject(businessEntity) : null;
		
		
		SDOChangeSummary inputSdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		SDOChangeSummary promotePreviewChangeSummary = promotePreviewSDO != null ? (SDOChangeSummary) promotePreviewSDO.getChangeSummary() : null;
		List<SDODataObject> promoteDataObjects = promotePreviewChangeSummary != null ? promotePreviewChangeSummary.getCreated() : null;
		
		Map<String, List<SDODataObject>> createdDataObjects = vendorSDOHelper.getDataObjectsByType(promoteDataObjects, inputSdoChangeSummary.getCreated());
		boolean validateOnly = (boolean) inParams.get(Constants.VALIDATE_ONLY);
		
		logger.info("CREATED DATA OBJECTS: " + createdDataObjects);
		
		if (validateOnly) {
			if (createdDataObjects != null && createdDataObjects.get(BusinessEntityConstants.REQUEST) != null && 
					createdDataObjects.get(BusinessEntityConstants.REQUEST).size() > 1) {
				validationErrors.add(createError(ErrorConstants.CANT_HAVE_MULTIPLE_ACTIVE_REQUESTS_ERROR_CODE,
						ErrorConstants.CANT_HAVE_MULTIPLE_ACTIVE_REQUESTS_ERROR_MESSAGE, 
						businessEntity + "." + BusinessEntityConstants.REQUEST, 
						helperContext.getDataFactory()));
				return validationErrors;
			}
			
			// Check that the user did not modify any previous Requests
			List<DataObject> inputRequests = inputSDOBe.getList(BusinessEntityConstants.REQUEST + "/item");
			if (inputRequests != null) {
				for (DataObject inputRequest : inputRequests) {
					if (inputRequest.getInt(BusinessEntityConstants.HUB_STATE_IND) == 1 &&
							inputSdoChangeSummary.isModified(inputRequest)) {
						validationErrors.add(createError(ErrorConstants.CANT_MODIFY_PREVIOUS_REQUEST_ERROR_CODE,
								ErrorConstants.CANT_MODIFY_PREVIOUS_REQUEST_ERROR_MESSAGE, 
								businessEntity + "." + BusinessEntityConstants.REQUEST, 
								helperContext.getDataFactory()));
						return validationErrors;
					}	
				}	
			}
			
		} else {
			// If the call is not validateOnly - meaning the user selected "Save", and we do not have any request, 
			// then create one.
			if (createdDataObjects == null || createdDataObjects.get(BusinessEntityConstants.REQUEST) == null || createdDataObjects.get(BusinessEntityConstants.REQUEST).size() < 1) {
				List<DataObject> requestList = inputSDOBe
	                    .getList(BusinessEntityConstants.REQUEST + "/item");
				inputSdoChangeSummary.resumeLogging();
				DataObject requestPager = inputSDOBe.getDataObject(BusinessEntityConstants.REQUEST);
				if (requestPager == null) {
					requestPager = inputSDOBe.createDataObject(BusinessEntityConstants.REQUEST);
				}
				
				DataObject requestItem = requestPager.createDataObject("item");
				requestItem.setDate("startDt", new Date());
				DataObject requestType = requestItem.createDataObject("reqTyp");
				requestType.setString("reqTypCd", "MAINTENANCE");
				DataObject requestSts = requestItem.createDataObject("reqSts");
				requestSts.setString("reqStsCd", "OPEN");
				inputSdoChangeSummary.pauseLogging();
			}
		}
		
		return validationErrors;
	}
}
