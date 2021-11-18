package at.ssw.coco.plEditor.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import at.ssw.coco.plEditor.features.errorDisplay.ErrorDisplayer;
import at.ssw.coco.plEditor.features.syntaxHighlighting.SyntaxManager;
import at.ssw.coco.plEditor.model.CocoClassLoader;

/**
 * Extends a <code>AbstractDecoratedTextEditor</code> to implement more advanced
 * features such as Syntax Highlighting, Code Completion, Display of Errors, etc.
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
@SuppressWarnings("restriction")
public class CLNGEditor extends AbstractDecoratedTextEditor {
	
	/** The Configuration */
	private CLNGEditorConfiguration fConfiguration;

	/** The Syntax Manager */
	private SyntaxManager fSyntaxManager;
	
	/** Manager for SWT Color Objects */
	private ISharedTextColors fSharedColors;
	
	/** The ClassLoader is responsible for loading the Scanner and Parser Classes, 
	 *  as well as storing the corresponding Constructors and Methods  */
	private CocoClassLoader fClassLoader;


	/** The Constructor */
	public CLNGEditor() {
		super();
		
		fClassLoader = new CocoClassLoader(this);
		
		//Initialize components for Syntax Highlighting
		fSharedColors = EditorsPlugin.getDefault().getSharedTextColors();		
		fSyntaxManager = new SyntaxManager(fSharedColors, this);
		
		//Initialize Source-viewer-configuration
		fConfiguration = new CLNGEditorConfiguration(getPreferenceStore(), fSyntaxManager, this);
		setSourceViewerConfiguration(fConfiguration);	
		
		setEditorContextMenuId("CLNGPopup");
	}	
	
	@Override
	protected void createActions() {
		super.createActions();
		
		IAction action = new ContentAssistAction(TextEditorMessages.getBundle(), "ContentAssistAction.", this);
		String id = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
		action.setActionDefinitionId(id);
		setAction(id, action);
	}

	//Override performSave, so that every time the editor is saved, it's Error Markers are updated
	@Override
	protected void performSave(boolean overwrite, IProgressMonitor progressMonitor) {
		super.performSave(overwrite, progressMonitor);
		
		//initialize ErrorDisplayer
		CocoClassLoader loader = fClassLoader;
		IDocument document = getSourceViewer().getDocument();
		IFile file = getFile();		
		ErrorDisplayer displayer = new ErrorDisplayer(loader, document, file);
		
		//update Error markers
		displayer.computeErrors();
	}
	
	/**
	 * Utility method - gets file from ATGEditor
	 * 
	 * @param atgEditor
	 * @return
	 */
	private IFile getFile() {
		IFile result = null;
		IEditorInput input = this.getEditorInput();
		if(input instanceof IFileEditorInput) {
			result = ((IFileEditorInput) input).getFile();
		}
		return result;
	}
	
	/** Return the Editor's ClassLoader */
	public CocoClassLoader getClassLoader() {
		return fClassLoader;	
	}
	
}