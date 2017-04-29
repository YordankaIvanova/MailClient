package com.iv.dani.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Целта на този клас е да предоставя методи за обработка и форматиране на
 * съдържанието на съобщенията. Информацията се представя в JSON формат.
 *
 */
@Component
public class MailFormatter {

	/**
	 * Методът прочита инфромацията в съобщението и конструира JSON обект, който
	 * отразява неговото съдържание. Съдържанието се състои от хедърната част на
	 * съобщението (изпращач, получател, дата, и т.н.) както и от тялото на
	 * съобщението.
	 *
	 * @param message
	 *            Съобщението, чието съдържание да бъде форматирано.
	 * @return JSON репрезентация на съобщението.
	 * @throws MessagingException
	 *             Грешка, при извличане на елемент от съобщението.
	 * @throws IOException
	 *             Грешка при прочитане на тялото на съобщението или при
	 *             образуването на JSON обекта.
	 */
	public String getMailMessageAsJson(Message message) throws MessagingException, IOException {
		if (message == null) {
			return null;
		}

		String messageAsJson = null;

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> mailMessage = new HashMap<String, Object>();
		mailMessage.put("id", getMessageId(message));
		mailMessage.put("from", message.getFrom());
		mailMessage.put("to", message.getRecipients(RecipientType.TO));
		mailMessage.put("cc", message.getRecipients(RecipientType.CC));
		mailMessage.put("bcc", message.getRecipients(RecipientType.BCC));
		mailMessage.put("subject", message.getSubject());
		mailMessage.put("sentDate", message.getSentDate().getTime());
		mailMessage.put("receivedDate", message.getReceivedDate().getTime());
		mailMessage.put("content", acquirePartContent(message));

		// Преобразуване на хеш-таблицата в JSON обект.
		messageAsJson = objectMapper.writeValueAsString(mailMessage);

		return messageAsJson;
	}

	/**
	 * Този метод взема хедърна инфромация за съобщенията, без да взема тялото
	 * на съобщението. Като резултата се генерира JSON масив от обекти, като
	 * всеки един от тях съдържа хедърна информация за конкретен имейл.
	 *
	 * @param messages
	 *            Съобщенията, които да бъдат представени в JSON формат.
	 * @return JSON стринг, съдържащ масив от JSON обекти.
	 * @throws UnsupportedEncodingException
	 *             Грешка при декодиране на хедър.
	 * @throws MessagingException
	 *             Грешка, при извличане на елемент от съобщението.
	 * @throws JsonProcessingException
	 *             Грешка при образуването на JSON маисва.
	 */
	public String getMailMessagesBasicHeadersAsJson(Message[] messages)
			throws UnsupportedEncodingException, MessagingException, JsonProcessingException {
		String[] jsonMessages = null;
		ObjectMapper objectMapper = new ObjectMapper();

		jsonMessages = new String[messages.length];

		for (int i = 0; i < messages.length; i++) {
			Map<String, Object> mailMessage = new HashMap<String, Object>();
			mailMessage.put("from", decodeWord(messages[i].getFrom()[0].toString()));
			mailMessage.put("subject", decodeWord(messages[i].getSubject()));
			mailMessage.put("date", messages[i].getReceivedDate().getTime());
			mailMessage.put("folderName", messages[i].getFolder().getName());
			mailMessage.put("id", getMessageId(messages[i]));
			mailMessage.putAll(MappedMessageFlag.getMappedMessageFlagsAsProperties(messages[i]));

			jsonMessages[i] = objectMapper.writeValueAsString(mailMessage);
		}

		String messagesAsJsonArray = objectMapper.writeValueAsString(jsonMessages);

		return messagesAsJsonArray;
	}

	/**
	 * Този метод извлича тялото на част от съобщението.
	 *
	 * @param part
	 *            Частта от съобщението, чието съдържание да бъде прочетено.
	 * @return Прочетеното съдържание.
	 * @throws IOException
	 *             Грешка при прочитане на съдържанието.
	 * @throws MessagingException
	 *             Грешка при прочитане на тялото на съобщението
	 */
	//TODO да се ивзличат прикачени файлове.
	private String acquirePartContent(Part part) throws IOException, MessagingException {
		StringBuilder partContent = new StringBuilder();
		// Текстовото съдържание може да бъде извлечено директно.
		if (part.isMimeType("text/*")) {
			partContent.append((String) part.getContent());
		} else if (part.isMimeType("multipart/alternative")) {
			// При този MIME тип, съобщението е представено в няколко формата -
			// html, текстови и т.н., като най-подходящия формат е най-последен
			// в списъка. За това се прави опит да се вземе съобщението в
			// подходящ формат, започвайки от последния елемент в масива.
			Multipart mp = (Multipart) part.getContent();
			int count = mp.getCount();
			for (int i = count - 1; i >= 0; i++) {
				String messageContents = acquirePartContent(mp.getBodyPart(i));
				if (messageContents != null) {
					partContent.append(messageContents);
					break;
				}
			}
		} else if (part.isMimeType("multipart/mixed")) {
			// Налични са няколко формата в това съобщение (например, текст,
			// прикачени файлов и др.)
			Multipart mp = (Multipart) part.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++) {
				partContent.append(acquirePartContent(mp.getBodyPart(i)));
			}
		}
		// В тялото на съобщението се съдържат вложени други съобщения.
		else if (part.isMimeType("message/rfc822")) {
			partContent.append((Message) part.getContent());
		}
		// Съдържанието е вложено изображение.
		else if (part.isMimeType("image/*")) {
			System.out.println("--------> image/jpeg");
			Object o = part.getContent();

			InputStream imageStream = (InputStream) o;
			partContent.append(getStringFromInputStream(imageStream));
		}
		// else if (p.getContentType().contains("image/")) {
		// System.out.println("content type" + p.getContentType());
		// File f = new File("image" + new Date().getTime() + ".jpg");
		// DataOutputStream output = new DataOutputStream(
		// new BufferedOutputStream(new FileOutputStream(f)));
		// com.sun.mail.util.BASE64DecoderStream test =
		// (com.sun.mail.util.BASE64DecoderStream) p
		// .getContent();
		// byte[] buffer = new byte[1024];
		// int bytesRead;
		// while ((bytesRead = test.read(buffer)) != -1) {
		// output.write(buffer, 0, bytesRead);
		// }
		// }
		else {
			Object o = part.getContent();
			if (o instanceof String) {
				partContent.append(partContent.append((String) o));
			} else if (o instanceof InputStream) {
				InputStream is = (InputStream) o;
				getStringFromInputStream(is);
			} else {
				return null;
			}
		}

		return partContent.toString();
	}

	/**
	 * Този метод извлича уникалния идентификатор на едно съобщение, ако е
	 * възможно. При липса на идентификатор, методът връща -1.
	 *
	 * @param message
	 *            Съобщението, чийто идентификатор да бъде извлечен.
	 * @return Идентификаторът на съобщението.
	 * @throws MessagingException
	 *             Грешка при извличане на идентификатора.
	 */
	private long getMessageId(Message message) throws MessagingException {
		long messageId = -1;
		Folder folder = message.getFolder();

		// Само папка, поддържаща концепцията за идентификатор, може да
		// предостави такава информация.
		if (folder instanceof UIDFolder) {
			UIDFolder uidFolder = (UIDFolder) folder;
			messageId = uidFolder.getUID(message);
		}

		return messageId;
	}

	/**
	 * Този метод извършва декодиране на дума при необходимост. Ако не е нужно,
	 * думата не се декодира.
	 *
	 * @param word
	 *            Думата за декодиране, ако е нужно.
	 * @return Думата, във формат подходящ за четене от потребител.
	 * @throws UnsupportedEncodingException
	 *             Грешка при непознат формат на кодиране.
	 * @throws ParseException
	 *             Грешка при декодиране на RFC822 или MIME хедър.
	 */
	private String decodeWord(String word) throws UnsupportedEncodingException, ParseException {
		// Думите, които са кодирани, започват с =?, а след тях следва формата
		// на кодиране.
		if (word != null && word.startsWith("=?")) {
			return MimeUtility.decodeWord(word);
		}

		return word;
	}

	private String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			// TODO handle properly
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO handle properly
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
}
