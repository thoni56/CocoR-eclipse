package at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion;

import at.ssw.coco.lib.features.contentAssist.codeCompletion.ProposalProvider;

/**
 * Extends a <code>ProposalProvider</code> to implement static Code Completion for 
 * the IgnoreCaseSegment Partition of the <code>ATGEditor</code>
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class IgnoreCaseSegmentProposalProvider extends ProposalProvider{
	
	public IgnoreCaseSegmentProposalProvider() {
		KEYWORDS = new String[]{"CHARACTERS", "TOKENS", "PRAGMAS", "COMMENTS", "IGNORE", "PRODUCTIONS"};
		segments = null;
	}


}
