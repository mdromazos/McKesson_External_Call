package com.informatica.mdm.bes.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AVSResponse {
	
	private String verificationCode;
	private String verificationMessage;
	private String authenticationCode;
	private String authenticationMessage;
	
	public AVSResponse(
			@JsonProperty("validationCode") String verificationCode,
			@JsonProperty("verificationMessage") String verificationMessage,
			@JsonProperty("authenticationCode") String authenticationCode,
			@JsonProperty("authenticationMessage") String authenticationMessage) {
		this.verificationCode = verificationCode;
		this.verificationMessage = verificationMessage;
		this.authenticationCode = authenticationCode;
		this.authenticationMessage = authenticationMessage;
	}
	
	
	public String getVerificationCode() { return verificationCode; }
	public String getVerificationMessage() { return verificationMessage; }
	public String getAuthenticationCode() { return authenticationCode; }
	public String getAuthenticationMessage() { return authenticationMessage; }

}
