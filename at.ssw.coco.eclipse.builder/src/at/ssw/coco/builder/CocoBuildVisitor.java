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

package at.ssw.coco.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import at.ssw.coco.core.CocoError;
import at.ssw.coco.core.CoreUtilities;
import at.ssw.coco.core.Mapping;

/**
 * Implements the resource visitor for the corresponding <code>CocoBuilder</code>
 * and <code>CocoNature</code>.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class CocoBuildVisitor implements IResourceVisitor, IResourceDeltaVisitor {
	private final Set<IResource> alreadyBuilt = new HashSet<IResource>();

	public boolean visit(IResource resource) throws CoreException {
		process(resource);
		return true; // visit children
	}

	public boolean visit(IResourceDelta delta) throws CoreException {
		if (delta.getKind() != IResourceDelta.REMOVED) {
			process(delta.getResource());
		}
		return true; // visit children
	}

	/**
	 * Visits the given resource and in case it is an .atg or a Coco .frame file,
	 * calls Coco/R to rebuild the scanner and parser files.
	 *
	 * @param resource The IResource to process.
	 * @throws CoreException
	 */
	private void process(IResource resource) throws CoreException {
		if (resource.getType() != IResource.FILE || !resource.exists()) {
			return;
		}

		String extension = resource.getFileExtension();
		if (CoreUtilities.ATG_EXTENSION.equalsIgnoreCase(extension)) {
			buildAtgOnce(resource);
		} else if (CoreUtilities.SCANNER_TEMPLATE.equalsIgnoreCase(resource.getName())
				|| CoreUtilities.PARSER_TEMPLATE.equalsIgnoreCase(resource.getName())) {
			IResource[] affectedAtgs = findDependentAtgs(
					resource.getWorkspace().getRoot(), resource.getParent());
			for (IResource atg : affectedAtgs) {
				buildAtgOnce(atg);
			}
		}
	}

	/**
	 * Search the workspace for .atg files which depend on the .frame files in <code>framesDir</code>.
	 *
	 * @param root The workspace root (or any container therein).
	 * @param framesDir The folder which contains the .frame files.
	 * @return The found resources.
	 * @throws CoreException
	 */
	private static IResource[] findDependentAtgs(IContainer root, IResource framesDir) throws CoreException {
		List<IResource> atgs = new ArrayList<IResource>();
		for (IResource member : root.members()) {
			if (!member.isAccessible())
				continue; // resource does not exist or project not open

			if (member.getType() == IResource.FILE) { // file
				if (isAtg(member)) {
					IPath framesDirPath = getFramesDirPath(member, null);
					if (framesDirPath != null && framesDirPath.equals(framesDir.getLocation())) {
						// custom frame files directory
						atgs.add(member);
					} else if (framesDirPath == null && framesDir.equals(member.getParent())) {
						// default. the frame files are in the same directory
						atgs.add(member);
					}
				}
			} else { // container (root, folder, project)
				if (member.getType() == IResource.PROJECT) {
					if (!isCocoProject((IProject) member)) {
						continue; // skip projects without Coco nature in the search
					}
				}
				atgs.addAll(Arrays.asList(findDependentAtgs((IContainer) member, framesDir)));
			}
		}
		return atgs.toArray(new IResource[atgs.size()]);
	}

	/**
	 * Build ATG only once.
	 */
	private void buildAtgOnce(IResource resource) throws CoreException {
		if (!alreadyBuilt.contains(resource)) {
			buildAtg(resource);
			alreadyBuilt.add(resource);
		}
	}

	/**
	 * Generates the parser and scanner java files using Coco/R.
	 *
	 * @param resource The ATG file to build.
	 * @throws CoreException
	 */
	private static void buildAtg(IResource resource) throws CoreException {
		// delete old problem markers
		resource.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
		resource.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);

		IPath filePath = resource.getLocation().makeAbsolute();
		IPath sourceDirPath = filePath.removeLastSegments(1).addTrailingSeparator();
		IPath outputDirPath = getOutputDirPath(resource, sourceDirPath);
		IPath framesDirPath = getFramesDirPath(resource, sourceDirPath);

		IContainer[] outputContainers = resource.getWorkspace().getRoot()
				.findContainersForLocationURI(outputDirPath.toFile().toURI());
		boolean outputInWorkspace = outputContainers.length != 0; // mapping useful?
		Mapping mapping = outputInWorkspace ? new Mapping() : null;

		// execute Coco
		List<CocoError> errors = execute(filePath.toOSString(),
				framesDirPath.toOSString(), outputDirPath.toOSString(),
				getPackageName(resource, outputDirPath), getTraceString(resource), mapping);

		// create Coco problem markers
		for (CocoError err : errors) {
			createMarker(err, resource, IMarker.PROBLEM);
		}

		// remove backup files
		removeGeneratedFiles(outputDirPath.toOSString(), CoreUtilities.OLD_SUFFIX);

		// refresh input folder (trace.txt)
		resource.getParent().refreshLocal(IResource.DEPTH_ONE, null);

		// refresh output folders
		for (IContainer container : outputContainers) {
			container.refreshLocal(IResource.DEPTH_ONE, null);
		}

		// set atg file location and mapping properties on the parser output file
		File parserFile = outputDirPath.append(CoreUtilities.PARSER_OUTPUT).toFile();
		for (IFile file : resource.getWorkspace().getRoot().findFilesForLocationURI(parserFile.toURI())) {
			if (file.exists()) {
				file.setSessionProperty(Activator.ATG_FILE_LOCATION, filePath);
				file.setSessionProperty(Activator.ATG_MAPPING, mapping);
			}
		}
	}

	/**
	 * @param resource The resource bearing the properties.
	 * @return The configured trace string.
	 */
	private static String getTraceString(IResource resource) {
		try {
			String traceStr = resource.getPersistentProperty(Activator.COCO_TRACE_STRING);
			if (traceStr != null) {
				return traceStr;
			}
		} catch (CoreException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Returns the user configured output directory (if activated in the resource's properties).
	 *
	 * @param resource The resource bearing the properties.
	 * @param defaultFramesDirPath Default path.
	 * @return Directory path.
	 */
	private static IPath getOutputDirPath(IResource resource, IPath defaultOutputDirPath) {
		try {
			String useOutputDir = resource.getPersistentProperty(Activator.USE_CUSTOM_COCO_OUTPUT_DIR);
			String outputDir = resource.getPersistentProperty(Activator.CUSTOM_COCO_OUTPUT_DIR);
			if ("true".equals(useOutputDir) && outputDir != null) {
				IPath outputDirPath = Path.fromPortableString(outputDir);
				if (!outputDirPath.isAbsolute()) { // relative paths are relative to the project root
					outputDirPath = resource.getProject().getLocation().makeAbsolute().append(outputDirPath);
				}
				return outputDirPath.addTrailingSeparator();
			}
		} catch (CoreException e) {
			// ignore exceptions, just use default path
		}
		return defaultOutputDirPath;
	}

	/**
	 * Returns the configured directory for .frame files (if activated in the resource's properties).
	 *
	 * @param resource The resource bearing the properties.
	 * @param defaultFramesDirPath Default path.
	 * @return Directory path.
	 */
	private static IPath getFramesDirPath(IResource resource, IPath defaultFramesDirPath) {
		try {
			String useFramesDir = resource.getPersistentProperty(Activator.USE_CUSTOM_COCO_FRAMES_DIR);
			String framesDir = resource.getPersistentProperty(Activator.CUSTOM_COCO_FRAMES_DIR);
			if ("true".equals(useFramesDir) && framesDir != null) {
				IPath framesDirPath = Path.fromPortableString(framesDir);
				if (!framesDirPath.isAbsolute()) { // relative paths are relative to the project root
					framesDirPath = resource.getProject().getLocation().makeAbsolute().append(framesDirPath);
				}
				return framesDirPath.addTrailingSeparator();
			}
		} catch (CoreException e) {
			// ignore exceptions, just use default path
		}
		return defaultFramesDirPath;
	}

	/**
	 * Returns the package name to be used for the output files.
	 *
	 * @param resource The ATG resource.
	 * @param packageLocation The path of the output directory.
	 * @return The package name.
	 * @throws JavaModelException
	 */
	private static String getPackageName(IResource resource, IPath packageLocation) throws JavaModelException {
		try {
			// see if the user set a package name
			String useNamespace = resource.getPersistentProperty(Activator.USE_CUSTOM_COCO_NAMESPACE);
			if ("true".equals(useNamespace)) {
				String namespace = resource.getPersistentProperty(Activator.CUSTOM_COCO_NAMESPACE);
				return namespace != null ? namespace : ""; // safety measure
			}
		} catch (CoreException e) {
			// ignore exceptions, just use default path
		}

		// use JavaCore to detect the package by its location
		final IContainer[] containers = resource.getWorkspace().getRoot()
				.findContainersForLocationURI(packageLocation.toFile().toURI());
		if (containers.length > 0) {
			final IContainer container = containers[0];
			if (container instanceof IFolder) {
				IJavaElement packageElem = JavaCore.create(container);
				if (packageElem instanceof IPackageFragmentRoot) {
					return ""; // The unnamed default package
				} else if (packageElem != null && packageElem.exists()) {
					return packageElem.getElementName();
				}
			}
		}

		// Alternative determination using path name
		return resource.getProjectRelativePath().removeLastSegments(1).toOSString().replace(File.separatorChar, '.');
	}

	/**
	 * Creates an <code>IMarker</code> on the given resource containing error details.
	 *
	 * @param error    The error to create the marker from.
	 * @param resource The corresponding <code>IResource</code>.
	 * @param type     The marker type.
	 * @return the created marker.
	 * @throws CoreException
	 */
	private static IMarker createMarker(CocoError error, IResource resource, String type) throws CoreException {
		Assert.isNotNull(resource);
		IMarker marker = resource.createMarker(type);

		marker.setAttribute(IMarker.SEVERITY, error.getWarning() ? IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR);
		marker.setAttribute(IMarker.MESSAGE, error.getMessage());
		if (error.getLine() >= 0) {
			marker.setAttribute(IMarker.LINE_NUMBER, error.getLine());
		}
		return marker;
	}

	/**
	 * Executes Coco/R with the given parameters.
	 *
	 * @param atg       The atg source file name.
	 * @param outDir    The output directory.
	 * @param frameDir  The directory containing the frame files.
	 * @param namespace The package name for generated classes.
	 * @param traceStr  Optional trace string.
	 * @param mapping   Optional mapping information.
	 * @return a list of <code>CocoError</code>s.
	 */
	public static List<CocoError> execute(String atg, String frameDir, String outDir, String namespace, String traceStr, Mapping mapping) {
		String srcDir = new Path(atg).removeLastSegments(1).addTrailingSeparator().toOSString();
		return CoreUtilities.executeCoco(atg, srcDir, outDir, frameDir, namespace, traceStr, mapping);
	}

	/**
	 * Removes the generated parser and scanner with the given suffix.
	 *
	 * @param path The parser and scanner output directory path.
	 * @param suffix The parser and scanner suffix.
	 */
	public static void removeGeneratedFiles(String path, String suffix) {
		new File(path, CoreUtilities.SCANNER_OUTPUT + suffix).delete();
		new File(path, CoreUtilities.PARSER_OUTPUT + suffix).delete();
	}

	/**
	 * @param resource The resource.
	 * @return whether the resource appears to be an ATG source file.
	 */
	private static boolean isAtg(IResource resource) {
		return resource.getType() == IResource.FILE &&
			CoreUtilities.ATG_EXTENSION.equalsIgnoreCase(resource.getFileExtension());
	}

	/**
	 * @param project The {@link IProject}.
	 * @return whether the project is a Coco/R project.
	 */
	private static boolean isCocoProject(IProject project) {
		try {
			return project.hasNature(BuilderUtilities.NATURE_ID);
		} catch (CoreException e) {
			// Project not open or not accessible
		}
		return false;
	}
}
