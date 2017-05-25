/*JavaScript*/

var TOKEN = "token";
var USERNAME = "username";

//ÐŸÐ¾ÐºÐ°Ð·Ð²Ð° loading Ñ�ÑŠÐ¾Ð±Ñ‰ÐµÐ½Ð¸ÐµÑ‚Ð¾, ÐºÐ¾Ð³Ð°Ñ‚Ð¾ Ajax Ð·Ð°Ñ�Ð²ÐºÐ°Ñ‚Ð° Ñ�Ðµ Ð¸Ð·Ð¿Ñ€Ð°Ñ‚Ð¸.
function showMessage() {
	$("#loading-balloon").show();
}

//Ð¡ÐºÑ€Ð¸Ð²Ð° Ñ�ÑŠÐ¾Ð±Ñ‰ÐµÐ½Ð¸ÐµÑ‚Ð¾, ÐºÐ¾Ð³Ð°Ñ‚Ð¾ Ajax Ð·Ð°Ñ�Ð²ÐºÐ°Ñ‚Ð° Ð²ÑŠÑ€Ð½Ðµ Ñ€ÐµÐ·ÑƒÐ»Ñ‚Ð°Ñ‚.
function hideMessage() {
	$("#loading-balloon").hide();
}

function getDateFormat(date, showTime) {
	var today = new Date();
	var dateTime;
	var fullDate = formatDate(date);

	if(date.getDate() == today.getDate() &&
	date.getMonth() == today.getMonth() &&
	date.getFullYear() == today.getFullYear()) {
		dateTime = formatTime(date);
	} else {
		if(showTime == true){
			dateTime = formatTime(date) + ", " + fullDate;
		} else {
			dateTime = fullDate;
		}
	}

	return dateTime;
}

function formatTime(date) {
	var time;
	var minutes;
	minutes = formatNumber(date.getMinutes());

	time = date.getHours() + ":" + minutes;
	return time;
}

function formatDate(date) {
	var fullDate = formatNumber(date.getDate()) + "."
		+ formatNumber(date.getMonth() + 1) + "." + date.getFullYear();

	return fullDate;
}

function formatNumber(number) {
	var formattedNumber;

	if(number < 10) {
		formattedNumber = "0" + number;
	} else {
		formattedNumber = "" + number;
	}

	return formattedNumber;
}

function getParameterByName(name, url) {
    if (!url) {
      url = window.location.href;
    }
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return null;
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function validateEmail(email) {
	var emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	return emailRegex.test(email);
}

function serializeObject(form) {
	var o = {};
	var a = form.serializeArray();
	$.each(a, function() {
		if (o[this.name]) {
			if (!o[this.name].push) {
				o[this.name] = [o[this.name]];
			}
			o[this.name].push(this.value || '');
		} else {
			o[this.name] = this.value || '';
		}
	});
	return o;
};
