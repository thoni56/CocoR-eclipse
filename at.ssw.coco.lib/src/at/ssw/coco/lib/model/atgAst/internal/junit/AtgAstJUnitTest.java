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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import at.ssw.coco.lib.model.atgAst.AtgAst;
import at.ssw.coco.lib.model.atgAst.nodeTypes.AbstractAtgAstNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.AttrDeclNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.AttribsNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.CommentDeclNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.CompilerNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.ExpressionNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.FactorNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.GlobalFieldNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.IdentNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.ImportsNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.InAttribsNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.OpSetNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.OutAttribsNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.ParserSpecNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.ProductionNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.ResolverNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.RootNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.ScannerSpecNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.SemTextNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.SetDeclNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.SetNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.SimSetNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.SymbolNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.TermNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.TokenDeclNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.TokenExprNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.TokenFactorNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.TokenTermNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.WhiteSpaceDeclNode;
import at.ssw.coco.lib.model.atgAst.nodeTypes.FactorNode.Kind;
import at.ssw.coco.lib.model.atgAst.nodeTypes.OpSetNode.SetOp;

/**
 * implements JUnitTest Cases for ATG Abstract Syntax Tree
 * a test-file (junitTest.atg) is parsed and an AST is generated.
 * This AST is matched to hard-coded reference Values in the test 
 * routines
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class AtgAstJUnitTest {

	/**
	 * Abstract Syntax Tree
	 */
	private static AtgAst atgAst;
	
	/**
	 * root node of AST
	 */
	private static RootNode root;
	
	/**
	 * relative path from working directory of test-file (junitTest.atg)  
	 */
	private final static String PATH = "src/at/ssw/coco/lib/atgAst/internal/junit/junitTest.atg";

	@BeforeClass
	public static void beforeClass() throws FileNotFoundException {
		File testFile = new File(PATH);
		atgAst = new AtgAst(new FileInputStream(testFile));	
		root = atgAst.getRoot();
	}
	
	@Test
	public void testRootNode() {
		//Root Node
		root = atgAst.getRoot();
		checkNode(root, 0, 1198, null);
		
		//Children Nodes
		assertNotNull(root.getImportsNode());
		assertNotNull(root.getCompilerNode());
	}
	
	@Test
	public void testImportsNode() {
		ImportsNode importsNode = root.getImportsNode();
		checkNode(importsNode, 0, 52, root);
	}
	
	@Test
	public void testCompilerNode() {
		CompilerNode compilerNode = root.getCompilerNode();
		checkNode(compilerNode, 54, 1144, root);

		//Children Nodes
		assertNotNull(compilerNode.getIdentNode());
		assertNotNull(compilerNode.getGlobalFieldNode());
		assertNotNull(compilerNode.getScannerSpecNode());
		assertNotNull(compilerNode.getParserSpecNode());
	}
	
	@Test
	public void testCompilerIdentNode() {
		//Compiler Ident
		IdentNode identNode = root.getCompilerNode().getIdentNode();
		checkNode(identNode, 63, 13, root.getCompilerNode());
		//Value
		assertEquals(IdentNode.Kind.COMPILER, identNode.getKind());
		assertTrue(identNode.getIdent().equals("JunitTestFile"));
	}

	@Test
	public void testGlobalFieldNode() {
		GlobalFieldNode globalFieldNode = root.getCompilerNode().getGlobalFieldNode();
		checkNode(globalFieldNode, 79, 46, root.getCompilerNode());
	}
	
	@Test
	public void testScannerSpecNode() {
		//ScannerSpecNode
		ScannerSpecNode scannerSpecNode = root.getCompilerNode().getScannerSpecNode();
		checkNode(scannerSpecNode, 127, 321, root.getCompilerNode());
		
		//IgnoreCaseFlag
		assertTrue(scannerSpecNode.isIgnoreCase());

		//Children Nodes
		assertNotNull(scannerSpecNode.getSetDeclNodes());
		assertFalse(scannerSpecNode.getSetDeclNodes().isEmpty());
		
		assertNotNull(scannerSpecNode.getTokenDeclNodes());
		assertFalse(scannerSpecNode.getTokenDeclNodes().isEmpty());
		
		assertNotNull(scannerSpecNode.getPragmaDeclNodes());
		assertFalse(scannerSpecNode.getPragmaDeclNodes().isEmpty());		
		
		assertNotNull(scannerSpecNode.getCommentDeclNodes());
		assertFalse(scannerSpecNode.getCommentDeclNodes().isEmpty());
		
		assertNotNull(scannerSpecNode.getWhiteSpaceDeclNodes());
		assertFalse(scannerSpecNode.getWhiteSpaceDeclNodes().isEmpty());		
	}
	
	@Test
	public void testSetDeclNodes() {
		List<SetDeclNode> setDeclNodes = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes();
		assertNotNull(setDeclNodes);
		assertEquals(4, setDeclNodes.size());		
	}

	@Test
	public void testSetDeclNode0() {
		SetDeclNode setDeclNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(0);
		checkNode(setDeclNode, 151, 29, root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(setDeclNode.getIdentNode());
		assertNotNull(setDeclNode.getSetNode());

		//IdentNode
		IdentNode identNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(0).getIdentNode();
		checkNode(identNode, 151, 6, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(0));
		//Value
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());
		assertTrue(identNode.getIdent().equals("letter"));

		//SetNode
		SetNode setNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(0).getSetNode();
		checkNode(setNode, 160, 19, 
				root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(0));

		//SimSetNode1
		assertNotNull(setNode.getSimSetNode());
		SimSetNode simSetNode = setNode.getSimSetNode();
		checkNode(simSetNode, 160, 8,
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(0)
					.getSetNode());
		assertTrue(simSetNode.getKind() == SimSetNode.Kind.CHAR_RANGE);
		assertTrue(simSetNode.getFrom() == 'A');
		assertTrue(simSetNode.getTo() == 'Z');
		
		//OpSetNode
		assertNotNull(setNode.getOpSetNodes().get(0));
		OpSetNode opSetNode = setNode.getOpSetNodes().get(0);
		checkNode(opSetNode, 169, 10,
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(0)
					.getSetNode());
		assertTrue(opSetNode.getSetOp() == SetOp.SETOP_PLUS);
		
		//SimSetNode2		
		assertNotNull(opSetNode.getSimSetNode());
		simSetNode = opSetNode.getSimSetNode();
		checkNode(simSetNode, 171, 8,
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(0)
					.getSetNode()
					.getOpSetNodes()
					.get(0));
		assertTrue(simSetNode.getKind() == SimSetNode.Kind.CHAR_RANGE);
		assertTrue(simSetNode.getFrom() == 'a');
		assertTrue(simSetNode.getTo() == 'z');
	}
	
	@Test
	public void testSetDeclNode1() {
		SetDeclNode setDeclNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(1);
		checkNode(setDeclNode, 182, 21, root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(setDeclNode.getIdentNode());
		assertNotNull(setDeclNode.getSetNode());

		//IdentNode
		IdentNode identNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(1).getIdentNode();
		checkNode(identNode, 182, 5, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(1));
		//Value
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());
		assertTrue(identNode.getIdent().equals("digit"));

		//SetNode
		SetNode setNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(1).getSetNode();
		checkNode(setNode, 190, 12, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(1));

		//SimSetNode
		assertNotNull(setNode.getSimSetNode());
		SimSetNode simSetNode = setNode.getSimSetNode();
		checkNode(simSetNode, 190, 12,
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(1)
					.getSetNode());
		assertTrue(simSetNode.getKind() == SimSetNode.Kind.STRING);
		assertTrue(simSetNode.getString().equals("0123456789"));
	}	

	@Test
	public void testSetDeclNode2() {
		SetDeclNode setDeclNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(2);
		checkNode(setDeclNode, 205, 10, root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(setDeclNode.getIdentNode());
		assertNotNull(setDeclNode.getSetNode());

		//IdentNode
		IdentNode identNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(2).getIdentNode();
		checkNode(identNode, 205, 2, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(2));
		//Value
		assertTrue(identNode.getIdent().equals("cr"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());

		//SetNode
		SetNode setNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(2).getSetNode();
		checkNode(setNode, 210, 4, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(2));

		//SimSetNode
		assertNotNull(setNode.getSimSetNode());
		SimSetNode simSetNode = setNode.getSimSetNode();
		checkNode(simSetNode, 210, 4,
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(2)
					.getSetNode());
		assertTrue(simSetNode.getKind() == SimSetNode.Kind.CHAR_RANGE);
		assertTrue(simSetNode.getFrom() == '\r');
		assertTrue(simSetNode.getTo() == '\r');
	}	

	@Test
	public void testSetDeclNode3() {
		SetDeclNode setDeclNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(3);
		checkNode(setDeclNode, 217, 10, root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(setDeclNode.getIdentNode());
		assertNotNull(setDeclNode.getSetNode());

		//IdentNode
		IdentNode identNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(3).getIdentNode();
		checkNode(identNode, 217, 2, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(3));
		//Value
		assertTrue(identNode.getIdent().equals("lf"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());

		//SetNode
		SetNode setNode = root.getCompilerNode().getScannerSpecNode().getSetDeclNodes().get(3).getSetNode();
		checkNode(setNode, 222, 4, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(3));

		//SimSetNode
		assertNotNull(setNode.getSimSetNode());
		SimSetNode simSetNode = setNode.getSimSetNode();
		checkNode(simSetNode, 222, 4,
				root.getCompilerNode()
					.getScannerSpecNode()
					.getSetDeclNodes()
					.get(3)
					.getSetNode());
		assertTrue(simSetNode.getKind() == SimSetNode.Kind.CHAR_RANGE);
		assertTrue(simSetNode.getFrom() == '\n');
		assertTrue(simSetNode.getTo() == '\n');
	}	
	
	@Test
	public void testTokenDeclNodes() {
		List<TokenDeclNode> tokenDeclNodes = root.getCompilerNode().getScannerSpecNode().getTokenDeclNodes();
		assertNotNull(tokenDeclNodes);
		assertEquals(2, tokenDeclNodes.size());		
	}
	
	@Test
	public void testTokenDeclNode0() {
		TokenDeclNode tokenDeclNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getTokenDeclNodes()
							.get(0);
		checkNode(tokenDeclNode, 237, 32,
				root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(tokenDeclNode.getSymbolNode());
		assertNotNull(tokenDeclNode.getTokenExprNode());
		assertNull(tokenDeclNode.getSemTextNode());
	}
	
	@Test
	public void testTokenDeclNode0_Symbol() {
		SymbolNode symbolNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getTokenDeclNodes()
							.get(0)
							.getSymbolNode();
		checkNode(symbolNode, 237, 5, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getTokenDeclNodes()
					.get(0));

		//Children Nodes
		assertNotNull(symbolNode.getIdentNode());

		//Value
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());
	}
	@Test
	public void testTokenDeclNode0_Symbol_Ident() {
		IdentNode identNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getTokenDeclNodes()
							.get(0)
							.getSymbolNode()
							.getIdentNode();
		checkNode(identNode, 237, 5, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getTokenDeclNodes()
					.get(0)
					.getSymbolNode());

		//Value
		assertTrue(identNode.getIdent().equals("ident"));
		assertEquals(IdentNode.Kind.TOKEN, identNode.getKind());
	}
	
	@Test
	public void testTokenDeclNode0_TokenExprNode() {
		TokenExprNode tokenExprNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getTokenDeclNodes()
							.get(0)
							.getTokenExprNode();
		checkNode(tokenExprNode, 245, 23, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getTokenDeclNodes()
					.get(0));

		//Children Nodes
		assertNotNull(tokenExprNode.getTokenTermNodes());
		assertEquals(1, tokenExprNode.getTokenTermNodes().size());
		
		//TokenTerm
		TokenTermNode tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 245, 23, tokenExprNode);
		assertNotNull(tokenTermNode.getTokenFactorNodes());
		assertEquals(2, tokenTermNode.getTokenFactorNodes().size());
		assertNull(tokenTermNode.getContextTokenExprNode());
		
		//TokenFactor 0
		TokenFactorNode tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 245, 6, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());
		
		//TokenFactor 0 Symbol
		SymbolNode symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 245, 6, tokenFactorNode);
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());

		//TokenFactor 0 Symbol Ident
		IdentNode identNode = symbolNode.getIdentNode();
		checkNode(identNode, 245, 6, symbolNode);
		assertTrue(identNode.getIdent().equals("letter"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());
		
		//TokenFactor 1
		tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(1);
		checkNode(tokenFactorNode, 252, 16, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.ARBITRARY_OFTEN, tokenFactorNode.getKind());
		
		//TokenFactor 1 TokenExpr
		tokenExprNode = tokenFactorNode.getTokenExprNode();
		checkNode(tokenExprNode, 253, 14, tokenFactorNode);
		assertEquals(2, tokenExprNode.getTokenTermNodes().size());
		
		//TokenFactor 1 TokenExpr TokenTerm 0
		tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 253, 6, tokenExprNode);

		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0
		tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 253, 6, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());

		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol
		symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 253, 6, tokenFactorNode);
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());
		
		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol Ident
		identNode = symbolNode.getIdentNode();
		checkNode(identNode, 253, 6, symbolNode);
		assertTrue(identNode.getIdent().equals("letter"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());

		//TokenFactor 1 TokenExpr TokenTerm 1
		tokenTermNode = tokenExprNode.getTokenTermNodes().get(1);
		checkNode(tokenTermNode, 262, 5, tokenExprNode);

		//TokenFactor 1 TokenExpr TokenTerm 1 TokenFactor 0
		tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 262, 5, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());

		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol
		symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 262, 5, tokenFactorNode);
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());
		
		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol Ident
		identNode = symbolNode.getIdentNode();
		checkNode(identNode, 262, 5, symbolNode);
		assertTrue(identNode.getIdent().equals("digit"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());
	}

	
	
	@Test
	public void testTokenDeclNode1() {
		TokenDeclNode tokenDeclNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getTokenDeclNodes()
							.get(1);
		checkNode(tokenDeclNode, 271, 23,
				root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(tokenDeclNode.getSymbolNode());
		assertNotNull(tokenDeclNode.getTokenExprNode());
		assertNull(tokenDeclNode.getSemTextNode());
	}

	@Test
	public void testTokenDeclNode1_Symbol() {
		SymbolNode symbolNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getTokenDeclNodes()
							.get(1)
							.getSymbolNode();
		checkNode(symbolNode, 271, 6, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getTokenDeclNodes()
					.get(1));

		//Children Nodes
		assertNotNull(symbolNode.getIdentNode());

		//Value
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());
	}
	@Test
	public void testTokenDeclNode1_Symbol_Ident() {
		IdentNode identNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getTokenDeclNodes()
							.get(1)
							.getSymbolNode()
							.getIdentNode();
		checkNode(identNode, 271, 6, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getTokenDeclNodes()
					.get(1)
					.getSymbolNode());

		//Value
		assertTrue(identNode.getIdent().equals("number"));
		assertEquals(IdentNode.Kind.TOKEN, identNode.getKind());
	}
	
	@Test
	public void testTokenDeclNode1_TokenExprNode() {
		TokenExprNode tokenExprNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getTokenDeclNodes()
							.get(1)
							.getTokenExprNode();
		checkNode(tokenExprNode, 280, 13, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getTokenDeclNodes()
					.get(1));

		//Children Nodes
		assertNotNull(tokenExprNode.getTokenTermNodes());
		assertEquals(1, tokenExprNode.getTokenTermNodes().size());
		
		//TokenTerm
		TokenTermNode tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 280, 13, tokenExprNode);
		assertNotNull(tokenTermNode.getTokenFactorNodes());
		assertEquals(2, tokenTermNode.getTokenFactorNodes().size());
		assertNull(tokenTermNode.getContextTokenExprNode());
		
		//TokenFactor 0
		TokenFactorNode tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 280, 5, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());
		
		//TokenFactor 0 Symbol
		SymbolNode symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 280, 5, tokenFactorNode);
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());

		//TokenFactor 0 Symbol Ident
		IdentNode identNode = symbolNode.getIdentNode();
		checkNode(identNode, 280, 5, symbolNode);
		assertTrue(identNode.getIdent().equals("digit"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());
		
		//TokenFactor 1
		tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(1);
		checkNode(tokenFactorNode, 286, 7, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.ARBITRARY_OFTEN, tokenFactorNode.getKind());
		
		//TokenFactor 1 TokenExpr
		tokenExprNode = tokenFactorNode.getTokenExprNode();
		checkNode(tokenExprNode, 287, 5, tokenFactorNode);
		assertEquals(1, tokenExprNode.getTokenTermNodes().size());
		
		//TokenFactor 1 TokenExpr TokenTerm 0
		tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 287, 5, tokenExprNode);

		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0
		tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 287, 5, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());

		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol
		symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 287, 5, tokenFactorNode);
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());
		
		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol Ident
		identNode = symbolNode.getIdentNode();
		checkNode(identNode, 287, 5, symbolNode);
		assertTrue(identNode.getIdent().equals("digit"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());
	}


	
	@Test
	public void testPragmaDeclNodes() {
		List<TokenDeclNode> tokenDeclNodes = 
			root.getCompilerNode().getScannerSpecNode().getPragmaDeclNodes();
		assertNotNull(tokenDeclNodes);
		assertEquals(1, tokenDeclNodes.size());		
	}
	
	@Test
	public void testPragmaDeclNode0() {
		TokenDeclNode tokenDeclNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getPragmaDeclNodes()
							.get(0);
		checkNode(tokenDeclNode, 305, 67,
				root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(tokenDeclNode.getSymbolNode());
		assertNotNull(tokenDeclNode.getTokenExprNode());
		assertNotNull(tokenDeclNode.getSemTextNode());
	}

	@Test
	public void testPragmaDeclNode0_Symbol() {
		SymbolNode symbolNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getPragmaDeclNodes()
							.get(0)
							.getSymbolNode();
		checkNode(symbolNode, 305, 6, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getPragmaDeclNodes()
					.get(0));

		//Children Nodes
		assertNotNull(symbolNode.getIdentNode());

		//Value
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());
	}
	@Test
	public void testPragmaDeclNode0_Symbol_Ident() {
		IdentNode identNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getPragmaDeclNodes()
							.get(0)
							.getSymbolNode()
							.getIdentNode();
		checkNode(identNode, 305, 6, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getPragmaDeclNodes()
					.get(0)
					.getSymbolNode());

		//Value
		assertTrue(identNode.getIdent().equals("switch"));
		assertEquals(IdentNode.Kind.PRAGMA, identNode.getKind());
	}
	
	@Test
	public void testPragmaDeclNode0_TokenExprNode() {
		TokenExprNode tokenExprNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getPragmaDeclNodes()
							.get(0)
							.getTokenExprNode();
		checkNode(tokenExprNode, 314, 22, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getPragmaDeclNodes()
					.get(0));

		//Children Nodes
		assertNotNull(tokenExprNode.getTokenTermNodes());
		assertEquals(1, tokenExprNode.getTokenTermNodes().size());
		
		//TokenTerm
		TokenTermNode tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 314, 22, tokenExprNode);
		assertNotNull(tokenTermNode.getTokenFactorNodes());
		assertEquals(2, tokenTermNode.getTokenFactorNodes().size());
		assertNull(tokenTermNode.getContextTokenExprNode());
		
		//TokenFactor 0
		TokenFactorNode tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 314, 3, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());
		
		//TokenFactor 0 Symbol
		SymbolNode symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 314, 3, tokenFactorNode);
		assertEquals(SymbolNode.Kind.CHAR, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals('$', symbolNode.getCharacter());
		
		//TokenFactor 1
		tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(1);
		checkNode(tokenFactorNode, 318, 18, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.ARBITRARY_OFTEN, tokenFactorNode.getKind());
		
		//TokenFactor 1 TokenExpr
		tokenExprNode = tokenFactorNode.getTokenExprNode();
		checkNode(tokenExprNode, 320, 14, tokenFactorNode);
		assertEquals(2, tokenExprNode.getTokenTermNodes().size());
		
		//TokenFactor 1 TokenExpr TokenTerm 0
		tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 320, 5, tokenExprNode);

		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0
		tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 320, 5, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());

		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol
		symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 320, 5, tokenFactorNode);
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());
		
		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol Ident
		IdentNode identNode = symbolNode.getIdentNode();
		checkNode(identNode, 320, 5, symbolNode);
		assertTrue(identNode.getIdent().equals("digit"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());


		//TokenFactor 1 TokenExpr TokenTerm 1
		tokenTermNode = tokenExprNode.getTokenTermNodes().get(1);
		checkNode(tokenTermNode, 328, 6, tokenExprNode);

		//TokenFactor 1 TokenExpr TokenTerm 1 TokenFactor 0
		tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 328, 6, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());

		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol
		symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 328, 6, tokenFactorNode);
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());
		
		//TokenFactor 1 TokenExpr TokenTerm 0 TokenFactor 0 Symbol Ident
		identNode = symbolNode.getIdentNode();
		checkNode(identNode, 328, 6, symbolNode);
		assertTrue(identNode.getIdent().equals("letter"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());

	}

	@Test
	public void testPragmaDeclNode0_SemText() {
		SemTextNode semTextNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getPragmaDeclNodes()
							.get(0)
							.getSemTextNode();
		checkNode(semTextNode, 338, 34, 
				root.getCompilerNode()
					.getScannerSpecNode()
					.getPragmaDeclNodes()
					.get(0));
	}

	@Test
	public void testCommentDeclNodes() {
		List<CommentDeclNode> commentDeclNodes = 
			root.getCompilerNode().getScannerSpecNode().getCommentDeclNodes();
		assertNotNull(commentDeclNodes);
		assertEquals(2, commentDeclNodes.size());		
	}

	@Test
	public void testCommentDeclNode0() {
		CommentDeclNode commentDeclNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getCommentDeclNodes()
							.get(0);
		checkNode(commentDeclNode, 374, 33,
				root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(commentDeclNode.getFrom());
		assertNotNull(commentDeclNode.getTo());
		assertTrue(commentDeclNode.isNested());
	}

	@Test
	public void testCommentDeclNode0_From_TokenExpr() {
		TokenExprNode tokenExprNode = root
				.getCompilerNode()
				.getScannerSpecNode()
				.getCommentDeclNodes()
				.get(0)
				.getFrom();
		checkNode(tokenExprNode, 388, 4, 
				root.getCompilerNode()
				.getScannerSpecNode()
				.getCommentDeclNodes()
				.get(0));

		//Children Nodes
		assertNotNull(tokenExprNode.getTokenTermNodes());
		assertEquals(1, tokenExprNode.getTokenTermNodes().size());
		
		//TokenTerm
		TokenTermNode tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 388, 4, tokenExprNode);
		assertNotNull(tokenTermNode.getTokenFactorNodes());
		assertEquals(1, tokenTermNode.getTokenFactorNodes().size());
		assertNull(tokenTermNode.getContextTokenExprNode());
		
		//TokenFactor 0
		TokenFactorNode tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 388, 4, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());
		
		//TokenFactor 0 Symbol
		SymbolNode symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 388, 4, tokenFactorNode);
		assertEquals(SymbolNode.Kind.STRING, symbolNode.getKind());
		assertTrue(symbolNode.getString().equals("/*"));
	}
	
	@Test
	public void testCommentDeclNode0_To_TokenExpr() {
		TokenExprNode tokenExprNode = root
				.getCompilerNode()
				.getScannerSpecNode()
				.getCommentDeclNodes()
				.get(0)
				.getTo();
		checkNode(tokenExprNode, 396, 4, 
				root.getCompilerNode()
				.getScannerSpecNode()
				.getCommentDeclNodes()
				.get(0));

		//Children Nodes
		assertNotNull(tokenExprNode.getTokenTermNodes());
		assertEquals(1, tokenExprNode.getTokenTermNodes().size());
		
		//TokenTerm
		TokenTermNode tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 396, 4, tokenExprNode);
		assertNotNull(tokenTermNode.getTokenFactorNodes());
		assertEquals(1, tokenTermNode.getTokenFactorNodes().size());
		assertNull(tokenTermNode.getContextTokenExprNode());
		
		//TokenFactor 0
		TokenFactorNode tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 396, 4, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());
		
		//TokenFactor 0 Symbol
		SymbolNode symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 396, 4, tokenFactorNode);
		assertEquals(SymbolNode.Kind.STRING, symbolNode.getKind());
		assertTrue(symbolNode.getString().equals("*/"));
	}

	@Test
	public void testCommentDeclNode1() {
		CommentDeclNode commentDeclNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getCommentDeclNodes()
							.get(1);
		checkNode(commentDeclNode, 408, 24,
				root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(commentDeclNode.getFrom());
		assertNotNull(commentDeclNode.getTo());
		assertFalse(commentDeclNode.isNested());
	}

	@Test
	public void testCommentDeclNode1_From_TokenExpr() {
		TokenExprNode tokenExprNode = root
				.getCompilerNode()
				.getScannerSpecNode()
				.getCommentDeclNodes()
				.get(1)
				.getFrom();
		checkNode(tokenExprNode, 422, 4, 
				root.getCompilerNode()
				.getScannerSpecNode()
				.getCommentDeclNodes()
				.get(1));

		//Children Nodes
		assertNotNull(tokenExprNode.getTokenTermNodes());
		assertEquals(1, tokenExprNode.getTokenTermNodes().size());
		
		//TokenTerm
		TokenTermNode tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 422, 4, tokenExprNode);
		assertNotNull(tokenTermNode.getTokenFactorNodes());
		assertEquals(1, tokenTermNode.getTokenFactorNodes().size());
		assertNull(tokenTermNode.getContextTokenExprNode());
		
		//TokenFactor 0
		TokenFactorNode tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 422, 4, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());
		
		//TokenFactor 0 Symbol
		SymbolNode symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 422, 4, tokenFactorNode);
		assertEquals(SymbolNode.Kind.STRING, symbolNode.getKind());
		assertTrue(symbolNode.getString().equals("//"));
	}
	
	@Test
	public void testCommentDeclNode1_To_TokenExpr() {
		TokenExprNode tokenExprNode = root
				.getCompilerNode()
				.getScannerSpecNode()
				.getCommentDeclNodes()
				.get(1)
				.getTo();
		checkNode(tokenExprNode, 430, 2, 
				root.getCompilerNode()
				.getScannerSpecNode()
				.getCommentDeclNodes()
				.get(1));

		//Children Nodes
		assertNotNull(tokenExprNode.getTokenTermNodes());
		assertEquals(1, tokenExprNode.getTokenTermNodes().size());
		
		//TokenTerm
		TokenTermNode tokenTermNode = tokenExprNode.getTokenTermNodes().get(0);
		checkNode(tokenTermNode, 430, 2, tokenExprNode);
		assertNotNull(tokenTermNode.getTokenFactorNodes());
		assertEquals(1, tokenTermNode.getTokenFactorNodes().size());
		assertNull(tokenTermNode.getContextTokenExprNode());
		
		//TokenFactor 0
		TokenFactorNode tokenFactorNode = tokenTermNode.getTokenFactorNodes().get(0);
		checkNode(tokenFactorNode, 430, 2, tokenTermNode);
		assertEquals(TokenFactorNode.Kind.SYMBOL, tokenFactorNode.getKind());
		
		//TokenFactor 0 Symbol
		SymbolNode symbolNode = tokenFactorNode.getSymbolNode();
		checkNode(symbolNode, 430, 2, tokenFactorNode);
		assertEquals(SymbolNode.Kind.IDENT, symbolNode.getKind());
		assertNull(symbolNode.getString());
		assertEquals(0, symbolNode.getCharacter());

		//TokenFactor 0 Symbol Ident
		IdentNode identNode = symbolNode.getIdentNode();
		checkNode(identNode, 430, 2, symbolNode);
		assertTrue(identNode.getIdent().equals("lf"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, identNode.getKind());
	}
	
	
	@Test
	public void testWhiteSpaceDeclNodes() {
		List<WhiteSpaceDeclNode> whiteSpaceDeclNodes = 
			root.getCompilerNode().getScannerSpecNode().getWhiteSpaceDeclNodes();
		assertNotNull(whiteSpaceDeclNodes);
		assertEquals(1, whiteSpaceDeclNodes.size());		
	}

	@Test
	public void testWhiteSpaceDeclNode0() {
		WhiteSpaceDeclNode whiteSpaceDeclNode = root
							.getCompilerNode()
							.getScannerSpecNode()
							.getWhiteSpaceDeclNodes()
							.get(0);
		checkNode(whiteSpaceDeclNode, 434, 14,
				root.getCompilerNode().getScannerSpecNode());

		//Children Nodes
		assertNotNull(whiteSpaceDeclNode.getSetNode());
		
		//SetNode
		SetNode setNode = whiteSpaceDeclNode.getSetNode();
		checkNode(setNode, 441, 7, whiteSpaceDeclNode);
		assertNotNull(setNode.getSimSetNode());
		assertNotNull(setNode.getOpSetNodes());
		
		//SimSetNode 0
		SimSetNode simSetNode = setNode.getSimSetNode();
		checkNode(simSetNode, 441, 2, setNode);
		checkNode(simSetNode.getIdentNode(), 441, 2, simSetNode);
		assertTrue(simSetNode.getIdentNode().getIdent().equals("cr"));
		assertEquals(IdentNode.Kind.CHARACTER_SET, simSetNode.getIdentNode().getKind());
		
		//OpSetNodes 
		List<OpSetNode> opSetNodes = setNode.getOpSetNodes();
		assertNotNull(opSetNodes);
		assertEquals(1, opSetNodes.size());
		
		//OpSetNode
		OpSetNode opSetNode = opSetNodes.get(0);
		checkNode(opSetNode, 444, 4, setNode);
		assertEquals(OpSetNode.SetOp.SETOP_PLUS, opSetNode.getSetOp());
		assertNotNull(opSetNode.getSimSetNode());
		
		//SimSetNode 1
		simSetNode = opSetNode.getSimSetNode();
		checkNode(simSetNode, 446, 2, opSetNode);
		checkNode(simSetNode.getIdentNode(), 446, 2, simSetNode);
		assertTrue(simSetNode.getIdentNode().getIdent().equals("lf"));		
		assertEquals(IdentNode.Kind.CHARACTER_SET, simSetNode.getIdentNode().getKind());
	}
	
	@Test
	public void testParserSpecNode() {
		ParserSpecNode parserSpecNode = root.getCompilerNode().getParserSpecNode();
		checkNode(parserSpecNode, 450, 728, root.getCompilerNode());

		//Children Nodes
		assertNotNull(parserSpecNode.getProductionNodes());
	}
	
	@Test
	public void testProductionNodes() {
		List<ProductionNode> productionNodes = 
			root.getCompilerNode().getParserSpecNode().getProductionNodes();
		assertNotNull(productionNodes);
		assertEquals(3, productionNodes.size());		
	}
	
	@Test
	public void testProductionNode0() {
		ProductionNode productionNode = 
			root.getCompilerNode().getParserSpecNode().getProductionNodes().get(0);
		checkNode(productionNode, 463, 319, root.getCompilerNode().getParserSpecNode());
		
		//Ident
		IdentNode identNode = productionNode.getIdentNode();
		checkNode(identNode, 463, 13, productionNode);
		assertTrue(identNode.getIdent().equals("JunitTestFile"));

		//AttrDecl
		AttrDeclNode attrDeclNode = productionNode.getAttrDeclNode();
		assertNull(attrDeclNode);
		
		//SemText
		SemTextNode semTextNode = productionNode.getSemTextNode();
		checkNode(semTextNode, 486, 124, productionNode);
	}

	@Test
	public void testProductionNode0_Expression() {
		ProductionNode productionNode = 
			root.getCompilerNode().getParserSpecNode().getProductionNodes().get(0);
		ExpressionNode expressionNode = productionNode.getExpressionNode();		
		checkExpressionNode(expressionNode, 627, 152, productionNode, 1);
		
		//Term0
		TermNode term0 = expressionNode.getTermNodes().get(0);
		checkTermNode(term0, 627, 152, expressionNode, 1);
					
			//Term0 - Factor0
			FactorNode factor0 = term0.getFactorNodes().get(0);
			checkFactorNode(factor0, 627, 152, term0, Kind.ARBITRARY_OFTEN);
			
				//Term0 - Factor0 - Expression
				ExpressionNode expr = factor0.getExpressionNode();
				checkExpressionNode(expr, 630, 147, factor0, 1);
									
					//Term0 - Factor0 - Expression - Term0
					term0 = expr.getTermNodes().get(0);
					checkTermNode(term0, 630, 147, expr, 2);
						
						//Term0 - Factor0 - Expression - Term0 - Factor0
						factor0 = term0.getFactorNodes().get(0);
						checkFactorNode(factor0, 630, 36, term0, Kind.SYMBOL, true);
			
							//Term0 - Factor0 - Expression - Term0 - Factor0 - Symbol
							SymbolNode sym = factor0.getSymbolNode();
							checkSymbolNode(sym, 630, 9, factor0, SymbolNode.Kind.IDENT);
							
							IdentNode ident = sym.getIdentNode();
							checkIdentNode(ident, 630, 9, sym, IdentNode.Kind.PRODUCTION, "Statement");
							
							//Term0 - Factor0 - Expression - Term0 - Factor0 - Attribs
							AttribsNode attribs = factor0.getAttribsNode();
							checkAttribsNode(attribs, 639, 27, factor0, true, true);

								//Term0 - Factor0 - Expression - Term0 - Factor0 - Attribs - Out
								OutAttribsNode out = attribs.getOutAttribsNode();
								checkNode(out, 640, 15, attribs);
								
								//Term0 - Factor0 - Expression - Term0 - Factor0 - Attribs - In
								InAttribsNode in = attribs.getInAttribsNode();
								checkNode(in, 657, 8, attribs);

						//Term0 - Factor0 - Expression - Term0 - Factor1
						FactorNode factor1 = term0.getFactorNodes().get(1);
						checkFactorNode(factor1, 668, 109, term0, Kind.SEM_TEXT);
					
							//Term0 - Factor0 - Expression - Term0 - Factor1 - SemText
							SemTextNode semText = factor1.getSemTextNode();
							checkNode(semText, 668, 109, factor1);
							
	}

	@Test
	public void testProductionNode1() {
		ProductionNode productionNode = 
			root.getCompilerNode().getParserSpecNode().getProductionNodes().get(1);
		checkNode(productionNode, 784, 277, root.getCompilerNode().getParserSpecNode());
		
		//Ident
		IdentNode identNode = productionNode.getIdentNode();
		checkNode(identNode, 784, 9, productionNode);
		assertTrue(identNode.getIdent().equals("Statement"));

		//AttrDecl
		AttrDeclNode attrDeclNode = productionNode.getAttrDeclNode();
		checkNode(attrDeclNode, 793, 35, productionNode);
		
			//AttrDecl Out
			attrDeclNode.getOutAttrDeclNode();
			//AttrDecl In
			attrDeclNode.getInAttrDeclNode();
		
		//SemText
		SemTextNode semTextNode = productionNode.getSemTextNode();
		checkNode(semTextNode, 831, 22, productionNode);
	}
	
	@Test
	public void testProductionNode1_Expression() {
		ProductionNode productionNode = 
			root.getCompilerNode().getParserSpecNode().getProductionNodes().get(1);
		ExpressionNode expressionNode = productionNode.getExpressionNode();
		checkExpressionNode(expressionNode, 866, 193, productionNode, 1);

		// t0
		TermNode t0 = expressionNode.getTermNodes().get(0);
		checkTermNode(t0, 866, 193, expressionNode, 1);
			
			//t0_f0
			FactorNode t0_f0 = t0.getFactorNodes().get(0);
			checkFactorNode(t0_f0, 866, 193, t0, FactorNode.Kind.PAR);
			
				//t0_f0_expr
				ExpressionNode t0_f0_expr = t0_f0.getExpressionNode();
				checkExpressionNode(t0_f0_expr, 869, 188, t0_f0, 3);
								
				//t0_f0_expr_t0
				TermNode t0_f0_expr_t0 = t0_f0_expr.getTermNodes().get(0);
				checkTermNode(t0_f0_expr_t0, 869, 68, t0_f0_expr, 7);
									
					//t0_f0_expr_t0_f0
					FactorNode t0_f0_expr_t0_f0 = t0_f0_expr_t0.getFactorNodes().get(0);
					checkFactorNode(t0_f0_expr_t0_f0, 869, 5, t0_f0_expr_t0,FactorNode.Kind.SYMBOL);
					
						//t0_f0_expr_t0_f0_sym
						SymbolNode t0_f0_expr_t0_f0_sym = t0_f0_expr_t0_f0.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t0_f0_sym, 869, 5, t0_f0_expr_t0_f0, SymbolNode.Kind.IDENT);
						
						//t0_f0_expr_t0_f0_sym_ident
						IdentNode t0_f0_expr_t0_f0_sym_ident = t0_f0_expr_t0_f0_sym.getIdentNode();
						checkIdentNode(t0_f0_expr_t0_f0_sym_ident, 869, 5, t0_f0_expr_t0_f0_sym, IdentNode.Kind.TOKEN, "ident");
						
					
					//t0_f0_expr_t0_f1
					FactorNode t0_f0_expr_t0_f1 = t0_f0_expr_t0.getFactorNodes().get(1);
					checkFactorNode(t0_f0_expr_t0_f1, 875, 18, t0_f0_expr_t0, FactorNode.Kind.OPTIONAL);
					
						//t0_f0_expr_t0_f1_expr
						ExpressionNode t0_f0_expr_t0_f1_expr = t0_f0_expr_t0_f1.getExpressionNode();
						checkExpressionNode(t0_f0_expr_t0_f1_expr, 877, 14, t0_f0_expr_t0_f1, 1);
										
						//t0_f0_expr_t0_f1_expr_t0
						TermNode t0_f0_expr_t0_f1_expr_t0 = t0_f0_expr_t0_f1_expr.getTermNodes().get(0);
						checkTermNode(t0_f0_expr_t0_f1_expr_t0, 877, 14, t0_f0_expr_t0_f1_expr, 3);
											
							//t0_f0_expr_t0_f1_expr_t0_f0
							FactorNode t0_f0_expr_t0_f1_expr_t0_f0 = t0_f0_expr_t0_f1_expr_t0.getFactorNodes().get(0);
							checkFactorNode(t0_f0_expr_t0_f1_expr_t0_f0, 877, 3, t0_f0_expr_t0_f1_expr_t0, FactorNode.Kind.SYMBOL);
							
								//t0_f0_expr_t0_f1_expr_t0_f0_sym
								SymbolNode t0_f0_expr_t0_f1_expr_t0_f0_sym = t0_f0_expr_t0_f1_expr_t0_f0.getSymbolNode();
								checkSymbolNode(t0_f0_expr_t0_f1_expr_t0_f0_sym, 877, 3, t0_f0_expr_t0_f1_expr_t0_f0, SymbolNode.Kind.STRING, "[");
								
							//t0_f0_expr_t0_f1_expr_t0_f1
							FactorNode t0_f0_expr_t0_f1_expr_t0_f1 = t0_f0_expr_t0_f1_expr_t0.getFactorNodes().get(1);
							checkFactorNode(t0_f0_expr_t0_f1_expr_t0_f0, 877, 3, t0_f0_expr_t0_f1_expr_t0, FactorNode.Kind.SYMBOL);
					
								//t0_f0_expr_t0_f1_expr_t0_f1_sym
								SymbolNode t0_f0_expr_t0_f1_expr_t0_f1_sym = t0_f0_expr_t0_f1_expr_t0_f1.getSymbolNode();
								checkSymbolNode(t0_f0_expr_t0_f1_expr_t0_f1_sym, 881, 6, t0_f0_expr_t0_f1_expr_t0_f1, SymbolNode.Kind.IDENT);

								//t0_f0_expr_t0_f1_expr_t0_f1_sym_ident
								IdentNode t0_f0_expr_t0_f1_expr_t0_f1_sym_ident = t0_f0_expr_t0_f1_expr_t0_f1_sym.getIdentNode();
								checkIdentNode(t0_f0_expr_t0_f1_expr_t0_f1_sym_ident, 881, 6, t0_f0_expr_t0_f1_expr_t0_f1_sym, IdentNode.Kind.TOKEN, "number");
					
							//t0_f0_expr_t0_f1_expr_t0_f2
							FactorNode t0_f0_expr_t0_f1_expr_t0_f2 = t0_f0_expr_t0_f1_expr_t0.getFactorNodes().get(02);
							checkFactorNode(t0_f0_expr_t0_f1_expr_t0_f2, 888, 3, t0_f0_expr_t0_f1_expr_t0, FactorNode.Kind.SYMBOL);
													
								//t0_f0_expr_t0_f1_expr_t0_f2_sym
								SymbolNode t0_f0_expr_t0_f1_expr_t0_f2_sym = t0_f0_expr_t0_f1_expr_t0_f2.getSymbolNode();
								checkSymbolNode(t0_f0_expr_t0_f1_expr_t0_f2_sym, 888, 3, t0_f0_expr_t0_f1_expr_t0_f2, SymbolNode.Kind.CHAR, ']');
					
					//t0_f0_expr_t0_f2
					FactorNode t0_f0_expr_t0_f2 = t0_f0_expr_t0.getFactorNodes().get(2);
					checkFactorNode(t0_f0_expr_t0_f2, 894, 3, t0_f0_expr_t0, FactorNode.Kind.SYMBOL);
					
						//t0_f0_expr_t0_f2_sym
						SymbolNode t0_f0_expr_t0_f2_sym = t0_f0_expr_t0_f2.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t0_f2_sym, 894, 3, t0_f0_expr_t0_f2, SymbolNode.Kind.STRING, "=");
					
					//t0_f0_expr_t0_f3
					FactorNode t0_f0_expr_t0_f3 = t0_f0_expr_t0.getFactorNodes().get(3);
					checkFactorNode(t0_f0_expr_t0_f3, 898, 6, t0_f0_expr_t0, FactorNode.Kind.SYMBOL);
										
						//t0_f0_expr_t0_f3_sym		
						SymbolNode t0_f0_expr_t0_f3_sym = t0_f0_expr_t0_f3.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t0_f3_sym, 898, 6, t0_f0_expr_t0_f3, SymbolNode.Kind.IDENT);
						
						//t0_f0_expr_t0_f3_sym_ident
						IdentNode t0_f0_expr_t0_f3_sym_ident = t0_f0_expr_t0_f3_sym.getIdentNode();
						checkIdentNode(t0_f0_expr_t0_f3_sym_ident, 898, 6, t0_f0_expr_t0_f3_sym, IdentNode.Kind.TOKEN, "number");

					//t0_f0_expr_t0_f4
					FactorNode t0_f0_expr_t0_f4 = t0_f0_expr_t0.getFactorNodes().get(4);
					checkFactorNode(t0_f0_expr_t0_f4, 905, 4, t0_f0_expr_t0, FactorNode.Kind.SYNC);
					
					//t0_f0_expr_t0_f5
					FactorNode t0_f0_expr_t0_f5 = t0_f0_expr_t0.getFactorNodes().get(5);
					checkFactorNode(t0_f0_expr_t0_f5, 910, 3, t0_f0_expr_t0, FactorNode.Kind.SYMBOL);
					
						//t0_f0_expr_t0_f5_sym
						SymbolNode t0_f0_expr_t0_f5_sym = t0_f0_expr_t0_f5.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t0_f5_sym, 910, 3, t0_f0_expr_t0_f5, SymbolNode.Kind.STRING, ";");
					
					//t0_f0_expr_t0_f6
					FactorNode t0_f0_expr_t0_f6 = t0_f0_expr_t0.getFactorNodes().get(6);
					checkFactorNode(t0_f0_expr_t0_f6, 914, 23, t0_f0_expr_t0, FactorNode.Kind.SEM_TEXT);
					
						//t0_f0_expr_t0_f6_semText
						SemTextNode t0_f0_expr_t0_f6_semText = t0_f0_expr_t0_f6.getSemTextNode();
						checkNode(t0_f0_expr_t0_f6_semText, 914, 23, t0_f0_expr_t0_f6);
					
				//t0_f0_expr_t1
				TermNode t0_f0_expr_t1 = t0_f0_expr.getTermNodes().get(1);
				checkTermNode(t0_f0_expr_t1, 942, 73, t0_f0_expr, 6);
					
					//t0_f0_expr_t1_f0
					FactorNode t0_f0_expr_t1_f0 = t0_f0_expr_t1.getFactorNodes().get(0);
					checkFactorNode(t0_f0_expr_t1_f0, 942, 4, t0_f0_expr_t1, FactorNode.Kind.SYMBOL);
				
						//t0_f0_expr_t1_f0_sym
						SymbolNode t0_f0_expr_t1_f0_sym = t0_f0_expr_t1_f0.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t1_f0_sym, 942, 4, t0_f0_expr_t1_f0, SymbolNode.Kind.STRING, "if");
					
					//t0_f0_expr_t1_f1
					FactorNode t0_f0_expr_t1_f1 = t0_f0_expr_t1.getFactorNodes().get(1);
					checkFactorNode(t0_f0_expr_t1_f1, 947, 3, t0_f0_expr_t1, FactorNode.Kind.SYMBOL);
					
						//t0_f0_expr_t1_f1_sym
						SymbolNode t0_f0_expr_t1_f1_sym = t0_f0_expr_t1_f1.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t1_f1_sym, 947, 3, t0_f0_expr_t1_f1, SymbolNode.Kind.STRING, "(");
					
					//t0_f0_expr_t1_f2
					FactorNode t0_f0_expr_t1_f2 = t0_f0_expr_t1.getFactorNodes().get(2);
					checkFactorNode(t0_f0_expr_t1_f2, 951, 19, t0_f0_expr_t1, FactorNode.Kind.SYMBOL, true);
					
						//t0_f0_expr_t1_f2_sym
						SymbolNode t0_f0_expr_t1_f2_sym = t0_f0_expr_t1_f2.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t1_f2_sym, 951, 9, t0_f0_expr_t1_f2, SymbolNode.Kind.IDENT);
						
						//t0_f0_expr_t1_f2_sym_ident
						IdentNode t0_f0_expr_t1_f2_sym_ident = t0_f0_expr_t1_f2_sym.getIdentNode();
						checkIdentNode(t0_f0_expr_t1_f2_sym_ident, 951, 9, t0_f0_expr_t1_f2_sym, IdentNode.Kind.PRODUCTION, "Condition");
						
						//t0_f0_expr_t1_f2_attribs
						AttribsNode t0_f0_expr_t1_f2_attribs = t0_f0_expr_t1_f2.getAttribsNode();
						checkAttribsNode(t0_f0_expr_t1_f2_attribs, 960, 10, t0_f0_expr_t1_f2, false, true);
										
							//t0_f0_expr_t1_f2_attribs_in
							InAttribsNode t0_f0_expr_t1_f2_attribs_in = t0_f0_expr_t1_f2_attribs.getInAttribsNode();
								checkNode(t0_f0_expr_t1_f2_attribs_in, 961, 8, t0_f0_expr_t1_f2_attribs);
					
					//t0_f0_expr_t1_f3
					FactorNode t0_f0_expr_t1_f3 = t0_f0_expr_t1.getFactorNodes().get(3);
					checkFactorNode(t0_f0_expr_t1_f3, 971, 3, t0_f0_expr_t1, FactorNode.Kind.SYMBOL);
					
						//t0_f0_expr_t1_f3_sym
						SymbolNode t0_f0_expr_t1_f3_sym = t0_f0_expr_t1_f3.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t1_f3_sym, 971, 3, t0_f0_expr_t1_f3, SymbolNode.Kind.STRING, ")");
						
					//t0_f0_expr_t1_f4
					FactorNode t0_f0_expr_t1_f4 = t0_f0_expr_t1.getFactorNodes().get(4);
					checkFactorNode(t0_f0_expr_t1_f4, 975, 36, t0_f0_expr_t1, FactorNode.Kind.SYMBOL, true);
					
						//t0_f0_expr_t1_f4_sym
						SymbolNode t0_f0_expr_t1_f4_sym = t0_f0_expr_t1_f4.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t1_f4_sym, 975, 9, t0_f0_expr_t1_f4, SymbolNode.Kind.IDENT);
						
						//t0_f0_expr_t1_f4_sym_ident
						IdentNode t0_f0_expr_t1_f4_sym_ident = t0_f0_expr_t1_f4_sym.getIdentNode();
						checkIdentNode(t0_f0_expr_t1_f4_sym_ident, 975, 9, t0_f0_expr_t1_f4_sym, IdentNode.Kind.PRODUCTION, "Statement");
						
						//t0_f0_expr_t1_f4_attribs
						AttribsNode t0_f0_expr_t1_f4_attribs = t0_f0_expr_t1_f4.getAttribsNode();
						checkAttribsNode(t0_f0_expr_t1_f4_attribs, 984, 27, t0_f0_expr_t1_f4, true, true);
						
						//t0_f0_expr_t1_f4_attribs_out
						OutAttribsNode t0_f0_expr_t1_f4_attribs_out = t0_f0_expr_t1_f4_attribs.getOutAttribsNode();
						checkNode(t0_f0_expr_t1_f4_attribs_out, 985, 15, t0_f0_expr_t1_f4_attribs);
						
						//t0_f0_expr_t1_f4_attribs_in
						InAttribsNode t0_f0_expr_t1_f4_attribs_in = t0_f0_expr_t1_f4_attribs.getInAttribsNode();
						checkNode(t0_f0_expr_t1_f4_attribs_in, 1002, 8, t0_f0_expr_t1_f4_attribs);
						
					//t0_f0_expr_t1_f5
					FactorNode t0_f0_expr_t1_f5 = t0_f0_expr_t1.getFactorNodes().get(5);
					checkFactorNode(t0_f0_expr_t1_f5, 1012, 3, t0_f0_expr_t1, FactorNode.Kind.SYMBOL);
					
						//t0_f0_expr_t1_f5_sym
						SymbolNode t0_f0_expr_t1_f5_sym = t0_f0_expr_t1_f5.getSymbolNode();
						checkSymbolNode(t0_f0_expr_t1_f5_sym, 1012, 3, t0_f0_expr_t1_f5, SymbolNode.Kind.STRING, ";");
				
				//t0_f0_expr_t2
				TermNode t0_f0_expr_t2 = t0_f0_expr.getTermNodes().get(2);
				checkTermNode(t0_f0_expr_t2, 1020, 37, t0_f0_expr, 2);

					//t0_f0_expr_t2_f0
					FactorNode t0_f0_expr_t2_f0 = t0_f0_expr_t2.getFactorNodes().get(0);
					checkFactorNode(t0_f0_expr_t2_f0, 1020, 3, t0_f0_expr_t2, FactorNode.Kind.ANY);
										
					//t0_f0_expr_t2_f1
					FactorNode t0_f0_expr_t2_f1 = t0_f0_expr_t2.getFactorNodes().get(1);
					checkFactorNode(t0_f0_expr_t2_f1, 1035, 22, t0_f0_expr_t2, FactorNode.Kind.SEM_TEXT);
					
						//t0_f0_expr_t2_f1_semText
						SemTextNode t0_f0_expr_t2_f1_semText = t0_f0_expr_t2_f1.getSemTextNode();
						checkNode(t0_f0_expr_t2_f1_semText, 1035, 22, t0_f0_expr_t2_f1);
		
	}

	@Test
	public void testProductionNode2() {
		ProductionNode productionNode = 
			root.getCompilerNode().getParserSpecNode().getProductionNodes().get(2);
		checkNode(productionNode, 1063, 115, root.getCompilerNode().getParserSpecNode());

		//Ident
		IdentNode identNode = productionNode.getIdentNode();
		checkNode(identNode, 1063, 9, productionNode);
		assertTrue(identNode.getIdent().equals("Condition"));

		//AttrDecl
		AttrDeclNode attrDeclNode = productionNode.getAttrDeclNode();
		checkNode(attrDeclNode, 1072, 18, productionNode);
		
		//SemText
		SemTextNode semTextNode = productionNode.getSemTextNode();
		assertNull(semTextNode);
	}
	
	@Test
	public void testProductionNode2_Expression() {
		ProductionNode productionNode = 
			root.getCompilerNode().getParserSpecNode().getProductionNodes().get(2);
		ExpressionNode expressionNode = productionNode.getExpressionNode();
		checkExpressionNode(expressionNode, 1098, 76, productionNode, 3);
		
		//t0
		TermNode t0 = expressionNode.getTermNodes().get(0);
		checkTermNode(t0, 1098, 39, expressionNode, 3, true);
		
			//t0_resolv
			ResolverNode t0_resolv = t0.getResolverNode();
			checkNode(t0_resolv, 1098, 21, t0);
		
			//t0_f0
			FactorNode t0_f0 = t0.getFactorNodes().get(0);
			checkFactorNode(t0_f0, 1120, 5, t0, FactorNode.Kind.SYMBOL);
			
				//t0_f0_sym
				SymbolNode t0_f0_sym = t0_f0.getSymbolNode();
				checkSymbolNode(t0_f0_sym, 1120, 5, t0_f0, SymbolNode.Kind.IDENT);
				
				//t0_f0_sym_ident
				IdentNode t0_f0_sym_ident = t0_f0_sym.getIdentNode();
				checkIdentNode(t0_f0_sym_ident, 1120, 5, t0_f0_sym, IdentNode.Kind.TOKEN, "ident");
				
			//t0_f1
			FactorNode t0_f1 = t0.getFactorNodes().get(1);
			checkFactorNode(t0_f1, 1126, 4, t0, FactorNode.Kind.SYMBOL);
			
				//t0_f1_sym
				SymbolNode t0_f1_sym = t0_f1.getSymbolNode();
				checkSymbolNode(t0_f1_sym, 1126, 4, t0_f1, SymbolNode.Kind.STRING, "==");
			
			//t0_f2
			FactorNode t0_f2 = t0.getFactorNodes().get(2);
			checkFactorNode(t0_f2, 1131, 6, t0, FactorNode.Kind.SYMBOL);

				//t0_f2_sym
				SymbolNode t0_f2_sym = t0_f2.getSymbolNode();
				checkSymbolNode(t0_f2_sym, 1131, 6, t0_f2, SymbolNode.Kind.IDENT);
				
				//t0_f2_sym_ident
				IdentNode t0_f2_sym_ident = t0_f2_sym.getIdentNode();
				checkIdentNode(t0_f2_sym_ident, 1131, 6, t0_f2_sym, IdentNode.Kind.TOKEN, "number");
				
		//t1
		TermNode t1 = expressionNode.getTermNodes().get(1);

			//t1_f0
			FactorNode t1_f0 = t1.getFactorNodes().get(0);
			checkFactorNode(t1_f0, 1142, 5, t1, FactorNode.Kind.SYMBOL);
			
				//t1_f0_sym
				SymbolNode t1_f0_sym = t1_f0.getSymbolNode();
				checkSymbolNode(t1_f0_sym, 1142, 5, t1_f0, SymbolNode.Kind.IDENT);
			
				//t1_f0_sym_ident
				IdentNode t1_f0_sym_ident = t1_f0_sym.getIdentNode();
				checkIdentNode(t1_f0_sym_ident, 1142, 5, t1_f0_sym, IdentNode.Kind.TOKEN, "ident");
			
			//t1_f1
			FactorNode t1_f1 = t1.getFactorNodes().get(1);
			checkFactorNode(t1_f1, 1148, 4, t1, FactorNode.Kind.SYMBOL);
			
				//t1_f1_sym
				SymbolNode t1_f1_sym = t1_f1.getSymbolNode();
				checkSymbolNode(t1_f1_sym, 1148, 4, t1_f1, SymbolNode.Kind.STRING, "==");
				
			//t1_f2
			FactorNode t1_f2 = t1.getFactorNodes().get(2);
			checkFactorNode(t1_f2, 1153, 5, t1, FactorNode.Kind.SYMBOL);
			
				//t1_f2_sym
				SymbolNode t1_f2_sym = t1_f2.getSymbolNode();
				checkSymbolNode(t1_f2_sym, 1153, 5, t1_f2, SymbolNode.Kind.IDENT);
			
				//t1_f2_sym_ident
				IdentNode t1_f2_sym_ident = t1_f2_sym.getIdentNode();
				checkIdentNode(t1_f2_sym_ident, 1153, 5, t1_f2_sym, IdentNode.Kind.TOKEN, "ident");

		//t2
		TermNode t2 = expressionNode.getTermNodes().get(2);
		checkTermNode(t2, 1163, 11, expressionNode, 1);
		
			//t2_f0
			FactorNode t2_f0 = t2.getFactorNodes().get(0);
			checkFactorNode(t2_f0, 1163, 11, t2, FactorNode.Kind.SYMBOL, false, true);
			
				//t2_f0_sym
				SymbolNode t2_f0_sym = t2_f0.getSymbolNode();
				checkSymbolNode(t2_f0_sym, 1168, 6, t2_f0, SymbolNode.Kind.IDENT);
			
				//t2_f0_sym_ident
				IdentNode t2_f0_sym_ident = t2_f0_sym.getIdentNode();
				checkIdentNode(t2_f0_sym_ident, 1168, 6, t2_f0_sym, IdentNode.Kind.TOKEN, "number");		
	}
	
	/**
	 * validates an AttribsNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param hasOutAttribs
	 * @param hasInAttribs
	 */
	private void checkAttribsNode(AttribsNode  node, int offset, int length,
			AbstractAtgAstNode parent, boolean hasOutAttribs, boolean hasInAttribs) {
		checkNode(node, offset, length, parent);
		if (hasOutAttribs) {
			assertNotNull(node.getOutAttribsNode());
		} else {
			assertNull(node.getOutAttribsNode());
		}
		if (hasInAttribs) {
			assertNotNull(node.getInAttribsNode());
		} else {
			assertNull(node.getInAttribsNode());
		}		
	}

	/**
	 * validates an ExpressionNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param nrTerms
	 */
	private void checkExpressionNode(ExpressionNode node, int offset, int length,
			AbstractAtgAstNode parent, int nrTerms) {
		checkNode(node, offset, length, parent);
		checkList(node.getTermNodes(), nrTerms);
	}

	/**
	 * validates an IdentNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param kind
	 * @param ident
	 */
	private void checkIdentNode(IdentNode node, int offset, int length,
			AbstractAtgAstNode parent, IdentNode.Kind kind, String ident) {
		checkNode(node, offset, length, parent);
		assertEquals(kind, node.getKind());
		assertEquals(ident, node.getIdent());
	}
	
	/**
	 * validates a SymbolNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param kind
	 */
	private void checkSymbolNode(SymbolNode node, int offset, int length,
			AbstractAtgAstNode parent, SymbolNode.Kind kind) {
		checkSymbolNode(node, offset, length, parent, kind, (char) 0, null);
	}

	/**
	 * validates a SymbolNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param kind
	 * @param string
	 */
	private void checkSymbolNode(SymbolNode node, int offset, int length,
			AbstractAtgAstNode parent, SymbolNode.Kind kind, String string) {
		checkSymbolNode(node, offset, length, parent, kind, (char) 0, string);	
	}

	/**
	 * validates a SymbolNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param kind
	 * @param ch
	 */
	private void checkSymbolNode(SymbolNode node, int offset, int length,
			AbstractAtgAstNode parent, SymbolNode.Kind kind, char ch) {
		checkSymbolNode(node, offset, length, parent, kind, ch, null);
	}

	/**
	 * validates a SymbolNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param kind
	 * @param ch
	 * @param string
	 */
	private void checkSymbolNode(SymbolNode node, int offset, int length,
			AbstractAtgAstNode parent, SymbolNode.Kind kind, char ch, String string) {
		checkNode(node, offset, length, parent);
		switch (kind) {
		case CHAR:
			assertFalse(node.getCharacter() == 0);
			assertNull(node.getString());
			assertNull(node.getIdentNode());
			break;
		case IDENT:
			assertTrue(node.getCharacter() == 0);
			assertNull(node.getString());
			assertNotNull(node.getIdentNode());
			break;
		case STRING:
			assertTrue(node.getCharacter() == 0);
			assertNotNull(node.getString());
			assertNull(node.getIdentNode());
			break;
		}	
	}
	
	/**
	 * validates a TermNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param nrFactors
	 */
	private void checkTermNode(TermNode node, int offset, int length,
			AbstractAtgAstNode parent, int nrFactors) {
		checkTermNode(node, offset, length, parent, nrFactors, false);
	}
	
	/**
	 * validates a TermNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param nrFactors
	 * @param hasResolver
	 */
	private void checkTermNode(TermNode node, int offset, int length,
			AbstractAtgAstNode parent, int nrFactors, boolean hasResolver) {
		checkNode(node, offset, length, parent);
		checkList(node.getFactorNodes(), nrFactors);
		if (hasResolver) {
			assertNotNull(node.getResolverNode());
		} else {
			assertNull(node.getResolverNode());
		}
	}

	/**
	 * validates a FactorNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param kind
	 */
	private void checkFactorNode(FactorNode node, int offset, int length,
			AbstractAtgAstNode parent, FactorNode.Kind kind) {
		checkFactorNode(node, offset, length, parent, kind, false, false);
	}
	
	/**
	 * validates a FactorNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param kind
	 * @param hasAttribs
	 */
	private void checkFactorNode(FactorNode node, int offset, int length,
			AbstractAtgAstNode parent, FactorNode.Kind kind, boolean hasAttribs) {
		checkFactorNode(node, offset, length, parent, kind, hasAttribs, false);
	}

	/**
	 * validates a FactorNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 * @param kind
	 * @param hasAttribs
	 * @param isWeak
	 */
	private void checkFactorNode(FactorNode node, int offset, int length,
			AbstractAtgAstNode parent, FactorNode.Kind kind, boolean hasAttribs, boolean isWeak) {
		checkNode(node, offset, length, parent);
		switch (kind) {
		case ANY:
			assertEquals(FactorNode.Kind.ANY, node.getKind());
			assertNull(node.getAttribsNode());
			assertNull(node.getExpressionNode());
			assertNull(node.getSemTextNode());
			assertNull(node.getSymbolNode());
			assertEquals(false, node.isWeakSymbol());
			break;
		case ARBITRARY_OFTEN:			
			assertEquals(FactorNode.Kind.ARBITRARY_OFTEN, node.getKind());
			assertNull(node.getAttribsNode());
			assertNotNull(node.getExpressionNode());
			assertNull(node.getSemTextNode());
			assertNull(node.getSymbolNode());
			assertEquals(false, node.isWeakSymbol());
			break;
		case OPTIONAL:			
			assertEquals(FactorNode.Kind.OPTIONAL, node.getKind());
			assertNull(node.getAttribsNode());
			assertNotNull(node.getExpressionNode());
			assertNull(node.getSemTextNode());
			assertNull(node.getSymbolNode());
			assertEquals(false, node.isWeakSymbol());
			break;
		case PAR:			
			assertEquals(FactorNode.Kind.PAR, node.getKind());
			assertNull(node.getAttribsNode());
			assertNotNull(node.getExpressionNode());
			assertNull(node.getSemTextNode());
			assertNull(node.getSymbolNode());
			assertEquals(false, node.isWeakSymbol());
			break;
		case SEM_TEXT:			
			assertEquals(FactorNode.Kind.SEM_TEXT, node.getKind());
			assertNull(node.getAttribsNode());
			assertNull(node.getExpressionNode());
			assertNotNull(node.getSemTextNode());
			assertNull(node.getSymbolNode());
			assertEquals(false, node.isWeakSymbol());
			break;
		case SYMBOL:
			assertEquals(FactorNode.Kind.SYMBOL, node.getKind());
			if (hasAttribs) {
				assertNotNull(node.getAttribsNode());
			} else {
				assertNull(node.getAttribsNode());
			}
			assertNull(node.getExpressionNode());
			assertNull(node.getSemTextNode());
			assertNotNull(node.getSymbolNode());
			assertEquals(isWeak, node.isWeakSymbol()); //Weak only in allowed in Symbols
			break;
		case SYNC:			
			assertEquals(FactorNode.Kind.SYNC, node.getKind());
			assertNull(node.getAttribsNode());
			assertNull(node.getExpressionNode());
			assertNull(node.getSemTextNode());
			assertNull(node.getSymbolNode());
			assertEquals(false, node.isWeakSymbol());
			break;
		}		
	}
	
	/**
	 * validates an AtgAstNode
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @param parent
	 */
	private void checkNode(
			AbstractAtgAstNode node, 
			int offset, 
			int length,
			AbstractAtgAstNode parent) {
		assertNotNull(node);
		assertEquals(offset, node.getRegion().getOffset());
		assertEquals(length, node.getRegion().getLength());
		assertEquals(parent, node.getParent());
	}
		
	/**
	 * validates an List of AtgAstNodes
	 *
	 * @param <T> Type of Node, T extends AbstractAtgAstNode
	 * @param nodes
	 * @param size
	 */
	private <T extends AbstractAtgAstNode> void checkList(
			List<T> nodes,
			int size) {
		assertNotNull(nodes);
		assertEquals(size, nodes.size());
	}
}
