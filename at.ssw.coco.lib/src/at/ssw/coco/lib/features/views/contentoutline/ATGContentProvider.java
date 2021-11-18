package at.ssw.coco.lib.features.views.contentoutline;

import at.ssw.coco.lib.model.atgmodel.ATGModel;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;

/**
 * Provides Methods for the <code>ATGContentOutlinePage</code>.
 *
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 *
 */

public class ATGContentProvider {
	

	public Object[] getChildren(Object parentElement) {
		if (!(parentElement instanceof ATGSegment)) {
			return new Object[0];
		}
		ATGSegment seg = (ATGSegment)parentElement;
		return seg.getChildren();
	}
	
	
	public Object getParent(Object element) {
		if (!(element instanceof ATGSegment)) {
			return null;
		}
		ATGSegment seg = (ATGSegment)element;
		return seg.getParent();
	}
	
	
	public boolean hasChildren(Object element) {
		if (!(element instanceof ATGSegment)) {
			return false;
		}
		ATGSegment seg = (ATGSegment)element;
		return seg.hasChildren();
	}
	
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ATGModel) {
			ATGModel atg = (ATGModel)inputElement;
			return atg.getElements();
		} else {
			return getChildren(inputElement);
		}
	}

}
