package com.iv.dani.mail;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;
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

@Component
public class MailFormatter {

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

		messageAsJson = objectMapper.writeValueAsString(mailMessage);

		return messageAsJson;
	}

	public String getMailMessagesAsJson(Message[] messages)
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
			mailMessage.put("flags", getMailMessageFlags(messages[i]));
			jsonMessages[i] = objectMapper.writeValueAsString(mailMessage);
		}

		String messagesAsJsonArray = objectMapper.writeValueAsString(jsonMessages);

		return messagesAsJsonArray;
	}

	private String acquirePartContent(Part p) throws IOException, MessagingException {
		StringBuilder partContent = new StringBuilder();
		// check if the content is plain text
		if (p.isMimeType("text/*")) {
			partContent.append((String) p.getContent());
		}
		else if (p.isMimeType("multipart/alternative")) {
			Multipart mp = (Multipart) p.getContent();
			int count = mp.getCount();
			for (int i = count - 1; i >= 0; i++) {
				String messageContents = acquirePartContent(mp.getBodyPart(i));
				if(messageContents != null) {
					partContent.append(messageContents);
					break;
				}
			}
		} else if(p.isMimeType("multipart/mixed")) {
			Multipart mp = (Multipart) p.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++) {
				partContent.append(acquirePartContent(mp.getBodyPart(i)));
			}
		}
		// check if the content is a nested message
		else if (p.isMimeType("message/rfc822")) {
			partContent.append((Message) p.getContent());
		}
		// check if the content is an inline image
		else if (p.isMimeType("image/*")) {
			System.out.println("--------> image/jpeg");
			Object o = p.getContent();

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
			Object o = p.getContent();
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

	private long getMessageId(Message message) throws MessagingException {
		long messageId = -1;
		Folder folder = message.getFolder();
		if (folder instanceof UIDFolder) {
			UIDFolder uidFolder = (UIDFolder) folder;
			messageId = uidFolder.getUID(message);
		}

		return messageId;
	}

	private List<String> getMailMessageFlags(Message message) throws MessagingException {
		List<String> flags = new ArrayList<String>();

		if (message.isSet(Flags.Flag.SEEN)) {
			flags.add("Seen");
		}
		// Check if the message is recent
		if (message.isSet(Flags.Flag.RECENT)) {
			flags.add("Recent");
		}
		// Check if the message has been removed
		if (message.isSet(Flags.Flag.DELETED)) {
			flags.add("Deleted");
		}
		// Check whether the message has been answered
		if (message.isSet(Flags.Flag.ANSWERED)) {
			flags.add("Answered");
		}
		// Check if the message is a draft
		if (message.isSet(Flags.Flag.DRAFT)) {
			flags.add("Draft");
		}
		// Check if the message was marked
		if (message.isSet(Flags.Flag.FLAGGED)) {
			flags.add("Marked");
		}

		return flags;
	}

	private String decodeWord(String word) throws UnsupportedEncodingException, ParseException {
		if (word.startsWith("=?")) {
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
