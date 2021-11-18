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
package at.ssw.coco.ide.features.views.contentoutline;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.texteditor.ITextEditor;

import at.ssw.coco.ide.model.atgmodel.ATGModelAdaptor;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;
import at.ssw.coco.lib.model.positions.CocoPosition;

/**
 * An information control displaying the content outline for an ATG ("Quick Outline").
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ATGOutlineInformationControl extends AbstractInformationControl implements IInformationControlExtension2 {
	private FilteredTree filteredTree;

	private ITextEditor fEditor;

	public ATGOutlineInformationControl(Shell parentShell, ITextEditor editor) {
		super(parentShell, true);
		Assert.isNotNull(editor);

		this.fEditor = editor;

		create();
	}

	@Override
	protected void createContent(Composite parent) {
		Composite composite = new Composite(parent, SWT.RESIZE);
		composite.setLayout(new GridLayout());
		filteredTree = new FilteredContentOutlinePage.FilteredTreeWithAutoExpand(
				composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter());

		getTreeViewer().setContentProvider(new ATGContentProviderAdaptor());
		getTreeViewer().setLabelProvider(new ATGLabelProvider());
		getTreeViewer().setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

		final Tree tree = getTreeViewer().getTree();
		// mouse over an item should select it
		tree.addMouseMoveListener(new MouseMoveListener() {
			private TreeItem fLastItem = null;

			public void mouseMove(MouseEvent e) {
				if (tree.equals(e.getSource())) {
					Object o = tree.getItem(new Point(e.x, e.y));
					if (o instanceof TreeItem) {
						if (!o.equals(fLastItem)) {
							fLastItem = (TreeItem) o;
							tree.setSelection(new TreeItem[] { fLastItem });
						}
					}
				}
			}
		});

		tree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				gotoSelectedElement();
			}
		});

		// on left-click go to the selected element
		tree.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {

				if (tree.getSelectionCount() < 1)
					return;

				if (e.button != 1)
					return;

				if (tree.equals(e.getSource())) {
					Object o = tree.getItem(new Point(e.x, e.y));
					TreeItem selection = tree.getSelection()[0];
					if (selection.equals(o))
						gotoSelectedElement();
				}
			}
		});
	}

	protected Object getSelectedElement() {
		if (getTreeViewer() == null)
			return null;

		return ((IStructuredSelection) getTreeViewer().getSelection()).getFirstElement();
	}

	private void gotoSelectedElement() {
		Object element = getSelectedElement();
		if (element != null) {
			dispose();
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

	protected TreeViewer getTreeViewer() {
		return (filteredTree != null) ? filteredTree.getViewer() : null;
	}

	public boolean hasContents() {
		return true;
	}

	/**
	 * Sets the input for the JFace <code>TreeViewer</code>, the ATG model.
	 *
	 * @see org.eclipse.jface.text.IInformationControlExtension2#setInput(Object)
	 */
	public void setInput(Object input) {
		if (input instanceof ATGModelAdaptor) {
			getTreeViewer().setInput(input);
		}
	}
}
