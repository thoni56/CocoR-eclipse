/*******************************************************************************
 * Copyright (C) 2009 Institute for System Software, JKU Linz
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
package at.ssw.coco.ide.style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Implements a syntax manager which handles the requested syntaxes.
 * 
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Markus Koppensteiner <mkoppensteiner@users.sf.net>
 */

@SuppressWarnings("restriction")
public final class SyntaxManager {

	public static enum ATG {
		/** The syntax id representing ATG code */
		DEFAULT,
		/** The syntax id representing an ATG keyword */
		KEYWORD,
		/** The syntax id representing a semantic action delimiter */
		INLINE_CODE_TAG;

		public static SyntaxManager getInstance(ISharedTextColors sharedColors) {

			if (jsm == null) {
				jsm = new SyntaxManager();

				jsm.sharedColors = sharedColors;

				jsm.addSyntax(Common.COMMENT,
						sharedColors.getColor(DefaultColors.COMMENT));
				jsm.addSyntax(Common.STRING,
						sharedColors.getColor(DefaultColors.STRING));

				jsm.addJavaSyntax(Java.DEFAULT,
						IJavaColorConstants.JAVA_DEFAULT, sharedColors);
				jsm.addJavaSyntax(Java.KEYWORD,
						IJavaColorConstants.JAVA_KEYWORD, sharedColors);
				jsm.addJavaSyntax(Java.KEYWORD_RETURN,
						IJavaColorConstants.JAVA_KEYWORD_RETURN, sharedColors);
				jsm.addJavaSyntax(Java.SINGLE_LINE_COMMENT,
						IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT,
						sharedColors);
				jsm.addJavaSyntax(Java.MULTI_LINE_COMMENT,
						IJavaColorConstants.JAVA_MULTI_LINE_COMMENT,
						sharedColors);
				jsm.addJavaSyntax(Java.STRING, IJavaColorConstants.JAVA_STRING,
						sharedColors);
				jsm.addJavaSyntax(Java.OPERATOR,
						IJavaColorConstants.JAVA_OPERATOR, sharedColors);
				jsm.addJavaSyntax(Java.BRACKET,
						IJavaColorConstants.JAVA_BRACKET, sharedColors);
				jsm.addJavaSyntax(Java.JAVADOC,
						IJavaColorConstants.JAVADOC_DEFAULT, sharedColors);

				jsm.addSyntax(ATG.DEFAULT,
						sharedColors.getColor(DefaultColors.DEFAULT));
				jsm.addSyntax(ATG.KEYWORD,
						sharedColors.getColor(DefaultColors.ATG_KEYWORD),
						SWT.BOLD);
				jsm.addSyntax(ATG.INLINE_CODE_TAG,
						sharedColors.getColor(DefaultColors.INLINE_CODE_TAG),
						SWT.BOLD);

			}

			return jsm;
		}
	}

	public static enum Common {
		/** The syntax id representing a comment */
		COMMENT,

		/** The syntax id representing a quoted string or character */
		STRING;
	}

	private static class DefaultColors {
		private static final RGB DEFAULT = new RGB(0, 0, 0);
		private static final RGB COMMENT = new RGB(63, 127, 95);
		private static final RGB STRING = new RGB(42, 0, 255);
		private static final RGB INLINE_CODE_TAG = new RGB(190, 140, 15);
		private static final RGB FRAME_KEYWORD = new RGB(13, 100, 0);
		private static final RGB ATG_KEYWORD = new RGB(50, 63, 112);
	}

	public static enum Frame {
		/** The syntax id representing a highlighted frame keyword */
		KEYWORD;

		public static SyntaxManager getInstance(ISharedTextColors sharedColors) {

			if (fsm == null) {
				fsm = new SyntaxManager();
				fsm.addSyntax(Common.COMMENT,
						sharedColors.getColor(DefaultColors.COMMENT));
				fsm.addSyntax(Common.STRING,
						sharedColors.getColor(DefaultColors.STRING));

				fsm.addJavaSyntax(Java.DEFAULT,
						IJavaColorConstants.JAVA_DEFAULT, sharedColors);
				fsm.addJavaSyntax(Java.KEYWORD,
						IJavaColorConstants.JAVA_KEYWORD, sharedColors);
				fsm.addJavaSyntax(Java.KEYWORD_RETURN,
						IJavaColorConstants.JAVA_KEYWORD_RETURN, sharedColors);
				fsm.addJavaSyntax(Java.SINGLE_LINE_COMMENT,
						IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT,
						sharedColors);
				fsm.addJavaSyntax(Java.MULTI_LINE_COMMENT,
						IJavaColorConstants.JAVA_MULTI_LINE_COMMENT,
						sharedColors);
				fsm.addJavaSyntax(Java.STRING, IJavaColorConstants.JAVA_STRING,
						sharedColors);
				fsm.addJavaSyntax(Java.OPERATOR,
						IJavaColorConstants.JAVA_OPERATOR, sharedColors);
				fsm.addJavaSyntax(Java.BRACKET,
						IJavaColorConstants.JAVA_BRACKET, sharedColors);
				fsm.addJavaSyntax(Java.JAVADOC,
						IJavaColorConstants.JAVADOC_DEFAULT, sharedColors);

				fsm.addSyntax(Frame.KEYWORD,
						sharedColors.getColor(DefaultColors.FRAME_KEYWORD),
						SWT.BOLD);

			}

			return fsm;
		}
	}

	public static enum Java {
		/** The syntax id representing Java code. */
		DEFAULT,

		/** The syntax id representing a Java keyword. */
		KEYWORD,

		/** The syntax id representing the return keyword. */
		KEYWORD_RETURN,

		/** The syntax id representing a Java single line comment. */
		SINGLE_LINE_COMMENT,

		/** The syntax id representing a Java multi line comment. */
		MULTI_LINE_COMMENT,

		/** The syntax id representing a Java String. */
		STRING,

		/** The syntax id representing a Java operator. */
		OPERATOR,

		/** The syntax id representing a Java bracket. */
		BRACKET,

		/** The syntax id representing a Java document comment. */
		JAVADOC,

	}

	private class PCListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			removeJavaStyle();
			addJavaStyle();
			firePropertyChangedEvent(evt);
		}
	}

	public interface PropertyChangedListener {
		public void propertyChanged(PropertyChangeEvent evt);
	}

	/** Listeners for property changes */
	private List<PropertyChangedListener> propertyChangedListeners;

	/** The table holding the <code>Token</code> representations */
	private final Map<Enum<?>, Token> syntaxTable = new HashMap<Enum<?>, Token>();

	/** The JDT UI preference store */
	private final IPreferenceStore javaPreferenceStore = PreferenceConstants
			.getPreferenceStore();

	/** The colors used */
	private ISharedTextColors sharedColors;
	private static SyntaxManager fsm = null;
	private static SyntaxManager jsm = null;

	/** The Constructor */
	private SyntaxManager() {
		propertyChangedListeners = new ArrayList<PropertyChangedListener>();
		JavaPlugin.getDefault().getCombinedPreferenceStore()
				.addPropertyChangeListener(new PCListener());
	}

	private void addJavaStyle() {
		addJavaSyntax(Java.DEFAULT, IJavaColorConstants.JAVA_DEFAULT,
				sharedColors);
		addJavaSyntax(Java.KEYWORD, IJavaColorConstants.JAVA_KEYWORD,
				sharedColors);
		addJavaSyntax(Java.KEYWORD_RETURN,
				IJavaColorConstants.JAVA_KEYWORD_RETURN, sharedColors);
		addJavaSyntax(Java.SINGLE_LINE_COMMENT,
				IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT, sharedColors);
		addJavaSyntax(Java.MULTI_LINE_COMMENT,
				IJavaColorConstants.JAVA_MULTI_LINE_COMMENT, sharedColors);
		addJavaSyntax(Java.STRING, IJavaColorConstants.JAVA_STRING,
				sharedColors);
		addJavaSyntax(Java.OPERATOR, IJavaColorConstants.JAVA_OPERATOR,
				sharedColors);
		addJavaSyntax(Java.BRACKET, IJavaColorConstants.JAVA_BRACKET,
				sharedColors);
		addJavaSyntax(Java.JAVADOC, IJavaColorConstants.JAVADOC_DEFAULT,
				sharedColors);
	}

	private void addJavaSyntax(Enum<Java> syntaxId, String preferenceKey,
			ISharedTextColors sharedColors) {
		final String colorPreferenceKey = preferenceKey;
		final String boldPreferenceKey = preferenceKey
				+ PreferenceConstants.EDITOR_BOLD_SUFFIX;
		final String italicPreferenceKey = preferenceKey
				+ PreferenceConstants.EDITOR_ITALIC_SUFFIX;
		final String strikethroughPreferenceKey = preferenceKey
				+ PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX;
		final String underlinePreferenceKey = preferenceKey
				+ PreferenceConstants.EDITOR_UNDERLINE_SUFFIX;

		addSyntax(syntaxId, sharedColors, javaPreferenceStore,
				colorPreferenceKey, boldPreferenceKey, italicPreferenceKey,
				strikethroughPreferenceKey, underlinePreferenceKey);
	}

	public void addPropertyChangedListener(PropertyChangedListener l) {
		propertyChangedListeners.add(l);
	}

	private void addSyntax(Enum<?> syntaxId, Color color) {
		syntaxTable.put(syntaxId, new Token(new TextAttribute(color, null,
				SWT.NORMAL)));
	}

	private void addSyntax(Enum<?> syntaxId, Color color, int style) {
		syntaxTable.put(syntaxId, new Token(new TextAttribute(color, null,
				style)));
	}

	private void addSyntax(Enum<?> syntaxId, ISharedTextColors sharedColors,
			IPreferenceStore store, String colorPreference,
			String boldPreference, String italicPreference,
			String strikethroughPreference, String underlinePreference) {
		int style = SWT.NORMAL;
		if (store.getBoolean(boldPreference)) {
			style |= SWT.BOLD;
		}
		if (store.getBoolean(italicPreference)) {
			style |= SWT.ITALIC;
		}
		if (store.getBoolean(strikethroughPreference)) {
			style |= TextAttribute.STRIKETHROUGH;
		}
		if (store.getBoolean(underlinePreference)) {
			style |= TextAttribute.UNDERLINE;
		}

		addSyntax(syntaxId, sharedColors.getColor(PreferenceConverter.getColor(
				store, colorPreference)), style);
	}

	protected void firePropertyChangedEvent(PropertyChangeEvent e) {
		for (PropertyChangedListener l : propertyChangedListeners) {
			l.propertyChanged(e);
		}
	}

	/**
	 * Returns the <code>Token</code> representation of the given syntax id.
	 * 
	 * @param syntaxId
	 *            the syntax id
	 * @return the <code>Token</code> representation.
	 */
	public Token getSyntaxToken(Enum<?> syntaxId) {
		return syntaxTable.get(syntaxId);
	}

	/**
	 * Returns the <code>TextAttribute</code> representation of the given
	 * <code>SyntaxDescription</code>.
	 * 
	 * @param syntaxId
	 *            the syntax id.
	 * @return the text attribute.
	 */
	public TextAttribute getTextAttribute(Enum<?> syntaxId) {
		Token t = syntaxTable.get(syntaxId);
		return (t == null ? null : (TextAttribute) t.getData());
	}

	private void removeJavaStyle() {
		syntaxTable.remove(Java.DEFAULT);
		syntaxTable.remove(Java.KEYWORD);
		syntaxTable.remove(Java.KEYWORD_RETURN);
		syntaxTable.remove(Java.SINGLE_LINE_COMMENT);
		syntaxTable.remove(Java.MULTI_LINE_COMMENT);
		syntaxTable.remove(Java.STRING);
		syntaxTable.remove(Java.OPERATOR);
		syntaxTable.remove(Java.BRACKET);
		syntaxTable.remove(Java.JAVADOC);
	}

	public void removePropertyChangedListener(PropertyChangedListener l) {
		propertyChangedListeners.remove(l);
	}
}
