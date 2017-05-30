package com.iv.dani.mail;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;

import com.iv.dani.mail.data.FolderData;
import com.iv.dani.mail.data.MappedMessageFlag;
import com.iv.dani.mail.data.UserLoginData;

/**
 * Целта на този клас е да предостави улеснение при поискване на определена
 * информация от кутията на потребителя.
 * <p>
 * За да може да се достъпи информацията от кутията на потребителя, необходимо е
 * първо да се извика методът {@link #connect()}. С този метод ще се осъществи
 * връзка с кутията на потребителя. След това могат да бъдат извиквани
 * останалите методи на класа.
 * <p>
 * След приключване на работата с потребителската кутия, нужно е да се затворят
 * всички отворени папки и да се прекрати комуникацията с потребителската кутия.
 * За това е нужно да се извика методът {@link #close()} в края на комуникацията
 * с мейл сървъра.
 * <p>
 * <b>ЗАБЕЛЕЖКА</b>: При подаване на името на папката, необходимо е да се
 * специфицира пълният път до папката. Например, за Gmail: [Gmail]/Trash, а не
 * само Trash; или Work/Important/Secret Project, а не само Secret Project.
 *
 */
public class JavaMailReader implements Closeable {
	private Store _store;
	private List<Folder> _folders = new ArrayList<Folder>();

	/**
	 * Този метод осъществява връзка с кутията на потребителя.
	 *
	 * @throws MessagingException
	 *             Грешка при свързване с потребитлеската кутия.
	 */
	public void connect(UserLoginData userLogin) throws MessagingException {
		// https://www.google.com/settings/security/lesssecureapps
		String storeProtocol = "imaps";

		Properties properties = new Properties();

		Session session = Session.getDefaultInstance(properties);
		_store = session.getStore(storeProtocol);
		_store.connect(
				userLogin.getImapHost(),
				userLogin.getUsername(),
				userLogin.getPassword());
	}

	/**
	 * Този метод дава възможност за прочитане на определен брой мейли, които се
	 * намират на определена страница. Тук понятието "страница" е абстрактно.
	 * Счита се, че имейлите условно са разделени на страници с определн брой
	 * имейли на всяка страница.
	 *
	 * @param folderFullName
	 *            Пълният път до папката, от която да се прочетат мейлите.
	 * @param page
	 *            Номерът на страницата.
	 * @param mailsOnPage
	 *            Броят мейли на всяка една страница.
	 * @return Прочетените мйели на страницата.
	 * @throws MessagingException
	 *             Грешка при прочитане на мейлите от папката или грешка при
	 *             отваряне на самата папка.
	 */
	public Message[] readMailMessages(String folderFullName, int page, int mailsOnPage) throws MessagingException {
		Message[] messages = null;

		// Connect to the email server.
		// Find the folder and open it for reading.
		Folder folder = openFolder(folderFullName, Folder.READ_ONLY);
		int totalMessages = folder.getMessageCount();
		if(totalMessages == 0) {
			return new Message[]{};
		}

		int listedMails = page * mailsOnPage;
		int lastMailToList = listedMails + mailsOnPage;
		if(totalMessages < lastMailToList) {
			lastMailToList = totalMessages;
		}

		messages = folder.getMessages(totalMessages - lastMailToList + 1, totalMessages - listedMails);

		// С цел да се укаже на мейл сървъра, че съобщенията се взимат вкупом,
		// може да се постигне оптимизация и намаляване на времето за извличане
		// на съобщенията посредством профили.
		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.add(FetchProfile.Item.ENVELOPE);
		fetchProfile.add(FetchProfile.Item.FLAGS);

		folder.fetch(messages, fetchProfile);

		return messages;
	}

	/**
	 * Този метод предоставя възможност за прочитане на единичен мейл от папка
	 * на база на неговия идентифиркатор.
	 *
	 * @param folderFullName
	 *            Пълният път до папката, от която да бъде прочетено
	 *            съобщението.
	 * @param messageId
	 *            Идентификаторът на съобщението.
	 * @return Прочетеното съобщение или <code>null</code>, ако съобщение с
	 *         указания идентификатор не е намерено.
	 * @throws MessagingException
	 *             Грешка при прочитане на мейлиа от папката или грешка при
	 *             отваряне на самата папка.
	 */
	public Message readSingleMailMessage(String folderFullName, long messageId) throws MessagingException {
		if (messageId < 0) {
			return null;
		}

		Message message = null;

		Folder folder = openFolder(folderFullName, Folder.READ_WRITE);
		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.add(FetchProfile.Item.ENVELOPE);
		fetchProfile.add(FetchProfile.Item.CONTENT_INFO);

		if (folder instanceof UIDFolder) {
			UIDFolder uidFolder = (UIDFolder) folder;
			message = uidFolder.getMessageByUID(messageId);

			folder.fetch(new Message[] { message }, fetchProfile);
		}

		return message;
	}

	/**
	 * Този метод предоставя възможност за промяна на флаг на последователност
	 * от съобщения, като флагът се указва с името си. Методът приема и
	 * аргумент, който указва дали този флаг да се добави към флаговете на
	 * съобщението или да бъде премахнат от тях.
	 *
	 * @param folderFullName
	 *            Пълният път до папката, където се съхраняват съобщенията.
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
	public void setMessagesFlag(String folderFullName, long[] messagesIds, MappedMessageFlag mappedMessageFlag,
			boolean shouldSetMessageFlag) throws MessagingException {
		// Изискваме флаг да е подаден.
		if (mappedMessageFlag == null) {
			throw new IllegalArgumentException("Message flag name not specified.");
		}

		// Отваряме папката за четене и запис. Тук се отваря и за запис, защото
		// ще се записват флагове на съобщения.
		Folder folder = openFolder(folderFullName, Folder.READ_WRITE);

		// Указваме на сървъра да оптимизира извличането на флагове за
		// съобщенията.
		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.add(FetchProfile.Item.FLAGS);

		// Намират се съобщенията по идентификатор и се задава флага на всяко
		// едно от тях.
		if (folder instanceof UIDFolder) {
			UIDFolder uidFolder = (UIDFolder) folder;
			Message[] messages = uidFolder.getMessagesByUID(messagesIds);
			folder.fetch(messages, fetchProfile);

			for (Message message : messages) {
				try {
					message.setFlag(mappedMessageFlag.getJavaMailFlag(), shouldSetMessageFlag);
				} catch (MessagingException e) {
					//TODO logging
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Този метод създава струкура от данни, която предоставя информация за
	 * основните папки в потребителската кутия - папката за входящи съобщения
	 * INBOX и за базовите папки, които са подпапки на дефолтната служебна
	 * папка.
	 *
	 * @param mailsPerFolderPage
	 *            Броят на мейли в една страница.
	 * @return Информация за базовите папки.
	 * @throws MessagingException
	 *             Грешка при прочитане на информацията за папките в
	 *             потребителската кутия.
	 */
	public List<FolderData> getBaseFoldersData(int mailsPerFolderPage) throws MessagingException {
		// Подредбата на папките в една мейл кутия има форма на дърво.
		// На върха на дървото е служебна дефолтна папка, която съдържа
		// като директни наследници папките от най-високо ниво в кутията.
		// Отделно, всяка папка може да има и подпапки.
		Folder rootFolder = _store.getDefaultFolder();
		Folder[] folders = rootFolder.list("%");

		// Взима се информацията само за входящата кутия и за служебните папки.
		List<FolderData> baseFoldersData = new ArrayList<FolderData>();
		for (Folder folder : folders) {
			FolderData folderData = null;
			if (!folder.getName().equalsIgnoreCase("inbox")) {
				boolean holdsMessages = (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
				if (!holdsMessages) {
					baseFoldersData.addAll(getSubfoldersData(folder, mailsPerFolderPage));
				}
			} else {
				folderData = getFolderData(folder, mailsPerFolderPage);
			}

			if (folderData != null) {
				baseFoldersData.add(folderData);
			}
		}

		return baseFoldersData;
	}

	@Override
	public void close() {
		try {
			closeFolders();

			if (_store != null && _store.isConnected()) {
				_store.close();
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Този метод извлича информация за подпапките на указаната папка.
	 *
	 * @param baseFolder
	 *            Папката, чиято подпапки да бъдат обходени.
	 * @param mailsPerFolderPage
	 *            Броят мейли на една страница от папката.
	 * @return Информация за подпапките на указаната папка.
	 * @throws MessagingException
	 *             Грешка при прочитане на информацията за папките в
	 *             потребителската кутия.
	 */
	private List<FolderData> getSubfoldersData(Folder baseFolder, int mailsPerFolderPage) throws MessagingException {
		Folder[] folders = baseFolder.list("%");
		List<FolderData> baseFoldersData = new ArrayList<FolderData>();

		for (Folder folder : folders) {
			FolderData folderData = getFolderData(folder, mailsPerFolderPage);

			if (folderData != null) {
				baseFoldersData.add(folderData);
			}
		}

		return baseFoldersData;
	}

	/**
	 * Този метод извлича информация за дадена папка. Ако папката не съществува,
	 * информация не се ивзлича.
	 *
	 * @param folder
	 *            Папката, чиято информация да снема.
	 * @param mailsPerFolderPage
	 *            Броят мейли на една страница във всяка папка.
	 * @return Информацията, снета за съответната папка.
	 * @throws MessagingException
	 *             Грешка при прочитане на информацията за папката в
	 *             потребителската кутия.
	 */
	private FolderData getFolderData(Folder folder, int mailsPerFolderPage) throws MessagingException {
		FolderData folderData = null;
		if (folder.exists()) {
			// Има 2 типа папки - такива, които съдържат имейли и папки и
			// такива, които съдържат само други папки. Определена
			// информация е достъпна само за папки, които съдържат в себе си
			// мейли, но не и за другия вид папки.
			boolean holdsMessages = (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
			folderData = new FolderData(folder.getName(), folder.getFullName());
			if (holdsMessages) {
				folderData.setMailsPerPage(mailsPerFolderPage);
				folderData.setTotalMessagesCount(folder.getMessageCount());
				folderData.setUnreadMessagesCount(folder.getUnreadMessageCount());
			}
		}

		return folderData;
	}

	private Folder openFolder(String folderFullName, int mode) throws MessagingException {
		Folder folder = _store.getFolder(folderFullName);
		folder.open(mode);
		_folders.add(folder);

		return folder;
	}

	private void closeFolders() throws MessagingException {
		for(Folder folder : _folders) {
			if (folder != null && folder.isOpen()) {
				folder.close(false);
				folder = null;
			}
		}
	}
}