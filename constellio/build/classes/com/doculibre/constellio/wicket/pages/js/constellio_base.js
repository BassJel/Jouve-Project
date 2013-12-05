/**
 * Function called by the foldable sections in the facets panel
 */
function toggleSection(foldableSectionContainerId, toggleImgId, openedImgURL, closedImgURL, cookieName) {
    var foldableSectionContainer = document.getElementById(foldableSectionContainerId);
    var toggleImg = document.getElementById(toggleImgId);
    var toggleImgSrc = toggleImg.src;
    var opened;
    if (toggleImgSrc.indexOf(openedImgURL) != -1) {
    	opened = false;
        foldableSectionContainer.style.display = 'none';
        toggleImg.src = closedImgURL;
        createCookie(cookieName, 'false', 1);
    } else {
    	opened = true;
        foldableSectionContainer.style.display = 'inline';
        toggleImg.src = openedImgURL;
        createCookie(cookieName, 'true', 1);
    }
}

/**
 * Source : http://www.quirksmode.org/js/cookies.html
 */
function createCookie(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function eraseCookie(name) {
	createCookie(name,"",-1);
}

function sendGet(url) {
	var oXMLHttpRequest	= new XMLHttpRequest;
	oXMLHttpRequest.open("GET", url, false);
	oXMLHttpRequest.onreadystatechange	= function() {
		if (this.readyState == XMLHttpRequest.DONE) {
			// my code
		}
	}
	oXMLHttpRequest.send(null);
}
