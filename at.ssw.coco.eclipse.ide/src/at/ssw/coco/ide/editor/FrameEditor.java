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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import at.ssw.coco.ide.PreferenceConstants;
import at.ssw.coco.ide.model.scanners.FramePartitionScanner;
import at.ssw.coco.ide.style.SyntaxManager;
import at.ssw.coco.lib.model.scanners.FramePartitions;

/**
 * Extends a <code>AbstractDecoratedTextEditor</code> to implement more advanced
 * features such as line numbers, rulers, current line highlighting, folding, etc.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class FrameEditor extends AbstractDecoratedTextEditor {
	private static final char[] BRACKETS = new char[] { '(',')', '{','}', '[',']' };

	/** The syntax-manager which organizes the different styles */
	private SyntaxManager syntaxManager;

	/** The projection support */
	private ProjectionSupport fProjectionSupport;

	/** The Constructor */
	public FrameEditor() {
		super();
		syntaxManager = SyntaxManager.Frame.getInstance(getSharedColors());
		setPreferenceStore(createCombinedPreferenceStore());
		setSourceViewerConfiguration(new FrameSourceViewerConfiguration(getPreferenceStore(), syntaxManager));
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class required) {
		if (ProjectionAnnotationModel.class.equals(required)) {
			if (fProjectionSupport != null) {
				Object adapter = fProjectionSupport.getAdapter(getSourceViewer(), required);
				if (adapter != null) {
					return adapter;
				}
			}
		}
		return super.getAdapter(required);
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer(Composite, IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

		// ensure source viewer decoration support has been created and configured
		SourceViewerDecorationSupport support = getSourceViewerDecorationSupport(viewer);
		if (support != null) {
			support.setCharacterPairMatcher(new DefaultCharacterPairMatcher(
					BRACKETS, IDocumentExtension3.DEFAULT_PARTITIONING));
			support.setMatchingCharacterPainterPreferenceKeys(
					PreferenceConstants.EDITOR_MATCHING_BRACKETS,
					PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);
		}

		return viewer;
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		ProjectionViewer projectionViewer = (ProjectionViewer)getSourceViewer();
		fProjectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
		fProjectionSupport.install();
		projectionViewer.doOperation(ProjectionViewer.TOGGLE);
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#doSetInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		setDocumentProvider(new TextFileDocumentProvider() {
			@Override
			public void connect(Object element) throws CoreException {
				super.connect(element);
				IDocument document = getDocument(element);
				if (document != null) {
					if (document.getDocumentPartitioner() == null) {
						IDocumentPartitioner partitioner = new FastPartitioner(
								new FramePartitionScanner(),
								FramePartitions.LEGAL_CONTENT_TYPES);
						partitioner.connect(document);
						document.setDocumentPartitioner(partitioner);
					}
				}
			}
		});

		super.doSetInput(input);
	}

	private IPreferenceStore createCombinedPreferenceStore() {
		return new ChainedPreferenceStore(new IPreferenceStore[] {
				PreferenceConstants.getPreferenceStore(),
				EditorsUI.getPreferenceStore() });
	}
}
