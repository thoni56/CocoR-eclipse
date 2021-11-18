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
package at.ssw.coco.lib.model.scanners;

import java.util.Arrays;
import java.util.List;


import at.ssw.coco.lib.model.detectors.whitespace.CocoWhitespaceDetector;
import at.ssw.coco.lib.model.detectors.word.CocoIdentDetector;
import at.ssw.coco.lib.model.detectors.word.WordDetector;
import at.ssw.coco.lib.model.scanners.BufferedDocumentScanner;
import at.ssw.coco.lib.model.scanners.types.CocoToken;
import at.ssw.coco.lib.model.scanners.types.Last;
import at.ssw.coco.lib.model.scanners.types.State;
import at.ssw.coco.lib.model.scanners.types.TokenImpl;

/**
 * Implements a partition scanner for .atg files.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class FastATGPartitionScanner {

	private static boolean isLegalContentType(String key) {
		return State.stateMap.containsKey(key);
	}

	/** The scanner. */
	private final BufferedDocumentScanner fScanner = new BufferedDocumentScanner(1000);	// faster implementation
	/** The offset of the last returned token. */
	private int fTokenOffset;
	/** The length of the last returned token. */
	private int fTokenLength;

	/** The current read character*/
	private int ch;
	/** The last read character*/
	private int lch;
	
	/** The state of the scanner. */
	private State fState;
	/** The last significant characters read. */
	private Last fLast;
	/** The amount of characters already read on first call to nextToken(). */
	private int fPrefixLength;

	/** The document to be scanned. */
	private String fDocument;
	
	private String[] fLegalLineDelimiters;

	/** The offset of the partial range to be scanned. */
	private int fRangeOffset;
	/** The length of the partial range to be scanned. */
	private int fRangeLength;

	/** Allow multi line comments to be nested. */
	private boolean fNestedMultiLineComments = true;

	/**
	 * @param NestedMultiLineComments the NestedMultiLineComments to set
	 */
	public void setNestedMultiLineComments(boolean NestedMultiLineComments) {
		fNestedMultiLineComments = NestedMultiLineComments;
	}

	private static final WordDetector IDENT_DETECTOR = new CocoIdentDetector();
	private static final CocoWhitespaceDetector WHITESPACE_DETECTOR = new CocoWhitespaceDetector();

	/** The keywords that are used to recognise the sub Partitions of the Scanner- and Parser-Specification. */
	private static final String COMPILER_KEYWORD = "COMPILER";
	private static final String IGNORECASE_KEYWORD = "IGNORECASE";
	private static final String CHARACTERS_KEYWORD = "CHARACTERS";
	private static final String TOKENS_KEYWORD = "TOKENS";
	private static final String PRAGMAS_KEYWORD = "PRAGMAS";
	private static final String COMMENTS_KEYWORD = "COMMENTS";
	private static final String IGNORE_KEYWORD = "IGNORE";
	private static final String DEFAULT_KEYWORD = "PRODUCTIONS";
	//Note: a Production doesn't have a keyword, and thus is never actually read in the document.
	//Productions are sub partitions of default and start after the character '='
	//and end after the character "."
	//But the keyword is needed to change between DEFAULT and PRODUCTIONS so I a DUMMY is used
	private static final String PRODUCTIONS_KEYWORD = "DUMMY";
	
	
	/** List containing the keyword that finish the Parse_Code Partition*/
	private static final List<String> PARSER_CODE_END_KEYWORDS = Arrays.asList(
			"IGNORECASE", "CHARACTERS", "TOKENS", "PRAGMAS", "COMMENTS", "IGNORE", "PRODUCTIONS");
	
	/** List containing the keyword that finish the IGNORECASE Partition*/
	private static final List<String> IGNORECASE_END_KEYWORDS = Arrays.asList(
			"CHARACTERS", "TOKENS", "PRAGMAS", "COMMENTS", "IGNORE", "PRODUCTIONS");
	
	/** List containing the keyword that finish the CHARACTERS Partition*/
	private static final List<String> CHARACTERS_END_KEYWORDS = Arrays.asList(
			"TOKENS", "PRAGMAS", "COMMENTS", "IGNORE", "PRODUCTIONS");
	
	/** List containing the keyword that finish the TOKENS Partition*/
	private static final List<String> TOKENS_END_KEYWORDS = Arrays.asList(
			"PRAGMAS", "COMMENTS", "IGNORE", "PRODUCTIONS");
	
	/** List containing the keyword that finish the PRAGMAS Partition*/
	private static final List<String> PRAGMAS_END_KEYWORDS = Arrays.asList(
			"COMMENTS", "IGNORE", "PRODUCTIONS");
	
	/** List containing the keyword that finish the COMMENTS Partition*/
	private static final List<String> COMMENTS_END_KEYWORDS = Arrays.asList(
			"COMMENTS", "IGNORE", "PRODUCTIONS");
	
	/** List containing the keyword that finish the IGNORE Partition*/
	private static final List<String> IGNORE_END_KEYWORDS = Arrays.asList(
			"PRODUCTIONS");
	
	/** represents the current main partition*/
	private String currentPartitionKeyword;
	
	public FastATGPartitionScanner() {
	}

	public FastATGPartitionScanner(String[] legalLineDelimiters, boolean nestedMultiLineComments) {
		this();
		this.fNestedMultiLineComments = nestedMultiLineComments;
	}

	/*
	 * @see IPartitionTokenScanner#setPartialRange(IDocument, int, int, String, int)
	 */
	public void setPartialRange(String document, int offset, int length, String contentType, int partitionOffset, 
			String[] legalLineDelimiters, String prevContentType) {
		fDocument = document;
		fLegalLineDelimiters = legalLineDelimiters;
		fRangeOffset = offset;
		fRangeLength = length;
		fScanner.setRange(document, legalLineDelimiters, offset, length);
		fTokenOffset = partitionOffset;
		fTokenLength = 0;
		fPrefixLength = offset - partitionOffset;
		fLast = Last.NONE;

		if (offset == partitionOffset) {
			// restart at beginning of partition
			fState = getState(contentType);

			// determine correct state by looking at the preceding partition
			if (fState == State.DEFAULT && partitionOffset >= 0) {
				try {
					fState = getState(prevContentType);
				} catch (Exception e) {
				}
			}
			fState = getNextState();
		} else {
			fState = getState(contentType);

			// start scanning in the IMPORTS state.
			if (fState == State.DEFAULT && offset == 0 && partitionOffset == -1) {
				fState = State.IMPORTS;
				fTokenOffset = fPrefixLength = 0; // we can't place a token at offset -1
			}
		}
	}

	private State getState(String contentType) {
		if (contentType == null)
			return State.DEFAULT;
		else if (isLegalContentType(contentType))
			return State.fromContentType(contentType);
		else
			return State.DEFAULT;
	}
	
	public CocoToken nextTokenImpl() {
		fTokenOffset += fTokenLength;
		fTokenLength = fPrefixLength;
		
		boolean skipEolHandlingOnce = false;

		while (true) {
			switch (fState) {
			case INLINE_CODE_START:
			case PRAGMAS_INLINE_CODE_START:
			case PRODUCTIONS_INLINE_CODE_START:
				// fall-through
			case INLINE_CODE_END:
			case PRAGMAS_INLINE_CODE_END:
			case PRODUCTIONS_INLINE_CODE_END:
				//fState = getNextState();
				return postFix(fState, getNextState(), 0);
				
			}
			lch=ch;
			ch = fScanner.read();

			// characters
			switch (ch) {
			case BufferedDocumentScanner.EOF:
				if (fTokenLength > 0) {
					fLast = Last.NONE; // ignore last
					return preFix(fState, State.DEFAULT, Last.NONE, 0);
				} else {
					fLast = Last.NONE;
					fPrefixLength = 0;
					return TokenImpl.EOF;
				}

			case '\r':
				if (skipEolHandlingOnce) {
					skipEolHandlingOnce = false;
					break;
				}
				if (fLast != Last.CARRIAGE_RETURN) {
					fLast = Last.CARRIAGE_RETURN;
					fTokenLength++;
					continue;
				} else {
					switch (fState) {
					case SINGLE_LINE_COMMENT:
					case CHARACTER:
					case STRING:
					case SINGLE_LINE_COMMENT_IMPORTS:
					case CHARACTER_IMPORTS:
					case STRING_IMPORTS:
					case SINGLE_LINE_COMMENT_PARSER_CODE:
					case CHARACTER_PARSER_CODE:
					case STRING_PARSER_CODE:
					case SINGLE_LINE_COMMENT_INLINE_CODE:
					case CHARACTER_INLINE_CODE:
					case STRING_INLINE_CODE:
					case SINGLE_LINE_COMMENT_COMPILER_IDENT:
					
					case SINGLE_LINE_COMMENT_IGNORECASE_SEGMENT:
					case CHARACTER_IGNORECASE_SEGMENT:
					case STRING_IGNORECASE_SEGMENT:
						
					case SINGLE_LINE_COMMENT_CHARACTERS_SEGMENT:
					case CHARACTER_CHARACTERS_SEGMENT:
					case STRING_CHARACTERS_SEGMENT:
						
					case SINGLE_LINE_COMMENT_TOKENS_SEGMENT:
					case CHARACTER_TOKENS_SEGMENT:
					case STRING_TOKENS_SEGMENT:
						
					case SINGLE_LINE_COMMENT_PRAGMAS_SEGMENT:
					case CHARACTER_PRAGMAS_SEGMENT:
					case STRING_PRAGMAS_SEGMENT:
						
					case SINGLE_LINE_COMMENT_COMMENTS_SEGMENT:
					case CHARACTER_COMMENTS_SEGMENT:
					case STRING_COMMENTS_SEGMENT:
						
					case SINGLE_LINE_COMMENT_IGNORE_SEGMENT:
					case CHARACTER_IGNORE_SEGMENT:
					case STRING_IGNORE_SEGMENT:
						
					case SINGLE_LINE_COMMENT_PRODUCTIONS_SEGMENT:
					case CHARACTER_PRODUCTIONS_SEGMENT:
					case STRING_PRODUCTIONS_SEGMENT:
					
					case SINGLE_LINE_COMMENT_PRAGMAS_INLINE_CODE:
					case CHARACTER_PRAGMAS_INLINE_CODE:
					case STRING_PRAGMAS_INLINE_CODE:
						
					case SINGLE_LINE_COMMENT_PRODUCTIONS_INLINE_CODE:
					case CHARACTER_PRODUCTIONS_INLINE_CODE:
					case STRING_PRODUCTIONS_INLINE_CODE:
						
						if (fTokenLength > 0) {
							CocoToken token = fState.getToken();

							fLast = Last.CARRIAGE_RETURN;
							fPrefixLength = 1;

							fState = getNextState();
							return token;
						} else {
							consume();
							continue;
						}

					default:
						consume();
						continue;
					}
				}

			case '\n':
				if (skipEolHandlingOnce) {
					skipEolHandlingOnce = false;
					break;
				}
				switch (fState) {
				
				case CHARACTER:
				case STRING:
		
				case CHARACTER_IMPORTS:
				case STRING_IMPORTS:

				case CHARACTER_PARSER_CODE:
				case STRING_PARSER_CODE:
			
				case CHARACTER_INLINE_CODE:
				case STRING_INLINE_CODE:
						
				case CHARACTER_IGNORECASE_SEGMENT:
				case STRING_IGNORECASE_SEGMENT:
					
				case CHARACTER_CHARACTERS_SEGMENT:
				case STRING_CHARACTERS_SEGMENT:
					
				case CHARACTER_TOKENS_SEGMENT:
				case STRING_TOKENS_SEGMENT:					
				
				case CHARACTER_PRAGMAS_SEGMENT:
				case STRING_PRAGMAS_SEGMENT:					
				
				case CHARACTER_COMMENTS_SEGMENT:
				case STRING_COMMENTS_SEGMENT:					
				
				case CHARACTER_IGNORE_SEGMENT:
				case STRING_IGNORE_SEGMENT:					
				
				case CHARACTER_PRODUCTIONS_SEGMENT:
				case STRING_PRODUCTIONS_SEGMENT:					
				
				case CHARACTER_PRAGMAS_INLINE_CODE:
				case STRING_PRAGMAS_INLINE_CODE:	
					
				case CHARACTER_PRODUCTIONS_INLINE_CODE:
				case STRING_PRODUCTIONS_INLINE_CODE:
					
					return postFix(fState, getNextState());

				case SINGLE_LINE_COMMENT:
				
				case SINGLE_LINE_COMMENT_IGNORECASE_SEGMENT:
				case SINGLE_LINE_COMMENT_CHARACTERS_SEGMENT:
				case SINGLE_LINE_COMMENT_TOKENS_SEGMENT:
				case SINGLE_LINE_COMMENT_PRAGMAS_SEGMENT:
				case SINGLE_LINE_COMMENT_COMMENTS_SEGMENT:
				case SINGLE_LINE_COMMENT_IGNORE_SEGMENT:
				case SINGLE_LINE_COMMENT_PRODUCTIONS_SEGMENT:
				
				case SINGLE_LINE_COMMENT_PRAGMAS_INLINE_CODE:
				case SINGLE_LINE_COMMENT_PRODUCTIONS_INLINE_CODE:
					
				case SINGLE_LINE_COMMENT_IMPORTS:
				case SINGLE_LINE_COMMENT_PARSER_CODE:
				case SINGLE_LINE_COMMENT_INLINE_CODE:
				case SINGLE_LINE_COMMENT_COMPILER_IDENT:
					
					return postFix(fState, getNextState());
					
				default:
					consume();
					continue;
				}

			default:
				if (skipEolHandlingOnce) {
					skipEolHandlingOnce = false;
					break;
				}
				if (fLast == Last.CARRIAGE_RETURN) {
					switch (fState) {
					case SINGLE_LINE_COMMENT:
					case CHARACTER:
					case STRING:
					case SINGLE_LINE_COMMENT_IMPORTS:
					case CHARACTER_IMPORTS:
					case STRING_IMPORTS:
					case SINGLE_LINE_COMMENT_PARSER_CODE:
					case CHARACTER_PARSER_CODE:
					case STRING_PARSER_CODE:
					case SINGLE_LINE_COMMENT_INLINE_CODE:
					case CHARACTER_INLINE_CODE:
					case STRING_INLINE_CODE:
					case SINGLE_LINE_COMMENT_COMPILER_IDENT:
						
					case SINGLE_LINE_COMMENT_IGNORECASE_SEGMENT:
					case CHARACTER_IGNORECASE_SEGMENT:
					case STRING_IGNORECASE_SEGMENT:
						
					case SINGLE_LINE_COMMENT_CHARACTERS_SEGMENT:
					case CHARACTER_CHARACTERS_SEGMENT:
					case STRING_CHARACTERS_SEGMENT:
						
					case SINGLE_LINE_COMMENT_TOKENS_SEGMENT:
					case CHARACTER_TOKENS_SEGMENT:
					case STRING_TOKENS_SEGMENT:
						
					case SINGLE_LINE_COMMENT_PRAGMAS_SEGMENT:
					case CHARACTER_PRAGMAS_SEGMENT:
					case STRING_PRAGMAS_SEGMENT:
						
					case SINGLE_LINE_COMMENT_COMMENTS_SEGMENT:
					case CHARACTER_COMMENTS_SEGMENT:
					case STRING_COMMENTS_SEGMENT:
						
					case SINGLE_LINE_COMMENT_IGNORE_SEGMENT:
					case CHARACTER_IGNORE_SEGMENT:
					case STRING_IGNORE_SEGMENT:
						
					case SINGLE_LINE_COMMENT_PRODUCTIONS_SEGMENT:
					case CHARACTER_PRODUCTIONS_SEGMENT:
					case STRING_PRODUCTIONS_SEGMENT:
						
					case SINGLE_LINE_COMMENT_PRAGMAS_INLINE_CODE:
					case CHARACTER_PRAGMAS_INLINE_CODE:
					case STRING_PRAGMAS_INLINE_CODE:
						
					case SINGLE_LINE_COMMENT_PRODUCTIONS_INLINE_CODE:
					case CHARACTER_PRODUCTIONS_INLINE_CODE:
					case STRING_PRODUCTIONS_INLINE_CODE:
						
						Last last;
						State newState = getNextState();
						switch (ch) {
						case '/':
							last = Last.SLASH;
							break;

						case '*':
							last = Last.STAR;
							break;

						case '\'':
							last = Last.NONE;
							newState = getContextualizedState(State.CHARACTER);
							break;

						case '"':
							last = Last.NONE;
							newState = getContextualizedState(State.STRING);
							break;

						case '\r':
							last = Last.CARRIAGE_RETURN;
							break;

						case '\\':
							last = Last.BACKSLASH;
							break;

						default:
							last = Last.NONE;
							break;
						}

						fLast = Last.NONE; // ignore fLast
						return preFix(fState, newState, last, 1);

					default:
						break;
					}
				}
			}

			// states
			switch (fState) {
			case COMPILER_KEYWORD:
				final int keywordLength = COMPILER_KEYWORD.length();
				fScanner.unread(); // unread character after COMPILER
				if (fPrefixLength == keywordLength) {
					return postFix(fState, getNextState(), 0);
				} else if (fPrefixLength < keywordLength) {
					while (fPrefixLength-- > 0) {
						fScanner.unread();
						fTokenLength--;
					}
					if (tryCompilerKeyword(fScanner.read()) > 0) {
						fTokenLength += keywordLength; // consume
						return postFix(fState, getNextState(), 0);
					} else {
						// COMPILER keyword broken
						fScanner.unread();
						fState = State.IMPORTS;
						continue;
					}
				} else { // fPrefixLength > keywordLength
					while (fPrefixLength-- > keywordLength) {
						fScanner.unread();
						fTokenLength--;
					}
					return postFix(fState, getNextState(), 0);
				}
				//break; //unreachable code
			case COMPILER_IDENT:
				// Comments
				switch (ch) {
				case '/':
					if (fLast == Last.SLASH) {
						if (fTokenLength - fLast.getLength() > 0) {
							return preFix(fState, getContextualizedState(State.SINGLE_LINE_COMMENT), Last.NONE, 2);
						} else {
							preFix(fState, getContextualizedState(State.SINGLE_LINE_COMMENT), Last.NONE, 2);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							continue;
						}

					} else {
						fTokenLength++;
						fLast = Last.SLASH;
						skipEolHandlingOnce = true;
						continue;
					}
				case '*':
					if (fLast == Last.SLASH) {
						if (fTokenLength - fLast.getLength() > 0)
							return preFix(fState, getContextualizedState(State.MULTI_LINE_COMMENT), Last.SLASH_STAR, 2);
						else {
							preFix(fState, getContextualizedState(State.MULTI_LINE_COMMENT), Last.SLASH_STAR, 2);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							continue;
						}
					} // else: fall-through (invalid *)
				default:
					if (fLast == Last.SLASH) { // this slash didn't start a comment (but was consumed so undo that)
						// unconsume
						fTokenLength--;
						fLast = Last.NONE;
					} else if (WHITESPACE_DETECTOR.isWhitespace((char) ch)) {
						consume();
						continue;
					} else if (consumeIdent(ch)) {
						return postFix(fState, getNextState(), 0);
					}

					// no ident was read
					if (fTokenLength > 0) {
						// no ident! -> terminate ident partition (containing only whitespace and/or comments)
						fScanner.unread();
						return postFix(fState, getNextState(), 0);
					} else {
						// no ident! -> skip empty ident partition and advance to the next state
						fScanner.unread();
						postFix(fState, getNextState(), 0);
						continue;
					}
				}
				//break; //unreachable code
			case IMPORTS:
				if (fState == State.IMPORTS && IDENT_DETECTOR.isWordStart((char) ch)) { // prefix
					int len;
					if ((len = tryCompilerKeyword(ch)) > 0) {
						fLast = Last.NONE; // ignore fLast
						if (fTokenLength > 0) {
							return preFix(fState, State.COMPILER_KEYWORD, Last.COMPILER, len);
						} else {
							preFix(fState, State.COMPILER_KEYWORD, Last.COMPILER, len);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							continue;
						}
					} else {
						consume();
						break;
					}
				}
				// fall-through
			case PARSER_CODE:
				if (fState == State.PARSER_CODE && IDENT_DETECTOR.isWordStart((char) ch)) { // prefix
					int len;
					if ((len = tryParserCodeEndKeyword(ch)) > 0) {
						fLast = Last.NONE; // ignore fLast
						if (fTokenLength > 0) {
							//return preFix(fState, State.IGNORECASE_SEGMENT, Last.NONE, len);
							return changeState(fState, Last.NONE, len, ch);
						} else {
							//preFix(fState, State.IGNORECASE_SEGMENT, Last.NONE, len);
							changeState(fState, Last.NONE, len, ch);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;//=continue;
						}
					} else { // optional block
						consume();
						break;//=continue;
					}
				}
				// fall-through
			case IGNORECASE_SEGMENT:
				if (fState == State.IGNORECASE_SEGMENT && IDENT_DETECTOR.isWordStart((char) ch)) { // prefix
					int len;
					if ((len = tryIgnorecaseSegmentEndKeyword(ch)) > 0) {
						fLast = Last.NONE; // ignore fLast
						if (fTokenLength > 0) {
							//return preFix(fState, State.CHARACTERS_SEGMENT, Last.NONE, len);
							return changeState(fState, Last.NONE, len, ch);
						} else {
							//preFix(fState, State.CHARACTERS_SEGMENT, Last.NONE, len);
							changeState(fState, Last.NONE, len, ch);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;//=continue;
						}
					} else { // optional block
						consume();
						break;//=continue;
					}
				}
				// fall-through
			case CHARACTERS_SEGMENT:
				if (fState == State.CHARACTERS_SEGMENT && IDENT_DETECTOR.isWordStart((char) ch)) { // prefix
					int len;
					if ((len = tryCharactersSegmentEndKeyword(ch)) > 0) {
						fLast = Last.NONE; // ignore fLast
						if (fTokenLength > 0) {
							//return preFix(fState, State.TOKENS_SEGMENT, Last.NONE, len);
							return changeState(fState, Last.NONE, len, ch);
						} else {
							//preFix(fState, State.TOKENS_SEGMENT, Last.NONE, len);
							changeState(fState, Last.NONE, len, ch);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;//=continue;
						}
					} else { // optional block
						consume();
						break;//=continue;
					}
				}
				// fall-through
			case TOKENS_SEGMENT:
				if (fState == State.TOKENS_SEGMENT && IDENT_DETECTOR.isWordStart((char) ch)) { // prefix
					int len;
					if ((len = tryTokensSegmentEndKeyword(ch)) > 0) {
						fLast = Last.NONE; // ignore fLast
						if (fTokenLength > 0) {
							//return preFix(fState, State.PRAGMAS_SEGMENT, Last.NONE, len);
							return changeState(fState, Last.NONE, len, ch);
						} else {
							//preFix(fState, State.PRAGMAS_SEGMENT, Last.NONE, len);
							changeState(fState, Last.NONE, len, ch);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;//=continue;
						}
					} else { // optional block
						consume();
						break;//=continue;
					}
				}
				// fall-through
			case PRAGMAS_SEGMENT:
				if (fState == State.PRAGMAS_SEGMENT ) { // prefix
					
					switch(ch){
					
					case '(':
						fTokenLength++;
						fLast = Last.LPAR;
						continue;
					
					case '.':
						if (fLast == Last.LPAR) {
							if (fTokenLength - fLast.getLength() > 0)
								return preFix(fState, State.PRAGMAS_INLINE_CODE_START, Last.NONE, 2);
							else {
								preFix(fState, State.PRAGMAS_INLINE_CODE_START, Last.NONE, 2);
								fTokenOffset += fTokenLength;
								fTokenLength = fPrefixLength;
								continue;
							}
						} else {
							consume();
							continue;
						}
					
					
					}				
					
					if(IDENT_DETECTOR.isWordStart((char) ch)){
					
						int len;
						if ((len = tryPragmasSegmentEndKeyword(ch)) > 0) {
							fLast = Last.NONE; // ignore fLast
							if (fTokenLength > 0) {
								//return preFix(fState, State.COMMENTS_SEGMENT, Last.NONE, len);
								return changeState(fState, Last.NONE, len, ch);
							} else {
								//preFix(fState, State.COMMENTS_SEGMENT, Last.NONE, len);
								changeState(fState, Last.NONE, len, ch);
								fTokenOffset += fTokenLength;
								fTokenLength = fPrefixLength;
								break;//=continue;
							}
						} else { // optional block
							
							consume();
							break;//=continue;							
						}
					}
				}
				// fall-through
					
					
					
			case COMMENTS_SEGMENT:
				if (fState == State.COMMENTS_SEGMENT && IDENT_DETECTOR.isWordStart((char) ch)) { // prefix
					int len;
					if ((len = tryCommentsSegmentEndKeyword(ch)) > 0) {
						fLast = Last.NONE; // ignore fLast
						if (fTokenLength > 0) {
							//return preFix(fState, State.IGNORE_SEGMENT, Last.NONE, len);
							return changeState(fState, Last.NONE, len, ch);
						} else {
							//preFix(fState, State.IGNORE_SEGMENT, Last.NONE, len);
							changeState(fState, Last.NONE, len, ch);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;//=continue;
						}
					} else { // optional block
						consume();
						break;//=continue;
					}
				}
				// fall-through
			case IGNORE_SEGMENT:
				if (fState == State.IGNORE_SEGMENT && IDENT_DETECTOR.isWordStart((char) ch)) { // prefix
					int len;
					if ((len = tryIgnoreSegmentEndKeyword(ch)) > 0) {
						fLast = Last.NONE; // ignore fLast
						if (fTokenLength > 0) {
							//return preFix(fState, State.DEFAULT, Last.NONE, len);
							return changeState(fState, Last.NONE, len, ch);
						} else {
							//preFix(fState, State.DEFAULT, Last.NONE, len);
							changeState(fState, Last.NONE, len, ch);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;//=continue;
						}
					} else { // optional block
						consume();
						break;//=continue;
					}
				}
				// fall-through
			case PRODUCTIONS_SEGMENT:
				if (fState == State.PRODUCTIONS_SEGMENT) {
					switch (ch) {
					case '(':
						fTokenLength++;
						fLast = Last.LPAR;
						continue;

					case '.':
						if (fLast == Last.LPAR) {
							if (fTokenLength - fLast.getLength() > 0){
								return preFix(fState, State.PRODUCTIONS_INLINE_CODE_START, Last.NONE, 2);
							}
							else {
								preFix(fState, State.PRODUCTIONS_INLINE_CODE_START, Last.NONE, 2);
								fTokenOffset += fTokenLength;
								fTokenLength = fPrefixLength;
								continue;
							}
						} else if ((lch < 'a' || lch > 'z' ) && (lch < 'A' || lch > 'Z')) {
							currentPartitionKeyword = DEFAULT_KEYWORD;
							//return preFix(fState, State.DEFAULT, Last.NONE, 1);
							return changeState(fState, Last.NONE, 1, ch);
						} else{
							int next = fScanner.read();
							fScanner.unread();
							if(next==' '||next=='\n'||next == '\r'||next=='\t'||next=='('){
								currentPartitionKeyword = DEFAULT_KEYWORD;
								//return preFix(fState, State.DEFAULT, Last.NONE, 1);
								return changeState(fState, Last.NONE, 1, ch);
							}
							consume();
							continue;
						}
					}
				}
			case DEFAULT:
				if (fState == State.DEFAULT) {
					switch (ch) {
					case '=':
						currentPartitionKeyword = PRODUCTIONS_KEYWORD;
						//return preFix(fState, State.PRODUCTIONS_SEGMENT, Last.NONE, 1);
						return changeState(fState, Last.NONE, 1, ch);
					case '(':
						fTokenLength++;
						fLast = Last.LPAR;
						continue;

					case '.':
						if (fLast == Last.LPAR) {
							if (fTokenLength - fLast.getLength() > 0)
								return preFix(fState, State.INLINE_CODE_START, Last.NONE, 2);
							else {
								preFix(fState, State.INLINE_CODE_START, Last.NONE, 2);
								fTokenOffset += fTokenLength;
								fTokenLength = fPrefixLength;
								continue;
							}
						} else {
							consume();
							continue;
						}
					}
				}
			case INLINE_CODE:
				if (fState == State.INLINE_CODE) {
					switch (ch) {
					case '.':
						fTokenLength++;
						fLast = Last.DOT;
						continue;

					case ')':
						if (fLast == Last.DOT) {
							if (fTokenLength - fLast.getLength() > 0)
								return preFix(fState, State.INLINE_CODE_END, Last.NONE, 2);
							else {
								preFix(fState, State.INLINE_CODE_END, Last.NONE, 2);
								fTokenOffset += fTokenLength;
								fTokenLength = fPrefixLength;
								continue;
							}
						} else {
							consume();
							continue;
						}
					}
				}
			
			case PRAGMAS_INLINE_CODE:
				if (fState == State.PRAGMAS_INLINE_CODE) {
					switch (ch) {
					case '.':
						fTokenLength++;
						fLast = Last.DOT;
						continue;

					case ')':
						if (fLast == Last.DOT) {
							if (fTokenLength - fLast.getLength() > 0)
								return preFix(fState, State.PRAGMAS_INLINE_CODE_END, Last.NONE, 2);
							else {
								preFix(fState, State.PRAGMAS_INLINE_CODE_END, Last.NONE, 2);
								fTokenOffset += fTokenLength;
								fTokenLength = fPrefixLength;
								continue;
							}
						} else {
							consume();
							continue;
						}
					default:
						fLast = Last.NONE;
					}
				}
	
				
			case PRODUCTIONS_INLINE_CODE:
				if (fState == State.PRODUCTIONS_INLINE_CODE) {
					switch (ch) {
					case '.':
						fTokenLength++;
						fLast = Last.DOT;
						continue;

					case ')':
						if (fLast == Last.DOT) {
							if (fTokenLength - fLast.getLength() > 0)
								return preFix(fState, State.PRODUCTIONS_INLINE_CODE_END, Last.NONE, 2);
							else {
								preFix(fState, State.PRODUCTIONS_INLINE_CODE_END, Last.NONE, 2);
								fTokenOffset += fTokenLength;
								fTokenLength = fPrefixLength;
								continue;
							}
						} else {
							consume();
							continue;
						}
					}
				}

				// common code
				switch (ch) {
				case '/':
					if (fLast == Last.SLASH) {
						if (fTokenLength - fLast.getLength() > 0) {
							return preFix(fState, getContextualizedState(State.SINGLE_LINE_COMMENT), Last.NONE, 2);
						} else {
							preFix(fState, getContextualizedState(State.SINGLE_LINE_COMMENT), Last.NONE, 2);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;
						}

					} else {
						fTokenLength++;
						fLast = Last.SLASH;
						break;
					}

				case '*':
					if (fLast == Last.SLASH) {
						if (fTokenLength - fLast.getLength() > 0)
							return preFix(fState, getContextualizedState(State.MULTI_LINE_COMMENT), Last.SLASH_STAR, 2);
						else {
							preFix(fState, getContextualizedState(State.MULTI_LINE_COMMENT), Last.SLASH_STAR, 2);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;
						}

					} else {
						consume();
						break;
					}

				case '\'':
					fLast = Last.NONE; // ignore fLast
					if (fTokenLength > 0)
						return preFix(fState, getContextualizedState(State.CHARACTER), Last.NONE, 1);
					else {
						preFix(fState, getContextualizedState(State.CHARACTER), Last.NONE, 1);
						fTokenOffset += fTokenLength;
						fTokenLength = fPrefixLength;
						break;
					}

				case '"':
					fLast = Last.NONE; // ignore fLast
					if (fTokenLength > 0)
						return preFix(fState, getContextualizedState(State.STRING), Last.NONE, 1);
					else {
						preFix(fState, getContextualizedState(State.STRING), Last.NONE, 1);
						fTokenOffset += fTokenLength;
						fTokenLength = fPrefixLength;
						break;
					}

				default:
					consume();
					break;
				}
				break;

			case SINGLE_LINE_COMMENT:
			case SINGLE_LINE_COMMENT_IMPORTS:
			case SINGLE_LINE_COMMENT_PARSER_CODE:
			case SINGLE_LINE_COMMENT_INLINE_CODE:
			case SINGLE_LINE_COMMENT_COMPILER_IDENT:
			case SINGLE_LINE_COMMENT_IGNORECASE_SEGMENT:
			case SINGLE_LINE_COMMENT_CHARACTERS_SEGMENT:
			case SINGLE_LINE_COMMENT_TOKENS_SEGMENT:
			case SINGLE_LINE_COMMENT_PRAGMAS_SEGMENT:
			case SINGLE_LINE_COMMENT_COMMENTS_SEGMENT:
			case SINGLE_LINE_COMMENT_IGNORE_SEGMENT:
			case SINGLE_LINE_COMMENT_PRODUCTIONS_SEGMENT:
			
			case SINGLE_LINE_COMMENT_PRAGMAS_INLINE_CODE:
			case SINGLE_LINE_COMMENT_PRODUCTIONS_INLINE_CODE:
				consume();
				break;

			case MULTI_LINE_COMMENT:
			case MULTI_LINE_COMMENT_IMPORTS:
			case MULTI_LINE_COMMENT_PARSER_CODE:
			case MULTI_LINE_COMMENT_INLINE_CODE:
			case MULTI_LINE_COMMENT_COMPILER_IDENT:
			case MULTI_LINE_COMMENT_IGNORECASE_SEGMENT:
			case MULTI_LINE_COMMENT_CHARACTERS_SEGMENT:
			case MULTI_LINE_COMMENT_TOKENS_SEGMENT:
			case MULTI_LINE_COMMENT_PRAGMAS_SEGMENT:
			case MULTI_LINE_COMMENT_COMMENTS_SEGMENT:
			case MULTI_LINE_COMMENT_IGNORE_SEGMENT:
			case MULTI_LINE_COMMENT_PRODUCTIONS_SEGMENT:
			
			case MULTI_LINE_COMMENT_PRAGMAS_INLINE_CODE:	
			case MULTI_LINE_COMMENT_PRODUCTIONS_INLINE_CODE:
				if (fNestedMultiLineComments) {
					if (fPrefixLength == 2) {
						readComment1(ch);
					} else {
						if (fPrefixLength > 2) {
							// continued multi line comment -> we need to know where we started (because of nesting!) -> backup to partition start + 2
							int delta = fPrefixLength - 2;
							{
								fScanner.setRange(fDocument, fLegalLineDelimiters, fRangeOffset - delta, fRangeLength + delta);
								fTokenLength = 2;
							}
						} else if (fPrefixLength < 2) {
							// skip "/*"
							if (fPrefixLength < 1) {
								consume();
								fScanner.read();
							}
							consume();
						}
						readComment1(fScanner.read());
					}
					return postFix(fState, getNextState());
				} else {
					switch (ch) {
					case '*':
						fTokenLength++;
						fLast = Last.STAR;
						break;

					case '/':
						if (fLast == Last.STAR) {
							return postFix(fState, getNextState());
						} else {
							consume();
							break;
						}

					default:
						consume();
						break;
					}
					break;
				}
			case STRING:
			case STRING_IMPORTS:
			case STRING_PARSER_CODE:
			case STRING_INLINE_CODE:
			case STRING_IGNORECASE_SEGMENT:
			case STRING_CHARACTERS_SEGMENT:
			case STRING_TOKENS_SEGMENT:
			case STRING_PRAGMAS_SEGMENT:
			case STRING_COMMENTS_SEGMENT:
			case STRING_IGNORE_SEGMENT:
			case STRING_PRODUCTIONS_SEGMENT:
				
			case STRING_PRAGMAS_INLINE_CODE:
			case STRING_PRODUCTIONS_INLINE_CODE:
				switch (ch) {
				case '\\':
					fLast = (fLast == Last.BACKSLASH) ? Last.NONE : Last.BACKSLASH;
					fTokenLength++;
					break;

				case '\"':
					if (fLast != Last.BACKSLASH) {
						return postFix(fState, getNextState());

					} else {
						consume();
						break;
					}

				default:
					consume();
					break;
				}
				break;

			case CHARACTER:
			case CHARACTER_IMPORTS:
			case CHARACTER_PARSER_CODE:
			case CHARACTER_INLINE_CODE:
			case CHARACTER_IGNORECASE_SEGMENT:
			case CHARACTER_CHARACTERS_SEGMENT:
			case CHARACTER_TOKENS_SEGMENT:
			case CHARACTER_PRAGMAS_SEGMENT:
			case CHARACTER_COMMENTS_SEGMENT:
			case CHARACTER_IGNORE_SEGMENT:
			case CHARACTER_PRODUCTIONS_SEGMENT:
				
			case CHARACTER_PRAGMAS_INLINE_CODE:
			case CHARACTER_PRODUCTIONS_INLINE_CODE:
				switch (ch) {
				case '\\':
					fLast = (fLast == Last.BACKSLASH) ? Last.NONE : Last.BACKSLASH;
					fTokenLength++;
					break;

				case '\'':
					if (fLast != Last.BACKSLASH) {
						return postFix(fState, getNextState());

					} else {
						consume();
						break;
					}

				default:
					consume();
					break;
				}
				break;
			}
		}
	}

	private final void consume() {
		fTokenLength++;
		fLast = Last.NONE;
	}

	private final CocoToken postFix(State state, State newState) {
		fTokenLength++;
		fLast = Last.NONE;
		fState = newState;
		fPrefixLength = 0;
		return state.getToken();
	}

	private final CocoToken postFix(State state, State newState, int postfixLength) {
		fTokenLength += postfixLength;
		fLast = Last.NONE;
		fState = newState;
		fPrefixLength = 0;
		return state.getToken();
	}

	private final CocoToken preFix(State state, State newState, Last last, int prefixLength) {
		fTokenLength -= fLast.getLength();
		fPrefixLength = prefixLength;
		fLast = last;
		CocoToken token = state.getToken();
		fState = newState;
		return token;
	}
	
	private final CocoToken changeState(State state, Last last, int prefixLength, int ch){
		String ident = currentPartitionKeyword;
		State newState;
		
		if(ident.equals(IGNORECASE_KEYWORD)){
			newState = State.IGNORECASE_SEGMENT; 
		}
		else if(ident.equals(CHARACTERS_KEYWORD)){
			newState = State.CHARACTERS_SEGMENT; 
		}
		else if(ident.equals(TOKENS_KEYWORD)){
			newState = State.TOKENS_SEGMENT; 
		}
		else if(ident.equals(PRAGMAS_KEYWORD)){
			newState = State.PRAGMAS_SEGMENT; 
		}
		else if(ident.equals(COMMENTS_KEYWORD)){
			newState = State.COMMENTS_SEGMENT; 
		}
		else if(ident.equals(IGNORE_KEYWORD)){
			newState = State.IGNORE_SEGMENT; 
		}
		else if(ident.equals(DEFAULT_KEYWORD)){
			newState = State.DEFAULT; 
		}
		else if(ident.equals(PRODUCTIONS_KEYWORD)){
			//A Production
			//ident == PRODUCTIONS_KEYWORD
			newState = State.PRODUCTIONS_SEGMENT;
		}
		else {
			newState = state.getSuperState();
		}
		currentPartitionKeyword = null;
		return preFix(state, newState, last, prefixLength);
	}
	

	/*
	 * @see ITokenScanner#setRange(IDocument, int, int)
	 */
	public void setRange(String document, int offset, int length, String[] legalLineDelimiters) {
		fScanner.setRange(document, legalLineDelimiters, offset, length);
		fTokenOffset = offset;
		fTokenLength = 0;
		fPrefixLength = 0;
		fLast = Last.NONE;
		fState = State.IMPORTS;

		fDocument = document;
		fLegalLineDelimiters = legalLineDelimiters;
		fRangeOffset = offset;
		fRangeLength = length;
	}

	/*
	 * @see ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		return fTokenLength;
	}

	/*
	 * @see ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		return fTokenOffset;
	}

	/**
	 * Read C-style nested multiline comment.
	 * Precondition: /* is already consumed.
	 * Starting character (ch) is the first character after the /*.
	 * Postcondition: Comment is consumed expect for the last character which
	 * will be consumed by a succeeding postFix() call.
	 */
	private void readComment1(int ch) {
		int level = 1;
		for (;;) {
			if (ch == '*') {
				lch = ch;
				ch = fScanner.read();
				consume();
				if (ch == '/') {
					level--;
					if (level == 0) {
						return;
					} else {
						lch = ch;
						ch = fScanner.read();
						consume();
					}
				}
			} else if (ch == '/') {
				lch = ch;
				ch = fScanner.read();
				consume();
				if (ch == '*') {
					level++;
					lch = ch;
					ch = fScanner.read();
					consume();
				}
			} else if (ch == BufferedDocumentScanner.EOF) {
				fTokenLength--; // unconsume
				return;
			}
			else {
				lch = ch;
				ch = fScanner.read();
				consume();
			}
		}
	}

	/**
	 * Try to read the COMPILER keyword. Doesn't consume in the successful case,
	 * this has to be done by a subsequent preFix() call.
	 * @param ch the first character of the ident
	 * @return the length of the read ident if it matches, 0 otherwise.
	 */
	private int tryCompilerKeyword(int ch) {
		String ident = scanIdent(ch);
		if (ident == null) {
			return 0;
		} else if (ident.equals(COMPILER_KEYWORD)) {
			currentPartitionKeyword = ident;
			return ident.length();
		} else {
			fTokenLength += ident.length() - 1; // leave the last character to consume()
			return 0;
		}
	}

	/**
	 * Try to read a keyword which terminates the compiler code block that
	 * follows the COMPILER keyword. Doesn't consume in the successful case,
	 * this has to be done by a subsequent preFix() call.
	 * @param ch the first character of the ident
	 * @return the length of the read ident if it matches, 0 otherwise.
	 */
	private int tryParserCodeEndKeyword(int ch) {
		String ident = scanIdent(ch);
		if (ident == null) {
			return 0;
		} else if (PARSER_CODE_END_KEYWORDS.contains(ident)) {
			currentPartitionKeyword = ident;
			return ident.length();
		} else {
			fTokenLength += ident.length() - 1; // leave the last character to consume()
			return 0;
		}
	}
	
	/**
	 * Try to read a keyword which terminates the ignorecase segment block that
	 * follows the IGNORECASE keyword. Doesn't consume in the successful case,
	 * this has to be done by a subsequent preFix() call.
	 * @param ch the first character of the ident
	 * @return the length of the read ident if it matches, 0 otherwise.
	 */
	private int tryIgnorecaseSegmentEndKeyword(int ch) {
		String ident = scanIdent(ch);
		if (ident == null) {
			return 0;
		} else if (IGNORECASE_END_KEYWORDS.contains(ident)) {
			currentPartitionKeyword = ident;
			return ident.length();
		} else {
			fTokenLength += ident.length() - 1; // leave the last character to consume()
			return 0;
		}
	}
	
	/**
	 * Try to read a keyword which terminates the character segment block that
	 * follows the CHARACTERS keyword. Doesn't consume in the successful case,
	 * this has to be done by a subsequent preFix() call.
	 * @param ch the first character of the ident
	 * @return the length of the read ident if it matches, 0 otherwise.
	 */
	private int tryCharactersSegmentEndKeyword(int ch) {
		String ident = scanIdent(ch);
		if (ident == null) {
			return 0;
		} else if (CHARACTERS_END_KEYWORDS.contains(ident)) {
			currentPartitionKeyword = ident;
			return ident.length();
		} else {
			fTokenLength += ident.length() - 1; // leave the last character to consume()
			return 0;
		}
	}
	
	/**
	 * Try to read a keyword which terminates the tokens segment block that
	 * follows the TOKENS keyword. Doesn't consume in the successful case,
	 * this has to be done by a subsequent preFix() call.
	 * @param ch the first character of the ident
	 * @return the length of the read ident if it matches, 0 otherwise.
	 */
	private int tryTokensSegmentEndKeyword(int ch) {
		String ident = scanIdent(ch);
		if (ident == null) {
			return 0;
		} else if (TOKENS_END_KEYWORDS.contains(ident)) {
			currentPartitionKeyword = ident;
			return ident.length();
		} else {
			fTokenLength += ident.length() - 1; // leave the last character to consume()
			return 0;
		}
	}
	
	/**
	 * Try to read a keyword which terminates the pragmas segment block that
	 * follows the PRAGMAS keyword. Doesn't consume in the successful case,
	 * this has to be done by a subsequent preFix() call.
	 * @param ch the first character of the ident
	 * @return the length of the read ident if it matches, 0 otherwise.
	 */
	private int tryPragmasSegmentEndKeyword(int ch) {
		String ident = scanIdent(ch);
		if (ident == null) {
			return 0;
		} else if (PRAGMAS_END_KEYWORDS.contains(ident)) {
			currentPartitionKeyword = ident;
			return ident.length();
		} else {
			fTokenLength += ident.length() - 1; // leave the last character to consume()
			return 0;
		}
	}
		
	/**
	 * Try to read a keyword which terminates the comments segment block that
	 * follows the COMMENTS keyword. Doesn't consume in the successful case,
	 * this has to be done by a subsequent preFix() call.
	 * @param ch the first character of the ident
	 * @return the length of the read ident if it matches, 0 otherwise.
	 */
	private int tryCommentsSegmentEndKeyword(int ch) {
		String ident = scanIdent(ch);
		if (ident == null) {
			return 0;
		} else if (COMMENTS_END_KEYWORDS.contains(ident)) {
			currentPartitionKeyword = ident;
			return ident.length();
		} else {
			fTokenLength += ident.length() - 1; // leave the last character to consume()
			return 0;
		}
	}
	

	/**
	 * Try to read a keyword which terminates the ignore segment block that
	 * follows the IGNORE keyword. Doesn't consume in the successful case,
	 * this has to be done by a subsequent preFix() call.
	 * @param ch the first character of the ident
	 * @return the length of the read ident if it matches, 0 otherwise.
	 */
	private int tryIgnoreSegmentEndKeyword(int ch) {
		String ident = scanIdent(ch);
		if (ident == null) {
			return 0;
		} else if (IGNORE_END_KEYWORDS.contains(ident)) {
			currentPartitionKeyword = ident;
			return ident.length();
		} else {
			fTokenLength += ident.length() - 1; // leave the last character to consume()
			return 0;
		}
	}

	/**
	 * Scan an ident string.
	 */
	private String scanIdent(int ch) {
		StringBuffer ident = new StringBuffer();
		final WordDetector wordDetector = IDENT_DETECTOR;
		if (wordDetector.isWordStart((char)ch)) {
			ident.append((char)ch);
			lch = ch;
			ch = fScanner.read();
			while (wordDetector.isWordPart((char)ch)) {
				ident.append((char)ch);
				lch = ch;
				ch = fScanner.read();
			}
			if (ch != BufferedDocumentScanner.EOF) fScanner.unread();
			return ident.toString();
		} else {
			return null;
		}
	}

	/**
	 * Consume an ident string.
	 */
	private boolean consumeIdent(int ch) {
		final WordDetector wordDetector = IDENT_DETECTOR;
		if (wordDetector.isWordStart((char)ch)) {
			consume();
			lch = ch;
			ch = fScanner.read();
			while (wordDetector.isWordPart((char)ch)) {
				consume();
				lch = ch;
				ch = fScanner.read();
			}
			if (ch != BufferedDocumentScanner.EOF) fScanner.unread();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Calculates the next State based on the current State, can only be used for certain a subset of the possible states
	 * @return the next state
	 */
	private State getNextState() {
		switch (fState) {
		case COMPILER_KEYWORD:
			return State.COMPILER_IDENT;
		case COMPILER_IDENT:
			return State.PARSER_CODE;

		case INLINE_CODE_START:
			return State.INLINE_CODE;
		case INLINE_CODE_END:
			return State.DEFAULT;
			
		case PRAGMAS_INLINE_CODE_START:
			return State.PRAGMAS_INLINE_CODE;
		case PRAGMAS_INLINE_CODE_END:
			return State.PRAGMAS_SEGMENT;
			
		case PRODUCTIONS_INLINE_CODE_START:
			return State.PRODUCTIONS_INLINE_CODE;
		case PRODUCTIONS_INLINE_CODE_END:
			return State.PRODUCTIONS_SEGMENT;
		
		default:
			return fState.getSuperState();
		}
	}

	/**
	 * Make the wanted state usable for the current context (i.e. the current
	 * state or its super-state if it is a sub-state).
	 */
	private State getContextualizedState(State wantedState) {
		return wantedState.toContextOf(fState);
	}
}

