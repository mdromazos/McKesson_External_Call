package com.informatica.mdm.bes.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.informatica.mdm.bes.automate.Automate;
import com.informatica.mdm.bes.validate.Validate;

/**
 * This class is meant to hold groups of Automations and Validations.  They are grouped together so that they can run in parallel using Threads
 * For each group it creates new threads for each Validation or Automation, then waits till every item in the group is finished, then it moves to the next group.
 * 
 * @author Matthew Dromazos
 * @version 1.0 1/10/2022
 */
public class ParallelBusinessRules extends BusinessRules {

	private List<List<Validate>> parallelValidations;
	private List<List<Automate>> parallelAutomations;
	
	public ParallelBusinessRules(int numGroupsValidate, int numGroupsAutomate) {
		parallelValidations = new ArrayList<List<Validate>>();
		parallelAutomations = new ArrayList<List<Automate>>();
		createGroups(parallelValidations, numGroupsValidate);
		createGroups(parallelAutomations, numGroupsAutomate);
	}
	
	
	public void addValidation(Validate validate, int group) {
		try {
			parallelValidations.get(group).add(validate);
		} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			parallelValidations.add(new ArrayList<Validate>());
			addValidation(validate, group);
		}
	}
	
	public void addAutomation(Automate automate, int group) {
		try {
			parallelAutomations.get(group).add(automate);
		} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			parallelAutomations.add(new ArrayList<Automate>());
			addAutomation(automate, group);
		}
	}
	
	public List<List<Validate>> getParallelValidations() { return parallelValidations; }
	public List<List<Automate>> getParallelAutomations() { return parallelAutomations; }

	private void createGroups(List<? extends List> c, int numGroups) {
		for (int i = 0; i < numGroups; i++) ((List<List<?>>)c).add(new ArrayList<ExternalCallProcess>());
	}
}
