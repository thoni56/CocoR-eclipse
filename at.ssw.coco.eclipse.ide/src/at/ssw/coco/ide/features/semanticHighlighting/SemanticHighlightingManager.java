package at.ssw.coco.ide.features.semanticHighlighting;

import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IColorManagerExtension;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import at.ssw.coco.ide.editor.ATGConfiguration;
import at.ssw.coco.ide.editor.ATGEditor;
import at.ssw.coco.ide.editor.ATGPresentationReconciler;

/**
 * Managing class for semantic highlighting
 * 
 * @author Markus Koppensteiner <mkoppensteiner@users.sf.net>
 */
public class SemanticHighlightingManager {

	static class HighlightedPosition extends Position {
		/** Highlighting of the position */
		private HighlightingStyle fStyle;
		/** Lock object */
		private Object fLock;

		/**
		 * Initialize the styled positions with the given offset, length and
		 * foreground color.
		 * 
		 * @param offset
		 *            The position offset
		 * @param length
		 *            The position length
		 * @param style
		 *            The position's highlighting
		 * @param lock
		 *            The lock object
		 */
		public HighlightedPosition(int offset, int length,
				HighlightingStyle style, Object lock) {
			super(offset, length);
			fStyle = style;
			fLock = lock;
		}

		/**
		 * @return a corresponding style range.
		 */
		public StyleRange createStyleRange() {
			int len = 0;
			if (fStyle.isEnabled()) {
				len = getLength();
			}

			TextAttribute textAttribute = fStyle.getTextAttribute();
			int style = textAttribute.getStyle();
			int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
			StyleRange styleRange = new StyleRange(getOffset(), len,
					textAttribute.getForeground(),
					textAttribute.getBackground(), fontStyle);
			styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
			styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;

			return styleRange;
		}

		/*
		 * @see org.eclipse.jface.text.Position#delete()
		 */
		@Override
		public void delete() {
			synchronized (fLock) {
				super.delete();
			}
		}

		/**
		 * @return the highlighting style.
		 */
		public HighlightingStyle getStyle() {
			return fStyle;
		}

		/**
		 * Is this position contained in the given range? Synchronizes on
		 * position updater.
		 * 
		 * @param off
		 *            The range offset
		 * @param len
		 *            The range length
		 * @return <code>true</code> if this position is not deleted and
		 *         contained in the given range.
		 */
		public boolean isContained(int off, int len) {
			synchronized (fLock) {
				return !isDeleted() && off <= getOffset()
						&& off + len >= getOffset() + getLength();
			}
		}

		/**
		 * Uses reference equality for the highlighting.
		 * 
		 * @param off
		 *            The offset
		 * @param len
		 *            The length
		 * @param style
		 *            The highlighting
		 * @return <code>true</code> if the given offset, length and
		 *         highlighting are equal to the internal ones.
		 */
		public boolean isEqual(int off, int len, HighlightingStyle style) {
			synchronized (fLock) {
				return !isDeleted() && getOffset() == off && getLength() == len
						&& fStyle == style;
			}
		}

		/*
		 * @see org.eclipse.jface.text.Position#setLength(int)
		 */
		@Override
		public void setLength(int length) {
			synchronized (fLock) {
				super.setLength(length);
			}
		}

		/*
		 * @see org.eclipse.jface.text.Position#setOffset(int)
		 */
		@Override
		public void setOffset(int offset) {
			synchronized (fLock) {
				super.setOffset(offset);
			}
		}

		/*
		 * @see org.eclipse.jface.text.Position#undelete()
		 */
		@Override
		public void undelete() {
			synchronized (fLock) {
				super.undelete();
			}
		}

		/**
		 * Sets the range
		 * 
		 * @param off
		 *            The range offset
		 * @param len
		 *            The range length
		 */
		public void update(int off, int len) {
			synchronized (fLock) {
				super.setOffset(off);
				super.setLength(len);
			}
		}

	}

	/** The style of a particular highlighting */
	static class HighlightingStyle {
		/** Text attribute */
		private TextAttribute fTextAttribute;
		/** Enabled state */
		private boolean fIsEnabled;

		/**
		 * Initialize with the given text attribute.
		 * 
		 * @param textAttribute
		 *            The text attribute
		 * @param isEnabled
		 *            the enabled state
		 */
		public HighlightingStyle(TextAttribute textAttribute, boolean isEnabled) {
			setTextAttribute(textAttribute);
			setEnabled(isEnabled);
		}

		/**
		 * @return the text attribute.
		 */
		public TextAttribute getTextAttribute() {
			return fTextAttribute;
		}

		/**
		 * @return the enabled state
		 */
		public boolean isEnabled() {
			return fIsEnabled;
		}

		/**
		 * @param isEnabled
		 *            the new enabled state
		 */
		public void setEnabled(boolean isEnabled) {
			fIsEnabled = isEnabled;
		}

		/**
		 * @param textAttribute
		 *            The background to set.
		 */
		public void setTextAttribute(TextAttribute textAttribute) {
			fTextAttribute = textAttribute;
		}
	}

	private class PCListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			propertyChanged(evt);
		}

	}

	private PCListener pcListener = new PCListener();

	/** Semantic highlighting presenter */
	private SemanticHighlightingPresenter fPresenter;
	/** Semantic highlighting reconciler */
	private SemanticHighlightingReconciler fReconciler;

	/** Semantic highlightings */
	private SemanticHighlighting[] fSemanticHighlightings;
	/** Highlightings */
	private HighlightingStyle[] fHighlightings;

	/** The editor */
	private ATGEditor fEditor;
	/** The source viewer */
	private SourceViewer fSourceViewer;
	/** The color manager */
	private IColorManager fColorManager;
	/** The preference store */
	private IPreferenceStore fPreferenceStore;

	/** The source viewer configuration */
	private ATGConfiguration fConfiguration;
	/** The presentation reconciler */
	private ATGPresentationReconciler fPresentationReconciler;

	/**
	 * helper method for PropertyChange of a highlighting style
	 * 
	 * @param highlighting
	 *            the HighlightingStyle
	 * @param event
	 *            the PropertyChangeEvent
	 */
	private void adaptToEnablementChange(HighlightingStyle highlighting,
			PropertyChangeEvent event) {
		Object value = event.getNewValue();
		boolean eventValue;
		if (value instanceof Boolean) {
			eventValue = ((Boolean) value).booleanValue();
		} else if (IPreferenceStore.TRUE.equals(value)) {
			eventValue = true;
		} else {
			eventValue = false;
		}
		highlighting.setEnabled(eventValue);
	}

	/**
	 * helper method for PropertyChange of a highlighting style
	 * 
	 * @param highlighting
	 *            the HighlightingStyle
	 * @param event
	 *            the PropertyChangeEvent
	 */
	private void adaptToTextForegroundChange(HighlightingStyle highlighting,
			PropertyChangeEvent event) {
		RGB rgb = null;

		Object value = event.getNewValue();
		if (value instanceof RGB) {
			rgb = (RGB) value;
		} else if (value instanceof String) {
			rgb = StringConverter.asRGB((String) value);
		}

		if (rgb != null) {

			String property = event.getProperty();
			Color color = fColorManager.getColor(property);

			if ((color == null || !rgb.equals(color.getRGB()))
					&& fColorManager instanceof IColorManagerExtension) {
				IColorManagerExtension ext = (IColorManagerExtension) fColorManager;
				ext.unbindColor(property);
				ext.bindColor(property, rgb);
				color = fColorManager.getColor(property);
			}

			TextAttribute oldAttr = highlighting.getTextAttribute();
			highlighting.setTextAttribute(new TextAttribute(color, oldAttr
					.getBackground(), oldAttr.getStyle()));
		}
	}

	/**
	 * helper method for PropertyChange of a highlighting style
	 * 
	 * @param highlighting
	 *            the HighlightingStyle
	 * @param event
	 *            the PropertyChangeEvent
	 * @param styleAttribute
	 *            the styleAttribute
	 */
	private void adaptToTextStyleChange(HighlightingStyle highlighting,
			PropertyChangeEvent event, int styleAttribute) {
		boolean eventValue = false;
		Object value = event.getNewValue();
		if (value instanceof Boolean) {
			eventValue = ((Boolean) value).booleanValue();
		} else if (IPreferenceStore.TRUE.equals(value)) {
			eventValue = true;
		}

		TextAttribute oldAttr = highlighting.getTextAttribute();
		boolean activeValue = (oldAttr.getStyle() & styleAttribute) == styleAttribute;

		if (activeValue != eventValue) {
			highlighting.setTextAttribute(new TextAttribute(oldAttr
					.getForeground(), oldAttr.getBackground(),
					eventValue ? oldAttr.getStyle() | styleAttribute : oldAttr
							.getStyle() & ~styleAttribute));
		}
	}

	/**
	 * binds a new color to the key
	 * 
	 * @param colorKey
	 *            the colorPreferenceKey
	 */
	private void addColor(String colorKey) {
		if (fColorManager != null && colorKey != null
				&& fColorManager.getColor(colorKey) == null) {
			RGB rgb = PreferenceConverter.getColor(fPreferenceStore, colorKey);
			if (fColorManager instanceof IColorManagerExtension) {
				IColorManagerExtension ext = (IColorManagerExtension) fColorManager;
				ext.unbindColor(colorKey);
				ext.bindColor(colorKey, rgb);
			}
		}
	}

	/**
	 * disables the manager
	 */
	private void disable() {
		if (fReconciler != null) {
			fReconciler.uninstall();
			fReconciler = null;
		}

		if (fPresenter != null) {
			fPresenter.uninstall();
			fPresenter = null;
		}

		if (fSemanticHighlightings != null) {
			disposeStyles();
		}
	}

	/**
	 * disposes the highlighting styles
	 */
	private void disposeStyles() {
		for (int i = 0; i < fSemanticHighlightings.length; i++) {
			removeColor(SemanticHighlightings
					.getColorPreferenceKey(fSemanticHighlightings[i]));
		}

		fSemanticHighlightings = null;
		fHighlightings = null;
	}

	/**
	 * enables the manager
	 */
	private void enable() {
		initStyles();

		fPresenter = new SemanticHighlightingPresenter();
		fPresenter.install(fSourceViewer, fPresentationReconciler);

		fReconciler = new SemanticHighlightingReconciler();
		fReconciler.install(fEditor, fSourceViewer, fPresenter,
				fSemanticHighlightings, fHighlightings);

	}

	public DocumentSynchronizer getSyncer() {
		return fReconciler.getSyncer();
	}

	/**
	 * initializes the highlighting styles
	 */
	private void initStyles() {
		fSemanticHighlightings = SemanticHighlightings
				.getSemanticHighlightings();
		fHighlightings = new HighlightingStyle[fSemanticHighlightings.length];

		for (int i = 0; i < fSemanticHighlightings.length; i++) {
			SemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];
			String colorKey = SemanticHighlightings
					.getColorPreferenceKey(semanticHighlighting);
			addColor(colorKey);

			String boldKey = SemanticHighlightings
					.getBoldPreferenceKey(semanticHighlighting);
			int style = fPreferenceStore.getBoolean(boldKey) ? SWT.BOLD
					: SWT.NORMAL;

			String italicKey = SemanticHighlightings
					.getItalicPreferenceKey(semanticHighlighting);
			if (fPreferenceStore.getBoolean(italicKey)) {
				style |= SWT.ITALIC;
			}

			String strikethroughKey = SemanticHighlightings
					.getStrikethroughPreferenceKey(semanticHighlighting);
			if (fPreferenceStore.getBoolean(strikethroughKey)) {
				style |= TextAttribute.STRIKETHROUGH;
			}

			String underlineKey = SemanticHighlightings
					.getUnderlinePreferenceKey(semanticHighlighting);
			if (fPreferenceStore.getBoolean(underlineKey)) {
				style |= TextAttribute.UNDERLINE;
			}

			boolean isEnabled = fPreferenceStore
					.getBoolean(SemanticHighlightings
							.getEnabledPreferenceKey(semanticHighlighting));

			fHighlightings[i] = new HighlightingStyle(new TextAttribute(
					fColorManager.getColor(PreferenceConverter.getColor(
							fPreferenceStore, colorKey)), null, style),
					isEnabled);
		}

	}

	/**
	 * installs the manager
	 * 
	 * @param editor
	 *            the used ATGEditor
	 * @param sourceViewer
	 *            the used SourceViewer
	 * @param colorManager
	 *            the used ColorManager
	 * @param preferenceStore
	 *            the used prefereceStore
	 * @param configuration
	 *            the used ATGConfiguration
	 */
	public void install(ATGEditor editor, SourceViewer sourceViewer,
			IColorManager colorManager, IPreferenceStore preferenceStore,
			ATGConfiguration configuration) {
		fEditor = editor;
		fSourceViewer = sourceViewer;
		fColorManager = colorManager;
		fPreferenceStore = preferenceStore;
		fConfiguration = configuration;

		if (fConfiguration != null) {
			fPresentationReconciler = (ATGPresentationReconciler) fConfiguration
					.getPresentationReconciler(sourceViewer);
		} else {
			fPresentationReconciler = null;
		}

		if (SemanticHighlightings.isEnabled(fPreferenceStore)) {
			enable();
		}
		fPreferenceStore.addPropertyChangeListener(pcListener);
	}

	private void propertyChanged(PropertyChangeEvent event) {

		if (fPreferenceStore == null) {
			return;
		}

		if (SemanticHighlightings.affectsEnablement(fPreferenceStore, event)) {
			if (SemanticHighlightings.isEnabled(fPreferenceStore)) {
				enable();
			} else {
				disable();
			}
		}

		if (!SemanticHighlightings.isEnabled(fPreferenceStore)) {
			return;
		}

		boolean refreshNeeded = false;

		for (int i = 0; i < fSemanticHighlightings.length; i++) {
			SemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];

			String colorKey = SemanticHighlightings
					.getColorPreferenceKey(semanticHighlighting);
			if (colorKey.equals(event.getProperty())) {
				adaptToTextForegroundChange(fHighlightings[i], event);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String boldKey = SemanticHighlightings
					.getBoldPreferenceKey(semanticHighlighting);
			if (boldKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, SWT.BOLD);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String italicKey = SemanticHighlightings
					.getItalicPreferenceKey(semanticHighlighting);
			if (italicKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, SWT.ITALIC);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String strikethroughKey = SemanticHighlightings
					.getStrikethroughPreferenceKey(semanticHighlighting);
			if (strikethroughKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event,
						TextAttribute.STRIKETHROUGH);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String underlineKey = SemanticHighlightings
					.getUnderlinePreferenceKey(semanticHighlighting);
			if (underlineKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event,
						TextAttribute.UNDERLINE);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String enabledKey = SemanticHighlightings
					.getEnabledPreferenceKey(semanticHighlighting);
			if (enabledKey.equals(event.getProperty())) {
				adaptToEnablementChange(fHighlightings[i], event);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}
		}

		if (refreshNeeded && fReconciler != null) {
			fReconciler.refresh();
		}

	}

	/**
	 * unbinds a color from the key
	 * 
	 * @param colorKey
	 *            the colorPreferenceKey
	 */
	private void removeColor(String colorKey) {
		if (fColorManager instanceof IColorManagerExtension) {
			((IColorManagerExtension) fColorManager).unbindColor(colorKey);
		}
	}

	/**
	 * uninstalls the manager
	 */
	public void uninstall() {
		fEditor = null;
		fSourceViewer = null;
		fColorManager = null;
		fPreferenceStore.removePropertyChangeListener(pcListener);
		fPreferenceStore = null;
		fConfiguration = null;
		if (fPresentationReconciler != null) {
			fPresentationReconciler = null;
		}

	}

}
