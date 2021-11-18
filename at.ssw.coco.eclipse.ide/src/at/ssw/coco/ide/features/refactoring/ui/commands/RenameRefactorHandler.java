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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import at.ssw.coco.ide.editor.ATGEditor;
import at.ssw.coco.ide.features.refactoring.core.RenameCharacterSetRefactoring;
import at.ssw.coco.ide.features.refactoring.core.RenamePragmaRefactoring;
import at.ssw.coco.ide.features.refactoring.core.RenameProductionRefactoring;
import at.ssw.coco.ide.features.refactoring.core.RenameRefactorInfo;
import at.ssw.coco.ide.features.refactoring.core.RenameTokenRefactoring;
import at.ssw.coco.ide.features.refactoring.ui.wizards.RenameRefactorWizard;
import at.ssw.coco.lib.model.atgAst.nodeTypes.AbstractAtgAstNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.IdentNode;

/**
 * implements Command Handler for Renaming Refactoring
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class RenameRefactorHandler extends AbstractHandler {

	/**
	 * ATG editor
	 */
	private ATGEditor atgEditor;
	
	/**
	 * Text Selection
	 */
	private ITextSelection textSelection;
	
	/**
	 * active workbench window shell
	 */
	private Shell shell;
	
	/**
	 * Rename Refactor DTO
	 */
	private RenameRefactorInfo info;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		//get selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		
		if( selection != null && selection instanceof ITextSelection ) {
			atgEditor = (ATGEditor) HandlerUtil.getActiveEditor(event);
			shell = HandlerUtil.getActiveShell(event);
			textSelection = (ITextSelection) selection; 
			info = new RenameRefactorInfo(
					textSelection.getOffset(),
					textSelection.getText(), 
					textSelection.getText(), 
					atgEditor);
			if (saveAll()) {
				openWizard();
			}
		}
		return null;
	}

	/**
	 * opens the rename wizard
	 */
	private void openWizard() {
		Refactoring ref = getRefactoring();		
		RenameRefactorWizard wizard = new RenameRefactorWizard( ref, info );
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation( wizard );
		try {
			op.run( shell, "Rename Refactoring Wizard");
		} catch( final InterruptedException irex ) {
			// operation canceled
		}
	}
	
	/**
	 * gets correct Refactoring from text selection - 
	 * @return CharacterSet-, Token-, Pragma- or production-refactoring
	 */
	private Refactoring getRefactoring() {
		
		List<AbstractAtgAstNode> selectedNodes = 
			atgEditor.getAtgAstManager()
				.getAtgAst()
				.getNodes(
						textSelection.getOffset(), 
						textSelection.getLength()); 

		for (AbstractAtgAstNode n : selectedNodes) {
			if (n instanceof IdentNode &&
				((IdentNode)n).getKind() == IdentNode.Kind.CHARACTER_SET) 
			{
				return new RenameCharacterSetRefactoring(info);
			} else if (n instanceof IdentNode &&
				((IdentNode)n).getKind() == IdentNode.Kind.TOKEN) 
			{ 
				return new RenameTokenRefactoring(info);
			
			} else if (n instanceof IdentNode &&
					((IdentNode)n).getKind() == IdentNode.Kind.PRAGMA) 
			{
				return new RenamePragmaRefactoring(info);
			} else if (n instanceof IdentNode &&
					(((IdentNode)n).getKind() == IdentNode.Kind.PRODUCTION ||
					 ((IdentNode)n).getKind() == IdentNode.Kind.COMPILER))
			{ 
				return new RenameProductionRefactoring(info);
			}
		}
		return null;
	}

	/**
	 * saves all files in workspace
	 * 
	 * @return true if done, false if not possible
	 */
	private static boolean saveAll() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return IDE.saveAllEditors( new IResource[] { workspaceRoot }, false );
	}
}
