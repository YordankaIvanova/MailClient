package com.iv.dani.mail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class MailMessagesController {
	private static final boolean IS_IN_DEV_MODE = false;

	private static final String MESSAGE_TEMPLATE = "{\"message\":\"%s\"}";
	private static final String ERROR_MESSAGE_TEMPLATE = "{\"error\":\"%s\"}";

	private String _dev_single_mail, _dev_mails;

	@Autowired
	private MailFormatter _mailFormatter;

	@Value("${mail.page.numMails}")
	private int _numMailsOnPage;

	@RequestMapping("/messages")
	public ResponseEntity<String> getMessagesfromMailBox(
			@RequestParam(name = "folderName", defaultValue = "inbox") String folderName,
			@RequestParam(name = "page", defaultValue = "0") int page)
			throws UnsupportedEncodingException, MessagingException, JsonProcessingException, IOException {

		String messagesAsJsonArray = null;
		JavaMailReader javaMailReader = new JavaMailReader();
		if (IS_IN_DEV_MODE) {
			if (_dev_mails == null) {
				_dev_mails = readFile("mails.txt");
			}

			messagesAsJsonArray = _dev_mails;
		} else {
			javaMailReader.connect();
			try {
				Message[] messages = javaMailReader.readMailMessages(folderName, page, _numMailsOnPage);
				messagesAsJsonArray = _mailFormatter.getMailMessagesBasicHeadersAsJson(messages);
			} finally {
				javaMailReader.close();
			}
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");
		ResponseEntity<String> response = new ResponseEntity<String>(messagesAsJsonArray, headers, HttpStatus.OK);

		return response;
	}

	/**
	 * РњР°СЂРєРёСЂР° РїРѕСЃР»РµРґРѕРІР°С‚РµР»РЅРѕСЃС‚ РѕС‚ СЃСЉРѕР±С‰РµРЅРёСЏ РєР°С‚Рѕ РїСЂРѕС‡РµС‚РµРЅРё. РЎСЉРѕР±С‰РµРЅРёСЏС‚Р° СЃРµ
	 * СѓРєР°Р·РІР°С‚ С‡СЂРµР· РјР°СЃРёРІ РѕС‚ С‚РµС…РЅРёС‚Рµ РёРґРµРЅС‚РёС„РёРєР°С‚РѕСЂРё. Р�РґРµРЅС‚РёС„РёРєР°С‚РѕСЂРёС‚Рµ РЅР°
	 * СЃСЉРѕР±С‰РµРЅРёРµС‚Рѕ СЃРµ РѕС‡Р°РєРІР° РґР° Р±СЉРґР°С‚ РІСЉРІ РІРёРґ РЅР° JSON РјР°СЃРёРІ.
	 *
	 * @param folderName
	 *            РџР°РїРєР°С‚Р°, РєСЉРґРµС‚Рѕ СЃРµ СЃСЉС…СЂР°РЅСЏРІР°С‚ СЃСЉРѕР±С‰РµРЅРёСЏС‚Р°.
	 * @param messagesIdsAsJsonArray
	 *            Р�РґРµРЅС‚РёС„РёРєР°С‚РѕСЂРёС‚Рµ РЅР° СЃСЉРѕР±С‰РµРЅРёСЏС‚Р° РІСЉРІ РІРёРґ РЅР° JSON РјР°СЃРёРІ.
	 * @return HTTP РѕС‚РіРѕРІРѕСЂ, РєРѕР№С‚Рѕ СѓРєР°Р·РІР° РґР°Р»Рё С„Р»Р°РіСЉС‚ "Р’РёРґСЏРЅ" Рµ СѓСЃРїРµС€РЅРѕ Р·Р°РґР°РґРµРЅ
	 *         РЅР° РІСЃСЏРєРѕ РµРґРЅРѕ СЃСЉРѕР±С‰РµРЅРёРµ.
	 * @throws MessagingException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping("/messages/mark/read")
	public ResponseEntity<String> setMessagesAsRead(@RequestParam(name = "folderName") String folderName,
			@RequestParam(name = "ids") String messagesIdsAsJsonArray)
			throws MessagingException, JsonParseException, JsonMappingException, IOException {

		return setMessagesSeenFlag(folderName, messagesIdsAsJsonArray, true);
	}

	/**
	 * РњР°СЂРєРёСЂР° РїРѕСЃР»РµРґРѕРІР°С‚РµР»РЅРѕСЃС‚ РѕС‚ СЃСЉРѕР±С‰РµРЅРёСЏ РєР°С‚Рѕ РЅРµРїСЂРѕС‡РµС‚РµРЅРё. РЎСЉРѕР±С‰РµРЅРёСЏС‚Р° СЃРµ
	 * СѓРєР°Р·РІР°С‚ С‡СЂРµР· РјР°СЃРёРІ РѕС‚ С‚РµС…РЅРёС‚Рµ РёРґРµРЅС‚РёС„РёРєР°С‚РѕСЂРё. Р�РґРµРЅС‚РёС„РёРєР°С‚РѕСЂРёС‚Рµ РЅР°
	 * СЃСЉРѕР±С‰РµРЅРёРµС‚Рѕ СЃРµ РѕС‡Р°РєРІР° РґР° Р±СЉРґР°С‚ РІСЉРІ РІРёРґ РЅР° JSON РјР°СЃРёРІ.
	 *
	 * @param folderName
	 *            РџР°РїРєР°С‚Р°, РєСЉРґРµС‚Рѕ СЃРµ СЃСЉС…СЂР°РЅСЏРІР°С‚ СЃСЉРѕР±С‰РµРЅРёСЏС‚Р°.
	 * @param messagesIdsAsJsonArray
	 *            Р�РґРµРЅС‚РёС„РёРєР°С‚РѕСЂРёС‚Рµ РЅР° СЃСЉРѕР±С‰РµРЅРёСЏС‚Р° РІСЉРІ РІРёРґ РЅР° JSON РјР°СЃРёРІ.
	 * @return HTTP РѕС‚РіРѕРІРѕСЂ, РєРѕР№С‚Рѕ СѓРєР°Р·РІР° РґР°Р»Рё С„Р»Р°РіСЉС‚ "Р’РёРґСЏРЅ" Рµ СѓСЃРїРµС€РЅРѕ Р·Р°РґР°РґРµРЅ
	 *         РЅР° РІСЃСЏРєРѕ РµРґРЅРѕ СЃСЉРѕР±С‰РµРЅРёРµ.
	 * @throws MessagingException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping("/messages/mark/unread")
	public ResponseEntity<String> setMessagesAsUnread(@RequestParam(name = "folderName") String folderName,
			@RequestParam(name = "ids") String messagesIds)
			throws MessagingException, JsonParseException, JsonMappingException, IOException {

		return setMessagesSeenFlag(folderName, messagesIds, false);
	}

	/**
	 * РњРµС‚РѕРґСЉС‚ Р·Р°РґР°РІР° С„Р»Р°РіСЉС‚ "Р’РёРґСЏРЅ" РЅР° РїРѕСЃР»РµРґРѕРІР°С‚РµР»РЅРѕСЃС‚ РѕС‚ СЃСЉРѕР±С‰РµРЅРёСЏ.
	 * РЎСЉРѕР±С‰РµРЅРёСЏС‚Р° СЃРµ СѓРєР°Р·РІР°С‚ С‡СЂРµР· РјР°СЃРёРІ РѕС‚ С‚РµС…РЅРёС‚Рµ РёРґРµРЅС‚РёС„РёРєР°С‚РѕСЂРё.
	 * Р�РґРµРЅС‚РёС„РёРєР°С‚РѕСЂРёС‚Рµ РЅР° СЃСЉРѕР±С‰РµРЅРёРµС‚Рѕ СЃРµ РѕС‡Р°РєРІР° РґР° Р±СЉРґР°С‚ РІСЉРІ РІРёРґ РЅР° JSON РјР°СЃРёРІ.
	 *
	 * @param folderName
	 *            Р�РјРµС‚Рѕ РЅР° РїР°РїРєР°С‚Р°, РєСЉРґРµС‚Рѕ С‚СЂСЏР±РІР° РґР° СЃРµ СЃСЉРґСЉСЂР¶Р°С‚ СЃСЉРѕР±С‰РµРЅРёСЏС‚Р°.
	 * @param messagesIdsAsJsonArray
	 *            Р�РґРµРЅС‚РёС„РёРєР°С‚РѕСЂРёС‚Рµ РЅР° СЃСЉРѕР±С‰РµРЅРёСЏС‚Р° РІСЉРІ РІРёРґ РЅР° JSON РјР°СЃРёРІ.
	 * @param shouldSet
	 *            РЈРєР°Р·РІР° РґР°Р»Рё С„Р»Р°РіСЉС‚ РґР° СЃРµ Р·Р°РґР°РґРµ РёР»Рё РґР° СЃРµ РїСЂРµРјР°С…РЅРµ РѕС‚ РІСЃСЏРєРѕ
	 *            РµРґРЅРѕ СЃСЉРѕР±С‰РµРЅРёРµ.
	 * @return HTTP РѕС‚РіРѕРІРѕСЂ, РєРѕР№С‚Рѕ СѓРєР°Р·РІР° РґР°Р»Рё С„Р»Р°РіСЉС‚ "Р’РёРґСЏРЅ" Рµ СѓСЃРїРµС€РЅРѕ Р·Р°РґР°РґРµРЅ
	 *         РЅР° РІСЃСЏРєРѕ РµРґРЅРѕ СЃСЉРѕР±С‰РµРЅРёРµ.
	 * @throws MessagingException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private ResponseEntity<String> setMessagesSeenFlag(String folderName, String messagesIdsAsJsonArray,
			boolean shouldSet) throws MessagingException, JsonParseException, JsonMappingException, IOException {
		JavaMailReader javaMailReader = new JavaMailReader();
		javaMailReader.connect();

		ResponseEntity<String> response = null;
		try {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				long[] ids = ArrayUtils.toPrimitive(objectMapper.readValue(messagesIdsAsJsonArray, Long[].class));

				//
				javaMailReader.setMessagesFlag(folderName, ids, MappedMessageFlag.SEEN_MAIL, shouldSet);

				response = new ResponseEntity<String>(String.format(MESSAGE_TEMPLATE, "Message flags successfully set"),
						HttpStatus.OK);
			} catch (MessagingException e) {
				response = new ResponseEntity<String>(
						String.format(ERROR_MESSAGE_TEMPLATE, "Failed to change message flags."),
						HttpStatus.BAD_REQUEST);
			}
		} finally {
			javaMailReader.close();
		}

		return response;
	}

	@RequestMapping("/mail")
	public ResponseEntity<String> getMessageFromFolder(@RequestParam(name = "id") long messageId,
			@RequestParam(name = "folderName") String folderName) throws MessagingException, IOException {
		String messageAsJson = null;
		JavaMailReader javaMailReader = new JavaMailReader();
		
		if (IS_IN_DEV_MODE) {
			if (_dev_single_mail == null) {
				_dev_single_mail = readFile("singleMail.txt");
			}

			messageAsJson = _dev_single_mail;
		} else {
			javaMailReader.connect();
			try {
				Message message = javaMailReader.readSingleMailMessage(folderName, messageId);
				messageAsJson = _mailFormatter.getMailMessageAsJson(message);
			} finally {
				javaMailReader.close();
			}
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");
		ResponseEntity<String> response = null;
		if (messageAsJson != null) {
			response = new ResponseEntity<String>(messageAsJson, headers, HttpStatus.OK);
		} else {
			response = new ResponseEntity<String>(String.format(ERROR_MESSAGE_TEMPLATE, "Message not found."), headers,
					HttpStatus.NOT_FOUND);
		}

		return response;
	}

	@RequestMapping("/folders/base")
	public ResponseEntity<String> getBaseFoldersData()
			throws MessagingException, JsonProcessingException {
		JavaMailReader javaMailReader = new JavaMailReader();
		javaMailReader.connect();
		
		String responseAsJson = null;

		try {
			List<FolderData> foldersData = javaMailReader.getBaseFoldersData(_numMailsOnPage);
			ObjectMapper objectMapper = new ObjectMapper();
			responseAsJson = objectMapper.writeValueAsString(foldersData);
		} finally {
			javaMailReader.close();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");
		ResponseEntity<String> response = null;
		if (responseAsJson != null) {
			response = new ResponseEntity<String>(responseAsJson, headers, HttpStatus.OK);
		} else {
			response = new ResponseEntity<String>(
					String.format(ERROR_MESSAGE_TEMPLATE, "No Folders discovered for the client mailbox."), headers,
					HttpStatus.NOT_FOUND);
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
