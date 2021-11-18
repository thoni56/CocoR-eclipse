/*******************************************************************************
 * Copyright (C) 2009 Institute for System Software, JKU Linz
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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import at.ssw.coco.ide.model.scanners.JavaCodeScanner;
import at.ssw.coco.ide.style.SyntaxManager;
import at.ssw.coco.lib.model.scanners.FramePartitions;

/**
 * This class bundles the configuration space of a source viewer. Instances of
 * this class are passed to the <code>configure</code> method of
 * <code>ISourceViewer</code>.
 *
 * It is extended to fit a .frame editor's needs. Including own scanners for
 * syntax highlighting, hyperlink detector, strategies and custom reconcilers.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class FrameSourceViewerConfiguration extends TextSourceViewerConfiguration {
	/** The scanner (use for syntax highlighting) for Java syntax */
	private ITokenScanner fJavaScanner;

	/** The syntax-manager which organizes the different styles */
	private SyntaxManager fSyntaxManager;

	public FrameSourceViewerConfiguration(IPreferenceStore preferenceStore, SyntaxManager syntaxManager) {
		super(preferenceStore);
		fSyntaxManager = syntaxManager;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return FramePartitions.LEGAL_CONTENT_TYPES;
	}

	/**
	 * Returns the <code>JavaScanner</code>
	 *
	 * @return the <code>JavaScanner</code>
	 */
	protected ITokenScanner getCodeScanner() {
		if (fJavaScanner == null) {
			fJavaScanner = new JavaCodeScanner(fSyntaxManager);
		}
		return fJavaScanner;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer contentDR = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(contentDR, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(contentDR, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer frameDR = new NonRuleBasedDamagerRepairer(
				fSyntaxManager.getTextAttribute(SyntaxManager.Frame.KEYWORD));
		reconciler.setDamager(frameDR, FramePartitions.FRAME_KEYWORD);
		reconciler.setRepairer(frameDR, FramePartitions.FRAME_KEYWORD);

		NonRuleBasedDamagerRepairer commentDR = new NonRuleBasedDamagerRepairer(
				fSyntaxManager.getTextAttribute(SyntaxManager.Common.COMMENT));
		reconciler.setDamager(commentDR, FramePartitions.MULTI_LINE_COMMENT);
		reconciler.setRepairer(commentDR, FramePartitions.MULTI_LINE_COMMENT);
		reconciler.setDamager(commentDR, FramePartitions.SINGLE_LINE_COMMENT);
		reconciler.setRepairer(commentDR, FramePartitions.SINGLE_LINE_COMMENT);

		NonRuleBasedDamagerRepairer stringDR = new NonRuleBasedDamagerRepairer(
				fSyntaxManager.getTextAttribute(SyntaxManager.Common.STRING));
		reconciler.setDamager(stringDR, FramePartitions.STRING);
		reconciler.setRepairer(stringDR, FramePartitions.STRING);
		reconciler.setDamager(stringDR, FramePartitions.CHARACTER);
		reconciler.setRepairer(stringDR, FramePartitions.CHARACTER);

		return reconciler;
	}
}
