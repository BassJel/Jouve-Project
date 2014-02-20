package com.doculibre.constellio.wicket.panels.admin.indexing;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class PopUpPage extends WebPage{

	public PopUpPage (String text){
		super();
		Label label = new Label("message",text);
		add(label);
	}
}
