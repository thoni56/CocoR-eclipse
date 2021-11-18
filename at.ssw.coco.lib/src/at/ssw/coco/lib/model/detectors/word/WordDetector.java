package at.ssw.coco.lib.model.detectors.word;

/**
 * Defines Methods to detect java keywords, idents, etc.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 *
 */

public interface WordDetector {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
	 */
	public boolean isWordPart(char c);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
	 */
	public boolean isWordStart(char c);

}