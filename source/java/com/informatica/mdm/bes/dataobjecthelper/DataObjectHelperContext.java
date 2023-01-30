package com.informatica.mdm.bes.dataobjecthelper;

import java.util.List;


import org.apache.log4j.Logger;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;

import com.informatica.mdm.bes.helper.StringHelper;

import commonj.sdo.DataObject;

public class DataObjectHelperContext {
	private static Logger logger = Logger.getLogger(StringHelper.class.getName());

	private DataObjectFieldHelper dataObjectFieldHelper;
	private DataObjectReader dataObjectReader;
	private DataObjectSearcher dataObjectSearcher;
	private DataObjectCombiner dataObjectCombiner;
	private CoFilterNodeHelper coFilterNodeHelper;
	private DataObjectDumper dataObjectDumper;
	private DataObjectPrinter dataObjectPrinter;
	private ErrorHelper errorHelper;
	private LookupHelper lookupHelper;
	private DataObjectGeneralHelper dataObjectGeneralHelper;
	private DataObjectCopier dataObjectCopier;
	
	public DataObjectHelperContext() {
		super();
		this.dataObjectDumper = new DataObjectDumper();
		this.dataObjectGeneralHelper = new DataObjectGeneralHelper();
		this.coFilterNodeHelper = new CoFilterNodeHelper();
		this.dataObjectFieldHelper = new DataObjectFieldHelper();
		this.dataObjectPrinter = new DataObjectPrinter();
		this.errorHelper = new ErrorHelper();
		this.dataObjectSearcher = new DataObjectSearcher(dataObjectFieldHelper, dataObjectGeneralHelper);
		this.dataObjectReader = new DataObjectReader(dataObjectDumper, coFilterNodeHelper);
		this.dataObjectCombiner = new DataObjectCombiner(dataObjectReader, dataObjectFieldHelper, dataObjectSearcher);
		this.lookupHelper = new LookupHelper(dataObjectGeneralHelper);
		this.dataObjectCopier = new DataObjectCopier(dataObjectFieldHelper);
	}
	
	public DataObjectFieldHelper getDataObjectFieldHelper() {
		return dataObjectFieldHelper;
	}
	
	public DataObjectReader getDataObjectReader() {
		return dataObjectReader;
	}
	
	public DataObjectSearcher getDataObjectSearcher() {
		return dataObjectSearcher;
	}
	
	public DataObjectCombiner getDataObjectCombiner() {
		return dataObjectCombiner;
	}
	
	public CoFilterNodeHelper getCoFilterNodeHelper() {
		return coFilterNodeHelper;
	}
	
	public DataObjectDumper getDataObjectDumper() {
		return dataObjectDumper;
	}
	
	public DataObjectPrinter getDataObjectPrinter() {
		return dataObjectPrinter;
	}
	
	public DataObjectGeneralHelper getDataObjectGeneralHelper() {
		return dataObjectGeneralHelper;
	}
	
	public LookupHelper getLookupHelper() {
		return lookupHelper;
	}
	
	public ErrorHelper getErrorHelper() {
		return errorHelper;
	}
	
	public DataObjectCopier getDataObjectCopier() {
		return dataObjectCopier;
	}
}
