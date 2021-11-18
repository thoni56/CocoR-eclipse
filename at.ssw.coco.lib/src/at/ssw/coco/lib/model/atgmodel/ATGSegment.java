/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
 * Copyright (C) 2009 Andreas Woess
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *******************************************************************************/

package at.ssw.coco.lib.model.atgmodel;

import java.util.ArrayList;
import java.util.List;

import at.ssw.coco.lib.model.positions.CocoPosition;
import at.ssw.coco.lib.model.positions.CocoRegion;
import at.ssw.coco.lib.model.positions.ICocoRegion;



/**
 * Implements a single ATG Segment. (Part of ATG model)
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class ATGSegment {

	/**
	 * The region where this segment is located in.
	 * Region objects are light-weight, so we don't need to modify them.
	 */
	private ICocoRegion fRegion;

	/** The level within the hierarchy of the ATG model */
	private int fLevel;

	/** The name */
	private final String fName;

	/** The segments parent segment */
	private ATGSegment fParent;

	/** The segments children segments */
	private final List<ATGSegment> fChildren;

	/** Holds Coco/R attributes. Optional. */
	private String fAttributes = "";

	/** The semantic type of this segment. */
	private final Type fType;

	public enum Type {
		SECTION_IMPORTS("Imports"),
		SECTION_COMPILER,
		SECTION_CODE("Code"),
		SECTION_SCANNER("Scanner"), // Scanner
		SECTION_PRODUCTIONS("Productions"), // Parser
		GROUP_IGNORECASE,
		GROUP_CHARACTERS,
		GROUP_TOKENS,
		GROUP_PRAGMAS,
		GROUP_COMMENTS,
		GROUP_IGNORE,
		ITEM_CHARSET,
		ITEM_TOKEN,
		ITEM_PRAGMA,
		ITEM_COMMENT,
		ITEM_IGNORE,
		ITEM_PRODUCTION,
		ERROR,
		UNKNOWN;

		private final String name;

		private Type() {
			name = super.toString().replaceFirst(".*_", "");
		}
		private Type(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static final Type ITEM_TYPES[] = new Type[] {
		Type.ITEM_CHARSET,  Type.ITEM_TOKEN, Type.ITEM_PRAGMA, Type.ITEM_PRODUCTION };

	/**
	 * The Constructor.
	 *
	 * @param type the type of the segment.
	 * @param name the segments name
	 */
	public ATGSegment(Type type, String name) {
		this(type, name, -1, -1);
	}

	/**
	 * The Constructor.
	 *
	 * @param type the type of the segment.
	 * @param name the segments name.
	 * @param startPos the start position.
	 */
	public ATGSegment(Type type, String name, int startPos) {
		this(type, name, startPos, startPos);
	}

	/**
	 * The Constructor.
	 *
	 * @param type the type of the segment.
	 * @param name the segments name.
	 * @param startPos the start position.
	 * @param endPos the end position.
	 */
	public ATGSegment(Type type, String name, int startPos, int endPos) {
		fType = type;
		fName = name;
		fRegion = new CocoRegion(startPos, endPos - startPos);
		fLevel = 0;
		fParent = null;
		fChildren = new ArrayList<ATGSegment>();
	}

	public ATGSegment(Type type) {
		this(type, type.toString());
	}

	public ATGSegment(Type type, int beg) {
		this(type, type.toString(), beg);
	}

	public ATGSegment(Type type, int beg, int end) {
		this(type, type.toString(), beg, end);
	}

	/**
	 * Indicates if the segment has children.
	 *
	 * @return a boolean value which indicates if the segment has children.
	 */
	public boolean hasChildren() {
		return !fChildren.isEmpty();
	}

	/**
	 * Adds a further <code>ATGSegment</code> as a child.
	 *
	 * @param child the ATG segment.
	 */
	public void addChild(ATGSegment child) {
		child.setParent(this);
		child.setLevel(fLevel + 1);
		updateChildLevel(child);
		fChildren.add(child);
		//fixupRegion(); // fixing up now might not work yet, thus don't even try
	}

	/**
	 * Recursivley updates the level of the child nodes.
	 */
	private static void updateChildLevel(ATGSegment seg) {
		for (ATGSegment child : seg.getChildren()) {
			child.setLevel(seg.getLevel() + 1);
			updateChildLevel(child);
		}
	}

	/**
	 * Sets the end of the <code>ATGSegment</code> region.
	 *
	 * Now that we get the end point, we should be able to fix the region up!
	 *
	 * @param end The end point.
	 */
	public void setEndPoint(int end) {
	    fixupRegion();
		if (0 < fRegion.getOffset()) {
			fRegion = new CocoRegion(fRegion.getOffset(), end - fRegion.getOffset());
		} // else: if we don't know the start, it's useless to know the end.
	}

	/**
	 * Sets the segment's level.
	 *
	 * @param level the level.
	 */
	public void setLevel(int level) {
		fLevel = level;
	}

	/**
	 * Sets the segment's parent.
	 *
	 * @param parent the parent.
	 */
	public void setParent(ATGSegment parent) {
		fParent = parent;
	}

	/**
	 * Returns a valid <code>Position</code>. This includes the segment's children, too.
	 *
	 * @return the position.
	 */
	public CocoPosition getPosition() {
		fixupRegion();
		int offset = fRegion.getOffset();
		int length = fRegion.getLength();
		return new CocoPosition(offset >= 0 ? offset : 0, length >= 0 ? length : 0);
	}

	/**
	 * Try to fix up the region by looking at the children.
	 * Make sure we get the full region spanned by them.
	 * Children must be in correct order.
	 */
	private void fixupRegion() {
		if (fRegion.getOffset() < 0 && !fChildren.isEmpty()) {
			ATGSegment first = fChildren.get(0), last = fChildren.get(fChildren.size() - 1);
			first.fixupRegion(); last.fixupRegion();

			int offset = first.fRegion.getOffset();
			if (0 < offset) {
				// just in case our end point won't get set, try to make a good estimate
				int length = 0;
				if (0 < last.fRegion.getOffset()) {
					length = (last.fRegion.getOffset() - offset) + last.fRegion.getLength();
				}
				fRegion = new CocoRegion(offset, length);
			} else System.out.println("fix-up failed: " + fRegion);
		}
	}

	/**
	 * Returns the region. The region offset might be -1.
	 *
	 * @return the region
	 */
	public ICocoRegion getRegion() {
		fixupRegion();
		return fRegion;
	}

	/**
	 * Returns the segment's level.
	 *
	 * @return the level.
	 */
	public int getLevel() {
		return fLevel;
	}

	/**
	 * Returns the segment's name.
	 *
	 * @return the name.
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Returns the segment's parent.
	 *
	 * @return the parent.
	 */
	public ATGSegment getParent() {
		return fParent;
	}

	/**
	 * Returns the segment's children.
	 *
	 * @return the children.
	 */
	public ATGSegment[] getChildren() {
		return fChildren.toArray(new ATGSegment[0]);
	}

	@Override
	public String toString() {
		return fName;
	}

	public void setAttributes(String attributes) {
		this.fAttributes = attributes;
	}

	public String getAttributes() {
		return fAttributes;
	}

	public Type getType() {
		return fType;
	}
}
