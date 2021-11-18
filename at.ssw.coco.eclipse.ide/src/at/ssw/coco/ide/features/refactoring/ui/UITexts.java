/*******************************************************************************
 * Copyright (C) 2011 Martin Preinfalk
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

package at.ssw.coco.ide.features.refactoring.ui;

import org.eclipse.osgi.util.NLS;

/**
 * implements NLS messages for refactoring UI Messages
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class UITexts {

	private static final String BUNDLE_NAME 
    	= "at.ssw.coco.ide.features.refactoring.ui.uitexts"; //$NON-NLS-1$

	static {
		NLS.initializeMessages( BUNDLE_NAME, UITexts.class );
	}

	/**
	 * message fields
	 */
	public static String renameCharacter_refuseDlg_title;
	public static String renameCharacter_refuseDlg_message;
	public static String renameCharacter_InputPage_lblNewName;

	public static String reformatProduction_refuseDlg_title;
  	public static String reformatProduction_refuseDlg_message;
	public static String reformatProduction_ReformatAllProductions;
	public static String reformatProduction_UseFixedOffset; 
}
