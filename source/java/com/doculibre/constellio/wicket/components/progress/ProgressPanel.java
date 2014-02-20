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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.wicketstuff.progressbar.ProgressBar;
import org.wicketstuff.progressbar.Progression;
import org.wicketstuff.progressbar.ProgressionModel;

import com.doculibre.constellio.utils.ProgressInfo;

@SuppressWarnings("serial")
public class ProgressPanel extends Panel {
    
    private IModel progressInfoModel;
    private ProgressBar progressBar;
    private ProgressInfoLabel progressInfoLabel;
    private boolean stopped;

    public ProgressPanel(String id, IModel progressInfoModel) {
        super(id);
        setOutputMarkupId(true);
        this.progressInfoModel = progressInfoModel;
        
        progressBar = new ProgressBar("progressBar", new ProgressionModel() {
            @Override
            protected Progression getProgression() {
                ProgressInfo progressInfo = (ProgressInfo) getProgressInfoModel().getObject();
                int progressPercent;
                int total = progressInfo.getTotal();
                int currentIndex = progressInfo.getCurrentIndex();
                if (total > 0) {
                    progressPercent = (int) (100 * (currentIndex + 1) / (double) total);
                } else {
                    progressPercent = 0;
                }
                return new Progression(progressPercent);
            }
        }) {

            @Override
            protected void onFinished(AjaxRequestTarget target) {
            	if (!stopped) {
            		stopped = true;
                    ProgressPanel.this.onFinished(target);
            	}
            }
        };

        progressInfoLabel = new ProgressInfoLabel("progressInfoLabel", progressInfoModel);

        add(progressBar);
        add(progressInfoLabel);
    }

    public IModel getProgressInfoModel() {
        return progressInfoModel;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public ProgressInfoLabel getProgressInfoLabel() {
        return progressInfoLabel;
    }

    @Override
    public void detachModels() {
        progressInfoModel.detach();
        super.detachModels();
    }
    
    public void start(AjaxRequestTarget target) {
    	stopped = false;
        setVisible(true);
        progressBar.start(target);
        progressInfoLabel.start(target);
        if (getParent() != null) {
            target.addComponent(getParent());
        } else {
            target.addComponent(this);
        }
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
