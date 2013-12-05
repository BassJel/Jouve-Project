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
package com.doculibre.constellio.wicket.components;

import java.util.Locale;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.dom4j.CDATA;
import org.dom4j.Element;

import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

@SuppressWarnings("serial")
public class ConnectorInstanceConfigFormSnippet extends WebComponent {

    private static final long serialVersionUID = 1L;

    private IModel connectorTypeNameModel;

    private String connectorName;

    private IModel configFormSnippetTextModel;

    public ConnectorInstanceConfigFormSnippet(String id, IModel connectorTypeNameModelP,
        String connectorName) {
        super(id);
        setOutputMarkupId(true);

        this.connectorTypeNameModel = connectorTypeNameModelP;
        this.connectorName = connectorName;

        this.configFormSnippetTextModel = getDefaultFormSnippetTextModel();
    }
    
    private IModel getDefaultFormSnippetTextModel() {
        return new LoadableDetachableModel() {
            @Override
            protected Object load() {
                String configFormSnippetText;
                String connectorTypeName = (String) connectorTypeNameModel.getObject();
                if (connectorTypeName != null) {
                    ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
                        .getConnectorManagerServices();
                    Locale locale = getLocale();
                    ConnectorManager connectorManager = connectorManagerServices.getDefaultConnectorManager();
                    configFormSnippetText = connectorManagerServices.getConfigFormSnippet(connectorManager,
                        connectorTypeName, connectorName, locale);
                } else {
                    configFormSnippetText = null;
                }
                return configFormSnippetText;
            }
        };
    }
    
    public void reset() {
        this.configFormSnippetTextModel = getDefaultFormSnippetTextModel();
    }

    public void setInvalidFormSnippetElement(Element formSnippetElement) {
        if (formSnippetElement != null) {
            CDATA cdata = (CDATA) formSnippetElement.node(0);
            String configFormSnippetText = cdata.getStringValue();
            this.configFormSnippetTextModel = new Model(configFormSnippetText);
        }
    }

    protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        StringBuffer tagBody = new StringBuffer();
        String configFormSnippetText = (String) configFormSnippetTextModel.getObject();
        if (configFormSnippetText != null) {
            tagBody.append(configFormSnippetText);
        }
        replaceComponentTagBody(markupStream, openTag, tagBody);
    }

    @Override
    public boolean isVisible() {
        // String connectorTypeName = (String) connectorTypeNameModel.getObject();
        // return connectorTypeName != null;
        return true;
    }

    @Override
    public void detachModels() {
        configFormSnippetTextModel.detach();
        connectorTypeNameModel.detach();
        super.detachModels();
    }

}
