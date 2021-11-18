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
package at.ssw.coco.lib.model.atgAst.internal.junit;

import java.lang.System;
import java.util.ArrayList;



public class Parser {
	public static final int _EOF = 0;
	public static final int _ident = 1;
	public static final int _number = 2;
	public static final int maxT = 11;
	public static final int _switch = 12;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	public int foo(int bar) {
		return bar * 2;
	}



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

			if (la.kind == 12) {
				System.out.println(foo(21)); 
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
	
	void JunitTestFile() {
		ArrayList<Integer> a = new ArrayList<Integer>();
		int resolver = 42; 
		int resolverOut = 0; 
		while (StartOf(1)) {
			resolverOut = Statement(resolver);
			a.add(resolver);
			resolver = resolverOut; 
			System.out.println("SemTextTest"); 
		}
	}

	int  Statement(int resolver) {
		int  resolverOut;
		resolverOut = 0; 
		if (la.kind == 1) {
			Get();
			if (la.kind == 3) {
				Get();
				Expect(2);
				Expect(4);
			}
			Expect(5);
			Expect(2);
			while (!(la.kind == 0 || la.kind == 6)) {SynErr(12); Get();}
			Expect(6);
			resolverOut = 42; 
		} else if (la.kind == 7) {
			Get();
			Expect(8);
			Condition(resolver);
			Expect(9);
			resolverOut = Statement(resolver);
			Expect(6);
		} else if (StartOf(2)) {
			Get();
			resolverOut = 0; 
		} else SynErr(13);
		return resolverOut;
	}

	void Condition(int resolverTest) {
		if (resolverTest==42) {
			Expect(1);
			Expect(10);
			Expect(2);
		} else if (la.kind == 1) {
			Get();
			Expect(10);
			Expect(1);
		} else if (la.kind == 2) {
			ExpectWeak(2, 3);
		} else SynErr(14);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		JunitTestFile();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,T,x, x,x,x,x, x},
		{x,T,T,T, T,T,T,T, T,T,T,T, x},
		{x,x,T,T, T,T,T,x, T,T,T,T, x},
		{T,x,x,x, x,x,T,x, x,T,x,x, x}

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
			case 3: s = "\"[\" expected"; break;
			case 4: s = "\"]\" expected"; break;
			case 5: s = "\"=\" expected"; break;
			case 6: s = "\";\" expected"; break;
			case 7: s = "\"if\" expected"; break;
			case 8: s = "\"(\" expected"; break;
			case 9: s = "\")\" expected"; break;
			case 10: s = "\"==\" expected"; break;
			case 11: s = "??? expected"; break;
			case 12: s = "this symbol not expected in Statement"; break;
			case 13: s = "invalid Statement"; break;
			case 14: s = "invalid Condition"; break;
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
