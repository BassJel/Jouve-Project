function toggleFacetteAutresValeurs(imgFacette, facetteId, nbValeurs) {
	var toggleVisible;
	if (imgFacette.src.indexOf("plus.gif") != -1) {
		toggleVisible = true;
	} else {
		toggleVisible = false;
	}
	
	for (var i = 10; i < nbValeurs; i++) {
		var valeurMasquee = document.getElementById(facetteId + "_" + i);
		if (toggleVisible) {
			valeurMasquee.style.display = "block";
		} else {
			valeurMasquee.style.display = "none";
		}
	}
	
	var indexOfSlash = imgFacette.src.lastIndexOf("/");
	var imgFacettePath = imgFacette.src.substring(0, indexOfSlash + 1);
	if (toggleVisible) {
		imgFacette.src = imgFacettePath + "moins.gif";
	} else {
		imgFacette.src = imgFacettePath + "plus.gif";
	}
}