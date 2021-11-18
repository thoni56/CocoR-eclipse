/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
 * Copyright (C) 2009 Andreas Woess
 * Copyright (C) 2011 Andreas Greilinger
 * Copyright (C) 2011 Konstantin Bina
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

package at.ssw.coco.ide.model.atgmodel;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;

import at.ssw.coco.lib.model.atgmodel.ATGModel;
import at.ssw.coco.lib.model.atgmodel.ATGModelImpl;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;
import at.ssw.coco.lib.model.atgmodel.ATGSegment.Type;
import at.ssw.coco.lib.model.atgmodel.impl.DocumentScanner;
/**
 * This class is an adaptor and is used to inlude and adapt the
 * library functions and methods into CocoEclipse.
 * (at.ssw.coco.lib.model.atgmodel.*)
 * 
 * Represents the content of an ATG file.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 *
 */
public class ATGModelAdaptor implements ATGModel {

	
	/** The coco ATGModel */
	private ATGModelImpl model;
	/**
	 * The Constructor.
	 *
	 * @param document The document.
	 */
	public ATGModelAdaptor(IDocument document) {
		Assert.isNotNull(document);
		model = new ATGModelImpl(new DocumentScanner(document.get()));
	}

	@Override
	public ATGSegment find(String name, Type[] acceptedTypes) {
		return model.find(name, acceptedTypes);
	}

	@Override
	public ATGSegment find(String name) {
		return model.find(name);
	}

	@Override
	public ATGSegment getDeclarations() {
		return model.getDeclarations();
	}

	@Override
	public ATGSegment[] getElements() {
		return model.getElements();
	}

	@Override
	public ATGSegment getImports() {
		return model.getImports();
	}

	@Override
	public ATGSegment[] getProductions() {
		return model.getProductions();
		
	}

	@Override
	public String[] listContent(Type[] acceptedTypes) {
		return model.listContent(acceptedTypes);
	}

	@Override
	public String[] listContent() {
		return model.listContent();
	}

	@Override
	public ATGSegment[] listSegments(Type[] acceptedTypes) {
		return model.listSegments(acceptedTypes);
	}

	@Override
	public ATGSegment[] listSegments() {
		return model.listSegments();
	}

	
}
