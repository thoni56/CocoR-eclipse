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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import at.ssw.coco.ide.Activator;
import at.ssw.coco.ide.features.refactoring.core.RenameRefactorInfo;
import at.ssw.coco.ide.features.refactoring.ui.UITexts;

/**
 * implements rename refactor input page for 
 * rename refactor wizard 
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class RenameRefactorInputPage extends UserInputWizardPage {

	private static final String DS_KEY = RenameRefactorInputPage.class.getName();

	/**
	 * Rename Info DTO
	 */
	private final RenameRefactorInfo info;
  
	/**
	 * Dialog Settings Reference
	 */
	private IDialogSettings dialogSettings;
	
	/**
	 * Text Input Field for new name
	 */
	private Text txtNewName;

	/**
	 * Constructor
	 * 
	 * @param info
	 */
	public RenameRefactorInputPage(final RenameRefactorInfo info) {
		super(RenameRefactorInputPage.class.getName());
		this.info = info;
		initDialogSettings();
	}

	@Override
  	public void createControl(final Composite parent) {
  		Composite composite = createRootComposite(parent);
  		setControl(composite);
  		createLblNewName(composite);
  		createTxtNewName(composite);
    	validate();
  	}

  	/**
	 * render layout
  	 * 
  	 * @param parent
  	 * @return
  	 */
  	private Composite createRootComposite(final Composite parent) {
  		Composite result = new Composite(parent, SWT.NONE);
    	GridLayout gridLayout = new GridLayout(2, false);
    	gridLayout.marginWidth = 10;
    	gridLayout.marginHeight = 10;
    	result.setLayout(gridLayout);
    	initializeDialogUnits(result);
    	Dialog.applyDialogFont(result);
    	return result;
  	}
  
  	/**
	 * render label for new name
  	 * 
  	 * @param composite
  	 */
  	private void createLblNewName(final Composite composite) {
	  	Label lblNewName = new Label(composite, SWT.NONE);
    	lblNewName.setText(UITexts.renameCharacter_InputPage_lblNewName);
  	}

  	/**
	 * render Text Input Field for new name
  	 * 
  	 * @param composite
  	 */
  	private void createTxtNewName(Composite composite) {
  		txtNewName = new Text(composite, SWT.BORDER);
  		txtNewName.setText(info.getOldName());
  		txtNewName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  		txtNewName.selectAll();
  		txtNewName.addKeyListener(new KeyAdapter() {
	    	public void keyReleased(final KeyEvent e) {
	      		info.setNewName(txtNewName.getText());
	      		validate();
	    	}
    	});
  	}
      
  	/**
  	 * initialize dialog settings
  	 */
  	private void initDialogSettings() {
	  	IDialogSettings ds = Activator.getDefault().getDialogSettings();
    	dialogSettings = ds.getSection(DS_KEY);
    	if(dialogSettings == null) {
    		dialogSettings = ds.addNewSection(DS_KEY);
    	}
  	}
  
  	/**
  	 * validate user inputs
  	 */
  	private void validate() {
  		String txt = txtNewName.getText();
    	setPageComplete(txt.length() > 0 && !txt.equals(info.getOldName()));
  	}
}
