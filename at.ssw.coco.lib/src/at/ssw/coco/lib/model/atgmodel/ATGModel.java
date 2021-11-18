package at.ssw.coco.lib.model.atgmodel;

/**
 * Represents the content of an ATG file.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */

public interface ATGModel {

	/**
	 * Returns the root segment of the ATG structure.
	 *
	 * @return the root segment.
	 */
	public ATGSegment[] getElements();

	/**
	 * Returns the productions segments
	 *
	 * @return the production segments
	 */
	public ATGSegment[] getProductions();

	/**
	 * Returns the declaration segment
	 *
	 * @return the declaration segment
	 */
	public ATGSegment getDeclarations();

	/**
	 * Returns the imports segment
	 *
	 * @return the imports segment
	 */
	public ATGSegment getImports();

	/**
	 * Looks for the ATG segment with the given name.
	 *
	 * @param name The name.
	 * @param acceptedTypes Which types to include in the search (all if <code>null</code>).
	 * @return the ATG segment.
	 */
	public ATGSegment find(String name, ATGSegment.Type[] acceptedTypes);

	public ATGSegment find(String name);

	/**
	 * Lists the content of the ATG model
	 *
	 * @return the names of the ATG segment.
	 */
	public String[] listContent(ATGSegment.Type[] acceptedTypes);

	public String[] listContent();

	/**
	 * Lists the segments of the ATG model.
	 *
	 * @return the the ATG segments.
	 */
	public ATGSegment[] listSegments(ATGSegment.Type[] acceptedTypes);

	public ATGSegment[] listSegments();

}