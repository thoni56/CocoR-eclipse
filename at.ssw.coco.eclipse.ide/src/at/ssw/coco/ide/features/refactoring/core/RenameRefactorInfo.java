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

package at.ssw.coco.ide.features.refactoring.core;

import at.ssw.coco.ide.editor.ATGEditor;

/**
 * implements Refactoring DTO for Renaming Refactorings
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class RenameRefactorInfo extends RefactorInfo {

	/**
	 * offset of name in file
	 */
	private int offset;
	
	/**
	 * string representation of new name
	 */
	private String newName;
	
	/**
	 * string representation of old name
	 */
	private String oldName;
	
	/**
	 * Constructor
	 * 
	 * @param offset of name in file
	 * @param newName new name
	 * @param oldName old name
	 * @param atgEditor ATG editor 
	 */
	public RenameRefactorInfo(int offset, String newName, String oldName,
			ATGEditor atgEditor) {
		super(atgEditor);
		this.offset = offset;
		this.newName = newName;
		this.oldName = oldName;
	}

	/**
	 * Getter - offset of name in file
	 * 
	 * @return
	 */
	public int getOffset() {
		return offset;
	}
  
	/**
	 * Getter - string representation of new name
	 * 
	 * @return
	 */
  	public String getNewName() {
	  	return newName;
  	}

  	/**
	 * Setter - string representation of new name
	 * 
  	 * @param newName
  	 */
  	public void setNewName(String newName) {
	  	this.newName = newName;
  	}

  	/**
  	 * Getter - string representation of old name
  	 * @return
  	 */
  	public String getOldName() {
	  	return oldName;
  	}
}
