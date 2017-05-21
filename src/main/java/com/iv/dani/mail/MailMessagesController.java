package com.iv.dani.mail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class MailMessagesController {
	private static final String MESSAGE_TEMPLATE = "{\"message\":\"%s\"}";
	private static final String ERROR_MESSAGE_TEMPLATE = "{\"error\":\"%s\"}";

	@Autowired
	private MailFormatter _mailFormatter;

	@Value("${mail.page.numMails}")
	private int _numMailsOnPage;

	@RequestMapping("/messages")
	public ResponseEntity<String> getMessagesfromMailBox(
			@RequestParam(name = "folderName", defaultValue = "inbox") String folderName,
			@RequestParam(name = "page", defaultValue = "0") int page) {

		JavaMailReader javaMailReader = new JavaMailReader();
		ResponseEntity<String> response = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");

		try {
			javaMailReader.connect();

			Message[] messages = javaMailReader.readMailMessages(folderName, page, _numMailsOnPage);
			String messagesAsJsonArray = _mailFormatter.getMailMessagesBasicHeadersAsJson(messages);
			response = new ResponseEntity<String>(messagesAsJsonArray, headers, HttpStatus.OK);
		} catch (MessagingException e) {
			response = new ResponseEntity<String>(
					String.format(ERROR_MESSAGE_TEMPLATE, "Could not read message."),
					headers,
					HttpStatus.NOT_FOUND);
		} catch (UnsupportedEncodingException | JsonProcessingException e) {
			response = new ResponseEntity<String>(
					String.format(ERROR_MESSAGE_TEMPLATE, "Could not process message content."),
					headers,
					HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			javaMailReader.close();
		}

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
	 */
	@RequestMapping("/messages/mark/read")
	public ResponseEntity<String> setMessagesAsRead(
			@RequestParam(name = "folderName") String folderName,
			@RequestParam(name = "ids") String messagesIdsAsJsonArray) {

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
	 */
	@RequestMapping("/messages/mark/unread")
	public ResponseEntity<String> setMessagesAsUnread(
			@RequestParam(name = "folderName") String folderName,
			@RequestParam(name = "ids") String messagesIds) {

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
	 */
	private ResponseEntity<String> setMessagesSeenFlag(
			String folderName,
			String messagesIdsAsJsonArray,
			boolean shouldSet) {
		JavaMailReader javaMailReader = new JavaMailReader();
		ResponseEntity<String> response = null;

		try {
			javaMailReader.connect();

			ObjectMapper objectMapper = new ObjectMapper();
			String[] strIds = objectMapper.readValue(messagesIdsAsJsonArray, String[].class);
			long[] ids = new long[strIds.length];
			for(int i = 0; i < strIds.length; i++) {
				ids[i] = Long.parseLong(strIds[i]);
			}

			javaMailReader.setMessagesFlag(folderName, ids, MappedMessageFlag.SEEN_MAIL, shouldSet);
			response = new ResponseEntity<String>(
					String.format(MESSAGE_TEMPLATE, "Message flags successfully set"),
					HttpStatus.OK);
		} catch (MessagingException | IOException e) {
			response = new ResponseEntity<String>(
					String.format(ERROR_MESSAGE_TEMPLATE, "Failed to change message flags."),
					HttpStatus.BAD_REQUEST);
		} finally {
			javaMailReader.close();
		}

		return response;
	}

	@RequestMapping("/mail")
	public ResponseEntity<String> getMessageFromFolder(
			@RequestParam(name = "id") long messageId,
			@RequestParam(name = "folderName") String folderName) {
		JavaMailReader javaMailReader = new JavaMailReader();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");
		ResponseEntity<String> response = null;

		try {
			javaMailReader.connect();

			Message message = javaMailReader.readSingleMailMessage(folderName, messageId);
			String messageAsJson = _mailFormatter.getMailMessageAsJson(message);
			response = new ResponseEntity<String>(messageAsJson, headers, HttpStatus.OK);
		} catch (MessagingException | IOException e) {
			response = new ResponseEntity<String>(
					String.format(ERROR_MESSAGE_TEMPLATE, "Message not found."),
					headers,
					HttpStatus.NOT_FOUND);
		} finally {
			javaMailReader.close();
		}

		return response;
	}

	@RequestMapping("/folders/base")
	public ResponseEntity<String> getBaseFoldersData() {
		JavaMailReader javaMailReader = new JavaMailReader();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");

		ResponseEntity<String> response = null;
		try {
			javaMailReader.connect();

			List<FolderData> foldersData = javaMailReader.getBaseFoldersData(_numMailsOnPage);
			ObjectMapper objectMapper = new ObjectMapper();
			String responseAsJson = objectMapper.writeValueAsString(foldersData);
			response = new ResponseEntity<String>(responseAsJson, headers, HttpStatus.OK);
		} catch (MessagingException e) {
			response = new ResponseEntity<String>(
					String.format(ERROR_MESSAGE_TEMPLATE, "No Folders discovered for the client mailbox."),
					headers,
					HttpStatus.NOT_FOUND);
		} catch (JsonProcessingException e) {
			response = new ResponseEntity<String>(
					String.format(ERROR_MESSAGE_TEMPLATE, "Folder data could not be processes."),
					headers,
					HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			javaMailReader.close();
		}

		return response;
	}
}
