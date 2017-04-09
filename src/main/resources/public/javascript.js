/*JavaScript and jQuery*/

/*Object*/
var remoteServer = {
	url: "/"
};

//$.ajax() връша XmlHttpRequest обект
function getMails() {
	var page = getCurrentPage();

	$.ajax({
		url: remoteServer.url + "messages?page=" + page,
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
	
	$("tr.changeBg").each(function() {
		var folderName = $(this).find("input[name=folderName]")[0];
		var id = $(this).find("input[name=id]")[0];
		
		$(this).find("td").slice(1).on("click", function() {
			if(sessionStorage.getItem(id.value) == null) {
				$.ajax({
					url: remoteServer.url + "mail?folderName=" + folderName.value + "&id=" + id.value,
					type: "GET",
					dataType: "text",
					success: function(result) {
						sessionStorage.setItem(id.value, result);
						location.href="readmail.html?id=" + id.value;
					}
				});
			} else {	
				location.href="readmail.html?id=" + id.value;
			}
		});
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

function generateManageMailMenu() {
	var manageMenu = document.getElementById("manage_mail_menu");
	
	var html= "";
	html += '<input id="all_checkbox" type="checkbox" onClick="allCheckBoxSelected(this)" />';
	html += '<label for="all_checkbox">All</label>';
	html += '<a class="menu_buttons" >Mark</a>';
	html += '<a class="menu_buttons" >Delete</a>';
	html += '<a class="menu_buttons" >Move in</a>';
	html += '<a id="previous_page" class="button"></a>';
	html += '<a id="next_page" class="button"></a>';
	
	manageMenu.innerHTML += html;
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

function generateButtons() {
	// нужно е id-то в скобите след $ да е на статичен елемент 
	$("#manage_mail_menu").on("click", "a#next_page", function() {
		var page = getCurrentPage();
		
		location.href = "folder.html?page=" + (page + 1);
	});
	
	$("#manage_mail_menu").on("click", "a#previous_page", function()  {
		var page = getCurrentPage();
		
		if(page != 0) {
			location.href = "folder.html?page=" + (page - 1);
		}
	});
}

function getCurrentPage() {
	var page = getParameterByName("page");
	if(page == null) {
		page = 0;
	} else {
		page = parseInt(page);
	}
	
	return page;
}
