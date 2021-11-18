package at.ssw.coco.lib.features.contentAssist.codeCompletion.staticCompletion;

import at.ssw.coco.lib.features.contentAssist.codeCompletion.ProposalProvider;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;

/**
 * Extends a <code>ProposalProvider</code> to implement static Code Completion for 
 * the CharactersSegment Partition of the <code>ATGEditor</code>
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class CharactersSegmentProposalProvider extends ProposalProvider{
	
	public CharactersSegmentProposalProvider() {
		KEYWORDS = new String[]{ "TOKENS", "PRAGMAS", "COMMENTS", "IGNORE", "PRODUCTIONS", "ANY"};
		segments = new ATGSegment.Type[] {ATGSegment.Type.ITEM_CHARSET};
	}
	

}
