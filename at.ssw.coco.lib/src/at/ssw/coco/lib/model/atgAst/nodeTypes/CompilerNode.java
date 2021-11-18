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
 * implements an ATG-AST-Node for Compiler
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class CompilerNode extends AbstractAtgAstNode {

	/**
	 * Subnode
	 */
	private IdentNode identNode;
	
	/**
	 * Subnode
	 */
	private IdentNode endMarkerIdent;
	
	/**
	 * Subnode
	 */
	private GlobalFieldNode globalFieldNode;
	
	/**
	 * Subnode
	 */
	private ScannerSpecNode scannerSpecNode;
	
	/**
	 * Subnode
	 */
	private ParserSpecNode parserSpecNode;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public CompilerNode(int offset) {
		super(offset);
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
	public IdentNode getEndMarkerIdent() {
		return endMarkerIdent;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setEndMarkerIdent(IdentNode identNode) {
		this.endMarkerIdent = identNode;
		this.endMarkerIdent.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public GlobalFieldNode getGlobalFieldNode() {
		return globalFieldNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setGlobalFieldNode(GlobalFieldNode globalFieldNode) {
		this.globalFieldNode = globalFieldNode;
		this.globalFieldNode.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public ScannerSpecNode getScannerSpecNode() {
		return scannerSpecNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setScannerSpecNode(ScannerSpecNode scannerSpecNode) {
		this.scannerSpecNode = scannerSpecNode;
		this.scannerSpecNode.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public ParserSpecNode getParserSpecNode() {
		return parserSpecNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setParserSpecNode(ParserSpecNode parserSpecNode) {
		this.parserSpecNode = parserSpecNode;
		this.parserSpecNode.setParent(this);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);
		if (visitChildren) {
			if (identNode != null) identNode.accept(atgAstVisitor);
			if (endMarkerIdent != null) endMarkerIdent.accept(atgAstVisitor);
			if (globalFieldNode != null) globalFieldNode.accept(atgAstVisitor);
			if (scannerSpecNode != null) scannerSpecNode.accept(atgAstVisitor);
			if (parserSpecNode != null) parserSpecNode.accept(atgAstVisitor);
		}
	}
}
