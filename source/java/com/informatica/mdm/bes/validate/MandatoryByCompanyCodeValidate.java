package com.informatica.mdm.bes.validate;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.domain.MandatoryByCompanyCode;
import com.informatica.mdm.bes.domain.MandatoryChild;
import com.informatica.mdm.bes.domain.MandatoryField;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

/**
 * This class will look at a properties field that details attributes that are mandatory to be filled 
 * in based on the Company Codes that are available to the supplier
 * 
 * @author Matthew Dromazos
 *
 */
public class MandatoryByCompanyCodeValidate extends Validate {

	private MandatoryByCompanyCode mandatoryByCompanyCd;
	private DataFactory dataFactory;
	
	public MandatoryByCompanyCodeValidate(DataFactory dataFactory) {
		this.dataFactory = dataFactory;
	}
	
	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		// Grab Business Entity Data Object
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		DataObject promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);
		
		validationErrors.addAll(checkMandatoryFields(promoteSDOBe, inputSDOBe, mandatoryByCompanyCd.getMandatoryFields()));
		
		mandatoryByCompanyCd.getMandatoryChildList().parallelStream().forEach(mandatoryChild -> {
			// Grab the list of taxes from both inputSDO and promoteSDO so we have the complete lists
			List<DataObject> inputPromoteSDOChild = vendorSDOHelper.getCombinedListAutomation(inputSDOBe, promoteSDOBe, 
					mandatoryChild.getChildName(), inputSDO, helperContext);
			
			// Grab just the promote tax list for reference
			List<DataObject> promoteSDOChild = promoteSDOBe.getList(mandatoryChild.getChildName());
			validationErrors.addAll(checkMandatoryFields(inputPromoteSDOChild, promoteSDOChild, mandatoryChild));
		});
		
		return validationErrors;		
	}
	
	private List<ValidationError> checkMandatoryFields(List<DataObject> inputPromoteSDOChildList, List<DataObject> promoteSDOChildList, MandatoryChild mandatoryChild) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		
		
		return validationErrors;
	}
	
	private List<ValidationError> checkMandatoryFields(DataObject inputPromoteSDOChild, DataObject promoteSDOChild, MandatoryChild mandatoryChild) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		mandatoryChild.getMandatoryFields().stream().forEach(mandatoryField -> {
			if (!isFieldPopulated(inputPromoteSDOChild, promoteSDOChild, mandatoryField)) {
				validationErrors.add(createError("", "", "", dataFactory));
			}
		});
		
		List<MandatoryChild> mandatoryChildList = mandatoryChild.getMandatoryChildList();
		if (mandatoryChildList == null || mandatoryChildList.isEmpty()) { return validationErrors; }
		else {
			mandatoryChildList.parallelStream().forEach((subMandatoryChild) -> {
				DataObject dataObjectChild = null;
				validationErrors.addAll(checkMandatoryFields(inputPromoteSDOChild, promoteSDOChild, subMandatoryChild));
			});
		}
		
		return validationErrors;
	}
	
	private List<ValidationError> checkMandatoryFields(DataObject inputPromoteSDOChild, DataObject promoteSDOChild, List<MandatoryField> mandatoryFields) {
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		mandatoryFields.stream().forEach(mandatoryField -> {
			if (!isFieldPopulated(inputPromoteSDOChild, promoteSDOChild, mandatoryField)) {
				validationErrors.add(createError("", "", "", dataFactory));
			}
		});
		
		return validationErrors;
	}
	
	private boolean isFieldPopulated(DataObject inputPromoteSDOChild, DataObject promoteSDOChild, MandatoryField mandatoryField) {
		// TODO
		String fieldValue = vendorSDOHelper.getString(inputPromoteSDOChild, promoteSDOChild, mandatoryField.getFieldName());
		
		return fieldValue != null;
	}

}
