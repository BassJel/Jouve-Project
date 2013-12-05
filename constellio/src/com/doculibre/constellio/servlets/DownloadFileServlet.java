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
package com.doculibre.constellio.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.doculibre.constellio.utils.ConstellioSpringUtils;

@SuppressWarnings("serial")
public class DownloadFileServlet extends HttpServlet{

	@Override
	public final void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String file = request.getParameter("file");
		String mimetype = request.getParameter("mimetype");

		String filePathFound = getFilePathFor(file);
		if (filePathFound == null) {
			response.sendError(404);
		} else {
			File fileFound = new File(filePathFound);
			response.setContentType( (mimetype != null) ? mimetype : "application/octet-stream" );
			response.setContentLength( (int)fileFound.length() );
			response.setHeader( "Content-Disposition", "attachment; filename=\"" + fileFound.getName() + "\"" );

			InputStream is = new BufferedInputStream(new FileInputStream(fileFound));
			IOUtils.copy(is, response.getOutputStream());
		}
	}
	
	public static String getFilePathFor(String file) {

		List<String> downloadDirs = ConstellioSpringUtils.getFileDownloadDirs();
		if (downloadDirs == null) {
			throw new RuntimeException("No download dirs specified");
		}
		
		Iterator<String> itDirs = downloadDirs.iterator();
		String filePathFound = null;
		while (filePathFound == null && itDirs.hasNext()) {
			String dir = itDirs.next();
			
			if (new File(file).exists()) {
				if (file.contains(dir)) {
					filePathFound = file;
				}
			} else {
				File test = new File(dir, file);
				if (test.exists()) {
					filePathFound = test.getAbsolutePath();
				}
			}
		}
		return filePathFound;
	}
	
}
