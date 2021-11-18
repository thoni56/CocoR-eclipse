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

import at.ssw.coco.lib.model.scanners.ATGKeywords;
import at.ssw.coco.ide.model.detectors.whitespace.WhitespaceDetectorAdaptor;
import at.ssw.coco.ide.model.detectors.word.CocoIdentDetectorAdaptor;
import at.ssw.coco.ide.style.SyntaxManager;

/**
 * Implements a scanner (used for syntax highlighting) which interprets and handles the corresponding section as ATG.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ATGLangScanner extends RuleBasedScanner {

	/**
	 * The Constructor.
	 *
	 * @param manager the syntax manager.
	 */
	public ATGLangScanner(SyntaxManager manager) {
		IToken defaultToken = manager.getSyntaxToken(SyntaxManager.ATG.DEFAULT);
		IToken keywordToken = manager.getSyntaxToken(SyntaxManager.ATG.KEYWORD);

		List<IRule> rules = new ArrayList<IRule>();

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new WhitespaceDetectorAdaptor()));

		WordRule wordRule = new WordRule(new CocoIdentDetectorAdaptor(), defaultToken);
		for (ATGKeywords word : ATGKeywords.values()) {
			wordRule.addWord(word.name(), keywordToken);
		}

		rules.add(wordRule);

		setRules(rules.toArray(new IRule[0]));
		setDefaultReturnToken(defaultToken);
	}
}
