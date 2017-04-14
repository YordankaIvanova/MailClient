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
	private JavaMailReader _javaMailReader = new JavaMailReader();

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
		if (IS_IN_DEV_MODE) {
			if (_dev_mails == null) {
				_dev_mails = readFile("mails.txt");
			}

			messagesAsJsonArray = _dev_mails;
		} else {
			_javaMailReader.connect();
			try {
				Message[] messages = _javaMailReader.readMailMessages(folderName, page, _numMailsOnPage);
				messagesAsJsonArray = _mailFormatter.getMailMessagesBasicHeadersAsJson(messages);
			} finally {
				_javaMailReader.close();
			}
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");
		ResponseEntity<String> response = new ResponseEntity<String>(messagesAsJsonArray, headers, HttpStatus.OK);

		return response;
	}

	/**
	 * Маркира последователност от съобщения като прочетени. Съобщенията се
	 * указват чрез масив от техните идентификатори. Идентификаторите на
	 * съобщението се очаква да бъдат във вид на JSON масив.
	 *
	 * @param folderName
	 *            Папката, където се съхраняват съобщенията.
	 * @param messagesIdsAsJsonArray
	 *            Идентификаторите на съобщенията във вид на JSON масив.
	 * @return HTTP отговор, който указва дали флагът "Видян" е успешно зададен
	 *         на всяко едно съобщение.
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
	 * Маркира последователност от съобщения като непрочетени. Съобщенията се
	 * указват чрез масив от техните идентификатори. Идентификаторите на
	 * съобщението се очаква да бъдат във вид на JSON масив.
	 *
	 * @param folderName
	 *            Папката, където се съхраняват съобщенията.
	 * @param messagesIdsAsJsonArray
	 *            Идентификаторите на съобщенията във вид на JSON масив.
	 * @return HTTP отговор, който указва дали флагът "Видян" е успешно зададен
	 *         на всяко едно съобщение.
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
	 * Методът задава флагът "Видян" на последователност от съобщения.
	 * Съобщенията се указват чрез масив от техните идентификатори.
	 * Идентификаторите на съобщението се очаква да бъдат във вид на JSON масив.
	 *
	 * @param folderName
	 *            Името на папката, където трябва да се съдържат съобщенията.
	 * @param messagesIdsAsJsonArray
	 *            Идентификаторите на съобщенията във вид на JSON масив.
	 * @param shouldSet
	 *            Указва дали флагът да се зададе или да се премахне от всяко
	 *            едно съобщение.
	 * @return HTTP отговор, който указва дали флагът "Видян" е успешно зададен
	 *         на всяко едно съобщение.
	 * @throws MessagingException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private ResponseEntity<String> setMessagesSeenFlag(String folderName, String messagesIdsAsJsonArray,
			boolean shouldSet) throws MessagingException, JsonParseException, JsonMappingException, IOException {
		_javaMailReader.connect();

		ResponseEntity<String> response = null;
		try {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				long[] ids = ArrayUtils.toPrimitive(objectMapper.readValue(messagesIdsAsJsonArray, Long[].class));

				//
				_javaMailReader.setMessagesFlag(folderName, ids, MappedMessageFlag.SEEN_MAIL, shouldSet);

				response = new ResponseEntity<String>(String.format(MESSAGE_TEMPLATE, "Message flags successfully set"),
						HttpStatus.OK);
			} catch (MessagingException e) {
				response = new ResponseEntity<String>(
						String.format(ERROR_MESSAGE_TEMPLATE, "Failed to change message flags."),
						HttpStatus.BAD_REQUEST);
			}
		} finally {
			_javaMailReader.close();
		}

		return response;
	}

	@RequestMapping("/mail")
	public ResponseEntity<String> getMessageFromFolder(@RequestParam(name = "id") long messageId,
			@RequestParam(name = "folderName") String folderName) throws MessagingException, IOException {

		String messageAsJson = null;
		if (IS_IN_DEV_MODE) {
			if (_dev_single_mail == null) {
				_dev_single_mail = readFile("singleMail.txt");
			}

			messageAsJson = _dev_single_mail;
		} else {
			_javaMailReader.connect();
			try {
				Message message = _javaMailReader.readSingleMailMessage(folderName, messageId);
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
			response = new ResponseEntity<String>(String.format(ERROR_MESSAGE_TEMPLATE, "Message not found."), headers,
					HttpStatus.NOT_FOUND);
		}

		return response;
	}

	@RequestMapping("/folders/base")
	public ResponseEntity<String> getBaseFoldersData()
			throws MessagingException, JsonProcessingException {
		_javaMailReader.connect();
		String responseAsJson = null;

		try {
			List<FolderData> foldersData = _javaMailReader.getBaseFoldersData(_numMailsOnPage);
			ObjectMapper objectMapper = new ObjectMapper();
			responseAsJson = objectMapper.writeValueAsString(foldersData);
		} finally {
			_javaMailReader.close();
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
