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

import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class provides the infrastructure for a builder to generate the parser & scanner and fulfills the contract
 * specified by the <code>org.eclipse.core.resources.builders</code> standard extension point.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 *
 */
public class CocoBuilder extends IncrementalProjectBuilder {
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	/**
	 * Performs a full build.
	 *
	 * @param monitor
	 *            the progress monitor.
	 * @throws CoreException
	 */
	protected void fullBuild(IProgressMonitor monitor) throws CoreException {
		CocoBuildVisitor buildVisitor = new CocoBuildVisitor();
		getProject().accept(buildVisitor);
	}

	/**
	 * Performs a incremental build.
	 *
	 * @param delta
	 *            the resource delta.
	 * @param monitor
	 *            the progress monitor.
	 * @throws CoreException
	 */
	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		CocoBuildVisitor buildVisitor = new CocoBuildVisitor();
		delta.accept(buildVisitor);
	}

	/**
	 * Appends this builder to the given target project.
	 *
	 * @param project
	 *            the target project.
	 * @throws CoreException
	 */
	public static void appendTo(IProject project) throws CoreException {
		Assert.isNotNull(project);
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		boolean found = false;

		for (ICommand command : commands) {
			if (command.getBuilderName().equals(BuilderUtilities.BUILDER_ID)) {
				found = true;
				break;
			}
		}
		if (!found) {
			// add builder to project
			ICommand command = desc.newCommand();
			command.setBuilderName(BuilderUtilities.BUILDER_ID);
			ICommand[] newCommands = new ICommand[commands.length + 1];
			// Add it before other builders.
			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0] = command;
			desc.setBuildSpec(newCommands);
			project.setDescription(desc, null);
		}
	}

	/**
	 * Removes this builder from the given target project.
	 *
	 * @param project
	 *            the target project.
	 * @throws CoreException
	 */
	public static void removeFrom(IProject project) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			String bn = commands[i].getBuilderName();
			if (bn.equals(BuilderUtilities.BUILDER_ID) || bn.equals(BuilderUtilities.BUILDER_ID_v1_3)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				desc.setBuildSpec(newCommands);
				project.setDescription(desc, null);
			}
		}
	}
}
