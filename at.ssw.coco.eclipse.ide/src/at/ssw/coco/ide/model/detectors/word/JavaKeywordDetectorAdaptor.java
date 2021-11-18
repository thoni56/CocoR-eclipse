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

package at.ssw.coco.ide.model.detectors.word;

import org.eclipse.jface.text.rules.IWordDetector;

import at.ssw.coco.lib.model.detectors.word.JavaKeywordDetector;

/**
 * This class is an adaptor and is used to inlude and adapt the
 * library functions and methods into CocoEclipse.
 * (at.ssw.coco.lib.model.detectors.word.JavaKeywordDetector)
 * 
 * Implements a <code>IWordDetector</code> to detect java keywords.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 *
 */
public class JavaKeywordDetectorAdaptor implements IWordDetector{
	
	private JavaKeywordDetector detector = new JavaKeywordDetector();
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
	 */
	public boolean isWordPart(char c) {
		return detector.isWordStart(c);
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
	 */
	public boolean isWordStart(char c) {
		return detector.isWordStart(c);
	}
}
