package com.informatica.mdm.bes.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.informatica.mdm.bes.config.BusinessEntityConstants;
import com.informatica.mdm.bes.config.Constants;
import com.informatica.mdm.bes.config.ErrorConstants;
import com.informatica.mdm.bes.config.VendorMainConstants;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * If the Ariba Check Request is set then there must be an Alternate ID child with type ANID.
 * 
 * @author Sharath
 *
 */
public class ANIDValidate extends Validate {

	@Override
	public List<ValidationError> doValidation(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity,
			DataObject promotePreviewSDO, CallContext callContext, CompositeServiceClient besClient, DataObject dbSDO) {
		pauseLogging(inputSDO);
		// Double check that this is not running during the Trade Registration View
		if (inputSDO == null || businessEntity.equals(Constants.BE_TRADE_REGIST_VIEW)
				|| businessEntity.equals(Constants.BE_NONTRADE_REGIST_VIEW))
			return null;
		Boolean validateOnly = (Boolean) inParams.get(Constants.VALIDATE_ONLY);
		Boolean isE360Save = isE360Save(validateOnly);

		// Run when validations when save button is clicked
		if (isE360Save) {

			DataObject promoteSDOBe = null;
			DataObject inputSDOBe = inputSDO.getDataObject(businessEntity);

			if (promotePreviewSDO != null)
				promoteSDOBe = promotePreviewSDO.getDataObject(businessEntity);

			String aribaCheckReq = vendorSDOHelper.getString(inputSDOBe, promoteSDOBe, inputSDO, VendorMainConstants.ARIBA_CHECK_REQ);
			if (aribaCheckReq != null) {
				if (aribaCheckReq.equalsIgnoreCase("No")) {
					List<DataObject> alternateIdList = vendorSDOHelper.getCombinedList(inputSDOBe, promoteSDOBe,
							VendorMainConstants.ALTERNATE_ID, inputSDO, helperContext);

					if (alternateIdList == null || alternateIdList.isEmpty()) {

						return createErrors(ErrorConstants.ANID_VALIDATE_ERROR_CODE,
								ErrorConstants.ANID_VALIDATE_ERROR_MESSAGE,
								businessEntity + "." + VendorMainConstants.ALTERNATE_ID, helperContext.getDataFactory(),
								"ERROR");
					} else {
						List<String> altIdTypList = new ArrayList<String>();

						alternateIdList.parallelStream().forEach((inputalternateId) -> {
							if (inputalternateId != null) {
								String inputPromoteAlternateRowid = inputalternateId
										.getString(BusinessEntityConstants.ROWID_OBJECT);
								DataObject promoteAlternateId = null;
								if (inputPromoteAlternateRowid != null && alternateIdList != null)
									promoteAlternateId = dataObjectHelperContext.getDataObjectSearcher()
											.searchDataObjectList(alternateIdList, inputPromoteAlternateRowid);
								String altIdTyp = vendorSDOHelper.getString(inputalternateId, promoteAlternateId,
										VendorMainConstants.ALTERNATE_ID_TYPE);
//								logger.info("Alternate Id Type : " + altIdTyp);
								altIdTypList.add(altIdTyp);
							}
						});
						if (!altIdTypList.contains(VendorMainConstants.ANID)) {
							return createErrors(ErrorConstants.ANID_VALIDATE_ERROR_CODE,
									ErrorConstants.ANID_VALIDATE_ERROR_MESSAGE,
									businessEntity + "." + VendorMainConstants.ALTERNATE_ID,
									helperContext.getDataFactory(), "ERROR");
						}
					}
				}

			} else {
				logger.info("Ariba Check Request is null");
			}
		}
		return null;
	}

	protected Boolean isE360Save(Boolean validateOnly) {
		return (validateOnly != null && !validateOnly) ? true : false;

	}

}
