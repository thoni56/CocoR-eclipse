package at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion;

import at.ssw.coco.lib.features.contentAssist.codeCompletion.ProposalProvider;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;

/**
 * Extends a <code>ProposalProvider</code> to implement static Code Completion for 
 * the TokensSegment Partition of the <code>ATGEditor</code>
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class TokensSegmentProposalProvider extends ProposalProvider{
	
	public TokensSegmentProposalProvider() {
		KEYWORDS = new String[]{"PRAGMAS", "COMMENTS", "IGNORE", "PRODUCTIONS", "CONTEXT"};
		segments = new ATGSegment.Type[] {ATGSegment.Type.ITEM_CHARSET};
	}
}
