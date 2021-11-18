/*******************************************************************************
 * Copyright (C) 2009 Institute for System Software, JKU Linz
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
package at.ssw.coco.ide.model.scanners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import at.ssw.coco.ide.model.detectors.whitespace.WhitespaceDetectorAdaptor;
import at.ssw.coco.ide.model.detectors.word.JavaKeywordDetectorAdaptor;
import at.ssw.coco.ide.model.scanners.rules.java.BracketRule;
import at.ssw.coco.ide.model.scanners.rules.java.OperatorRule;
import at.ssw.coco.ide.style.SyntaxManager;



/**
 * Implements a scanner (used for syntax highlighting) which interprets and handles the corresponding section as Java
 * source code.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Markus Koppensteiner <mkoppensteiner@users.sf.net>
 */

public class JavaCodeScanner extends RuleBasedScanner {

	private static final String[] KEYWORDS = { "abstract", "break", "case",
			"catch", "class", "const", "continue", "default", "do", "else",
			"extends", "final", "finally", "for", "goto", "if", "implements",
			"import", "instanceof", "interface", "native", "new", "package",
			"private", "protected", "public", "static", "strictfp",
			"super", "switch", "synchronized", "this", "throw", "throws",
			"transient", "try", "volatile", "while" };

	private static final String[] JAVA14_KEYWORDS = { "assert" };

	private static final String[] JAVA15_KEYWORDS = { "enum" };

	private static final String[] TYPES = { "void", "boolean", "char", "byte", "short", "int", "long", "float", "double" };

	private static final String[] CONSTANTS = { "false", "null", "true" };

	private static final String[][] HIGHLIGHT = { KEYWORDS, JAVA14_KEYWORDS, JAVA15_KEYWORDS, TYPES, CONSTANTS };
	
	private List<IRule> rules;
	
	private WordRule wordRule;
	
	protected SyntaxManager syntaxManager;

	/**
	 * The Constructor.
	 *
	 * @param manager the syntax manager.
	 */
	public JavaCodeScanner(SyntaxManager syntaxManager) {
		super();
		this.syntaxManager = syntaxManager;
		
		createRules();
		applyRules();
	}


	protected List<IRule> createRules() {
		IToken defaultToken = syntaxManager.getSyntaxToken(SyntaxManager.Java.DEFAULT);
		IToken keywordToken = syntaxManager.getSyntaxToken(SyntaxManager.Java.KEYWORD);
		IToken returnToken = syntaxManager.getSyntaxToken(SyntaxManager.Java.KEYWORD_RETURN);
		
		rules = new ArrayList<IRule>();

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new WhitespaceDetectorAdaptor()));
		
		IToken token = syntaxManager.getSyntaxToken(SyntaxManager.Java.OPERATOR);
		rules.add(new OperatorRule(token));
		
		token = syntaxManager.getSyntaxToken(SyntaxManager.Java.BRACKET);
		rules.add(new BracketRule(token));

		// Add word rule for keywords, types, and constants.
		wordRule = new WordRule(new JavaKeywordDetectorAdaptor(), defaultToken);
		for (String[] arr : HIGHLIGHT) {
			for (String str : arr) {
				wordRule.addWord(str, keywordToken);
			}
		}
		wordRule.addWord("return", returnToken);
		
		rules.add(wordRule);
		return rules;
	}

	/**
	 * rereads styles from syntax manager
	 */
	public void reCreateRules(){
		createRules();
		applyRules();
	}

	/**
	 * applies the created rules
	 */
	private void applyRules() {
		setRules(rules.toArray(new IRule[rules.size()]));
		setDefaultReturnToken(syntaxManager.getSyntaxToken(SyntaxManager.Java.DEFAULT));
	}

	
}
