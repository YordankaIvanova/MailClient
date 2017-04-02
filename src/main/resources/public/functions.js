/*JavaScript*/

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