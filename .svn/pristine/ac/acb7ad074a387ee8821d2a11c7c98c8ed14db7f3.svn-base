
    var rechercheSuggeree = "";
    
    function remplacerMot(nouveauMot, indexMot) {
    	nouveauMot = nouveauMot.replace(" ", "&nbsp;");	
        
    	var nouvelleRecherche;
        var ancienneRecherche = rechercheSuggeree;
        
        var mots = ancienneRecherche.split(" ");

        if (indexMot < mots.length) {
            mots[indexMot] = nouveauMot;
            nouvelleRecherche = "";
            for (i = 0; i < mots.length; i++) {
                nouvelleRecherche = nouvelleRecherche + mots[i];
                if (i < mots.length) {
                    nouvelleRecherche = nouvelleRecherche + " ";
                }
            }
        } else {
            nouvelleRecherche = ancienneRecherche;
        }
        
        rechercheSuggeree = nouvelleRecherche;
        
        var tagChange = document.getElementById("mot" + indexMot);
        if (tagChange) {
            tagChange.innerHTML = nouveauMot;
        } else {
        	alert("Tag with id 'mot" + indexMot + "' is missing");
        }
    }
    
    function lancerRechercheSuggeree(idChampRecherche, idSubmitRecherche) {
        document.getElementById(idChampRecherche).value = rechercheSuggeree.replace("&nbsp;", " ");
        document.getElementById(idSubmitRecherche).click();
    }
    
    /**
     * Fonction qui va retourner le premier tag parent du tag reçu en argument dont le nom
     * correspond au nom reçu en argument.
     * 
     * Par exemple, on peut obtenir le tag <table> qui contient un tag <a>.
     * 
     * @param nomTag Le nom du tag recherché 
     * @param tagCourant L'objet Javascript pour le tag dont on cherche le parent.
     */
    function getTagParent(nomTag, tagCourant) {
        //Les noms de tags sont en majuscules
    	nomTag = nomTag.toUpperCase();
    	
    	var tagParent = tagCourant.parentNode;
    	//On remonte jusqu'à ce qu'il n'y ait plus de tag parent
        while (tagParent && tagParent.tagName.toUpperCase() != nomTag) {
        	tagParent = tagParent.parentNode;
        }
        return tagParent;
    }

    function iecompattest(){
        return (document.compatMode && document.compatMode.indexOf("CSS")!=-1)? document.documentElement : document.body
    }

    var ie5=document.all && !window.opera;
    var ns6=document.getElementById;

    function findCoords(e) {
    	var posX = 0, posY = 0;

        eventX=ie5? event.clientX : e.clientX
        eventY=ie5? event.clientY : e.clientY
        		
        //Find out how close the mouse is to the corner of the window
        var rightedge=ie5? iecompattest().clientWidth-eventX : window.innerWidth-eventX
        var bottomedge=ie5? iecompattest().clientHeight-eventY : window.innerHeight-eventY
        //if the horizontal distance isn't enough to accomodate the width of the context menu

        //position the horizontal position of the menu where the mouse was clicked
        posX=ie5? iecompattest().scrollLeft+eventX : window.pageXOffset+eventX;
        //same concept with the vertical position

        posY=ie5? iecompattest().scrollTop+event.clientY : window.pageYOffset+eventY;
        		   
        return [ posX, posY ];    		   
    }


//** Chrome Drop Down Menu- Author: Dynamic Drive (http://www.dynamicdrive.com)

//** Updated: July 14th 06' to v2.0
	//1) Ability to "left", "center", or "right" align the menu items easily, just by modifying the CSS property "text-align".
	//2) Added an optional "swipe down" transitional effect for revealing the drop down menus.
	//3) Support for multiple Chrome menus on the same page.

//** Updated: Nov 14th 06' to v2.01- added iframe shim technique

//** Updated: July 23rd, 08 to v2.4
	//1) Main menu items now remain "selected" (CSS class "selected" applied) when user moves mouse into corresponding drop down menu. 
	//2) Adds ability to specify arbitrary HTML that gets added to the end of each menu item that carries a drop down menu (ie: a down arrow image).
	//3) All event handlers added to the menu are now unobstrusive, allowing you to define your own "onmouseover" or "onclick" events on the menu items.
	//4) Fixed elusive JS error in FF that sometimes occurs when mouse quickly moves between main menu items and drop down menus

//** Updated: Oct 29th, 08 to v2.5 (only .js file modified from v2.4)
	//1) Added ability to customize reveal animation speed (# of steps)
	//2) Menu now works in IE8 beta2 (a valid doctype at the top of the page is required)

var cssdropdown={
disappeardelay: 250, //set delay in miliseconds before menu disappears onmouseout
dropdownindicator: '<span></span>', //specify full HTML to add to end of each menu item with a drop down menu
enablereveal: [true, 5], //enable swipe effect? [true/false, steps (Number of animation steps. Integer between 1-20. Smaller=faster)]
enableiframeshim: 1, //enable "iframe shim" in IE5.5 to IE7? (1=yes, 0=no)

//No need to edit beyond here////////////////////////

dropmenuobj: null, asscmenuitem: null, domsupport: document.all || document.getElementById, standardbody: null, iframeshimadded: false, revealtimers: {},

getposOffset:function(what, offsettype){
	var totaloffset=(offsettype=="left")? what.offsetLeft : what.offsetTop;
	var parentEl=what.offsetParent;
	while (parentEl!=null){
		totaloffset=(offsettype=="left")? totaloffset+parentEl.offsetLeft : totaloffset+parentEl.offsetTop;
		parentEl=parentEl.offsetParent;
	}
	return totaloffset;
},

css:function(el, targetclass, action){
	var needle=new RegExp("(^|\\s+)"+targetclass+"($|\\s+)", "ig")
	if (action=="check")
		return needle.test(el.className)
	else if (action=="remove")
		el.className=el.className.replace(needle, "")
	else if (action=="add" && !needle.test(el.className))
		el.className+=" "+targetclass
},

showmenu:function(dropmenu, e){
	if (this.enablereveal[0]){
		if (!dropmenu._trueheight || dropmenu._trueheight<10)
			dropmenu._trueheight=dropmenu.offsetHeight
		clearTimeout(this.revealtimers[dropmenu.id])
		dropmenu.style.height=dropmenu._curheight=0
		dropmenu.style.overflow="hidden"
		dropmenu.style.visibility="visible"
		dropmenu.style.display="inline"
		this.revealtimers[dropmenu.id]=setInterval(function(){cssdropdown.revealmenu(dropmenu)}, 10)
	}
	else{
		dropmenu.style.visibility="visible"
		dropmenu.style.display="inline"
	}
	this.css(this.asscmenuitem, "selected", "add")
},

revealmenu:function(dropmenu, dir){
	var curH=dropmenu._curheight, maxH=dropmenu._trueheight, steps=this.enablereveal[1]
	if (curH<maxH){
		var newH=Math.min(curH, maxH)
		dropmenu.style.height=newH+"px"
		dropmenu._curheight= newH + Math.round((maxH-newH)/steps) + 1
	}
	else{ //if done revealing menu
		dropmenu.style.height="auto"
		dropmenu.style.overflow="hidden"
		clearInterval(this.revealtimers[dropmenu.id])
	}
},

clearbrowseredge:function(obj, whichedge){
	var edgeoffset=0
	if (whichedge=="rightedge"){
		var windowedge=document.all && !window.opera? this.standardbody.scrollLeft+this.standardbody.clientWidth-15 : window.pageXOffset+window.innerWidth-15
		var dropmenuW=this.dropmenuobj.offsetWidth
		if (windowedge-this.dropmenuobj.x < dropmenuW)  //move menu to the left?
			edgeoffset=dropmenuW-obj.offsetWidth
	}
	else{
		var topedge=document.all && !window.opera? this.standardbody.scrollTop : window.pageYOffset
		var windowedge=document.all && !window.opera? this.standardbody.scrollTop+this.standardbody.clientHeight-15 : window.pageYOffset+window.innerHeight-18
		var dropmenuH=this.dropmenuobj._trueheight
		if (windowedge-this.dropmenuobj.y < dropmenuH){ //move up?
			edgeoffset=dropmenuH+obj.offsetHeight
			if ((this.dropmenuobj.y-topedge)<dropmenuH) //up no good either?
				edgeoffset=this.dropmenuobj.y+obj.offsetHeight-topedge
		}
	}
	return edgeoffset
},

dropit:function(obj, e, dropmenuID){
	if (this.dropmenuobj!=null) //hide previous menu
		this.hidemenu() //hide menu
	this.clearhidemenu()
	this.dropmenuobj=document.getElementById(dropmenuID) //reference drop down menu
	this.asscmenuitem=obj //reference associated menu item
	this.showmenu(this.dropmenuobj, e)
	this.dropmenuobj.x=this.getposOffset(obj, "left")
	this.dropmenuobj.y=this.getposOffset(obj, "top")
	this.dropmenuobj.style.left=this.dropmenuobj.x-this.clearbrowseredge(obj, "rightedge")+"px"
	this.dropmenuobj.style.top=this.dropmenuobj.y-this.clearbrowseredge(obj, "bottomedge")+obj.offsetHeight+1+"px"
	this.positionshim() //call iframe shim function
},

positionshim:function(){ //display iframe shim function
	if (this.iframeshimadded){
		if (this.dropmenuobj.style.visibility=="visible"){
			this.shimobject.style.width=this.dropmenuobj.offsetWidth+"px"
			this.shimobject.style.height=this.dropmenuobj._trueheight+"px"
			this.shimobject.style.left=parseInt(this.dropmenuobj.style.left)+"px"
			this.shimobject.style.top=parseInt(this.dropmenuobj.style.top)+"px"
			this.shimobject.style.display="inline"
		}
	}
},

hideshim:function(){
	if (this.iframeshimadded)
		this.shimobject.style.display='none'
},

isContained:function(m, e){
	var e=window.event || e
	var c=e.relatedTarget || ((e.type=="mouseover")? e.fromElement : e.toElement)
	while (c && c!=m)try {c=c.parentNode} catch(e){c=m}
	if (c==m)
		return true
	else
		return false
},

dynamichide:function(m, e){
	if (!this.isContained(m, e)){
		this.delayhidemenu()
	}
},

delayhidemenu:function(){
	this.delayhide=setTimeout("cssdropdown.hidemenu()", this.disappeardelay) //hide menu
},

hidemenu:function(){
	this.css(this.asscmenuitem, "selected", "remove")
	this.dropmenuobj.style.visibility='hidden'
	this.dropmenuobj.style.display='none'
	this.dropmenuobj.style.left=this.dropmenuobj.style.top="-1000px"
	this.hideshim()
},

clearhidemenu:function(){
	if (this.delayhide!="undefined")
		clearTimeout(this.delayhide)
},

addEvent:function(target, functionref, tasktype){
	if (target.addEventListener)
		target.addEventListener(tasktype, functionref, false);
	else if (target.attachEvent)
		target.attachEvent('on'+tasktype, function(){return functionref.call(target, window.event)});
},

startchrome:function(){
	if (!this.domsupport)
		return
	this.standardbody=(document.compatMode=="CSS1Compat")? document.documentElement : document.body
	for (var ids=0; ids<arguments.length; ids++){
		var menuitems=document.getElementById(arguments[ids]).getElementsByTagName("a")
		for (var i=0; i<menuitems.length; i++){
			if (menuitems[i].getAttribute("rel")){
				var relvalue=menuitems[i].getAttribute("rel")
				var asscdropdownmenu=document.getElementById(relvalue)
				this.addEvent(asscdropdownmenu, function(){cssdropdown.clearhidemenu()}, "mouseover")
				this.addEvent(asscdropdownmenu, function(e){cssdropdown.dynamichide(this, e)}, "mouseout")
				this.addEvent(asscdropdownmenu, function(){cssdropdown.delayhidemenu()}, "click")
				try{
					menuitems[i].innerHTML=menuitems[i].innerHTML+" "+this.dropdownindicator
				}catch(e){}
				this.addEvent(menuitems[i], function(e){ //show drop down menu when main menu items are mouse over-ed
					if (!cssdropdown.isContained(this, e)){
						var evtobj=window.event || e
						cssdropdown.dropit(this, evtobj, this.getAttribute("rel"))
					}
				}, "mouseover")
				this.addEvent(menuitems[i], function(e){cssdropdown.dynamichide(this, e)}, "mouseout") //hide drop down menu when main menu items are mouse out
				this.addEvent(menuitems[i], function(){cssdropdown.delayhidemenu()}, "click") //hide drop down menu when main menu items are clicked on
			}
		} //end inner for
	} //end outer for
	if (this.enableiframeshim && document.all && !window.XDomainRequest && !this.iframeshimadded){ //enable iframe shim in IE5.5 thru IE7?
		document.write('<IFRAME id="iframeshim" src="about:blank" frameBorder="0" scrolling="no" style="left:0; top:0; position:absolute; display:none;z-index:90; background: transparent;"></IFRAME>')
		this.shimobject=document.getElementById("iframeshim") //reference iframe object
		this.shimobject.style.filter='progid:DXImageTransform.Microsoft.Alpha(style=0,opacity=0)'
		this.iframeshimadded=true
	}
} //end startchrome

}




























//** AnyLink CSS Menu v2.0- (c) Dynamic Drive DHTML code library: http://www.dynamicdrive.com
//** Script Download/ instructions page: http://www.dynamicdrive.com/dynamicindex1/anylinkcss.htm
//** January 19', 2009: Script Creation date

//**May 23rd, 09': v2.1
	//1) Automatically adds a "selectedanchor" CSS class to the currrently selected anchor link
	//2) For image anchor links, the custom HTML attributes "data-image" and "data-overimage" can be inserted to set the anchor's default and over images.

//**June 1st, 09': v2.2
	//1) Script now runs automatically after DOM has loaded. anylinkcssmenu.init) can now be called in the HEAD section

//**May 23rd, 10': v2.21: Fixes script not firing in IE when inside a frame page

if (typeof dd_domreadycheck=="undefined") //global variable to detect if DOM is ready
	var dd_domreadycheck=false

var anylinkcssmenu={

menusmap: {},
preloadimages: [],
effects: {delayhide: 200, shadow:{enabled:true, opacity:0.3, depth: [5, 5]}, fade:{enabled:true, duration:200}}, //customize menu effects

dimensions: {},

getoffset:function(what, offsettype){
	return (what.offsetParent)? what[offsettype]+this.getoffset(what.offsetParent, offsettype) : what[offsettype]
},

getoffsetof:function(el){
	el._offsets={left:this.getoffset(el, "offsetLeft"), top:this.getoffset(el, "offsetTop"), h: el.offsetHeight}
},

getdimensions:function(menu){
	this.dimensions={anchorw:menu.anchorobj.offsetWidth, anchorh:menu.anchorobj.offsetHeight,
		docwidth:(window.innerWidth ||this.standardbody.clientWidth)-20,
		docheight:(window.innerHeight ||this.standardbody.clientHeight)-15,
		docscrollx:window.pageXOffset || this.standardbody.scrollLeft,
		docscrolly:window.pageYOffset || this.standardbody.scrollTop
	}
	if (!this.dimensions.dropmenuw){
		this.dimensions.dropmenuw=menu.dropmenu.offsetWidth
		this.dimensions.dropmenuh=menu.dropmenu.offsetHeight
	}
},

isContained:function(m, e){
	var e=window.event || e
	var c=e.relatedTarget || ((e.type=="mouseover")? e.fromElement : e.toElement)
	while (c && c!=m)try {c=c.parentNode} catch(e){c=m}
	if (c==m)
		return true
	else
		return false
},

setopacity:function(el, value){
	el.style.opacity=value
	if (typeof el.style.opacity!="string"){ //if it's not a string (ie: number instead), it means property not supported
		el.style.MozOpacity=value
		if (el.filters){
			el.style.filter="progid:DXImageTransform.Microsoft.alpha(opacity="+ value*100 +")"
		}
	}
},

showmenu:function(menuid){
	var menu=anylinkcssmenu.menusmap[menuid]
	clearTimeout(menu.hidetimer)
	this.getoffsetof(menu.anchorobj)
	this.getdimensions(menu)
	var posx=menu.anchorobj._offsets.left + (menu.orientation=="lr"? this.dimensions.anchorw : 0) //base x pos
	var posy=menu.anchorobj._offsets.top+this.dimensions.anchorh - (menu.orientation=="lr"? this.dimensions.anchorh : 0)//base y pos
	if (posx+this.dimensions.dropmenuw+this.effects.shadow.depth[0]>this.dimensions.docscrollx+this.dimensions.docwidth){ //drop left instead?
		posx=posx-this.dimensions.dropmenuw + (menu.orientation=="lr"? -this.dimensions.anchorw : this.dimensions.anchorw)
	}
	if (posy+this.dimensions.dropmenuh>this.dimensions.docscrolly+this.dimensions.docheight){  //drop up instead?
		posy=Math.max(posy-this.dimensions.dropmenuh - (menu.orientation=="lr"? -this.dimensions.anchorh : this.dimensions.anchorh), this.dimensions.docscrolly) //position above anchor or window's top edge
	}
	if (this.effects.fade.enabled){
		this.setopacity(menu.dropmenu, 0) //set opacity to 0 so menu appears hidden initially
		if (this.effects.shadow.enabled)
			this.setopacity(menu.shadow, 0) //set opacity to 0 so shadow appears hidden initially
	}
	menu.dropmenu.setcss({left:posx+'px', top:posy+'px', visibility:'visible'})
	if (this.effects.shadow.enabled)
		menu.shadow.setcss({left:posx+anylinkcssmenu.effects.shadow.depth[0]+'px', top:posy+anylinkcssmenu.effects.shadow.depth[1]+'px', visibility:'visible'})
	if (this.effects.fade.enabled){
		clearInterval(menu.animatetimer)
		menu.curanimatedegree=0
		menu.starttime=new Date().getTime() //get time just before animation is run
		menu.animatetimer=setInterval(function(){anylinkcssmenu.revealmenu(menuid)}, 20)
	}
},

revealmenu:function(menuid){
	var menu=anylinkcssmenu.menusmap[menuid]
	var elapsed=new Date().getTime()-menu.starttime //get time animation has run
	if (elapsed<this.effects.fade.duration){
		this.setopacity(menu.dropmenu, menu.curanimatedegree)
		if (this.effects.shadow.enabled)
			this.setopacity(menu.shadow, menu.curanimatedegree*this.effects.shadow.opacity)
	}
	else{
		clearInterval(menu.animatetimer)
		this.setopacity(menu.dropmenu, 1)
		menu.dropmenu.style.filter=""
	}
	menu.curanimatedegree=(1-Math.cos((elapsed/this.effects.fade.duration)*Math.PI)) / 2
},

setcss:function(param){
	for (prop in param){
		this.style[prop]=param[prop]
	}
},

setcssclass:function(el, targetclass, action){
	var needle=new RegExp("(^|\\s+)"+targetclass+"($|\\s+)", "ig")
	if (action=="check")
		return needle.test(el.className)
	else if (action=="remove")
		el.className=el.className.replace(needle, "")
	else if (action=="add" && !needle.test(el.className))
		el.className+=" "+targetclass
},

hidemenu:function(menuid){
	var menu=anylinkcssmenu.menusmap[menuid]
	clearInterval(menu.animatetimer)
	menu.dropmenu.setcss({visibility:'hidden', left:0, top:0})
	menu.shadow.setcss({visibility:'hidden', left:0, top:0})
},

getElementsByClass:function(targetclass){
	if (document.querySelectorAll)
		return document.querySelectorAll("."+targetclass)
	else{
		var classnameRE=new RegExp("(^|\\s+)"+targetclass+"($|\\s+)", "i") //regular expression to screen for classname
		var pieces=[]
		var alltags=document.all? document.all : document.getElementsByTagName("*")
		for (var i=0; i<alltags.length; i++){
			if (typeof alltags[i].className=="string" && alltags[i].className.search(classnameRE)!=-1)
				pieces[pieces.length]=alltags[i]
		}
		return pieces
	}
},

addEvent:function(targetarr, functionref, tasktype){
	if (targetarr.length>0){
		var target=targetarr.shift()
		if (target.addEventListener)
			target.addEventListener(tasktype, functionref, false)
		else if (target.attachEvent)
			target.attachEvent('on'+tasktype, function(){return functionref.call(target, window.event)})
		this.addEvent(targetarr, functionref, tasktype)
	}
},

domready:function(functionref){ //based on code from the jQuery library
	if (dd_domreadycheck){
		functionref()
		return
	}
	// Mozilla, Opera and webkit nightlies currently support this event
	if (document.addEventListener) {
		// Use the handy event callback
		document.addEventListener("DOMContentLoaded", function(){
			document.removeEventListener("DOMContentLoaded", arguments.callee, false )
			functionref();
			dd_domreadycheck=true
		}, false )
	}
	else if (document.attachEvent){
		// If IE and not an iframe
		// continually check to see if the document is ready
		if ( document.documentElement.doScroll && window == window.top) (function(){
			if (dd_domreadycheck) return
			try{
				// If IE is used, use the trick by Diego Perini
				// http://javascript.nwbox.com/IEContentLoaded/
				document.documentElement.doScroll("left")
			}catch(error){
				setTimeout( arguments.callee, 0)
				return;
			}
			//and execute any waiting functions
			functionref();
			dd_domreadycheck=true
		})();
	}
	if (document.attachEvent && parent.length>0) //account for page being in IFRAME, in which above doesn't fire in IE
		this.addEvent([window], function(){functionref()}, "load");
},

addState:function(anchorobj, state){
	if (anchorobj.getAttribute('data-image')){
		var imgobj=(anchorobj.tagName=="IMG")? anchorobj : anchorobj.getElementsByTagName('img')[0]
		if (imgobj){
			imgobj.src=(state=="add")? anchorobj.getAttribute('data-overimage') : anchorobj.getAttribute('data-image')
		}
	}
	else
		anylinkcssmenu.setcssclass(anchorobj, "selectedanchor", state)
},


setupmenu:function(targetclass, anchorobj, pos){
	this.standardbody=(document.compatMode=="CSS1Compat")? document.documentElement : document.body
	var relattr=anchorobj.getAttribute("rel")
	var dropmenuid=relattr.replace(/\[(\w+)\]/, '')
	var menu=this.menusmap[targetclass+pos]={
		id: targetclass+pos,
		anchorobj: anchorobj,	
		dropmenu: document.getElementById(dropmenuid),
		revealtype: (relattr.length!=dropmenuid.length && RegExp.$1=="click")? "click" : "mouseover",
		orientation: anchorobj.getAttribute("rev")=="lr"? "lr" : "ud",
		shadow: document.createElement("div")
	}
	menu.anchorobj._internalID=targetclass+pos
	menu.anchorobj._isanchor=true
	menu.dropmenu._internalID=targetclass+pos
	menu.shadow._internalID=targetclass+pos
	menu.shadow.className="anylinkshadow"
	document.body.appendChild(menu.dropmenu) //move drop down div to end of page
	document.body.appendChild(menu.shadow)
	menu.dropmenu.setcss=this.setcss
	menu.shadow.setcss=this.setcss
	menu.shadow.setcss({width: menu.dropmenu.offsetWidth+"px", height:menu.dropmenu.offsetHeight+"px"})
	this.setopacity(menu.shadow, this.effects.shadow.opacity)
	this.addEvent([menu.anchorobj, menu.dropmenu, menu.shadow], function(e){ //MOUSEOVER event for anchor, dropmenu, shadow
		var menu=anylinkcssmenu.menusmap[this._internalID]
		if (this._isanchor && menu.revealtype=="mouseover" && !anylinkcssmenu.isContained(this, e)){ //event for anchor
			anylinkcssmenu.showmenu(menu.id)
			anylinkcssmenu.addState(this, "add")
		}
		else if (typeof this._isanchor=="undefined"){ //event for drop down menu and shadow
			clearTimeout(menu.hidetimer)
		}
	}, "mouseover")
	this.addEvent([menu.anchorobj, menu.dropmenu, menu.shadow], function(e){ //MOUSEOUT event for anchor, dropmenu, shadow
		if (!anylinkcssmenu.isContained(this, e)){
			var menu=anylinkcssmenu.menusmap[this._internalID]
			menu.hidetimer=setTimeout(function(){
				anylinkcssmenu.addState(menu.anchorobj, "remove")
				anylinkcssmenu.hidemenu(menu.id)
			}, anylinkcssmenu.effects.delayhide)
		}
	}, "mouseout")
	this.addEvent([menu.anchorobj, menu.dropmenu], function(e){ //CLICK event for anchor, dropmenu
		var menu=anylinkcssmenu.menusmap[this._internalID]
		if ( this._isanchor && menu.revealtype=="click"){
			if (menu.dropmenu.style.visibility=="visible")
				anylinkcssmenu.hidemenu(menu.id)
			else{
				anylinkcssmenu.addState(this, "add")
				anylinkcssmenu.showmenu(menu.id)
			}
			if (e.preventDefault)
				e.preventDefault()
			return false
		}
		else
			menu.hidetimer=setTimeout(function(){anylinkcssmenu.hidemenu(menu.id)}, anylinkcssmenu.effects.delayhide)
	}, "click")
},

init:function(targetclass){
	this.domready(function(){anylinkcssmenu.trueinit(targetclass)})
},

trueinit:function(targetclass){
	var anchors=this.getElementsByClass(targetclass)
	var preloadimages=this.preloadimages
	for (var i=0; i<anchors.length; i++){
		if (anchors[i].getAttribute('data-image')){ //preload anchor image?
			preloadimages[preloadimages.length]=new Image()
			preloadimages[preloadimages.length-1].src=anchors[i].getAttribute('data-image')
		}
		if (anchors[i].getAttribute('data-overimage')){ //preload anchor image?
			preloadimages[preloadimages.length]=new Image()
			preloadimages[preloadimages.length-1].src=anchors[i].getAttribute('data-overimage')
		}
		this.setupmenu(targetclass, anchors[i], i)
	}
}

}