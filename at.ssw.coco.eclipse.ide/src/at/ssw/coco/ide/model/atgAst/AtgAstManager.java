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

package at.ssw.coco.ide.model.atgAst;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import at.ssw.coco.ide.editor.ATGEditor;
import at.ssw.coco.lib.model.atgAst.AtgAst;

/**
 * Manages Access and Creation of the Abstract Syntax Tree of the ATG-File
 * 
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class AtgAstManager {
	
	/**
	 * dirty flag - if set document has changed and ATG AST is not
	 * up to date
	 */
	private boolean dirty = false;
	
	/**
	 * ATG Abstract Syntax Tree
	 */
	private AtgAst atgAst;
	
	/**
	 * Editor of File
	 */
	private ATGEditor atgEditor;
	
	/**
	 * Document Listener - Listens for documentChange events 
	 * of the ATGEditor, if document changes the dirty flag is set
	 */
	private IDocumentListener documentListener = new IDocumentListener() {		
		@Override
		public void documentChanged(DocumentEvent event) {
			dirty = true;				
		}		
		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
	};
	
	/**
	 * Constructor
	 * @param atgEditor
	 */
	public AtgAstManager(ATGEditor atgEditor) {
		this.atgEditor = atgEditor;
		this.dirty = true;
	}
	
	/**
	 * Getter for the ATG Abstract Syntax Tree
	 * if document has changed since last call of this
	 * Getter the AST will be recreated before this method 
	 * returns
	 * @return ATG Abstract Syntax Tree
	 */
	public AtgAst getAtgAst() {
		if (dirty) {
			createAst();
		}		
		return atgAst;
	}

	/**
	 * gets Document from ATG Editor, transforms it to an InputStream
	 * (UTF-8 encoded), calls the ATG AST Parser and resets the 
	 * dirty flag after AST creation.
	 */
	private void createAst() {
		try {
			IEditorInput editorInput = atgEditor.getEditorInput();
			IDocumentProvider documentProvider = atgEditor.getDocumentProvider();
			IDocument document = documentProvider.getDocument(editorInput);
			document.addDocumentListener(documentListener);
			String text = document.get(0, document.getLength());
			InputStream in = new ByteArrayInputStream(text.getBytes("UTF-8"));
			atgAst = new AtgAst(in);
			dirty = false;
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
