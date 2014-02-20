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
package com.doculibre.constellio.wicket.components.links;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * @author Sven Meier
 * @author Ernesto Reinaldo Barreiro (reiern70@gmail.com)
 */
@SuppressWarnings("serial")
public abstract class AJAXDownload extends AbstractAjaxBehavior
{
	/**
	 * Call this method to initiate the download.
	 */
	public void initiate(AjaxRequestTarget target)
	{
		CharSequence url = getCallbackUrl();

		target.appendJavascript("window.location.href='" + url + "'");
	}

	@Override
	public void onRequest()
	{
		getComponent().getRequestCycle().setRequestTarget(
				new ResourceStreamRequestTarget(getResourceStream(), getFileName()));
	}

	/**
	 * @see ResourceStreamRequestTarget#getFileName()
	 */
	protected String getFileName()
	{
		return null;
	}

	/**
	 * Hook method providing the actual resource stream.
	 */
	protected abstract IResourceStream getResourceStream();
}
