package at.ssw.coco.lib.model.scanners.types;

/**
 * Represents the last read special signs and their length.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */

//beginnings of prefixes and postfixes
public enum Last {
	NONE(0),
	BACKSLASH(1), // postfix for STRING and CHARACTER
	SLASH(1), // prefix for SINGLE_LINE or MULTI_LINE or JAVADOC
	SLASH_STAR(2), // prefix for MULTI_LINE_COMMENT or JAVADOC
	SLASH_STAR_STAR(3), // prefix for MULTI_LINE_COMMENT or JAVADOC
	STAR(1), // postfix for MULTI_LINE_COMMENT or JAVADOC
	CARRIAGE_RETURN(1), // postfix for STRING, CHARACTER and SINGLE_LINE_COMMENT
	COMPILER("COMPILER".length()), // prefix for COMPILER_KEYWORD
	LPAR(1), // prefix for INLINE_CODE_START
	DOT(1), // prefix for INLINE_CODE_END
	;

	public final int length;

	private Last(int length) {
		this.length = length;
	}

	public int getLength() {
		return length;
	}
}
