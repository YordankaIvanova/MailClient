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
	
	var mailContent = document.getElementById("content_table");
	createMailTemplate(mailContent, jsonContentMail);
}

function createMailTemplate(mailContent, mail) {
	/*mailContent.innerHTML = "";*/
	
	/*first row- subject*/
	var tableRow = mailContent.insertRow();
	tableCell = tableRow.insertCell(0);
	tableCell.innerHTML = mail.subject;
	
	/*second row- from*/
	var tableRow = mailContent.insertRow();
	var tableCell = tableRow.insertCell(0);
	tableCell.innerHTML = "From:";
	
	var tableCell = tableRow.insertCell(1);
	tableCell.innerHTML = mail.from[0].personal;
	
	var tableCell = tableRow.insertCell(2);
	tableCell.innerHTML = mail.from[0].address;
	
	var date = new Date();
	date.setTime(mail.sentDate);
	tableCell = tableRow.insertCell(3);
	tableCell.innerHTML = date.toUTCString();
	
	/*third row- to*/
	var tableRow = mailContent.insertRow();
	var tableCell = tableRow.insertCell(0);
	tableCell.innerHTML = "To:";
	
	var tableCell = tableRow.insertCell(1);
	tableCell.innerHTML = check(mail.to);
	
	var date = new Date();
	date.setTime(mail.receivedDate);
	tableCell = tableRow.insertCell(2);
	tableCell.innerHTML = date.toUTCString();
	
	var tableRow = mailContent.insertRow();
	var tableCell = tableRow.insertCell(0);
	tableCell.innerHTML = "CC:";
	
	var tableCell = tableRow.insertCell(1);
	tableCell.innerHTML = check(mail.cc);
	
	var tableRow = mailContent.insertRow();
	var tableCell = tableRow.insertCell(0);
	tableCell.innerHTML = "BCC:";
	
	var tableCell = tableRow.insertCell(1);
	tableCell.innerHTML = check(mail.bcc);
	
	/*the content*/
	var tableRow = mailContent.insertRow();
	var tableCell = tableRow.insertCell(0);
	var mail = document.createElement("div");
	mail.id = "content";
	tableCell.appendChild(mail);
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