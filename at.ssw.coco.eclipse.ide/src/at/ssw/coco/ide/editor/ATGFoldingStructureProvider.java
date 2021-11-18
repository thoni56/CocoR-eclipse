/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;

import at.ssw.coco.lib.model.atgmodel.ATGSegment;
import at.ssw.coco.lib.model.positions.CocoPosition;

/**
 * Provides the folding structure and already modifies the given ProjectionAnnotationModel.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 *
 */
public class ATGFoldingStructureProvider {
	private static final EnumSet<ATGSegment.Type> EXCLUDED_TYPES = EnumSet.of(ATGSegment.Type.SECTION_SCANNER);

	/** The document */
	private IDocument fDocument;

	/** The text editor */
	private ITextEditor fEditor;

	/** The project annotation model */
	private ProjectionAnnotationModel fAnnotationModel;

	/** A set of <code>Position</code>s containing the folding regions */
	private Set<Position> fFoldingRegions;

	/**
	 * The Constructor
	 *
	 * @param annotationModel The project annotation model.
	 */
	public ATGFoldingStructureProvider(ITextEditor editor) {
		fFoldingRegions = new HashSet<Position>();
		fEditor = editor;
	}

	/**
	 * Sets the document.
	 *
	 * @param document The document.
	 */
	public void setDocument(IDocument document) {
		fDocument = document;
	}

	/**
	 * Performs the update task.
	 *
	 * @param model A segment of the ATG model.
	 */
	public void updateFoldingRegions(ATGSegment[] model) {
		try {
			// Delete all recognized folding regions, instead of updating the old ones.
			fFoldingRegions.clear();
			if (fDocument.getLength() > 0) {
				addFoldingRegions(model);
			}
			updateFoldingRegions();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recursively add the folding regions to the set of <code>Position</code>s
	 *
	 * @param elements The ATG segments.
	 * @throws BadLocationException
	 */
	private void addFoldingRegions(ATGSegment[] elements) throws BadLocationException {
		if (elements == null) {
			return;
		}
		for (ATGSegment seg : elements) {
			if (seg.getLevel() >= 3) {
				continue;
			}

			if (!EXCLUDED_TYPES.contains(seg.getType())) {
				CocoPosition p = seg.getPosition();
				Position pos = new Position(p.getOffset(), p.getLength());
				//Position pos = seg.getPosition();
				int startLine = fDocument.getLineOfOffset(pos.getOffset());
				int endLine = fDocument.getLineOfOffset(pos.getOffset() + pos.getLength());
				if (startLine < endLine) {
					int start = fDocument.getLineOffset(startLine);
					int end = fDocument.getLineOffset(endLine) + fDocument.getLineLength(endLine);
					Position position = new Position(start, end - start);
					fFoldingRegions.add(position);
				}
			}

			addFoldingRegions(seg.getChildren());
		}
	}

	/**
	 * Modifies the annotation model using the current folding regions and the differences to the previous model.
	 */
	@SuppressWarnings("unchecked")
	private void updateFoldingRegions() {
		fAnnotationModel = (ProjectionAnnotationModel)fEditor.getAdapter(ProjectionAnnotationModel.class);
		if (fAnnotationModel == null) {
			return;
		}

		// make foldingRegionsToAdd a copy of fFoldingRegions
		Set<Position> foldingRegionsToAdd = new HashSet<Position>();
		foldingRegionsToAdd.addAll(fFoldingRegions);

		// find out which regions need to be removed/added
		ArrayList<Annotation> annotationsToRemove = new ArrayList<Annotation>();
		Iterator<Annotation> annotationIterator = fAnnotationModel.getAnnotationIterator();
		while (annotationIterator.hasNext()) {
			Annotation currentAnnotation = annotationIterator.next();
			Position currentPosition = new Position(fAnnotationModel.getPosition(currentAnnotation).offset, fAnnotationModel.getPosition(currentAnnotation).length);
			if (!fFoldingRegions.contains(currentPosition)) {
				// folding region vanished -> remove it's Annotation
				annotationsToRemove.add(currentAnnotation);
			} else {
				// folding region unchanged -> remove it from to-add list
				foldingRegionsToAdd.remove(currentPosition);
			}
		}

		// create a Map of folding regions to add
		Map<Annotation, Position> annotationsToAdd = new HashMap<Annotation, Position>();
		for (Position pos : foldingRegionsToAdd) {
			annotationsToAdd.put(new ProjectionAnnotation(), pos);
		}

		// remove / add annotations
		Annotation[] arr = new Annotation[0];
		fAnnotationModel.replaceAnnotations(annotationsToRemove.toArray(arr), annotationsToAdd);
	}
}
