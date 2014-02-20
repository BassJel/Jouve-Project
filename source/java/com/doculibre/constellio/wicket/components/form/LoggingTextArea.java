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
package com.doculibre.constellio.wicket.components.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.time.Duration;

@SuppressWarnings("serial")
public class LoggingTextArea extends TextArea {

    private Date lastLoggingDate;

    private List<String> loggedLines = new ArrayList<String>();

    public LoggingTextArea(String id, final LineProvider lineProvider, long refreshDelayMillis) {
        super(id);
        setModel(new LoadableDetachableModel() {
            @Override
            protected Object load() {
                List<String> potentialNewLines;
                if (lastLoggingDate == null) {
                    potentialNewLines = lineProvider.getFirstLines();
                    lastLoggingDate = new Date();
                } else {
                    potentialNewLines = lineProvider.getLatestLines(DateUtils.addSeconds(lastLoggingDate, -30));
                }
                
                // Remove unnecessary lines to avoid a slllloooooowwww memory leak! ;)
                int maxRows = 100;
                while (!loggedLines.isEmpty() && loggedLines.size() > maxRows) {
                    // Remove oldest
                    loggedLines.remove(0);
                }

                int firstLineToAdd = 0;
                for (String potentialNewLine : potentialNewLines) {
                    if (!loggedLines.contains(potentialNewLine)) {
                        break;
                    } else {
                        firstLineToAdd++;
                    }
                }

                if (firstLineToAdd < potentialNewLines.size()) {
                    loggedLines.addAll(potentialNewLines.subList(firstLineToAdd, potentialNewLines.size()));
                }

                StringBuffer sb = new StringBuffer();
                for (String loggedLine : loggedLines) {
                    sb.insert(0, loggedLine + "\n");
                }

                if (!potentialNewLines.isEmpty()) {
                    lastLoggingDate = new Date();
                }
                return sb.toString();
            }
        });
        initComponents(refreshDelayMillis);
    }

    public LoggingTextArea(String id, final IModel loggedLinesModel, long refreshDelayMillis) {
        super(id);
        setModel(new LoadableDetachableModel() {
            @SuppressWarnings("unchecked")
            @Override
            protected Object load() {
                StringBuffer sb = new StringBuffer();
                List<String> loggedLines = (List<String>) loggedLinesModel.getObject();
                for (String loggedLine : loggedLines) {
                    sb.append(loggedLine + "\n");
                }
                return sb.toString();
            }

            @Override
            protected void onDetach() {
                loggedLinesModel.detach();
                super.onDetach();
            }
        });
        initComponents(refreshDelayMillis);
    }

    private void initComponents(long refreshDelayMillis) {
        add(new AjaxSelfUpdatingTimerBehavior(Duration.milliseconds(refreshDelayMillis)));
        add(new SimpleAttributeModifier("readonly", "readonly"));
    }

    public static interface LineProvider extends Serializable {
        
        List<String> getFirstLines();

        List<String> getLatestLines(Date lastLoggingDate);

    }

}
