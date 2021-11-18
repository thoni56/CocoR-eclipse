/*******************************************************************************
 * Copyright (C) 2009 Institute for System Software, JKU Linz
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
package at.ssw.coco.ide.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import at.ssw.coco.ide.model.scanners.ATGPartitionerAdaptor;
import at.ssw.coco.ide.model.scanners.FastATGPartitionScannerAdaptor;

/**
 * A shareable document provider for text files ({@link org.eclipse.core.resources.IFile})
 * that assigns an ATG partition scanner to each document.
 *
 * @author Andreas Woess <andwoe@users.sf.net>
 */
public class ATGDocumentProvider extends TextFileDocumentProvider {
	@Override
	public void connect(Object element) throws CoreException {
		super.connect(element);
		IDocument document = getDocument(element);
		if (document != null) {
			if (document.getDocumentPartitioner() == null) {
				IDocumentPartitioner partitioner = new ATGPartitionerAdaptor(new FastATGPartitionScannerAdaptor());
				partitioner.connect(document);
				document.setDocumentPartitioner(partitioner);
			}
		} else {
			System.err.println("Unable to get the document!");
		}
	}
}
