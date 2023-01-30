package com.informatica.mdm.bes.automate;

import java.util.Map;

import org.eclipse.persistence.sdo.SDOChangeSummary;

import com.informatica.mdm.bes.domain.ExternalCallProcess;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.HelperContext;

/**
 * @author Matthew Dromazos
 * @version 1.0 2/22/2022
 * This is the superclass of all Automate classes.  It provides the common method doAutomate used as an entry point to be called for all Automate classes.
 * This also provides some logic for setting a DataObject Lookup.
 */
public abstract class Automate extends ExternalCallProcess {
	
	public Automate() { }
	
	public abstract ValidationError doAutomate(DataObject inputSDO, HelperContext helperContext,
			Map<String, Object> inParams, Map<String, Object> outParams, String businessEntity, DataObject promoteSDO, CallContext callContext
 			,CompositeServiceClient besClient);

	
	public String toString() {
		return "Automation {" +
				"automateName=" + this.getClass().getName() +
				"}";
	}
	
	protected DataObject setDataObjectLookup(DataObject dataObjectSDO, DataObject valueToSet, SDOChangeSummary sdoChangeSummary, String lookupName, String businessEntity) {
		DataObject lookupSDO = dataObjectSDO.getDataObject(lookupName);
		if (valueToSet == null || lookupSDO == null)
			return dataObjectSDO;
		
		Boolean isRootCreate = sdoChangeSummary.getRootObject().getChangeSummary().isCreated(sdoChangeSummary.getRootObject());
		if (!isRootCreate || !sdoChangeSummary.isLogging())
			sdoChangeSummary.resumeLogging();
		
		return dataObjectSDO;
	}
}
