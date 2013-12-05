function goToUrl(url, rootPrefix, restrictionPrefix) {
	var completedUrl = url;
	var elements = document.getElementsByTagName("*");
	for ( var i = 0; i < elements.length; i++) {
		var element = elements[i];
		if (!(typeof element.name == "undefined") && element.name != "") {
			if (element.name.indexOf(rootPrefix) != -1
					&& completedUrl.indexOf(element.name) == -1 && element.name.indexOf(restrictionPrefix) == -1) {
				
				completedUrl += "&";
				completedUrl += element.name;
				completedUrl += "=";
				completedUrl += element.value;
				
			}
		}
	}
	document.location.href = completedUrl;
}