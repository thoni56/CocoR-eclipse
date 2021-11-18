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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

/**
 * Manages the installation and de-installation of global actions for the ATG text editor.
 * <p>
 * If instantiated and used as-is, this contributor connects the following global actions:
 * <ul>
 * 		<li>Quick Outline</li>
 * </ul>
 * @see org.eclipse.ui.editors.text.TextEditorActionContributor
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ATGTextEditorActionContributor extends TextEditorActionContributor {
	private RetargetTextEditorAction fShowOutline;

	public ATGTextEditorActionContributor() {
		fShowOutline = new RetargetTextEditorAction(TextEditorMessages.getBundle(), "ShowOutline.");
		fShowOutline.setActionDefinitionId(ProjectionViewerWithOutline.ACTIONID_SHOW_OUTLINE);
	}

	@Override
	public void contributeToMenu(IMenuManager menu) {
		super.contributeToMenu(menu);

		IMenuManager navigateMenu = menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fShowOutline);
		}
	}

	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		ITextEditor textEditor = null;
		if (part instanceof ITextEditor)
			textEditor = (ITextEditor)part;

		fShowOutline.setAction(getAction(textEditor, ProjectionViewerWithOutline.ACTIONID_SHOW_OUTLINE));
	}
}
