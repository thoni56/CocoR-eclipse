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

package at.ssw.coco.ide.wizard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import at.ssw.coco.builder.Activator;
import at.ssw.coco.builder.BuilderUtilities;
import at.ssw.coco.builder.CocoNature;
import at.ssw.coco.core.CoreUtilities;
import at.ssw.coco.ide.IdeUtilities;

/**
 * Implements a wizard to extend a <code>JavaProject</code> by the Coco/R extensions.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 *
 */
public class CocoWizard extends BasicNewResourceWizard {

	/** The wizard title */
	private static final String WIZARD_TITLE = "New Coco/R Grammar";

	/**
	 * Copies the needed files to generate a Coco/R Parser & Scanner.
	 *
	 * @param destination The Destination Path.
	 * @param force Overwrite existing files.
	 * @throws CoreException
	 */
	public static void copy(IContainer destination, boolean force) throws CoreException {
		for (String filename : CoreUtilities.ALL_TEMPLATES) {
			InputStream fin;
			try {
				fin = CoreUtilities.getTemplate(filename).openStream();
			} catch (IOException ex) {
				BuilderUtilities.logError("Could not open file", ex);
				continue;
			}
			IFile destFile = destination.getFile(new Path(filename));
			if (!destFile.exists()) {
				destFile.create(fin, true, null);
			} else {
				if (force) {
					destFile.setContents(fin, IResource.FORCE, null);
				} else {
					String msg = "File '" + filename + "' already exists";
					throw new AccessControlException(msg);
				}
			}
			try {
				fin.close();
			} catch (IOException ex) {
				BuilderUtilities.logError("Could not close file", ex);
			}
		}
	}

	/**
	 * Throws a <code>CoreException</code> with an associated status.
	 *
	 * @param message the exception message.
	 * @param severity the severity ID.
	 * @throws CoreException
	 */
	public static void throwCoreException(String message, int severity) throws CoreException {
		IStatus status = new Status(severity, Activator.PLUGIN_ID, IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/** The wizards page layout. */
	private CocoWizardPage fPage;

	/** The current selection */
	private ISelection fSelection;

	/** The template file to be opened. */
	IFile fTemplate;

	/** The Constructor */
	public CocoWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle(WIZARD_TITLE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		fPage = new CocoWizardPage(fSelection);
		addPage(fPage);
	}

	/**
	 * Remove the template files
	 *
	 * @param monitor
	 *            the progress monitor.
	 * @throws CoreException
	 */
	private void cleanUp(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Deleting templates", 1);
		if (fTemplate != null) {
			fTemplate.delete(true, monitor);
		}
		monitor.worked(1);
	}

	/**
	 * Copies/ Provides the necessary templates for a Coco/R Parser/Scanner.
	 *
	 * @param resource the container resource.
	 * @param filename the file name.
	 * @param monitor the progress monitor.
	 * @throws CoreException
	 */
	private void copyFiles(IResource resource, String filename, boolean overwrite, IProgressMonitor monitor)
			throws CoreException {
		IContainer container = (IContainer)resource;
		try {
			copy(container, overwrite);
		} catch (AccessControlException e) {
			throwCoreException(e.getMessage(), IStatus.WARNING);
		}
		// Rename template ATG
		final IFile file = container.getFile(new Path(CoreUtilities.ATG_TEMPLATE));
		if (!file.exists()) {
			InputStream bcs = createBasicContentStream();
			file.create(bcs, true, monitor);
			try {
				bcs.close();
			} catch (IOException e) {
			}
		}
		fTemplate = container.getFile(new Path(filename));
		if (fTemplate.exists()) {
			if (overwrite) {
				fTemplate.delete(true, monitor);
			} else {
				fTemplate = file;
			}
		}
		file.move(new Path(filename), true, monitor);
	}

	/**
	 * Return the basic structure of an ATG file.
	 *
	 * @return the basic ATG.
	 */
	private InputStream createBasicContentStream() {
		String contents = "COMPILER Coco\n\nPRODUCTIONS\n\nEND Coco.";
		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * The worker method. It will find the container, create the file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 *
	 * @param containername the destination containers name.
	 * @param filename the ATG filename.
	 * @param monitor the progress monitor.
	 * @throws CoreException
	 */
	private void doFinish(String containername, String filename, boolean overwrite, IProgressMonitor monitor)
			throws CoreException {
		// create a sample file
		monitor.beginTask("Creating templates", 3);
		IResource container = getContainer(containername);
		copyFiles(container, filename, overwrite, monitor);
		monitor.worked(1);

		monitor.setTaskName("Opening file for editing...");
		openTemplate();
		monitor.worked(1);

		monitor.setTaskName("Set Coco/R nature & builder...");
		CocoNature.appendTo(container.getProject());
		monitor.worked(1);
	}

	/**
	 * Returns the container to the corresponding container name.
	 *
	 * @param containerName The container name.
	 * @return the container to the corresponding container name.
	 * @throws CoreException
	 */
	private IResource getContainer(String containerName) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.", IStatus.ERROR);
		}
		return resource;
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize from it.
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fSelection = selection;
	}

	/**
	 * Open the template within the active pages editor.
	 */
	private void openTemplate() {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, fTemplate, true);
				} catch (PartInitException e) {
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					cleanUp(monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException ex) {
			Throwable realException = ex.getTargetException();
			IStatus status;
			if (realException instanceof CoreException) {
				status = ((CoreException)realException).getStatus();
			} else {
				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Unexpected error", realException);
			}
			ErrorDialog.openError(getShell(), "Error", status.getMessage(), status);
			IdeUtilities.logError(status);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		final String containerName = fPage.getContainerName();
		final String fileName = fPage.getFileName();
		final boolean force = fPage.overwriteFiles();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, force, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException ex) {
			Throwable realException = ex.getTargetException();
			IStatus status;
			if (realException instanceof CoreException) {
				status = ((CoreException)realException).getStatus();
			} else {
				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Unexpected error", realException);
			}
			ErrorDialog.openError(getShell(), "Error", status.getMessage(), status);
			IdeUtilities.logError(status);
			return false;
		}
		return true;
	}
}
