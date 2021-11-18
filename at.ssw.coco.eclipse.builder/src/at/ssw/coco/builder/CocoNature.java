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

package at.ssw.coco.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * Implements the Coco/R Extension project nature.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 *
 */
public class CocoNature implements IProjectNature {
	/** The corresponding project. */
	private IProject fProject;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		// Add nature-specific information
		// for the project, such as adding a builder
		// to a project's build spec.
		CocoBuilder.appendTo(fProject);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		// Remove the nature-specific information here.
		CocoBuilder.removeFrom(fProject);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return fProject;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		fProject = project;
	}

	/**
	 * Appends this nature to the given target project.
	 *
	 * @param project
	 *            The target project.
	 * @throws CoreException
	 */
	public static void appendTo(IProject project) throws CoreException {
		Assert.isNotNull(project);
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (String nature : natures) {
			if (nature.equals(BuilderUtilities.NATURE_ID)) {
				return;
			}
		}

		// Add nature to project
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		// Append it at the end.
		newNatures[natures.length] = BuilderUtilities.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}

	/**
	 * Removes this nature from the given target project.
	 *
	 * @param project
	 *            The target project.
	 * @throws CoreException
	 */
	public static void removeFrom(IProject project) throws CoreException {
		Assert.isNotNull(project);
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (natures[i].equals(BuilderUtilities.NATURE_ID) || natures[i].equals(BuilderUtilities.NATURE_ID_v1_3)) {
				String[] newNatures = new String[natures.length - 1];
				System.arraycopy(natures, 0, newNatures, 0, i);
				System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
			}
		}
	}
}
