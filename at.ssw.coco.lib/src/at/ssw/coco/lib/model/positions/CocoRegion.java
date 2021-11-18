package at.ssw.coco.lib.model.positions;

/**
 * Represents a Region in Coco
 *
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */

public class CocoRegion implements ICocoRegion {

	/** The region offset */
	private int fOffset;
	/** The region length */
	private int fLength;

	/**
	 * Create a new region.
	 *
	 * @param offset the offset of the region
	 * @param length the length of the region
	 */
	public CocoRegion(int offset, int length) {
		fOffset= offset;
		fLength= length;
	}

	/*
	 * @see org.eclipse.jface.text.IRegion#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/*
	 * @see org.eclipse.jface.text.IRegion#getOffset()
	 */
	public int getOffset() {
		return fOffset;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
	 	if (o instanceof ICocoRegion) {
	 		ICocoRegion r= (ICocoRegion) o;
	 		return r.getOffset() == fOffset && r.getLength() == fLength;
	 	}
	 	return false;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
	 	return (fOffset << 24) | (fLength << 16);
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "offset: " + fOffset + ", length: " + fLength; //$NON-NLS-1$ //$NON-NLS-2$;
	}
}
