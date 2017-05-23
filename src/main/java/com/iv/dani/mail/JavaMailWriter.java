package com.iv.dani.mail;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Component;

import com.iv.dani.mail.data.MailMessage;
import com.iv.dani.mail.data.UserLoginData;

//TODO javadoc and logging
@Component
public class JavaMailWriter {
	// FIXME: remove
	private static final UserLoginData DEFAULT_USER =
			new UserLoginData(
					"miroslav.shtarbev@gmail.com",
					"miro17protoBG",
					"imap.gmail.com",
					"smtp.gmail.com");

	public void sendMessage(UserLoginData userLoginData, MailMessage message) throws MessagingException {
		if (userLoginData == null) {
			userLoginData = DEFAULT_USER;
		}

		Session mailConnection = Session.getInstance(new Properties());
		MimeMessage emailMessage = new MimeMessage(mailConnection);

		Address sender = new InternetAddress(userLoginData.getUsername());
		Address recepient = new InternetAddress(message.getTo());

		emailMessage.setFrom(sender);
		emailMessage.setRecipient(Message.RecipientType.TO, recepient);
		emailMessage.setRecipients(Message.RecipientType.CC, getCcAddressees(message.getCc()));
		emailMessage.setSubject(message.getMailSubject());
		emailMessage.setText(message.getMailContent(), "utf-8", "html");

		Transport transport = null;
		try {
			transport = mailConnection.getTransport("smtps");
			transport.connect(userLoginData.getSmtpHost(), DEFAULT_USER.getUsername(), DEFAULT_USER.getPassword());
			transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
		} finally {
			if (transport != null) {
				transport.close();
			}
		}
	}

	private Address[] getCcAddressees(String ccAddressees) throws AddressException {
		if (ccAddressees.isEmpty()) {
			return null;
		}

		String[] ccEmailAddressNames = ccAddressees.split(",");
		Address[] ccEmailAddresses = null;

		if (ccEmailAddressNames.length == 0) {
			ccEmailAddresses = new Address[] { new InternetAddress(ccAddressees) };
		} else {
			ccEmailAddresses = new InternetAddress[ccEmailAddressNames.length];
			for (int i = 0; i < ccEmailAddressNames.length; i++) {
				ccEmailAddresses[i] = new InternetAddress(ccEmailAddressNames[i]);
			}
		}

		return ccEmailAddresses;
	}
}
