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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * An abstract base class for content outline pages with a filter.
 * <p>
 * Internally, each content outline page consists of a standard tree viewer;
 * selections made in the tree viewer are reported as selection change events by
 * the page (which is a selection provider). The tree viewer is not created
 * until <code>createPage</code> is called; consequently, subclasses must extend
 * <code>createControl</code> to configure the tree viewer with a proper content
 * provider, label provider, and input element.
 * </p>
 * 
 * @see org.eclipse.ui.views.contentoutline.ContentOutlinePage
 * 
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public abstract class FilteredContentOutlinePage extends Page implements
		IContentOutlinePage, ISelectionChangedListener {
	/**
	 * Extend {@link FilteredTree} to automatically expand its tree when the
	 * filter has been cleared.
	 * 
	 * @author Andreas Woess <andwoe@users.sf.net>
	 */
	public static class FilteredTreeWithAutoExpand extends FilteredTree {
		@SuppressWarnings("deprecation")
		public FilteredTreeWithAutoExpand(Composite parent, int treeStyle,
				PatternFilter filter) {
			super(parent, treeStyle, filter);
		}

		/**
		 * Creates a workbench job that will refresh the tree based on the
		 * current filter text.
		 * 
		 * @return a workbench job that can be scheduled to refresh the tree
		 * @since eclipse 3.4
		 */
		@Override
		protected WorkbenchJob doCreateRefreshJob() {
			final WorkbenchJob superJob = super.doCreateRefreshJob();

			return new WorkbenchJob(superJob.getName()) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					IStatus status = superJob.runInUIThread(monitor); // do
																		// filtering

					if (status.isOK()) {
						// expand the tree if filter has been cleared
						String text = getFilterString();
						if (text != null && text.length() == 0) {
							treeViewer.expandToLevel(treeViewer
									.getAutoExpandLevel());
						}
					}
					return status;
				}
			};
		}
	}

	private ListenerList selectionChangedListeners = new ListenerList();

	private FilteredTree filteredTree;

	/**
	 * Create a new content outline page.
	 */
	protected FilteredContentOutlinePage() {
		super();
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	/**
	 * The implementation of this <code>IContentOutlinePage</code> method
	 * creates a filtered tree viewer. Subclasses must extend this method
	 * configure the tree viewer with a proper content provider, label provider,
	 * and input element.
	 * 
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		filteredTree = new FilteredTreeWithAutoExpand(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter());
		getTreeViewer().addSelectionChangedListener(this);
	}

	/**
	 * Fires a selection changed event.
	 * 
	 * @param selection
	 *            the new selection
	 */
	protected void fireSelectionChanged(ISelection selection) {
		// create an event
		final SelectionChangedEvent event = new SelectionChangedEvent(this,
				selection);

		// fire the event
		Object[] listeners = selectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					l.selectionChanged(event);
				}
			});
		}
	}

	/**
	 * @see org.eclipse.ui.part.IPage#getControl()
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	@Override
	public Control getControl() {
		// return (getTreeViewer() != null) ? getTreeViewer().getControl() :
		// null;
		return filteredTree;
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		if (getTreeViewer() == null) {
			return StructuredSelection.EMPTY;
		}
		return getTreeViewer().getSelection();
	}

	/**
	 * Returns this page's tree viewer.
	 * 
	 * @return this page's tree viewer, or <code>null</code> if
	 *         <code>createControl</code> has not been called yet
	 */
	protected TreeViewer getTreeViewer() {
		return (filteredTree != null) ? filteredTree.getViewer() : null;
	}

	/**
	 * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
	 */
	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		pageSite.setSelectionProvider(this);
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	/**
	 * Gives notification that the tree selection has changed.
	 * 
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		fireSelectionChanged(event.getSelection());
	}

	/**
	 * Sets focus to a part in the page.
	 */
	@Override
	public void setFocus() {
		getTreeViewer().getControl().setFocus();
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(ISelection)
	 */
	public void setSelection(ISelection selection) {
		if (getTreeViewer() != null) {
			getTreeViewer().setSelection(selection);
		}
	}
}
