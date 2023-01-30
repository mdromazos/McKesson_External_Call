/**
 * 
 */
package com.informatica.mdm.bes.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author mdromazo
 *
 */
public class ExternalCallPropertiesServiceTest {
	
	ExternalCallPropertiesService externalCallPropertiesService;
	
	@Test
	public void WhenCreateRunMatchbookTrue_ThenGetRunMatchbookTrue() {
		externalCallPropertiesService = new ExternalCallPropertiesService();
		
		boolean expectedResult = true;
		boolean response = externalCallPropertiesService.getRunMatchbook();
		
		assertEquals(expectedResult, response);
	}

}
