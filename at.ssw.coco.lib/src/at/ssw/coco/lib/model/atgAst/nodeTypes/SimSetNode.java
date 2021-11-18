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
 * implements an ATG-AST-Node for SimSet
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class SimSetNode extends AbstractAtgAstNode {
	
	/**
	 * enumerates Kind of different SimSetNodes
	 * 
	 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
	 *
	 */
	public enum Kind {
		STRING,
		IDENT,
		CHAR_RANGE,
		ANY
	}
	
	/**
	 * kind of SimSet
	 */
	private Kind kind;
	
	/**
	 * STRING kind - string representation of SimSet
	 */
	private String string;
	
	/**
	 * IDENT Kind - Subnode
	 */
	private IdentNode identNode;
	
	/**
	 * CHAR_RANGE - from character
	 */
	private char from;
	
	/**
	 * CHAR_RANGE -  to character
	 */
	private char to;

	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public SimSetNode(int offset) {
		super(offset);
	}
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 * @param identNode
	 */
	public SimSetNode(int offset, IdentNode identNode) {
		this(offset);
		this.kind = Kind.IDENT;
		this.getRegion().setEnd(identNode.getRegion().getOffset() + identNode.getRegion().getLength());
		this.setIdentNode(identNode);
	}

	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 * @param string
	 */
	public SimSetNode(int offset, String string) {
		this(offset);
		this.kind = Kind.STRING;
		this.string = string;
	}

	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 * @param from
	 */
	public SimSetNode(int offset, char from) {
		this(offset);
		this.kind = Kind.CHAR_RANGE;
		this.from = from;
		this.to = from;	//has to be set explicitly (default is from for 1 char Ranges)
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
	 * Getter - Kind IDENT - Subnode 
	 * @return subnode
	 */
	public IdentNode getIdentNode() {
		return identNode;
	}

	/**
	 * Setter - Kind IDENT - Subnode,
	 * also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setIdentNode(IdentNode identNode) {
		this.identNode = identNode;
		this.identNode.setParent(this);
	}

	/**
	 * Getter - CHAR_RANGE - from character
	 * @return
	 */
	public char getFrom() {
		return from;
	}

	/**
	 * Setter - CHAR_RANGE - from character
	 * @param from
	 */
	public void setFrom(char from) {
		this.from = from;
	}

	/**
	 * Getter - CHAR_RANGE - to character
	 * @return
	 */
	public char getTo() {
		return to;
	}

	/**
	 * Setter - CHAR_RANGE - to character
	 * @param to
	 */
	public void setTo(char to) {
		this.to = to;
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
