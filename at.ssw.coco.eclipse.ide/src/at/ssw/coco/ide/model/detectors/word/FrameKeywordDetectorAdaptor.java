/*******************************************************************************
 * Copyright (C) 2009 Institute for System Software, JKU Linz
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
package at.ssw.coco.ide.model.detectors.word;

import org.eclipse.jface.text.rules.IWordDetector;

import at.ssw.coco.lib.model.detectors.word.FrameKeywordDetector;

/**
 * This class is an adaptor and is used to inlude and adapt the
 * library functions and methods into CocoEclipse.
 * (at.ssw.coco.lib.model.detectors.word.FrameKeywordDetector)
 * 
 * Implements a <code>IWordDetector</code> to detect frame keywords.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class FrameKeywordDetectorAdaptor implements IWordDetector {

	private FrameKeywordDetector detector = new FrameKeywordDetector();
	
	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.detectors.FrameKeywordDetector#isWordPart(char)
	 */
	public boolean isWordPart(char c) {
		return detector.isWordPart(c);
	}

	/* (non-Javadoc)
	 * @see at.ssw.coco.lib.editor.detectors.FrameKeywordDetector#isWordStart(char)
	 */
	public boolean isWordStart(char c) {
		return detector.isWordStart(c);
	}
	
}
