package at.ssw.coco.ide;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Preference constants.
 *
 * @see org.eclipse.jdt.ui.PreferenceConstants
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class PreferenceConstants {
	public static final String EDITOR_MATCHING_BRACKETS = "matchingBrackets"; //$NON-NLS-1$
	public static final String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor"; //$NON-NLS-1$
	public static final String EDITOR_FOLDING_ENABLED= "editor_folding_enabled";

	public static final String OUTLINE_LEXICAL_SORTING_CHECKED = "LexicalSortingAction.isChecked"; //$NON-NLS-1$

	public static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public static void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(EDITOR_MATCHING_BRACKETS, true);
		store.setDefault(OUTLINE_LEXICAL_SORTING_CHECKED, false);
		store.setDefault(EDITOR_FOLDING_ENABLED, true);

		PreferenceInitializer.setThemeBasedPreferences(store);
	}
}
