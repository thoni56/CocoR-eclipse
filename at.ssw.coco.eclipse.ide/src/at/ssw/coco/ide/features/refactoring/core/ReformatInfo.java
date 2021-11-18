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
 * implements class for Reformatting Info 
 * Data Transfer Object (DTO)
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class ReformatInfo extends RefactorInfo {

	/**
	 * offset of text selection in file
	 */
	private int selectionOffset = -1;
	
	/**
	 * length of text selection in file
	 */
	private int selectionLength = 0;
	
	/**
	 * tabwidth of Editor
	 */
	private int tabwidth = -1;
	
	/**
	 * offset for Semantic Action tabulators
	 */
	private int javaLineOffset = -1;
	
	/**
	 * if set javaLineOffset should be used for 
	 * Semantic Action tabulators, if not an autocomputed
	 * estimate is used (default)
	 */
	private boolean useFixedOffset = false;
	
	/**
	 * if set all productions of file should be 
	 * reformatted, if not the text selection is
	 * used
	 */
	private boolean reformatAllProductions = true;
	
	/**
	 * Constructor
	 * 
	 * @param atgEditor - editor
	 * @param tabwith - tabwith of editor
	 * @param selectionOffset - offset of text selection in file
	 * @param selectionLength - length of text selection in file
	 */
	public ReformatInfo(ATGEditor atgEditor, int tabwith, int selectionOffset, int selectionLength) {
		super(atgEditor);
		this.selectionOffset = selectionOffset;
		this.selectionLength = selectionLength;
		this.tabwidth = tabwith;
		this.javaLineOffset = 40;
		this.useFixedOffset = false;
		this.reformatAllProductions = true;
	}

	/**
	 * Constructor
	 * 
	 * @param atgEditor - editor
	 * @param tabWidth - tabwith of editor
	 */
	public ReformatInfo(ATGEditor atgEditor, int tabWidth) {
		this(atgEditor, tabWidth, -1, 0);
		this.reformatAllProductions = true;
	}

	/**
	 * Getter - tabulator width of editor
	 * 
	 * @return tabwidth
	 */
	public int getTabwith() {
		return tabwidth;
	}

	/**
	 * Getter - offset for Semantic Action tabulators
	 * 
	 * @return javaLineOffset
	 */
	public int getJavaLineOffset() {
		return javaLineOffset;
	}

	/**
	 * Getter - if set all productions of file should be 
	 * reformatted, if not the text selection is used
	 * 
	 * @return reformatAllProductions flag
	 */
	public boolean isReformatAllProductions() {
		return reformatAllProductions;
	}

	/**
	 * Setter - offset for Semantic Action tabulators
	 * 
	 * @param javaLineOffset
	 */
	public void setJavaLineOffset(int javaLineOffset) {
		this.javaLineOffset = javaLineOffset;
	}

	/**
	 * Setter - if set all productions of file should be 
	 * reformatted, if not the text selection is used
	 * 
	 * @param reformatAllProductions
	 */
	public void setReformatAllProductions(boolean reformatAllProductions) {
		this.reformatAllProductions = reformatAllProductions;
	}

	/**
	 * Getter - if set javaLineOffset should be used for 
	 * Semantic Action tabulators, if not an autocomputed
	 * estimate is used (default)
	 * 
	 * @return useFixedOffset
	 */
	public boolean isUseFixedOffset() {
		return useFixedOffset;
	}

	/**
	 * Setter - if set javaLineOffset should be used for 
	 * Semantic Action tabulators, if not an autocomputed
	 * estimate is used (default)

	 * @param useFixedOffset
	 */
	public void setUseFixedOffset(boolean useFixedOffset) {
		this.useFixedOffset = useFixedOffset;
	}

	/**
	 * offset of text selection in file
	 * 
	 * @return selectionOffset
	 */
	public int getSelectionOffset() {
		return selectionOffset;
	}

	/**
	 * length of text selection in file
	 * 
	 * @return selectionLength
	 */
	public int getSelectionLength() {
		return selectionLength;
	}
}
