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
 * implements an ATG-AST-Node for Symbol
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class SymbolNode extends AbstractAtgAstNode {

	/**
	 * enumerates Kind of different SymbolNodes
	 * 
	 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
	 *
	 */
	public enum Kind {
		IDENT,
		STRING,
		CHAR
	}

	/**
	 * Kind of Symbol
	 */
	private Kind kind;
	
	/**
	 * Subnode
	 */
	private IdentNode identNode;
	
	/**
	 * Kind STRING - string representation
	 */
	private String string;
	
	/**
	 * Kind CHARACTER - char representation
	 */
	private char character;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public SymbolNode(int offset) {
		super(offset);
	}
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 * @param identNode
	 */
	public SymbolNode(int offset, IdentNode identNode) {
		this(offset);
		this.kind = Kind.IDENT;
		this.setIdentNode(identNode);
	}
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 * @param string
	 */
	public SymbolNode(int offset, String string) {
		this(offset);
		this.kind = Kind.STRING;
		this.string = string;
	}

	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 * @param character
	 */
	public SymbolNode(int offset, char character) {
		this(offset);
		this.kind = Kind.CHAR;
		this.character = character;
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
	 * Getter - Kind STRING - string representation
	 * @return
	 */
	public String getString() {
		return string;
	}

	/**
	 * Setter - Kind STRING - string representation
	 * @param string
	 */
	public void setString(String string) {
		this.string = string;
	}

	/**
	 * Getter - Kind CHARACTER - char representation
	 * @return
	 */
	public char getCharacter() {
		return character;
	}

	/**
	 * Setter - Kind CHARACTER - char representation
	 * @param character
	 */
	public void setCharacter(char character) {
		this.character = character;
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);		
		if (visitChildren) {
			if (identNode != null && kind == Kind.IDENT) 
				identNode.accept(atgAstVisitor);
		}
	}
}
