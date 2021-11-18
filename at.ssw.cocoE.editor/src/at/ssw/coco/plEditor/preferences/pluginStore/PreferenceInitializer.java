package at.ssw.coco.plEditor.preferences.pluginStore;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import at.ssw.coco.plEditor.EditorPlugin;


public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		EditorPlugin plugin = EditorPlugin.getDefault();
		IPreferenceStore store = plugin.getPreferenceStore();
		
		String path = EditorPlugin.getDefault().getStateLocation().toFile().getAbsolutePath();
		File file = new File(path+Path.SEPARATOR+"editors");
		file.mkdirs();
		path = file.getAbsolutePath();
		
		
		store.setDefault(PreferenceConstants.fieldName, path);
		store.setValue(PreferenceConstants.fieldName, path);

	}

}
