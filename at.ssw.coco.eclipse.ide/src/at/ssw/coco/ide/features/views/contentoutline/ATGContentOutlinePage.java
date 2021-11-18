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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;

import at.ssw.coco.ide.Activator;
import at.ssw.coco.ide.IdeUtilities;
import at.ssw.coco.ide.PreferenceConstants;
import at.ssw.coco.ide.editor.ATGEditor;
import at.ssw.coco.ide.style.ImageManager;
import at.ssw.coco.lib.model.atgmodel.ATGModelListener;
import at.ssw.coco.lib.model.atgmodel.ATGModelProvider;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;
import at.ssw.coco.lib.model.positions.CocoPosition;

/**
 * Implements a ATG specific content outline page.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ATGContentOutlinePage extends FilteredContentOutlinePage {
	private final ATGModelListener fModelListener = new ATGModelListener() {
		public void modelChanged() {
			setTreeViewerInput();
		}
	};

	private class LexicalSortingAction extends Action {
		private final ViewerComparator comparator = new ViewerComparator();
		private static final String PREFERENCE_KEY = PreferenceConstants.OUTLINE_LEXICAL_SORTING_CHECKED;

		public LexicalSortingAction() {
			setText("Sort");
			setImageDescriptor(IdeUtilities.getImageDescriptor(
					ImageManager.BASE_FOLDER + ImageManager.ICON_LEXICAL_SORT));
			setToolTipText("Sort");
			setDescription("Sort");

			boolean checked = (Activator.getDefault() == null) ? false
					: Activator.getDefault().getPreferenceStore().getBoolean(PREFERENCE_KEY);

			valueChanged(checked, false);
		}

		@Override
		public void run() {
			valueChanged(isChecked(), true);
		}

		private void valueChanged(final boolean on, boolean store) {
			setChecked(on);
			BusyIndicator.showWhile(getTreeViewer().getControl().getDisplay(),
					new Runnable() {
						public void run() {
							if (on) {
								getTreeViewer().setComparator(comparator);
							} else {
								getTreeViewer().setComparator(null);
							}
						}
					});

			if (store)
				if (Activator.getDefault() != null)
					Activator.getDefault().getPreferenceStore().setValue(PREFERENCE_KEY, on);
		}
	}

	/** The ATG model provider */
	private ATGModelProvider fModelProvider;

	/** The text editor */
	private ATGEditor fEditor;

	/**
	 * The Constructor.
	 *
	 * @param modelProvider The ATG model provider
	 */
	public ATGContentOutlinePage(ATGEditor editor, ATGModelProvider modelProvider) {
		Assert.isNotNull(editor);
		Assert.isNotNull(modelProvider);
		fEditor = editor;
		fModelProvider = modelProvider;

		fModelProvider.addModelListener(fModelListener);
	}

	/**
	 * @see ATGModelListener#modelChanged()
	 */
	public void modelChanged() {
		setTreeViewerInput();
	}

	/**
	 * Sets the input for the JFace <code>TreeViewer</code>, the ATG model.
	 */
	private void setTreeViewerInput() {
		getTreeViewer().setInput(fModelProvider.getATGModel());
	}

	/**
	 * @see org.eclipse.ui.views.contentoutline.ContentOutlinePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		IToolBarManager toolBarManager = getSite().getActionBars().getToolBarManager();
		if (toolBarManager != null) {
			toolBarManager.add(new LexicalSortingAction());
		}

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new ATGContentProviderAdaptor());
		viewer.setLabelProvider(new ATGLabelProvider());
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		viewer.addSelectionChangedListener(this);

		setTreeViewerInput();
	}

	/**
	 * @see org.eclipse.ui.views.contentoutline.ContentOutlinePage#selectionChanged(SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);

		IStructuredSelection selection = (IStructuredSelection)event.getSelection();
		if (selection.isEmpty()) {
			fEditor.resetHighlightRange();
		} else {
			Object element = selection.getFirstElement();
			Assert.isTrue(element instanceof ATGSegment);
			ATGSegment segment = (ATGSegment)element;
			CocoPosition pos = segment.getPosition();
			try {
				fEditor.resetHighlightRange(); // force the cursor being moved
				fEditor.setHighlightRange(pos.getOffset(), pos.getLength(), true);
			} catch (IllegalArgumentException x) {
				fEditor.resetHighlightRange();
			}
		}
	}

	/**
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (fEditor == null)
			return;

		fEditor.outlinePageClosed();
		fEditor = null;

		fModelProvider.removeModelListener(fModelListener);
		fModelProvider = null;
	}
}
