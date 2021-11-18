package at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion;

import at.ssw.coco.lib.features.contentAssist.codeCompletion.ProposalProvider;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;

/**
 * Extends a <code>ProposalProvider</code> to implement static Code Completion for 
 * the PragmasSegment Partition of the <code>ATGEditor</code>
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class PragmasSegmentProposalProvider extends ProposalProvider{
	
	public PragmasSegmentProposalProvider() {
		KEYWORDS = new String[]{"COMMENTS", "IGNORE", "PRODUCTIONS"};
		segments = new ATGSegment.Type[] {ATGSegment.Type.ITEM_CHARSET};
	}
}
