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

import at.ssw.coco.lib.model.atgAst.nodeTypes.AbstractAtgAstNode;

/**
 * Visitor Interface of the ATG - Abstract Syntax Tree
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 */
public interface AtgAstVisitor {
	
	/**
	 * visit method - (visitor pattern) called for all n
	 * ATG AST nodes visited in tree traversal. 
	 * 
	 * @param node
	 * @return true if subnodes of node should be visited, false
	 * if subnodes of node should not be visited
	 */
	public boolean visit(AbstractAtgAstNode node);
}
