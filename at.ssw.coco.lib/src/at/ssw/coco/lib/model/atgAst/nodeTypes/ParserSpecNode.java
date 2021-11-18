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
 * implements an ATG-AST-Node for ParserSpec
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class ParserSpecNode extends AbstractAtgAstNode {

	/**
	 * Subnode List
	 */
	private ArrayList<ProductionNode> productionNodes;
	
	/**
	 * Constructor
	 * @param offset - offset of node in ATG-File
	 */
	public ParserSpecNode(int offset) {
		super(offset);
		this.productionNodes = new ArrayList<ProductionNode>();
	}

	/**
	 * Subnode List Getter
	 * @return unmodifiable Copy of internal Subnode List
	 */
	public List<ProductionNode> getProductionNodes() {
		return Collections.unmodifiableList(productionNodes);
	}

	/**
	 * Subnode List Adder, also sets Subnodes parent to this node 
	 * @param termNode
	 */
	public void addProductionNode(ProductionNode productionNode) {
		productionNode.setParent(this);
		productionNodes.add(productionNode);
	}

	@Override
	public void accept(AtgAstVisitor atgAstVisitor) {
		boolean visitChildren = atgAstVisitor.visit(this);
		if (visitChildren) {
			for (ProductionNode n : productionNodes) {
				n.accept(atgAstVisitor);
			}
		}
	}
}
