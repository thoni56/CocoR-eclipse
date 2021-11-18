/*******************************************************************************
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
package at.ssw.coco.ide.model.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import at.ssw.coco.lib.model.scanners.ATGPartitioner;
import at.ssw.coco.lib.model.scanners.ATGPartitions;

/**
 * This class is an adaptor and is used to inlude and adapt the
 * library functions and methods into CocoEclipse.
 * (at.ssw.coco.lib.model.scanners.ATGPartitioner) 
 * 
 * Extend {@link FastPartitioner} to cover non-default open partitions.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class ATGPartitionerAdaptor extends FastPartitioner {
	/** The identifier of the partitioning. */
	public static final String PARTITIONING = ATGPartitions.PARTITIONING;

	/** The identifier of the default partition content type. */
	public static final String DEFAULT = ATGPartitions.DEFAULT;
	
	
	private static Map<String, String> openPartitionMap;
	
	
	public ATGPartitionerAdaptor(IPartitionTokenScanner scanner) {
		super(scanner, ATGPartitions.getLegalContentTypes());
	}

	@Override
	public ITypedRegion getPartition(int offset, boolean preferOpenPartitions) {
		ITypedRegion region = super.getPartition(offset);
		if (preferOpenPartitions) {
			if (region.getOffset() == offset
					&& !region.getType().equals(DEFAULT)){					
				if (offset > 0) {
					region = getPartition(offset - 1);
					if (   (region.getType().equals(DEFAULT)) || (region.getType().equals(null))  ) {
						return new TypedRegion(offset, 0, DEFAULT);
					} else {
						return new TypedRegion(offset, 0, getOpenPartition(region.getType()));
					}
				} else {
					return new TypedRegion(offset, 0, ATGPartitions.IMPORTS);
				}
			}
		}
        return region;
	}

	private String getOpenPartition(String type) {
		if (openPartitionMap == null) openPartitionMap = ATGPartitioner.getOpenPartitionMap();

		String defaultType = openPartitionMap.get(type);
		return (defaultType != null) ? defaultType : type;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * May be replaced or extended by subclasses.
	 * </p>
	 */
	@Override
	public ITypedRegion[] computePartitioning(int offset, int length, boolean includeZeroLengthPartitions) {
		checkInitialization();
		List list= new ArrayList();

		try {

			int endOffset= offset + length;

			Position[] category= getPositions();
			
//			for(Position p : category){
//				System.out.println(p.getOffset() + " " + p.getLength());
//			}
			

			TypedPosition previous= null, current= null;
			int start, end, gapOffset;
			Position gap= new Position(0);

			int startIndex= getFirstIndexEndingAfterOffset(category, offset);
			int endIndex= getFirstIndexStartingAfterOffset(category, endOffset);
			for (int i= startIndex; i < endIndex; i++) {

				current= (TypedPosition) category[i];

				gapOffset= (previous != null) ? previous.getOffset() + previous.getLength() : 0;
				gap.setOffset(gapOffset);
				
				//TODO AG this is just a quickfix, find error!!!
//				System.out.println(current.getOffset() + " " + gap.getOffset());
			
				int temp = current.getOffset() - gap.getOffset();
				
				if(temp>=0){
					gap.setLength(temp);
					if ((includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length)) ||
							(gap.getLength() > 0 && gap.overlapsWith(offset, length))) {
						start= Math.max(offset, gapOffset);
						end= Math.min(endOffset, gap.getOffset() + gap.getLength());
						list.add(new TypedRegion(start, end - start, IDocument.DEFAULT_CONTENT_TYPE));
					}

					if (current.overlapsWith(offset, length)) {
						start= Math.max(offset, current.getOffset());
						end= Math.min(endOffset, current.getOffset() + current.getLength());
						list.add(new TypedRegion(start, end - start, current.getType()));
					}
				}
				
//				gap.setLength(current.getOffset() - gapOffset);
//				if ((includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length)) ||
//						(gap.getLength() > 0 && gap.overlapsWith(offset, length))) {
//					start= Math.max(offset, gapOffset);
//					end= Math.min(endOffset, gap.getOffset() + gap.getLength());
//					list.add(new TypedRegion(start, end - start, IDocument.DEFAULT_CONTENT_TYPE));
//				}
//
//				if (current.overlapsWith(offset, length)) {
//					start= Math.max(offset, current.getOffset());
//					end= Math.min(endOffset, current.getOffset() + current.getLength());
//					list.add(new TypedRegion(start, end - start, current.getType()));
//				}

				previous= current;
			}

			if (previous != null) {
				gapOffset= previous.getOffset() + previous.getLength();
				gap.setOffset(gapOffset);
				gap.setLength(fDocument.getLength() - gapOffset);
				if ((includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length)) ||
						(gap.getLength() > 0 && gap.overlapsWith(offset, length))) {
					start= Math.max(offset, gapOffset);
					end= Math.min(endOffset, fDocument.getLength());
					list.add(new TypedRegion(start, end - start, IDocument.DEFAULT_CONTENT_TYPE));
				}
			}

			if (list.isEmpty())
				list.add(new TypedRegion(offset, length, IDocument.DEFAULT_CONTENT_TYPE));

		} catch (BadPositionCategoryException ex) {
			// Make sure we clear the cache
			clearPositionCache();
		} catch (RuntimeException ex) {
			// Make sure we clear the cache
			clearPositionCache();
			throw ex;
		}

		TypedRegion[] result= new TypedRegion[list.size()];
		list.toArray(result);
		return result;
	}
	
	/**
	 * Returns the index of the first position which ends after the given offset.
	 *
	 * @param positions the positions in linear order
	 * @param offset the offset
	 * @return the index of the first position which ends after the offset
	 */
	private int getFirstIndexEndingAfterOffset(Position[] positions, int offset) {
		int i= -1, j= positions.length;
		while (j - i > 1) {
			int k= (i + j) >> 1;
			Position p= positions[k];
			if (p.getOffset() + p.getLength() > offset)
				j= k;
			else
				i= k;
		}
		return j;
	}
	
	/**
	 * Returns the index of the first position which starts at or after the given offset.
	 *
	 * @param positions the positions in linear order
	 * @param offset the offset
	 * @return the index of the first position which starts after the offset
	 */
	private int getFirstIndexStartingAfterOffset(Position[] positions, int offset) {
		int i= -1, j= positions.length;
		while (j - i > 1) {
			int k= (i + j) >> 1;
			Position p= positions[k];
			if (p.getOffset() >= offset)
				j= k;
			else
				i= k;
		}
		return j;
	}
	
	/**
	 * Returns <code>true</code> if the given ranges overlap with or touch each other.
	 *
	 * @param gap the first range
	 * @param offset the offset of the second range
	 * @param length the length of the second range
	 * @return <code>true</code> if the given ranges overlap with or touch each other
	 */
	private boolean overlapsOrTouches(Position gap, int offset, int length) {
		return gap.getOffset() <= offset + length && offset <= gap.getOffset() + gap.getLength();
	}
}
