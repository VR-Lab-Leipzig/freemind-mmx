/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2006  Joerg Mueller, Daniel Polansky, Christian Foltin and others.
 *
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Created on 25.02.2006
 */
/*$Id: BooleanProperty.java,v 1.1.2.1 2006-02-25 23:10:58 christianfoltin Exp $*/
package freemind.common;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import com.jgoodies.forms.builder.DefaultFormBuilder;


public class BooleanProperty extends JCheckBox implements
		PropertyControl, PropertyBean {
	String description;

	String label;

	/**
	 * @param description
	 * @param label
	 */
	public BooleanProperty(String description, String label) {
		super();
		this.description = description;
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public String getLabel() {
		return label;
	}

	public void setValue(String value) {
		if (value == null
				|| !(value.toLowerCase().equals("true") || value
						.toLowerCase().equals("false"))) {
			throw new IllegalArgumentException("Cannot set a boolean to "
					+ value);
		}
		setSelected(value.toLowerCase().equals("true"));
	}

	public String getValue() {
		return isSelected() ? "true" : "false";
	}

	public void layout(DefaultFormBuilder builder, TextTranslator pTranslator) {
		JLabel label = builder
				.append(pTranslator.getText(getLabel()), this);
		label.setToolTipText(pTranslator.getText(getDescription()));
	}

}