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

package at.ssw.coco.ide.wizard;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import at.ssw.coco.core.CoreUtilities;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The page will only
 * accept a file name without the extension OR with the extension that matches the expected one (atg).
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 *
 */
public class CocoWizardPage extends WizardPage {

	/** The page title */
	private static final String PAGE_TITLE = "Coco/R Parser && Scanner";

	/** The page description */
	private static final String PAGE_DESCRIPTION =
		"This wizard sets up everything needed to extend your Java project with a Coco/R Parser & Scanner.";

	/** The container field text */
	private Text fContainerText;

	/** The filename field text */
	private Text fFileText;

	/** The checkbox to decide to overwrite file or not */
	private Button fOverwrite;

	/** The selection */
	private ISelection fSelection;

	/**
	 * The Constructor
	 *
	 * @param pageName the page's name
	 */
	public CocoWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle(PAGE_TITLE);
		setDescription(PAGE_DESCRIPTION);
		this.fSelection = selection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");

		fContainerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fContainerText.setLayoutData(gd);
		fContainerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fFileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fFileText.setLayoutData(gd);
		fFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		// Dummy Objects
		new Label(container, SWT.CHECK);
		new Label(container, SWT.CHECK);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.minimumHeight = 50;
		gd.horizontalAlignment = SWT.RIGHT;
		fOverwrite = new Button(container, SWT.CHECK);
		fOverwrite.setSelection(false);
		fOverwrite.setText("Overwrite existing files");
		fOverwrite.setLayoutData(gd);

		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
		} else if (container == null || (container.getType() & (IResource.FOLDER | IResource.PROJECT)) == 0) {
			updateStatus("File container must exist");
		} else if (!container.isAccessible()) {
			updateStatus("Folder must be writable");
		} else if (fileName.length() == 0) {
			updateStatus("File name must be specified");
		} else if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
		} else {
			int dotLoc = fileName.lastIndexOf('.');
			updateStatus(null);
			if (dotLoc != -1) {
				String ext = fileName.substring(dotLoc + 1);
				if (!ext.equalsIgnoreCase(CoreUtilities.ATG_EXTENSION)) {
					updateStatus("File extension must be \"" + CoreUtilities.ATG_EXTENSION + "\"");
				}
			}
		}
	}

	/**
	 * Returns the container name.
	 *
	 * @return the container name.
	 */
	public String getContainerName() {
		return fContainerText.getText();
	}

	/**
	 * Returns the ATG file name.
	 *
	 * @return the ATG file name.
	 */
	public String getFileName() {
		return fFileText.getText();
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for the container field.
	 */
	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace()
				.getRoot(), false, "Select new file container");
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				fContainerText.setText(((Path)result[0]).toString());
			}
		}
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if (fSelection != null && !fSelection.isEmpty() && fSelection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection)fSelection;
			if (ssel.size() > 1) {
				return;
			}
			Object obj = ssel.getFirstElement();
			if (obj instanceof IAdaptable) {
				obj = ((IAdaptable)obj).getAdapter(IResource.class);
			}
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer) {
					container = (IContainer)obj;
				} else {
					container = ((IResource)obj).getParent();
				}
				fContainerText.setText(container.getFullPath().toString());
			}
		}
		fFileText.setText("new_file." + CoreUtilities.ATG_EXTENSION);
	}

	public boolean overwriteFiles() {
		return fOverwrite.getSelection();
	}

	/**
	 * Sets the current status of the wizard.
	 *
	 * @param message a possible error message.
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
}
