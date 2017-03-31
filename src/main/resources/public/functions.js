/*JavaScript*/

function getDateFormat(date,flag) {
	var today = new Date();
	var dateTime = new Date();
	var fullDate = date.getDate() + "." + date.getMonth() + "." + date.getFullYear();
	if(date.getDate() == today.getDate() &&
	date.getMonth() == today.getMonth() &&
	date.getFullYear() == today.getFullYear()) {
		
		return getTime(date);
	} else {
		if(flag == true){
			dateTime = date.getTime(date) + ", " + fullDate;
			return dateTime;
		} else {
			dateTime = fullDate;
			return dateTime;
		}
	  }
}

function getTime(date) {
	var time;
	var minutes;
	if(date.getMinutes() < 10) {
		minutes = ":0" + date.getMinutes();
	} else {
		minutes = ":" + date.getMinutes();
	}
	
	time = date.getHours() + minutes;
	return time;
}