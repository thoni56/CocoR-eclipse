/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import at.ssw.coco.ide.IdeUtilities;

/**
 * Implements a image manager which provides the requested images.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 *
 */
public class ImageManager {

	/** The base folder of the managed images */
	public static final String BASE_FOLDER = "icons/contentoutline/";

	/** The icon representing the ATG compiler segment */
	public static final String ICON_COMPILER = "c.gif";

	/** The icon representing the ATG settings segment */
	public static final String ICON_SETTINGS = "s.gif";

	/** The icon representing the ATG prductions segment */
	public static final String ICON_PRODUCTIONS = "p.gif";

	/** The icon representing a further sub segment */
	public static final String ICON_SUBITEM = "dot.gif";

	/** The icon representing a further sub-sub segment */
	public static final String ICON_SUBSUBITEM = "square.gif";

	/** The icon for the sort button */
	public final static String ICON_LEXICAL_SORT = "alphab_sort.gif";

	/** The <code>Map</code> holding the images */
	private final Map<String, Image> imageTable;

	/** The Constructor */
	public ImageManager() {
		imageTable = new HashMap<String, Image>();
	}

	/**
	 * Disposes the image data.
	 */
	public void dispose() {
		for (Image img : imageTable.values()) {
			img.dispose();
		}
		imageTable.clear();
	}

	/**
	 * Returns the requested image.
	 *
	 * @param RL the resource locator
	 * @return the requested image.
	 */
	public Image getImage(String RL) {
		Image img = imageTable.get(RL);
		if (img == null) {
			ImageDescriptor imgDescriptor = IdeUtilities.getImageDescriptor(BASE_FOLDER + RL);
			if (imgDescriptor != null) {
				img = imgDescriptor.createImage();
				if (img != null) {
					imageTable.put(RL, img);
				}
			}
		}
		return img;
	}
}
