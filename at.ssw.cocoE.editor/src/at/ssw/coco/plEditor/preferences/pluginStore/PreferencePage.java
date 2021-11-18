package at.ssw.coco.plEditor.preferences.pluginStore;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import at.ssw.coco.plEditor.EditorPlugin;


public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	
	public PreferencePage() {
		super(GRID);
		
		
		
		EditorPlugin plugin = EditorPlugin.getDefault();
		IPreferenceStore store = plugin.getPreferenceStore();
		
//		setTitle("the title");
//		
		setPreferenceStore(store);
	}
	
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createFieldEditors() {
	
		
		DirectoryFieldEditor de = new DirectoryFieldEditor(PreferenceConstants.fieldName, PreferenceConstants.fieldLabel, getFieldEditorParent());
		
		addField(de);
		
		
		
//		
//		IWorkbench wb = PlatformUI.getWorkbench();
//		
//		
//		
//		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
//		IContentType type = contentTypeManager.getContentType("at.ssw.cocoE.editor.CocoEContentType");
//		
//		String[] knownFiles = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
//		
//		
//		
//		String[][] editorEntries = new String[knownFiles.length][2];
//		for(int i=0; i<knownFiles.length; i++){
//			editorEntries[i][0] = knownFiles[i];
//			editorEntries[i][1] = knownFiles[i];
//		}
//		
//		ComboFieldEditor comboBox = new ComboFieldEditor(PreferenceConstants.comboBoxName, PreferenceConstants.comboBoxLabel, editorEntries, getFieldEditorParent());
//		
//		addField(comboBox);
//		
//		Composite parent = this.getFieldEditorParent();
//		
//		Button b = new Button(parent, SWT.PUSH);
//		b.setSize(100, 100);
//		b.setText("Edit Highlighting");
//		b.addSelectionListener(new SelectionAdapter() {
////			MyApp app = new MyApp();
//			
//		
//		});
		
		

	}
}
