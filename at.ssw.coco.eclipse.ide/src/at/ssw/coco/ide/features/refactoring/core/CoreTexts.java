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

package at.ssw.coco.ide.features.refactoring.core;

import org.eclipse.osgi.util.NLS;

/**
 * implements NLS messages for refactoring Core Messages
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * 
 */
public class CoreTexts extends NLS {
 
	private static final String BUNDLE_NAME 
		= "at.ssw.coco.ide.features.refactoring.core.coretexts"; //$NON-NLS-1$
  
	static {
		NLS.initializeMessages( BUNDLE_NAME, CoreTexts.class );
	}
	
	/**
	 * message fields
	 */
	public static String renameRefactor_noSourceFile;
	public static String renameRefactor_roFile;
	public static String renameRefactor_collectingChanges;
	public static String renameRefactor_checking;
	public static String renameRefactor_noAtgFile;
	public static String renameRefactor_buildFailed;
	public static String renameRefactor_errorsInFile;
	
	public static String renameCharacterSetRefactor_name;
	public static String renameCharacterSetRefactor_noCharacter;

	public static String renameTokenRefactor_name;
	public static String renameTokenRefactor_noToken;
	
	public static String renamePragmaRefactor_name;
	public static String renamePragmaRefactor_noPragma;

	public static String renameProductionRefactor_name;
	public static String renameProductionRefactor_noProduction;	

	public static String reformatProductionRefactor_name;
	public static String reformatProductionRefactor_collectingChanges;
}