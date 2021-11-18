/**
 * Copyright (C) 2009 Andreas Woess, University of Linz
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package Coco;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;

import at.ssw.coco.core.Mapping;

/**
 * Extend the Coco/R class <code>ParserGen</code> to get a mapping between the
 * generated parser file and corresponding sections in the ATG file.
 * 
 * @author Andreas Woess <andwoe@users.sf.net>
 */
final class MappingParserGen extends ParserGen {
	protected static final class MappingPrintWriter extends PrintWriter {
		private static final String lineSeparator = System.getProperty(
				"line.separator", "\n");

		private int line = 1; // [1..
		private int column = 0; // [0..
		private int offset = 0; // [0..
		private boolean lastWasCR = false;

		public MappingPrintWriter(String fileName) throws FileNotFoundException {
			super(fileName);
		}

		public MappingPrintWriter(Writer out) {
			super(out);
		}

		public int getColumn() {
			return column;
		}

		public int getLine() {
			return line;
		}

		public int getOffset() {
			return offset;
		}

		public Mapping.Position getPosition() {
			return new Mapping.Position(line, column, offset);
		}

		@Override
		public void println() {
			super.println();
			line++;
			column = 0;
			offset += lineSeparator.length();
			lastWasCR = false;
		}

		@Override
		public void write(final int c) {
			super.write(c);

			if (c == '\n' || c == '\r') {
				if (!(c == '\n' && lastWasCR)) {
					line++;
				}
				column = 0;
			} else {
				column++;
			}
			offset++;
			lastWasCR = (c == '\r');
		}

		@Override
		public void write(final String s) {
			super.write(s);

			if (s.length() == 0) {
				return;
			}

			for (int i = 0; i < s.length(); i++) {
				final int ch = s.charAt(i);
				if (ch == '\r') {
					if (i + 1 < s.length() && s.charAt(i + 1) == '\n') {
						i++;
					}
					line++;
					column = 0;
				} else if (ch == '\n') {
					line++;
					column = 0;
				} else {
					column++;
				}
			}
			offset += s.length();
			lastWasCR = s.charAt(s.length() - 1) == '\r';
		}
	}

	private final BufferHelper bufferHelper;

	private final Mapping mapping;

	public MappingParserGen(Parser parser) {
		this(parser, new Mapping());
	}

	/**
	 * @see ParserGen#ParserGen(Parser)
	 */
	public MappingParserGen(Parser parser, Mapping mapping) {
		super(parser);
		bufferHelper = new BufferHelper(parser.scanner.buffer); // Coco-Plugin
		this.mapping = mapping;
	}

	/**
	 * @see ParserGen#CopySourcePart(Position, int)
	 */
	@Override
	void CopySourcePart(Position pos, int indent) {
		// Copy text described by pos from atg to gen
		int ch, i;
		if (pos != null) {
			buffer.setPos(pos.beg);
			ch = buffer.Read();
			Indent(indent);
			done: while (buffer.getPos() <= pos.end) {
				while (ch == CR || ch == LF) { // eol is either CR or CRLF or LF
					gen.println();
					Indent(indent);
					if (ch == CR) {
						ch = buffer.Read();
					} // skip CR
					if (ch == LF) {
						ch = buffer.Read();
					} // skip LF
					for (i = 1; i <= pos.col && ch <= ' '; i++) {
						// skip blanks at beginning of line
						ch = buffer.Read();
					}
					if (i <= pos.col) {
						pos.col = i - 1; // heading TABs => not enough blanks
					}
					if (buffer.getPos() > pos.end) {
						break done;
					}
				}
				mapping.add(((MappingPrintWriter) gen).getPosition(),
						bufferHelper.getLinePosition(buffer.getPos() - 1), 1); // Coco-Plugin
				gen.print((char) ch);
				ch = buffer.Read();
			}
			if (indent > 0) {
				gen.println();
			}
		}
	}

	public Mapping getMapping() {
		return mapping;
	}

	/**
	 * @see ParserGen#OpenGen(boolean)
	 */
	@Override
	protected void OnWriteParserInitializationDone() {
		super.OnWriteParserInitializationDone();
		gen = new MappingPrintWriter(gen); // Coco-Plugin
	}
}
