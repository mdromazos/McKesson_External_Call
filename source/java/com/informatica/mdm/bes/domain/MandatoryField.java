package com.informatica.mdm.bes.domain;


import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "field")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class MandatoryField implements Serializable {
	private String fieldName;
	
	public MandatoryField() {
		super();
	}
	
	public MandatoryField(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getFieldName() { return fieldName; }
	
	@Override
	public String toString() {
		return "Field [Child =" + fieldName + "]";
	}


}
