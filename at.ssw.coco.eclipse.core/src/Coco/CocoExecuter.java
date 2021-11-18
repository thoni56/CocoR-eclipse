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

package Coco;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;

import at.ssw.coco.core.CocoError;
import at.ssw.coco.core.Mapping;

/**
 * Handles the execution of the Coco/R. This includes analyzing the output (extracting the error messages)
 * <p>
 * Note: This class must be in the package Coco due to limitations of the current Coco/R version.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 *
 */
public final class CocoExecuter {

	private CocoExecuter() {
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
	 * @return a list of <code>CocoError</code>s.
	 */
	public static List<CocoError> execute(String srcName, String srcDir, String outDir,
			String frameDir, String nsName, String traceStr, Mapping mapping) {
		List<CocoError> result = new ArrayList<CocoError>();
		try {
			Scanner scanner = new Scanner(srcName);
			Parser parser = new Parser(scanner);
			parser.errors = new CocoErrors(result);

			parser.trace = new Trace(srcDir);
			parser.tab = new Tab(parser);
			parser.dfa = new DFA(parser);
			parser.pgen = (mapping != null) ?
					new MappingParserGen(parser, mapping) : new ParserGen(parser);

			parser.tab.srcName = srcName;
			parser.tab.srcDir = srcDir;
			parser.tab.nsName = nsName;
			parser.tab.frameDir = frameDir;
			parser.tab.outDir = outDir;
			if (traceStr != null) parser.tab.SetDDT(traceStr);

			parser.Parse();

			parser.trace.Close();

		} catch (FatalError ex) {
			result.add(new CocoError(false, ex.getMessage()));
		}
		return result;
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
	 * @return a list of <code>CocoError</code>s.
	 */
	public static List<CocoError> execute(InputStream stream, String srcName, String srcDir, String outDir,
			String frameDir, String nsName, String traceStr, Mapping mapping) {
		List<CocoError> result = new ArrayList<CocoError>();
		try {
			Scanner scanner = new Scanner(stream);
			Parser parser = new Parser(scanner);
			parser.errors = new CocoErrors(result);

			parser.trace = new Trace(srcDir);
			parser.tab = new Tab(parser);
			parser.dfa = new DFA(parser);
			parser.pgen = (mapping != null) ?
					new MappingParserGen(parser, mapping) : new ParserGen(parser);

			parser.tab.srcName = srcName;
			parser.tab.srcDir = srcDir;
			parser.tab.nsName = nsName;
			parser.tab.frameDir = frameDir;
			parser.tab.outDir = outDir;
			if (traceStr != null) parser.tab.SetDDT(traceStr);

			parser.Parse();

			parser.trace.Close();

		} catch (FatalError ex) {
			result.add(new CocoError(false, ex.getMessage()));
		}
		return result;
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
	 * @return a list of <code>CocoError</code>s.
	 */
	public static String generateATGEditorTempParser(InputStream stream, String srcName, String srcDir,
			String frameDir, String nsName, String traceStr) {
		List<CocoError> result = new ArrayList<CocoError>();
		String parserString = new String();
		try {
			Scanner scanner = new Scanner(stream);
			Parser parser = new Parser(scanner);
			parser.errors = new CocoErrors(result);

			parser.trace = new Trace(srcDir);
			parser.tab = new Tab(parser);
			parser.dfa = new DFA(parser);
			
			TemporaryParserGen gen = new TemporaryParserGen(parser);
			parser.pgen = gen;

			parser.tab.srcName = srcName;
			parser.tab.srcDir = srcDir;
			parser.tab.nsName = nsName;
			parser.tab.frameDir = frameDir;
			if (traceStr != null) parser.tab.SetDDT(traceStr);

			parser.Parse();

			parser.trace.Close();
			
			parserString = gen.getParser();
		
		} catch (FatalError ex) {
			result.add(new CocoError(false, ex.getMessage()));
		}
		return parserString;
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
	 * @return a list of <code>CocoError</code>s.
	 */
	public static Tab generateParserTab(String srcName, String srcDir,
			String frameDir, String nsName, String traceStr) {
		List<CocoError> result = new ArrayList<CocoError>();
		Scanner scanner = new Scanner(srcName);
		Parser parser = new Parser(scanner);
		try {
			
			parser.errors = new CocoErrors(result);

			parser.trace = new Trace(srcDir);
			parser.tab = new Tab(parser);
			parser.dfa = new DFA(parser);
			
			TemporaryParserGen gen = new TemporaryParserGen(parser);
			parser.pgen = gen;

			parser.tab.srcName = srcName;
			parser.tab.srcDir = srcDir;
			parser.tab.nsName = nsName;
			parser.tab.frameDir = frameDir;
			if (traceStr != null) parser.tab.SetDDT(traceStr);

			parser.Parse();

			parser.trace.Close();
		
		} catch (FatalError ex) {
			result.add(new CocoError(false, ex.getMessage()));
		}
		return parser.tab;
	}
	
	
	/**
	 * Executes Coco/R with and create config files for the editor.
	 *
	 * @param srcName The atg file name.
	 * @param srcDir The source directory.
	 * @param outDir The output directory.
	 * @param frameDir The directory containing the frame files.
	 * @param nsName The parsers package name.
	 * @param traceStr The trace string.
	 * @return a list of <code>CocoError</code>s.
	 */
	public static Parser generateEditor(String srcName, String srcDir, String outDir,
			String frameDir, String nsName, String traceStr) {
		Scanner scanner = null;
		Parser parser = null;
		
		try {
			scanner = new Scanner(srcName);
			parser = new Parser(scanner);
			
			parser.trace = new Trace(srcDir);
			parser.tab = new Tab(parser);
			parser.dfa = new DFA(parser);
			parser.pgen = new ParserGen(parser);

			parser.tab.srcName = srcName;
			parser.tab.srcDir = srcDir;
			parser.tab.nsName = nsName;
			parser.tab.frameDir = frameDir;
			parser.tab.outDir = outDir;
			if (traceStr != null) parser.tab.SetDDT(traceStr);

			parser.Parse();

			parser.trace.Close();
		
			
			
		} catch (FatalError ex) {
			System.out.println(ex.getMessage());
		}
		
		return parser;

	}
	
}
