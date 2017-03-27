/*JavaScript and jQuery*/

/*Object*/
var remoteServer = {
	url: "http://localhost:8081/"
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
	
	var mailTable = $("content_table");
	for(n = 0; n < jsonMails.length; n++){
		var mail = $.parseJSON(jsonMails[n]);
		createMailTableRow(mailTable, mail);
	}
}

function createMailTableRow(mailTable, mail) {
	var tableRow = mailTable.insertRow(-1);
	$(tableRow).click(function(){
        location.href="readmail.html?folderName=" + mail.folderName + "&id=" + mail.id;
    });
	tableRow.setAttribute("class", "changeBg");
	
	var tableCell = tableRow.insertCell(0);
	var mailCheckBox = document.createElement("input");
	mailCheckBox.type = "checkbox";
	var label = document.createElement("label"); 
	tableCell.appendChild(mailCheckBox);
	tableCell.appendChild(label);
	
	tableCell = tableRow.insertCell(1);
	tableCell.innerHTML = mail.from;
	
	tableCell = tableRow.insertCell(2);
	tableCell.innerHTML = mail.subject;
	
	var date = new Date();
	date.setTime(mail.date);
	tableCell = tableRow.insertCell(3);
	tableCell.innerHTML = date.toUTCString();
}

function allCheckBoxSelected(allCheckBox) {
	hideAndShowButtons(allCheckBox);
	checkAndUncheckAllMails(allCheckBox);
}

function checkAndUncheckAllMails(allCheckBox) {
	var mail_table_tbody = $("content_table").children[0];
	for(n = 0; n < mail_table_tbody.children.length; n++) {
		mail_table_tbody.children[n].firstChild.firstChild.checked = allCheckBox.checked;
	}
}

function hideAndShowButtons(allCheckBox) {
	var elements = $("menu_buttons");
	for(n = 0; n < elements.length; n++) {
		if(allCheckBox.checked){
			elements[n].style.visibility="visible";
		} else {
			elements[n].style.visibility="hidden";
		}
	}
}