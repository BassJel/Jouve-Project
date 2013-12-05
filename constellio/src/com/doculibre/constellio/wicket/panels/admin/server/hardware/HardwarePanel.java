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
package com.doculibre.constellio.wicket.panels.admin.server.hardware;

import org.apache.wicket.markup.html.basic.Label;

import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class HardwarePanel extends AjaxPanel {

	public HardwarePanel(String id) {
		super(id);
		
		int mb = 1024 * 1024;

//		Map<String, String> bigMap = new HashMap<String, String>();
//		for (int i = 0; i < 100000; i++) {
//			bigMap.put(Math.random() + "", Math.random() + "");
//		}

		Runtime runtime = Runtime.getRuntime();

		add(new Label("activeThreads", "" + Thread.activeCount()));
		add(new Label("nbProcessors", "" + runtime.availableProcessors()));

//		System.out.println("##### Heap utilization statistics [MB] #####");

		// Print used memory
		add(new Label("usedMemory", "" + (runtime.totalMemory() - runtime.freeMemory()) / mb));

		// Print free memory
		add(new Label("freeMemory", "" + (runtime.freeMemory()) / mb));

		// Print total available memory
		add(new Label("totalMemory", "" + (runtime.totalMemory()) / mb));

        // Print total available memory
        add(new Label("maxMemory", "" + (runtime.maxMemory()) / mb));

		// Print Maximum available memory
//		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
	}

}
