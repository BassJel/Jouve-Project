/* Author: 

*/

(function($){
	var formRecherche = $('#form-recherche');
	var btnRecherchePrincipale = $('#bouton-recherche-principale');
	var optionsRecherche = $('#options-recherche');
	var btnOptionsRecherche = $('#bouton-options-recherche');
	
	// optionsRecherche.hide()
	btnOptionsRecherche.click(switchOptionsRecherche);
	btnRecherchePrincipale.click(submitForm);
	
	function submitForm(e) {
		//e.preventDefault();
//		formRecherche.submit();
	}

	function switchOptionsRecherche(e) {
		//if (!$.browser.msie) {
			e.preventDefault();
			optionsRecherche.slideToggle('fast', changeOptionsRechercheBoutonLbl);
		//}
	}

	function changeOptionsRechercheBoutonLbl(){
		btnOptionsRecherche.toggleClass('ouvert');
		var optionsRechercheOuvertes;
		if (optionsRecherche.is(':visible')) {
			// btnOptionsRecherche.text("Moins d'options");
			btnOptionsRecherche.text(lblOptionsRechercheMoins);
			optionsRechercheOuvertes = true;
		} else {
			// btnOptionsRecherche.text("Plus d'options");
			btnOptionsRecherche.text(lblOptionsRecherchePlus);
			optionsRechercheOuvertes = false;
		}
		$.cookie('optionsRechercheOuvertes', '' + optionsRechercheOuvertes, { expires: 365 });
	}

	var suggestionJsIsActive = false;

	function activateSuggestionOuvrable(el){
		var h3 = el.find('h3');
		var uls = h3.next('ul');
		uls.hide();
		h3.addClass('fermer');
		h3.click(clickTitreSuggestion);
		suggestionJsIsActive = true;
	}


	function desactivateSuggestionOuvrable(el){
		var h3 = el.find('h3');
		var uls = h3.next('ul');
		uls.attr('style','');
		h3.removeClass();
		h3.unbind('click', clickTitreSuggestion);
		suggestionJsIsActive = false;
	}

	function clickTitreSuggestion(e){
		e.preventDefault();
		$(e.currentTarget).next('ul').toggle();
		$(e.currentTarget).toggleClass('ouvert');
	}


/*
	var raffinementJsIsActive = false

	function activateRaffinementOuvrable(el){
		var h2 = el.find('h2');
		var uls = h2.next('div');
		uls.hide();
		h2.addClass('fermer');
		h2.click(clickTitreRaffinement)
		raffinementJsIsActive = true;
	}


	function desactivateRaffinementOuvrable(el){
		var h2 = el.find('h2');
		var uls = h2.next('div');
		uls.attr('style','')
		h2.removeClass()
		h2.unbind('click', clickTitreRaffinement);
		raffinementJsIsActive = false;
	}

	function clickTitreRaffinement(e){
		e.preventDefault()
			$(e.currentTarget).next('div').toggle();
			$(e.currentTarget).toggleClass('ouvert');
	}*/

	/*
	if($('#suggestions-recherche .suggestion-3-colonnes-colonne').css('float') == 'none'){
		activateSuggestionOuvrable($('#suggestions-recherche'));
	}

	$(window).resize(function(){
		if($('#suggestions-recherche .suggestion-3-colonnes-colonne').css('float') == 'none'){
			if(!suggestionJsIsActive){
				activateSuggestionOuvrable($('#suggestions-recherche'));
			}
		}else{
			desactivateSuggestionOuvrable($('#suggestions-recherche'));
		}
	});
	*/


	/*
	if($('.bloc-3-colonnes .bloc-3-colonnes-secondaire').css('float') == 'none'){
		activateRaffinementOuvrable($('#raffinement-recherche'));
	}

	$(window).resize(function(){
		if($('.bloc-3-colonnes .bloc-3-colonnes-secondaire').css('float') == 'none'){
			if(!raffinementJsIsActive){
				activateRaffinementOuvrable($('#raffinement-recherche'));
			}
		}else{
			desactivateRaffinementOuvrable($('#raffinement-recherche'));
		}
	})
	*/




	var listeTailles = '';
	$('#taille-texte button').each(function(i, el){
		listeTailles += $(el).attr('id') + ' ';
	});
	$('#taille-texte button').click(function(e){
		e.preventDefault();
		var nouvelleClasse = $(e.currentTarget).attr('id');
		$('body').removeClass(listeTailles).addClass(nouvelleClasse);
		$.cookie('taille-texte',nouvelleClasse,{expires: 365});
	});
	if($.cookie('taille-texte')){
		var id = $.cookie('taille-texte');
		$('#'+id).click();
	}


/* Pour démo: */
//	$('#navigation-onglets-recherche li a').click(function(e){
//		e.preventDefault();
//		console.log(e)
//		$(e.currentTarget).parent().siblings().find('a').removeClass('actif')
//		$(e.currentTarget).addClass('actif')
//	})





})(jQuery);


/************************DÉBUT INFOBULLE*************************************/

$(function(){
	try {
		/*$("a.tooltiplink").simpletooltip();*/
		$(".clic").simpletooltip({click: true, keyup:true, hideCallback: function(lien, infobulle){ $(infobulle).remove();}});
	} catch(err) {
		// alert(err);
	}
});

/********************************  FIN infobulle ***********************************/



