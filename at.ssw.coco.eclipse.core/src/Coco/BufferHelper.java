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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ssw.coco.core.Mapping.Position;

final class BufferHelper {
	/** Line start positions */
	private final List<Integer> linePositions = new ArrayList<Integer>();

	/**
	 * Buffer helper, scans the buffer for line starts
	 * @param buffer Buffer of the atg file
	 */
	public BufferHelper(Buffer buffer) {
		int oldPos = buffer.getPos();

		buffer.setPos(0);

		int ch;
		linePositions.add(buffer.getPos());
		while ((ch = buffer.Read()) != Buffer.EOF)
		{
			if (ch == '\r' && buffer.Peek() != '\n') ch = '\n';
			if (ch == '\n') {
				linePositions.add(buffer.getPos());
			}
		}

		buffer.setPos(oldPos);
	}

	/**
	 * Convert file position to line and column.
	 *
	 * @param pos Absolute file position.
	 * @return Line and column numbers.
	 */
	public Position getLinePosition(int pos)
	{
		int index = Collections.binarySearch(linePositions, pos);
		if (index < 0) {
			index = (-index-1)-1;
			if (index < 0) index = 0;
		}

		return new Position(
				index + 1,
				pos - linePositions.get(index),
				pos);
	}
}
