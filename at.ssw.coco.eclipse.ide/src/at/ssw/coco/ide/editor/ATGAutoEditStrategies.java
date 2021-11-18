package at.ssw.coco.ide.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

import at.ssw.coco.lib.model.scanners.ATGPartitions;

public class ATGAutoEditStrategies {
	/**
	 * This strategy always copies the indentation of the previous line.
	 * If the previous line started the inline code block, indent the
	 * code to the column after the start tag.
	 * Assumes an open inline code block at the line break position.
	 * <p>
	 * The following example shows where the caret would be placed:
	 * <pre>
	 *   "COMPILER"                    (. beg = t.pos; .)
	 *   ident                         (.&para;
	 *                                   ^
	 * </pre>
	 * </p>
	 */
	public static final class InlineCodeIndentLineStrategy implements IAutoEditStrategy {
		/**
		 * Returns the first offset greater than <code>offset</code> and smaller than
		 * <code>end</code> whose character is not a space or tab character. If no such
		 * offset is found, <code>end</code> is returned.
		 *
		 * @param document the document to search in
		 * @param offset the offset at which searching start
		 * @param end the offset at which searching stops
		 * @return the offset in the specified range whose character is not a space or tab
		 * @exception BadLocationException if position is an invalid range in the given document
		 */
		protected int findEndOfWhiteSpace(IDocument document, int offset, int end) throws BadLocationException {
			for (int i = end - 1; i >= offset; i--) {
				char c = document.getChar(i);
				if (c != ' ' && c != '\t') {
					ITypedRegion partition = document.getPartition(i);
					if (ATGPartitions.INLINE_CODE_START.equals(partition.getType())) {
						offset = partition.getOffset() + partition.getLength();
						break;
					} else {
						i -= (i - partition.getOffset());
					}
				}
			}
			while (offset < end) {
				char c = document.getChar(offset);
				if (c != ' ' && c != '\t') {
					return offset;
				}
				offset++;
			}
			return end;
		}

		/**
		 * Copies the indentation of the previous line.
		 *
		 * @param d the document to work on
		 * @param c the command to deal with
		 */
		private void autoIndentAfterNewLine(IDocument d, DocumentCommand c) {

			if (c.offset == -1 || d.getLength() == 0)
				return;

			try {
				// find start of line
				int p = (c.offset == d.getLength() ? c.offset  - 1 : c.offset);
				IRegion info = d.getLineInformationOfOffset(p);
				int start = info.getOffset();

				// find white spaces
				int end = findEndOfWhiteSpace(d, start, c.offset);

				StringBuffer buf = new StringBuffer(c.text);
				if (end > start) {
					// append to input
					buf.append(d.get(start, end - start).replaceAll("\\S", " ")); // replace non-space characters with spaces
				}

				c.text = buf.toString();

			} catch (BadLocationException excp) {
				// stop work
			}
		}

		public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
			if (c.length == 0 && c.text != null && TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1)
				autoIndentAfterNewLine(d, c);
		}
	}

	/**
	 * This strategy usually copies the indentation of the previous line.
	 * However, if the previous line ends an inline code block, the indentation
	 * is determined from the line where it started.
	 * Assumes the default partition at the line break position.
	 * <p>
	 * The following example shows where the caret would be placed:
	 * <pre>
	 *   ident                         (. gramName = t.val;
	 *                                    beg = la.pos;
	 *                                  .)&para;
	 *   ^
	 * </pre>
	 * </p>
	 */
	public static final class PostInlineCodeIndentLineStrategy implements IAutoEditStrategy {
		/**
		 * Returns the first offset greater than <code>offset</code> and smaller than
		 * <code>end</code> whose character is not a space or tab character. If no such
		 * offset is found, <code>end</code> is returned.
		 *
		 * The returned offset can be <code>&lt; offset</code> indicating that
		 * the indentation should be copied from a preceding line.
		 *
		 * @param document the document to search in
		 * @param offset the offset at which searching start
		 * @param end the offset at which searching stops
		 * @return the offset in the specified range whose character is not a space or tab
		 * @exception BadLocationException if position is an invalid range in the given document
		 */
		protected int findEndOfWhiteSpace(IDocument document, int offset, int end) throws BadLocationException {
			done: for (int i = end - 1; i >= offset; i--) {
				char c = document.getChar(i);
				if (c != ' ' && c != '\t') {
					ITypedRegion partition = document.getPartition(i);
					if (ATGPartitions.INLINE_CODE_END.equals(partition.getType())) {
						// determine the start of the inline code block
						int inlineCodeStart;
						while ((inlineCodeStart = partition.getOffset() - 1) >= 0) {
							partition = document.getPartition(inlineCodeStart);
							if (ATGPartitions.INLINE_CODE_START.equals(partition.getType())) {
								end = partition.getOffset();
								offset = document.getLineInformationOfOffset(end).getOffset();
								break done;
							}
						}
					} else {
						i -= (i - partition.getOffset());
					}
				}
			}
		while (offset < end) {
			char c = document.getChar(offset);
			if (c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
		}

		/**
		 * Copies the indentation of the previous line.
		 *
		 * @param d the document to work on
		 * @param c the command to deal with
		 */
		private void autoIndentAfterNewLine(IDocument d, DocumentCommand c) {

			if (c.offset == -1 || d.getLength() == 0)
				return;

			try {
				// find start of line
				int p = (c.offset == d.getLength() ? c.offset  - 1 : c.offset);
				IRegion info = d.getLineInformationOfOffset(p);
				int start = info.getOffset();

				// find white spaces
				int end = findEndOfWhiteSpace(d, start, c.offset);

				if (end < start) { // white space is in a preceding line
					start = d.getLineInformationOfOffset(end).getOffset();
				}

				StringBuffer buf = new StringBuffer(c.text);
				if (end > start) {
					// append to input
					buf.append(d.get(start, end - start));
				}

				c.text = buf.toString();

			} catch (BadLocationException excp) {
				// stop work
			}
		}

		public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
			if (c.length == 0 && c.text != null && TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1)
				autoIndentAfterNewLine(d, c);
		}
	}
}
