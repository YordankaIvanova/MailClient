package com.iv.dani.mail;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MailFormatter {

	public String getMailMessageAsJson(Message message) throws JsonProcessingException, MessagingException {
		if (message == null) {
			return null;
		}
		
		String messageAsJson = null;
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> mailMessage = new HashMap<String, Object>();
		mailMessage.put("from", message.getFrom());
		mailMessage.put("to", message.getRecipients(RecipientType.TO));
		mailMessage.put("cc", message.getRecipients(RecipientType.CC));
		mailMessage.put("bcc", message.getRecipients(RecipientType.BCC));
		mailMessage.put("subject", message.getSubject());
		mailMessage.put("sentDate", message.getSentDate().getTime());
		mailMessage.put("receivedDate", message.getReceivedDate().getTime());
		mailMessage.put("content", "");
		// mailMessage.put("content", message.getContent());

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
}
