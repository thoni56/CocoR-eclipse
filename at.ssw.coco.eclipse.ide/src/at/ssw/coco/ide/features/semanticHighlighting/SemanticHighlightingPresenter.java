package at.ssw.coco.ide.features.semanticHighlighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;

import at.ssw.coco.ide.IdeUtilities;
import at.ssw.coco.ide.editor.ATGPresentationReconciler;
import at.ssw.coco.ide.features.semanticHighlighting.SemanticHighlightingManager.HighlightedPosition;
import at.ssw.coco.ide.features.semanticHighlighting.SemanticHighlightingManager.HighlightingStyle;

/**
 * managing class for text presentation
 * 
 * @author Markus Koppensteiner <mkoppensteiner@users.sf.net>
 */
public class SemanticHighlightingPresenter {

	private class DocListener implements IDocumentListener {

		public void documentAboutToBeChanged(DocumentEvent evt) {
			setCanceled(true);
		}

		public void documentChanged(DocumentEvent evt) {
			// nothing to implement
		}

	}

	/** fast position updating */
	private class HighlightingPositionUpdater implements IPositionUpdater {

		private final String fCategory;

		public HighlightingPositionUpdater(String category) {
			fCategory = category;
		}

		public void update(DocumentEvent e) {
			int eventOffset = e.getOffset();
			int eventEnd = eventOffset + e.getLength();

			try {

				Position[] positions = e.getDocument().getPositions(fCategory);

				for (int i = 0; i < positions.length; i++) {
					HighlightedPosition position = (HighlightedPosition) positions[i];

					int offset = position.getOffset();
					int end = offset + position.getLength();

					if (offset >= eventEnd) {
						updateWithPrecedingEvent(position, e);
					} else if (end <= eventOffset) {
						// skip
					} else {
						position.delete();
					}

				}

			} catch (BadPositionCategoryException exc) {
				IdeUtilities.logError(exc.getMessage(), exc);
			}
		}

		/**
		 * adapts the follower position
		 * 
		 * @param position
		 *            the HighlightedPosition
		 * @param event
		 *            the occurred Document event
		 */
		private void updateWithPrecedingEvent(HighlightedPosition position,
				DocumentEvent event) {
			String newText = event.getText();
			int eventNewLength = newText != null ? newText.length() : 0;
			int deltaLength = eventNewLength - event.getLength();
			position.setOffset(position.getOffset() + deltaLength);
		}
	}

	private class TIListener implements ITextInputListener {

		public void inputDocumentAboutToBeChanged(IDocument oldInput,
				IDocument newInput) {
			setCanceled(true);
			releaseDocument(oldInput);
			clearPositions();
		}

		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			manageDocument(newInput);
		}

	}

	private class TPListener implements ITextPresentationListener {

		public void applyTextPresentation(TextPresentation tp) {
			applyTextPres(tp);
		}

	}

	/**
	 * @return a category describing String
	 */
	public static String getCategory() {
		return "__semanticHighlighting";
	}

	private TPListener tpListener = new TPListener();

	private TIListener tiListener = new TIListener();

	private DocListener docListener = new DocListener();
	/** the PoitionUpdater */
	private IPositionUpdater fPositionUpdater = new HighlightingPositionUpdater(
			getCategory());
	/** the sourceViewer */
	private SourceViewer fSourceViewer;
	/** the ATGPresentationReconciler */
	private ATGPresentationReconciler fPresentationReconciler;
	/** current positions */
	private List<HighlightedPosition> fPositions = new ArrayList<HighlightedPosition>();
	/** the lock object */
	private Object fPositionLock = new Object();

	/** canceled flag */
	private boolean fIsCanceled = false;

	/**
	 * adds all given positions to current positions
	 * 
	 * @param positions
	 *            the positions to add
	 */
	public void addAllPositions(List<HighlightedPosition> positions) {
		synchronized (fPositionLock) {
			positions.addAll(fPositions);
		}
	}

	/** applies styleRanges */
	private void applyPositions(TextPresentation textPresentation,
			int startIndex, int endIndex) {
		List<StyleRange> ranges = new ArrayList<StyleRange>(endIndex
				- startIndex);
		while (startIndex < endIndex) {
			HighlightedPosition position = (HighlightedPosition) fPositions
					.get(startIndex);
			if (!position.isDeleted()) {
				ranges.add(position.createStyleRange());
			}
			startIndex++;
		}
		StyleRange[] array = new StyleRange[ranges.size()];
		array = (StyleRange[]) ranges.toArray(array);
		textPresentation.replaceStyleRanges(array);
	}

	/** applies the styleRange */
	private int applySinglePosition(TextPresentation textPresentation,
			int startIndex, int endIndex) {
		while (startIndex < endIndex) {
			HighlightedPosition position = (HighlightedPosition) fPositions
					.get(startIndex);
			if (!position.isDeleted()) {
				textPresentation.replaceStyleRange(position.createStyleRange());
			}
			startIndex++;
		}
		return startIndex;
	}

	/** applies styleRanges */
	private void applyTextPres(TextPresentation textPresentation) {
		IRegion region = textPresentation.getExtent();
		int startIndex = computeIndexAtOffset(fPositions, region.getOffset());
		int endIndex = computeIndexAtOffset(fPositions, region.getOffset()
				+ region.getLength());

		if (endIndex - startIndex > 2) {
			applyPositions(textPresentation, startIndex, endIndex);
		} else {
			applySinglePosition(textPresentation, startIndex, endIndex);
		}
	}

	/**
	 * deletes all current highlighted positions
	 */
	private void clearPositions() {
		synchronized (fPositionLock) {
			fPositions.clear();
		}
	}

	/**
	 * 
	 * @param positions
	 * @param offset
	 * @return the next positions index >= the given offset
	 */
	private int computeIndexAtOffset(List<HighlightedPosition> positions,
			int offset) {
		int lower = -1;
		int index = positions.size();
		while (index - lower > 1) {
			int middle = (lower + index) >> 1;
			Position position = (Position) positions.get(middle);
			if (position.getOffset() >= offset) {
				index = middle;
			} else {
				lower = middle;
			}
		}
		return index;
	}

	/**
	 * 
	 * @param positions
	 * @param position
	 * @return if the position is contained in position
	 */
	private boolean contain(List<HighlightedPosition> positions,
			Position position) {
		return indexOf(positions, position) != -1;
	}

	/**
	 * creates a new HighlightedPosition
	 * 
	 * @param offset
	 *            the positions offset
	 * @param length
	 *            the positions length
	 * @param highlightingStyle
	 *            the style
	 * @return a new HighlightedPosition
	 */
	public HighlightedPosition createHighlightedPosition(int offset,
			int length, HighlightingStyle highlightingStyle) {
		return new HighlightedPosition(offset, length, highlightingStyle,
				fPositionUpdater);
	}

	/**
	 * 
	 * @param addedPositions
	 *            the positions to add
	 * @param removedPositions
	 *            the positions to remove
	 * @return a repair description to be applied to the current
	 *         textPresentation
	 */
	public TextPresentation createPresentation(
			TreeSet<HighlightedPosition> addedPositions,
			List<HighlightedPosition> removedPositions) {
		SourceViewer sourceViewer = fSourceViewer;
		ATGPresentationReconciler presentationReconciler = fPresentationReconciler;
		if (sourceViewer == null || presentationReconciler == null) {
			return null;
		}

		if (isCanceled()) {
			return null;
		}

		IDocument document = sourceViewer.getDocument();
		if (document == null) {
			return null;
		}

		int minStart = Integer.MAX_VALUE;
		int maxEnd = Integer.MIN_VALUE;

		for (HighlightedPosition h : removedPositions) {
			int offset = h.getOffset();
			minStart = Math.min(minStart, offset);
			maxEnd = Math.max(maxEnd, offset + h.getLength());
		}

		for (HighlightedPosition h : addedPositions) {
			int offset = h.getOffset();
			minStart = Math.min(minStart, offset);
			maxEnd = Math.max(maxEnd, offset + h.getLength());
		}

		if (minStart < maxEnd) {
			try {
				minStart = Math.min(minStart, document.getLength());
				maxEnd = Math.min(maxEnd, document.getLength());
				synchronized (getLockObject(document)) {
					return presentationReconciler.createRepairDescription(
							new Region(minStart, maxEnd - minStart), document);
				}
			} catch (RuntimeException e) {
				// concurrent modification from UI thread
			}
		}

		return null;
	}

	/**
	 * 
	 * @param textPresentation
	 *            the repair description
	 * @param addedPositions
	 *            the positions to add
	 * @param removedPositions
	 *            the positions to remove
	 * @return a Runnable that repairs the invalid textPresentation
	 */
	public Runnable createUpdateRunnable(
			final TextPresentation textPresentation,
			TreeSet<HighlightedPosition> addedPositions,
			List<HighlightedPosition> removedPositions) {
		if (fSourceViewer == null || textPresentation == null) {
			return null;
		}

		final HighlightedPosition[] added = new SemanticHighlightingManager.HighlightedPosition[addedPositions
				.size()];
		addedPositions.toArray(added);
		final SemanticHighlightingManager.HighlightedPosition[] removed = new SemanticHighlightingManager.HighlightedPosition[removedPositions
				.size()];
		removedPositions.toArray(removed);

		if (isCanceled()) {
			return null;
		}

		Runnable runnable = new Runnable() {
			public void run() {
				updatePresentation(textPresentation, added, removed);
			}
		};
		return runnable;
	}

	/**
	 * 
	 * @param document
	 *            the document, the lock object should correspond to
	 * @return a lock object for the given document
	 */
	private Object getLockObject(IDocument document) {
		if (document instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) document).getLockObject();
			if (lock != null) {
				return lock;
			}
		}
		return document;
	}

	/**
	 * 
	 * @return the lock object
	 */
	public Object getPositionLock() {
		return fPositionLock;
	}

	/**
	 * renews all current positions and adapts the new style
	 * 
	 * @param highlightingStyle
	 *            the style changed
	 */
	public void highlightingStyleChanged(HighlightingStyle highlightingStyle) {
		for (int i = 0; i < fPositions.size(); i++) {
			HighlightedPosition position = (HighlightedPosition) fPositions
					.get(i);
			if (position.getStyle() == highlightingStyle) {
				fSourceViewer.invalidateTextPresentation(position.getOffset(),
						position.getLength());
			}
		}
	}

	/**
	 * 
	 * @param positions
	 * @param position
	 * @return the index of the position in positions
	 */
	private int indexOf(List<HighlightedPosition> positions, Position position) {
		int index = computeIndexAtOffset(positions, position.getOffset());
		int size = positions.size();
		while (index < size) {
			if (positions.get(index) == position) {
				return index;
			}
			index++;
		}
		return -1;
	}

	/**
	 * installs the presenter
	 * 
	 * @param sourceViewer
	 *            the used sourceViewer
	 * @param presentationReconciler
	 *            the used ATGPresentationReconciler
	 */
	public void install(SourceViewer sourceViewer,
			ATGPresentationReconciler presentationReconciler) {
		fSourceViewer = sourceViewer;
		fPresentationReconciler = presentationReconciler;

		fSourceViewer.addTextPresentationListener(tpListener);
		fSourceViewer.addTextInputListener(tiListener);
		manageDocument(fSourceViewer.getDocument());
	}

	/**
	 * invalidates all highlighted positions
	 */
	private void invalidateTextPresentation() {
		for (int i = 0; i < fPositions.size(); i++) {
			Position position = (Position) fPositions.get(i);
			fSourceViewer.invalidateTextPresentation(position.getOffset(),
					position.getLength());
		}
	}

	/**
	 * 
	 * @return the canceled flag
	 */
	public boolean isCanceled() {
		IDocument document = fSourceViewer != null ? fSourceViewer
				.getDocument() : null;
		if (document == null) {
			return fIsCanceled;
		}

		synchronized (getLockObject(document)) {
			return fIsCanceled;
		}
	}

	/**
	 * install document
	 * 
	 * @param document
	 *            to be installed
	 */
	private void manageDocument(IDocument document) {
		if (document != null) {
			document.addPositionCategory(getCategory());
			document.addPositionUpdater(fPositionUpdater);
			document.addDocumentListener(docListener);
		}
	}

	/**
	 * uninstall document
	 * 
	 * @param document
	 *            to be released
	 */
	private void releaseDocument(IDocument document) {
		if (document != null) {
			document.removeDocumentListener(docListener);
			document.removePositionUpdater(fPositionUpdater);
			try {
				document.removePositionCategory(getCategory());
			} catch (BadPositionCategoryException e) {
				IdeUtilities.logError(e.getMessage(), e);
			}
		}
	}

	/**
	 * sets canceled flag
	 * 
	 * @param isCanceled
	 *            new boolean value
	 */
	public void setCanceled(boolean isCanceled) {
		IDocument document = fSourceViewer != null ? fSourceViewer
				.getDocument() : null;
		if (document == null) {
			fIsCanceled = isCanceled;
			return;
		}

		synchronized (getLockObject(document)) {
			fIsCanceled = isCanceled;
		}
	}

	/**
	 * uninstalls the presenter
	 */
	public void uninstall() {
		setCanceled(true);

		if (fSourceViewer != null) {
			fSourceViewer.removeTextPresentationListener(tpListener);
			releaseDocument(fSourceViewer.getDocument());
			invalidateTextPresentation();
			clearPositions();

			fSourceViewer.removeTextInputListener(tiListener);
			fSourceViewer = null;
		}
	}

	/**
	 * updates the positions in the document and stores the new positions sorted
	 * in fPositions
	 */
	private void updatePositions(HighlightedPosition[] addedPositions,
			HighlightedPosition[] removedPositions, IDocument document,
			String positionCategory,
			List<HighlightedPosition> removedPositionsList)
			throws BadPositionCategoryException, BadLocationException {

		List<HighlightedPosition> oldPositions = fPositions;
		int newSize = Math.max(fPositions.size() + addedPositions.length
				- removedPositions.length, 10);

		List<HighlightedPosition> newPositions = new ArrayList<HighlightedPosition>(
				newSize);
		Position position = null;
		Position addedPosition = null;
		int i = 0, oldSize = oldPositions.size(), addedSize = addedPositions.length;

		while (position == null && i < oldSize) {
			position = (Position) oldPositions.get(i++);
			if (position.isDeleted() || contain(removedPositionsList, position)) {
				document.removePosition(positionCategory, position);
				position = null;
			}
		}

		int j = 0;
		while (j < addedSize || addedPosition != null) {

			if (addedPosition == null && j < addedSize) {
				addedPosition = addedPositions[j++];
				document.addPosition(positionCategory, addedPosition);
			}

			if (position != null) {
				if (addedPosition != null) {
					if (position.getOffset() <= addedPosition.getOffset()) {
						newPositions.add((HighlightedPosition) position);
						position = null;
					} else {
						newPositions.add((HighlightedPosition) addedPosition);
						addedPosition = null;
					}
				} else {
					newPositions.add((HighlightedPosition) position);
					position = null;
				}
			} else if (addedPosition != null) {
				newPositions.add((HighlightedPosition) addedPosition);
				addedPosition = null;
			}
		}

		fPositions = newPositions;
	}

	/**
	 * repairs the invalid textPresentation
	 * 
	 * @param textPresentation
	 *            the repair description
	 * @param addedPositions
	 *            the positions to add
	 * @param removedPositions
	 *            the positions to remove
	 */
	public void updatePresentation(TextPresentation textPresentation,
			HighlightedPosition[] addedPositions,
			HighlightedPosition[] removedPositions) {
		if (fSourceViewer == null) {
			return;
		}

		if (isCanceled()) {
			return;
		}

		IDocument document = fSourceViewer.getDocument();
		if (document == null) {
			return;
		}

		String positionCategory = getCategory();

		List<HighlightedPosition> removedPositionsList = Arrays
				.asList(removedPositions);

		try {
			synchronized (fPositionLock) {
				updatePositions(addedPositions, removedPositions, document,
						positionCategory, removedPositionsList);
			}
		} catch (BadPositionCategoryException e) {
			IdeUtilities.logError(e.getMessage(), e);
		} catch (BadLocationException e) {
			IdeUtilities.logError(e.getMessage(), e);
		}

		if (textPresentation != null) {
			fSourceViewer.changeTextPresentation(textPresentation, false);
		} else {
			fSourceViewer.invalidateTextPresentation();
		}
	}

}
