package com.iv.dani.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Този клас предоставя общи константи, които кореспондират на флаг на
 * съобщение, както и методи, с които да се извлече кой флаг е активен за
 * съобщение.
 */
public enum MappedMessageFlag {
	SEEN_MAIL("Seen", Flags.Flag.SEEN),
	RECENT_MAIL("Recent", Flags.Flag.RECENT),
	DELETED_MAIL("Deleted", Flags.Flag.DELETED),
	ANSWERED_MAIL("Answered", Flags.Flag.ANSWERED),
	DRAFT_MAIL("Draft", Flags.Flag.DRAFT),
	MARKED_MAIL("Marked", Flags.Flag.FLAGGED);

	private String _flagName;
	private Flags.Flag _javaMailFlag;

	private MappedMessageFlag(String flagName, Flags.Flag javaMailFlag) {
		_flagName = flagName;
		_javaMailFlag = javaMailFlag;
	}

	/**
	 * Този метод извършва проверка на флаговете на съобщението и връща списък с
	 * активните за съобщението флагове.
	 *
	 * @param message
	 *            Съобщението, чието флагове да бъдат проверени.
	 * @return Списък с активните за съобщението флагове.
	 * @throws MessagingException
	 *             Грешка, при извличане на елемент от съобщението.
	 */
	public static List<String> getMailMessageMappedFlags(Message message) throws MessagingException {
		List<String> flags = new ArrayList<String>();

		// Преминава се през всеки един елемент на енумерацията и се проверява
		// дали JavaMail флагът, който се съдържа в обект от енумерацията, е
		// активен за проверяваното съобщение.
		for(MappedMessageFlag currentMappedFlag : MappedMessageFlag.values()) {
			if(message.isSet(currentMappedFlag.getJavaMailFlag())) {
				flags.add(currentMappedFlag.toString());
			}
		}

		return flags;
	}

	/**
	 * Този метод открива флаг по името му.
	 *
	 * @param mappedFlagName
	 *            Името на флага, който се търси.
	 * @return Откритият флаг или <code>null</code>, ако флаг с указаното име не
	 *         е намерен.
	 */
	public static MappedMessageFlag getMappedMessageFlag(String mappedFlagName) {
		MappedMessageFlag mappedFlag = null;

		for(MappedMessageFlag currentMappedFlag : MappedMessageFlag.values()) {
			if(currentMappedFlag.toString().equals(mappedFlagName)) {
				mappedFlag = currentMappedFlag;
				break;
			}
		}

		return mappedFlag;
	}

	/**
	 * Този метод преминава през флаговете на едно съобщение и създава
	 * хештаблица, в която срещу всеки флаг е указано дали е зададен или не.
	 *
	 * @param message
	 *            Съобщението, чието флагове да се обходят.
	 * @return Хештаблица с флаговете и тяхното състояние - зададен или не.
	 */
	/*
	 * Методът ще създаде таблицата ето така (това е само илюстративен пример):
	 * "seen" : false
	 * "recent" : true
	 * "deleted" : false
	 * "answered" : false
	 * ...
	 */
	public static Map<String, Boolean> getMappedMessageFlagsAsProperties(Message message) {
		Map<String, Boolean> flagsAsProperties = new HashMap<String, Boolean>();

		for(MappedMessageFlag currentMappedFlag : MappedMessageFlag.values()) {
			String flagName = currentMappedFlag.toString().toLowerCase();
			Boolean flagState = false;
			try {
				flagState = message.isSet(currentMappedFlag.getJavaMailFlag());
			} catch (MessagingException e) {
				// Ако флагът не може да се намери в съобщението, предполагаме,
				// че го няма.
			}

			flagsAsProperties.put(flagName, flagState);
		}

		return flagsAsProperties;
	}

	/**
	 * Този метод връща флагът на съобщение, но така, както е представен в JavaMail библиотеката.
	 *
	 * @return JavaMail флаг за едно съобщение.
	 */
	public Flags.Flag getJavaMailFlag() {
		return _javaMailFlag;
	}

	@Override
	public String toString() {
		return _flagName;
	}
}
