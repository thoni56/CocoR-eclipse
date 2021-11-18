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

import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Extends the {@link ProjectionViewer} with an optional outline presenter which
 * is acquired from the SourceViewerConfiguration if the interface
 * OutlinePresenterProvider is implemented.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ProjectionViewerWithOutline extends ProjectionViewer {
	// @see import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
	public static final String ACTIONID_SHOW_OUTLINE = "at.ssw.coco.eclipse.ide.commands.show.outline";
	// @see org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer
	public static final int OPCODE_SHOW_OUTLINE = 51;

	private IInformationPresenter outlinePresenter;

	public interface OutlinePresenterProvider {
		public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer);
	}

	public ProjectionViewerWithOutline(Composite parent, IVerticalRuler ruler,
			IOverviewRuler overviewRuler, boolean showsAnnotationOverview, int styles) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
	}

	@Override
	public void configure(SourceViewerConfiguration configuration) {
		super.configure(configuration);
		if (configuration instanceof OutlinePresenterProvider) {
			outlinePresenter = ((OutlinePresenterProvider) configuration).getOutlinePresenter(this);
			if (outlinePresenter != null) {
				outlinePresenter.install(this);
			}
		}
	}

	@Override
	public void unconfigure() {
		if (outlinePresenter != null) {
			outlinePresenter.uninstall();
			outlinePresenter = null;
		}
		super.unconfigure();
	}

	public boolean canDoOperation(int operation) {
		if (OPCODE_SHOW_OUTLINE == operation) {
			return (outlinePresenter != null);
		}
		return super.canDoOperation(operation);
	}

	public void doOperation(int operation) {
		if (getTextWidget() != null) {
			switch (operation) {
			case OPCODE_SHOW_OUTLINE:
				if (outlinePresenter != null)
					outlinePresenter.showInformation();
				return;
			}
		}
		super.doOperation(operation);
	}
}

