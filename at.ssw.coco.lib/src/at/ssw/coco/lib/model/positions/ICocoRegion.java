package at.ssw.coco.lib.model.positions;

/**
 * Represents a Region in Coco
 *
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */

public interface ICocoRegion {

	/**
	 * Returns the length of the region.
	 *
	 * @return the length of the region
	 */
	int getLength();

	/**
	 * Returns the offset of the region.
	 *
	 * @return the offset of the region
	 */
	int getOffset();

}
