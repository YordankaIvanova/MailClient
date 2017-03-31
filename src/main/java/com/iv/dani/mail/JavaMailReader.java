package com.iv.dani.mail;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
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

	public Message[] readMailMessages(String folderName, int page, int mailsOnPage)
			throws UnsupportedEncodingException, MessagingException {
		Message[] messages = null;

		// Connect to the email server.
		// Find the inbox and open it for reading.
		_folder = _store.getFolder(folderName);
		_folder.open(Folder.READ_ONLY);
		int totalMessages = _folder.getMessageCount();

		int listedMails = page * mailsOnPage;
		int lastMailToList = listedMails + mailsOnPage;
		messages = _folder.getMessages(totalMessages - lastMailToList + 1, totalMessages - listedMails);

		// С цел да се укаже на мейл сървъра, че съобщенията се взимат вкупом,
		// може да се постигне оптимизация и намаляване на времето за извличане
		// на съобщенията посредством профили.
		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.add(FetchProfile.Item.ENVELOPE);
		fetchProfile.add(FetchProfile.Item.FLAGS);

		_folder.fetch(messages, fetchProfile);

		return messages;
	}

	public Message readSingleMailMessage(String folderName, long messageId) throws MessagingException {
		if (messageId < 0) {
			return null;
		}

		Message message = null;

		_folder = _store.getFolder(folderName);
		_folder.open(Folder.READ_WRITE);

		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.add(FetchProfile.Item.ENVELOPE);
		fetchProfile.add(FetchProfile.Item.CONTENT_INFO);

		if (_folder instanceof UIDFolder) {
			UIDFolder uidFolder = (UIDFolder) _folder;
			message = uidFolder.getMessageByUID(messageId);

			_folder.fetch(new Message[] {message}, fetchProfile);
		}

		return message;
	}

	/**
	 * Този метод предоставя възможност за промяна на флаг на последователност
	 * от съобщения, като флагът се указва с името си. Методът приема и
	 * аргумент, който указва дали този флаг да се добави към флаговете на
	 * съобщението или да бъде премахнат от тях.
	 *
	 * @param folderName
	 *            Името на папката, където се съхраняват съобщенията.
	 * @param messagesIds
	 *            Идентификаторите на съобщенията.
	 * @param mappedMessageFlag
	 *            Константата, която показва кой е флагът.
	 * @param shouldSetMessageFlag
	 *            Указва дали флагът да се зададе или премахне.
	 * @throws MessagingException
	 *             Грешка при отваряне на папката, прочитане на съдържанието на
	 *             папката или грешка при задаване на флаг на съобщението.
	 */
	public void setMessagesFlag(String folderName, long[] messagesIds, MappedMessageFlag mappedMessageFlag,
			boolean shouldSetMessageFlag) throws MessagingException {
		// Изискваме флаг да е подаден.
		if (mappedMessageFlag == null) {
			throw new IllegalArgumentException("Message flag name not specified.");
		}

		// Отваряме папката за четене и запис. Тук се отваря и за запис, защото
		// ще се записват флагове на съобщения.
		_folder = _store.getFolder(folderName);
		_folder.open(Folder.READ_WRITE);

		// Указваме на сървъра да оптимизира извличането на флагове за
		// съобщенията.
		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.add(FetchProfile.Item.FLAGS);

		// Намират се съобщенията по идентификатор и се задава флага на всяко
		// едно от тях.
		if (_folder instanceof UIDFolder) {
			UIDFolder uidFolder = (UIDFolder) _folder;
			Message[] messages = uidFolder.getMessagesByUID(messagesIds);
			_folder.fetch(messages, fetchProfile);

			for (Message message : messages) {
				message.setFlag(mappedMessageFlag.getJavaMailFlag(), shouldSetMessageFlag);
			}
		}
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