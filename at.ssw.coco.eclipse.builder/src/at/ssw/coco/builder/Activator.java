/*******************************************************************************
 * Copyright (C) 2006 Institute for System Software, JKU Linz
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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "at.ssw.coco.eclipse.builder";

	public static final QualifiedName CUSTOM_COCO_OUTPUT_DIR = new QualifiedName(
			Activator.PLUGIN_ID, "CUSTOM_COCO_OUTPUT_DIR");
	public static final QualifiedName USE_CUSTOM_COCO_OUTPUT_DIR = new QualifiedName(
			Activator.PLUGIN_ID, "USE_CUSTOM_COCO_OUTPUT_DIR");

	public static final QualifiedName CUSTOM_COCO_FRAMES_DIR = new QualifiedName(
			Activator.PLUGIN_ID, "CUSTOM_COCO_FRAMES_DIR");
	public static final QualifiedName USE_CUSTOM_COCO_FRAMES_DIR = new QualifiedName(
			Activator.PLUGIN_ID, "USE_CUSTOM_COCO_FRAMES_DIR");

	public static final QualifiedName CUSTOM_COCO_NAMESPACE = new QualifiedName(
			Activator.PLUGIN_ID, "CUSTOM_COCO_NAMESPACE");
	public static final QualifiedName USE_CUSTOM_COCO_NAMESPACE = new QualifiedName(
			Activator.PLUGIN_ID, "USE_CUSTOM_COCO_NAMESPACE");

	public static final QualifiedName COCO_TRACE_STRING = new QualifiedName(
			Activator.PLUGIN_ID, "COCO_TRACE_STRING");

	public static final QualifiedName ATG_FILE_LOCATION = new QualifiedName(
			Activator.PLUGIN_ID, "ATG_FILE_LOCATION");
	public static final QualifiedName ATG_MAPPING = new QualifiedName(
			Activator.PLUGIN_ID, "ATG_MAPPING");

	// The shared instance
	private static Activator plugin;

	private final IResourceChangeListener postBuildListener = new PostBuildListener();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(postBuildListener, IResourceChangeEvent.POST_BUILD);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(postBuildListener);

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
