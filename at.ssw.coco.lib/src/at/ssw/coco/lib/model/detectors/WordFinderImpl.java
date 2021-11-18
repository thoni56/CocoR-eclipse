/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
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

package at.ssw.coco.lib.model.detectors;

import at.ssw.coco.lib.model.detectors.word.CocoIdentDetector;
import at.ssw.coco.lib.model.positions.CocoRegion;
import at.ssw.coco.lib.model.positions.ICocoRegion;

/**
 * Implements a general purpose word finder. The word type is determined by the passed <code>IWordDetector</code>.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class WordFinderImpl implements WordFinder {

	/** The content of the Document */
	private String content;
	
	/** The word detector */
	private CocoIdentDetector fWordDetector;

	/**
	 * The Constructor.
	 *
	 * @param document
	 *            the document.
	 * @param wordDetector
	 *            the word detector.
	 */
	public WordFinderImpl(String document, CocoIdentDetector wordDetector) {
		content = document;
		fWordDetector = wordDetector;
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.detectors.WordFinder#locatePrefix(int)
	 */
	public ICocoRegion locatePrefix(int offset) {
		int start = -2;
		
		int pos = offset - 1;
		char c = ' ';
		
		while (pos >= 0) {
			c = content.charAt(pos);
			if (!fWordDetector.isWordPart(c)) {
				break;
			}
			--pos;
		}
		if (!fWordDetector.isWordStart(c) || pos < 0) {
			pos++;
		}
		if (pos < offset) {
			start = pos;
		}
		return new CocoRegion(start, offset - start);
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.detectors.WordFinder#getPrefix(int)
	 */
	public String getPrefix(int offset) {
		ICocoRegion prefix = locatePrefix(offset);
		String result = "";
		try  {
			result = content.substring(prefix.getOffset(), prefix.getOffset()+prefix.getLength());
		} catch (StringIndexOutOfBoundsException e) {
			//do nothing, because no prefix found
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.detectors.WordFinder#locateSuffix(int)
	 */
	public ICocoRegion locateSuffix(int offset) {
		int end = -1;

		
		int pos = offset;
		char c;
		int length = content.length();

		while (pos < length) {
			c = content.charAt(pos);
			if (!fWordDetector.isWordPart(c)) {
				break;
			}
			++pos;
		}
		end = pos;
		return new CocoRegion(offset, end - offset);
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.detectors.WordFinder#getSuffix(int)
	 */
	public String getSuffix(int offset) {
		ICocoRegion suffix = locateSuffix(offset);
		String result = content.substring(suffix.getOffset(), suffix.getOffset() + suffix.getLength());		
		return result;
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.detectors.WordFinder#findWord(int)
	 */
	public ICocoRegion findWord(int offset) { // TODO: use locate Pre-/Suffix
		int start = -2;
		int end = -1;

		
		int pos = offset;
		char c;

		while (pos >= 0) {
			c = content.charAt(pos);
			if (!fWordDetector.isWordPart(c)) {
				break;
			}
			--pos;
		}
		if (pos < offset) {
			start = pos;
		}

		pos = offset;
		int length = content.length();

		while (pos < length) {
			c = content.charAt(pos);
			if (!fWordDetector.isWordPart(c)) {
				break;
			}
			++pos;
		}
		end = pos;


		if (start >= -1 && end > -1) {
			if (start == offset && end == offset) {
				return new CocoRegion(offset, 0);
			} else if (start == offset) {
				return new CocoRegion(start, end - start);
			} else {
				return new CocoRegion(start + 1, end - start - 1);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.detectors.WordFinder#extractWord(at.ssw.coco.lib.editor.positions.ICocoRegion)
	 */
	public String extractWord(ICocoRegion region) {
		String result = null;
		
		if (region != null) {
			result = content.substring(region.getOffset(), region.getOffset() + region.getLength());
		}
		return result;
	}
}
