/*JavaScript and jQuery*/

function showButtons() {
	var manageMail = document.getElementById("manage_mail_menu");
	manageMail.innerHTML = "";
	
	var btnhtml = "";
	btnhtml += '<button type="submit" id="sendbutton">Send</button>';
	btnhtml += '<button type="button" id="discardbutton">Discard</button>';
	
	manageMail.innerHTML += btnhtml;
}


function showMailForm() {
	var writeLetter = document.getElementById("mail_content");
	writeLetter.innerHTML = "";
	
	var html = "";
	html += '<form id="mailform">';
	html += '<p><label id="formtolabel">To:</label>'; 
	html += '<input type="text" name="to" class="forminput" value="" /></p>';
	html += '<p><label id="formcclabel">Cc:</label>'; 
	html += '<input type="text" name="cc" class="forminput" value="" /></p>';
	html += '<p><label id="formsbjlabel">Subject:</label>';
	html += '<input type="text" name="mailsubject" class="forminput" value="" /></p>';
	html += '<textarea name="mailcontent" id="editor"></textarea>';
	html += '</form>';
	
	writeLetter.innerHTML += html;
	CKEDITOR.replace('editor');

}

$(document).ready(function() {
	
	$("body").on('click','button#sendbutton', function() {
			var mail = serializeObject($("form#mailform"));
			mail.mailcontent = CKEDITOR.instances.editor.getData();
			var jsonMailString = JSON.stringify(mail);
			console.log(mail);
	});
});

