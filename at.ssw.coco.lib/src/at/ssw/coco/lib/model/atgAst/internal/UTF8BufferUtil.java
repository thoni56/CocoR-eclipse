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

package at.ssw.coco.lib.model.atgAst.internal;

/**
 * Utility Class that forces the use of UTF8Buffer in a
 * Coco/R Scanner
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class UTF8BufferUtil {
	
	/**
	 * sets the Buffer of the given scanner to an UTF8Buffer
	 * if not already done.
	 * 
	 * @param scanner
	 */
	public static void forceUseOfUTF8Buffer(Scanner scanner){
		if (!(scanner.buffer instanceof UTF8Buffer)) {
			scanner.buffer = new UTF8Buffer(scanner.buffer);
		}
	}
}
