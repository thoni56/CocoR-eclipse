/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
 * Copyright (C) 2009 Andreas Woess
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
package at.ssw.coco.lib.model.atgmodel;


/**
 * Represents the interface for the provider of the ATG model.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public interface ATGModelProvider {
	/**
	 * Adds an <code>ATGModelListener</code> to the "inform-me list".
	 *
	 * @param listener The ATG model listener.
	 */
	void addModelListener(ATGModelListener listener);

	/**
	 * Removes an <code>ATGModelListener</code>.
	 *
	 * @param listener The ATG model listener.
	 */
	void removeModelListener(ATGModelListener listener);

	/**
	 * Return the ATG Model
	 *
	 * @return the ATG Model
	 */
	ATGModel getATGModel();

	/**
	 * Sets the corresponding ATG model.
	 *
	 * @param model The ATG model.
	 */
	void setATGModel(ATGModel model);
}
