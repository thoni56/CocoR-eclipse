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

package at.ssw.coco.ide.features.refactoring.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import at.ssw.coco.lib.model.atgAst.AtgAstVisitor;
import at.ssw.coco.lib.model.atgAst.nodeTypes.AbstractAtgAstNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.ExpressionNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.FactorNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.ProductionNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.SemTextNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.TermNode;

/**
 * implements a Reformatter for a single ATG - Production
 *  
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class ATGProductionReformatter {
	
	/**
	 * Line Feed Constant
	 */
	private static final char LF = '\n';
	
	/**
	 * Reformat Info DTO for Reformat Refactoring
	 */
	private ReformatInfo info;
	
	/**
	 * Document containing production to be reformatted
	 */
	private IDocument document;
	
	/**
	 * output buffer
	 */
	private StringBuilder outBuf;
	
	/**
	 * The node to be reformatted
	 */
	private ProductionNode productionNode;
	
	/**
	 * offset for Tabulators in Semantic Actions (SemTexts)
	 */
	private int currJavaLineOffset;
	
	/**
	 * offset for Tabulators in normal ATG Code
	 */
	private int currAtgLineOffset;
	
	/**
	 * number of char written in curr Line
	 */
	private int currLineCounter;
	
	/**
	 * text representatino of the production
	 */
	private String productionText;

	/**
	 * enumerates ATG Symbols and binds them to a String Representation
	 * 
	 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
	 *
	 */
	private enum ATGSymbol {
		PROD_EQ 	("="),
		PROD_END 	("."),
		TERM_SEPARATOR ("|"),
		FACTOR_SEPERATOR (" "),
		ARB_OFTEN_OPEN ("{"),
		ARB_OFTEN_CLOSE ("}"),
		PAR_OPEN ("("),
		PAR_CLOSE (")"),
		OPTIONAL_OPEN ("["),
		OPTIONAL_CLOSE ("]"),
		WEAK ("WEAK"),
		ANY ("ANY"),
		SYNC ("SYNC");
		
		/**
		 * String Representation
		 */
		private String sym;
		
		/**
		 * @param sym - StringRepresentation of Symbol
		 */
		private ATGSymbol(String sym) {
			this.sym = sym;
		}
		
		/**
		 * 
		 * @return StringRepresentation of Symbol
		 */
		public String getSymbol() {
			return sym;
		}

		/**
		 * 
		 * @return true if the String Representation is containing whitespaces only
		 */
		public boolean isWhiteSpace() {
			for (int i = 0; i < sym.length(); i++) {
				if (!Character.isWhitespace(sym.charAt(i))) {
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * Exception Class for Exceptions thrown in the Reformatter
	 * 
	 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
	 *
	 */
	public class ATGProductionReformatterException extends Exception {
		private static final long serialVersionUID = 5137479767140502694L;

		ATGProductionReformatterException(String message) {
			super(message);
		}

		public ATGProductionReformatterException(Throwable cause) {
			super(cause);
		}			
	}
	
	/**
	 * constructs a new ATGProductionReformatter
	 * 
	 * @param document - Document containing production to be reformatted
	 * @param productionNode - node to be reformatted
	 * @param info - ReformatInfo DTO
	 * @throws ATGProductionReformatterException
	 */
	public ATGProductionReformatter(IDocument document, ProductionNode productionNode, ReformatInfo info) throws ATGProductionReformatterException {
		this.document = document;
		this.productionNode = productionNode;
		this.info = info;
		this.currAtgLineOffset = 0;
		this.productionText = getString(productionNode);
		if (!info.isUseFixedOffset())
			this.currJavaLineOffset = estimateOptimalJavaOffset() + info.getTabwith();
		else
			this.currJavaLineOffset = info.getJavaLineOffset();
	}

	/**
	 * reformats the Production
	 * @return String containing reformatted production Text
	 * @throws ATGProductionReformatterException if any errors occurs
	 */
	public String reformat() throws ATGProductionReformatterException {
		this.outBuf = new StringBuilder();

		//check preceding LF
		try {
			if (document.getChar(productionNode.getRegion().getOffset() - 1) != LF) {
				newLine();
			}
		} catch (BadLocationException e) {
			throw new ATGProductionReformatterException(e);
		}
		
		//Production Ident
		writeNode(productionNode.getIdentNode());
		AbstractAtgAstNode last = productionNode.getIdentNode();

		//Attribs
		if (productionNode.getAttrDeclNode() != null) {
			writeNode(productionNode.getAttrDeclNode());
			last = productionNode.getAttrDeclNode();
		}
		
		//SemText
		if (productionNode.getSemTextNode() != null) {
			writeNode(productionNode.getSemTextNode());
			last = productionNode.getSemTextNode();
		}
		
		//Equal Sign
		newAtgLineIfNeccesary();		
		writeAtgSymbolAfterNode(ATGSymbol.PROD_EQ, last);		
		newAtgLineIfNeccesary();
		
		//Expression
		writeExpressionNode(productionNode.getExpressionNode());
		
		//Point
		newAtgLineIfNeccesary();
		writeAtgSymbol(ATGSymbol.PROD_END, -1); //The Point ist the last Character in the Production Node
												//Don't scan for comments after it.
		
		//check following LF
		try {
			if (document.getChar(	productionNode.getRegion().getOffset() + 
									productionNode.getRegion().getLength()) != LF) {
				newLine();
			}
		} catch (BadLocationException e) {
			throw new ATGProductionReformatterException(e);
		}

		return outBuf.toString();
	}

	/**
	 * reformats and writes an ExpressionNode
	 * 
	 * @param expressionNode
	 * @throws ATGProductionReformatterException
	 */
	private void writeExpressionNode(ExpressionNode expressionNode) throws ATGProductionReformatterException {
		if (expressionNode != null) {
			List<TermNode> terms = expressionNode.getTermNodes();
			for (int i = 0; i < terms.size(); i++) {
				writeTermNode(terms.get(i));
				if (i < (terms.size() - 1)) {
					newAtgLineIfNeccesary();
					writeAtgSymbolAfterNode(ATGSymbol.TERM_SEPARATOR, terms.get(i));
					newAtgLineIfNeccesary();
				}
			}
		}
	}

	/**
	 * reformats and writes a Term Node
	 * 
	 * @param term
	 * @throws ATGProductionReformatterException
	 */
	private void writeTermNode(TermNode term) throws ATGProductionReformatterException {
		if (term != null) {
			writeNode(term.getResolverNode());
	
			List<FactorNode> factors = term.getFactorNodes();
			for (FactorNode factor : factors) {
				writeFactorNode(factor);
			}
		}
	}

	/**
	 * reformats and writes a FactorNode
	 * 
	 * @param factor
	 * @throws ATGProductionReformatterException
	 */
	private void writeFactorNode(FactorNode factor) throws ATGProductionReformatterException {
		if (factor != null) {
			
			if (isAfterLF()) {
				appendAtgTab();
			} else {
				writeAtgSymbol(ATGSymbol.FACTOR_SEPERATOR, factor.getRegion().getOffset());		
			}
			
			switch (factor.getKind()) {
			case SYMBOL:
				if (factor.isWeakSymbol()) {
					writeAtgSymbol(ATGSymbol.WEAK, factor.getRegion().getOffset());
				}
				writeNode(factor.getSymbolNode());
				writeNode(factor.getAttribsNode());
				break;
			case PAR:
				writeParFactorNode(factor, ATGSymbol.PAR_OPEN, ATGSymbol.PAR_CLOSE);
				break;
			case OPTIONAL:
				writeParFactorNode(factor, ATGSymbol.OPTIONAL_OPEN, ATGSymbol.OPTIONAL_CLOSE);
				break;
			case ARBITRARY_OFTEN:
				writeParFactorNode(factor, ATGSymbol.ARB_OFTEN_OPEN, ATGSymbol.ARB_OFTEN_CLOSE);
				break;
			case SEM_TEXT:
				writeNode(factor.getSemTextNode());
				break;
			case ANY:
				writeAtgSymbol(ATGSymbol.ANY, factor.getRegion().getOffset());
				break;
			case SYNC:
				writeAtgSymbol(ATGSymbol.SYNC, factor.getRegion().getOffset());
				break;
			}
		}
	}

	/**
	 * implements a CommentNode - CommentNodes are not part of the ATG AST - 
	 * therfore they have to be parsed seperatly
	 * 
	 * @author martin
	 *
	 */
	private class CommentNode extends AbstractAtgAstNode {
		public CommentNode(int beg, int length) {
			super(beg);
			getRegion().setLength(length);
		}
		@Override
		public void accept(AtgAstVisitor atgAstVisitor) {
			// dummy
		}
	}
	
	/**
	 * implements a CommentNodeParser - CommentNodes are not part of the ATG AST - 
	 * therfore they have to be parsed seperatly, handles LineComments and Nested Comments
	 * 
	 * @author martin
	 *
	 */
	private class CommentNodeParser {
		
		private int start;
		private int curr;

		private int offset;
		private int length;
		private final int productionNodeOffset;
				
		/**
		 * Constructor
		 * @param offset - in document, where to start search for a comment
		 */
		CommentNodeParser(int offset) {
			this.productionNodeOffset = productionNode.getRegion().getOffset();
			this.start = offset - productionNode.getRegion().getOffset();
			this.curr = start;
			this.offset = -1;
			this.length = 0;
		}
		
		/**
		 * find the next comment node, searches from offset until it finds 
		 * text (overreads whitespaces). if following text is a comment, a
		 * CommentNode is created and returned. This method is intended to be 
		 * called only once, ie it returns always the same comment
		 * 
		 * @return CommentNode or null if no comment was found
		 * @throws ATGProductionReformatterException
		 */
		CommentNode getNode() throws ATGProductionReformatterException {
			this.offset = -1;
			this.length = 0;
			this.curr = start;
			
			if (!computeOffset()) {
				return null;
			}
			computeLength();
			return new CommentNode(offset, length);
		}

		/**
		 * searches for comment and computes the offset of CommentNode in the document
		 * @return true if a comment starts after offset, false if no comment found
		 */
		private boolean computeOffset()  {
			//overread whitespace
			while(curr < productionText.length() && Character.isWhitespace(productionText.charAt(curr))) {
				curr++;			
			}
			//check if comment start
			if(curr < (productionText.length() - 1) &&
					curr < (productionText.length()-1) && 
					productionText.charAt(curr) == '/' &&
					(productionText.charAt(curr+1) == '/' ||
					productionText.charAt(curr+1) == '*')) {
				offset = productionNodeOffset + curr;
				return true;
			}
			return false;
		}
		
		/**
		 * once found the offset of the CommentNode this computes the length
		 * of if, by overreading the comment, also nested ones.
		 * 
		 * @throws ATGProductionReformatterException
		 */
		private void computeLength() throws ATGProductionReformatterException {
			//which one to overread first?
			try {
				if( productionText.charAt(curr) == '/' &&
					productionText.charAt(curr+1) == '/') {
					//lineComment
					overreadLineComment();					
				} else if( productionText.charAt(curr) == '/' &&
					productionText.charAt(curr+1) == '*') {
					//nested Comment
					overreadNestedComment();
				} else {
					throw new ATGProductionReformatterException("start of comment missing");
				}
				length = productionNodeOffset + curr - offset; 
			} catch (StringIndexOutOfBoundsException e) {
				throw new ATGProductionReformatterException(e);
			}
		}

		/**
		 * overreads a nested Comment
		 * 
		 * @throws StringIndexOutOfBoundsException
		 */
		private void overreadNestedComment() throws StringIndexOutOfBoundsException {
			curr+=2;
			while(!(productionText.charAt(curr) == '*' &&
				  productionText.charAt(curr+1) == '/')) {
				if( productionText.charAt(curr) == '/' &&
					productionText.charAt(curr+1) == '*') {
						//nested Comment recursive
						overreadNestedComment();
						curr++;
					}
				curr++;
			}
			curr+=2;
		}

		/**
		 * overreads a line comment
		 * 
		 * @throws StringIndexOutOfBoundsException
		 */
		private void overreadLineComment() throws StringIndexOutOfBoundsException {
			curr+=2;
			while(productionText.charAt(curr) != '\n') {
				curr++;
			}
		}
	}
	
	/**
	 * reformats and writes any CommentNode that may follow after node
	 * 
	 * @param node
	 * @throws ATGProductionReformatterException
	 */
	private void appendCommentsIfNeccesary(AbstractAtgAstNode node) throws ATGProductionReformatterException {
		appendCommentsIfNeccesary(node.getRegion().getOffset() + node.getRegion().getLength());		
	}

	/**
	 * reformats and writes any CommentNode that may follow after offset
	 * 
	 * @param offset
	 * @throws ATGProductionReformatterException
	 */
	private void appendCommentsIfNeccesary(int offset) throws ATGProductionReformatterException {
		CommentNode commentNode = (new CommentNodeParser(offset)).getNode();
		while (commentNode != null) {
			int tab = currLineCounter + 1;
			String[] lines = getString(commentNode).split(Character.toString(LF));
			for (int i = 0; i < lines.length; i++) {
				if (isAfterLF()) {
					appendTab(tab);
				} else {
					write(" ");
				}
				write(lines[i]);
				if (i < (lines.length - 1)) 
					newAtgLineIfNeccesary();			
			}
			//check if there are comments behind the last comment
			offset = commentNode.getRegion().getOffset() + commentNode.getRegion().getLength();
			commentNode = (new CommentNodeParser(offset)).getNode();
		}
	}
	
	/**
	 * computes offset of sym that follows after last
	 * 
	 * @param last
	 * @param sym
	 * @return offset of sym that follows after last or -1 if not found
	 */
	private int getOffsetAfter(AbstractAtgAstNode last, ATGSymbol sym) {
		//rel Start in ProductionText
		int start = last.getRegion().getOffset() + 
				last.getRegion().getLength() - 
				productionNode.getRegion().getOffset();
		
		//rel Offset
		int offset = productionText.indexOf(sym.getSymbol(), start);
		
		if (offset > 0) {
			//abs offset
			return offset + productionNode.getRegion().getOffset();
		}
		return -1;
	}

	/**
	 * reformats and writes sym after node
	 * @param sym
	 * @param node
	 * @throws ATGProductionReformatterException
	 */
	private void writeAtgSymbolAfterNode(ATGSymbol sym, AbstractAtgAstNode node) throws ATGProductionReformatterException {
		int offset = getOffsetAfter(node, sym);
		writeAtgSymbol(sym, offset);
	}

	/**
	 * reformats and writes sym after offset
	 * @param sym
	 * @param offset
	 * @throws ATGProductionReformatterException
	 */
	private void writeAtgSymbol(ATGSymbol sym, int offset) throws ATGProductionReformatterException {
		write(sym.getSymbol());
		if (!sym.isWhiteSpace() && offset > 0) { //if sym is a whitespace the comment was already printed
												 //after the writeNode Routine
			appendCommentsIfNeccesary(offset + sym.getSymbol().length());
		}
	}
	
	/**
	 * reformats and writes a FactorNode containing Parenthesis and a Subexpression
	 * recursively
	 * 
	 * @param factor
	 * @param symIn
	 * @param symOut
	 * @throws ATGProductionReformatterException
	 */
	private void writeParFactorNode(FactorNode factor, ATGSymbol symIn,
			ATGSymbol symOut) throws ATGProductionReformatterException {
		newAtgLineIfNeccesary();
		writeAtgSymbol(symIn, factor.getRegion().getOffset());
		AtgTabIn();
		newAtgLineIfNeccesary();
		writeExpressionNode(factor.getExpressionNode());
		AtgTabOut();				
		newAtgLineIfNeccesary();
		writeAtgSymbol(symOut, factor.getRegion().getOffset() + factor.getRegion().getLength() - 1);
		newAtgLineIfNeccesary();
	}
	
	/**
	 * reformats and writes a Node
	 * 
	 * @param node
	 * @throws ATGProductionReformatterException
	 */
	private void writeNode(AbstractAtgAstNode node)
	throws ATGProductionReformatterException {
		if (node != null) {
			String[] lines = getString(node).split(Character.toString(LF));
			
			for (int i = 0; i < lines.length; i++) {
				if (node instanceof SemTextNode) {
					if (i == 1) {	//tab in second line if in multiline SemText
						JavaTabIn();
					}
					adjustJavaOutTabs(lines[i]);
					appendJavaTab();
				} else {
					if (isAfterLF())
						appendAtgTab();				
				}
				
				write(lines[i].trim());
				
				if (node instanceof SemTextNode) {
					adjustJavaInTabs(lines[i]);
				}
				
				if (i < (lines.length - 1)) 
					newAtgLineIfNeccesary();			
			}
			//look for comments after the node
			appendCommentsIfNeccesary(node);

			//append lf after semText
			if (node instanceof SemTextNode) {
				if (lines.length > 1) { //tab out if this was a multiline SemText
					JavaTabOut();
				}
				newAtgLineIfNeccesary();		
			} 
		}
	}
	
	/**
	 * calculates Java Tabs - if it finds '}' in the
	 * current line Java Tab is reduced.
	 * @param line
	 */
	private void adjustJavaOutTabs(String line) {
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == '}') {
				JavaTabOut();
			}
		}
	}

	/**
	 * calculates Java Tabs - if it finds '{' in the
	 * current line Java Tab is incremented.
	 * @param line
	 */
	private void adjustJavaInTabs(String line) {
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == '{') {
				JavaTabIn();
			}
		}
	}

	/**
	 * decrements ATG Tab
	 */
	private void AtgTabOut() {
		currAtgLineOffset-=info.getTabwith();
	}

	/**
	 * increments ATG Tab
	 */
	private void AtgTabIn() {
		currAtgLineOffset+=info.getTabwith();
	}

	/**
	 * decrements Java Tab
	 */
	private void JavaTabOut() {
		currJavaLineOffset-=info.getTabwith();
	}

	/**
	 * increments Java Tab
	 */
	private void JavaTabIn() {
		currJavaLineOffset+=info.getTabwith();
	}

	/**
	 * writes Atg Tab
	 */
	private void appendAtgTab() {
		appendTab(currAtgLineOffset);
	}

	/**
	 * writes Java Tab
	 */
	private void appendJavaTab() {
		appendTab(currJavaLineOffset);
	}
	
	/**
	 * writes Tabulators until offset is reached
	 * 
	 * @param offset
	 */
	private void appendTab(int offset) {
		for (int i = currLineCounter; i < offset; i = (i / info.getTabwith()) * info.getTabwith() + info.getTabwith()) { 
			outBuf.append("\t");
			currLineCounter += info.getTabwith();
		}
	}
	
	/**
	 * 
	 * @return true if last written char was a LF
	 */
	private boolean isAfterLF() {
		return outBuf.length() > 0 && outBuf.charAt(outBuf.length()-1) == LF;
	}
	
	/**
	 * writes a new line with ATG Tabulator unless not already done before 
	 */
	private void newAtgLineIfNeccesary() {
		newLineIfNeccesary();
		appendAtgTab();
	}

	/**
	 * writes a new line unless not already done before 
	 */
	private void newLineIfNeccesary() {
		if (!isAfterLF()) {
			newLine();
		}
	}

	/**
	 * writes a new line 
	 */
	private void newLine() {
		outBuf.append(LF);
		currLineCounter = 0;
	}
	
	/**
	 * writes a String
	 * 
	 * @param string
	 */
	private void write(String string) {
		outBuf.append(string);
		currLineCounter += string.length();
	}

	/**
	 * gets StringRepresentation of node
	 * 
	 * @param node
	 * @return
	 * @throws ATGProductionReformatterException
	 */
	private String getString(AbstractAtgAstNode node) throws ATGProductionReformatterException {
		try {
			return document.get(
				node.getRegion().getOffset(), 
				node.getRegion().getLength());
		} catch (BadLocationException e) {
			throw new ATGProductionReformatterException(e);
		}
	}
	
	/**
	 * computes an estimate for the optimal Java Tabulator Offset in this Production
	 * 
	 * @return estimated java Tabulator Offset
	 * @throws ATGProductionReformatterException
	 */
	private int estimateOptimalJavaOffset() throws ATGProductionReformatterException {
		String production = getString(productionNode);
		ArrayList<String> atgLines = new ArrayList<String>();
		boolean isInAtgCode = true;
		int beg = 0;
		for (int i = 0; i < production.length(); i++) {
			if (isInAtgCode) {
				switch (production.charAt(i)) {
				case '\n':
					if ((i-1) > beg)
						atgLines.add(trimEndOfString(production.substring(beg, i)));
					beg = i+1;
					break;
				case '(':
					if (i < production.length()-1) {
						if (production.charAt(i+1) == '.') {
							if ((i-1) > beg)
								atgLines.add(trimEndOfString(production.substring(beg, i)));
							beg = i+2;
							isInAtgCode = false;
						}
					}					
					break;
				default:
					break;
				}
			} else {
				switch (production.charAt(i)) {
				case '.':
					if (i < production.length()-1) {
						if (production.charAt(i+1) == ')') {
							isInAtgCode = true;
							beg = i+2;
						}
					}					
					break;
				default:
					break;
				}				
			}			
		}
		
		int max = 0;
		for (String s : atgLines) {
			if (s.length() > max) {
				max = s.length();
			}
		}
		return max;
	}

	/**
	 * removes whitespaces from end of String
	 * 
	 * @param trim
	 * @return
	 */
	private String trimEndOfString(String trim) {
		StringBuilder sb = new StringBuilder(trim);
		while(sb.length() > 0 && 
			(sb.charAt(sb.length()-1) == ' ' || sb.charAt(sb.length()-1) == '\t')) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
}