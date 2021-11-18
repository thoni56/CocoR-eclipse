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

package at.ssw.coco.ide.features.refactoring.ui.wizards;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import at.ssw.coco.ide.features.refactoring.core.ReformatInfo;

/**
 * implements reformat ATG productions wizard 
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class ReformatProductionWizard extends RefactoringWizard {

	/**
	 * Reformat Info DTO
	 */
	private final ReformatInfo info;

	/**
	 * Constructor
	 * 
	 * @param refactoring
	 * @param info
	 */
  	public ReformatProductionWizard(
  			final Refactoring refactoring, 
  			final ReformatInfo info ) {
  		super( refactoring, DIALOG_BASED_USER_INTERFACE );
  		this.info = info;
  	}

  	@Override
  	protected void addUserInputPages() {
  		setDefaultPageTitle( getRefactoring().getName() );
  		addPage( new ReformatProductionInputPage( info ) );
  	}
}
