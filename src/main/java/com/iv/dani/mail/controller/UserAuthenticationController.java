package com.iv.dani.mail.controller;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iv.dani.mail.JavaMailReader;
import com.iv.dani.mail.UserSessionStore;
import com.iv.dani.mail.data.UserLoginData;
import com.iv.dani.mail.util.HttpUtils;

@RestController
public class UserAuthenticationController {
	@Autowired
	private UserSessionStore _userSessionStore;

	@Autowired
	private HttpUtils _httpUtils;

	@RequestMapping(value = "/auth/login", method = RequestMethod.POST)
	public  ResponseEntity<String> login(
			@RequestBody UserLoginData userLoginData) {
		JavaMailReader mailReader = new JavaMailReader();
		ResponseEntity<String> response = null;

		try {
			mailReader.connect(userLoginData);
			String userToken = _userSessionStore.createUserSession(userLoginData);
			response = _httpUtils.createSuccessPlainTextResponse(userToken);
		} catch (MessagingException e) {
			response = _httpUtils.createErrorPlainTextResponse("Authentication failed. Please try again!", HttpStatus.BAD_REQUEST);
		} finally {
			mailReader.close();
		}

		return response;
	}

	@RequestMapping(value = "/auth/logout", method = RequestMethod.POST)
	public  ResponseEntity<String> logout(
			@RequestHeader(HttpUtils.USER_TOKEN_HTTP_HEADER_NAME) String userToken) {
		JavaMailReader mailReader = new JavaMailReader();
		ResponseEntity<String> response = null;

		try {
			_userSessionStore.removeUserSession(userToken);
			response = _httpUtils.createMessagePlainTextResponse("User logged out.", HttpStatus.OK);
		} catch (IllegalStateException e) {
			response = _httpUtils.createErrorPlainTextResponse("No user is authenticated!", HttpStatus.BAD_REQUEST);
		} finally {
			mailReader.close();
		}

		return response;
	}
}
