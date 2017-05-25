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
	html += '<input type="text" name="to" id="to_field" class="forminput" value="" /></p>';
	html += '<p id="recipients"></p>';
	html += '<p><label id="formcclabel">Cc:</label>'; 
	html += '<input type="text" name="cc" id="cc_field" class="forminput" value="" /></p>';
	html += '<p id="cc_recipients"></p>';
	html += '<p><label id="formsbjlabel">Subject:</label>';
	html += '<input type="text" name="mailsubject" id="sbj_field" class="forminput" value="" /></p>';
	html += '<p id="subject"></p>';
	html += '<textarea name="mailcontent" id="editor"></textarea>';
	html += '</form>';
	
	writeLetter.innerHTML += html;
	CKEDITOR.replace('editor');
}

$(document).ready(function() {
	function buttonBehaviour() {
		if(confirm("Your message will be discarded.") == true) {
			window.history.back();
	    }
	}
	
	var createBtn = $("#create_letter");
	createBtn.removeAttr("href");
	
	createBtn.on("click", function(){
		buttonBehaviour();
	});

	$("body").on('click','button#sendbutton', function() {
		var value = $("#to_field").val();
		var ccvalue = $("#cc_field").val();
		 
		if(value.length < 1) {
			 $("#recipients").html("Please specify at least one recipient.");
		}
		if($("#sbj_field").val().length < 1) {
			$("#subject").html("Please fill out the subject field.");
		}
		
		if(value.length > 0 && !validateEmail(value)) {
			$("#recipients").html("Uncorrect email address.");
		} else if(validateEmail(value)) {
			$("#recipients").html("");
		}
		
		if(ccvalue.length > 0 && !validateEmail(ccvalue)) {
			$("#cc_recipients").html("Uncorrect email address.");
		} else if(validateEmail(ccvalue)) {
			$("#cc_recipients").html("");
		}
		
		var mail = serializeObject($("form#mailform"));
		mail.mailcontent = CKEDITOR.instances.editor.getData();
		var jsonMailString = JSON.stringify(mail);
		var SessionValue = sessionStorage.getItem(TOKEN); 
		
		$.ajax({
			url: "/mail/send",
			type: "PUT",
			headers: {
        		"X-User-Token": SessionValue
   			},
			dataType: "json",
			data: jsonMailString,
			contentType: "application/json; charset=utf-8",
			success: function(result) {
				location.href = "notification.html";
			}
		});
	});
	
	$("body").on('click','button#discardbutton', function() {
		buttonBehaviour();	
	});
});

