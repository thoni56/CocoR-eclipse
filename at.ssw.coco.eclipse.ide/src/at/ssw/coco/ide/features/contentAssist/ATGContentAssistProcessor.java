/*******************************************************************************
 * Copyright (C) 2006 Christian Wressnegger
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

package at.ssw.coco.ide.features.contentAssist;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import at.ssw.coco.ide.features.contentAssist.codeCompletion.semanticCompletion.MethProposal;
import at.ssw.coco.ide.features.contentAssist.codeCompletion.semanticCompletion.VarProposal;
import at.ssw.coco.ide.features.views.contentoutline.ATGLabelProvider;
import at.ssw.coco.ide.model.detectors.WordFinderAdaptor;
import at.ssw.coco.ide.model.detectors.word.CocoIdentDetectorAdaptor;
import at.ssw.coco.lib.model.atgmodel.ATGModelProvider;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;
import at.ssw.coco.lib.model.scanners.ATGKeywords;

/**
 * This content assist processor proposes completions but does not compute
 * context information for a particular content type. The completions lexicon
 * consists of the ATG keywords and the content specific names of productions
 * and defined types.
 * 
 * @author Christian Wressnegger <k0356707@students.jku.at>
 * @author Andreas Woess <andwoe@users.sf.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 * 
 */
public class ATGContentAssistProcessor implements IContentAssistProcessor {

	public class PorposalComparator implements Comparator<ICompletionProposal> {
		public int compare(ICompletionProposal o1, ICompletionProposal o2) {
			return o1.getDisplayString().toLowerCase().compareTo(
					o2.getDisplayString().toLowerCase());
		}
	}

	protected static class StyledCompletionProposal implements
			ICompletionProposal, ICompletionProposalExtension6 {
		private static String getReplacementString(Object object) {
			if (object instanceof ATGSegment) {
				ATGSegment segment = (ATGSegment) object;
				return segment.getName();
			}
			if (object instanceof VarProposal) {
				VarProposal vp = (VarProposal) object;
				return vp.getName();
			}

			if (object instanceof MethProposal) {
				MethProposal mp = (MethProposal) object;
				String s = mp.toString();
				return s.substring(0, s.indexOf(':') - 1);
			}
			String s = object.toString();
			String defaultString = "no Default Proposals";

			if (s.contains(defaultString)) {
				s = s.substring(0, s.indexOf(defaultString));
			}

			return s;
		}

		private final CompletionProposal completionProposal;
		private final ATGLabelProvider labelProvider;
		private final Object replacementObject;

		private final String replacementString;

		public StyledCompletionProposal(Object replacementObject,
				int replacementOffset, int replacementLength,
				ATGLabelProvider labelProvider) {
			this.replacementObject = replacementObject;
			this.replacementString = getReplacementString(replacementObject);
			this.labelProvider = labelProvider;
			this.completionProposal = new CompletionProposal(replacementString,
					replacementOffset, replacementLength, replacementString
							.length()/* cursorPosition */);
		}

		public void apply(IDocument document) {
			completionProposal.apply(document);
		}

		public String getAdditionalProposalInfo() {
			return completionProposal.getAdditionalProposalInfo();
		}

		public IContextInformation getContextInformation() {
			return completionProposal.getContextInformation();
		}

		public String getDisplayString() {
			String defaultString = "no Default Proposals";
			if (replacementObject.toString().contains(defaultString)) {
				return defaultString;
			}
			return labelProvider.getText(replacementObject);
		}

		public Image getImage() {
			return completionProposal.getImage();
		}

		public Point getSelection(IDocument document) {
			return completionProposal.getSelection(document);
		}

		public StyledString getStyledDisplayString() {
			String defaultString = "no Default Proposals";
			if (replacementObject.toString().contains(defaultString)) {
				return new StyledString(defaultString);
			}
			return labelProvider.getStyledText(replacementObject);
		}
	}

	protected ATGModelProvider modelProvider;

	/** The word detector */
	protected static final CocoIdentDetectorAdaptor fIdentDetector = new CocoIdentDetectorAdaptor();

	/**
	 * The Constructor.
	 * 
	 * @param editor
	 *            The ATG editor.
	 */
	public ATGContentAssistProcessor(ATGModelProvider modelProvider) {
		Assert.isNotNull(modelProvider);
		this.modelProvider = modelProvider;
	}

	/**
	 * @param viewer
	 *            The used text viewer
	 * @param offset
	 *            The cursor positions in the ATG File
	 * 
	 *            This method defines the keywords and the context based
	 *            completionPropoals that should be used for Code Completion and
	 *            is equivalent to"computeCompletionProposals(viewer, offset, ATGLangScanner.KEYWORDS, modelProvider.getATGModel().listSegments())"
	 * 
	 *            Has to be overridden in the subclasses of
	 *            contentAssistProcessor that define the completion strategies.
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		Object[][] segments = new Object[][] { modelProvider.getATGModel()
				.listSegments() };
		return computeCompletionProposals(viewer, offset, ATGKeywords.values(),
				segments);
	}

	/**
	 * 
	 * @param viewer
	 *            The used text viewer
	 * @param offset
	 *            The cursor positions in the ATG File
	 * @param keywords
	 *            The Keywords that should be proposed for Code Completion
	 * @param segments
	 *            The context based Completion Proposals for Code Completion
	 * @return
	 * 
	 * 
	 */
	protected ICompletionProposal[] computeCompletionProposals(
			ITextViewer viewer, int offset, Object[] keywords,
			Object[][] segments) {

		ITextSelection selection = (ITextSelection) viewer
				.getSelectionProvider().getSelection();
		WordFinderAdaptor wordFinder = new WordFinderAdaptor(viewer
				.getDocument(), fIdentDetector);
		String prefix = wordFinder.getPrefix(offset);
		IRegion region = new Region(offset - prefix.length(), prefix.length()
				+ selection.getLength());

		prefix = prefix.toLowerCase();
		LinkedList<ICompletionProposal> completionProposals = new LinkedList<ICompletionProposal>();
		Set<ICompletionProposal> completionProposals2 = new TreeSet<ICompletionProposal>(
				new PorposalComparator());

		ATGLabelProvider labelProvider = new ATGLabelProvider();
		if (keywords != null) {
			for (Object entry : keywords) {
				String text = entry.toString();
				if (text.toLowerCase().startsWith(prefix)) {
					completionProposals.add(new StyledCompletionProposal(entry,
							region.getOffset(), region.getLength(),
							labelProvider));
				}
			}
		}
		if (segments != null) {
			for (Object[] arr : segments) {
				for (Object entry : arr) {
					String text = entry.toString();
					if (text.toLowerCase().startsWith(prefix)) {
						completionProposals2.add(new StyledCompletionProposal(
								entry, region.getOffset(), region.getLength(),
								labelProvider));
					}
				}
			}
		}
		completionProposals.addAll(completionProposals2);

		if (completionProposals.isEmpty()) {
			String entry = prefix + "no Default Proposals";
			completionProposals.add(new StyledCompletionProposal(entry, region
					.getOffset(), region.getLength(), labelProvider));
		}

		return completionProposals.toArray(new ICompletionProposal[0]);
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return null;
	}

	/** Defines the Chatacters that should automatically invoke codeComletion */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	public String getErrorMessage() {
		return null;
	}
}
