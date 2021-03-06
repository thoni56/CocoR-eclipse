/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package at.ssw.coco.lib.model.scanners;

/**
 * A BufferedDocumentScanner.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */


import at.ssw.coco.lib.model.positions.CocoAssert;


/**
 * A buffered document scanner. The buffer always contains a section
 * of a fixed size of the document to be scanned.
 */

public final class BufferedDocumentScanner{

	/**
	 * The value returned when this scanner has read EOF.
	 */
	public static final int EOF= -1;
	
	/** The document being scanned. */
	private String fDocument;
	/** The offset of the document range to scan. */
	private int fRangeOffset;
	/** The length of the document range to scan. */
	private int fRangeLength;
	/** The delimiters of the document. */
	private char[][] fDelimiters;

	/** The buffer. */
	private final char[] fBuffer;
	/** The offset of the buffer within the document. */
	private int fBufferOffset;
	/** The valid length of the buffer for access. */
	private int fBufferLength;
	/** The offset of the scanner within the buffer. */
	private int fOffset;


	/**
	 * Creates a new buffered document scanner.
	 * The buffer size is set to the given number of characters.
	 *
	 * @param size the buffer size
	 */
	public BufferedDocumentScanner(int size) {
		CocoAssert.isTrue(size >= 1);
		fBuffer= new char[size];
	}

	/**
	 * Fills the buffer with the contents of the document starting at the given offset.
	 *
	 * @param offset the document offset at which the buffer starts
	 */
	private final void updateBuffer(int offset) {

		fBufferOffset= offset;

		if (fBufferOffset + fBuffer.length > fRangeOffset + fRangeLength)
			fBufferLength= fRangeLength - (fBufferOffset - fRangeOffset);
		else
			fBufferLength= fBuffer.length;

		try {
			final String content= fDocument.substring(fBufferOffset, fBufferOffset+fBufferLength);
			content.getChars(0, fBufferLength, fBuffer, 0);
		} catch (Exception e) {
		}
	}

	/**
	 * Configures the scanner by providing access to the document range over which to scan.
	 *
	 * @param document the document to scan
	 * @param offset the offset of the document range to scan
	 * @param length the length of the document range to scan
	 */
	public final void setRange(String document, String[] delimiters, int offset, int length) {

		fDocument= document;
		fRangeOffset= offset;
		fRangeLength= length;
		fDelimiters= new char[delimiters.length][];
		for (int i= 0; i < delimiters.length; i++)
			fDelimiters[i]= delimiters[i].toCharArray();

		updateBuffer(offset);
		fOffset= 0;
	}

	/*
	 * @see ICharacterScanner#read()
	 */
	public final int read() {

		if (fOffset == fBufferLength) {
			int end= fBufferOffset + fBufferLength;
			if (end == fDocument.length() || end == fRangeOffset + fRangeLength)
				return EOF;
			else {
				updateBuffer(fBufferOffset + fBufferLength);
				fOffset= 0;
			}
		}

		try {
			return fBuffer[fOffset++];
		} catch (ArrayIndexOutOfBoundsException ex) {
			StringBuffer buf= new StringBuffer();
			buf.append("Detailed state of 'BufferedDocumentScanner:'"); //$NON-NLS-1$
			buf.append("\n\tfOffset= "); //$NON-NLS-1$
			buf.append(fOffset);
			buf.append("\n\tfBufferOffset= "); //$NON-NLS-1$
			buf.append(fBufferOffset);
			buf.append("\n\tfBufferLength= "); //$NON-NLS-1$
			buf.append(fBufferLength);
			buf.append("\n\tfRangeOffset= "); //$NON-NLS-1$
			buf.append(fRangeOffset);
			buf.append("\n\tfRangeLength= "); //$NON-NLS-1$
			buf.append(fRangeLength);
			//JavaPlugin.logErrorMessage(buf.toString());
			throw ex;
		}
	}

	/*
	 * @see ICharacterScanner#unread
	 */
	public final void unread() {

		if (fOffset == 0) {
			if (fBufferOffset == fRangeOffset) {
				// error: BOF
			} else {
				updateBuffer(fBufferOffset - fBuffer.length);
				fOffset= fBuffer.length - 1;
			}
		} else {
			--fOffset;
		}
	}


	/*
	 * @see ICharacterScanner#getLegalLineDelimiters()
	 */
	public final char[][] getLegalLineDelimiters() {
		return fDelimiters;
	}
}
