package at.ssw.coco.plEditor.features.syntaxHighlighting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import at.ssw.coco.plEditor.model.CocoClassLoader;

/**
 * This class reads tokens from a clng file (using a coco generated Scanner) and
 * generates fitting syntactic Presentation Tokens that are used for Syntac
 * Highlighting.
 */

/**
 * Implements a <code> ITokenScanner </code> to scan the content of the CLNG Editor 
 * and generate Tokens for the DamagerRepairer, that is used to create Syntax Highlighting <br> <br>
 * 
 * Uses a Coco Scanner to scan single tokens and uses a <code> SyntaxManager </code> to create the according TextPresentation.
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 *
 */
public class CLNGScanner implements ITokenScanner {

	
	private Class<?> Scanner;
	
	/** All Constructors of the Coco generated Scanner Class */
	private Constructor<?>[] fConstructors;
	/** All Mentods of the Coco generated Scanner Class */
//	private Method[] fMethods;
	
	private Method ScanMethod;
	private Method PeekMethod;
	private Method ResetPeekMethod;
	private Method GetPeekTokenOffsetMethod;
	private Method GetPeekTokenKindMethod;
	private Method GetPeekTokenValMethod;
	
	
	/** Instance of the Coco generated Scanner */
	private Object fScanner;

	/** The current offset within the document */
	private int fOffset;
	/** The end position of the document */
	private int fEndPos;

	/** The current token */
	private IToken fToken;
	/** The length of the current token */
	private int fLength;

	/** The Syntax Manager */
	private SyntaxManager fManager;
	

	/** The constructor */
	public CLNGScanner(SyntaxManager manager) {
		fManager = manager;
		fManager.init();
		CocoClassLoader loader = fManager.getClassLoader();
		
		Scanner = loader.getScanner();
		
		fConstructors = loader.getScannerConstructors();
		
		try {
			
			ScanMethod = Scanner.getMethod("Scan");
			PeekMethod = Scanner.getMethod("Peek");
			ResetPeekMethod = Scanner.getMethod("ResetPeek");
			GetPeekTokenOffsetMethod = Scanner.getMethod("getPeekTokenOffset");
			GetPeekTokenKindMethod = Scanner.getMethod("getPeekTokenKind");
			GetPeekTokenValMethod = Scanner.getMethod("getPeekTokenVal");
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void setRange(IDocument document, int offset, int length) {

		// (re)set fields
		fOffset = offset;
		fEndPos = fOffset + length;
		fLength = 0;

		// generate new (Coco)Scanner and thus reset the position of that Scanner.
		try {
			InputStream stream = new ByteArrayInputStream(document.get().getBytes("UTF-8"));	
			fScanner = instantiateScanner(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	@Override
	public IToken nextToken() {
		try {
			// set offset the position of the next token
			fOffset += fLength;

			// use the scanner to look at the next token (but don't consume it yet)
			peek();

			// the offset of the look ahead token
			int laOffset = getPeekTokenOffset();

			// if the offset of the look ahead token is higher than the current offset,
			// fScanner has skipped characters (either whitespace or comments)
			if (fOffset < laOffset) {
				// generate a new syntactic presentation token from offset to lookahead offset
				// this token can be used to create syntax highlighting for comments
				fToken = new Token(fManager.getCommentsPresentationStyle());
				fLength = laOffset - fOffset;
			}
			
			//else create new token from the Coco Scanner's current token.
			else {
				fOffset = laOffset;
				fLength = getPeekTokenVal().length();

				// if the end of the document hasn't been reached yet,
				// generate a new syntactic presentation token
				if (fOffset < fEndPos) {
					fToken = new Token(fManager.getPresentationStyle(getPeekTokenKind()));
				}
				// else return end of file
				else {
					fToken = Token.EOF;
				}

				// consume next token
				scan();

			}
			// Reset the look ahead token
			resetPeek();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return fToken;
	}

	
	@Override
	public int getTokenOffset() {
		return fOffset;
	}
	
	@Override
	public int getTokenLength() {
		return fLength;
	}

	
	
	
	/* 
	 * The following methods are used to create the Coco Scanner 
	 * and invoke it's methods using Java Reflection 
	 */
	

	/** Use Java Reflection to instantiate a new Scanner */
	private Object instantiateScanner(InputStream stream) throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Object o = null;
		for (Constructor<?> c : fConstructors) {
			Class<?>[] parameters = c.getParameterTypes();
			if (parameters.length == 1
					&& parameters[0].equals(InputStream.class)) {
				o = c.newInstance(stream);
			}
		}
		return o;
	}

	/** Use Java Reflection to invoke the "Scan" method of fScanner */
	private void scan() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		ScanMethod.invoke(fScanner);
	}

	/** Use Java Reflection to invoke the "Peek" method of fScanner */
	private void peek() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		PeekMethod.invoke(fScanner);
	}

	/** Use Java Reflection to invoke the "ResetPeek" method of fScanner */
	private void resetPeek() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		ResetPeekMethod.invoke(fScanner);
	}

	/** Use Java Reflection to invoke the "getPeekTokenOffset" method of fScanner */
	private int getPeekTokenOffset() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int pos = -1;
		pos = (Integer) GetPeekTokenOffsetMethod.invoke(fScanner);
		return pos;
	}

	/** Use Java Reflection to invoke the "getPeekTokenKind" method of fScanner */
	private int getPeekTokenKind() throws IllegalArgumentException,	IllegalAccessException, InvocationTargetException {
		int kind = -1;
		kind = (Integer) GetPeekTokenKindMethod.invoke(fScanner);
		return kind;
	}

	/** Use Java Reflection to invoke the "getPeekTokenVal" method of fScanner */
	private String getPeekTokenVal() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String val = "";
		val = GetPeekTokenValMethod.invoke(fScanner).toString();
		return val;
	}

}
