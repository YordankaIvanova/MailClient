/*JavaScript and jQuery*/



$(document).ready(function() {
	
	$("#signin_btn").on('click', function() {
		$(this).text("Authenticating...");
		
		var userNameFieldValue = $("#username").val();
		if(userNameFieldValue.length < 1) {
			 $("#un_warning").html("Please insert your mail address.");
		}
		if($("#password").val().length < 1) {
			$("#pass_warning").html("Please insert your password.");
		}
		if($("#imap").val().length < 1) {
			$("#imap_warning").html("Please an IMAP Host.");
		}
		if($("#smtp").val().length < 1) {
			$("#smtp_warning").html("Please a SMTP Host.");
		}
		
		if(userNameFieldValue.length > 0 && !validateEmail(userNameFieldValue)) {
			$("#un_warning").html("Incorrect email address.");
		} else if(validateEmail(userNameFieldValue)) {
			$("#un_warning").html("");
		}

		var loginInf = serializeObject($("#login_form > form"));
		var jsonString = JSON.stringify(loginInf);
		console.log(jsonString);
	
		$.ajax({
				url: "/auth/login",
				type: "POST",
				dataType: "json",
			    data: jsonString,
				contentType: "application/json; charset=utf-8",
				success: function(response){
					sessionStorage.setItem(TOKEN, response);
					location.href = "folder.html";
				},
				error: function(xhr, status, error) { 
					$(this).text("Sign in");
                    var errorMessage = $.parseJSON(xhr.responseText);
                    $("#error").html(errorMessage.error);
                } 
		});
	});
});