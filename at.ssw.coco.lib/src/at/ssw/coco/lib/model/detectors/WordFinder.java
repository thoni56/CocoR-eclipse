package at.ssw.coco.lib.model.detectors;

import at.ssw.coco.lib.model.positions.ICocoRegion;

/**
 * A general purpose word finder.
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 *
 */

public interface WordFinder {

	/**
	 * Locates the region of the cursor prefix on the basis of the given word detector.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the region of the prefix.
	 */
	public ICocoRegion locatePrefix(int offset);

	/**
	 * Returns the cursor prefix on the basis of the given word detector.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the prefix.
	 */
	public String getPrefix(int offset);

	/**
	 * Locates the region of the cursor postfix on the basis of the given word detector.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the region of the postfix.
	 */
	public ICocoRegion locateSuffix(int offset);

	/**
	 * Returns the cursor prefix on the basis of the given word detector.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the prefix.
	 */
	public String getSuffix(int offset);

	/**
	 * Locates the word the cursor is located on.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the region of this word.
	 */
	public ICocoRegion findWord(int offset);

	/**
	 * Extracts the string determined by the given region.
	 *
	 * @param region
	 *            the region.
	 * @return the word as <code>String</code>
	 */
	public String extractWord(ICocoRegion region);

}