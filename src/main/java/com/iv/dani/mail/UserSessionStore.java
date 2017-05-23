package com.iv.dani.mail;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.iv.dani.mail.data.UserLoginData;

/**
 * Този клас предоставя способи за съхранение на въведената потребителска
 * инфромация и издаването на токен, когато информацията се съхрани.
 *
 */
@Component
public class UserSessionStore {
	private Map<String, UserLoginData> _userSessionStore = new ConcurrentHashMap<String, UserLoginData>();

	/**
	 * Методът съхранява потребителската информация и издава токен, чрез който
	 * тази информация може да се идентифицира.
	 *
	 * @param userLoginData
	 *            Потребителската информация при автентикиране.
	 * @return Токен, чрез който се идентифицира потребителската информация.
	 */
	public String createUserSession(UserLoginData userLoginData) {
		String newUserToken = requestToken();
		_userSessionStore.put(newUserToken, userLoginData);

		return newUserToken;
	}

	/**
	 * Този метод връща потребителската информация при автентикиране на база на
	 * подадения токен.
	 *
	 * @param userToken
	 *            Потребителският токен.
	 * @return Потребителската информация при автенктикиране.
	 */
	public UserLoginData getUserSession(String userToken) {
		return _userSessionStore.get(userToken);
	}

	/**
	 * Този клас премахва съхранената потребителска информация, която се
	 * идентифицира с подадения като аргумент токен.
	 *
	 * @param userToken
	 *            Потребителският токен.
	 * @throws IllegalStateException
	 *             Ако не е намерена потребителска информация, на която
	 *             съответства токена.
	 */
	public void removeUserSession(String userToken) throws IllegalStateException {
		UserLoginData userLoginData = _userSessionStore.remove(userToken);
		if (userLoginData == null) {
			throw new IllegalStateException("No such user exists.");
		}
	}

	private String requestToken() {
		return UUID.randomUUID().toString();
	}
}
