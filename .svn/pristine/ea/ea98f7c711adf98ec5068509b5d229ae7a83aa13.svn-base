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
package com.doculibre.constellio.wicket.pages;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.time.Duration;

import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.global.GlobalThemePlugin;
import com.doculibre.constellio.wicket.session.ConstellioSession;

public class BaseConstellioPage extends WebPage {

	public static final String DISPLAY_LANG_PARAM = "displayLang";
	public static final String CURL_PARAM = "curl";

	private Label titleLabel;
	private Component headerComponent;
	private Component footerComponent;

	public BaseConstellioPage() {
		super();
		initComponents();
	}

	public BaseConstellioPage(PageParameters params) {
		super(params);
		initComponents();
	}

	public BaseConstellioPage(IModel model) {
		super(model);
		initComponents();
	}

	@SuppressWarnings("serial")
	private void initComponents() {
		Component keepAliveComponent = new WebMarkupContainer("keepAlive");
		add(keepAliveComponent);

		if (ConstellioSession.get().getUser() != null) {
			keepAliveComponent.add(new AbstractAjaxTimerBehavior(Duration.seconds(30)) {
				@Override
				protected void onTimer(AjaxRequestTarget target) {
					// Do nothing, will prevent page from expiring
				}
			});
		}
		initStyling();
	}

	protected void initStyling() {
		add(HeaderContributor.forJavaScript(BaseConstellioPage.class, "js/constellio_base.js"));

		GlobalThemePlugin globalThemePlugin = PluginFactory.getPlugin(GlobalThemePlugin.class);
		if (globalThemePlugin != null) {
			List<AbstractHeaderContributor> headerContributors = globalThemePlugin.getHeaderContributors(this);
			for (AbstractHeaderContributor headerContributor : headerContributors) {
				add(headerContributor);
			}
		}

		titleLabel = newTitleLabel("pageTitle");
		if (titleLabel != null) {
			add(titleLabel);
		}

		headerComponent = newHeaderComponent("header");
		if (headerComponent != null) {
			add(headerComponent);
		}

		footerComponent = newFooterComponent("footer");
		if (footerComponent != null) {
			add(footerComponent);
		}
	}

	protected Label newTitleLabel(String id) {
		return new Label(id, new StringResourceModel("pageTitle", this, null));
	}

	protected Component newHeaderComponent(String id) {
		Component headerComponent = null;
		GlobalThemePlugin globalThemePlugin = PluginFactory.getPlugin(GlobalThemePlugin.class);
		if (globalThemePlugin != null) {
			headerComponent = globalThemePlugin.getHeaderComponent(id, this);
		}
		if (headerComponent == null) {
			headerComponent = new WebMarkupContainer(id).setVisible(false);
		}
		return headerComponent;
	}

	protected Component newFooterComponent(String id) {
		Component footerComponent = null;
		GlobalThemePlugin globalThemePlugin = PluginFactory.getPlugin(GlobalThemePlugin.class);
		if (globalThemePlugin != null) {
			footerComponent = globalThemePlugin.getFooterComponent(id, this);
		}
		if (footerComponent == null) {
			footerComponent = new WebMarkupContainer(id).setVisible(false);
		}
		return footerComponent;
	}

	public Label getTitleLabel() {
		return titleLabel;
	}

	public Component getHeaderComponent() {
		return headerComponent;
	}

	public Component getFooterComponent() {
		return footerComponent;
	}

}
