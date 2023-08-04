package com.informatica.mdm.bes.automate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

public class MaskAutomate extends Automate {
	List<String> roles;
	
	public MaskAutomate(List<String> roles) {
		this.roles = roles;
	}
	
	public final Map<String, MaskCondition> MASK_CONDITIONS = Stream.of(
			  new AbstractMap.SimpleEntry<>("TaxInformation.taxNum", 
					  new MaskAutomate.MaskCondition(true, "taxNumTyp/taxNumTypCd", "SSN", 5,
							  new ArrayList<String>(Arrays.asList("SupplierLiaisonMMS"))))
			  
			  ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	@Override
	public List<ValidationError> doAutomate(DataObject inputSDO, HelperContext helperContext, Map<String, Object> inParams,
			Map<String, Object> outParams, String businessEntity, DataObject promoteSDO, CallContext callContext,
			CompositeServiceClient besClient) {
		
		SDOChangeSummary inputSdoChangeSummary = (SDOChangeSummary) inputSDO.getChangeSummary();
		
		inputSdoChangeSummary.pauseLogging();
		DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);
		inputSDO.getDataObject(businessEntity).setString("fullNm", "");
		
		for (String fieldKey : MASK_CONDITIONS.keySet()) {
			MaskCondition maskCondition = MASK_CONDITIONS.get(fieldKey);
			List<String> maskFieldRoles = maskCondition.roles;
			boolean maskField = roles.parallelStream().anyMatch(role -> maskFieldRoles.contains(role));
			if (!maskField)
				continue;
			maskField(inputSDOBe, fieldKey, maskCondition);
		}

		return null;
	}
	

	public void maskField(DataObject inputSDO, String field, MaskCondition maskCondition) {
		String[] fieldSplit = field.split(".");
		if(fieldSplit.length > 1) {
			String childName = fieldSplit[0];
			List<DataObject> childList = inputSDO.getList(childName + "/item");
			boolean isChildArray = childList != null;
			if (isChildArray) {
				for (DataObject childDo : childList) {
					maskField(childDo, fieldSplit[1], maskCondition);
				}
			} else {
				DataObject childDo = inputSDO.getDataObject(childName);
				maskField(childDo, fieldSplit[0], maskCondition);
			}
		} else {
			if (maskCondition.dependentOnType) {
				String typeFieldVal = inputSDO.getString(maskCondition.typeFieldNm);
				if (typeFieldVal != null && typeFieldVal.equals(maskCondition.typeFieldVal)) {
					inputSDO.set(field, null);
				}
			} else {
				String fieldVal = inputSDO.getString(field);
				if (fieldVal == null)
					return;
				
				String maskedValue = fieldVal.substring(0, maskCondition.maskDigits) + fieldVal.substring(maskCondition.maskDigits, fieldVal.length()-1);
				inputSDO.set(field, maskedValue);
			}
		}
	}
	
	private class MaskCondition {
		public boolean dependentOnType;
		public String typeFieldNm;
		public String typeFieldVal;
		public int maskDigits;
		public List<String> roles;
		
		public MaskCondition(boolean dependentOnType, 
							 String typeFieldNm, 
							 String typeFieldVal, 
							 int maskDigits,
							 List<String> roles) {
			this.dependentOnType = dependentOnType;
			this.typeFieldNm = typeFieldNm;
			this.typeFieldVal = typeFieldVal;
			this.roles = roles;
		}
	}

}
