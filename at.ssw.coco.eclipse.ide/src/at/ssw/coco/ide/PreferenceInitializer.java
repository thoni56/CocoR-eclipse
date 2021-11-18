package at.ssw.coco.ide;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.spelling.SpellingService;

/**
 * Preference initializer.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	private static final String ID_PREFIX = Activator.PLUGIN_ID + ".";

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PreferenceConstants.getPreferenceStore();

		// disable the spelling service
		store.setDefault(SpellingService.PREFERENCE_SPELLING_ENABLED, false);

		PreferenceConstants.initializeDefaultPreferences(store);
	}

	public static void setThemeBasedPreferences(IPreferenceStore store) {
		ColorRegistry registry= null;
		if (PlatformUI.isWorkbenchRunning())
				registry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();

		setDefaultColor(store, registry, PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, new RGB(192,192,192));
	}

	private static void setDefaultColor(IPreferenceStore store, ColorRegistry registry, String key, RGB defaultRGB) {
		if (registry != null) {
			RGB rgb = registry.getRGB(ID_PREFIX + key);
			if (rgb != null) {
				PreferenceConverter.setDefault(store, key, rgb);
				return;
			}
		}
		PreferenceConverter.setDefault(store, key, defaultRGB);
	}
}
