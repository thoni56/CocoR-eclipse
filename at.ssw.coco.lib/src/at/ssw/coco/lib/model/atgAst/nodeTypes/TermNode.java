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
 * implements an ATG-AST-Node for Term
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class TermNode extends AbstractAtgAstNode {

	/**
	 * Subnode
	 */
	private ResolverNode resolverNode;
	
	/**
	 * Subnode List
	 */
	private ArrayList<FactorNode> factorNodes;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public TermNode(int offset) {
		super(offset);
		factorNodes = new ArrayList<FactorNode>();
	}

	/**
	 * Subnode Getter 
	 * @return subnode
	 */
	public ResolverNode getResolverNode() {
		return resolverNode;
	}

	/**
	 * Subnode Setter
	 * @param resolverNode
	 */
	public void setResolverNode(ResolverNode resolverNode) {
		this.resolverNode = resolverNode;
		this.resolverNode.setParent(this);
	}

	/**
	 * Subnode List Getter
	 * @return unmodifiable Copy of internal Subnode List
	 */
	public List<FactorNode> getFactorNodes() {
		return Collections.unmodifiableList(factorNodes);
	}

	/**
	 * Subnode List Adder, also sets Subnodes parent to this node 
	 * @param termNode
	 */
	public void addFactorNode(FactorNode factorNode) {
		factorNode.setParent(this);
		factorNodes.add(factorNode);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);		
		if (visitChildren) {
			if (resolverNode != null) 
				resolverNode.accept(atgAstVisitor);
			for (FactorNode n : factorNodes) {
				n.accept(atgAstVisitor);
			}
		}
	}
}
