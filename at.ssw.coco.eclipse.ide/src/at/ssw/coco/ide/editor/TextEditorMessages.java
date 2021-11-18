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
package at.ssw.coco.ide.editor;

import java.util.ResourceBundle;

public class TextEditorMessages {
    private static final String BUNDLE_NAME = TextEditorMessages.class.getName();
    private static ResourceBundle theBundle = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Returns the message bundle which contains constructed keys.
     *
     * @return the message bundle
     */
    public static ResourceBundle getBundle() {
            return theBundle;
    }

	private TextEditorMessages() {
	}
}
