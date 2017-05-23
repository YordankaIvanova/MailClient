package com.iv.dani.mail.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Този клас е отговорен за предоставяне на методи, чрез които да се създават
 * HTTP отговори. Освен това, този клас дефинира някои общи константи при
 * комуникация между web клиента и Java клиента.
 *
 */
@Component
public class HttpUtils {
	public static final String USER_TOKEN_HTTP_HEADER_NAME = "X-User-Token";

	private static final String MESSAGE_TEMPLATE = "{\"message\":\"%s\"}";
	private static final String ERROR_MESSAGE_TEMPLATE = "{\"error\":\"%s\"}";

	/**
	 * Този метод създаваа HTTP отговор със статус 200, в тялото на когото се
	 * съдържа изпращаната информация.
	 *
	 * @param responseBody
	 *            Тялото на HTTP отговора.
	 * @return HTTP отговор със статус 200.
	 */
	public ResponseEntity<String> createSuccessPlainTextResponse(String responseBody) {
		return createResponse(responseBody,	HttpStatus.OK);
	}

	/**
	 * Този метод създава HTTP отговор с указания статус, който връща JSON обект
	 * с единствен атрибут "message", в който се съдържа тялото на съобщение.
	 *
	 * @param messageBody
	 *            Съобщението, което да се добави в отговора.
	 * @param httpStatus
	 *            Статусът на HTTP отговора.
	 * @return HTTP отговор, който съдържа JSON съобщение.
	 */
	public ResponseEntity<String> createMessagePlainTextResponse(String messageBody, HttpStatus httpStatus) {
		return createResponse(
				String.format(MESSAGE_TEMPLATE, messageBody),
				httpStatus);
	}

	/**
	 * Този метод създава HTTP отговор с указания статус, който връща JSON обект
	 * с единствен атрибут "error", в който се съдържа тялото на съобщение за
	 * грешка.
	 *
	 * @param errorBody
	 *            Съобщението, което да се добави в отговора.
	 * @param httpStatus
	 *            Статусът на HTTP отговора.
	 * @return HTTP отговор, който съдържа JSON съобщение.
	 */
	public ResponseEntity<String> createErrorPlainTextResponse(String errorBody, HttpStatus httpStatus) {
		return createResponse(
				String.format(ERROR_MESSAGE_TEMPLATE, errorBody),
				httpStatus);
	}

	private ResponseEntity<String> createResponse(String body, HttpStatus httpStatus) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/plain");

		return new ResponseEntity<String>(
				body,
				headers,
				httpStatus);
	}
}
