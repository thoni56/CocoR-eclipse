/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
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

package at.ssw.coco.ide.features.views.contentoutline;

import java.util.Arrays;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;

import at.ssw.coco.ide.style.ImageManager;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;

/**
 * Implements the label provider for the <code>ATGContentOutlinePage</code>.
 * 
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ATGLabelProvider extends StyledCellLabelProvider implements
		ILabelProvider {
	/** The image manager */
	private ImageManager imgManager;

	/** The Constructor */
	public ATGLabelProvider() {
		imgManager = new ImageManager();
	}

	@Override
	public void dispose() {
		super.dispose();
		imgManager.dispose();
	}

	/**
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object o) {
		if (!(o instanceof ATGSegment)) {
			return null;
		}
		ATGSegment seg = (ATGSegment) o;
		String icon;

		switch (seg.getType()) {
			case SECTION_COMPILER:
				icon = ImageManager.ICON_COMPILER;
				break;
			case SECTION_PRODUCTIONS:
				icon = ImageManager.ICON_PRODUCTIONS;
				break;
			case SECTION_SCANNER:
				icon = ImageManager.ICON_SETTINGS;
				break;
			default:
				switch (seg.getLevel()) {
					case 0:
					case 1:
					case 2:
						icon = ImageManager.ICON_SUBITEM;
						break;
					default:
						icon = ImageManager.ICON_SUBSUBITEM;
						break;
				}
		}

		return imgManager.getImage(icon);
	}

	public StyledString getStyledText(Object element) {
		if (!(element instanceof ATGSegment)) {
			return new StyledString(getText(element));
		}
		ATGSegment seg = (ATGSegment) element;
		StyledString sstr = new StyledString(seg.getName());
		sstr.append(seg.getAttributes(), StyledString.DECORATIONS_STYLER);
		return sstr;
	}

	/**
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (!(element instanceof ATGSegment)) {
			return element.toString();
		}
		ATGSegment seg = (ATGSegment) element;
		return seg.getName();
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();

		StyledString styledString = getStyledText(element);
		String newText = styledString.toString();

		StyleRange[] oldStyleRanges = cell.getStyleRanges();
		StyleRange[] newStyleRanges = isOwnerDrawEnabled() ? styledString
				.getStyleRanges() : null;

		if (!Arrays.equals(oldStyleRanges, newStyleRanges)) {
			cell.setStyleRanges(newStyleRanges);
			if (cell.getText().equals(newText)) {
				// make sure there will be a refresh from a change
				cell.setText(""); //$NON-NLS-1$
			}
		}

		cell.setText(newText);
		cell.setImage(getImage(element));

		// no super call required. changes on item will trigger the refresh.
	}
}
