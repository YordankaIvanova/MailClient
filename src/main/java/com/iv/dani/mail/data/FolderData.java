package com.iv.dani.mail.data;

/**
 * Този клас съдържа в себе си определна информация за една папка от една мейл
 * кутия. Тази информация ще се предоставя за четене от потребителския
 * интерфейс.
 *
 */
public class FolderData {
	private String _folderName, _folderFullName;
	private int _unreadMessagesCount, _totalMessagesCount, _totalViewingPages, _mailsPerPage;

	/**
	 * Създава обект, който съдържа миимум необходимата информация за
	 * избразяване на една папка.
	 *
	 * @param folderName
	 *            Името на папката.
	 * @param folderFullName
	 *            Пълният път на папката.
	 */
	public FolderData(String folderName, String folderFullName) {
		_folderName = folderName;
		_folderFullName = folderFullName;
	}

	/**
	 * Методът задава колко мейла има на една страница.
	 *
	 * @param mailsPerPage
	 *            Броят мейли на една страница.
	 */
	public void setMailsPerPage(int mailsPerPage) {
		_mailsPerPage = mailsPerPage;
	}

	/**
	 * Методът задава броят на непрочетените съобщения в папката.
	 *
	 * @param unreadMessagesCount
	 *            Броят на непрочетените съобщения.
	 */
	public void setUnreadMessagesCount(int unreadMessagesCount) {
		_unreadMessagesCount = unreadMessagesCount;
	}

	/**
	 * Методът задава колко е целият брой на съобщенията, които се намират в
	 * папката.
	 *
	 * @param totalMessagesCount
	 *            Броят на всички съобщения в папката.
	 */
	public void setTotalMessagesCount(int totalMessagesCount) {
		_totalMessagesCount = totalMessagesCount;
	}

	/**
	 * Методът извлича името на текущата папка.
	 *
	 * @return Името на папката.
	 */
	public String getFolderName() {
		return _folderName;
	}

	/**
	 * Методът извлича пълния път на папка.
	 *
	 * @return Пълният път на папка.
	 */
	public String getFolderFullName() {
		return _folderFullName;
	}

	/**
	 * Методът извлича броя на непрочетените в папката писма.
	 *
	 * @return Броят на непрочетените съобщения.
	 */
	public int getUnreadMessageCount() {
		return _unreadMessagesCount;
	}

	/**
	 * Методът извлича колко е броят на всички писма в папката.
	 *
	 * @return Броят на съобщенията в папката.
	 */
	public int getTotalMessageCount() {
		return _totalMessagesCount;
	}

	/**
	 * Методър връща броя на съобщенията, които се показват на една страница при
	 * преглеждане на мейлите в папката.
	 *
	 * @return Броят на мейлите на една страница.
	 */
	public int getMailsPerPage() {
		return _mailsPerPage;
	}

	/**
	 * Методър връща колко е броят на страниците, на които се разделят мейлите в
	 * него.
	 *
	 * @return Страниците за преглеждане на съобщения.
	 */
	public int getTotalViewingPages() {
		_totalViewingPages = _totalMessagesCount / _mailsPerPage;
		if (_totalMessagesCount % _mailsPerPage != 0) {
			_totalViewingPages++;
		}

		return _totalViewingPages;
	}
}
