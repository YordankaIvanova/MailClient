package com.iv.dani.mail.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MailMessage {
	private final String _cc, _to, _mailsubject, _mailcontent;

	@JsonCreator
	public MailMessage(
			 @JsonProperty("cc") String cc,
			 @JsonProperty("to") String to,
			 @JsonProperty("mailsubject") String mailsubject,
			 @JsonProperty("mailcontent") String mailcontent) {
		_cc = cc;
		_to = to;
		_mailsubject = mailsubject;
		_mailcontent = mailcontent;
	}

	public String getCc() {
		return _cc;
	}

	public String getTo() {
		return _to;
	}

	public String getMailContent() {
		return _mailcontent;
	}

	public String getMailSubject() {
		return _mailsubject;
	}
}
