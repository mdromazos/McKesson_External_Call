/**
 * 
 */
package com.informatica.mdm.bes.domain;

import java.util.Map;

import com.informatica.mdm.bes.dataobjecthelper.DataObjectHelperContext;
import com.informatica.mdm.bes.helper.VendorSDOHelper;
import com.informatica.mdm.cs.CallContext;
import com.informatica.mdm.cs.client.CompositeServiceClient;
import com.informatica.mdm.sdo.cs.base.ValidationError;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;

/**
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public abstract class ExternalCallProcess {
	protected VendorSDOHelper vendorSDOHelper;
	protected DataObjectHelperContext dataObjectHelperContext;

	
	public void setVendorSDOHelper(VendorSDOHelper vendorSDOHelper) {
		this.vendorSDOHelper = vendorSDOHelper;
	}
	
	public VendorSDOHelper getVendorSDOHelper() {
		return vendorSDOHelper;
	}
	
	public void setDataObjectHelperContext(DataObjectHelperContext dataObjectHelperContext) {
		this.dataObjectHelperContext = dataObjectHelperContext;
	}
	
	public DataObjectHelperContext getDataObjectHelperContext() {
		return dataObjectHelperContext;
	}
}
