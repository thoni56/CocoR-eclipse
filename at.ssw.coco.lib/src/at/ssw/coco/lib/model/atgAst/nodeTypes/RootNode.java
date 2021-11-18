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
 * implements an ATG-AST-Node for Root
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class RootNode extends AbstractAtgAstNode {

	/**
	 * Subnode
	 */
	private ImportsNode importsNode;
	
	/**
	 * Subnode
	 */
	private CompilerNode compilerNode;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public RootNode(int offset) {
		super(offset);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public ImportsNode getImportsNode() {
		return importsNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setImportsNode(ImportsNode importsNode) {
		this.importsNode = importsNode;
		this.importsNode.setParent(this);
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public CompilerNode getCompilerNode() {
		return compilerNode;
	}

	/**
	 * Subnode Setter, also sets Subnodes parent to this node 
	 * @param inAttrDeclNode
	 */
	public void setCompilerNode(CompilerNode compilerNode) {
		this.compilerNode = compilerNode;
		this.compilerNode.setParent(this);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);
		if (visitChildren) {
			if (importsNode != null) importsNode.accept(atgAstVisitor);
			if (compilerNode != null) compilerNode.accept(atgAstVisitor);
		}
	}
}
