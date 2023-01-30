package com.informatica.mdm.bes.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mandatory")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class MandatoryByCompanyCode {
	
	private List<MandatoryField> mandatoryFields;
	private List<MandatoryChild> mandatoryChildList;
	
	public MandatoryByCompanyCode() {
		super();
	}
	
	public MandatoryByCompanyCode(List<MandatoryField> mandatoryFields) {
		this.mandatoryFields = mandatoryFields;
	}
	
	public List<MandatoryField> getMandatoryFields() { return mandatoryFields; }
	public List<MandatoryChild> getMandatoryChildList() { return mandatoryChildList; }
}
