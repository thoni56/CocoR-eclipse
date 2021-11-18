/*******************************************************************************
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
package at.ssw.coco.lib.model.detectors.whitespace;

/**
 * Defines Methods to detect whitespace.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class JavaWhitespaceDetector implements WhitespaceDetector {
	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.detectors.WhitespaceDetector#isWhitespace(char)
	 */
	public boolean isWhitespace(char ch) {
		return Character.isWhitespace(ch);
	}
}
