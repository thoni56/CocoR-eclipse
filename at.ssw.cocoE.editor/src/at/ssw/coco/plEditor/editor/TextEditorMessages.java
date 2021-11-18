package at.ssw.coco.plEditor.editor;

import java.util.ResourceBundle;

/**
 * Provides the RecourceBundle for the CLNGEdtor.
 * Actions have to be specified in <code>TextEditorMessages.properties</code>
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 *
 */
public class TextEditorMessages {
    private static final String BUNDLE_NAME = TextEditorMessages.class.getName();
    private static ResourceBundle theBundle = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Returns the message bundle which contains constructed keys.
     *
     * @return the message bundle
     */
    public static ResourceBundle getBundle() {
            return theBundle;
    }

	private TextEditorMessages() {
	}
}
