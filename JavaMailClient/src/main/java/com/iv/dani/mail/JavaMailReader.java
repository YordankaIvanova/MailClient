package com.iv.dani.mail;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;

public class JavaMailReader implements Closeable {
	private Store _store;
	private Folder _folder;

	public void connect() throws MessagingException {
		// https://www.google.com/settings/security/lesssecureapps
		String host = "imap.gmail.com";
		String username = "miroslav.shtarbev@gmail.com";
		String password = "miro17protoBG";
		String storeProtocol = "imaps";

		Properties properties = new Properties();

		Session session = Session.getDefaultInstance(properties);
		_store = session.getStore(storeProtocol);
		_store.connect(host, username, password);
	}

	public Message[] readMailMessages(String folderName) throws UnsupportedEncodingException, MessagingException {
		Message[] messages = null;
		
		try {
			// Connect to the email server.
			// Find the inbox and open it for reading.
			_folder = _store.getFolder(folderName);
			_folder.open(Folder.READ_ONLY);
			int totalMessages = _folder.getMessageCount();

			// Get the first ten messages and display them.
			messages = _folder.getMessages(totalMessages - 20, totalMessages);
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return messages;
	}
	
	public Message getMessageFromFolderById(String folderName, long messageId) throws MessagingException {
		if(messageId < 0) {
			return null;
		}
		
		Message message = null;
		
		_folder = _store.getFolder(folderName);
		_folder.open(Folder.READ_ONLY);
		if(_folder instanceof UIDFolder) {
			UIDFolder uidFolder = (UIDFolder) _folder;
			message = uidFolder.getMessageByUID(messageId);
		}
		
		return message;
	}

	public void close() {
		try {
			if (_folder != null) {
				_folder.close(false);
				_folder = null;
			}
			
			if (_store != null) {
				_store.close();
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}