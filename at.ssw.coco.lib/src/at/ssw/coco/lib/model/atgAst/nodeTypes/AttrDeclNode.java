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
 * implements an ATG-AST-Node for AttrDecl
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class AttrDeclNode extends AbstractAtgAstNode {

	/**
	 * Subnode
	 */
	private OutAttrDeclNode outAttrDeclNode;
	
	/**
	 * Subnode
	 */
	private InAttrDeclNode inAttrDeclNode;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public AttrDeclNode(int offset) {
		super(offset);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public OutAttrDeclNode getOutAttrDeclNode() {
		return outAttrDeclNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param outAttrDeclNode
	 */
	public void setOutAttrDeclNode(OutAttrDeclNode outAttrDeclNode) {
		this.outAttrDeclNode = outAttrDeclNode;
		this.outAttrDeclNode.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public InAttrDeclNode getInAttrDeclNode() {
		return inAttrDeclNode;
	}


	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setInAttrDeclNode(InAttrDeclNode inAttrDeclNode) {
		this.inAttrDeclNode = inAttrDeclNode;
		this.inAttrDeclNode.setParent(this);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);		
		if (visitChildren) {
			if (outAttrDeclNode != null) 
				outAttrDeclNode.accept(atgAstVisitor);
			if (inAttrDeclNode != null) 
				inAttrDeclNode.accept(atgAstVisitor);
		}
	}
}
