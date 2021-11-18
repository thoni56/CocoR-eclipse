/*******************************************************************************
 * Copyright (C) 2009 Andreas Woess
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
package at.ssw.coco.lib.model.atgmodel.impl;

import java.io.InputStream;

/**
 * Extends the Coco/R Scanner so that it can be used with <code>IDocument</code>s.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public final class DocumentScanner extends Scanner {
	private static final InputStream dummyStream = new InputStream() {
		@Override
		public final int read() {
			return 0;
		}
	};

	public DocumentScanner(String document) {
		super(dummyStream);
		super.buffer = new CharArrayBuffer(document.toCharArray());
		super.Init();
	}

	private static final class CharArrayBuffer extends Buffer {
		public static final int EOF = Buffer.EOF;
		private final char[] buf;
		private final int bufLen;
		private int bufPos;

		public CharArrayBuffer(char[] buf) {
			super(dummyStream);
			this.buf = buf;
			bufPos = 0;
			bufLen = buf.length;
		}

		@Override
		protected void finalize() throws Throwable {
		}

		@Override
		public int Read() {
			if (bufPos < bufLen) {
				return buf[bufPos++];
			} else {
				return EOF;
			}
		}

		@Override
		public int Peek() {
			int curPos = bufPos;
			int ch = Read();
			setPos(curPos);
			return ch;
		}

		@Override
		public String GetString(int beg, int end) {
			if (beg < 0 || beg > bufLen) {
				throw new FatalError("buffer out of bounds access, position: " + beg);
			}
			if (end > bufLen)
				end = bufLen;
			return new String(buf, beg, end - beg);
		}

		@Override
		public int getPos() {
			return bufPos;
		}

		@Override
		public void setPos(int value) {
			if (value < 0 || value > bufLen) {
				throw new FatalError("buffer out of bounds access, position: " + value);
			}
			bufPos = value;
		}
	}
}
