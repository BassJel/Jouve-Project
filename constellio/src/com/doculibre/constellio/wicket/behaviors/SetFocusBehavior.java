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
package com.doculibre.constellio.wicket.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.IFormVisitorParticipant;
import org.apache.wicket.markup.html.form.FormComponent.IVisitor;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Wicket behavior that will set the focus on a given HTML tag if it has a
 * focus() function.
 * 
 * It can also do the following if the focusComponent is a Form object:
 * 
 * If the form has been submitted and there is an invalid field, it will set the
 * focus on this field.
 * 
 * If the form has not been submitted, it will set the focus on its first
 * non-hidden, non-button and visible FormComponent.
 * 
 * @author Vincent Dussault
 */
@SuppressWarnings("serial")
public class SetFocusBehavior extends AbstractBehavior {

	private IModel focusComponentModel;

	/**
	 * If the focusComponent must be determined at render time, use this
	 * constructor.
	 * 
	 * @param focusComponentModel
	 */
	public SetFocusBehavior(IModel focusComponentModel) {
		this.focusComponentModel = focusComponentModel;
	}

	/**
	 * If the focusComponent can be determined at construction time, use this
	 * constructor.
	 * 
	 * @param focusComponent
	 */
	public SetFocusBehavior(Component focusComponent) {
		this(new Model(focusComponent));
	}

	/**
	 * @see org.apache.wicket.behavior.AbstractBehavior#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */
	public void renderHead(final IHeaderResponse response) {
		Component focusComponent = (Component) focusComponentModel.getObject();
		if (focusComponent != null) {
			// If the focusComponent is a form, we will set the focus on one of
			// its fields.
			if (focusComponent instanceof Form) {
				Form form = (Form) focusComponent;

				// If the form has been submitted, we want to set the focus on
				// its first invalid field.
				if (form.isSubmitted()) {
					form.visitFormComponentsPostOrder(new IVisitor() {
						public Object formComponent(
								IFormVisitorParticipant formParticipant) {
							Object result;
							if (formParticipant instanceof FormComponent) {
								FormComponent formComponent = (FormComponent) formParticipant;
								// We cannot set focus on an hidden field or a button.
								if (!(formComponent instanceof HiddenField)
										&& !(formComponent instanceof Button)
										&& !formComponent.isValid()) {
									setFocus(formComponent, response);
									// Will break the loop.
									result = Component.IVisitor.STOP_TRAVERSAL;
								} else {
									// Will continue the loop.
									result = Component.IVisitor.CONTINUE_TRAVERSAL;
								}
							} else {
								// Will continue the loop.
								result = Component.IVisitor.CONTINUE_TRAVERSAL;
							}
							return result;
						}
					});
				} else {
					// Since the form has not been submitted, we will set the
					// focus on its first field.
					form.visitFormComponentsPostOrder(new IVisitor() {
						public Object formComponent(
								IFormVisitorParticipant formParticipant) {
							Object result;
							if (formParticipant instanceof FormComponent) {
								FormComponent formComponent = (FormComponent) formParticipant;
								// We cannot set focus on an hidden field or a button.
								if (!(formComponent instanceof HiddenField)
										&& !(formComponent instanceof Button)
										&& formComponent.isEnabled()
										&& formComponent.isVisible()) {
									setFocus(formComponent, response);
									// Will break the loop.
									result = Component.IVisitor.STOP_TRAVERSAL;
								} else {
									// Will continue the loop.
									result = Component.IVisitor.CONTINUE_TRAVERSAL;
								}
							} else {
								// Will continue the loop.
								result = Component.IVisitor.CONTINUE_TRAVERSAL;
							}
							return result;
						}
					});
				}
			} else {
				// The focusComponent is not a form, so we will set the focus on
				// it.
				setFocus(focusComponent, response);
			}
		}
	}

	/**
	 * Forces the focusComponent to print its markup id and adds a Javascript
	 * function call to the <head> tag that will set the focus on it.
	 * 
	 * @param focusComponent
	 * @param response
	 */
	private void setFocus(Component focusComponent, IHeaderResponse response) {
		// Forces to print the id attribute.
		focusComponent.setOutputMarkupId(true);
		// Sets the id attribute if it does not already exist.
		String focusFormComponentId = focusComponent.getMarkupId(true);
		// Function call added to the <head> tag.
		response.renderOnLoadJavascript("document.getElementById('"
				+ focusFormComponentId + "').focus()");
	}

}
