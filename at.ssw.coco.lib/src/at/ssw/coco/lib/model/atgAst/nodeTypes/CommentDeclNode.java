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
 * implements an ATG-AST-Node for CommentDecl
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class CommentDeclNode extends AbstractAtgAstNode {

	/**
	 * Subnode
	 */
	private TokenExprNode from;
	
	/**
	 * Subnode
	 */
	private TokenExprNode to;
	
	/**
	 * nested flag - if set the Coco/R CommentDecl is NESTED
	 */
	private boolean nested = false;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public CommentDeclNode(int offset) {
		super(offset);
		this.from = null;
		this.to = null;
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public TokenExprNode getFrom() {
		return from;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setFrom(TokenExprNode from) {
		this.from = from;
		this.from.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public TokenExprNode getTo() {
		return to;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setTo(TokenExprNode to) {
		this.to = to;
		this.to.setParent(this);
	}

	/**
	 * Getter for nested CommentDecl flag
	 * @return
	 */
	public boolean isNested() {
		return nested;
	}

	/**
	 * Setter for nested CommentDecl flag
	 * @param nested
	 */
	public void setNested(boolean nested) {
		this.nested = nested;
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);		
		if (visitChildren) {
			if (from != null) 
				from.accept(atgAstVisitor);
			if (to != null)
				to.accept(atgAstVisitor);
		}
	}
}
