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
package at.ssw.coco.ide.model.scanners;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import at.ssw.coco.lib.model.scanners.FastATGPartitionScanner;
import at.ssw.coco.lib.model.scanners.types.CocoToken;

/**
 * This class is an adaptor and is used to inlude and adapt the
 * library functions and methods into CocoEclipse.
 * (at.ssw.coco.lib.model.scanners.FastATGPartitionScanner) 
 * 
 * Implements a partition scanner for .atg files.
 *
 * @author Andreas Woess <andwoe@users.sf.net>#
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */
public class FastATGPartitionScannerAdaptor implements IPartitionTokenScanner {

	
	
	private FastATGPartitionScanner partitionScanner;
	
	public FastATGPartitionScannerAdaptor() {
		partitionScanner = new FastATGPartitionScanner();
	}

	public FastATGPartitionScannerAdaptor(boolean nestedMultiLineComments) {
		this();
		partitionScanner.setNestedMultiLineComments(nestedMultiLineComments);
	}

	/*
	 * @see IPartitionTokenScanner#setPartialRange(IDocument, int, int, String, int)
	 */
	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
		try {
			int prev = offset;
			if(prev<=0){
				prev = 1;
			}
			partitionScanner.setPartialRange(document.get(), offset, length, contentType, partitionOffset, 
					document.getLegalLineDelimiters(), document.getContentType(prev-1));
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		CocoToken token = partitionScanner.nextTokenImpl();
		if(token.isEOF()){
			return Token.EOF;
		}
		else if(token.isWhitespace()){
			return Token.WHITESPACE;
		}
		else if(token.isUndefined()){
			return Token.UNDEFINED;
		}
		else {
			//System.out.println(token.getData());
		IToken t = new Token(token.getData());
		return t;
		}
	}

	/*
	 * @see ITokenScanner#setRange(IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {
		partitionScanner.setRange(document.get(), offset, length, document.getLegalLineDelimiters());
	}

	/*
	 * @see ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		return partitionScanner.getTokenLength();
	}

	/*
	 * @see ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		return partitionScanner.getTokenOffset();
	}
}

