package com.doculibre.constellio.wicket.panels.admin.indexing;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class SimpleContentPanel extends Panel{
	
	public SimpleContentPanel(String id, String text) {
        super(id);
        add(new Label("message",text));
    }

}
