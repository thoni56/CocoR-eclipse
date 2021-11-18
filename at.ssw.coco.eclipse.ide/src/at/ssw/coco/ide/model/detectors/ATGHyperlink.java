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

package at.ssw.coco.ide.model.detectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import at.ssw.coco.ide.editor.ATGEditor;

/**
 * Implements a hyperlink within the ATG editor.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ATGHyperlink implements IHyperlink {
	/** The hyperlink text region */
	private IRegion fSrcRegion;

	/** The destination region */
	private IRegion fDestRegion;

	/**
	 * The Constructor
	 *
	 * @param sourceViewer The ISourceViewer
	 * @param srcRegion The source region
	 * @param destRegion The destination region
	 */
	public ATGHyperlink(IRegion srcRegion, IRegion destRegion) {
		Assert.isNotNull(srcRegion);
		Assert.isNotNull(destRegion);

		fSrcRegion = srcRegion;
		fDestRegion = destRegion;
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
	 */
	public IRegion getHyperlinkRegion() {
		return fSrcRegion;
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
	 */
	public String getHyperlinkText() {
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
	 */
	public String getTypeLabel() {
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
	 */
	public void open() {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editor;
			Assert.isTrue(textEditor instanceof ATGEditor);

			final IRegion oldHighlightRange = textEditor.getHighlightRange();
			final boolean sameHighlightRange = oldHighlightRange == null ? false :
				(oldHighlightRange.getOffset() == fDestRegion.getOffset() && oldHighlightRange.getLength() == fDestRegion.getLength());

			try {
				if (sameHighlightRange)
					// force the cursor being moved
					textEditor.resetHighlightRange();

				textEditor.setHighlightRange(fDestRegion.getOffset(), fDestRegion.getLength(), true);

				if (!sameHighlightRange)
					// mark the location in navigation history
					textEditor.getSelectionProvider().setSelection(new TextSelection(fDestRegion.getOffset(), 0));
			} catch (IllegalArgumentException x) {
				textEditor.resetHighlightRange();
			}
		}
	}
}
