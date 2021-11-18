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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import at.ssw.coco.ide.Activator;
import at.ssw.coco.ide.features.refactoring.core.ReformatInfo;
import at.ssw.coco.ide.features.refactoring.ui.UITexts;

/**
 * implements reformat ATG productions input page for 
 * reformat productions wizard 
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class ReformatProductionInputPage extends UserInputWizardPage {

	private static final String DS_KEY = ReformatProductionInputPage.class.getName();

	/**
	 * Reformat Info DTO
	 */
	private final ReformatInfo info;
  
	/**
	 * Dialog Settings Reference
	 */
	private IDialogSettings dialogSettings;
	
	/**
	 * Check Box - reformat all productions
	 */
	private Button btnReformatAll;
	
	/**
	 * Check Box - use fix semantic text offset
	 */
	private Button btnUseFixedOffset;
	
	/**
	 * Text Input Field for fix semantic text offset
	 */
	private Text txtJavaLineOffset;

	/**
	 * Constructor
	 * 
	 * @param info
	 */
	public ReformatProductionInputPage(final ReformatInfo info) {
		super(ReformatProductionInputPage.class.getName());
		this.info = info;
		initDialogSettings();
	}

	@Override
  	public void createControl(final Composite parent) {
  		Composite composite = createRootComposite(parent);
  		setControl(composite);
  		//createLblMessage(composite);
  		createBtnReformatAllProductions(composite); 
  		createBtnUseFixedOffset(composite); 
  		createTxtJavaLineOffset(composite);
  	}

  	/**
  	 * render Check Box - use fix semantic text offset
  	 * 
  	 * @param composite
  	 */
	private void createBtnUseFixedOffset(Composite composite) {
		btnUseFixedOffset = new Button(composite, SWT.CHECK);
		btnUseFixedOffset.setText(UITexts.reformatProduction_UseFixedOffset);
		btnUseFixedOffset.setSelection(info.isUseFixedOffset());
		btnUseFixedOffset.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
					info.setUseFixedOffset(btnUseFixedOffset.getSelection());
					txtJavaLineOffset.setVisible(info.isUseFixedOffset());
					validate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
 	}

	/**
	 * Render Check Box - reformat all productions
	 * 
	 * @param composite
	 */
	private void createBtnReformatAllProductions(Composite composite) {
		btnReformatAll = new Button(composite, SWT.CHECK);
  		btnReformatAll.setText(UITexts.reformatProduction_ReformatAllProductions);
  		btnReformatAll.setSelection(info.isReformatAllProductions());
  		if (info.getSelectionOffset() < 0) {
  			btnReformatAll.setVisible(false);
  		}
 		btnReformatAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
					info.setReformatAllProductions(btnReformatAll.getSelection());
					validate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	/**
	 * render layout
	 * 
	 * @param parent
	 * @return root composite
	 */
  	private Composite createRootComposite(final Composite parent) {
  		Composite result = new Composite(parent, SWT.NONE);
    	GridLayout gridLayout = new GridLayout(1, false);
    	gridLayout.marginWidth = 10;
    	gridLayout.marginHeight = 10;
    	result.setLayout(gridLayout);
    	initializeDialogUnits(result);
    	Dialog.applyDialogFont(result);
    	return result;
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
	 * render Text Input Field for fix semantic text offset
	 * 
  	 * @param composite
  	 */
  	private void createTxtJavaLineOffset(Composite composite) {
  		txtJavaLineOffset = new Text(composite, SWT.BORDER);
  		txtJavaLineOffset.setText(Integer.toString(info.getJavaLineOffset()));
  		txtJavaLineOffset.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  		txtJavaLineOffset.selectAll();
  		txtJavaLineOffset.setVisible(info.isUseFixedOffset());
  		txtJavaLineOffset.addKeyListener(new KeyAdapter() {
	    	public void keyReleased(final KeyEvent e) {
	    		try {
		    		int offset = Integer.parseInt(txtJavaLineOffset.getText());
		    		if (offset >= 0) {
		    			info.setJavaLineOffset(offset);
		    		}
	    		} catch (NumberFormatException exception) {
	    		}
	    		validate();
	    	}
    	});
  	}
  	
  	/**
  	 * validate user inputs
  	 */
	private void validate() {
		try {
    		if (!info.isUseFixedOffset()) {
    			setPageComplete(true);
    		} else {
				int offset = Integer.parseInt(txtJavaLineOffset.getText());
	    		if (offset >= 0) {
	    			setPageComplete(true);
	    		} else {
	    			setPageComplete(false);
	    		}
    		}
		} catch (NumberFormatException exception) {
			setPageComplete(false);
		}
	}
}
