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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import at.ssw.coco.lib.model.atgAst.AtgAst;
import at.ssw.coco.lib.model.atgAst.AtgAstVisitor;
import at.ssw.coco.lib.model.atgAst.nodeTypes.AbstractAtgAstNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.IdentNode;

/**
 * implements Refactoring for Renaming ATG Character Sets
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class RenameCharacterSetRefactoring extends AbstractAtgRefactoring {

	/**
	 * Rename Refactor Info DTO
	 */
	private final RenameRefactorInfo info;

	/**
	 * Constructor
	 * 
	 * @param info Rename Refactor Info DTO
	 */
	public RenameCharacterSetRefactoring(RenameRefactorInfo info) {
		super(info);
		this.info = info;
	}

	@Override
	public String getName() {
		return CoreTexts.renameCharacterSetRefactor_name;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = super.checkInitialConditions(pm);
		pm.worked(50);
		
		//Check if Selection is a CharacterSet
		AtgAst atgAst = info.getAtgEditor().getAtgAstManager().getAtgAst();
		List<AbstractAtgAstNode> nodes = 
			atgAst.getNodes(info.getOffset(), info.getOldName().length());
		boolean characterSetIdentFound = false;
		for (AbstractAtgAstNode node : nodes) {
			if (node instanceof IdentNode &&
				((IdentNode) node).getKind() == IdentNode.Kind.CHARACTER_SET) 
			{
				characterSetIdentFound = true;
			}			
		}
		if (!characterSetIdentFound)
			result.addFatalError( CoreTexts.renameCharacterSetRefactor_noCharacter );
		
		pm.done();
		return result;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		pm.beginTask( CoreTexts.renameRefactor_checking, 100 );

		// nothing to be checked (yet)

		pm.done();
		return result;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		CompositeChange rootChange = new CompositeChange("CompositeChange");
		try {
			pm.beginTask( CoreTexts.renameRefactor_collectingChanges, 100 );
			
			IFile file = getFile(info.getAtgEditor());
			TextFileChange textFileChange = new TextFileChange( file.getName(), file );
			rootChange.add(textFileChange);
			
			//a file change contains a tree of edits, first add the root of them
			final MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
			textFileChange.setEdit(fileChangeRootEdit);    
			
			AtgAstVisitor renameCharacterVisitor = new AtgAstVisitor() {
				
				@Override
				public boolean visit(AbstractAtgAstNode node) {
					
					if (node instanceof IdentNode) {
						IdentNode identNode = (IdentNode) node;
						if (identNode.getKind() == IdentNode.Kind.CHARACTER_SET &&
							identNode.getIdent().equals(info.getOldName())) {
							//found ident to change
							ReplaceEdit edit = new ReplaceEdit(	
									identNode.getRegion().getOffset(), 
					                identNode.getRegion().getLength(),
					                info.getNewName());			
							fileChangeRootEdit.addChild(edit);
							return false;
						}
					} 
					return true;
				}
			};
			
			//visit atgAst
			info.getAtgEditor()
				.getAtgAstManager()
				.getAtgAst()
				.getRoot()
				.accept(renameCharacterVisitor);
	
		} finally {
			pm.done();
		}
		return rootChange;
	}
}
