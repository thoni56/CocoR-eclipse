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

package at.ssw.coco.core;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

import Coco.CocoExecuter;

/**
 * Utility class with ID-strings and static methods.
 *
 * @author Christian Wimmer
 */
public final class CoreUtilities {
	/** The plug-in ID as speficified in the MANIFEST.MF */
	public static final String PLUGIN_ID = "at.ssw.coco.eclipse.core";

	/** The file extension of atg-files. */
	public static final String ATG_EXTENSION = "atg";

	/** The file name of the generated scanner file. */
	public static final String SCANNER_OUTPUT = "Scanner.java";

	/** The file name of the generated scanner file. */
	public static final String PARSER_OUTPUT = "Parser.java";

	/** The suffix to mark old scanner and parser files. */
	public static String OLD_SUFFIX = ".old";

	/** The file name of the atg template file. */
	public static String ATG_TEMPLATE = "template.atg";

	/** The file name of the scanner template file. */
	public static String SCANNER_TEMPLATE = "Scanner.frame";

	/** The file name of the parser template file. */
	public static String PARSER_TEMPLATE = "Parser.frame";

	/** The file names of all template files. */
	public static String[] ALL_TEMPLATES = { ATG_TEMPLATE, SCANNER_TEMPLATE, PARSER_TEMPLATE };

	private CoreUtilities() {
		// private constructor - prevent instantiation of this utility class
	}

	/**
	 * Executes Coco/R with the given parameters.
	 *
	 * @param srcName The atg file name.
	 * @param srcDir The source directory.
	 * @param outDir The output directory.
	 * @param frameDir The directory containing the frame files.
	 * @param nsName The parsers package name.
	 * @param traceStr The trace string.
	 * @param mapping An optional mapping.
	 * @return a list of <code>CocoError</code>s.
	 */
	public static List<CocoError> executeCoco(String srcName, String srcDir, String outDir, String frameDir, String nsName, String traceStr, Mapping mapping) {
		return CocoExecuter.execute(srcName, srcDir, outDir, frameDir, nsName, traceStr, mapping);
	}
	public static List<CocoError> executeCoco(String srcName, String srcDir, String outDir, String frameDir, String nsName, String traceStr) {
		return CocoExecuter.execute(srcName, srcDir, outDir, frameDir, nsName, traceStr, null);
	}
	public static List<CocoError> executeCoco(String srcName, String srcDir, String outDir, String frameDir, String nsName) {
		return CocoExecuter.execute(srcName, srcDir, outDir, frameDir, nsName, null, null);
	}
	public static List<CocoError> executeCoco(InputStream stream, String srcName, String srcDir, String outDir, String frameDir, String nsName){
		return CocoExecuter.execute(stream, srcName, srcDir, outDir, frameDir, nsName, null, null);
	}
	
	public static String generateTemporaryParser(InputStream stream, String srcName, String srcDir, String frameDir, String nsName){
		return CocoExecuter.generateATGEditorTempParser(stream, srcName, srcDir, frameDir, nsName, null);
	}
	
	public static Coco.Tab generateParserTab( String srcName, String srcDir, String frameDir, String nsName){
		return CocoExecuter.generateParserTab(srcName, srcDir, frameDir, nsName, null);
	}
	
	public static Coco.Parser generateEditor(String srcName, String srcDir, String outDir, String frameDir, String nsName, String traceStr){
		return CocoExecuter.generateEditor(srcName, srcDir, outDir, frameDir, nsName, traceStr);
	}
	
	
	
	/**
	 * Computes the template URL where a template file can be copied from.
	 *
	 * @param filename The template file (one of {@link #ALL_TEMPLATES}).
	 * @return The URL of the template file.
	 */
	public static URL getTemplate(String filename) {
		return Platform.getBundle(PLUGIN_ID).getEntry("lib/" + filename);
	}

	/**
	 * Utility method to report an error message to the platform log.
	 *
	 * @param message The human-readable message.
	 * @param ex The exception, or null if not applicable.
	 */
	public static void logError(String message, Throwable ex) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, 0, message, ex);
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		Platform.getLog(bundle).log(status);
	}
}
