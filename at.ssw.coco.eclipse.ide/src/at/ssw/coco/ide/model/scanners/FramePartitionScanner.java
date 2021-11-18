/*******************************************************************************
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
package at.ssw.coco.ide.model.scanners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import at.ssw.coco.ide.model.scanners.rules.WordPredicateRule;
import at.ssw.coco.lib.model.scanners.FramePartitions;

/**
 * Implements the scanner used to partition .frame files.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class FramePartitionScanner extends RuleBasedPartitionScanner {
	private static final String[] COMMON_TOKENS = { "-->begin",
			"-->declarations", "-->initialization", "$$$" };
	private static final String[] SCANNER_TOKENS = { "-->casing2", "-->casing3",
			"-->casing", "-->comments", "-->scan1", "-->scan2", "-->scan3",
			"-->scan4" /*in earlier Coco/R versions only*/ };
	private static final String[] PARSER_TOKENS = { "-->constants", "-->pragmas",
			"-->productions", "-->parseRoot", "-->errors" };

	public FramePartitionScanner() {
		final IToken multiLineCommentToken = new Token(FramePartitions.MULTI_LINE_COMMENT);
		final IToken singleLineCommentToken = new Token(FramePartitions.SINGLE_LINE_COMMENT);
		final IToken stringToken = new Token(FramePartitions.STRING);
		final IToken charToken = new Token(FramePartitions.CHARACTER);
		final IToken frameToken = new Token(FramePartitions.FRAME_KEYWORD);

		List<IPredicateRule> rules = new ArrayList<IPredicateRule>();

		// Add rule for multiple line comments.
		rules.add(new MultiLineRule("/*", "*/", multiLineCommentToken, (char)0, true));

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", singleLineCommentToken));

		for (String[] arr : new String[][] { COMMON_TOKENS, SCANNER_TOKENS, PARSER_TOKENS }) {
			for (String str : arr) {
				// rules.add(new WordPatternRule(new FrameKeywordDetector(), str, "", frameToken));
				rules.add(new WordPredicateRule(str, frameToken));
			}
		}

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", stringToken, '\\'));
		rules.add(new SingleLineRule("'", "'", charToken, '\\'));

		setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
	}
}
