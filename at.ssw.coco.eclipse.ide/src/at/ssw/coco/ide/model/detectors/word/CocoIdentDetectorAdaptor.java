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
package at.ssw.coco.ide.model.detectors.word;

import org.eclipse.jface.text.rules.IWordDetector;

import at.ssw.coco.lib.model.detectors.word.CocoIdentDetector;

/**
 * This class is an adaptor and is used to inlude and adapt the
 * library functions and methods into CocoEclipse.
 * (at.ssw.coco.lib.model.detectors.word.CocoIdentDetector)
 * 
 * Implements an <code>IWordDetector</code> that detects idents in the same way as Coco/R.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class CocoIdentDetectorAdaptor implements IWordDetector {

	private CocoIdentDetector detector = new CocoIdentDetector();
	
	public CocoIdentDetector getDetector(){
		return detector;
	}
	
	public boolean isWordPart(char c) {
		return detector.isWordPart(c);
	}

	public boolean isWordStart(char c) {
		return detector.isWordStart(c);
	}

}
