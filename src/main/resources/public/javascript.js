/*JavaScript and jQuery*/

/*Object*/
var remoteServer = {
	url: "/"
};

//$.ajax() връша XmlHttpRequest обект
function getMails() {
	if(hasSelectedEmails() == true) {
		setTimeout(getMails, REPEAT_INTERVAL);
	} else {
		var page = getCurrentPage();
		var folderName = getParameterByName("folderName");
		var pageUrl = null;
		if(folderName != null) {
			pageUrl = remoteServer.url + 'messages?folderName='+ folderName +'&page=' + page;
		} else {
			pageUrl = remoteServer.url + 'messages?page=' + page;
		}

		var SessionValue = sessionStorage.getItem(TOKEN);

		$.ajax({
			url: pageUrl,
			type: "GET",
			headers: {
        		"X-User-Token": SessionValue
   		    },
			dataType: "text",
			success: function(result) {
				generateMailTable(result);
			},
			complete: function() {
				hideMessage();
				setTimeout(getMails, REPEAT_INTERVAL);
			},
			error: function(jqXHR) {
				// Unauthorized
				if (jqXHR.status == 401) {
					location.href = "login.html";
					sessionStorage.clear();
				}
			}
		});
	}
}

function generateMailTable(data) {
	var jsonMails = $.parseJSON(data);
	var tableRows = '<tbody>';

	var mailTable = document.getElementById("content_table");
	mailTable.innerHTML = "";
	for(n = jsonMails.length - 1; n >= 0 ; n--){
		var mail = $.parseJSON(jsonMails[n]);
		tableRows += createMailTableRow(mail);
	}
	tableRows += '</tbody>';
	mailTable.innerHTML += tableRows;

	$("tr.changeBg").each(function() {
		var folderName = $(this).find("input[name=folderName]")[0];
		var id = $(this).find("input[name=id]")[0];

		// При кликане където и да е в първата клетка на таблицата,
		// пооменяме състоянието на checkbox-а, така че той да се селектира, ако
		// е деселектиран и обратното, ако е селектиран.
		$(this).find("td").first().on("click", function() {
			var input = $(this).find('input[name=rowCheckbox]')[0];
			var currentState = input.checked;
			input.checked = !currentState;

			// Прави се проверка дали има селектирани имейли и
			// ако има - показват се бутоните. В противен случай -
			// бутоните се скриват.
			var shouldShowButtons = hasSelectedEmails();
			hideAndShowButtons(shouldShowButtons);
		});

		// При кликане в полето на останалите клетки в реда, извършва се
		// операция по извличане на съдържанеието на съобщението
		// и преминаване към страницата, която ще покаже съдържанието
		// на мейла.
		$(this).find("td").slice(1).on("click", function() {

			// Оптимизация - ако мейлът вече е бил прочетен, съдържанието
			// му е съхранено вече в сесията и се извлича директно от нея.
			if(sessionStorage.getItem(id.value) == null) {
				var SessionValue = sessionStorage.getItem(TOKEN);

				$.ajax({
					url: remoteServer.url + "mail?folderName=" + folderName.value + "&id=" + id.value,
					type: "GET",
					headers: {
		        		"X-User-Token": SessionValue
		   		    },
					dataType: "text",
					beforeSend: function() {
						  showMessage();
					},
					success: function(result) {
						hideMessage();
						sessionStorage.setItem(id.value, result);
						location.href="readmail.html?id=" + id.value;
					},
					complete: function() {
						hideMessage();
					},
					error: function(jqXHR) {
						// Unauthorized
						if (jqXHR.status == 401) {
							location.href = "login.html";
							sessionStorage.clear();
						}
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
	html += '<td><input type="checkbox" name="rowCheckbox" class="checked" /><label></label></td>';
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

	html += '<select id="markMailSelector" class="menu_buttons">';
	html += '<option value="markRead">Mark as Read</option>';
	html += '<option value="markUnread">Mark as Unread</option></select>';

	html += '<a id="previous_page" class="button"></a>';
	html += '<a id="next_page" class="button"></a>';

	manageMenu.innerHTML += html;

	$("#markMailSelector").on("click", function() {
		var optionSelected = $("option:selected", this);
	    var valueSelected = this.value;
	    var idsAsJSON = getIDsOfSelectedMailsAsJSON();
	    var SessionValue = sessionStorage.getItem(TOKEN);
	    if(valueSelected == "markRead") {
    		$.ajax({
				url: "/messages/mark/read?" + getCurrentFolderParameter() + "ids=" + idsAsJSON,
				type: "POST",
				headers: {
	        		"X-User-Token": SessionValue
	        	}
   		    }).done(function() {
				var selectedMails = $("tr:has(input[name=rowCheckbox]:checked)");
				selectedMails.removeClass("unread");
				selectedMails.addClass("read");
			}).fail(function(jqXHR) {
				// Unauthorized
				if (jqXHR.status == 401) {
					location.href = "login.html";
					sessionStorage.clear();
				}
			});

	    } else if(valueSelected == "markUnread") {
	    	$.ajax({
				url: "/messages/mark/unread?" + getCurrentFolderParameter() + "ids=" + idsAsJSON,
				type: "POST",
				headers: {
	        		"X-User-Token": SessionValue
	        	}
   		    }).done(function() {
				var selectedMails = $("tr:has(input[name=rowCheckbox]:checked)");
				selectedMails.removeClass("read");
				selectedMails.addClass("unread");
			}).fail(function(jqXHR) {
				// Unauthorized
				if (jqXHR.status == 401) {
					location.href = "login.html";
					sessionStorage.clear();
				}
			});
	    }
	});
}

function getIDsOfSelectedMailsAsJSON() {
	var ids = [];
	$("tr:has(input[name=rowCheckbox]:checked)")
		.find("input[name=id]")
		.each(function() {
			ids.push($(this)[0].value);
		});

	return JSON.stringify(ids);
}

function allCheckBoxSelected(allCheckBox) {
	hideAndShowButtons(allCheckBox.checked);
	checkAndUncheckAllMails(allCheckBox);
}

function checkAndUncheckAllMails(allCheckBox) {
	var mail_table_tbody = document.getElementById("content_table").children[0];
	for(n = 0; n < mail_table_tbody.children.length; n++) {
		mail_table_tbody.children[n].firstChild.firstChild.checked = allCheckBox.checked;
	}
}

function hideAndShowButtons(shouldShow) {
	var elements = document.getElementsByClassName("menu_buttons");
	for(n = 0; n < elements.length; n++) {
		if(shouldShow){
			elements[n].style.visibility="visible";
		} else {
			elements[n].style.visibility="hidden";
		}
	}
}

function generateButtons() {
	$("a#next_page").on("click", function() {
		var page = getCurrentPage();
		var folderNameParam = getCurrentFolderParameter();

		location.href = 'folder.html?'+ folderNameParam + 'page=' + (page + 1);
	});

	$("a#previous_page").on("click", function()  {
		var page = getCurrentPage();

		if(page != 0) {
			var folderNameParam = getCurrentFolderParameter();

			location.href = 'folder.html?'+ folderNameParam + 'page=' + (page - 1);
		}
	});
}

function hasSelectedEmails() {
	var n = $("input[name=rowCheckbox]:checked").length;
	var hasSelectedEmails = (n > 0);

	return hasSelectedEmails;
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

function getCurrentFolderParameter() {
	var folderName = getParameterByName("folderName");
	if(folderName == null) {
		folderName = "INBOX";
	}

	var parameter = "";
	parameter = 'folderName='+ folderName + '&';

	return parameter;
}
