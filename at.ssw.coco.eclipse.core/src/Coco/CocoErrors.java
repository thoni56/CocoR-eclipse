/*******************************************************************************
 * Copyright (C) 2009 Institute for System Software, JKU Linz
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

package Coco;

import java.util.List;

import at.ssw.coco.core.CocoError;

/**
 * Extends the Coco/R <code>Errors</code> class to store the error messages in a seperate List.
 * <p>
 * Note: This class must be in the package Coco due to limitations of the current Coco/R version.
 *
 * @author Christian Wimmer
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class CocoErrors extends Errors {
	private List<CocoError> errors;

	/**
	 * The Constructor.
	 *
	 * @param errors The <code>List</code> of Coco/R errors & warnings.
	 */
	public CocoErrors(List<CocoError> errors) {
		this.errors = errors;
	}

	@Override
	protected void printMsg(int line, int column, String msg) {
		errors.add(new CocoError(false, line, column, msg));
	}

	@Override
	public void SemErr(String s) {
		errors.add(new CocoError(false, s));
		count++;
	}

	@Override
	public void Warning(int line, int column, String message) {
		errors.add(new CocoError(true, line, column, message));
	}

	@Override
	public void Warning(String s) {
		errors.add(new CocoError(true, s));
	}
}
