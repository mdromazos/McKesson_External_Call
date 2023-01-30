package com.informatica.mdm.bes.domain;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "child")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class MandatoryChild implements Serializable {
	 
	  private static final long serialVersionUID = 1L;
	  
	  private String childName;
	  private List<MandatoryField> mandatoryFields;
	  private List<MandatoryChild> mandatoryChildList;
	  
	  public MandatoryChild() {
		  super();
	  }
	  
	  public MandatoryChild(String childName, List<MandatoryField> mandatoryFields) {
		  this.childName = childName;
		  this.mandatoryFields = mandatoryFields;
	  }
	  
	  public List<MandatoryField> getMandatoryFields() { return mandatoryFields; }
	  
	  public String getChildName() { return childName; }
	  
	  public List<MandatoryChild> getMandatoryChildList() { return mandatoryChildList; }
	  
	  @Override
	  public String toString() {
	    return "Child [Child =" + childName + "]";
	  }

}
