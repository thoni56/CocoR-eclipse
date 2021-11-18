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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ssw.coco.lib.model.atgAst.AtgAstVisitor;

/**
 * implements an ATG-AST-Node for TokenTerm
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class TokenTermNode extends AbstractAtgAstNode {

	/**
	 * Subnode List
	 */
	private ArrayList<TokenFactorNode> tokenFactorNodes;
	
	/**
	 * Subnode
	 */
	private TokenExprNode contextTokenExprNode;

	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public TokenTermNode(int offset) {
		super(offset);
		this.tokenFactorNodes = new ArrayList<TokenFactorNode>();
		contextTokenExprNode = null;
	}
	
	/**
	 * Subnode List Getter
	 * @return unmodifiable Copy of internal Subnode List
	 */
	public List<TokenFactorNode> getTokenFactorNodes() {
		return Collections.unmodifiableList(tokenFactorNodes);
	}

	/**
	 * Subnode List Adder, also sets Subnodes parent to this node 
	 * @param termNode
	 */
	public void addTokenFactorNode(TokenFactorNode tokenFactorNode) {
		tokenFactorNode.setParent(this);
		tokenFactorNodes.add(tokenFactorNode);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public TokenExprNode getContextTokenExprNode() {
		return contextTokenExprNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setContextTokenExprNode(TokenExprNode contextTokenExprNode) {
		contextTokenExprNode.setParent(this);
		this.contextTokenExprNode = contextTokenExprNode;
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);		
		if (visitChildren) {
			for (TokenFactorNode n : tokenFactorNodes) {
				n.accept(atgAstVisitor);
			}
			if (contextTokenExprNode != null)
				contextTokenExprNode.accept(atgAstVisitor);
		}
	}
}
