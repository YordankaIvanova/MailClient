/*JavaScript*/

function getDateFormat(date,flag) {
	var today = new Date();
	var dateTime = new Date();
	var time;
	if(date.getDate() == today.getDate() &&
	date.getMonth() == today.getMonth() &&
	date.getFullYear() == today.getFullYear()) {
		time = date.getHours() + ":" + date.getMinutes();
		return time;
	} else {
		if(flag == true){
			dateTime = date.getHours() + ":" + date.getMinutes() + ", " +
			date.getDate() + "." + date.getMonth() + "." + date.getFullYear();
			return dateTime;
		} else {
			dateTime = date.getDate() + "." + date.getMonth() + "." + date.getFullYear();
			return dateTime;
		}
	  }
}