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
 * implements an ATG-AST-Node for ScannerSpec
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class ScannerSpecNode extends AbstractAtgAstNode {

	/**
	 * ignore case flag - if set Coco/R IGNORECASE was parsed
	 */
	private boolean ignoreCase;
	
	/**
	 * Subnode List
	 */
	private ArrayList<SetDeclNode> setDeclNodes;
	
	/**
	 * Subnode List
	 */
	private ArrayList<TokenDeclNode> tokenDeclNodes;
	
	/**
	 * Subnode List
	 */
	private ArrayList<TokenDeclNode> pragmaDeclNodes;
	
	/**
	 * Subnode List
	 */
	private ArrayList<CommentDeclNode> commentDeclNodes;
	
	/**
	 * Subnode List
	 */
	private ArrayList<WhiteSpaceDeclNode> whiteSpaceDeclNodes;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public ScannerSpecNode(int offset) {
		super(offset);
		this.setDeclNodes = new ArrayList<SetDeclNode>();
		this.tokenDeclNodes = new ArrayList<TokenDeclNode>();
		this.pragmaDeclNodes = new ArrayList<TokenDeclNode>();
		this.commentDeclNodes = new ArrayList<CommentDeclNode>();
		this.whiteSpaceDeclNodes = new ArrayList<WhiteSpaceDeclNode>();
		this.setIgnoreCase(false);
	}

	/**
	 * Getter - ignore case flag - if set Coco/R IGNORECASE was parsed
	 * @return
	 */
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	/**
	 * Setter - ignore case flag - if set Coco/R IGNORECASE was parsed
	 * @param ignoreCase
	 */
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	/**
	 * Subnode List Getter
	 * @return unmodifiable Copy of internal Subnode List
	 */
	public List<SetDeclNode> getSetDeclNodes() {
		return Collections.unmodifiableList(setDeclNodes);
	}

	/**
	 * Subnode List Adder, also sets Subnodes parent to this node 
	 * @param termNode
	 */
	public void addSetDeclNode(SetDeclNode setDeclNode) {
		setDeclNode.setParent(this);
		setDeclNodes.add(setDeclNode);
	}

	/**
	 * Subnode List Getter
	 * @return unmodifiable Copy of internal Subnode List
	 */
	public List<TokenDeclNode> getTokenDeclNodes() {
		return Collections.unmodifiableList(tokenDeclNodes);
	}

	/**
	 * Subnode List Adder, also sets Subnodes parent to this node 
	 * @param termNode
	 */
	public void addTokenDeclNode(TokenDeclNode tokenDeclNode) {
		tokenDeclNode.setParent(this);
		tokenDeclNodes.add(tokenDeclNode);
	}

	/**
	 * Subnode List Getter
	 * @return unmodifiable Copy of internal Subnode List
	 */
	public List<TokenDeclNode> getPragmaDeclNodes() {
		return Collections.unmodifiableList(pragmaDeclNodes);
	}

	/**
	 * Subnode List Adder, also sets Subnodes parent to this node 
	 * @param termNode
	 */
	public void addPragmaDeclNode(TokenDeclNode tokenDeclNode) {
		tokenDeclNode.setParent(this);
		pragmaDeclNodes.add(tokenDeclNode);
	}

	/**
	 * Subnode List Getter
	 * @return unmodifiable Copy of internal Subnode List
	 */
	public List<CommentDeclNode> getCommentDeclNodes() {
		return Collections.unmodifiableList(commentDeclNodes);
	}

	/**
	 * Subnode List Adder, also sets Subnodes parent to this node 
	 * @param termNode
	 */
	public void addCommentDeclNode(CommentDeclNode commentDeclNode) {
		commentDeclNode.setParent(this);
		commentDeclNodes.add(commentDeclNode);
	}

	/**
	 * Subnode List Getter
	 * @return unmodifiable Copy of internal Subnode List
	 */
	public List<WhiteSpaceDeclNode> getWhiteSpaceDeclNodes() {
		return Collections.unmodifiableList(whiteSpaceDeclNodes);
	}

	/**
	 * Subnode List Adder, also sets Subnodes parent to this node 
	 * @param termNode
	 */
	public void addWhiteSpaceDeclNode(WhiteSpaceDeclNode whiteSpaceDeclNode) {
		whiteSpaceDeclNode.setParent(this);
		whiteSpaceDeclNodes.add(whiteSpaceDeclNode);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);
		if (visitChildren) {
			for (SetDeclNode n : setDeclNodes) {
				n.accept(atgAstVisitor);
			}
			for (TokenDeclNode n : tokenDeclNodes) {
				n.accept(atgAstVisitor);
			}
			for (TokenDeclNode n : pragmaDeclNodes) {
				n.accept(atgAstVisitor);
			}
			for (CommentDeclNode n : commentDeclNodes) {
				n.accept(atgAstVisitor);
			}
			for (WhiteSpaceDeclNode n : whiteSpaceDeclNodes) {
				n.accept(atgAstVisitor);
			}
		}
	}
}
