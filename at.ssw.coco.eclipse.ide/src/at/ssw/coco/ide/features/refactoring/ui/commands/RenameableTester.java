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

package at.ssw.coco.ide.features.refactoring.ui.commands;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import at.ssw.coco.ide.editor.ATGEditor;
import at.ssw.coco.lib.model.atgAst.AtgAst;
import at.ssw.coco.lib.model.atgAst.nodeTypes.AbstractAtgAstNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.IdentNode;

/**
 * implements property tester if text selection is renameable  
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class RenameableTester extends PropertyTester {

	/**
	 * Constructor
	 */
	public RenameableTester() {
		super();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof ITextSelection) {
			ITextSelection selection = (ITextSelection) receiver;
			if (property.equals("isRenameable")) {
				return isRenameable(selection);
			}
		}
		return false;
	}
	
	/**
	 * checks if text selection is renameable
	 * 
	 * @param selection
	 * @return true if text selection is renameable
	 */
	private boolean isRenameable(final ITextSelection selection) {
		AtgAst atgAst = getAtgAst();		
		if (atgAst != null) {
			List<AbstractAtgAstNode> selectedNodes = 
				atgAst.getNodes(selection.getOffset(), selection.getLength()); 

			for (AbstractAtgAstNode n : selectedNodes) {
				if (n instanceof IdentNode &&
					(((IdentNode)n).getKind() == IdentNode.Kind.CHARACTER_SET ||
					 ((IdentNode)n).getKind() == IdentNode.Kind.TOKEN ||
					 ((IdentNode)n).getKind() == IdentNode.Kind.COMPILER ||
					 ((IdentNode)n).getKind() == IdentNode.Kind.PRAGMA ||
					 ((IdentNode)n).getKind() == IdentNode.Kind.PRODUCTION)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * gets ATG Abstract Syntax Tree from active editor  
	 * @return
	 */
	private AtgAst getAtgAst() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		IEditorPart part = page.getActiveEditor();
		if (part instanceof ATGEditor) {
		   ATGEditor editor = (ATGEditor)part;
		   return editor.getAtgAstManager().getAtgAst();
		}
		return null;
	}
}
