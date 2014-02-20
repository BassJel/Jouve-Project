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
package com.doculibre.constellio.wicket.panels.admin.connector.schedule;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.scheduler.SerializableScheduleTimeInterval;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.ConnectorManagerServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.connector.AddEditConnectorPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.google.enterprise.connector.scheduler.ScheduleTimeInterval;

@SuppressWarnings("serial")
public class TimeIntervalListPanel extends SingleColumnCRUDPanel {

	public TimeIntervalListPanel(String id) {
		super(id);

		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
			    AddEditConnectorPanel addEditConnectorPanel = (AddEditConnectorPanel) findParent(AddEditConnectorPanel.class);
			    return addEditConnectorPanel.getSchedule().getTimeIntervals();
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		ConnectorManagerServices connectorManagerServices = ConstellioSpringUtils
				.getConnectorManagerServices();
		List<ScheduleTimeInterval> timeIntervals = connectorManagerServices.createDefaultTimeIntervals();
		SerializableScheduleTimeInterval timeInterval = new SerializableScheduleTimeInterval(timeIntervals
				.get(0));
		return new AddEditTimeIntervalPanel(id, timeInterval, -1);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
		SerializableScheduleTimeInterval timeInterval = (SerializableScheduleTimeInterval) entityModel
				.getObject();
		return new AddEditTimeIntervalPanel(id, timeInterval, index);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		SerializableScheduleTimeInterval timeInterval = (SerializableScheduleTimeInterval) entity;
		StringBuffer intervalString = new StringBuffer();

		int startHour = timeInterval.getStartTime().getHour();
		if (startHour < 10) {
			intervalString.append("0");
		}
		intervalString.append(startHour);
		intervalString.append(":00");

		intervalString.append("-");

		int endHour = timeInterval.getEndTime().getHour();
		if (endHour < 10) {
			intervalString.append("0");
		}
		intervalString.append(endHour);
		intervalString.append(":00");

		return intervalString.toString();
	}

	@Override
	protected WebMarkupContainer createDeleteLink(String id,
			final IModel entityModel,
			final int timeIntervalIndex) {
		return new AjaxLink(id) {
			@Override
			public void onClick(AjaxRequestTarget target) {
                AddEditConnectorPanel addEditConnectorPanel = (AddEditConnectorPanel) findParent(AddEditConnectorPanel.class);
                List<SerializableScheduleTimeInterval> timeIntervals = addEditConnectorPanel.getSchedule().getTimeIntervals();
                timeIntervals.remove(timeIntervalIndex);
				target.addComponent(TimeIntervalListPanel.this);
				if (timeIntervals.isEmpty()) {
	                addEditConnectorPanel.getDisabledScheduleField().setModelObject(true);
	                target.addComponent(addEditConnectorPanel.getDisabledScheduleField());
				}
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator() {
				return new AjaxCallDecorator() {
					@Override
					public CharSequence decorateScript(CharSequence script) {
						String confirmMsg = getLocalizer().getString("confirmDelete", TimeIntervalListPanel.this);
						return "if (confirm('" + confirmMsg + "')) {" + script + "} else { return false; }";
					}
				};
			}
		};
	}

	@Override
	protected BaseCRUDServices<? extends ConstellioEntity> getServices() {
		return null;
	}

}
