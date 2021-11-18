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
package at.ssw.coco.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaModelMarker;

import at.ssw.coco.core.CoreUtilities;
import at.ssw.coco.core.Mapping;

/**
 * This visitor creates Java problem markers originally assigned by the Java
 * builder to the generated parser file in the respective regions of the ATG
 * source file.
 *
 * The required mapping information is stored by the Coco/R builder as a session
 * property on the generated file.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
final class MarkerMappingVisitor implements IResourceDeltaVisitor {
	public boolean visit(IResourceDelta delta) throws CoreException {
		if (delta.getResource().getType() == IResource.FILE) {
			if (delta.getKind() != IResourceDelta.REMOVED) {
				processResourceDelta(delta);
			}
		}
		return true; // visit children
	}

	private void processResourceDelta(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (!isParser(resource) || !resource.exists() || delta.getKind() == IResourceDelta.REMOVED)
			return;

		IPath atgFilePath = (IPath) resource.getSessionProperty(Activator.ATG_FILE_LOCATION);
		if (atgFilePath == null)
			return;

		IFile[] files = resource.getWorkspace().getRoot().findFilesForLocationURI(
				atgFilePath.toFile().toURI());
		for (IFile atgFile : files) {
			if (!atgFile.exists()) continue;

			cleanJavaMarkers(atgFile);

			Mapping atgmap = readMapping(resource);
			if (atgmap == null) {
				return; // missing or invalid mapping
			}

			IMarker[] javamarkers = resource.findMarkers(
					IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);

			if (javamarkers.length > 0) {
				IMarker[] atgmarkers = filterMarkers(javamarkers, atgmap, 0);
				atgmarkers = createMarkers(atgmarkers, atgFile);
				shiftMarkers(atgmarkers, atgmap);
			}
		}
	}

	private Mapping readMapping(IResource resource) throws CoreException {
		return (Mapping) resource.getSessionProperty(Activator.ATG_MAPPING);
	}

	private static IMarker[] filterMarkers(IMarker[] markers, Mapping mapping, int minimumSeverity) throws CoreException {
		List<IMarker> filtered = new ArrayList<IMarker>();
		for (IMarker marker : markers) {
			try {
				int charStart = ((Integer) marker.getAttribute(IMarker.CHAR_START)).intValue();
				int charEnd = ((Integer) marker.getAttribute(IMarker.CHAR_END)).intValue();
				if (minimumSeverity > 0) {
					int severity = ((Integer) marker.getAttribute(IMarker.SEVERITY)).intValue();
					if (severity < minimumSeverity) {
						continue;
					}
				}
				Mapping.Position startpos = mapping.get(charStart);
				Mapping.Position endpos = mapping.get(charEnd);
				if (startpos != null && endpos != null && startpos.line == endpos.line) {
					filtered.add(marker);
				}
			} catch (NullPointerException e) { // no such attribute
			} catch (ClassCastException e) { // not an integer?!
			}
		}
		return filtered.toArray(new IMarker[filtered.size()]);
	}

	private static IMarker[] createMarkers(IMarker[] markers, IResource resource) throws CoreException {
		if (markers.length > 0) {
			for (IMarker marker : markers) {
				IMarker createdMarker = resource.createMarker(marker.getType());
				createdMarker.setAttributes(marker.getAttributes());
			}
			return resource.findMarkers(markers[0].getType(), false, IResource.DEPTH_ZERO);
		} else {
			return new IMarker[0];
		}
	}

	private static void shiftMarkers(IMarker[] markers, Mapping mapping) throws CoreException {
		for (int i = 0; i < markers.length; i++) {
			try {
				int charStart = ((Integer) markers[i].getAttribute(IMarker.CHAR_START)).intValue();
				int charEnd = ((Integer) markers[i].getAttribute(IMarker.CHAR_END)).intValue();
				int lineNumber = ((Integer) markers[i].getAttribute(IMarker.LINE_NUMBER)).intValue();

				Mapping.Position position = mapping.get(charStart);

				if (position != null) {
					charEnd = position.offset + (charEnd - charStart);
					charStart = position.offset;
					lineNumber = position.line;
				}

				markers[i].setAttribute(IMarker.CHAR_START, charStart);
				markers[i].setAttribute(IMarker.CHAR_END, charEnd);
				markers[i].setAttribute(IMarker.LINE_NUMBER, lineNumber);
			} catch (NullPointerException e) { // no such attribute
			} catch (ClassCastException e) { // not an integer?!
			}
		}
	}

	private static boolean isParser(IResource resource) {
		return CoreUtilities.PARSER_OUTPUT.equalsIgnoreCase(resource.getName());
	}

	private static void cleanJavaMarkers(IResource resource) throws CoreException {
		resource.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
	}
}
