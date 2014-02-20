<style type="text/css">

	#bandeauRecherche {
		margin-left:  17px;
		margin-right: 17px;
		background-image:none;
	}
	
	#bandeauRecherche h1{
		display:none;
	}
	
	#resultatsRecherche {
		margin-left:  17px;
		margin-right: 17px;
		width: 530px;
	}
</style>

		<div id="mainHeader">
			<a href="http://www.ethique.gouv.qc.ca/" id="mainHeaderLogo"><img alt="Commission de l'éthique de la science et de la technologie (CEST)" src="http://www.ethique.gouv.qc.ca/media/img/mainHeader_logo_cest.gif" class="floatLeft" width="194" height="77" /></a>
			<div id="mainHeaderBandeau">
				<img alt="Bandeau" src="http://www.ethique.gouv.qc.ca/media/img/mainHeader_bandeau.jpg" width="450" height="57" />
				<ul id="mainHeaderMenuFr">
					<li id="mainHeaderMenuFr1"><a href="http://www.ethique.gouv.qc.ca/@@_fr.html" title="Accueil"><span>Accueil</span></a></li>
					<li id="mainHeaderMenuFr2"><a href="http://www.ethique.gouv.qc.ca/@@@_plan_du_site.html" title="Plan du site"><span>Plan du site</span></a></li>
					<li id="mainHeaderMenuFr3"><a href="http://www.ethique.gouv.qc.ca/Coordonnees.html" title="Courrier"><span>Courrier</span></a></li>
					<li id="mainHeaderMenuFr4"><a href="http://www.gouv.qc.ca" title="Portail Québec"><span>Portail Québec</span></a></li>
					<li id="mainHeaderMenuFr5"><a href="./" title="Recherche"><span>Recherche</span></a></li>
					<li id="mainHeaderMenuFr6"><a href="http://www.ethique.gouv.qc.ca/@@_en.html" title="English"><span>English</span></a></li>
					<li id="mainHeaderMenuFr7"><a href="http://www.ethique.gouv.qc.ca/@@_es.html" title="Espa&ntilde;ol"><span>Espa&ntilde;ol</span></a></li>
				</ul>
			</div>
		</div>
		<div id="mainContent">
			<p id="mainContentTopShadow"/>
			<div id="mainContentWrapper">
				<div id="leftMenu">
					<ul>
						<li class="menuElement">
							<a href="javascript:void(0);" title="À propos de la Commission">À propos de la Commission</a>
							<ul style="display: none;">
								<li class="sousMenuFirstElement"><a href="http://www.ethique.gouv.qc.ca/Presentation-de-la-CEST.html" title="Présentation de la CEST" accesskey="a">Présentation de la CEST</a></li>
								<li><a href="http://www.ethique.gouv.qc.ca/Description-et-composition-de-la.html" title="Description et composition de la CEST" accesskey="b">Description et composition de la CEST</a></li>
								<li><a href="http://www.ethique.gouv.qc.ca/Mission-et-mandat.html" title="Mission et mandat" accesskey="c">Mission et mandat</a></li>
								<li><a href="http://www.ethique.gouv.qc.ca/Domaines-de-reflexion.html" title="Domaines de réflexion" accesskey="d">Domaines de réflexion</a></li>
								<li><a href="http://www.ethique.gouv.qc.ca/Les-membres-de-la-CEST.html" title="Les membres de la CEST" accesskey="e">Les membres de la CEST</a></li>
								<li class="sousMenuLastElement"><a href="http://www.ethique.gouv.qc.ca/Le-secretariat-de-la-CEST.html" title="Le secrétariat de la CEST" accesskey="f">Le secrétariat de la CEST</a></li>							
							</ul>
						</li>
						<li class="menuElement">
							<a href="http://www.ethique.gouv.qc.ca/-Publications-.html" title="Publications">Publications</a>
						</li>
						<li class="menuElement">
							<a href="javascript:void(0);" title="Projets de la Commission">Projets de la Commission</a>					
							<ul style="display: none;">						
								<li class="sousMenuElementSeul"><a href="http://www.ethique.gouv.qc.ca/-Projets-en-cours-.html" title="Projets en cours" accesskey="g">Projets en cours</a></li>
							</ul>
						</li>
						<li class="menuElement">
							<a href="http://www.ethique.gouv.qc.ca/-Liens-utiles-.html" title="Liens utiles">Liens utiles</a>
						</li>
						<li class="menuElement" id="menuElementSelected">
							<a href="http://www.ethique.gouv.qc.ca/-Nouveautes-.html" title="Nouveautés">Nouveautés</a>
						</li>
						<li class="iconsElement">
							<a href="javascript:void(0);" onclick="Actions.fontSizeDown(); return false;"><img alt="Diminuer la taille du texte" src="http://www.ethique.gouv.qc.ca/media/img/leftMenu_iconedown.gif" width="16" height="16" /></a>
							<a href="javascript:void(0);" onclick="Actions.fontSizeUp(); return false;"><img alt="Augmenter la taille du texte" src="http://www.ethique.gouv.qc.ca/media/img/leftMenu_iconeup.gif" width="16" height="16" /></a>
							<a href="javascript:void(0);" onclick="Actions.printPage(); return false;"><img alt="Imprimer la page" src="http://www.ethique.gouv.qc.ca/media/img/leftMenu_iconeprint.gif" width="16" height="16" /></a>
							<a href="javascript:void(0);" onclick="Actions.openForwardBox(); return false;"><img alt="Envoyer cette page par courriel" src="http://www.ethique.gouv.qc.ca/media/img/leftMenu_iconemail.gif" width="16" height="16" /></a>
						</li>
					</ul>
					<p id="themaLinksTopShadow"/>
					<div id="themaLinks">
						<div id="forwardToBox" style="display: none;">
							<form action="@-recherche.html" id="forwardToForm" name="forwardToForm" onsubmit="Actions.forwardPage.init(); return false;">
								<h2>Envoyer cette page</h2>
								<label for="forwardToBoxFrom">De:</label>
								<input type="text" name="forwardToBoxFrom" id="forwardToBoxFrom" title="" maxlength="40" />
								<label for="forwardToBoxTo">À:</label>
								<input type="text" name="forwardToBoxTo" id="forwardToBoxTo" title="" maxlength="40" />
								<input type="hidden" name="forwardToBoxLang" value="fr" />
								<input type="hidden" name="forwardToBoxTitle" value="Commission de l'éthique de la science et de la technologie (CEST)" />
								<input type="hidden" name="forwardToBoxUrl" value="http://www.ethique.gouv.qc.ca/@@_fr.html" />
								<input type="submit" id="forwardToBoxSubmit" />
							</form>
							<hr/>
						</div>
						<a href="http://www.ethique.gouv.qc.ca/BIG/"><img alt="BIG - Les Banques d'information Génétique" src="http://www.ethique.gouv.qc.ca/media/img/leftMenu_logo_big.gif" width="149" height="41" /></a>
						<hr/>
						<a href="http://www.ethique.gouv.qc.ca/OGM/"><img alt="Pour une gestion éthique des Organismes Génétiquement Modifiés (OGM)" src="http://www.ethique.gouv.qc.ca/media/img/leftMenu_logo_ogm.gif" width="149" height="53" /></a>
					<p id="themaLinksBottom"/>
					</div>
					<p id="cstLinkTop"/>
					<div id="cstLink">
						<a href="http://www.cst.gouv.qc.ca"><img alt="CST" src="http://www.ethique.gouv.qc.ca/media/img/leftMenu_logo_cst.gif" width="140" height="43" /></a>
					</div>
					<p id="cstLinkBottom"/>
					<p id="leftMenuBottom"/>
				</div>
				<div id="content">
					<h1>Recherche</h1>
					