package at.ssw.coco.ide.model.scanners.rules.java;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class BracketRule implements IRule {

	/** Java brackets */
	private final char[] JAVA_BRACKETS = { '(', ')', '{', '}', '[', ']' };
	/** Token to return for this rule */
	private final IToken fToken;

	/**
	 * Creates a new bracket rule.
	 * 
	 * @param token
	 *            Token to use for this rule
	 */
	public BracketRule(IToken token) {
		fToken = token;
	}

	/**
	 * Is this character a bracket character?
	 * 
	 * @param character
	 *            Character to determine whether it is a bracket character
	 * @return <code>true</code> if the character is a bracket,
	 *         <code>false</code> otherwise.
	 */
	public boolean isBracket(char character) {
		for (int i = 0; i < JAVA_BRACKETS.length; i++) {
			if (JAVA_BRACKETS[i] == character)
				return true;
		}
		return false;
	}

	/*
	 * @see
	 * org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text
	 * .rules.ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {

		int character = scanner.read();
		if (isBracket((char) character)) {
			do {
				character = scanner.read();
			} while (isBracket((char) character));
			scanner.unread();
			return fToken;
		} else {
			scanner.unread();
			return Token.UNDEFINED;
		}
	}
}
