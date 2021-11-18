package at.ssw.coco.lib.model.atgmodel;

import java.util.ArrayList;
import java.util.List;

import at.ssw.coco.lib.model.atgmodel.impl.DocumentScanner;
import at.ssw.coco.lib.model.atgmodel.impl.Parser;

/**
 * Represents the content of an ATG file.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */

public class ATGModelImpl implements ATGModel {
	/** The first segment of the ATG structure */
	private ATGSegment fRoot;

	/**
	 * The Constructor.
	 *
	 * @param document The document.
	 */
	public ATGModelImpl (DocumentScanner scanner){
		initATGModel(scanner);
	}
	
	
	private void initATGModel(DocumentScanner scanner) {
		try {
			Parser parser = new Parser(scanner);
			parser.Parse();
			fRoot = parser.getOutline();
		} catch (Exception e) {
			fRoot = new ATGSegment(ATGSegment.Type.ERROR, "An error occured parsing this file!");
			System.err.println(e.getMessage());
		}
	}
	
	/*(non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#getElements()
	 */
	public ATGSegment[] getElements() {
		return new ATGSegment[] { fRoot };
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#getProductions()
	 */
	public ATGSegment[] getProductions() {
		for (ATGSegment seg : fRoot.getChildren()) {
			if (seg.getType().equals(ATGSegment.Type.SECTION_PRODUCTIONS)) {
				return seg.getChildren();
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#getDeclarations()
	 */
	public ATGSegment getDeclarations() {
		for (ATGSegment seg : fRoot.getChildren()) {
			if (seg.getType().equals(ATGSegment.Type.SECTION_CODE)) {
				return seg;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#getImports()
	 */
	public ATGSegment getImports() {
		for (ATGSegment seg : fRoot.getChildren()) {
			if (seg.getType().equals(ATGSegment.Type.SECTION_IMPORTS)) {
				return seg;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#find(java.lang.String, at.ssw.coco.lib.editor.model.ATGSegment.Type[])
	 */
	public ATGSegment find(String name, ATGSegment.Type[] acceptedTypes) {
		return find(fRoot, name, acceptedTypes);
	}
	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#find(java.lang.String)
	 */
	public ATGSegment find(String name) {
		return find(name, ATGSegment.ITEM_TYPES);
	}

	/**
	 * The recursive procedure to look through the ATG model.
	 *
	 * @param segment The current ATG segment.
	 * @param name The name.
	 * @param acceptedTypes Which types to include in the search (all if <code>null</code>).
	 * @return the ATG segment.
	 */
	private ATGSegment find(ATGSegment segment, String name, ATGSegment.Type[] acceptedTypes) {
		if ((acceptedTypes == null || contains(acceptedTypes, segment.getType()))
				&& name.equals(segment.getName())) {
			return segment;
		}
		for (ATGSegment seg : segment.getChildren()) {
			ATGSegment result = find(seg, name, acceptedTypes);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private static <T> boolean contains(T[] array, T wanted) {
		for (T candidate : array) {
			if (candidate.equals(wanted)) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#listContent(at.ssw.coco.lib.editor.model.ATGSegment.Type[])
	 */
	public String[] listContent(ATGSegment.Type[] acceptedTypes) {
		List<String> content = new ArrayList<String>();
		listContent(fRoot, content, acceptedTypes);
		return content.toArray(new String[0]);
	}
	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#listContent()
	 */
	public String[] listContent() {
		return listContent(ATGSegment.ITEM_TYPES);
	}

	/**
	 * The recursive procedure to list the content of the ATG model.
	 *
	 * @param segment The current ATG segment.
	 * @param content The <code>ArrayList</code> which holds the content.
	 * @param acceptedTypes Which types to include (all if <code>null</code>).
	 */
	private void listContent(ATGSegment segment, List<String> content, ATGSegment.Type[] acceptedTypes) {
		if (segment == null) {
			return;
		}
		if (acceptedTypes == null || contains(acceptedTypes, segment.getType())) {
			content.add(segment.getName());
		}
		for (ATGSegment seg : segment.getChildren()) {
			listContent(seg, content, acceptedTypes);
		}
	}

	/**
	 * The recursive procedure to list the content of the ATG model.
	 *
	 * @param segment The current ATG segment.
	 * @param list The <code>List</code> which holds the segments.
	 * @param acceptedTypes Which segment types to include (all if <code>null</code>).
	 */
	private void listSegments(ATGSegment segment, List<ATGSegment> list, ATGSegment.Type[] acceptedTypes) {
		if (segment == null) {
			return;
		}
		if (acceptedTypes == null || contains(acceptedTypes, segment.getType())) {
			list.add(segment);
		}
		for (ATGSegment seg : segment.getChildren()) {
			listSegments(seg, list, acceptedTypes);
		}
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#listSegments(at.ssw.coco.lib.editor.model.ATGSegment.Type[])
	 */
	public ATGSegment[] listSegments(ATGSegment.Type[] acceptedTypes) {
		List<ATGSegment> segments = new ArrayList<ATGSegment>();
		listSegments(fRoot, segments, acceptedTypes);
		return segments.toArray(new ATGSegment[segments.size()]);
	}
	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.model.ATGModel#listSegments()
	 */
	public ATGSegment[] listSegments() {
		return listSegments(ATGSegment.ITEM_TYPES);
	}
}
