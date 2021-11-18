package at.ssw.coco.ide.features.contentAssist.codeCompletion.staticCompletion;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import at.ssw.coco.ide.features.contentAssist.ATGContentAssistProcessor;
import at.ssw.coco.lib.features.contentAssist.codeCompletion.ProposalProvider;
import at.ssw.coco.lib.model.atgmodel.ATGModelProvider;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;

/**
 * Extends a <code>ATGContentAssistProcessor</code> to implement static Code Completion for 
 * the CharactersSegment Partition of the <code>ATGEditor</code>
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class ATGCompletionProcessor extends ATGContentAssistProcessor {
	
	private String[] KEYWORDS;
	private ATGSegment.Type[] segments;
	
	public ATGCompletionProcessor(ATGModelProvider modelProvider, ProposalProvider provider) {
		super(modelProvider);
		KEYWORDS = provider.getKEYWORDS();
		segments = provider.getSegments();
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		Object[][] index;
		if (segments == null){
			index = null;
		}
		else{
			index = new Object[][] {modelProvider.getATGModel().listSegments(segments)};
		}
		return computeCompletionProposals(viewer, offset, KEYWORDS, index);
	}
}
