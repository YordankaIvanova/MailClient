/*JavaScript and jQuery*/

function getMailContent() {
	var id = getParameterByName("id");
	
	var data = sessionStorage.getItem(id);
	var jsonContentMail = $.parseJSON(data);

	var mailContent = document.getElementById("mail_content");
	mailContent.innerHTML = "";
	createMailView(mailContent, jsonContentMail);
}

function createMailView(mailContent, mail) {
	var sentDate = new Date();
	sentDate.setTime(mail.sentDate);

	var receivedDate = new Date();
	receivedDate.setTime(mail.receivedDate);

	var html = "";
	html += '<table class="mail_table">';
	html +=	'<tr><th colspan="4">'+ mail.subject + '</th></tr>';
	html +=	'<tr><td class="label fromlabel"> From: </td>';
	html +=	'<td colspan="2"><p id="name_from">' + mail.from[0].personal + '</p>';
	html += '<p>' + formatMail(mail.from[0].address) + '</p></td>';
	html += '<td class="sentdate"><span class="datelabel"> Sent: </span>' + getDateFormat(sentDate,true) + '</td></tr>';
	html += '<tr><td class="label"> To: </td><td>' + check(mail.to) + '</td>';
	html +=	'<td class="receiveddate" colspan="2"><span class="datelabel"> Received: </span>' + getDateFormat(receivedDate,true) + '</td></tr>';

	var ccMails = check(mail.cc);
	if(ccMails != null) {
		html +=	'<tr><td class="label"> Cc: </td><td>' + ccMails + '</td></tr>';
	}

	var bccMails = check(mail.bcc);
	if(bccMails != null) {
		html +=	'<tr><td class="label"> Bcc: </td><td>' + bccMails +'</td></tr>';
	}

	html += '</table>';
	html += '<div class="mail_body">' + mail.content + '</div>';

	mailContent.innerHTML += html;
	var styleTags = mailContent.getElementsByTagName("style");
	for(var i = 0; i < styleTags.length; i++) {
		styleTags[i].innerHTML = "";
	}
}

function check(recipients) {
	if(recipients == null){
		return null;
	}

	var str = "";
	for(i=0; i< recipients.length;i++){
		str += formatMail(recipients[i].address);
		if(i != recipients.length - 1){
			str += ", ";
		}
	}

	return str;
}

function formatMail(mailAddress) {
	return "&lt;" + mailAddress + "&gt;";
}