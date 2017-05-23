package com.iv.dani.mail.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserLoginData {
	private String _username, _password, _host;

	@JsonCreator
	public UserLoginData(
			 @JsonProperty("username") String username,
			 @JsonProperty("password") String password,
			 @JsonProperty("host") String host) {
		_username = username;
		_password = password;
		_host = host;
	}

	public String getUsername() {
		return _username;
	}

	public String getPassword() {
		return _password;
	}

	public String getHost() {
		return _host;
	}
}
