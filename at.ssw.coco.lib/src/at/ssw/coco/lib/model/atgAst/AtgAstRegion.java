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

package at.ssw.coco.lib.model.atgAst;

/**
 * Data Structure for Regions in a string consisting of offset and length;
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 */
public class AtgAstRegion {
	
	/**
	 * offset of a node in ATG-File
	 */
	private int offset;
	
	/**
	 * length of a node in ATG-File 
	 */
	private int length;
	
	/**
	 * Default Constructor
	 * 
	 * offset and length initalized with -1
	 */
	public AtgAstRegion() {
		super();
		this.offset = -1;
		this.length = -1;
	}

	/**
	 * Constructor 
	 * 
	 * @param offset of a node in ATG-File
	 * @param length of a node in ATG-File
	 */
	public AtgAstRegion(int offset, int length) {
		super();
		this.offset = offset;
		this.length = length;
	}
	
	/**
	 * Getter - offset of a node in ATG-File
	 * @return
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * Setter - offset of a node in ATG-File
	 * @param offset
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	/**
	 * Getter - length of a node in ATG-File
	 * @return
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * Setter - length of a node in ATG-File
	 * @param length
	 */
	public void setLength(int length) {
		this.length = length;
	}
	
	/**
	 * sets length of node to end - offset, 
	 * be careful to set offset to the right value first!
	 * @param end
	 */
	public void setEnd(int end) {
		length = end - offset;
	}
	
	/**
	 * @return string representation of Region
	 */
	public String toString() {
		return "[offset: " + offset + ", length: " + length + "]";
	}
}
