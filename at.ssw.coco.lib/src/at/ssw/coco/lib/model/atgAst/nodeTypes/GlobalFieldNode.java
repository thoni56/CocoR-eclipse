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

package at.ssw.coco.lib.model.atgAst.nodeTypes;

import at.ssw.coco.lib.model.atgAst.AtgAstVisitor;

/**
 * implements an ATG-AST-Node for GlobalField
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class GlobalFieldNode extends AbstractAtgAstNode {

	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public GlobalFieldNode(int offset) {
		super(offset);
	}

	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 * @param end - end of node in ATG-File
	 */
	public GlobalFieldNode(int offset, int end) {
		this(offset);
		this.getRegion().setEnd(end);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		atgAstVisitor.visit(this);
	}
}
