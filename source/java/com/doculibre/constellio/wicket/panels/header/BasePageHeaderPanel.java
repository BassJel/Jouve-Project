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
package com.doculibre.constellio.wicket.panels.header;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.resource.ByteArrayResource;

import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.session.ConstellioSession;

@SuppressWarnings("serial")
public class BasePageHeaderPanel extends Panel {

    public BasePageHeaderPanel(String id, Page owner) {
        super(id);
        add(newPreHeaderComponent("preHeader", owner));
    }

    protected Component newPreHeaderComponent(String id, Page owner) {
        return new WebMarkupContainer(id).setVisible(false);
    }

    public static Image newLargeLogo(String id) {
        ResourceReference imageResourceReference = new ResourceReference("logoLarge_" + System.currentTimeMillis()) {
            @Override
            protected Resource newResource() {
                SearchInterfaceConfig searchInterfaceConfig = ConstellioSpringUtils
                    .getSearchInterfaceConfigServices().get();
                Resource imageResource;
                byte[] logoBytes = searchInterfaceConfig.getLogoLargeContent();
                // Convert resource path to absolute path relative to base package
                if (logoBytes != null) {
                    imageResource = new ByteArrayResource("image", logoBytes);
                } else {
                    imageResource = PackageResource.get(BaseConstellioPage.class, "images/logo_constellio.gif");
                }
                return imageResource;
            }
        };
        Image image = new NonCachingImage(id, imageResourceReference);
        image.setVisible(!ConstellioSession.get().isPortletMode());
        return image;
    }

    public static Image newSmallLogo(String id) {
        ResourceReference imageResourceReference = new ResourceReference("logoSmall_" + System.currentTimeMillis()) {
            @Override
            protected Resource newResource() {
                SearchInterfaceConfig searchInterfaceConfig = ConstellioSpringUtils
                    .getSearchInterfaceConfigServices().get();
                Resource imageResource;
                byte[] logoBytes = searchInterfaceConfig.getLogoSmallContent();
                // Convert resource path to absolute path relative to base package
                if (logoBytes != null) {
                    imageResource = new ByteArrayResource("image", logoBytes);
                } else {
                    imageResource = PackageResource.get(BaseConstellioPage.class, "images/logo_petit.png");
                }
                return imageResource;
            }
        };
        Image image = new NonCachingImage(id, imageResourceReference);
        image.setVisible(!ConstellioSession.get().isPortletMode());
        return image;
    }

}
