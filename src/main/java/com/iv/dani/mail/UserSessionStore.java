package com.iv.dani.mail;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.iv.dani.mail.data.UserLoginData;

/**
 * Този клас предоставя способи за съхранение на въведената потребителска
 * инфромация и издаването на токен, когато информацията се съхрани.
 *
 */
@Component
public class UserSessionStore {
	private static final int TTL_DELAY = 1 * 60 * 1000; // 1min
	private static final int TOKEN_TTL = 15;

	private Map<String, UserSession> _userSessionStore = new ConcurrentHashMap<String, UserSession>();

	/**
	 * Методът съхранява потребителската информация и издава токен, чрез който
	 * тази информация може да се идентифицира.
	 *
	 * @param userLoginData
	 *            Потребителската информация при автентикиране.
	 * @return Токен, чрез който се идентифицира потребителската информация.
	 */
	public synchronized String createUserSession(UserLoginData userLoginData) {
		String newUserToken = requestToken();
		UserSession userSession = new UserSession(userLoginData);
		_userSessionStore.put(newUserToken, userSession);

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
	public synchronized UserLoginData getUserSession(String userToken) {
		UserSession userSession = _userSessionStore.get(userToken);
		if (userSession == null) {
			throw new IllegalStateException("User token not found.");
		}

		userSession.resetTtl();
		return userSession.getUserLoginData();
	}

	/**
	 * Този клас премахва съхранената потребителска информация, която се
	 * идентифицира с подадения като аргумент токен.
	 *
	 * @param userToken
	 *            Потребителският токен.
	 */
	public synchronized void removeUserSession(String userToken) throws IllegalStateException {
		UserSession userSession = _userSessionStore.remove(userToken);
		if (userSession == null) {
			throw new IllegalStateException("User token not found.");
		}
	}

	private String requestToken() {
		return UUID.randomUUID().toString();
	}

	@Scheduled(fixedDelay = TTL_DELAY)
	private void checkForExpiredTokens() {
		for (Map.Entry<String, UserSession> sessionData : _userSessionStore.entrySet()) {
			String token = sessionData.getKey();
			UserSession userSession = sessionData.getValue();

			userSession.diminishTtl();
			if (userSession.isExpired()) {
				removeUserSession(token);
			}
		}
	}

	private static final class UserSession {
		private final UserLoginData _userLoginData;
		private int _ttl;

		public UserSession(UserLoginData userLoginData) {
			_userLoginData = userLoginData;
			resetTtl();
		}

		public UserLoginData getUserLoginData() {
			return _userLoginData;
		}

		public void diminishTtl() {
			_ttl--;
		}

		public void resetTtl() {
			_ttl = TOKEN_TTL;
		}

		public boolean isExpired() {
			return _ttl == 0;
		}
	}
}
