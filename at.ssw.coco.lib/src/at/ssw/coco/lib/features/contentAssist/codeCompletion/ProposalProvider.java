package at.ssw.coco.lib.features.contentAssist.codeCompletion;

import at.ssw.coco.lib.model.atgmodel.ATGSegment;

/**
 * A generic Code Completion Proposal Provider used to implement Code Completion for 
 * the <code>ATGEditor</code> of CocoEclipse
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */

public abstract class ProposalProvider {
	/**
	 * @return the kEYWORDS
	 */
	public String[] getKEYWORDS() {
		return KEYWORDS;
	}
	/**
	 * @return the segments
	 */
	public ATGSegment.Type[] getSegments() {
		return segments;
	}
	protected String[] KEYWORDS;
	protected ATGSegment.Type[] segments;
}
