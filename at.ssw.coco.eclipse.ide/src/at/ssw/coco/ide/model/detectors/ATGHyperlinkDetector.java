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

package at.ssw.coco.ide.model.detectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;

import at.ssw.coco.ide.model.detectors.word.CocoIdentDetectorAdaptor;
import at.ssw.coco.lib.model.atgmodel.ATGModel;
import at.ssw.coco.lib.model.atgmodel.ATGModelProvider;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;
import at.ssw.coco.lib.model.positions.CocoRegion;
import at.ssw.coco.lib.model.positions.ICocoRegion;
import at.ssw.coco.lib.model.scanners.ATGPartitions;

/**
 * Implements a hyperlink detector which tries to find a hyperlink at a given
 * location in a given text viewer.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ATGHyperlinkDetector implements IHyperlinkDetector {
	/** The content types for which to detect. */
	private static final String[] ALLOWED_PARTITIONS = new String[] {
		ATGPartitions.DEFAULT, ATGPartitions.COMPILER_IDENT };

	/** The ATG model provider */
	private ATGModelProvider modelProvider;

	/** The word detector */
	private static final CocoIdentDetectorAdaptor fIdentDetector = new CocoIdentDetectorAdaptor();

	/**
	 * The Constructor
	 *
	 * @param editor The ATG editor
	 */
	public ATGHyperlinkDetector(ATGModelProvider modelProvider) {
		Assert.isNotNull(modelProvider);
		this.modelProvider = modelProvider;
	}

	
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		return detectHyperlinks(textViewer, new CocoRegion(region.getOffset(), region.getLength()), canShowMultipleHyperlinks);
	}
	
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, ICocoRegion region, boolean canShowMultipleHyperlinks) {
		ATGModel fATGModel = modelProvider.getATGModel();
		if (region == null || fATGModel == null || !(textViewer instanceof ISourceViewer)) {
			return null;
		}

		int offset = region.getOffset();
		try { // check the partition type
			String contentType = textViewer.getDocument().getPartition(offset).getType();
			if (!contains(ALLOWED_PARTITIONS, contentType)) return null;
		} catch (BadLocationException e) {
			// should not happen
		}

		WordFinderAdaptor wordFinder = new WordFinderAdaptor(textViewer.getDocument(), fIdentDetector);
		ICocoRegion cocoWordRegion = wordFinder.findWord(offset);
		String word = wordFinder.extractWord(cocoWordRegion);
		if (word == null) {
			return null;
		}

		ATGSegment segment = fATGModel.find(word);
		Region wordRegion = new Region(cocoWordRegion.getOffset(), cocoWordRegion.getLength());
		if (segment != null) {
			return new IHyperlink[] { new ATGHyperlink(wordRegion, new Region(segment.getRegion().getOffset(), segment.getRegion().getLength()))};
		}

		return null;
	}

	private static <T> boolean contains(T[] array, T wanted) {
		for (T candidate : array) {
			if (candidate.equals(wanted)) return true;
		}
		return false;
	}
}
