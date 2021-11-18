/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
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

package at.ssw.coco.ide.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Shell;

import at.ssw.coco.ide.model.atgmodel.ATGModelAdaptor;

/**
 * Implements the reconciler strategy + extension to build the model of the ATG file and update the folding structure.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 */
public class ATGReconcilerStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	/** The representation of the ATG content */
	private ATGModelAdaptor fATGModel;

	/** The editor including the model representation */
	private ATGEditor fEditor;

	/** The document */
	private IDocument fDocument;

	/** The folding structure rpovider */
	private ATGFoldingStructureProvider fFoldingStructureProvider;

	/**
	 * The Constructor.
	 *
	 * @param editor The ATG editor
	 */
	public ATGReconcilerStrategy(ATGEditor editor) {
		fEditor = editor;
		fFoldingStructureProvider = new ATGFoldingStructureProvider(editor);
	}

	/**
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(IRegion)
	 */
	public void reconcile(IRegion partition) {
		initialReconcile();
	}

	/**
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(DirtyRegion, IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		initialReconcile();
	}

	/**
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {
		fDocument = document;
		fFoldingStructureProvider.setDocument(fDocument);
	}

	/**
	 * The common "reconcile method" which does the mentioned task.
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		Shell shell = fEditor.getSite().getShell();
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				fATGModel = new ATGModelAdaptor(fDocument);

				if (fATGModel != null) {
					fEditor.getATGModelProvider().setATGModel(fATGModel);
					fFoldingStructureProvider.updateFoldingRegions(fATGModel.getElements());
				}
			}
		});
	}

	/**
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
	}
}
