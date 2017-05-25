const NAV_FOLDERS_SESSION_PROPERTY_NAME = "nav_folders";
const DELAY = 5000;
const REPEAT_INTERVAL = 15000;

$(document).ready(function() {
	$("#name").html(value);
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
		},
		complete: function() {
			setTimeout(readFoldersData, REPEAT_INTERVAL);
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