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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import at.ssw.coco.lib.model.atgAst.AtgAstVisitor;
import at.ssw.coco.lib.model.atgAst.nodeTypes.AbstractAtgAstNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.ProductionNode;

/**
 * implements Refactoring for Reformatting Productions
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class ReformatProductionRefactoring extends AbstractAtgRefactoring {

	/**
	 * Reformat Info DTO
	 */
	private ReformatInfo info;
	
	/**
	 * Constructor
	 * 
	 * @param info Reformat Info DTO
	 */
	public ReformatProductionRefactoring(ReformatInfo info) {
		super(info);
		this.info = info;
	}

	@Override
	public String getName() {
		return CoreTexts.reformatProductionRefactor_name;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return super.checkInitialConditions(pm, false);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		CompositeChange rootChange = new CompositeChange("CompositeChange");
		try {
			pm.beginTask( CoreTexts.reformatProductionRefactor_collectingChanges, 100 );
			
			IFile file = getFile(info.getAtgEditor());
			TextFileChange textFileChange = new TextFileChange( file.getName(), file );
			rootChange.add(textFileChange);
			
			//a file change contains a tree of edits, first add the root of them
			final MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
			textFileChange.setEdit(fileChangeRootEdit);    
			
			AtgAstVisitor renameCharacterVisitor = new AtgAstVisitor() {
				
				@Override
				public boolean visit(AbstractAtgAstNode node) {
					
					if (node instanceof ProductionNode && 		// production node
						(globalReformat() || localSelection(node) || localCursorPos(node)))
					{							
						ProductionNode productionNode = (ProductionNode) node;
						IEditorInput editorInput = info.getAtgEditor().getEditorInput();
						IDocumentProvider documentProvider = info.getAtgEditor().getDocumentProvider();
						IDocument document = documentProvider.getDocument(editorInput);

						String production = null;
						try {
							production = document.get(	
									productionNode.getRegion().getOffset(), 
									productionNode.getRegion().getLength());
							try {
								ATGProductionReformatter atgProductionReformatter = new ATGProductionReformatter(document, productionNode, info);
								production = atgProductionReformatter.reformat();
								ReplaceEdit edit = new ReplaceEdit(	
										productionNode.getRegion().getOffset(), 
										productionNode.getRegion().getLength(),
										production);			
								fileChangeRootEdit.addChild(edit);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
						return false;
					} 
					return true;
				}

				private boolean localCursorPos(AbstractAtgAstNode node) {
					return 	info.getSelectionLength() == 0 &&
							info.getSelectionOffset() >= node.getRegion().getOffset() && 
							info.getSelectionOffset() <= (node.getRegion().getOffset() + node.getRegion().getLength());
				}

				private boolean localSelection(AbstractAtgAstNode node) {
					return 	info.getSelectionLength() > 0 &&
							info.getSelectionOffset() <= node.getRegion().getOffset() &&  
							(info.getSelectionOffset() + info.getSelectionLength()) >= 
								(node.getRegion().getOffset() + node.getRegion().getLength());
				}

				private boolean globalReformat() {
					return info.isReformatAllProductions();
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
