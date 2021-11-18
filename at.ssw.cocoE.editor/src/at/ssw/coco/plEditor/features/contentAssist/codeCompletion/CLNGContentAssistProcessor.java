package at.ssw.coco.plEditor.features.contentAssist.codeCompletion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.SortedSet;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import Coco.NodeProvider;
import Coco.Tab;
import at.ssw.coco.core.CoreUtilities;
import at.ssw.coco.plEditor.EditorPlugin;
import at.ssw.coco.plEditor.model.CocoClassLoader;
import at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants;

public class CLNGContentAssistProcessor implements IContentAssistProcessor {

	
	/** Constant that should be displayed if no matching proposals have been found  */
	private final String NO_DEFAULT_PROPOSALS = "no Default Proposals";
	
	/** The ClassLoader used to load the Scanner and Parser files  */
	private CocoClassLoader fLoader;
	
	private Class<?> ProposalProvider;
	
	/** All Mentods of the ProposalProvider Class */
//	private Method[] fMethods;
	
	private CodeCompletionStrategy strategy;
	
	private int lastOffset;
	
	private Tab tab;
	
	private String currentWord = new String();
	
	private SortedSet<NodeProvider> tokenProposals;
	private SortedSet<NodeProvider> productionProposals; 
	private SortedSet<String> userProposals; 
	
	
	public CLNGContentAssistProcessor(CocoClassLoader loader){
		fLoader = loader;
		ProposalProvider = loader.getProposalProvider();
//		fMethods = loader.getProposalProviderMethods();
		
		strategy = new CodeCompletionStrategy(loader);
		
		lastOffset = -2;
		
		String preferences = EditorPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.fieldName);
		
//		String srcDir = fLoader.getPackagePath();
		String editorSuffix = fLoader.getEditorSuffix();
		String srcDir = preferences+Path.SEPARATOR+editorSuffix;
		String name = editorSuffix + ".atg";
		String nsName = fLoader.getPackageName();
		name = srcDir+ Path.SEPARATOR + name;
		
		
		tab = CoreUtilities.generateParserTab(name, srcDir, srcDir, nsName);
		
		
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		
		IDocument document = viewer.getDocument();
//		LinkedList<ICompletionProposal> completionProposals = new LinkedList<ICompletionProposal>();
		char currentChar = ' ';
		if(offset>0){
			currentChar = document.get().charAt(offset-1);
		}
		boolean isWhitSpace = Character.isWhitespace(currentChar);
		
		
		
		if(offset == lastOffset+1 && (currentWord.isEmpty() || !isWhitSpace)){
			if(!isWhitSpace){
				currentWord+=currentChar;
			}
		}
		
		else if(offset == lastOffset-1 && (currentWord.length()>0 || isWhitSpace)){
			if(currentWord.length()>0){
				currentWord = currentWord.substring(0, currentWord.length()-1);
			}	
		}
		
	

		else{
			strategy.start(document, offset, tab);
			
			
			tokenProposals = strategy.getTokenProposals();
			productionProposals = strategy.getProductionProposals();
			userProposals = strategy.getUserProposals();
			
			currentWord = strategy.getCurrentWord().toLowerCase();
		}
		
		
		currentWord = currentWord.toLowerCase();
		lastOffset = offset;
		
		return generateProposalArray(offset-currentWord.length(), tokenProposals, productionProposals, userProposals, currentWord);
	}

	
	
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
	
	
	
	private ICompletionProposal[] generateProposalArray(int offset, SortedSet<NodeProvider> tokenProposals, SortedSet<NodeProvider> productionProposals, SortedSet<String> userProposals, String currentWord){
	
		LinkedList<ICompletionProposal> completionProposals = new LinkedList<ICompletionProposal>();
		
		Image tokenImage = null;
		Image productionImage = null;
		Image userImage = null;
		ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(EditorPlugin.PLUGIN_ID, "icons/Token.gif");
		tokenImage = desc.createImage();
		

		desc = AbstractUIPlugin.imageDescriptorFromPlugin(EditorPlugin.PLUGIN_ID, "icons/Production.gif");
		productionImage = desc.createImage();
		
		desc = AbstractUIPlugin.imageDescriptorFromPlugin(EditorPlugin.PLUGIN_ID, "icons/User.gif");
		userImage = desc.createImage();
		
		completionProposals.addAll(createUserProposals(offset, userImage, userProposals, currentWord));
			
		completionProposals.addAll(createProposalsFromSymbols(offset, tokenImage, tokenProposals, currentWord));
		
		completionProposals.addAll(createProposalsFromSymbols(offset, productionImage, productionProposals, currentWord));
		
		if(completionProposals.isEmpty()){
			CompletionProposal testProposal = new CompletionProposal(" ", offset, 0, offset-currentWord.length(), null, NO_DEFAULT_PROPOSALS, null, null);
			completionProposals.add(testProposal);
		}
		
		return completionProposals.toArray(new ICompletionProposal[0]);
		
		
	}
	
	private LinkedList<ICompletionProposal> createProposalsFromSymbols(int offset, Image image, SortedSet<NodeProvider> symbols, String currentWord){
		LinkedList<ICompletionProposal> completionProposals = new LinkedList<ICompletionProposal>();
		
		try {
			for(NodeProvider np : symbols){
				String name = np.getSymbol().getName();
								
				String displayString = name;
				String replaceMentString = name;
				
				if(name.startsWith("\"") && name.endsWith("\"")){
										
					displayString = getKeywordDisplayString(name);
					replaceMentString = getKeywordSymbolReplacementString(name, displayString);
				}
				
				else{
					displayString = getDisplayString(name);
					replaceMentString = getReplacementString(name, displayString);
				}
				
				if(displayString.toLowerCase().startsWith(currentWord)){
					CompletionProposal proposal = new  CompletionProposal(replaceMentString, offset, currentWord.length(), replaceMentString.length(), image, displayString, null, null);
					completionProposals.add(proposal);
				}
			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return completionProposals;
	}
	
	private LinkedList<ICompletionProposal> createUserProposals(int offset, Image image, SortedSet<String> userProposals, String currentWord){
		LinkedList<ICompletionProposal> completionProposals = new LinkedList<ICompletionProposal>();
		
		for(String s : userProposals){
			
			
			if(s.toLowerCase().startsWith(currentWord)){
				CompletionProposal proposal = new  CompletionProposal(s, offset, currentWord.length(), s.length(), image, s, null, null);
				completionProposals.add(proposal);
			}
		}
		
		return completionProposals;
	}
	
	
	
	//reflection
	
	private String getDisplayString(String proposal) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		String methodName = "get_"+proposal+"_displayString";
		
		String displayString = proposal;
				
		try {
			Method m = ProposalProvider.getMethod(methodName);
			displayString = m.invoke(null).toString();
		} catch (SecurityException e) {
			//do nothing
		} catch (NoSuchMethodException e) {
			//do nothing
		}
		
		
		
//		for (Method m : fMethods) {
//			if (m.getName().equals(methodName)) {
//				displayString = (String) m.invoke(null);
//			}
//		}
		
		return displayString;
	}
	
	private String getReplacementString(String proposal, String displayString) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		String methodName = "get_"+proposal+"_replacementString";
		
		String replacementString = displayString;
		
		try {
			Method m = ProposalProvider.getMethod(methodName);
			replacementString = m.invoke(null).toString();
		} catch (SecurityException e) {
			//do nothing
		} catch (NoSuchMethodException e) {
			//do nothing
		}
		
//		for (Method m : fMethods) {
//			if (m.getName().equals(methodName)) {
//				replacementString = (String) m.invoke(null);
//			}
//		}
		
		return replacementString;
	}
	
	private String getKeywordDisplayString(String proposal) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		String methodName = "transform_Keyword_displayString";
		
		String displayString = proposal;
		
		try {
			Method m = ProposalProvider.getMethod(methodName, String.class);
			displayString = m.invoke(null, proposal).toString();
		} catch (SecurityException e) {
			//do nothing
		} catch (NoSuchMethodException e) {
			//do nothing
		}
		
		
//		for (Method m : fMethods) {
//			if (m.getName().equals(methodName)) {
//				
//				Class[] parameters = m.getParameterTypes();
//				if(parameters.length == 1 && parameters[0].equals(String.class)){
//					displayString = (String) m.invoke(null, proposal);
//				}
//			}
//		}
		
		return displayString;
	}
	
	private String getKeywordSymbolReplacementString(String proposal, String displayString) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		String methodName = "transform_Keyword_replacementString";
		
		String replacementString = displayString;
		
		try {
			Method m = ProposalProvider.getMethod(methodName, String.class);
			replacementString = m.invoke(null, proposal).toString();
		} catch (SecurityException e) {
			//do nothing
		} catch (NoSuchMethodException e) {
			//do nothing
		}
		
//		for (Method m : fMethods) {
//			if (m.getName().equals(methodName)) {
//				Class[] parameters = m.getParameterTypes();
//				if(parameters.length == 1 && parameters[0].equals(String.class)){
//					replacementString = (String) m.invoke(null, proposal);
//				}
//			}
//		}
		
		
		return replacementString;
	}

}
