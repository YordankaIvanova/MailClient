const NAV_FOLDERS_SESSION_PROPERTY_NAME = "nav_folders";
const DELAY = 5000;
const REPEAT_INTERVAL = 7500;
var timer_id = -1;

$(document).ready(function() {
	var token = sessionStorage.getItem(TOKEN);
	if (token != null) {
		$.ajax({
				url: "/auth/verify",
				type: "GET",
				dataType: "json",
				contentType: "application/json; charset=utf-8",
				headers: {
        			"X-User-Token": token
   				},
				success: function(response){
					if (response.isValid != "true") {
						location.href = "login.html";
						sessionStorage.clear();
					}
				}
		});
	} else {
		location.href = "login.html";
	}

	$("#name").html(sessionStorage.getItem(USERNAME));
	$("#exit").on('click', function() {
		$.ajax({
			url: "/auth/logout",
			type: "POST",
			headers: {
	    		"X-User-Token": sessionStorage.getItem(TOKEN)
		    },
			dataType: "text",
			success: function(result) {
				sessionStorage.removeItem(TOKEN);
				location.href="login.html";
			}
		});
	});
});

function readFoldersData() {
	var SessionValue = sessionStorage.getItem(TOKEN);

	$.ajax({
		url: "/folders/base",
		type: "GET",
		headers: {
    		"X-User-Token": SessionValue
	    },
		dataType: "text",
		success: function(result) {
			sessionStorage.setItem(NAV_FOLDERS_SESSION_PROPERTY_NAME, result);
			renderFoldersData();
			timer_id = setTimeout(readFoldersData, REPEAT_INTERVAL);
		},
		error: function(jqXHR) {
			// Unauthorized
			if (jqXHR.status == 401) {
				clearTimeout(timer_id);
			}
		}
	});
}

function renderFoldersData() {
	var foldersJSON = sessionStorage.getItem(NAV_FOLDERS_SESSION_PROPERTY_NAME);
	var html = "";

	if(foldersJSON == null) {
		return html;
	}

	var foldersData = $.parseJSON(foldersJSON);
	for(var i = 0; i < foldersData.length; i++) {
		html += renderSingleFolderData(foldersData[i]);
	}

	$("#folders_menu").html(html);
}

function renderSingleFolderData(folderData) {
	var html = '<li><a href="/folder.html?folderName=' + folderData.folderFullName + '">';
	html += folderData.folderName + ' (' + folderData.unreadMessageCount + ')' + '</a></li>';

	return html;
}