/*******************************************************************************
 * Copyright (C) 2011 Martin Preinfalk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *******************************************************************************/

package at.ssw.coco.ide.features.refactoring.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.ITextSelection;

/**
 * implements property tester if selection is a 
 * text selection  
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class TextSelectionTester extends PropertyTester {

	/**
	 * Constructor
	 */
	public TextSelectionTester() {
		super();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof ITextSelection) {
			ITextSelection selection = (ITextSelection) receiver;
			if (property.equals("isNotEmpty")) {
				return selection.getLength() > 0; 
			}
			else if (property.equals("isEmpty")) {
				return selection.getLength() == 0;
			}
		}
		return false;
	}
}
