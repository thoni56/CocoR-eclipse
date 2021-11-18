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
 * implements an ATG-AST-Node for TokenFactor
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class TokenFactorNode extends AbstractAtgAstNode {

	/**
	 * enumerates Kind of different TokenFactorNodes
	 * 
	 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
	 *
	 */
	public enum Kind {
		SYMBOL,
		PAR,
		OPTIONAL,
		ARBITRARY_OFTEN
	}

	/**
	 * Kind of TokenFactor
	 */
	private Kind kind;
	
	/**
	 * Subnode
	 */
	private SymbolNode symbolNode;
	
	/**
	 * Subnode
	 */
	private TokenExprNode tokenExprNode;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public TokenFactorNode(int offset) {
		super(offset);
		this.symbolNode = null;
		this.tokenExprNode = null;
	}

	/**
	 * Getter - Kind of Node
	 * @return
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * Setter - Kind of Node
	 * @param kind
	 */
	public void setKind(Kind kind) {
		this.kind = kind;
	}
	
	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public SymbolNode getSymbolNode() {
		return symbolNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setSymbolNode(SymbolNode symbolNode) {
		this.symbolNode = symbolNode;
		this.symbolNode.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public TokenExprNode getTokenExprNode() {
		return tokenExprNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setTokenExprNode(TokenExprNode tokenExprNode) {
		this.tokenExprNode = tokenExprNode;
		this.tokenExprNode.setParent(this);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);		
		if (visitChildren) {
			if (symbolNode != null && kind == Kind.SYMBOL) 
				symbolNode.accept(atgAstVisitor);
			if (tokenExprNode != null && kind != Kind.SYMBOL)
				tokenExprNode.accept(atgAstVisitor);
		}
	}
}
