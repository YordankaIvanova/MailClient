/*JavaScript and jQuery*/

/*Object*/
var remoteServer = {
	url: "/"
};

function getMails() {
	$.ajax({
		url: remoteServer.url + "messages",
		type: "GET",
		dataType: "text",
		success: function(result) {
			generateMailTable(result);
		}
	});

}

function generateMailTable(data) {
	var jsonMails = $.parseJSON(data);
	var tableRows = '<tbody>'; 

	var mailTable = document.getElementById("content_table");
	for(n = jsonMails.length - 1; n >= 0 ; n--){
		var mail = $.parseJSON(jsonMails[n]);
		tableRows += createMailTableRow(mail);
	}
	tableRows += '</tbody>';
	mailTable.innerHTML += tableRows;
	
	$(".changeBg").click(function(){
        location.href="readmail.html?folderName=" + mail.folderName + "&id=" + mail.id;
    });
}

function createMailTableRow (mail) {
	var receiveddate = new Date();
	receiveddate.setTime(mail.date);
	
	var html = "";
	if(mail.seen == false){
		html += '<tr class="changeBg unread">';
	} else {
		html += '<tr class="changeBg">';
	}
	html += '<td><input type="checkbox"><label></label></td>';
	html += '<td>' + mail.from + '</td>';
	html += '<td>' + mail.subject + '</td>';
	html += '<td>' + getDateFormat(receiveddate,false) + '</td></tr>';
	return html;
}

function allCheckBoxSelected(allCheckBox) {
	hideAndShowButtons(allCheckBox);
	checkAndUncheckAllMails(allCheckBox);
}

function checkAndUncheckAllMails(allCheckBox) {
	var mail_table_tbody = document.getElementById("content_table").children[0];
	for(n = 0; n < mail_table_tbody.children.length; n++) {
		mail_table_tbody.children[n].firstChild.firstChild.checked = allCheckBox.checked;
	}
}

function hideAndShowButtons(allCheckBox) {
	var elements = document.getElementsByClassName("menu_buttons");
	for(n = 0; n < elements.length; n++) {
		if(allCheckBox.checked){
			elements[n].style.visibility="visible";
		} else {
			elements[n].style.visibility="hidden";
		}
	}
}