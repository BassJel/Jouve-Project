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
package com.doculibre.constellio.wicket.components.progress;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.time.Duration;
import org.wicketstuff.progressbar.support.DynamicAjaxSelfUpdatingTimerBehavior;

import com.doculibre.constellio.utils.ProgressInfo;

@SuppressWarnings("serial")
public class ProgressInfoLabel extends Label {
	
	private IModel progressInfoModel;
	
	public ProgressInfoLabel(String id, IModel progressInfoModel) {
		super(id, getProgressInfoModel(progressInfoModel));
		this.progressInfoModel = progressInfoModel;
		setOutputMarkupId(true);
	}
	
	private static IModel getProgressInfoModel(final IModel progressInfoModel) {
		return new LoadableDetachableModel() {
			@Override
			protected Object load() {
				ProgressInfo progressInfo = (ProgressInfo) progressInfoModel.getObject();
				int total = progressInfo.getTotal();
				int currentIndex = progressInfo.getCurrentIndex();
				String progressInfoText = (currentIndex + 1) + " / " + total;
				return progressInfoText;
			}
		};
	}
	
	/**
	 * Start the progress bar.
	 *
	 * This must happen in an AJAX request.
	 *
	 * @param target
	 */
	public void start(AjaxRequestTarget target) {
		setVisible(true);
		add(new DynamicAjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND) {
			@Override
			protected void onPostProcessTarget(AjaxRequestTarget target) {
				ProgressInfo progressInfo = (ProgressInfo) progressInfoModel.getObject();
				int currentIndex = progressInfo.getCurrentIndex();
				int total = progressInfo.getTotal();
				if (total != 0 && currentIndex == total - 1) {
					// stop the self update
					stop();
					// do custom action
					onFinished(target);
				}
			}
		});
		if (getParent() != null) {
			target.addComponent(getParent());
		} else {
			target.addComponent(this);
		}
	}

	@Override
	public void detachModels() {
		progressInfoModel.detach();
		super.detachModels();
	}

	/**
	 * Override this method for custom action
	 * on finish of the task when progression.isDone()
	 *
	 * This could be cleaning up or hiding the ProgressBar
	 * for example.
	 *
	 * @param target
	 */
	protected void onFinished(AjaxRequestTarget target) {

	}

}
