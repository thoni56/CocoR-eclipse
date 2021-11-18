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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

/**
 * Utility class with ID-strings and static methods.
 *
 * @author Christian Wimmer
 */
public class BuilderUtilities {
	/** The ID of the project nature. */
	public static final String NATURE_ID = Activator.PLUGIN_ID + ".CocoNature";
	public static final String NATURE_ID_v1_3 = "at.ssw.coco.builder.CocoNature";

	/** The ID of the builder. */
	public static final String BUILDER_ID = Activator.PLUGIN_ID + ".CocoBuilder";
	public static final String BUILDER_ID_v1_3 = "at.ssw.coco.builder.CocoBuilder";

	private BuilderUtilities() {
		// private constructor - prevent instantiation of this utility class
	}

	/**
	 * Utility method to report an error message to the platform log.
	 *
	 * @param message
	 *            The human-readable message.
	 * @param ex
	 *            The exception, or null if not applicable.
	 */
	public static void logError(String message, Throwable ex) {
		IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, message, ex);
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		Platform.getLog(bundle).log(status);
	}
}
