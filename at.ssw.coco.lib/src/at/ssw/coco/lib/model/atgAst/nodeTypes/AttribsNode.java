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
 * implements an ATG-AST-Node for Attribs
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class AttribsNode extends AbstractAtgAstNode {

	/**
	 * Subnode
	 */
	private OutAttribsNode outAttribsNode;
	
	/**
	 * Subnode
	 */
	private InAttribsNode inAttribsNode;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public AttribsNode(int offset) {
		super(offset);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public OutAttribsNode getOutAttribsNode() {
		return outAttribsNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setOutAttribsNode(OutAttribsNode outAttribsNode) {
		this.outAttribsNode = outAttribsNode;
		this.outAttribsNode.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public InAttribsNode getInAttribsNode() {
		return inAttribsNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setInAttribsNode(InAttribsNode inAttribsNode) {
		this.inAttribsNode = inAttribsNode;
		this.inAttribsNode.setParent(this);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);		
		if (visitChildren) {
			if (outAttribsNode != null) 
				outAttribsNode.accept(atgAstVisitor);
			if (inAttribsNode != null) 
				inAttribsNode.accept(atgAstVisitor);
		}
	}
}
