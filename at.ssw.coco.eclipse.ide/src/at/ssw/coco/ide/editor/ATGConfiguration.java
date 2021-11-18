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



import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import at.ssw.coco.ide.features.contentAssist.codeCompletion.semanticCompletion.JavaCompletionProcessor;
import at.ssw.coco.ide.features.contentAssist.codeCompletion.staticCompletion.ATGCompletionProcessor;
import at.ssw.coco.ide.features.views.contentoutline.ATGOutlineInformationControl;
import at.ssw.coco.ide.model.detectors.ATGHyperlinkDetector;
import at.ssw.coco.ide.model.scanners.ATGLangScanner;
import at.ssw.coco.ide.model.scanners.JavaCodeScanner;
import at.ssw.coco.ide.style.SyntaxManager;
import at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion.CharactersSegmentProposalProvider;
import at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion.CommentsSegmentProposalProvider;
import at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion.DefaultProposalProvider;
import at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion.IgnoreCaseSegmentProposalProvider;
import at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion.IgnoreSegmentProposalProvider;
import at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion.PragmasSegmentProposalProvider;
import at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion.ProductionsSegmentProposalProvider;
import at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion.TokensSegmentProposalProvider;
import at.ssw.coco.lib.model.scanners.ATGPartitions;


/**
 * This class bundles the configuration space of a source viewer. Instances of this class are passed to the
 * <code>configure</code> method of <code>ISourceViewer</code>. It is extended to fit a ATG Editors needs. Including own
 * scanners for syntax highlighting, hyperlink detector, strategies and custom reconcilers.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ATGConfiguration extends TextSourceViewerConfiguration implements
		ProjectionViewerWithOutline.OutlinePresenterProvider {

	/** The array ATG Hyperlink detector */
	private IHyperlinkDetector[] fHyperlinkDetectors;

	/** The scanner (use for syntax highlighting) for the ATG syntax */
	private ITokenScanner fATGScanner;

	/** The scanner (use for syntax highlighting) for java syntax */
	private ITokenScanner fJavaScanner;

	/** The syntax-manager which organizes the different styles */
	private SyntaxManager fSyntaxManager;

	/** The editor including the model representation */
	private ATGEditor fEditor;



	public ATGConfiguration(ATGEditor editor, IPreferenceStore preferenceStore, SyntaxManager syntaxManager) {
		super(preferenceStore);
		fEditor = editor;
		fSyntaxManager = syntaxManager;

	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		
		return ATGPartitions.getLegalContentTypes();
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
	/*
	 * This Method creates the  ContentAssistant that is necessary for Code Completion
	 * Defines a "Content Assist Processor" for every ATG File Partition that should 
	 * support code Completion and installs it for distinct Partition
	 */
		ContentAssistant assistant = new ContentAssistant();
		IContentAssistProcessor processor;
		
		//processor = new ATGContentAssistProcessor(fEditor.getATGModelProvider());
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		processor = new ATGCompletionProcessor(fEditor.getATGModelProvider(), new IgnoreCaseSegmentProposalProvider());
		assistant.setContentAssistProcessor(processor, ATGPartitions.IGNORECASE_SEGMENT);
		
		processor = new ATGCompletionProcessor(fEditor.getATGModelProvider(), new CharactersSegmentProposalProvider());
		assistant.setContentAssistProcessor(processor, ATGPartitions.CHARACTERS_SEGMENT);
		
		processor = new ATGCompletionProcessor(fEditor.getATGModelProvider(), new TokensSegmentProposalProvider());
		assistant.setContentAssistProcessor(processor, ATGPartitions.TOKENS_SEGMENT);
		
		processor = new ATGCompletionProcessor(fEditor.getATGModelProvider(), new PragmasSegmentProposalProvider());
		assistant.setContentAssistProcessor(processor, ATGPartitions.PRAGMAS_SEGMENT);
		
		processor = new ATGCompletionProcessor(fEditor.getATGModelProvider(), new CommentsSegmentProposalProvider());
		assistant.setContentAssistProcessor(processor, ATGPartitions.COMMENTS_SEGMENT);
		
		processor = new ATGCompletionProcessor(fEditor.getATGModelProvider(), new IgnoreSegmentProposalProvider());
		assistant.setContentAssistProcessor(processor, ATGPartitions.IGNORE_SEGMENT);
		
		processor = new ATGCompletionProcessor(fEditor.getATGModelProvider(), new ProductionsSegmentProposalProvider());
		assistant.setContentAssistProcessor(processor, ATGPartitions.PRODUCTIONS_SEGMENT);
		
		processor = new ATGCompletionProcessor(fEditor.getATGModelProvider(), new DefaultProposalProvider());
		assistant.setContentAssistProcessor(processor, ATGPartitions.DEFAULT);
		
		processor = new ATGCompletionProcessor(fEditor.getATGModelProvider(), new IgnoreCaseSegmentProposalProvider());
		processor = new JavaCompletionProcessor(fEditor.getATGModelProvider(), fEditor);
		assistant.enableAutoActivation(true);
		assistant.setContentAssistProcessor(processor, ATGPartitions.PARSER_CODE);
		
		
		assistant.enableColoredLabels(true);
		//assistant.enablePrefixCompletion(true);
		//assistant.enableAutoInsert(true);
		return assistant;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(ISourceViewer)
	 */
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (fHyperlinkDetectors == null) {
			fHyperlinkDetectors = new IHyperlinkDetector[] { new ATGHyperlinkDetector(fEditor.getATGModelProvider()) };
		}
		return fHyperlinkDetectors;
	}

	/**
	 * Returns the <code>ATGLangScanner</code>
	 *
	 * @return the <code>ATGLangScanner</code>
	 */
	protected ITokenScanner getATGScanner() {
		if (fATGScanner == null) {
			fATGScanner = new ATGLangScanner(fSyntaxManager);
		}
		return fATGScanner;
	}

	/**
	 * Returns the <code>JavaCodeScanner</code>
	 *
	 * @return the <code>JavaCodeScanner</code>
	 */
	protected ITokenScanner getJavaCodeScanner() {
		if (fJavaScanner == null) {
			fJavaScanner = new JavaCodeScanner(fSyntaxManager);
		}
		return fJavaScanner;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		ATGReconcilerStrategy strategy = new ATGReconcilerStrategy(fEditor);
		MonoReconciler reconciler = new MonoReconciler(strategy, false);
		reconciler.setProgressMonitor(new NullProgressMonitor());
		reconciler.setDelay(500);
		return reconciler;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		/*
		 * This Method is used for Syntax/Semantic Highlighting. 
		 * Every ATG Partition that should support Highlighting has to register a damager and a repairer.
		 */
		
		PresentationReconciler reconciler = new ATGPresentationReconciler();

		// Coco code
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getATGScanner());
		reconciler.setDamager(dr, ATGPartitions.DEFAULT);
		reconciler.setRepairer(dr, ATGPartitions.DEFAULT);
		
		reconciler.setDamager(dr, ATGPartitions.IGNORECASE_SEGMENT);
		reconciler.setRepairer(dr, ATGPartitions.IGNORECASE_SEGMENT);
		
		reconciler.setDamager(dr, ATGPartitions.CHARACTERS_SEGMENT);
		reconciler.setRepairer(dr, ATGPartitions.CHARACTERS_SEGMENT);

		reconciler.setDamager(dr, ATGPartitions.TOKENS_SEGMENT);
		reconciler.setRepairer(dr, ATGPartitions.TOKENS_SEGMENT);
		
		reconciler.setDamager(dr, ATGPartitions.PRAGMAS_SEGMENT);
		reconciler.setRepairer(dr, ATGPartitions.PRAGMAS_SEGMENT);

		reconciler.setDamager(dr, ATGPartitions.COMMENTS_SEGMENT);
		reconciler.setRepairer(dr, ATGPartitions.COMMENTS_SEGMENT);

		reconciler.setDamager(dr, ATGPartitions.IGNORE_SEGMENT);
		reconciler.setRepairer(dr, ATGPartitions.IGNORE_SEGMENT);
		
		reconciler.setDamager(dr, ATGPartitions.PRODUCTIONS_SEGMENT);
		reconciler.setRepairer(dr, ATGPartitions.PRODUCTIONS_SEGMENT);

		// COMPILER statement
		reconciler.setDamager(dr, ATGPartitions.COMPILER_KEYWORD);
		reconciler.setRepairer(dr, ATGPartitions.COMPILER_KEYWORD);
		reconciler.setDamager(dr, ATGPartitions.COMPILER_IDENT);
		reconciler.setRepairer(dr, ATGPartitions.COMPILER_IDENT);

		// inline code delimiting tags
		NonRuleBasedDamagerRepairer tagDR = new NonRuleBasedDamagerRepairer(
				fSyntaxManager.getTextAttribute(SyntaxManager.ATG.INLINE_CODE_TAG));
		reconciler.setDamager(tagDR, ATGPartitions.INLINE_CODE_START);
		reconciler.setRepairer(tagDR, ATGPartitions.INLINE_CODE_START);
		reconciler.setDamager(tagDR, ATGPartitions.INLINE_CODE_END);
		reconciler.setRepairer(tagDR, ATGPartitions.INLINE_CODE_END);

		// inline code
		ITokenScanner javaCodeScanner = getJavaCodeScanner();
		fEditor.fJavaCodeScanner = (JavaCodeScanner)javaCodeScanner;
		dr = new DefaultDamagerRepairer(javaCodeScanner);
		reconciler.setDamager(dr, ATGPartitions.IMPORTS);
		reconciler.setRepairer(dr, ATGPartitions.IMPORTS);

		dr = new DefaultDamagerRepairer(javaCodeScanner);
		reconciler.setDamager(dr, ATGPartitions.PARSER_CODE);
		reconciler.setRepairer(dr, ATGPartitions.PARSER_CODE);

		dr = new DefaultDamagerRepairer(javaCodeScanner);
		reconciler.setDamager(dr, ATGPartitions.INLINE_CODE);
		reconciler.setRepairer(dr, ATGPartitions.INLINE_CODE);
		reconciler.setDamager(dr, ATGPartitions.PRAGMAS_INLINE_CODE);
		reconciler.setRepairer(dr, ATGPartitions.PRAGMAS_INLINE_CODE);
		reconciler.setDamager(dr, ATGPartitions.PRODUCTIONS_INLINE_CODE);
		reconciler.setRepairer(dr, ATGPartitions.PRODUCTIONS_INLINE_CODE);
		

		NonRuleBasedDamagerRepairer commentDR = new NonRuleBasedDamagerRepairer(
				fSyntaxManager.getTextAttribute(SyntaxManager.Common.COMMENT));

		{ // comments
			String contentTypes[] = new String[] {
					ATGPartitions.MULTI_LINE_COMMENT,
					ATGPartitions.SINGLE_LINE_COMMENT,
					
					ATGPartitions.MULTI_LINE_COMMENT_COMPILER_IDENT,
					ATGPartitions.SINGLE_LINE_COMMENT_COMPILER_IDENT,
					
					ATGPartitions.MULTI_LINE_COMMENT_IGNORECASE_SEGMENT,
					ATGPartitions.SINGLE_LINE_COMMENT_IGNORECASE_SEGMENT,
					
					ATGPartitions.MULTI_LINE_COMMENT_CHARACTERS_SEGMENT,
					ATGPartitions.SINGLE_LINE_COMMENT_CHARACTERS_SEGMENT,
					
					ATGPartitions.MULTI_LINE_COMMENT_TOKENS_SEGMENT,
					ATGPartitions.SINGLE_LINE_COMMENT_TOKENS_SEGMENT,
					
					ATGPartitions.MULTI_LINE_COMMENT_PRAGMAS_SEGMENT,
					ATGPartitions.SINGLE_LINE_COMMENT_PRAGMAS_SEGMENT,
					
					ATGPartitions.MULTI_LINE_COMMENT_COMMENTS_SEGMENT,
					ATGPartitions.SINGLE_LINE_COMMENT_COMMENTS_SEGMENT,
					
					ATGPartitions.MULTI_LINE_COMMENT_IGNORE_SEGMENT,
					ATGPartitions.SINGLE_LINE_COMMENT_IGNORE_SEGMENT,
					
					ATGPartitions.MULTI_LINE_COMMENT_PRODUCTIONS_SEGMENT,
					ATGPartitions.SINGLE_LINE_COMMENT_PRODUCTIONS_SEGMENT,
					
					};
			for (String ct : contentTypes) {
				reconciler.setDamager(commentDR, ct);
				reconciler.setRepairer(commentDR, ct);
			}
		}

		NonRuleBasedDamagerRepairer javaSLCommentDR = new NonRuleBasedDamagerRepairer(
				fSyntaxManager.getTextAttribute(SyntaxManager.Java.SINGLE_LINE_COMMENT));

		{ // java comments single line
			String contentTypes[] = new String[] {
					ATGPartitions.SINGLE_LINE_COMMENT_IMPORTS,
					ATGPartitions.SINGLE_LINE_COMMENT_PARSER_CODE,
			};
			for (String ct : contentTypes) {
				reconciler.setDamager(javaSLCommentDR, ct);
				reconciler.setRepairer(javaSLCommentDR, ct);
			}
		}

		NonRuleBasedDamagerRepairer javaMLCommentDR = new NonRuleBasedDamagerRepairer(
				fSyntaxManager.getTextAttribute(SyntaxManager.Java.MULTI_LINE_COMMENT));

		{ // java comments multi line
			String contentTypes[] = new String[] {
					ATGPartitions.MULTI_LINE_COMMENT_IMPORTS,
					ATGPartitions.MULTI_LINE_COMMENT_PARSER_CODE,
					
			};
			for (String ct : contentTypes) {
				reconciler.setDamager(javaMLCommentDR, ct);
				reconciler.setRepairer(javaMLCommentDR, ct);
			}
		}

		NonRuleBasedDamagerRepairer stringDR = new NonRuleBasedDamagerRepairer(
				fSyntaxManager.getTextAttribute(SyntaxManager.Common.STRING));

		{ // strings
			String contentTypes[] = new String[] {
					ATGPartitions.STRING,
					ATGPartitions.CHARACTER,
					
					ATGPartitions.STRING_IGNORECASE_SEGMENT,
					ATGPartitions.CHARACTER_IGNORECASE_SEGMENT,
					
					ATGPartitions.STRING_CHARACTERS_SEGMENT,
					ATGPartitions.CHARACTER_CHARACTERS_SEGMENT,
					
					ATGPartitions.STRING_TOKENS_SEGMENT,
					ATGPartitions.CHARACTER_TOKENS_SEGMENT,
					
					ATGPartitions.STRING_PRAGMAS_SEGMENT,
					ATGPartitions.CHARACTER_PRAGMAS_SEGMENT,
					
					ATGPartitions.STRING_COMMENTS_SEGMENT,
					ATGPartitions.CHARACTER_COMMENTS_SEGMENT,
					
					ATGPartitions.STRING_IGNORE_SEGMENT,
					ATGPartitions.CHARACTER_IGNORE_SEGMENT,
					
					ATGPartitions.STRING_PRODUCTIONS_SEGMENT,
					ATGPartitions.CHARACTER_PRODUCTIONS_SEGMENT,
			};
			for (String ct : contentTypes) {
				reconciler.setDamager(stringDR, ct);
				reconciler.setRepairer(stringDR, ct);
			}
		}

		NonRuleBasedDamagerRepairer javaStringDR = new NonRuleBasedDamagerRepairer(
				fSyntaxManager.getTextAttribute(SyntaxManager.Java.STRING));

		{ // java strings
			String contentTypes[] = new String[] {
					ATGPartitions.STRING_IMPORTS,
					ATGPartitions.CHARACTER_IMPORTS,
					ATGPartitions.STRING_PARSER_CODE,
					ATGPartitions.CHARACTER_PARSER_CODE,
					ATGPartitions.STRING_INLINE_CODE,
					ATGPartitions.CHARACTER_INLINE_CODE,
					ATGPartitions.STRING_PRAGMAS_INLINE_CODE,
					ATGPartitions.CHARACTER_PRAGMAS_INLINE_CODE,
					ATGPartitions.STRING_PRODUCTIONS_INLINE_CODE,
					ATGPartitions.CHARACTER_PRODUCTIONS_INLINE_CODE,};
			for (String ct : contentTypes) {
				reconciler.setDamager(javaStringDR, ct);
				reconciler.setRepairer(javaStringDR, ct);
			}
		}

		
		return reconciler;
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if (ATGPartitions.INLINE_CODE.equals(contentType) || ATGPartitions.INLINE_CODE_END.equals(contentType)) {
			return new IAutoEditStrategy[] {
					new ATGAutoEditStrategies.InlineCodeIndentLineStrategy()
			};
		} else if (ATGPartitions.DEFAULT.equals(contentType)) {
			return new IAutoEditStrategy[] {
					new ATGAutoEditStrategies.PostInlineCodeIndentLineStrategy()
			};
		}

		// use DefaultIndentLineAutoEditStrategy for the rest
		return super.getAutoEditStrategies(sourceViewer, contentType);
	}


	/**
	 * Returns the outline presenter which will determine and show
	 * information requested for the current cursor position.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an information presenter
	 */
	public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer) {
		InformationPresenter presenter;
		presenter = new InformationPresenter(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new ATGOutlineInformationControl(parent, fEditor);
			}
		});
		presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);

		IInformationProvider provider = new ATGModelInformationProvider();
		for (String contentType : getConfiguredContentTypes(sourceViewer)) {
			presenter.setInformationProvider(provider, contentType);
		}
		presenter.setSizeConstraints(50, 30, true, false);
		return presenter;
	}

	private final class ATGModelInformationProvider implements IInformationProvider, IInformationProviderExtension {

		public ATGModelInformationProvider() {
		}

		public IRegion getSubject(ITextViewer textViewer, int offset) {
			if (textViewer != null)
				return new Region(offset, 0);
			return null;
		}

		public String getInformation(ITextViewer textViewer, IRegion subject) {
			return getInformation2(textViewer, subject).toString();
		}

		public Object getInformation2(ITextViewer textViewer, IRegion subject) {
			return fEditor.getATGModelProvider().getATGModel();
		}
	}
}
