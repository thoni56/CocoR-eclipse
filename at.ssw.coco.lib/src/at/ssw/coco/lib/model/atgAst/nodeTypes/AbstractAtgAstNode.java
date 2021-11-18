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

import at.ssw.coco.lib.model.atgAst.AtgAstRegion;
import at.ssw.coco.lib.model.atgAst.AtgAstVisitor;

/**
 * implements a BaseType for ATG AST Nodes
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public abstract class AbstractAtgAstNode {
	
	/**
	 * parent Node
	 */
	private AbstractAtgAstNode parent;
	
	/**
	 * offset and length
	 */
	private AtgAstRegion region;

	/**
	 * 
	 * @param offset - offset of Node in file
	 */
	public AbstractAtgAstNode(int offset) {
		super();
		this.region = new AtgAstRegion();
		this.region.setOffset(offset);
	}
	
	/**
	 * parent Node Getter
	 * @return parent or null if no parent set
	 */
	public AbstractAtgAstNode getParent() {
		return parent;
	}

	/**
	 * parent Node Setter
	 * @param parent
	 */
	public void setParent(AbstractAtgAstNode parent) {
		this.parent = parent;
	}

	/**
	 * 
	 * @return region of Node in File
	 */
	public AtgAstRegion getRegion() {
		return region;
	}
	
	/**
	 * accepts an IAtgAstVisitor and calls its
	 * visit method with this node as parameter
	 * 
	 * if the node contains subnodes it subsequently calls
	 * their accept methods with the atgAstVisitor as parameter
	 * if and only if the visit method for this node returns true,
	 * if visit method returns false subnodes of this node will 
	 * not be traversed.
	 * 
	 * @param atgAstVisitor
	 */
	public abstract void accept(AtgAstVisitor atgAstVisitor);
}
