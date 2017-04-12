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
	private Folder _folder;

	/**
	 * Този метод осъществява връзка с кутията на потребителя.
	 *
	 * @throws MessagingException
	 *             Грешка при свързване с потребитлеската кутия.
	 */
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
		// Find the inbox and open it for reading.
		_folder = _store.getFolder(folderFullName);
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

		_folder = _store.getFolder(folderFullName);
		_folder.open(Folder.READ_WRITE);

		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.add(FetchProfile.Item.ENVELOPE);
		fetchProfile.add(FetchProfile.Item.CONTENT_INFO);

		if (_folder instanceof UIDFolder) {
			UIDFolder uidFolder = (UIDFolder) _folder;
			message = uidFolder.getMessageByUID(messageId);

			_folder.fetch(new Message[] { message }, fetchProfile);
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
		_folder = _store.getFolder(folderFullName);
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

	/**
	 * Този метод създава струкура от данни, която предоставя информация за
	 * подпапките на указаната папка.
	 *
	 * @парам folderName Името на папката, чиито подпапки да бъдат обходени.
	 * @param mailsPerFolderPage
	 *            Броят на мейли в една страница.
	 * @return Информация за подпапките в текущата папка.
	 * @throws MessagingException
	 *             Грешка при прочитане на информацията за папките в
	 *             потребителската кутия.
	 */
	public List<FolderData> getFoldersData(String folderName, int mailsPerFolderPage) throws MessagingException {

		// Подредбата на папките в една мейл кутия има форма на дърво.
		// На върха на дървото е служебна дефолтна папка, която съдържа
		// като директни наследници папките от най-високо ниво в кутията.
		// Отделно, всяка папка може да има и подпапки.
		Folder rootFolder = null;
		if (folderName == null || folderName.isEmpty()) {
			rootFolder = _store.getDefaultFolder();
		} else {
			rootFolder = _store.getFolder(folderName);
		}

		return traverseFolderHierarchy(rootFolder, mailsPerFolderPage);
	}

	/**
	 * Този метод извлича информация за подпапките на дадена папка. Има служебни
	 * папки, които не съдържат мейли а само други папки. В такъв случай се
	 * извличат и техните подпапки.
	 *
	 * @param folder
	 *            Папката, чийто подпапки да бъдат обходени.
	 * @param mailsPerFolderPage
	 *            Броят мейли на една страница във всяка папка.
	 * @return Йерархичната структура на подпапките на указаната папка.
	 * @throws MessagingException
	 *             Грешка при прочитане на информацията за папките в
	 *             потребителската кутия.
	 */
	private List<FolderData> traverseFolderHierarchy(Folder rootFolder, int mailsPerFolderPage) throws MessagingException {
		// Вземат се директните наследници на папката, т.е. подпапките на
		// текущата папка.
		Folder[] folders = rootFolder.list("%");

		List<FolderData> foldersData = new ArrayList<FolderData>();

		for(Folder folder : folders) {
			if (folder.exists()) {
				// Има 2 типа папки - такива, които съдържат имейли и папки и
				// такива, които съдържат само други папки. Определена
				// информация е достъпна само за папки, които съдържат в себе си
				// имейли, но не и за другия вид папки.
				boolean holdsMessages = (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
				FolderData folderData = new FolderData(folder.getName(), folder.getFullName(), holdsMessages);
				if (holdsMessages) {
					folderData.setMailsPerPage(mailsPerFolderPage);
					folderData.setTotalMessagesCount(folder.getMessageCount());
					folderData.setUnreadMessagesCount(folder.getUnreadMessageCount());
				} else {
					folderData.setSubfolders(traverseFolderHierarchy(folder, mailsPerFolderPage));
				}

				foldersData.add(folderData);
			}
		}

		return foldersData;
	}

	@Override
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