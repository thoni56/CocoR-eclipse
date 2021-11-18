/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
 * Copyright (C) 2009 Andreas Woess
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import at.ssw.coco.ide.PreferenceConstants;
import at.ssw.coco.ide.model.atgAst.AtgAstManager;
import at.ssw.coco.ide.model.atgmodel.ATGModelAdaptor;
import at.ssw.coco.ide.model.scanners.JavaCodeScanner;
import at.ssw.coco.ide.features.semanticHighlighting.DocumentSynchronizer;
import at.ssw.coco.ide.features.semanticHighlighting.SemanticHighlightingManager;
import at.ssw.coco.ide.features.views.contentoutline.ATGContentOutlinePage;
import at.ssw.coco.ide.style.SyntaxManager;
import at.ssw.coco.ide.style.SyntaxManager.PropertyChangedListener;
import at.ssw.coco.lib.model.atgmodel.ATGModel;
import at.ssw.coco.lib.model.atgmodel.ATGModelListener;
import at.ssw.coco.lib.model.atgmodel.ATGModelProvider;

/**
 * Extends a <code>AbstractDecoratedTextEditor</code> to implement more advanced
 * features such as line numbers, rulers, current line highlighting, folding,
 * etc.
 * 
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */
@SuppressWarnings("restriction")
public class ATGEditor extends AbstractDecoratedTextEditor {

	private class PCListener implements PropertyChangedListener {
		public void propertyChanged(PropertyChangeEvent evt) {
			fJavaCodeScanner.reCreateRules();
			getSourceViewer().invalidateTextPresentation();
		}
	}

	private static final String CONTEXT_ID = "at.ssw.coco.eclipse.ide.editor.context.atg";
	private static final char[] BRACKETS = new char[] { '(', ')', '{', '}',
			'[', ']' };

	/** The syntax-manager which organizes the different styles */
	private SyntaxManager syntaxManager;

	/** The custom ATG content outline page */
	private ATGContentOutlinePage fOutlinePage;

	/** The projection support */
	private ProjectionSupport fProjectionSupport;

	/** The Configuration */
	private ATGConfiguration fConfiguration;

	/** The Manager for semantic highlighting */
	private SemanticHighlightingManager fSemanticHighlightingManager;

	/** The scanner used for syntax coloring */
	protected JavaCodeScanner fJavaCodeScanner = null;
	
	/** The atgAst Manager used for refactoring */
	private AtgAstManager fAtgAstManager = null;

	private final ATGModelProvider fModelProvider = new ATGModelProvider() {
		/** The representation of the ATG content */
		private ATGModelAdaptor fATGModel;
		/** ATG model change listeners */
		private final ListenerList modelListenerList = new ListenerList();

		public void addModelListener(ATGModelListener listener) {
			Assert.isNotNull(listener);
			modelListenerList.add(listener);
		}

		private void fireModelChanged() {
			// fire the event
			Object[] listeners = modelListenerList.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				final ATGModelListener l = (ATGModelListener) listeners[i];
				l.modelChanged();
			}
		}

		public ATGModelAdaptor getATGModel() {
			return fATGModel;
		}

		public void removeModelListener(ATGModelListener listener) {
			modelListenerList.remove(listener);
		}

		public void setATGModel(ATGModel model) {
			fATGModel = (ATGModelAdaptor) model;
			fireModelChanged();
		}
	};

	/** The Constructor */
	public ATGEditor() {
		super();
		syntaxManager = SyntaxManager.ATG.getInstance(getSharedColors());
		setPreferenceStore(createCombinedPreferenceStore());
		fConfiguration = new ATGConfiguration(this, getPreferenceStore(),
				syntaxManager);
		setSourceViewerConfiguration(fConfiguration);
		fSemanticHighlightingManager = new SemanticHighlightingManager();
		syntaxManager.addPropertyChangedListener(new PCListener());
		fAtgAstManager = new AtgAstManager(this);
	}

	private void activateContext() {
		IContextService service = (IContextService) getSite().getService(
				IContextService.class);
		service.activateContext(CONTEXT_ID);
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createActions()
	 */
	@Override
	protected void createActions() {
		super.createActions();

		// All useful actions are already defined in the parent classes. The
		// only one missing is the action necessary for activating content
		// assist with Ctrl+Space.
		IAction action = new ContentAssistAction(
				TextEditorMessages.getBundle(), "ContentAssistAction.", this);
		String id = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
		action.setActionDefinitionId(id);
		setAction(id, action);

		// Quick Outline
		action = new TextOperationAction(TextEditorMessages.getBundle(),
				"ShowOutline.", this,
				ProjectionViewerWithOutline.OPCODE_SHOW_OUTLINE, true);
		action.setActionDefinitionId(ProjectionViewerWithOutline.ACTIONID_SHOW_OUTLINE);
		setAction(ProjectionViewerWithOutline.ACTIONID_SHOW_OUTLINE, action);
	}

	private IPreferenceStore createCombinedPreferenceStore() {
		return new ChainedPreferenceStore(new IPreferenceStore[] {
				PreferenceConstants.getPreferenceStore(),
				EditorsUI.getPreferenceStore() });
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		activateContext();

		if (getPreferenceStore().getBoolean(
				PreferenceConstants.EDITOR_FOLDING_ENABLED)) {
			ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
			if (!viewer.isProjectionMode()) {
				if (viewer.canDoOperation(ProjectionViewer.TOGGLE)) {
					viewer.doOperation(ProjectionViewer.TOGGLE);
				}
			}
		}
		installSematicHighlighting();
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer(Composite,
	 *      IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		ProjectionViewer viewer = new ProjectionViewerWithOutline(parent,
				ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

		fProjectionSupport = new ProjectionSupport(viewer,
				getAnnotationAccess(), getSharedColors());
		// summarize errors and warnings in a collapsed region.
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$

		fProjectionSupport.install();

		// ensure source viewer decoration support has been created and
		// configured
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

	@Override
	protected final void doSetInput(IEditorInput input) throws CoreException {
		setDocumentProvider(new ATGDocumentProvider());
		super.doSetInput(input);
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage = new ATGContentOutlinePage(this,
						getATGModelProvider());
			}
			return fOutlinePage;
		}
		if (ProjectionAnnotationModel.class.equals(required)) {
			if (fProjectionSupport != null) {
				Object adapter = fProjectionSupport.getAdapter(
						getSourceViewer(), required);
				if (adapter != null) {
					return adapter;
				}
			}
		}
		return super.getAdapter(required);
	}

	public ATGModelProvider getATGModelProvider() {
		return fModelProvider;
	}

	public DocumentSynchronizer getSyncer() {
		return fSemanticHighlightingManager.getSyncer();
	}

	private void installSematicHighlighting() {

		SourceViewer sourceViewer = (SourceViewer) getSourceViewer();
		IColorManager colorManager = JavaUI.getColorManager();
		IPreferenceStore preferenceStore = JavaPlugin.getDefault()
				.getCombinedPreferenceStore();
		ATGConfiguration configuration = fConfiguration;

		fSemanticHighlightingManager.install(this, sourceViewer, colorManager,
				preferenceStore, configuration);

	}

	/**
	 * "Marks" the outline page as unused.
	 */
	public void outlinePageClosed() {
		fOutlinePage = null;
	}

	public AtgAstManager getAtgAstManager() {
		return fAtgAstManager;
	}
}
