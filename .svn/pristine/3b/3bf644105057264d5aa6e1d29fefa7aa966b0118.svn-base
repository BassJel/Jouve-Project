/**
 * Constellio, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.doculibre.constellio.wicket.components.holders;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public abstract class ModalLinkHolder extends AjaxPanel {
	
	public static final String LINK_ID = "link";
	
	public static final String LABEL_ID = "label";
	
    private WebMarkupContainer link;

    private Component label;
	
	private ModalWindow modalWindow;
    
    public ModalLinkHolder(String id) {
        super(id);
        IModel labelModel = getLabelModel();
        initComponents(labelModel);
    }
	
	public ModalLinkHolder(String id, IModel labelModel) {
		super(id);
        initComponents(labelModel);
	}
	
	private void initComponents(IModel labelModel) {
        link = newLink(LINK_ID);
        label = newLabel(LABEL_ID, labelModel);
        
        add(link);
        link.add(label);
        
        IModel titleModel = getTitleModel();
        if (titleModel != null) {
        	link.add(new AttributeModifier("title", true, titleModel));
        }
        
        modalWindow = new ModalWindow("modal");
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        add(modalWindow);
	}
    
    public WebMarkupContainer getLink() {
        return link;
    }

    public Component getLabel() {
        return label;
    }
    
    public ModalWindow getModalWindow() {
        return modalWindow;
    }
	
	protected IModel getTitleModel() {
	    return null;
	}
	
	protected IModel getLabelModel() {
	    return null;
	}

    protected Component newLabel(String id, IModel labelModel) {
		return new Label(id, labelModel);
	}
	
	protected abstract WebMarkupContainer newLink(String id);
    
}
