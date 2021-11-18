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
package at.ssw.coco.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class stores mapping information between an output file and its source file.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public final class Mapping {
	public static class Position implements Comparable<Position> {
		public int line;
		public int column;
		public int offset;

		public Position() {
		}

		public Position(int line, int column, int offset) {
			this.line = line;
			this.column = column;
			this.offset = offset;
		}

		@Override
		public String toString() {
			return line + "," + column + "," + offset;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Position && this.offset == ((Position) other).offset;
		}

		public static Position fromString(String s) throws IllegalArgumentException {
			String[] a = s.split(",");
			if (a.length >= 3) {
				final int line = Integer.parseInt(a[0]);
				final int column = Integer.parseInt(a[1]);
				final int offset = Integer.parseInt(a[2]);
				return new Position(line, column, offset);
			} else {
				throw new IllegalArgumentException();
			}
		}

		public int compareTo(Position other) {
			return other.offset - this.offset;
		}
	}

	private static class Range {
		public Position from = new Position();
		public Position to = new Position();
		public int length;

		public Range(Position from, Position to, int length) {
			super();
			this.from = from;
			this.to = to;
			this.length = length;
		}

		@Override
		public String toString() {
			return from.toString() + ";" + to.toString() + ";" + length;
		}

		public static Range fromString(String s) throws IllegalArgumentException {
			String[] a = s.split(";");
			if (a.length >= 3) {
				return new Range(
						Position.fromString(a[0]),
						Position.fromString(a[1]),
						Integer.parseInt(a[2]));
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	private final List<Range> ranges = new ArrayList<Range>();

	public Mapping() {
	}

	/**
	 * Get the associated position in the source file.
	 *
	 * @param line Line in the output file.
	 * @param column Column in the output file.
	 * @return the Position in the source file.
	 */
	public Position get(final int line, final int column) {
		if (ranges.isEmpty()) return null;

		int index = Collections.binarySearch(ranges, new Object(), new Comparator<Object>() {
			public int compare(Object candidate, Object dummy) {
				int result = ((Range)candidate).from.line - line;
				if (0 == result) {
					result = ((Range)candidate).from.column - column;
				}
				return result;
			}
		});

		if (index >= 0) {
			return ranges.get(index).to;
		} else {
			index = (-index-1) - 1;
			if (index < 0) return null;

			Range entry = ranges.get(index);
			if (entry.from.line == line
					&& entry.from.column <= column
					&& entry.from.column + entry.length > column) {
				int delta = column - entry.from.column;
				return new Position(
						entry.to.line,
						entry.to.column + delta,
						entry.to.offset + delta);
			}
		}

		return null;
	}

	/**
	 * Get the associated position in the source file.
	 *
	 * @param offset Offset in the output file.
	 * @return the Position in the source file.
	 */
	public Position get(final int offset) {
		if (ranges.isEmpty()) return null;

		int index = Collections.binarySearch(ranges, new Object(), new Comparator<Object>() {
			public int compare(Object candidate, Object dummy) {
				return ((Range)candidate).from.offset - offset;
			}
		});

		if (index >= 0) {
			return ranges.get(index).to;
		} else {
			index = (-index-1) - 1;
			if (index < 0) return null;

			Range entry = ranges.get(index);
			if (entry.from.offset <= offset
					&& offset <= entry.from.offset + entry.length) {
				int delta = offset - entry.from.offset;
				return new Position(
						entry.to.line,
						entry.to.column + delta,
						entry.to.offset + delta);
			}
		}

		return null;
	}

	/**
	 * Set a mapping from <code>outputPos</code> to <code>sourcePos</code>.
	 * @param outputPos Starting position in the output file.
	 * @param sourcePos Starting position in the source file.
	 * @param length Length of the mapping.
	 */
	public void add(Position outputPos, Position sourcePos, int length) {
		if (!ranges.isEmpty()) {
			Range cur = ranges.get(ranges.size() - 1);
			if (cur != null
					&& cur.from.line == outputPos.line
					&& cur.from.column + cur.length == outputPos.column
					&& cur.to.line == sourcePos.line
					&& cur.to.column + cur.length == sourcePos.column) {
				cur.length += length;
				return;
			}
		}

		ranges.add(new Range(outputPos, sourcePos, length));
	}

	public void write(String filename, String grammar) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(filename);

		for (Range range : ranges) {
			writer.println(range);
		}
		writer.close();
	}

	public void read(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		String line;
		while ((line = reader.readLine()) != null) {
			try {
				ranges.add(Range.fromString(line));
			} catch (IllegalArgumentException e) {
				break;
			}
		}
		reader.close();
	}

	@Override
	public String toString() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		for (Range range : ranges) {
			writer.println(range);
		}
		writer.close();
		return stringWriter.getBuffer().toString();
	}

	public void readString(String s) {
		BufferedReader reader = new BufferedReader(new StringReader(s));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				try {
					ranges.add(Range.fromString(line));
				} catch (IllegalArgumentException e) {
					break;
				}
			}
		} catch (IOException e) {
			// ignore
		}
	}
}
