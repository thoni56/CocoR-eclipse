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
 * implements an ATG-AST-Node for Ident
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class IdentNode extends AbstractAtgAstNode {

	/**
	 * enumerates Kind of different IdentNodes
	 * 
	 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
	 *
	 */
	public enum Kind {
		INVALID,
		COMPILER,
		CHARACTER_SET,
		TOKEN,
		PRAGMA,
		PRODUCTION,
		ATTRIBUTE
	}

	/**
	 * kind of IdentNode
	 */
	private Kind kind;
	
	/**
	 * string representation of ident
	 */
	private String ident;

	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 * @param kind
	 * @param ident
	 */
	public IdentNode(int offset, Kind kind, String ident) {
		super(offset);
		this.ident = ident;
		this.kind = kind;
		this.getRegion().setLength(ident.length());
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
	 * Getter - string representation of ident
	 * @return
	 */
	public String getIdent() {
		return ident;
	}

	/**
	 * Setter - string representation of ident
	 * @param ident
	 */
	public void setIdent(String ident) {
		this.ident = ident;
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		atgAstVisitor.visit(this);
	}
}
