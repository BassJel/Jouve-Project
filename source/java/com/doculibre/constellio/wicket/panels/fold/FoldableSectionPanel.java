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
package com.doculibre.constellio.wicket.panels.fold;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public abstract class FoldableSectionPanel extends AjaxPanel {

    private WebMarkupContainer titleSection;
    private WebMarkupContainer toggleLink;
    private Image toggleImg;
    private WebMarkupContainer foldableSectionContainer;
    private Component foldableSection;

    private boolean opened = true;

    protected static final ResourceReference OPENED_IMG_RESOURCE_REFERENCE = new ResourceReference(
        BaseConstellioPage.class, "images/ico_ouvert.png");
    protected static final ResourceReference CLOSED_IMG_RESOURCE_REFERENCE = new ResourceReference(
        BaseConstellioPage.class, "images/ico_ferme.png");

    public FoldableSectionPanel(String id, String titleModelKey) {
        super(id);
        initComponents(new StringResourceModel(titleModelKey, this, null));
    }

    public FoldableSectionPanel(String id, IModel titleModel) {
        super(id);
        initComponents(titleModel);
    }

    private void initComponents(IModel titleModel) {
        titleSection = new WebMarkupContainer("titleSection");
        add(titleSection);
        titleSection.add(new SimpleAttributeModifier("class", getTitleSectionStyleClass()));

        toggleLink = newToggleLink("toggleLink");
        titleSection.add(toggleLink);
        toggleLink.add(new Label("title", titleModel));

        toggleImg = new NonCachingImage("toggleImg") {
            @Override
            protected ResourceReference getImageResourceReference() {
                ResourceReference imageResourceReference;
                if (isOpened()) {
                    imageResourceReference = OPENED_IMG_RESOURCE_REFERENCE;
                } else {
                    imageResourceReference = CLOSED_IMG_RESOURCE_REFERENCE;
                }
                return imageResourceReference;
            }
        };
        toggleImg.setOutputMarkupId(true);
        toggleLink.add(toggleImg);

        foldableSectionContainer = newFoldableSectionContainer("foldableSectionContainer");
        foldableSectionContainer.setOutputMarkupId(true);
        add(foldableSectionContainer);
        foldableSection = newFoldableSection("foldableSection");
        foldableSectionContainer.add(foldableSection);
        foldableSection.setOutputMarkupId(true);
    }
    
    protected String getTitleSectionStyleClass() {
        return "blocAdmin";
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
    
    public WebMarkupContainer getTitleSection() {
        return titleSection;
    }

    public WebMarkupContainer getToggleLink() {
        return toggleLink;
    }

    public Image getToggleImg() {
        return toggleImg;
    }

    public Component getFoldableSection() {
        return foldableSection;
    }

    public WebMarkupContainer getFoldableSectionContainer() {
        return foldableSectionContainer;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && (foldableSection == null || foldableSection.isVisible());
    }

    protected void onToggle(AjaxRequestTarget target) {
    }

    protected WebMarkupContainer newToggleLink(String id) {
        return new AjaxLink(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setOpened(!isOpened());
                onToggle(target);
                target.addComponent(FoldableSectionPanel.this);
            }
        };
    }

    protected WebMarkupContainer newFoldableSectionContainer(String id) {
        return new WebMarkupContainer(id) {
            @Override
            public boolean isVisible() {
                return isOpened();
            }
        };
    }

    protected abstract Component newFoldableSection(String id);

}
