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
 * implements an ATG-AST-Node for Factor
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class FactorNode extends AbstractAtgAstNode {
	
	/**
	 * enumerates Kind of different FactorNodes
	 * 
	 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
	 *
	 */
	public enum Kind {
		SYMBOL,
		PAR,
		OPTIONAL,
		ARBITRARY_OFTEN,
		SEM_TEXT,
		ANY,
		SYNC
	}
	
	/**
	 * kind of FactorNode
	 */
	private Kind kind;
	
	/**
	 * Subnode
	 */
	private SymbolNode symbolNode;
	
	/**
	 * weakSymbol flag - if set this Symbol has WEAK modifier
	 */
	private boolean weakSymbol = false;
	
	/**
	 * Subnode
	 */
	private AttribsNode attribsNode;
	
	/**
	 * Subnode
	 */
	private ExpressionNode expressionNode;
	
	/**
	 * Subnode
	 */
	private SemTextNode semTextNode;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public FactorNode(int offset) {
		super(offset);
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
	public AttribsNode getAttribsNode() {
		return attribsNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setAttribsNode(AttribsNode attribsNode) {
		this.attribsNode = attribsNode;
		this.attribsNode.setParent(this);
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
	 * weakSymbol flag - if set this Symbol has WEAK modifier
	 * @return
	 */
	public boolean isWeakSymbol() {
		return weakSymbol;
	}

	/**
	 * weakSymbol flag - if set this Symbol has WEAK modifier
	 * @param weak
	 */
	public void setWeakSymbol(boolean weak) {
		this.weakSymbol = weak;
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);
		if (visitChildren) {
			switch (kind) {
			case SYMBOL:
				if (symbolNode != null) 
					symbolNode.accept(atgAstVisitor);
				if (attribsNode != null) 
					attribsNode.accept(atgAstVisitor);				
				break;
			case PAR:
			case OPTIONAL:
			case ARBITRARY_OFTEN:
				if (expressionNode != null) 
					expressionNode.accept(atgAstVisitor);
				break;
			case SEM_TEXT:
				if (semTextNode != null)
					semTextNode.accept(atgAstVisitor);
				break;
			default:
				break;				
			}
		}
	}
}
