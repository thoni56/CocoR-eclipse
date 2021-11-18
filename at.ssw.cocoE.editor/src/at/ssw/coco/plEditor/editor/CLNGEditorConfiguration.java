package at.ssw.coco.plEditor.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import at.ssw.coco.plEditor.features.contentAssist.codeCompletion.CLNGContentAssistProcessor;
import at.ssw.coco.plEditor.features.syntaxHighlighting.CLNGScanner;
import at.ssw.coco.plEditor.features.syntaxHighlighting.NewDamagerRepairer;
import at.ssw.coco.plEditor.features.syntaxHighlighting.SyntaxManager;

/**
 * This class bundles the configuration space of a source viewer. Instances of this class are passed to the
 * <code>configure</code> method of <code>ISourceViewer</code>. It is extended to fit a CLNG Editors needs. Including own
 * scanners for syntax highlighting and ContentAssistProcessors for Code Completion.
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class CLNGEditorConfiguration extends TextSourceViewerConfiguration {

	private CLNGEditor fEditor;
	
	/** The Scanner used to read the tokens from the clng-file */
	private CLNGScanner fScanner;
	/** The Syntax Manager */
	private SyntaxManager fManager;
	
	
	/**
	 * The Constructor
	 * @param preferenceStore The Plugin's preference Store
	 * @param manager The SyntacManager, that should be used for Syntax Highlighting
	 */
	public CLNGEditorConfiguration(IPreferenceStore preferenceStore, SyntaxManager manager, CLNGEditor editor) {
		super(preferenceStore);
		fEditor = editor;
		fManager = manager;
	}
	
	
	//override and create/set own PresentationReconciler 
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
				
		// generate a new Scanner (and thus reset the Position to 0)
		fScanner = new CLNGScanner(fManager);
		
		// initialize a new Damager/Repairer with the current Scanner
		NewDamagerRepairer dr = new NewDamagerRepairer(fScanner);
				
		// generate the reconciler
		PresentationReconciler reconciler= new PresentationReconciler();	
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		return reconciler;
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		CLNGContentAssistProcessor processor = new CLNGContentAssistProcessor(fEditor.getClassLoader());
		
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		
		
		return assistant;
	}
}
