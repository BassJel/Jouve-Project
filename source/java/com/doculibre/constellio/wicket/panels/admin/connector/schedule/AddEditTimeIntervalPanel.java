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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.scheduler.SerializableScheduleTimeInterval;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.connector.AddEditConnectorPanel;

@SuppressWarnings("serial")
public class AddEditTimeIntervalPanel extends SaveCancelFormPanel {

	private IModel timeIntervalModel;

	private int timeIntervalIndex;

	public AddEditTimeIntervalPanel(String id, SerializableScheduleTimeInterval timeInterval, int index) {
		super(id, true);
		this.timeIntervalModel = new Model(timeInterval.clone());
		this.timeIntervalIndex = index;

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(timeIntervalModel));

		List<Integer> hours = new ArrayList<Integer>();
		for (int i = 0; i < 24; i++) {
			hours.add(i);
		}
		IChoiceRenderer hoursRenderer = new ChoiceRenderer() {
			@Override
			public Object getDisplayValue(Object object) {
				Integer hour = (Integer) object;
				StringBuffer hourString = new StringBuffer();
				if (hour < 10) {
					hourString.append("0");
				}
				hourString.append(hour);
				hourString.append(":00");
				return hourString.toString();
			}
		};

		form.add(new DropDownChoice("startTime.hour", hours, hoursRenderer));
		form.add(new DropDownChoice("endTime.hour", hours, hoursRenderer));
	}

	private boolean isConflict(int startHour, int endHour) {
		int index = -1;
        AddEditConnectorPanel addEditConnectorPanel = (AddEditConnectorPanel) findParent(AddEditConnectorPanel.class);
        List<SerializableScheduleTimeInterval> timeIntervals = addEditConnectorPanel.getSchedule().getTimeIntervals();

        int i = 0;
		for (SerializableScheduleTimeInterval scheduleTimeInterval : timeIntervals) {
			if (scheduleTimeInterval.getStartTime().getHour() == startHour
					&& scheduleTimeInterval.getEndTime().getHour() == endHour) {
				index = i;
				break;
			}
			i++;
		}
		return index != -1 && index != timeIntervalIndex;
	}

	@Override
	public void detachModels() {
		timeIntervalModel.detach();
		super.detachModels();
	}

	@Override
	protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
			@Override
			protected Object load() {
		        String titleKey = timeIntervalIndex == -1  ? "add" : "edit";
		        return new StringResourceModel(titleKey, AddEditTimeIntervalPanel.this, null).getObject();
			}
        };
	}

	@Override
	protected Component newReturnComponent(String id) {
		return null;
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
        AddEditConnectorPanel addEditConnectorPanel = (AddEditConnectorPanel) findParent(AddEditConnectorPanel.class);
        List<SerializableScheduleTimeInterval> timeIntervals = addEditConnectorPanel.getSchedule().getTimeIntervals();

		SerializableScheduleTimeInterval timeInterval = (SerializableScheduleTimeInterval) timeIntervalModel
				.getObject();
		int startHour = timeInterval.getStartTime().getHour();
		int endHour = timeInterval.getEndTime().getHour();

		if (isConflict(startHour, endHour)) {
		    error(getLocalizer().getString("conflict", this));
		} else if (timeIntervalIndex != -1) {
		    timeIntervals.set(timeIntervalIndex, timeInterval);
		} else {
		    timeIntervals.add(timeInterval);
		}

	}

	@Override
	protected void defaultReturnAction(AjaxRequestTarget target) {
		super.defaultReturnAction(target);
		AddEditConnectorPanel addEditConnectorPanel = (AddEditConnectorPanel) findParent(AddEditConnectorPanel.class);
		addEditConnectorPanel.getSchedule().setDisabled(false);
		TimeIntervalListPanel timeIntervalListPanel = (TimeIntervalListPanel) findParent(TimeIntervalListPanel.class);
		target.addComponent(timeIntervalListPanel);
		target.addComponent(addEditConnectorPanel.getDisabledScheduleField()); 
	}

}
