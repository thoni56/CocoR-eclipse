/**
 * Copyright (C) 2009 Andreas Woess
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package at.ssw.coco.ide.model.properties;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.forms.widgets.Section;

import at.ssw.coco.builder.Activator;

/**
 * A property page for .atg files that allows to set their Coco/R parameters.
 * 
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class CocoParameterPropertyPage extends PropertyPage {
	private static boolean isTrue(String s) {
		return "true".equals(s);
	}

	private static String nonEmtpyOrNull(String s) {
		return s.length() > 0 ? s : null;
	}

	private static String nonNull(String s) {
		return s != null ? s : "";
	}

	private static String trueOrNull(boolean b) {
		return b ? "true" : null;
	}

	private static int uniq(char[] array) {
		int len = array.length;
		for (int i = array.length - 1; i > 0; i--) {
			if (array[i - 1] == array[i]) {
				System.arraycopy(array, i, array, i - 1, len - i);
				len--;
			}
		}
		return len;
	}

	private Button chkCustomOutputDir;
	private Text txtCustomOutputDir;
	private Button btnSelectOutputDir;

	private Button chkCustomNamespace;
	private Text txtCustomNamespace;

	private Button chkCustomFramesDir;

	private Text txtCustomFramesDir;

	private Button btnSelectFramesDir;

	private Text txtTraceString;

	private Button btnClearTraceString;

	@Override
	protected Control createContents(Composite parent) {
		final Font font = parent.getFont();

		GridData gdGrp = new GridData(SWT.FILL, SWT.NONE, true, true);
		GridData gdChk = new GridData(SWT.FILL, SWT.NONE, false, false);
		gdChk.horizontalSpan = 2;
		GridData gdTxt = new GridData(GridData.FILL_HORIZONTAL);
		GridData gdBtn = new GridData(SWT.FILL, SWT.NONE, false, false);

		final Composite contentsComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = layout.marginHeight = 0;
		contentsComposite.setLayout(layout);
		contentsComposite.setFont(font);

		final Composite composite = new Composite(contentsComposite, SWT.NONE);
		composite.setLayoutData(gdGrp);
		layout = new GridLayout(1, false);
		layout.marginWidth = layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setFont(font);

		final Composite mainGroup = new Composite(composite, SWT.NONE);
		mainGroup.setLayoutData(gdGrp);
		layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 0;
		mainGroup.setLayout(layout);
		mainGroup.setFont(font);

		chkCustomOutputDir = new Button(mainGroup, SWT.CHECK);
		chkCustomOutputDir.setText("Use custom output directory:");
		chkCustomOutputDir.setLayoutData(gdChk);
		chkCustomOutputDir.setFont(font);
		chkCustomOutputDir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				txtCustomOutputDir.setEnabled(chkCustomOutputDir.getSelection());
				btnSelectOutputDir.setEnabled(chkCustomOutputDir.getSelection());
			}
		});

		txtCustomOutputDir = new Text(mainGroup, SWT.BORDER);
		txtCustomOutputDir.setEnabled(false);
		txtCustomOutputDir.setLayoutData(gdTxt);

		btnSelectOutputDir = new Button(mainGroup, SWT.PUSH);
		btnSelectOutputDir.setText("Browse ...");
		btnSelectOutputDir.setEnabled(false);

		btnSelectOutputDir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(),
						SWT.MULTI);
				dialog.setText("Select the output folder for Coco/R generated files.");

				if (getFile() != null) {
					dialog.setFilterPath(getFile().getLocation()
							.removeLastSegments(1).toOSString());
				}

				String res = dialog.open();
				if (res != null) {
					IPath thePath = Path.fromOSString(res);
					IPath projectPath = getProject().getLocation();

					// if directory is within the project...
					if (projectPath.isPrefixOf(thePath)) {
						// make the path relative to the project path
						thePath = thePath.makeRelativeTo(projectPath);
					}
					txtCustomOutputDir.setText(thePath.toPortableString());
				}
			}
		});

		chkCustomFramesDir = new Button(mainGroup, SWT.CHECK);
		gdGrp = new GridData(GridData.FILL_HORIZONTAL);
		chkCustomFramesDir.setText("Use custom frame files directory:");
		chkCustomFramesDir.setLayoutData(gdChk);
		chkCustomFramesDir.setFont(font);
		chkCustomFramesDir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				txtCustomFramesDir.setEnabled(chkCustomFramesDir.getSelection());
				btnSelectFramesDir.setEnabled(chkCustomFramesDir.getSelection());
			}
		});

		txtCustomFramesDir = new Text(mainGroup, SWT.BORDER);
		txtCustomFramesDir.setEnabled(false);
		txtCustomFramesDir.setLayoutData(gdTxt);

		btnSelectFramesDir = new Button(mainGroup, SWT.PUSH);
		btnSelectFramesDir.setText("Browse ...");
		btnSelectFramesDir.setEnabled(false);

		btnSelectFramesDir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(),
						SWT.MULTI);
				dialog.setText("Select the folder containing the Coco/R frame files.");

				if (getFile() != null) {
					dialog.setFilterPath(getFile().getLocation()
							.removeLastSegments(1).toOSString());
				}

				String res = dialog.open();
				if (res != null) {
					IPath thePath = Path.fromOSString(res);
					IPath projectPath = getProject().getLocation();

					// if directory is within the project...
					if (projectPath.isPrefixOf(thePath)) {
						// make the path relative to the project path
						thePath = thePath.makeRelativeTo(projectPath);
					}
					txtCustomFramesDir.setText(thePath.toPortableString());
				}
			}
		});

		chkCustomNamespace = new Button(mainGroup, SWT.CHECK);
		gdGrp = new GridData(GridData.FILL_HORIZONTAL);
		chkCustomNamespace.setText("Use custom package/namespace:");
		chkCustomNamespace.setLayoutData(gdChk);
		chkCustomNamespace.setFont(font);
		chkCustomNamespace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				txtCustomNamespace.setEnabled(chkCustomNamespace.getSelection());
			}
		});

		txtCustomNamespace = new Text(mainGroup, SWT.BORDER);
		txtCustomNamespace.setEnabled(false);
		txtCustomNamespace.setLayoutData(gdTxt);
		new Label(mainGroup, SWT.NONE).setVisible(false);

		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setVisible(false);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		Section section = new Section(composite, SWT.NONE);
		section.setLayoutData(gdGrp);
		section.setLayout(new GridLayout(1, false));
		section.setText("Trace options:");
		final Composite traceGroup = new Composite(section, SWT.NONE);
		traceGroup.setLayoutData(gdGrp);
		traceGroup.setLayout(new GridLayout(2, false));
		section.setClient(traceGroup);

		txtTraceString = new Text(traceGroup, SWT.BORDER);
		txtTraceString.setLayoutData(gdTxt);
		txtTraceString.setEnabled(true);

		btnClearTraceString = new Button(traceGroup, SWT.PUSH);
		btnClearTraceString.setText("Clear");
		btnClearTraceString.setLayoutData(gdBtn);
		btnClearTraceString.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtTraceString.setText("");
			}
		});

		createTraceCheckButton(traceGroup, "trace automaton", 'A')
				.setLayoutData(gdChk);
		createTraceCheckButton(traceGroup, "list first/follow sets", 'F')
				.setLayoutData(gdChk);
		createTraceCheckButton(traceGroup, "print syntax graph", 'G')
				.setLayoutData(gdChk);
		createTraceCheckButton(traceGroup, "trace computation of first sets",
				'I').setLayoutData(gdChk);
		createTraceCheckButton(traceGroup, "list ANY and SYNC sets", 'J')
				.setLayoutData(gdChk);
		createTraceCheckButton(traceGroup, "print statistics", 'P')
				.setLayoutData(gdChk);
		createTraceCheckButton(traceGroup, "list symbol table", 'S')
				.setLayoutData(gdChk);
		createTraceCheckButton(traceGroup, "list cross reference table", 'X')
				.setLayoutData(gdChk);

		readValues();

		return contentsComposite;
	}

	private Button createTraceCheckButton(Composite parent, String text,
			final char ch) {
		final Button button = new Button(parent, SWT.CHECK);
		button.setText(text);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String traceString = txtTraceString.getText().toUpperCase();
				boolean add = button.getSelection();
				if (add && -1 == traceString.indexOf(ch)) {
					char[] traces = (traceString + ch).toUpperCase()
							.toCharArray();
					Arrays.sort(traces);
					int len = uniq(traces);
					traceString = new String(traces, 0, len);
					txtTraceString.setText(traceString);
				} else if (!add && -1 != traceString.indexOf(ch)) {
					char[] traces = traceString.replace(String.valueOf(ch), "")
							.toUpperCase().toCharArray();
					Arrays.sort(traces);
					int len = uniq(traces);
					traceString = new String(traces, 0, len);
					txtTraceString.setText(traceString);
				}
			}
		});
		txtTraceString.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String traces = txtTraceString.getText().toUpperCase();
				boolean set = (-1 != traces.indexOf(ch));
				if (button.getSelection() != set) {
					button.setSelection(set);
				}
			}
		});
		return button;
	}

	private IFile getFile() {
		return (getElement() != null) ? (IFile) getElement().getAdapter(
				IFile.class) : null;
	}

	private IProject getProject() {
		return (getFile() != null) ? getFile().getProject() : null;
	}

	@Override
	protected void performDefaults() {
		chkCustomOutputDir.setSelection(false);
		txtCustomOutputDir.setText("");
		chkCustomFramesDir.setSelection(false);
		txtCustomFramesDir.setText("");
		chkCustomNamespace.setSelection(false);
		txtCustomNamespace.setText("");
		txtTraceString.setText("");

		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		storeValues();
		return super.performOk();
	}

	private void readValues() {
		final IFile file = getFile();
		if (file != null) {
			try {
				String outputDir = nonNull(file
						.getPersistentProperty(Activator.CUSTOM_COCO_OUTPUT_DIR));
				txtCustomOutputDir.setText(outputDir);

				boolean useOutputDir = isTrue(file
						.getPersistentProperty(Activator.USE_CUSTOM_COCO_OUTPUT_DIR));
				chkCustomOutputDir.setSelection(useOutputDir);
				txtCustomOutputDir.setEnabled(useOutputDir);
				btnSelectOutputDir.setEnabled(useOutputDir);

				String framesDir = nonNull(file
						.getPersistentProperty(Activator.CUSTOM_COCO_FRAMES_DIR));
				txtCustomFramesDir.setText(framesDir);

				boolean useFramesDir = isTrue(file
						.getPersistentProperty(Activator.USE_CUSTOM_COCO_FRAMES_DIR));
				chkCustomFramesDir.setSelection(useFramesDir);
				txtCustomFramesDir.setEnabled(useFramesDir);
				btnSelectFramesDir.setEnabled(useFramesDir);

				String namespace = nonNull(file
						.getPersistentProperty(Activator.CUSTOM_COCO_NAMESPACE));
				txtCustomNamespace.setText(namespace);

				boolean useNamespace = isTrue(file
						.getPersistentProperty(Activator.USE_CUSTOM_COCO_NAMESPACE));
				chkCustomNamespace.setSelection(useNamespace);
				txtCustomNamespace.setEnabled(useNamespace);

				String traceString = nonNull(file
						.getPersistentProperty(Activator.COCO_TRACE_STRING));
				txtTraceString.setText(traceString);
			} catch (CoreException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void storeValues() {
		final IFile file = getFile();
		if (file != null) {
			try {
				boolean useCustomOutputDir = chkCustomOutputDir.getSelection();
				String customOutputDir = txtCustomOutputDir.getText();
				boolean useCustomFramesDir = chkCustomFramesDir.getSelection();
				String customFramesDir = txtCustomFramesDir.getText();
				boolean useCustomNamespace = chkCustomNamespace.getSelection();
				String customNamespace = txtCustomNamespace.getText();
				String traceString = nonEmtpyOrNull(txtTraceString.getText());

				if (!useCustomOutputDir && "".equals(customOutputDir)) {
					customOutputDir = null;
				}
				if (!useCustomFramesDir && "".equals(customFramesDir)) {
					customFramesDir = null;
				}
				if (!useCustomNamespace && "".equals(customNamespace)) {
					customNamespace = null;
				}

				file.setPersistentProperty(
						Activator.USE_CUSTOM_COCO_OUTPUT_DIR,
						trueOrNull(useCustomOutputDir));
				file.setPersistentProperty(Activator.CUSTOM_COCO_OUTPUT_DIR,
						customOutputDir);
				file.setPersistentProperty(
						Activator.USE_CUSTOM_COCO_FRAMES_DIR,
						trueOrNull(useCustomFramesDir));
				file.setPersistentProperty(Activator.CUSTOM_COCO_FRAMES_DIR,
						customFramesDir);
				file.setPersistentProperty(Activator.USE_CUSTOM_COCO_NAMESPACE,
						trueOrNull(useCustomNamespace));
				file.setPersistentProperty(Activator.CUSTOM_COCO_NAMESPACE,
						customNamespace);
				file.setPersistentProperty(Activator.COCO_TRACE_STRING,
						traceString);
			} catch (CoreException ex) {
				ex.printStackTrace();
			}
		}
	}
}
