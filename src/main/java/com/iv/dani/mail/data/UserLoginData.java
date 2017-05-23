package com.iv.dani.mail.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserLoginData {
	private String _username, _password, _imapHost, _smtpHost;

	@JsonCreator
	public UserLoginData(
			 @JsonProperty("username") String username,
			 @JsonProperty("password") String password,
			 @JsonProperty("imapHost") String imapHost,
			 @JsonProperty("smtpHost") String smtpHost) {
		_username = username;
		_password = password;
		_imapHost = imapHost;
		_smtpHost = smtpHost;
	}

	public String getUsername() {
		return _username;
	}

	public String getPassword() {
		return _password;
	}

	public String getImapHost() {
		return _imapHost;
	}

	public String getSmtpHost() {
		return _smtpHost;
	}
}
