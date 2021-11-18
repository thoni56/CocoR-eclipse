/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
 * Copyright (C) 2011 Andreas Greilinger
 * Copyright (C) 2011 Konstantin Bina
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

package at.ssw.coco.ide.model.detectors;

import org.eclipse.jface.text.IDocument;

import at.ssw.coco.ide.model.detectors.word.CocoIdentDetectorAdaptor;
import at.ssw.coco.lib.model.detectors.WordFinder;
import at.ssw.coco.lib.model.detectors.WordFinderImpl;
import at.ssw.coco.lib.model.positions.ICocoRegion;

/**
 * This class is an adaptor and is used to inlude and adapt the
 * library functions and methods into CocoEclipse.
 * (at.ssw.coco.lib.model.detectors.WordFinderImpl;) 
 * 
 * Implements a general purpose  finder. The  type is determined by the passed <code>IWordDetector</code>.
 * 
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 *
 */
public class WordFinderAdaptor implements WordFinder {	

	private  WordFinderImpl wordFinder;
	
	/**
	 * The Constructor.
	 *
	 * @param document
	 *            the document.
	 * @param wordDetector
	 *            the word detector.
	 */
	public WordFinderAdaptor(IDocument document, CocoIdentDetectorAdaptor wordDetector) {
		wordFinder =  new WordFinderImpl(document.get(), wordDetector.getDetector());
	}

	/**
	 * Locates the region of the cursor prefix on the basis of the given word detector.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the region of the prefix.
	 */
	public ICocoRegion locatePrefix(int offset) {
		return wordFinder.locatePrefix(offset);
	}

	/**
	 * Returns the cursor prefix on the basis of the given word detector.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the prefix.
	 */
	public String getPrefix(int offset) {
		return wordFinder.getPrefix(offset);
	}

	/**
	 * Locates the region of the cursor postfix on the basis of the given word detector.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the region of the postfix.
	 */
	public ICocoRegion locateSuffix(int offset) {
		return wordFinder.locateSuffix(offset);
	}

	/**
	 * Returns the cursor prefix on the basis of the given word detector.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the prefix.
	 */
	public String getSuffix(int offset) {
		return wordFinder.getSuffix(offset);
	}

	/**
	 * Locates the word the cursor is located on.
	 *
	 * @param offset
	 *            the cursor offset.
	 * @return the region of this word.
	 */
	public ICocoRegion findWord(int offset) { // TODO: use locate Pre-/Suffix
		return wordFinder.findWord(offset);
	}

	/**
	 * Extracts the string determined by the given region.
	 *
	 * @param region
	 *            the region.
	 * @return the word as <code>String</code>
	 */
	public String extractWord(ICocoRegion region) {
		return wordFinder.extractWord(region);
	}
}
