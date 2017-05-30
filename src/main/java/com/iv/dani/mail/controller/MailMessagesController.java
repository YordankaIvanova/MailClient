package com.iv.dani.mail.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iv.dani.mail.JavaMailReader;
import com.iv.dani.mail.JavaMailWriter;
import com.iv.dani.mail.UserSessionStore;
import com.iv.dani.mail.data.FolderData;
import com.iv.dani.mail.data.MailMessage;
import com.iv.dani.mail.data.MappedMessageFlag;
import com.iv.dani.mail.util.HttpUtils;
import com.iv.dani.mail.util.MailFormatter;

//TODO make the X-User-Token Header mandatory; it is not required for now.
/**
 * Този контролер служи за обработка на заявки за извличане на информация за
 * мейлите, съхранявани в указаната потребителска поща.
 */
@RestController
public class MailMessagesController {
	private static final String FOLDER_NAME_PROPERTY = "folderName";
	private static final String PAGE_PROPERTY = "page";
	private static final String MESSAGES_IDS_LIST_PROPERTY = "ids";
	private static final String MESSAGE_ID_PROPERTY = "id";

	@Autowired
	private HttpUtils _httpUtils;

	@Autowired
	private UserSessionStore _userSessionStore;

	@Autowired
	private MailFormatter _mailFormatter;

	@Autowired
	private JavaMailWriter _mailwriter;

	@Value("${mail.page.numMails}")
	private int _numMailsOnPage;

	/**
	 * Този метод извлича основна информация за определен брой мейли от
	 * указаната папка (име на мейла, изпращач, дата на получаване, др.). UID на
	 * началния и крайния мейл зависят от указаната страница за преглеждане на
	 * мейли.
	 *
	 * @param folderName
	 *            Папката, където се съхраняват съобщенията.
	 * @param page
	 *            Страницата от мейли, която се преглежда.
	 * @param userToken
	 *            Потребителският токен за автентикация.
	 * @return JSON масив от обекти, които съдържат основната информация за
	 *         мейлите, или съобщение за грешка, ако информацията не може да
	 *         бъде извлечена от мейл сървъра или обработена до JSON.
	 */
	@RequestMapping("/messages")
	public ResponseEntity<String> getMessagesfromMailBox(
			@RequestParam(name = FOLDER_NAME_PROPERTY, defaultValue = "inbox") String folderName,
			@RequestParam(name = PAGE_PROPERTY, defaultValue = "0") int page,
			@RequestHeader(HttpUtils.USER_TOKEN_HTTP_HEADER_NAME) String userToken) {

		JavaMailReader javaMailReader = new JavaMailReader();
		ResponseEntity<String> response = null;

		try {
			javaMailReader.connect(_userSessionStore.getUserSession(userToken));

			Message[] messages = javaMailReader.readMailMessages(folderName, page, _numMailsOnPage);
			String messagesAsJsonArray = _mailFormatter.getMailMessagesBasicHeadersAsJson(messages);
			response = _httpUtils.createSuccessPlainTextResponse(messagesAsJsonArray);
		} catch (MessagingException e) {
			response = _httpUtils.createErrorPlainTextResponse(
					"Could not read message.",
					HttpStatus.NOT_FOUND);
		} catch (UnsupportedEncodingException | JsonProcessingException e) {
			response = _httpUtils.createErrorPlainTextResponse(
					"Could not process message content.",
					HttpStatus.NOT_FOUND);
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
	 * @param userToken
	 *            Потребителският токен за автентикация.
	 * @return HTTP отговор, който указва дали флагът "Видян" е успешно зададен
	 *         на всяко едно съобщение.
	 */
	@RequestMapping("/messages/mark/read")
	public ResponseEntity<String> setMessagesAsRead(
			@RequestParam(name = FOLDER_NAME_PROPERTY) String folderName,
			@RequestParam(name = MESSAGES_IDS_LIST_PROPERTY) String messagesIdsAsJsonArray,
			@RequestHeader(HttpUtils.USER_TOKEN_HTTP_HEADER_NAME) String userToken) {

		return setMessagesSeenFlag(userToken, folderName, messagesIdsAsJsonArray, true);
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
	 * @param userToken
	 *            Потребителският токен за автентикация.
	 * @return HTTP отговор, който указва дали флагът "Видян" е успешно зададен
	 *         на всяко едно съобщение.
	 */
	@RequestMapping("/messages/mark/unread")
	public ResponseEntity<String> setMessagesAsUnread(
			@RequestParam(name = FOLDER_NAME_PROPERTY) String folderName,
			@RequestParam(name = MESSAGES_IDS_LIST_PROPERTY) String messagesIds,
			@RequestHeader(HttpUtils.USER_TOKEN_HTTP_HEADER_NAME) String userToken) {

		return setMessagesSeenFlag(userToken, folderName, messagesIds, false);
	}

	/**
	 * Този метод извлича цялото съдържанеие на мейл за неговото прочитане.
	 *
	 * @param messageId
	 *            Идентификатора на мейла.
	 * @param folderName
	 *            Името на папката, където се съхранява писмото.
	 * @param userToken
	 *            Потребителският токен за автентикация.
	 * @return JSON обект, който съдържа информацията за мейла, или съобщение за
	 *         грешка, ако информацията не може да се вземе от мейл сървъра или
	 *         не може да се обработи до JSON.
	 */
	@RequestMapping("/mail")
	public ResponseEntity<String> getMessageFromFolder(
			@RequestParam(name = MESSAGE_ID_PROPERTY) long messageId,
			@RequestParam(name = FOLDER_NAME_PROPERTY) String folderName,
			@RequestHeader(HttpUtils.USER_TOKEN_HTTP_HEADER_NAME) String userToken) {
		JavaMailReader javaMailReader = new JavaMailReader();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");
		ResponseEntity<String> response = null;

		try {
			javaMailReader.connect(_userSessionStore.getUserSession(userToken));

			Message message = javaMailReader.readSingleMailMessage(folderName, messageId);
			String messageAsJson = _mailFormatter.getMailMessageAsJson(message);
			response = _httpUtils.createSuccessPlainTextResponse(messageAsJson);
		} catch (MessagingException | IOException e) {
			response = _httpUtils.createErrorPlainTextResponse(
					"Message not found.",
					HttpStatus.NOT_FOUND);
		} finally {
			javaMailReader.close();
		}

		return response;
	}

	/**
	 * Методът служи за вземане на основна информация за базовите папки в
	 * клиентската поща. Информацията, която се взема, се съхранява в обект от
	 * клас {@link FolderData}.
	 *
	 * @param userToken
	 *            Потребителският токен за автентикация.
	 * @return JSON масив от обекти, в който ще се съдържа събраната инфомация,
	 *         или съобщения за грешка при неуспешно вземане или обработване на
	 *         информацията.
	 */
	@RequestMapping("/folders/base")
	public ResponseEntity<String> getBaseFoldersData(
			@RequestHeader(HttpUtils.USER_TOKEN_HTTP_HEADER_NAME) String userToken) {
		JavaMailReader javaMailReader = new JavaMailReader();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");

		ResponseEntity<String> response = null;
		try {
			javaMailReader.connect(_userSessionStore.getUserSession(userToken));

			List<FolderData> foldersData = javaMailReader.getBaseFoldersData(_numMailsOnPage);
			ObjectMapper objectMapper = new ObjectMapper();
			String responseAsJson = objectMapper.writeValueAsString(foldersData);
			response = _httpUtils.createSuccessPlainTextResponse(responseAsJson);
		} catch (MessagingException e) {
			response = _httpUtils.createErrorPlainTextResponse(
					"No Folders discovered for the client mailbox.",
					HttpStatus.NOT_FOUND);
		} catch (JsonProcessingException e) {
			response = _httpUtils.createErrorPlainTextResponse(
					"Folder data could not be processes.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			javaMailReader.close();
		}

		return response;
	}

	@RequestMapping(value = "/mail/send", method = RequestMethod.PUT)
	public ResponseEntity<String> writeMailMessage(
			@RequestHeader(HttpUtils.USER_TOKEN_HTTP_HEADER_NAME) String userToken,
			@RequestBody MailMessage mailMessage) {

		ResponseEntity<String> response = null;
		try {
			_mailwriter.sendMessage(_userSessionStore.getUserSession(userToken), mailMessage);
			response = _httpUtils.createMessagePlainTextResponse("Message successfully sent", HttpStatus.OK);
		} catch (MessagingException e) {
			//FIXME logging.
			e.printStackTrace();
			response = _httpUtils.createErrorPlainTextResponse("Message could not be sent", HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	/**
	 * Методът задава флагът "Видян" на последователност от съобщения.
	 * Съобщенията се указват чрез масив от техните идентификатори.
	 * Идентификаторите на съобщението се очаква да бъдат във вид на JSON масив.
	 *
	 * @param userToken
	 *            Потребителският токен за автентикация.
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
			String userToken,
			String folderName,
			String messagesIdsAsJsonArray,
			boolean shouldSet) {
		JavaMailReader javaMailReader = new JavaMailReader();
		ResponseEntity<String> response = null;

		try {
			javaMailReader.connect(_userSessionStore.getUserSession(userToken));

			ObjectMapper objectMapper = new ObjectMapper();
			String[] strIds = objectMapper.readValue(messagesIdsAsJsonArray, String[].class);
			long[] ids = new long[strIds.length];
			for(int i = 0; i < strIds.length; i++) {
				ids[i] = Long.parseLong(strIds[i]);
			}

			javaMailReader.setMessagesFlag(folderName, ids, MappedMessageFlag.SEEN_MAIL, shouldSet);
			response = _httpUtils.createSuccessPlainTextResponse("Message flags successfully set");
		} catch (MessagingException | IOException e) {
			response = _httpUtils.createErrorPlainTextResponse(
					"Failed to change message flags.",
					HttpStatus.BAD_REQUEST);
		} finally {
			javaMailReader.close();
		}

		return response;
	}
}
