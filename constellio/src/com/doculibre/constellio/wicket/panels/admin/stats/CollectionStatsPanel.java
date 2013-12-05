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
package com.doculibre.constellio.wicket.panels.admin.stats;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.doculibre.constellio.entities.CollectionStatsFilter;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.stats.report.StatsConstants;
import com.doculibre.constellio.utils.ConstellioDateUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.renderers.StringResourceChoiceRenderer;

@SuppressWarnings("serial")
public class CollectionStatsPanel extends AjaxPanel {

	private String statsType = StatsConstants.REQUEST_LOG;

	private Panel statsPanel;

	private Date startDate;
	private Date endDate;
	private int rows = 15;
	private boolean includeFederatedCollections = true;
	
	public CollectionStatsPanel(String id, final String collectionName) {
		super(id);

		endDate = new Date();
		startDate = DateUtils.addMonths(endDate, -1);

		Form form = new Form("form") {
			@Override
			protected void onSubmit() {
				statsPanel.replaceWith(statsPanel = new CollectionStatsReportPanel(statsPanel.getId(),
						collectionName, statsType, startDate, endDate, rows, includeFederatedCollections));
			}
		};
		add(form);
		
		IModel queryExcludeRegexpsModel = new Model() {
			@Override
			public Object getObject() {
				String result;
				AdminCollectionPanel adminCollectionPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = adminCollectionPanel.getCollection();
				CollectionStatsFilter statsFilter = collection.getStatsFilter();
				if (statsFilter != null) {
					StringBuffer sb = new StringBuffer();
					Set<String> existingRegexps = statsFilter.getQueryExcludeRegexps();
					for (String existingRegexp : existingRegexps) {
						sb.append(existingRegexp);
						sb.append("\n");
					}
					result = sb.toString();
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public void setObject(Object object) {
				String queryExcludeRegexpsStr = (String) object;
				String[] newRegexpsArray = StringUtils.split(queryExcludeRegexpsStr, "\n");
				List<String> newRegexps = new ArrayList<String>();
				for (String newRegexp : newRegexpsArray) {
					newRegexps.add(newRegexp.trim());
				}
				
				AdminCollectionPanel adminCollectionPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = adminCollectionPanel.getCollection();
				CollectionStatsFilter statsFilter = collection.getStatsFilter();
				if (statsFilter == null) {
					statsFilter = new CollectionStatsFilter();
					statsFilter.setRecordCollection(collection);
					collection.setStatsFilter(statsFilter);
				}
				
				Set<String> existingRegexps = statsFilter.getQueryExcludeRegexps();
				if (!CollectionUtils.isEqualCollection(existingRegexps, newRegexps)) {
					RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
					EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
					if (!entityManager.getTransaction().isActive()) {
						entityManager.getTransaction().begin();
					}
					existingRegexps.clear();
					existingRegexps.addAll(newRegexps);
					collectionServices.makePersistent(collection, false);
					
					entityManager.getTransaction().commit();
				}
			}
		};
		
		form.add(new TextArea("queryExcludeRegexps", queryExcludeRegexpsModel));
		form.add(new DateTextField("startDate", new PropertyModel(this, "startDate"), "yyyy-MM-dd").add(new DatePicker()));
		form.add(new DateTextField("endDate", new PropertyModel(this, "endDate"), "yyyy-MM-dd").add(new DatePicker()));
		form.add(new TextField("rows", new PropertyModel(this, "rows"), Integer.class));
		form.add(new CheckBox("includeFederatedCollections", new PropertyModel(this, "includeFederatedCollections")) {
			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					AdminCollectionPanel adminCollectionPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
					RecordCollection collection = adminCollectionPanel.getCollection();
					visible = collection.isFederationOwner();
				}
				return visible ? visible : false;
			}
		});

		form.add(new DropDownChoice("statsType", new PropertyModel(this, "statsType"),
				StatsConstants.ALL_STATS, new StringResourceChoiceRenderer("statsType", this)));

		form.add(new Label("title", new PropertyModel(this, "statsType")));
		statsPanel = new AjaxLazyLoadPanel("statsPanel") {
			@Override
			public Component getLazyLoadComponent(String markupId) {
				return new CollectionStatsReportPanel(markupId, collectionName, statsType,
						startDate, endDate, rows, includeFederatedCollections);
			}
		};
		form.add(statsPanel);
	}

	public String getStatsType() {
		return statsType;
	}

	public void setStatsType(String typeStatistiques) {
		this.statsType = typeStatistiques;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		if (startDate == null) {
			startDate = new Date();
		}
		this.startDate = ConstellioDateUtils.getBeginningOfDay(startDate);
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		if (endDate == null) {
			endDate = new Date();
		}
		this.endDate = ConstellioDateUtils.getEndOfDay(endDate);
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		if (rows > 0) {
			this.rows = rows;
		}
	}

	public boolean isIncludeFederatedCollections() {
		return includeFederatedCollections;
	}

	public void setIncludeFederatedCollections(boolean includeFederatedCollections) {
		this.includeFederatedCollections = includeFederatedCollections;
	} 

}
