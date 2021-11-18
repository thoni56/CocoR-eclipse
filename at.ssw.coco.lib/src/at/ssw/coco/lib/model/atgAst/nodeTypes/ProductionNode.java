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
 * implements an ATG-AST-Node for Production
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class ProductionNode extends AbstractAtgAstNode {

	/**
	 * Subnode
	 */
	private IdentNode identNode;
	
	/**
	 * Subnode
	 */
	private AttrDeclNode attrDeclNode;
	
	/**
	 * Subnode
	 */
	private SemTextNode semTextNode;
	
	/**
	 * Subnode
	 */
	private ExpressionNode expressionNode;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 * @param identNode
	 */
	public ProductionNode(int offset, IdentNode identNode) {
		super(offset);
		setIdentNode(identNode);
	}
	
	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public IdentNode getIdentNode() {
		return identNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setIdentNode(IdentNode identNode) {
		this.identNode = identNode;
		this.identNode.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public AttrDeclNode getAttrDeclNode() {
		return attrDeclNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setAttrDeclNode(AttrDeclNode attrDeclNode) {
		this.attrDeclNode = attrDeclNode;
		this.attrDeclNode.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public SemTextNode getSemTextNode() {
		return semTextNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setSemTextNode(SemTextNode semTextNode) {
		this.semTextNode = semTextNode;
		this.semTextNode.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public ExpressionNode getExpressionNode() {
		return expressionNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setExpressionNode(ExpressionNode expressionNode) {
		this.expressionNode = expressionNode;
		this.expressionNode.setParent(this);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);
		if (visitChildren) {
			if (identNode != null) 
				identNode.accept(atgAstVisitor);
			if (attrDeclNode != null) 
				attrDeclNode.accept(atgAstVisitor);
			if (semTextNode != null) 
				semTextNode.accept(atgAstVisitor);
			if (expressionNode != null) 
				expressionNode.accept(atgAstVisitor);
		}
	}
}
