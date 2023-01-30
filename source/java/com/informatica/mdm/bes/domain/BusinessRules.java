package com.informatica.mdm.bes.domain;


import com.informatica.mdm.bes.automate.Automate;
import com.informatica.mdm.bes.validate.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds data on what validations and automations should be run at a given time.
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class BusinessRules {
	
	List<Validate> validations;
	
	List<Automate> automations;
	
	public BusinessRules() {
		this.automations = new ArrayList<>();
		this.validations = new ArrayList<>();
	}
	
	public BusinessRules(List<Validate> validations, List<Automate> automations) {
		this.validations = validations;
		this.automations = automations;
	}
	 
	public List<Validate> getValidations() {
		return validations;
	}
	
	public void setValidations(List<Validate> validations) {
		this.validations = validations;
	}
	
	public List<Automate> getAutomations() {
		return automations;
	}
	
	public void setAutomations(List<Automate> automations) {
		this.automations = automations;
	}
	
	@Override
	public String toString() {
		return "BusinessRules{" +
				"Validations {" + validations.stream().map(validation -> validation.toString()) + "}," +
				"Automations {" + automations.stream().map(automation -> automation.toString()) + "}," +
				"}";
	}

}
