var remoteServer = {
	url: "http://localhost:8081/"
};

function getMailContent() {
	var id = getParameterByName("id");
	var folderName = getParameterByName("folderName");;
	
	$.ajax({
		url: remoteServer.url + "/mail?folderName=" + folderName + "&id=" + id,
		type: "GET",
		dataType: "text",
		success: function(result) {
			generateTemplate(result);
		}
	});
}

function getParameterByName(name, url) {
    if (!url) {
      url = window.location.href;
    }
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function generateTemplate(data) {
	var jsonContentMail = $.parseJSON(data);
	
	var mailContent = document.getElementById("mail_table");
	insertInTable(mailContent, jsonContentMail);
}

function insertInTable(mailContent, mail) {
	/*first row- subject*/
	document.getElementById("subject").innerHTML = mail.subject;
	
	/*second row- from*/
	document.getElementById("name_from").innerHTML = mail.from[0].personal;
	document.getElementById("from_email").innerHTML = mail.from[0].address;
	var date = new Date();
	date.setTime(mail.sentDate);
	document.getElementById("sentdate").innerHTML = date.toUTCString();
	
	/*third row- to*/
	document.getElementById("recipient_email").innerHTML = check(mail.to);
	var date = new Date();
	date.setTime(mail.receivedDate);
	document.getElementById("receiveddate").innerHTML = date.toUTCString();

	document.getElementById("cc_email").innerHTML = check(mail.cc);
	
	document.getElementById("bcc_email").innerHTML = check(mail.bcc);
	
	/*the content
	var tableRow = mailContent.insertRow();
	var tableCell = tableRow.insertCell(0);
	var mail = document.createElement("div");
	mail.id = "content";
	tableCell.appendChild(mail); */
}

function check(recipients) {
	if(recipients == null){ 
		return "";
	}
	
	var str = "";
	for(i=0; i< recipients.length;i++){
		str += recipients[i].address;
		if(i != recipients.length - 1){
			str += ", ";
		}
	}
	
	return str;
}