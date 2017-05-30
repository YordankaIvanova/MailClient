/*JavaScript and jQuery*/

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
					if (response.isValid == "true") {
						location.href = "folder.html";
					} else {
						sessionStorage.removeItem(TOKEN);
					}
				}
		});
	}

	$("#signin_btn").on('click', function() {
		var signInButton = $(this);
		var isLoginInfoValid = true;
		signInButton.text("Authenticating...");

		var userNameFieldValue = $("#username").val();
		if(userNameFieldValue.length < 1) {
			 $("#un_warning").html("Please insert your mail address.");
			 isLoginInfoValid = false;
		}
		if($("#password").val().length < 1) {
			$("#pass_warning").html("Please insert your password.");
			isLoginInfoValid = false;
		}
		if($("#imap").val().length < 1) {
			$("#imap_warning").html("Please an IMAP Host.");
			isLoginInfoValid = false;
		}
		if($("#smtp").val().length < 1) {
			$("#smtp_warning").html("Please a SMTP Host.");
			isLoginInfoValid = false;
		}

		if(userNameFieldValue.length > 0 && !validateEmail(userNameFieldValue)) {
			$("#un_warning").html("Incorrect email address.");
			isLoginInfoValid = false;
		} else if(validateEmail(userNameFieldValue)) {
			$("#un_warning").html("");
		}

		if (isLoginInfoValid == true) {
			// Make button unclickable
			signInButton.disabled = true;

			sessionStorage.setItem(USERNAME, userNameFieldValue);
			var loginInf = serializeObject($("#login_form > form"));
			var jsonString = JSON.stringify(loginInf);

			$.ajax({
					url: "/auth/login",
					type: "POST",
					dataType: "json",
				    data: jsonString,
					contentType: "application/json; charset=utf-8",
					success: function(response){
						sessionStorage.setItem(TOKEN, response.token);
						location.href = "folder.html";
					},
					error: function(xhr, status, error) {
						signInButton.text("Sign in");
	                    var errorMessage = $.parseJSON(xhr.responseText);
	                    $("#error").html(errorMessage.error);
	                },
	                complete: function() {
	                	signInButton.disabled = false;
	                }
			});
		}
	});
});