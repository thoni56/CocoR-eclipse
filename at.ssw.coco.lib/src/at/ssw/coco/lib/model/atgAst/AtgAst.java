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

package at.ssw.coco.lib.model.atgAst;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import at.ssw.coco.lib.model.atgAst.internal.Parser;
import at.ssw.coco.lib.model.atgAst.internal.Scanner;
import at.ssw.coco.lib.model.atgAst.internal.UTF8BufferUtil;
import at.ssw.coco.lib.model.atgAst.nodeTypes.AbstractAtgAstNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.RootNode;

/**
 * Implements an abstract Syntax tree (AST) from a given ATG-File
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class AtgAst {
	
	/**
	 * root of ATG Abstract Syntax Tree
	 */
	private RootNode root;

	/**
	 * Creates a new Abstract Syntax Tree
	 * 
	 * @param text InputStream of the ATG-File, 
	 * if in is invalid no AST will be generated (getRoot() will return null)
	 */
	public AtgAst(InputStream in) {

		try {
			Scanner scanner = new Scanner(in);
			UTF8BufferUtil.forceUseOfUTF8Buffer(scanner); //workaround for UTF8-files without BOM
			Parser parser = new Parser(scanner);
			parser.Parse();
			root = parser.getRoot();
		} catch (Exception e) {
			e.printStackTrace();
			root = null;
		}
	}
	
	/**
	 * @return root node of the Abstract Syntax Tree, null if 
	 * generation of syntax tree failed
	 */
	public RootNode getRoot() {
		return root;
	}

	/**
	 * search for nodes on offset <code> offset </code> with length <code>length</code>
	 * @param offset
	 * @param length
	 * @return list of nodes that matches position, if no nodes are found returns empty list
	 */
	public List<AbstractAtgAstNode> getNodes(final int offset, final int length) {
		final ArrayList<AbstractAtgAstNode> result = new ArrayList<AbstractAtgAstNode>();
		AtgAstVisitor visitor = new AtgAstVisitor() {
			@Override
			public boolean visit(AbstractAtgAstNode node) {
				if (node.getRegion().getOffset() == offset &&
					node.getRegion().getLength() == length)
				{
					result.add(node);
					return true; //visit children, there may be subnodes with the same offset and length
				}
				if (node.getRegion().getOffset() > offset || 	//no child visit if node starts after offset  
					(node.getRegion().getOffset() + node.getRegion().getLength()) <= offset ) {		//or ends before offset
					return false;						
				}
				return true;
			}
		};
		root.accept(visitor);
		return result;
	}
}
