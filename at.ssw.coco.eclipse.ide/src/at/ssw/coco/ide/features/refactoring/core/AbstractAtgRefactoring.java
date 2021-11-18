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

package at.ssw.coco.ide.features.refactoring.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import at.ssw.coco.ide.editor.ATGEditor;

/**
 * implements Abstract Base Class for all ATG refactorings
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public abstract class AbstractAtgRefactoring extends Refactoring {

	/**
	 * Refactoring DTO
	 */
	protected RefactorInfo info;
	
	/**
	 * Constructor
	 * @param info
	 */
	public AbstractAtgRefactoring(RefactorInfo info) {
		super();
		this.info = info;
	}

	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return checkInitialConditions(pm, true);
	}
	
	/**
	 * checks inital conditions before refactoring:
	 * <p>
	 *  - file exists<br>
	 *  - file is an ATG File ('.atg' case insensitive)<br> 
	 *  - file is writeable<br>
	 *  - file is free of Coco/R Erros (only if checkATGErrors is set)<br>
	 *  </p>
	 * @param pm Progress Monitor
	 * @param checkATGErrors if set file will be checked for Coco Errors
	 * @return RefactoringStatus
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm, boolean checkATGErrors)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		
		//Check if file exists
		IFile sourceFile = getFile(info.getAtgEditor());
		if( sourceFile == null || !sourceFile.exists() ) {
			result.addFatalError( CoreTexts.renameRefactor_noSourceFile );
		}
		
		//Check if file is .atg (case insensitive)
		if( !sourceFile.getFileExtension().toLowerCase().equals("atg")) {
			result.addFatalError( CoreTexts.renameRefactor_noAtgFile );
		}
		
		//Check if file is writeable
		if(sourceFile.isReadOnly() ) {
			result.addFatalError( CoreTexts.renameRefactor_roFile );
		}
		
		//Check if file is free of Coco/R Errors
		if (checkATGErrors) {
			try {
				sourceFile.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, pm);
				IMarker[] markers = sourceFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
				
				for (IMarker m : markers){
					if (m.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) == IMarker.SEVERITY_ERROR) {
						result.addFatalError( CoreTexts.renameRefactor_errorsInFile );											
					}
				}
			} catch (CoreException e) {
				result.addFatalError( CoreTexts.renameRefactor_buildFailed );
			}
		}
		return result;
	}
	
	/**
	 * Utility method - gets file from ATGEditor
	 * 
	 * @param atgEditor
	 * @return
	 */
	protected static IFile getFile(ATGEditor atgEditor) {
		IFile result = null;
		IEditorInput input = atgEditor.getEditorInput();
		if(input instanceof IFileEditorInput) {
			result = ((IFileEditorInput) input).getFile();
		}
		return result;
	}
}
