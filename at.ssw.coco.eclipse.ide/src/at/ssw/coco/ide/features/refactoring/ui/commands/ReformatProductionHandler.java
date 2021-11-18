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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import at.ssw.coco.ide.editor.ATGEditor;
import at.ssw.coco.ide.features.refactoring.core.ReformatInfo;
import at.ssw.coco.ide.features.refactoring.core.ReformatProductionRefactoring;
import at.ssw.coco.ide.features.refactoring.ui.wizards.ReformatProductionWizard;

/**
 * implements Command Handler for Reformat ATG productions
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class ReformatProductionHandler extends AbstractHandler {

	/**
	 * active workbench window shell
	 */
	private Shell shell;
	
	/**
	 * Reformat Info DTO
	 */
	private ReformatInfo info;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		
		shell = HandlerUtil.getActiveShell(event);
		ATGEditor atgEditor = (ATGEditor) HandlerUtil.getActiveEditor(event);
		
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		
		if( selection != null && selection instanceof ITextSelection ) {
			info = new ReformatInfo(atgEditor, 
					getTabWidth(), 
					((ITextSelection)selection).getOffset(),
					((ITextSelection)selection).getLength());
			if (((ITextSelection)selection).getLength() > 0) {
				info.setReformatAllProductions(false);
			}
		} else {
			info = new ReformatInfo(atgEditor, 
					getTabWidth());
		}
		
		if (saveAll()) {
			openWizard();
		}
		return null;
	}

	/**
	 * opens the reformat production wizard
	 */
	private void openWizard() {
		Refactoring ref = new ReformatProductionRefactoring(info);		
		ReformatProductionWizard wizard = new ReformatProductionWizard( ref, info );
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation( wizard );
		try {
			op.run( shell, "Reformatting Wizard");
		} catch( final InterruptedException irex ) {
			// operation canceled
		}
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
	
	/**
	 * gets actual tabwidth from preference store
	 * 
	 * @return tabwidth
	 */
	private int getTabWidth() {
		IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore();
		return generalTextStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
	}
}
