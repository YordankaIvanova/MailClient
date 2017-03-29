package com.iv.dani.mail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class GetMessagesFromMailBox {
	private static final boolean IS_IN_DEV_MODE = false;

	private String _dev_single_mail, _dev_mails;
	private JavaMailReader _javaMailReader = new JavaMailReader();
	@Autowired
	private MailFormatter _mailFormatter;

	@CrossOrigin
	@RequestMapping("/messages")
	public ResponseEntity<String> getMessagesfromMailBox(
			@RequestParam(name = "folderName", defaultValue = "inbox") String folderName)
			throws UnsupportedEncodingException, MessagingException, JsonProcessingException, IOException {


		String messagesAsJsonArray = null;
		if (IS_IN_DEV_MODE) {
			if (_dev_mails == null) {
				_dev_mails = readFile("mails.txt");
			}

			messagesAsJsonArray = _dev_mails;
		} else {
			_javaMailReader.connect();
			try {
				Message[] messages = _javaMailReader.readMailMessages(folderName);
				messagesAsJsonArray = _mailFormatter.getMailMessagesAsJson(messages);
			} finally {
				_javaMailReader.close();
			}
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");
		ResponseEntity<String> response = new ResponseEntity<String>(messagesAsJsonArray, headers, HttpStatus.OK);

		return response;
	}

	@CrossOrigin
	@RequestMapping("/mail")
	public ResponseEntity<String> getMessageFromFolder(@RequestParam(name = "id", required = true) long messageId,
			@RequestParam(name = "folderName", required = true) String folderName)
			throws MessagingException, IOException {

		String messageAsJson = null;
		if (IS_IN_DEV_MODE) {
			if (_dev_single_mail == null) {
				_dev_single_mail = readFile("singleMail.txt");
			}

			messageAsJson = _dev_single_mail;
		} else {
			_javaMailReader.connect();
			try {
				Message message = _javaMailReader.getMessageFromFolderById(folderName, messageId);
				messageAsJson = _mailFormatter.getMailMessageAsJson(message);
			} finally {
				_javaMailReader.close();
			}
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");
		ResponseEntity<String> response = null;
		if (messageAsJson != null) {
			response = new ResponseEntity<String>(messageAsJson, headers, HttpStatus.OK);
		} else {
			response = new ResponseEntity<String>("{\"error\":\"Message nor found.\"}", headers, HttpStatus.NOT_FOUND);
		}

		return response;
	}

	// FIXME remove -for dev only
	private String readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		StringBuilder sb = new StringBuilder();
		try {
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}

		} finally {
			br.close();
		}

		return sb.toString();
	}
}
