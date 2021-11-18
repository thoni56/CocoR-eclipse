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

package at.ssw.coco.builder.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

import at.ssw.coco.builder.CocoNature;
import at.ssw.coco.core.CoreUtilities;

/**
 * Implements an action to remove the Coco/R Extensions.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 */
public class RemoveCocoAction extends Action implements IActionDelegate {
	private static final String DIALOG_TITLE = "Coco/R";

	/** The selected item */
	private ISelection fSelection;

	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		if (!(fSelection instanceof IStructuredSelection)) {
			return;
		}

		try {
			Iterator iter = ((IStructuredSelection)fSelection).iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable)obj).getAdapter(IProject.class);
					if (project != null) {
						CocoNature.removeFrom(project); // fails if project not open

						// remove exclusion filter
						IJavaProject javaproject = JavaCore.create(project);
						if (javaproject != null && javaproject.exists()) {
							String exclusionfilter = javaproject.getOption(
									JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, false);

							if (exclusionfilter != null) {
								boolean set = false;
								if (exclusionfilter.contains("*.atg")) {
									exclusionfilter = exclusionfilter.replaceFirst(",?\\*\\.atg", "");
									set = true;
								}
								if (exclusionfilter.contains("*.frame")) {
									exclusionfilter = exclusionfilter.replaceFirst(",?\\*\\.frame", "");
									set = true;
								}

								if (set) {
									String globalexclusionfilter = JavaCore.getOption(
											JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER);
									if (exclusionfilter.equals(globalexclusionfilter)) {
										exclusionfilter = null; // remove the option from the project preferences
									}

									javaproject.setOption(
											JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, exclusionfilter);
								}
							}
						}
					}
				}
			}

			MessageDialog.openInformation(getShell(), DIALOG_TITLE, "Coco/R extensions removed from project.\n"
					+ CoreUtilities.ATG_EXTENSION + " files are no longer compiled to parsers and scanners.");
		} catch (CoreException ex) {
			ErrorDialog.openError(getShell(), DIALOG_TITLE, "Unable to remove Coco/R extension", ex.getStatus());
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}
}
