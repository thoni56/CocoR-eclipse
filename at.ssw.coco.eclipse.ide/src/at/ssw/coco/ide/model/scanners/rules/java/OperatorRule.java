package at.ssw.coco.ide.model.scanners.rules.java;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class OperatorRule implements IRule {

	/** Java operators */
	private final char[] JAVA_OPERATORS = { ';', '.', '=', '/', '\\', '+',
			'-', '*', '<', '>', ':', '?', '!', ',', '|', '&', '^', '%', '~' };
	/** Token to return for this rule */
	private final IToken fToken;

	/**
	 * Creates a new operator rule.
	 * 
	 * @param token
	 *            Token to use for this rule
	 */
	public OperatorRule(IToken token) {
		fToken = token;
	}

	/**
	 * Is this character an operator character?
	 * 
	 * @param character
	 *            Character to determine whether it is an operator character
	 * @return <code>true</code> if the character is an operator,
	 *         <code>false</code> otherwise.
	 */
	public boolean isOperator(char character) {
		for (int i = 0; i < JAVA_OPERATORS.length; i++) {
			if (JAVA_OPERATORS[i] == character)
				return true;
		}
		return false;
	}

	/*
	 * @see
	 * org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {

		int character = scanner.read();
		if (isOperator((char) character)) {
			do {
				character = scanner.read();
			} while (isOperator((char) character));
			scanner.unread();
			return fToken;
		} else {
			scanner.unread();
			return Token.UNDEFINED;
		}
	}
}
