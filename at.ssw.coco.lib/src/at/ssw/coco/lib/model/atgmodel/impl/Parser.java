/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
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
 package at.ssw.coco.lib.model.atgmodel.impl;

import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.GROUP_CHARACTERS;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.GROUP_COMMENTS;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.GROUP_IGNORE;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.GROUP_IGNORECASE;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.GROUP_PRAGMAS;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.GROUP_TOKENS;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.ITEM_CHARSET;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.ITEM_COMMENT;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.ITEM_IGNORE;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.ITEM_PRODUCTION;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.ITEM_TOKEN;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.SECTION_CODE;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.SECTION_COMPILER;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.SECTION_IMPORTS;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.SECTION_PRODUCTIONS;
import static at.ssw.coco.lib.model.atgmodel.ATGSegment.Type.SECTION_SCANNER;

import java.io.IOException;

import at.ssw.coco.lib.model.atgmodel.ATGSegment;

/**
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 */


public class Parser {
	public static final int _EOF = 0;
	public static final int _ident = 1;
	public static final int _number = 2;
	public static final int _string = 3;
	public static final int _badString = 4;
	public static final int _char = 5;
	public static final int maxT = 44;
	public static final int _ddtSym = 45;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	public ATGSegment fRoot;

	public ATGSegment getOutline() throws IOException {
		if (fRoot == null) {
			throw new IOException("Run the Parser before fetching its output!");
		}
		return fRoot;
	}

	private static boolean spaceBetween(String left, String right, boolean spaceInSource) {
		if (left.length() == 0 || right.length() == 0) return false;
		char last = left.charAt(left.length()-1);
		char first = right.charAt(0);
		if (last == ',')
			return true;
		else if ((last == '>' || last == ']') && Character.isJavaIdentifierPart(first))
			return true;
		else if (Character.isJavaIdentifierPart(last)
				&& Character.isJavaIdentifierPart(first))
			return spaceInSource;
		else
			return false;
	}

/*-------------------------------------------------------------------------*/



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			if (la.kind == 45) {
			}
			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void Coco() {
		int beg, end;
		ATGSegment imports=null, ignores=null, comments=null;
		ATGSegment settings, productions;
		ATGSegment group, child;
		String str0, attribs;
		
		if (StartOf(1)) {
			Get();
			beg = t.pos; 
			while (StartOf(1)) {
				Get();
			}
			imports = new ATGSegment(SECTION_IMPORTS, beg, la.pos-1); 
		}
		Expect(6);
		beg = t.pos; 
		Expect(1);
		fRoot = new ATGSegment(SECTION_COMPILER, t.val, beg);
		settings = new ATGSegment(SECTION_SCANNER);
		if (imports != null) fRoot.addChild(imports);
		beg = end = la.pos;
		
		while (StartOf(2)) {
			Get();
			end = t.pos + t.val.length(); 
		}
		if (end > beg) fRoot.addChild(new ATGSegment(SECTION_CODE, beg, end)); 
		if (la.kind == 7) {
			Get();
			settings.addChild(new ATGSegment(GROUP_IGNORECASE, t.pos, t.pos + t.val.length())); 
		}
		if (la.kind == 8) {
			Get();
			group = new ATGSegment(GROUP_CHARACTERS, t.pos); 
			while (la.kind == 1) {
				SetDecl(group);
			}
			group.setEndPoint(t.pos + t.val.length()); settings.addChild(group); 
		}
		if (la.kind == 9) {
			Get();
			group = new ATGSegment(GROUP_TOKENS, t.pos); 
			while (la.kind == 1 || la.kind == 3 || la.kind == 5) {
				TokenDecl(group);
			}
			group.setEndPoint(t.pos + t.val.length()); settings.addChild(group); 
		}
		if (la.kind == 10) {
			Get();
			group = new ATGSegment(GROUP_PRAGMAS, t.pos); 
			while (la.kind == 1 || la.kind == 3 || la.kind == 5) {
				TokenDecl(group);
			}
			group.setEndPoint(t.pos + t.val.length()); settings.addChild(group); 
		}
		while (la.kind == 11) {
			Get();
			beg = t.pos;
			if (comments == null) comments = new ATGSegment(GROUP_COMMENTS, beg); 
			Expect(12);
			TokenExpr();
			str0 = "FROM " + t.val +" TO "; 
			Expect(13);
			TokenExpr();
			str0 += t.val; 
			if (la.kind == 14) {
				Get();
				str0 += " (NESTED)"; 
			}
			comments.addChild(new ATGSegment(ITEM_COMMENT, str0, beg, t.pos + t.val.length()));
			comments.setEndPoint(t.pos + t.val.length()); 
		}
		if (comments!=null) settings.addChild(comments); 
		while (la.kind == 15) {
			Get();
			beg = t.pos;
			if (ignores==null) ignores = new ATGSegment(GROUP_IGNORE, t.pos); 
			str0 = Set();
			ignores.setEndPoint(t.pos + t.val.length());
			ignores.addChild(new ATGSegment(ITEM_IGNORE, str0, beg, t.pos + t.val.length())); 
		}
		if (ignores!=null) settings.addChild(ignores);
		settings.setEndPoint(t.pos + t.val.length()); 
		while (!(la.kind == 0 || la.kind == 16)) {SynErr(45); Get();}
		Expect(16);
		fRoot.addChild(settings);
		productions = new ATGSegment(SECTION_PRODUCTIONS, t.pos);
		fRoot.addChild(productions);
		
		while (la.kind == 1) {
			Get();
			beg = t.pos; str0 = t.val; attribs = null; 
			if (la.kind == 24 || la.kind == 29) {
				attribs = AttrDecl();
				attribs = "<" + attribs + ">"; 
			}
			if (la.kind == 42) {
				SemText();
			}
			ExpectWeak(17, 3);
			Expression();
			ExpectWeak(18, 4);
			child = new ATGSegment(ITEM_PRODUCTION, str0, beg, t.pos + t.val.length());
			if (attribs != null) child.setAttributes(attribs);
			productions.addChild(child);
			
		}
		productions.setEndPoint(t.pos + t.val.length()); 
		Expect(19);
		Expect(1);
		Expect(18);
		fRoot.setEndPoint(t.pos); 
	}

	void SetDecl(ATGSegment parent) {
		ATGSegment child; String s = null; assert s == null; 
		Expect(1);
		child = new ATGSegment(ITEM_CHARSET, t.val, t.pos); 
		Expect(17);
		s = Set();
		child.setEndPoint(t.pos + t.val.length()); parent.addChild(child); 
		Expect(18);
	}

	void TokenDecl(ATGSegment parent) {
		ATGSegment child; 
		Sym();
		child = new ATGSegment(ITEM_TOKEN, t.val, t.pos); 
		while (!(StartOf(5))) {SynErr(46); Get();}
		if (la.kind == 17) {
			Get();
			TokenExpr();
			Expect(18);
		} else if (StartOf(6)) {
		} else SynErr(47);
		if (la.kind == 42) {
			SemText();
		}
		child.setEndPoint(t.pos + t.val.length()); parent.addChild(child); 
	}

	void TokenExpr() {
		TokenTerm();
		while (WeakSeparator(33,7,8) ) {
			TokenTerm();
		}
	}

	String  Set() {
		String  s;
		String s2; 
		s = SimSet();
		while (la.kind == 20 || la.kind == 21) {
			if (la.kind == 20) {
				Get();
				s2 = SimSet();
				s += " + " + s2; 
			} else {
				Get();
				s2 = SimSet();
				s += " - " + s2; 
			}
		}
		return s;
	}

	String  AttrDecl() {
		String  s;
		int beg; s = ""; int end = 0; 
		if (la.kind == 24) {
			Get();
			if (la.kind == 25 || la.kind == 26) {
				if (la.kind == 25) {
					Get();
				} else {
					Get();
				}
				s += ("^".equals(t.val) ? "out" : t.val) + " "; beg = la.pos; 
				TypeName();
				s += scanner.buffer.GetString(beg, t.pos + t.val.length()) + " "; 
				Expect(1);
				s += t.val; 
				if (la.kind == 27) {
					Get();
				} else if (la.kind == 28) {
					Get();
					s += t.val; 
					while (StartOf(9)) {
						if (StartOf(10)) {
							Get();
							s += (spaceBetween(s, t.val, end != t.pos) ? " " : "") + t.val; end = t.pos+t.val.length(); 
						} else {
							Get();
						}
					}
					Expect(27);
				} else SynErr(48);
			} else if (StartOf(11)) {
				while (StartOf(12)) {
					if (StartOf(13)) {
						Get();
						s += (spaceBetween(s, t.val, end != t.pos) ? " " : "") + t.val; end = t.pos+t.val.length(); 
					} else {
						Get();
					}
				}
				Expect(27);
			} else SynErr(49);
		} else if (la.kind == 29) {
			Get();
			if (la.kind == 25 || la.kind == 26) {
				if (la.kind == 25) {
					Get();
				} else {
					Get();
				}
				s += ("^".equals(t.val) ? "out" : t.val) + " "; beg = la.pos; 
				TypeName();
				s += scanner.buffer.GetString(beg, t.pos + t.val.length()) + " "; 
				Expect(1);
				s += t.val; 
				if (la.kind == 30) {
					Get();
				} else if (la.kind == 28) {
					Get();
					s += t.val; 
					while (StartOf(14)) {
						if (StartOf(15)) {
							Get();
							s += (spaceBetween(s, t.val, end != t.pos) ? " " : "") + t.val; end = t.pos+t.val.length(); 
						} else {
							Get();
						}
					}
					Expect(30);
				} else SynErr(50);
			} else if (StartOf(11)) {
				while (StartOf(16)) {
					if (StartOf(17)) {
						Get();
						s += (spaceBetween(s, t.val, end != t.pos) ? " " : "") + t.val; end = t.pos+t.val.length(); 
					} else {
						Get();
					}
				}
				Expect(30);
			} else SynErr(51);
		} else SynErr(52);
		return s;
	}

	void SemText() {
		Expect(42);
		while (StartOf(18)) {
			if (StartOf(19)) {
				Get();
			} else if (la.kind == 4) {
				Get();
			} else {
				Get();
			}
		}
		Expect(43);
	}

	void Expression() {
		Term();
		while (WeakSeparator(33,20,21) ) {
			Term();
		}
	}

	String  SimSet() {
		String  s;
		s = ""; 
		if (la.kind == 1) {
			Get();
			s = t.val; 
		} else if (la.kind == 3) {
			Get();
			s = t.val; 
		} else if (la.kind == 5) {
			Char();
			s = t.val; 
			if (la.kind == 22) {
				Get();
				Char();
				s += " .. " + t.val; 
			}
		} else if (la.kind == 23) {
			Get();
			s = t.val; 
		} else SynErr(53);
		return s;
	}

	void Char() {
		Expect(5);
	}

	void Sym() {
		if (la.kind == 1) {
			Get();
		} else if (la.kind == 3 || la.kind == 5) {
			if (la.kind == 3) {
				Get();
			} else {
				Get();
			}
		} else SynErr(54);
	}

	void TypeName() {
		Expect(1);
		while (la.kind == 18 || la.kind == 24 || la.kind == 31) {
			if (la.kind == 18) {
				Get();
				Expect(1);
			} else if (la.kind == 31) {
				Get();
				Expect(32);
			} else {
				Get();
				TypeName();
				while (la.kind == 28) {
					Get();
					TypeName();
				}
				Expect(27);
			}
		}
	}

	void Term() {
		if (StartOf(22)) {
			if (la.kind == 40) {
				Resolver();
			}
			Factor();
			while (StartOf(23)) {
				Factor();
			}
		} else if (StartOf(24)) {
		} else SynErr(55);
	}

	void Resolver() {
		Expect(40);
		Expect(35);
		Condition();
	}

	void Factor() {
		switch (la.kind) {
		case 1: case 3: case 5: case 34: {
			if (la.kind == 34) {
				Get();
			}
			Sym();
			if (la.kind == 24 || la.kind == 29) {
				Attribs();
			}
			break;
		}
		case 35: {
			Get();
			Expression();
			Expect(36);
			break;
		}
		case 31: {
			Get();
			Expression();
			Expect(32);
			break;
		}
		case 37: {
			Get();
			Expression();
			Expect(38);
			break;
		}
		case 42: {
			SemText();
			break;
		}
		case 23: {
			Get();
			break;
		}
		case 39: {
			Get();
			break;
		}
		default: SynErr(56); break;
		}
	}

	void Attribs() {
		if (la.kind == 24) {
			Get();
			if (la.kind == 25 || la.kind == 26) {
				if (la.kind == 25) {
					Get();
				} else {
					Get();
				}
				while (StartOf(25)) {
					if (StartOf(26)) {
						Get();
					} else if (la.kind == 31 || la.kind == 35) {
						Bracketed();
					} else {
						Get();
					}
				}
				if (la.kind == 27) {
					Get();
				} else if (la.kind == 28) {
					Get();
					while (StartOf(9)) {
						if (StartOf(10)) {
							Get();
						} else {
							Get();
						}
					}
					Expect(27);
				} else SynErr(57);
			} else if (StartOf(11)) {
				while (StartOf(12)) {
					if (StartOf(13)) {
						Get();
					} else {
						Get();
					}
				}
				Expect(27);
			} else SynErr(58);
		} else if (la.kind == 29) {
			Get();
			if (la.kind == 25 || la.kind == 26) {
				if (la.kind == 25) {
					Get();
				} else {
					Get();
				}
				while (StartOf(27)) {
					if (StartOf(28)) {
						Get();
					} else if (la.kind == 31 || la.kind == 35) {
						Bracketed();
					} else {
						Get();
					}
				}
				if (la.kind == 30) {
					Get();
				} else if (la.kind == 28) {
					Get();
					while (StartOf(14)) {
						if (StartOf(15)) {
							Get();
						} else {
							Get();
						}
					}
					Expect(30);
				} else SynErr(59);
			} else if (StartOf(11)) {
				while (StartOf(16)) {
					if (StartOf(17)) {
						Get();
					} else {
						Get();
					}
				}
				Expect(30);
			} else SynErr(60);
		} else SynErr(61);
	}

	void Condition() {
		while (StartOf(29)) {
			if (la.kind == 35) {
				Get();
				Condition();
			} else {
				Get();
			}
		}
		Expect(36);
	}

	void TokenTerm() {
		TokenFactor();
		while (StartOf(7)) {
			TokenFactor();
		}
		if (la.kind == 41) {
			Get();
			Expect(35);
			TokenExpr();
			Expect(36);
		}
	}

	void TokenFactor() {
		if (la.kind == 1 || la.kind == 3 || la.kind == 5) {
			Sym();
		} else if (la.kind == 35) {
			Get();
			TokenExpr();
			Expect(36);
		} else if (la.kind == 31) {
			Get();
			TokenExpr();
			Expect(32);
		} else if (la.kind == 37) {
			Get();
			TokenExpr();
			Expect(38);
		} else SynErr(62);
	}

	void Bracketed() {
		if (la.kind == 35) {
			Get();
			while (StartOf(29)) {
				if (la.kind == 31 || la.kind == 35) {
					Bracketed();
				} else {
					Get();
				}
			}
			Expect(36);
		} else if (la.kind == 31) {
			Get();
			while (StartOf(30)) {
				if (la.kind == 31 || la.kind == 35) {
					Bracketed();
				} else {
					Get();
				}
			}
			Expect(32);
		} else SynErr(63);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		Coco();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,T,x,T, x,T,x,x, x,x,T,T, x,x,x,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x},
		{x,T,T,T, T,T,x,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, T,T,T,x, x,x,x,x, T,T,T,x, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{T,T,x,T, x,T,x,x, x,x,T,T, x,x,x,T, T,T,T,x, x,x,x,T, x,x,x,x, x,x,x,T, x,T,T,T, x,T,x,T, T,x,T,x, x,x},
		{T,T,x,T, x,T,x,x, x,x,T,T, x,x,x,T, T,T,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x},
		{T,T,x,T, x,T,x,x, x,x,T,T, x,x,x,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x},
		{x,T,x,T, x,T,x,x, x,x,T,T, x,x,x,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x},
		{x,T,x,T, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,T, x,T,x,x, x,x,x,x, x,x},
		{x,x,x,x, x,x,x,x, x,x,x,T, x,T,T,T, T,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x, T,x,x,x, T,x,T,x, x,x,x,x, x,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,x, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,x, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x,x,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,x,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,x,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x,x,T, T,T,x,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x,x,T, T,T,x,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,x, T,x},
		{x,T,T,T, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,x,x, T,x},
		{x,T,x,T, x,T,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,T, x,x,x,x, x,x,x,T, T,T,T,T, T,T,T,T, T,x,T,x, x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x, T,x,x,x, T,x,T,x, x,x,x,x, x,x},
		{x,T,x,T, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,T, x,x,T,T, x,T,x,T, T,x,T,x, x,x},
		{x,T,x,T, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,T, x,x,T,T, x,T,x,T, x,x,T,x, x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x, T,T,x,x, T,x,T,x, x,x,x,x, x,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,x, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,x, x,T,T,x, T,T,T,x, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, x,T,x,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, x,T,x,x, T,T,T,x, T,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, x,T,T,T, T,T,T,T, T,x},
		{x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, x,T,T,T, T,T,T,T, T,T,T,T, T,x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "ident expected"; break;
			case 2: s = "number expected"; break;
			case 3: s = "string expected"; break;
			case 4: s = "badString expected"; break;
			case 5: s = "char expected"; break;
			case 6: s = "\"COMPILER\" expected"; break;
			case 7: s = "\"IGNORECASE\" expected"; break;
			case 8: s = "\"CHARACTERS\" expected"; break;
			case 9: s = "\"TOKENS\" expected"; break;
			case 10: s = "\"PRAGMAS\" expected"; break;
			case 11: s = "\"COMMENTS\" expected"; break;
			case 12: s = "\"FROM\" expected"; break;
			case 13: s = "\"TO\" expected"; break;
			case 14: s = "\"NESTED\" expected"; break;
			case 15: s = "\"IGNORE\" expected"; break;
			case 16: s = "\"PRODUCTIONS\" expected"; break;
			case 17: s = "\"=\" expected"; break;
			case 18: s = "\".\" expected"; break;
			case 19: s = "\"END\" expected"; break;
			case 20: s = "\"+\" expected"; break;
			case 21: s = "\"-\" expected"; break;
			case 22: s = "\"..\" expected"; break;
			case 23: s = "\"ANY\" expected"; break;
			case 24: s = "\"<\" expected"; break;
			case 25: s = "\"^\" expected"; break;
			case 26: s = "\"out\" expected"; break;
			case 27: s = "\">\" expected"; break;
			case 28: s = "\",\" expected"; break;
			case 29: s = "\"<.\" expected"; break;
			case 30: s = "\".>\" expected"; break;
			case 31: s = "\"[\" expected"; break;
			case 32: s = "\"]\" expected"; break;
			case 33: s = "\"|\" expected"; break;
			case 34: s = "\"WEAK\" expected"; break;
			case 35: s = "\"(\" expected"; break;
			case 36: s = "\")\" expected"; break;
			case 37: s = "\"{\" expected"; break;
			case 38: s = "\"}\" expected"; break;
			case 39: s = "\"SYNC\" expected"; break;
			case 40: s = "\"IF\" expected"; break;
			case 41: s = "\"CONTEXT\" expected"; break;
			case 42: s = "\"(.\" expected"; break;
			case 43: s = "\".)\" expected"; break;
			case 44: s = "??? expected"; break;
			case 45: s = "this symbol not expected in Coco"; break;
			case 46: s = "this symbol not expected in TokenDecl"; break;
			case 47: s = "invalid TokenDecl"; break;
			case 48: s = "invalid AttrDecl"; break;
			case 49: s = "invalid AttrDecl"; break;
			case 50: s = "invalid AttrDecl"; break;
			case 51: s = "invalid AttrDecl"; break;
			case 52: s = "invalid AttrDecl"; break;
			case 53: s = "invalid SimSet"; break;
			case 54: s = "invalid Sym"; break;
			case 55: s = "invalid Term"; break;
			case 56: s = "invalid Factor"; break;
			case 57: s = "invalid Attribs"; break;
			case 58: s = "invalid Attribs"; break;
			case 59: s = "invalid Attribs"; break;
			case 60: s = "invalid Attribs"; break;
			case 61: s = "invalid Attribs"; break;
			case 62: s = "invalid TokenFactor"; break;
			case 63: s = "invalid Bracketed"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
