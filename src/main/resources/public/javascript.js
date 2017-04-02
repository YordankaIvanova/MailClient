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
	
	$("tr.changeBg").on("click", function(){
		var folderName = $(this).find("input[name=folderName]")[0];
		var id = $(this).find("input[name=id]")[0];
        location.href="readmail.html?folderName=" + folderName.value + "&id=" + id.value;
    });
}

function createMailTableRow (mail) {
	var receiveddate = new Date();
	receiveddate.setTime(mail.date);
	
	var html = "";
	if(mail.seen == false){
		html += '<tr class="changeBg unread">';
	} else {
		html += '<tr class="changeBg read">';
	}
	html += '<td><input type="checkbox"/><label></label></td>';
	html += '<td>' + mail.from + '</td>';
	html += '<td>' + mail.subject + '</td>';
	html += '<td>' + getDateFormat(receiveddate,false) + '</td>';
	html += '<input type="hidden" name="folderName" value="' + mail.folderName + '"/>';
	html += '<input type="hidden" name="id" value="' + mail.id + '"/>';
	html += '</tr>';
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