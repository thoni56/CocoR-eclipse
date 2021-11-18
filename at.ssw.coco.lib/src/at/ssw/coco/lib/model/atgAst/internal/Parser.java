/*******************************************************************************
 * Copyright (C) 2011 Martin Preinfalk
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
package at.ssw.coco.lib.model.atgAst.internal;

import at.ssw.coco.lib.model.atgAst.nodeTypes.*;

import java.util.HashSet;
import java.util.ArrayList;

/**
 * Parser for Coco/R Grammer that builds an Abstract Syntax Tree
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
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
	public static final int _optionSym = 46;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	private RootNode root = null;
	private CompilerNode compilerNode = null;
	private GlobalFieldNode globalFieldNode = null;
	private ImportsNode importsNode = null;
	private ScannerSpecNode scannerSpecNode = null;
	private ParserSpecNode parserSpecNode = null;
	
	// returns root Node after parsing
	public RootNode getRoot() {
		return root;
	}

	// Sets for idents to compute their Kinds afterwards
	private HashSet<String> characterSets = new HashSet<String>();
	private HashSet<String> tokens = new HashSet<String>();
	private HashSet<String> pragmas = new HashSet<String>();
	private HashSet<String> productions = new HashSet<String>();
	private ArrayList<IdentNode> idents = new ArrayList<IdentNode>();

	// computes an Idents Kind by given name, has to be
	// called after parsing, when the Hashset were already filled
	private IdentNode.Kind getKindForName(String ident) {
		if (characterSets.contains(ident)) {
			return IdentNode.Kind.CHARACTER_SET;
		} else if (tokens.contains(ident)) {
			return IdentNode.Kind.TOKEN;
		} else if (pragmas.contains(ident)) {
			return IdentNode.Kind.PRAGMA;
		} else if (productions.contains(ident)) {
			return IdentNode.Kind.PRODUCTION;
		} else {		
			return IdentNode.Kind.INVALID;
		}
	}

	// computes all idents Kinds that are unknown to this point
	// has to be called after parsing, when the Hashset were already filled
	private void computeIdentKinds() {
		for (IdentNode n : idents) {
			if (n.getKind() == IdentNode.Kind.INVALID) {
				n.setKind(getKindForName(n.getIdent()));
			}
		}
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
			if (la.kind == 46) {
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
		int beg = 0; 
		int end = 0;
		root = new RootNode(beg); 
		if (StartOf(1)) {
			Get();
			beg = t.charPos; //begin of imports 
			importsNode = new ImportsNode(beg);
			root.setImportsNode(importsNode); 
			while (StartOf(1)) {
				Get();
				end = t.charPos + t.val.length(); 
			}
			importsNode.getRegion().setEnd(end); 
		}
		Expect(6);
		beg = t.charPos; //begin of compiler
		compilerNode = new CompilerNode(beg);
		root.setCompilerNode(compilerNode); 
		Expect(1);
		beg = t.charPos; //begin of ident
		IdentNode ident = new IdentNode(beg, IdentNode.Kind.COMPILER, t.val);
		idents.add(ident);
		compilerNode.setIdentNode(ident); 
		beg = end = la.pos; 
		while (StartOf(2)) {
			Get();
			end = t.charPos + t.val.length(); 
		}
		if (end > beg) {
		globalFieldNode = new GlobalFieldNode(beg, end);
		compilerNode.setGlobalFieldNode(globalFieldNode);
		} 
		beg = la.pos; //begin of scannerSpec
		scannerSpecNode = new ScannerSpecNode(beg); 
		compilerNode.setScannerSpecNode(scannerSpecNode); 
		if (la.kind == 7) {
			Get();
			scannerSpecNode.setIgnoreCase(true); 
		}
		if (la.kind == 8) {
			Get();
			while (la.kind == 1) {
				SetDeclNode setDeclNode; 
				setDeclNode = SetDecl();
				scannerSpecNode.addSetDeclNode(setDeclNode); 
			}
		}
		if (la.kind == 9) {
			Get();
			while (la.kind == 1 || la.kind == 3 || la.kind == 5) {
				TokenDeclNode tokenDeclNode; 
				tokenDeclNode = TokenDecl();
				tokenDeclNode.getRegion().setEnd(t.charPos + t.val.length());
				tokenDeclNode.getSymbolNode().getIdentNode().setKind(IdentNode.Kind.TOKEN);
				tokens.add(tokenDeclNode.getSymbolNode().getIdentNode().getIdent());
				scannerSpecNode.addTokenDeclNode(tokenDeclNode); 
			}
		}
		if (la.kind == 10) {
			Get();
			while (la.kind == 1 || la.kind == 3 || la.kind == 5) {
				TokenDeclNode tokenDeclNode; 
				tokenDeclNode = TokenDecl();
				tokenDeclNode.getRegion().setEnd(t.charPos + t.val.length());
				tokenDeclNode.getSymbolNode().getIdentNode().setKind(IdentNode.Kind.PRAGMA);
				pragmas.add(tokenDeclNode.getSymbolNode().getIdentNode().getIdent());
				scannerSpecNode.addPragmaDeclNode(tokenDeclNode); 
			}
		}
		while (la.kind == 11) {
			Get();
			CommentDeclNode commentDeclNode = new CommentDeclNode(t.charPos);
			TokenExprNode from, to; 
			Expect(12);
			from = TokenExpr();
			commentDeclNode.setFrom(from); 
			Expect(13);
			to = TokenExpr();
			commentDeclNode.setTo(to); 
			if (la.kind == 14) {
				Get();
				commentDeclNode.setNested(true); 
			}
			commentDeclNode.getRegion().setEnd(t.charPos + t.val.length()); 
			scannerSpecNode.addCommentDeclNode(commentDeclNode); 
		}
		while (la.kind == 15) {
			Get();
			SetNode setNode; 
			WhiteSpaceDeclNode whiteSpaceDeclNode = new WhiteSpaceDeclNode(t.charPos); 
			setNode = Set();
			whiteSpaceDeclNode.setSetNode(setNode);
			whiteSpaceDeclNode.getRegion().setEnd(t.charPos + t.val.length()); 
			scannerSpecNode.addWhiteSpaceDeclNode(whiteSpaceDeclNode); 
		}
		scannerSpecNode.getRegion().setEnd(t.charPos + t.val.length()); 
		while (!(la.kind == 0 || la.kind == 16)) {SynErr(45); Get();}
		Expect(16);
		parserSpecNode = new ParserSpecNode(t.charPos);
		compilerNode.setParserSpecNode(parserSpecNode); 
		SemTextNode semTextNode; 
		AttrDeclNode attrDeclNode; 
		ExpressionNode expressionNode; 
		while (la.kind == 1) {
			Get();
			IdentNode identNode = new IdentNode(
			t.charPos,
			IdentNode.Kind.PRODUCTION,
			t.val);
			idents.add(identNode);
			productions.add(t.val);
			ProductionNode productionNode = new ProductionNode(t.charPos, identNode); 
			if (la.kind == 24 || la.kind == 29) {
				attrDeclNode = AttrDecl();
				productionNode.setAttrDeclNode(attrDeclNode); 
			}
			if (la.kind == 42) {
				semTextNode = SemText();
				productionNode.setSemTextNode(semTextNode); 
			}
			ExpectWeak(17, 3);
			expressionNode = Expression();
			if (expressionNode != null) 
			productionNode.setExpressionNode(expressionNode); 
			ExpectWeak(18, 4);
			productionNode.getRegion().setEnd(t.charPos + t.val.length()); 
			parserSpecNode.addProductionNode(productionNode); 
		}
		parserSpecNode.getRegion().setEnd(t.charPos + t.val.length()); 
		Expect(19);
		Expect(1);
		ident = new IdentNode(t.charPos, IdentNode.Kind.COMPILER, t.val);
		idents.add(ident);
		compilerNode.setEndMarkerIdent(ident); 
		Expect(18);
		end = t.charPos + t.val.length(); 
		compilerNode.getRegion().setEnd(end);
		root.getRegion().setEnd(end); 
		computeIdentKinds(); 
	}

	SetDeclNode  SetDecl() {
		SetDeclNode  setDeclNode;
		SetNode setNode; 
		Expect(1);
		setDeclNode = new SetDeclNode(t.charPos);
		IdentNode identNode = new IdentNode(t.charPos, IdentNode.Kind.CHARACTER_SET, t.val);
		characterSets.add(t.val);
		idents.add(identNode);
		setDeclNode.setIdentNode(identNode); 
		Expect(17);
		setNode = Set();
		setDeclNode.setSetNode(setNode); 
		Expect(18);
		setDeclNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return setDeclNode;
	}

	TokenDeclNode  TokenDecl() {
		TokenDeclNode  tokenDeclNode;
		tokenDeclNode = new TokenDeclNode(la.charPos); 
		SymbolNode symbolNode;
		SemTextNode semTextNode; 
		TokenExprNode tokenExprNode; 
		symbolNode = Sym();
		tokenDeclNode.setSymbolNode(symbolNode); 
		tokens.add(symbolNode.getIdentNode().getIdent());
		while (!(StartOf(5))) {SynErr(46); Get();}
		if (la.kind == 17) {
			Get();
			tokenExprNode = TokenExpr();
			tokenDeclNode.setTokenExprNode(tokenExprNode); 
			Expect(18);
		} else if (StartOf(6)) {
		} else SynErr(47);
		if (la.kind == 42) {
			semTextNode = SemText();
			tokenDeclNode.setSemTextNode(semTextNode); 
		}
		tokenDeclNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return tokenDeclNode;
	}

	TokenExprNode  TokenExpr() {
		TokenExprNode  tokenExprNode;
		tokenExprNode = new TokenExprNode(la.charPos); 
		TokenTermNode tokenTermNode; 
		tokenTermNode = TokenTerm();
		tokenExprNode.addTokenTermNode(tokenTermNode); 
		while (WeakSeparator(33,7,8) ) {
			tokenTermNode = TokenTerm();
			tokenExprNode.addTokenTermNode(tokenTermNode); 
		}
		tokenExprNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return tokenExprNode;
	}

	SetNode  Set() {
		SetNode  setNode;
		setNode = new SetNode(la.charPos);
		SimSetNode simSetNode; 
		simSetNode = SimSet();
		setNode.setSimSetNode(simSetNode); 
		while (la.kind == 20 || la.kind == 21) {
			if (la.kind == 20) {
				OpSetNode opSetNode = new OpSetNode(la.charPos); 
				Get();
				simSetNode = SimSet();
				opSetNode.setSetOp(OpSetNode.SetOp.SETOP_PLUS);
				opSetNode.setSimSetNode(simSetNode); 
				opSetNode.getRegion().setEnd(t.charPos + t.val.length());
					setNode.addOpSetNode(opSetNode); 
			} else {
				OpSetNode opSetNode = new OpSetNode(la.charPos); 
				Get();
				simSetNode = SimSet();
				opSetNode.setSetOp(OpSetNode.SetOp.SETOP_MINUS);
				opSetNode.setSimSetNode(simSetNode); 
				opSetNode.getRegion().setEnd(t.charPos + t.val.length());
					setNode.addOpSetNode(opSetNode); 
			}
		}
		setNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return setNode;
	}

	AttrDeclNode  AttrDecl() {
		AttrDeclNode  attrDeclNode;
		attrDeclNode = new AttrDeclNode(la.charPos); 
		TypeNameNode typeNameNode; 
		if (la.kind == 24) {
			Get();
			if (la.kind == 25 || la.kind == 26) {
				if (la.kind == 25) {
					Get();
				} else {
					Get();
				}
				OutAttrDeclNode outAttrDeclNode = new OutAttrDeclNode(t.charPos); 
				typeNameNode = TypeName();
				outAttrDeclNode.setTypeNameNode(typeNameNode); 
				Expect(1);
				IdentNode identNode = new IdentNode(
				t.charPos,
				IdentNode.Kind.ATTRIBUTE,
				t.val);
				idents.add(identNode);
				outAttrDeclNode.setIdentNode(identNode);
							outAttrDeclNode.getRegion().setEnd(t.charPos + t.val.length()); 
							attrDeclNode.setOutAttrDeclNode(outAttrDeclNode); 
				if (la.kind == 27) {
					Get();
				} else if (la.kind == 28) {
					Get();
					int beg = la.charPos; 
					while (StartOf(9)) {
						if (StartOf(10)) {
							Get();
						} else {
							Get();
						}
					}
					int end = t.charPos + t.val.length();
					if (end > beg) {
						InAttrDeclNode inAttrDeclNode = new InAttrDeclNode(beg);
						inAttrDeclNode.getRegion().setEnd(end); 
						attrDeclNode.setInAttrDeclNode(inAttrDeclNode); 
					} 
					Expect(27);
				} else SynErr(48);
			} else if (StartOf(11)) {
				int beg = la.charPos; 
				while (StartOf(12)) {
					if (StartOf(13)) {
						Get();
					} else {
						Get();
					}
				}
				int end = t.charPos + t.val.length();
				if (end > beg) {
					InAttrDeclNode inAttrDeclNode = new InAttrDeclNode(beg);
					inAttrDeclNode.getRegion().setEnd(end); 
					attrDeclNode.setInAttrDeclNode(inAttrDeclNode); 
				} 
				Expect(27);
			} else SynErr(49);
			attrDeclNode.getRegion().setEnd(t.charPos + t.val.length()); 
		} else if (la.kind == 29) {
			Get();
			if (la.kind == 25 || la.kind == 26) {
				if (la.kind == 25) {
					Get();
				} else {
					Get();
				}
				OutAttrDeclNode outAttrDeclNode = new OutAttrDeclNode(t.charPos); 
				typeNameNode = TypeName();
				outAttrDeclNode.setTypeNameNode(typeNameNode); 
				Expect(1);
				IdentNode identNode = new IdentNode(
				t.charPos,
				IdentNode.Kind.ATTRIBUTE,
				t.val);
				idents.add(identNode);
				outAttrDeclNode.setIdentNode(identNode);
							outAttrDeclNode.getRegion().setEnd(t.charPos + t.val.length()); 
							attrDeclNode.setOutAttrDeclNode(outAttrDeclNode); 
				if (la.kind == 30) {
					Get();
				} else if (la.kind == 28) {
					Get();
					int beg = la.charPos; 
					while (StartOf(14)) {
						if (StartOf(15)) {
							Get();
						} else {
							Get();
						}
					}
					int end = t.charPos + t.val.length();
					if (end > beg) {
						InAttrDeclNode inAttrDeclNode = new InAttrDeclNode(beg);
						inAttrDeclNode.getRegion().setEnd(end); 
						attrDeclNode.setInAttrDeclNode(inAttrDeclNode); 
					} 
					Expect(30);
				} else SynErr(50);
			} else if (StartOf(11)) {
				int beg = la.charPos; 
				while (StartOf(16)) {
					if (StartOf(17)) {
						Get();
					} else {
						Get();
					}
				}
				int end = t.charPos + t.val.length();
				if (end > beg) {
					InAttrDeclNode inAttrDeclNode = new InAttrDeclNode(beg);
					inAttrDeclNode.getRegion().setEnd(end); 
					attrDeclNode.setInAttrDeclNode(inAttrDeclNode); 
				} 
				Expect(30);
			} else SynErr(51);
			attrDeclNode.getRegion().setEnd(t.charPos + t.val.length()); 
		} else SynErr(52);
		return attrDeclNode;
	}

	SemTextNode  SemText() {
		SemTextNode  semTextNode;
		Expect(42);
		semTextNode = new SemTextNode(t.charPos); 
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
		semTextNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return semTextNode;
	}

	ExpressionNode  Expression() {
		ExpressionNode  expressionNode;
		expressionNode = new ExpressionNode(la.charPos); 
		TermNode termNode; 
		termNode = Term();
		if (termNode != null) expressionNode.addTermNode(termNode); 
		while (WeakSeparator(33,20,21) ) {
			termNode = Term();
			if (termNode != null) expressionNode.addTermNode(termNode); 
		}
		expressionNode.getRegion().setEnd(t.charPos + t.val.length()); 
		if (expressionNode.getRegion().getLength() <= 0) { //Term might be empty
			expressionNode = null; 
		} 
		return expressionNode;
	}

	SimSetNode  SimSet() {
		SimSetNode  simSetNode;
		char from, to; 
		simSetNode = null; 
		if (la.kind == 1) {
			Get();
			IdentNode identNode = new IdentNode(
			t.charPos,
			IdentNode.Kind.INVALID,
			t.val);
			idents.add(identNode);
			simSetNode = new SimSetNode(t.charPos, identNode); 
		} else if (la.kind == 3) {
			Get();
			String s = t.val.substring(1, t.val.length() - 1);
			simSetNode = new SimSetNode(t.charPos, s); 
			simSetNode.getRegion().setEnd(t.charPos + t.val.length()); 
		} else if (la.kind == 5) {
			from = Char();
			simSetNode = new SimSetNode(t.charPos, from); 
			if (la.kind == 22) {
				Get();
				to = Char();
				simSetNode.setTo(to); 
			}
			simSetNode.getRegion().setEnd(t.charPos + t.val.length()); 
		} else if (la.kind == 23) {
			Get();
			simSetNode = new SimSetNode(t.charPos);
			simSetNode.setKind(SimSetNode.Kind.ANY);
			simSetNode.getRegion().setEnd(t.charPos + t.val.length()); 
		} else SynErr(53);
		return simSetNode;
	}

	char  Char() {
		char  c;
		Expect(5);
		c = 0;
		try {
			String s = EscapeSeqUtil.unescape(t.val);
			c = s.toCharArray()[1]; 
		} catch (Exception e) {
			SemErr (e.getMessage());
		} 
		return c;
	}

	SymbolNode  Sym() {
		SymbolNode  symbolNode;
		symbolNode=null; 
		if (la.kind == 1) {
			Get();
			IdentNode identNode = new IdentNode(
			t.charPos,
			IdentNode.Kind.INVALID,
			t.val);
			idents.add(identNode);
					symbolNode = new SymbolNode(t.charPos, identNode);
					symbolNode.getRegion().setEnd(t.charPos + t.val.length()); 
		} else if (la.kind == 3 || la.kind == 5) {
			if (la.kind == 3) {
				Get();
				String s = t.val.substring(1, t.val.length() - 1);
				symbolNode = new SymbolNode(t.charPos, s);
				symbolNode.getRegion().setEnd(t.charPos + t.val.length()); 
			} else {
				Get();
				symbolNode = new SymbolNode(t.charPos, t.val.charAt(1));
				symbolNode.getRegion().setEnd(t.charPos + t.val.length()); 
			}
		} else SynErr(54);
		return symbolNode;
	}

	TypeNameNode  TypeName() {
		TypeNameNode  typeNameNode;
		typeNameNode = new TypeNameNode(la.charPos); 
		TypeNameNode dummy;
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
				dummy = TypeName();
				while (la.kind == 28) {
					Get();
					dummy = TypeName();
				}
				Expect(27);
			}
		}
		typeNameNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return typeNameNode;
	}

	TermNode  Term() {
		TermNode  termNode;
		FactorNode factorNode;
		ResolverNode resolverNode;
		termNode = null; 
		if (StartOf(22)) {
			termNode = new TermNode(la.charPos); 
			if (la.kind == 40) {
				resolverNode = Resolver();
				termNode.setResolverNode(resolverNode); 
			}
			factorNode = Factor();
			termNode.addFactorNode(factorNode); 
			while (StartOf(23)) {
				factorNode = Factor();
				termNode.addFactorNode(factorNode); 
			}
			termNode.getRegion().setEnd(t.charPos + t.val.length()); 
		} else if (StartOf(24)) {
		} else SynErr(55);
		return termNode;
	}

	ResolverNode  Resolver() {
		ResolverNode  resolverNode;
		resolverNode = new ResolverNode(la.charPos); 
		Expect(40);
		Expect(35);
		Condition();
		resolverNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return resolverNode;
	}

	FactorNode  Factor() {
		FactorNode  factorNode;
		factorNode = new FactorNode(la.charPos);
		SymbolNode symbolNode;
		AttribsNode attribsNode;
		SemTextNode semTextNode; 
		ExpressionNode expressionNode; 
		switch (la.kind) {
		case 1: case 3: case 5: case 34: {
			if (la.kind == 34) {
				Get();
				factorNode.setWeakSymbol(true); 
			}
			symbolNode = Sym();
			factorNode.setKind(FactorNode.Kind.SYMBOL);
			factorNode.setSymbolNode(symbolNode); 
			if (la.kind == 24 || la.kind == 29) {
				attribsNode = Attribs();
				factorNode.setAttribsNode(attribsNode); 
			}
			break;
		}
		case 35: {
			Get();
			expressionNode = Expression();
			Expect(36);
			factorNode.setKind(FactorNode.Kind.PAR);
			factorNode.setExpressionNode(expressionNode); 
			break;
		}
		case 31: {
			Get();
			expressionNode = Expression();
			Expect(32);
			factorNode.setKind(FactorNode.Kind.OPTIONAL);
			factorNode.setExpressionNode(expressionNode); 
			break;
		}
		case 37: {
			Get();
			expressionNode = Expression();
			Expect(38);
			factorNode.setKind(FactorNode.Kind.ARBITRARY_OFTEN);
			factorNode.setExpressionNode(expressionNode); 
			break;
		}
		case 42: {
			semTextNode = SemText();
			factorNode.setKind(FactorNode.Kind.SEM_TEXT);
			factorNode.setSemTextNode(semTextNode); 
			break;
		}
		case 23: {
			Get();
			factorNode.setKind(FactorNode.Kind.ANY); 
			break;
		}
		case 39: {
			Get();
			factorNode.setKind(FactorNode.Kind.SYNC); 
			break;
		}
		default: SynErr(56); break;
		}
		factorNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return factorNode;
	}

	AttribsNode  Attribs() {
		AttribsNode  attribsNode;
		attribsNode = new AttribsNode(la.charPos); 
		if (la.kind == 24) {
			Get();
			if (la.kind == 25 || la.kind == 26) {
				if (la.kind == 25) {
					Get();
				} else {
					Get();
				}
				OutAttribsNode outAttribsNode = new OutAttribsNode(t.charPos); 
				while (StartOf(25)) {
					if (StartOf(26)) {
						Get();
					} else if (la.kind == 31 || la.kind == 35) {
						Bracketed();
					} else {
						Get();
					}
				}
				outAttribsNode.getRegion().setEnd(t.charPos + t.val.length()); 
				attribsNode.setOutAttribsNode(outAttribsNode); 
				if (la.kind == 27) {
					Get();
				} else if (la.kind == 28) {
					Get();
					int beg = la.charPos; 
					while (StartOf(9)) {
						if (StartOf(10)) {
							Get();
						} else {
							Get();
						}
					}
					int end = t.charPos + t.val.length();
					if (end > beg) {
						InAttribsNode inAttribsNode = new InAttribsNode(beg);
						inAttribsNode.getRegion().setEnd(end); 
						attribsNode.setInAttribsNode(inAttribsNode); 
					} 
					Expect(27);
				} else SynErr(57);
			} else if (StartOf(11)) {
				int beg = la.charPos; 
				while (StartOf(12)) {
					if (StartOf(13)) {
						Get();
					} else {
						Get();
					}
				}
				int end = t.charPos + t.val.length();
				if (end > beg) {
					InAttribsNode inAttribsNode = new InAttribsNode(beg); 
					inAttribsNode.getRegion().setEnd(end); 
				attribsNode.setInAttribsNode(inAttribsNode); 
				} 
				Expect(27);
			} else SynErr(58);
			attribsNode.getRegion().setEnd(t.charPos + t.val.length()); 
		} else if (la.kind == 29) {
			Get();
			if (la.kind == 25 || la.kind == 26) {
				if (la.kind == 25) {
					Get();
				} else {
					Get();
				}
				OutAttribsNode outAttribsNode = new OutAttribsNode(t.charPos); 
				while (StartOf(27)) {
					if (StartOf(28)) {
						Get();
					} else if (la.kind == 31 || la.kind == 35) {
						Bracketed();
					} else {
						Get();
					}
				}
				outAttribsNode.getRegion().setEnd(t.charPos + t.val.length()); 
				attribsNode.setOutAttribsNode(outAttribsNode); 
				if (la.kind == 30) {
					Get();
				} else if (la.kind == 28) {
					Get();
					int beg = la.charPos; 
					while (StartOf(14)) {
						if (StartOf(15)) {
							Get();
						} else {
							Get();
						}
					}
					int end = t.charPos + t.val.length();
					if (end > beg) {
						InAttribsNode inAttribsNode = new InAttribsNode(beg);
						inAttribsNode.getRegion().setEnd(end); 
						attribsNode.setInAttribsNode(inAttribsNode); 
					} 
					Expect(30);
				} else SynErr(59);
			} else if (StartOf(11)) {
				int beg = la.charPos; 
				while (StartOf(16)) {
					if (StartOf(17)) {
						Get();
					} else {
						Get();
					}
				}
				int end = t.charPos + t.val.length();
				if (end > beg) {
					InAttribsNode inAttribsNode = new InAttribsNode(beg); 
					inAttribsNode.getRegion().setEnd(end); 
				attribsNode.setInAttribsNode(inAttribsNode); 
				} 
				Expect(30);
			} else SynErr(60);
			attribsNode.getRegion().setEnd(t.charPos + t.val.length()); 
		} else SynErr(61);
		return attribsNode;
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

	TokenTermNode  TokenTerm() {
		TokenTermNode  tokenTermNode;
		tokenTermNode = new TokenTermNode(la.charPos); 
		TokenFactorNode tokenFactorNode; 
		TokenExprNode tokenExprNode;
		tokenFactorNode = TokenFactor();
		tokenTermNode.addTokenFactorNode(tokenFactorNode); 
		while (StartOf(7)) {
			tokenFactorNode = TokenFactor();
			tokenTermNode.addTokenFactorNode(tokenFactorNode); 
		}
		if (la.kind == 41) {
			Get();
			Expect(35);
			tokenExprNode = TokenExpr();
			tokenTermNode.setContextTokenExprNode(tokenExprNode); 
			Expect(36);
		}
		tokenTermNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return tokenTermNode;
	}

	TokenFactorNode  TokenFactor() {
		TokenFactorNode  tokenFactorNode;
		tokenFactorNode = new TokenFactorNode(la.charPos); 
		SymbolNode symbolNode; 
		TokenExprNode tokenExprNode; 
		if (la.kind == 1 || la.kind == 3 || la.kind == 5) {
			symbolNode = Sym();
			tokenFactorNode.setKind(TokenFactorNode.Kind.SYMBOL); 
			tokenFactorNode.setSymbolNode(symbolNode); 
		} else if (la.kind == 35) {
			Get();
			tokenExprNode = TokenExpr();
			Expect(36);
			tokenFactorNode.setKind(TokenFactorNode.Kind.PAR); 
			tokenFactorNode.setTokenExprNode(tokenExprNode); 
		} else if (la.kind == 31) {
			Get();
			tokenExprNode = TokenExpr();
			Expect(32);
			tokenFactorNode.setKind(TokenFactorNode.Kind.OPTIONAL); 
			tokenFactorNode.setTokenExprNode(tokenExprNode); 
		} else if (la.kind == 37) {
			Get();
			tokenExprNode = TokenExpr();
			Expect(38);
			tokenFactorNode.setKind(TokenFactorNode.Kind.ARBITRARY_OFTEN); 
			tokenFactorNode.setTokenExprNode(tokenExprNode); 
		} else SynErr(62);
		tokenFactorNode.getRegion().setEnd(t.charPos + t.val.length()); 
		return tokenFactorNode;
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
