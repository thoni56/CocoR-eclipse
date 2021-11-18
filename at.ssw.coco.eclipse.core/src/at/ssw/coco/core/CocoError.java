/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
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

package at.ssw.coco.core;

/**
 * Represents a error returned by the Coco/R Parser
 *
 * @author Christian Wimmer
 * @author Christian Wressnegger <k0356707@students.jku.at>
 *
 */
public class CocoError {
	/** True if it is a warning, false if it is an error. */
	private boolean fWarning;

	/** The line number. */
	private int fLine;

	/** The column number. */
	private int fColumn;

	/** The error message. */
	private String fMessage;

	/**
	 * The Constructor
	 *
	 * @param warning
	 *            True if it is a warning, false if it is an error.
	 * @param line
	 *            The line number.
	 * @param column
	 *            The column of the occurrence.
	 * @param message
	 *            The error message.
	 */
	public CocoError(boolean warning, int line, int column, String message) {
		this.fWarning = warning;
		this.fLine = line;
		this.fColumn = column;
		this.fMessage = message.trim();
	}

	/**
	 * The Constructor.
	 *
	 * @param warning
	 *            True if it is a warning, false if it is an error.
	 * @param message
	 *            The error message.
	 */
	public CocoError(boolean warning, String message) {
		this(warning, -1, -1, message);
	}

	/**
	 * Returns the servity of the error.
	 *
	 * @return True if it is a warning, false if it is an error.
	 */
	public boolean getWarning() {
		return fWarning;
	}

	/**
	 * Returns the line number of the error.
	 *
	 * @return the line number of the error.
	 */
	public int getLine() {
		return fLine;
	}

	/**
	 * Returns the column number of the error.
	 *
	 * @return the column number of the error.
	 */
	public int getColumn() {
		return fColumn;
	}

	/**
	 * Returns the error message.
	 *
	 * @return the error message.
	 */
	public String getMessage() {
		return fMessage;
	}
}
