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
package at.ssw.coco.ide.model.scanners.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;

/**
 * A predicate rule for detecting words.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class WordPredicateRule extends SingleLineRule {
	/**
	 * Creates a rule for the given word which, if detected, will return the specified token.
	 *
	 * @param word the word
	 * @param token the token to be returned on success
	 */
	public WordPredicateRule(String word, IToken token) {
		this(word, token, (char)0);
	}

	/**
	 * Creates a rule for the given word which, if detected, will return the specified token.
	 * Any character which follows the given escapeCharacter will be ignored.
	 *
	 * @param word the word
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 */
	public WordPredicateRule(String word, IToken token, char escapeCharacter) {
		super(word, "", token, escapeCharacter);
	}

	/**
	 * Returns whether the end sequence was detected.
	 * Since there's no end sequence, always returns true.
	 *
	 * @param scanner the scanner to be used
	 * @return <code>true</code>
	 */
	@Override
	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		return true;
	}
}
