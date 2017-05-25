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
	private static final String TOKEN_RESPONSE_BODY_TEMPLATE = "{\"token\":\"%s\"}";
	private static final String VALID_TOKEN_TEMPLATE = "{\"isValid\":\"%b\"}";

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
			response = _httpUtils
					.createSuccessJsonResponse(String.format(TOKEN_RESPONSE_BODY_TEMPLATE, userToken));
		} catch (MessagingException e) {
			response = _httpUtils.createErrorPlainTextResponse("Authentication failed. Please try again!",
					HttpStatus.BAD_REQUEST);
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

	@RequestMapping(value = "/auth/verify", method = RequestMethod.GET)
	public  ResponseEntity<String> verify(
			@RequestHeader(HttpUtils.USER_TOKEN_HTTP_HEADER_NAME) String userToken) {
		ResponseEntity<String> response = null;

		boolean isValid = _userSessionStore.getUserSession(userToken) != null;
		response = _httpUtils.createSuccessJsonResponse(String.format(VALID_TOKEN_TEMPLATE, isValid));

		return response;
	}
}
