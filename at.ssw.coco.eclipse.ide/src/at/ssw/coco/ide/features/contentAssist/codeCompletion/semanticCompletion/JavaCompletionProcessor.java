package at.ssw.coco.ide.features.contentAssist.codeCompletion.semanticCompletion;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
 
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import at.ssw.coco.ide.editor.ATGEditor;
import at.ssw.coco.ide.features.contentAssist.ATGContentAssistProcessor;
import at.ssw.coco.ide.features.semanticHighlighting.DocumentSynchronizer;
import at.ssw.coco.ide.features.views.contentoutline.ATGLabelProvider;
import at.ssw.coco.ide.model.detectors.WordFinderAdaptor;
import at.ssw.coco.lib.model.atgmodel.ATGModelProvider;
import at.ssw.coco.lib.model.positions.CocoRegion;
import at.ssw.coco.lib.model.positions.ICocoRegion;



/**
 * Extends a <code>ATGContentAssistProcessor</code> to implement semantic Code Completion for Java Code within 
 * the ParserCode Partition of the <code>ATGEditor</code>
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class JavaCompletionProcessor extends ATGContentAssistProcessor {

	final private int CLASS_DECL = ASTNode.TYPE_DECLARATION;
	final private int METHOD_DECL = ASTNode.METHOD_DECLARATION;

	
	private WordFinderAdaptor wordFinder;
	private IDocument doc;
	private ATGEditor fEditor;
	private CompilationUnit ast;
	private ASTNodeSearchVisitor visitor;
	private DocumentSynchronizer fSyncer;
	private IPackageBinding packageBinding;
	
	public JavaCompletionProcessor(ATGModelProvider modelProvider, ATGEditor e) {
		super(modelProvider);
		fEditor = e;
	}
	
	/**
	 * @see at.ssw.coco.ide.features.contentAssist.ATGContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[]{'.'};
	}
	
	/**
	 * @see at.ssw.coco.ide.features.contentAssist.ATGContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		//the atg document
		doc = viewer.getDocument();
		//the document synchronizer
		fSyncer = fEditor.getSyncer();
		
		//contains all the semantic actions of the current operation
		ArrayList<SemanticAction> wordList = new ArrayList<SemanticAction>();
		
		// create AST and visit nodes to gain information
		gainASTInformation(fSyncer.mapToJava(offset));
				
		//locate the prefix of the current written word
		wordFinder = new WordFinderAdaptor(doc, fIdentDetector);
		String prefix = wordFinder.getPrefix(offset);
		
		//locate the end of the current semantic operation
		int lastWordPos = offset - prefix.length()-1;
		
		try {
			//calculate the semantic actions
			wordList = calculateSemanticActionWordList(lastWordPos);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		if(wordList==null){
			//if word List is null, no Proposals should be shown
			return computeCompletionProposals(viewer, offset, null, null, null);
		}
		else{
			//calculate the fields and Methods of the current class
			ArrayList[] fam =  calculateCurrentFieldsAndMethods();
			ArrayList<VarProposal> fields = extractFields(fam);
			ArrayList<MethProposal> methods = extractMethods(fam);
			ArrayList<VarProposal> locals = extractLocals(fam);
			
			if(wordList.isEmpty()){
				//if the wordList is empty, all proposals of the current Class should be shown
				return computeCompletionProposals(viewer, offset, fields, methods, locals);
			}
			else{
				//if wordList isn't empty, the proposals must be calculated based on the semantic actions within the wordList
				
				int iteration = 0;
				if(wordList.get(0).getName().equals("this")){
					iteration++;
					if(iteration>=wordList.size()){
						//if the List does only contain the action "this", the current Proposals, just without the locals, should be shown
						return computeCompletionProposals(viewer, offset, fields, methods, null);
					}
				}
				//the wordList isn't empty, the locals won't be shown and thus be handled as normal Variable Proposals for further calculating
				fields.addAll(locals);
				//calculate the semantic proposal Lists based on the wordList.
				fam =  calculateSemanticFieldsAndMethods(iteration, wordList, fields, methods);
				fields = extractFields(fam);
				methods = extractMethods(fam);
				
				//calculate the styled completion Proposals based in the gathered fields and method proposals.
				return computeCompletionProposals(viewer, offset, fields, methods, null);
			}
		}
	}
	
	
	/**
	 * @param viewer the text viewer
	 * @param offset the offset
	 * @param fields a list of Variable Proposals
	 * @param methods a list of Method Proposals
	 * @param locals a list of local Variable Proposals
	 * @return the list of proposals
	 */
	private ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset, ArrayList<VarProposal> fields, ArrayList<MethProposal> methods, ArrayList<VarProposal> locals){
		
		
		ITextSelection selection = (ITextSelection)viewer.getSelectionProvider().getSelection();
		WordFinderAdaptor wordFinder = new WordFinderAdaptor(viewer.getDocument(), fIdentDetector);
		String prefix = wordFinder.getPrefix(offset);
		ICocoRegion region = new CocoRegion(offset - prefix.length(), prefix.length() + selection.getLength());

		String prefixLow = prefix.toLowerCase();
		
		LinkedList<ICompletionProposal> completionProposals = new LinkedList<ICompletionProposal>();
		Set<ICompletionProposal> fieldProposals = new TreeSet<ICompletionProposal>(new PorposalComparator());
		Set<ICompletionProposal> methodProposals = new TreeSet<ICompletionProposal>(new PorposalComparator());
		Set<ICompletionProposal> constructorProposals = new TreeSet<ICompletionProposal>(new PorposalComparator());
		Set<ICompletionProposal> localProposals = new TreeSet<ICompletionProposal>(new PorposalComparator());
		
		ATGLabelProvider labelProvider = new ATGLabelProvider();
		
		if(fields!=null){
			for(VarProposal entry : fields){
				String text = entry.toString();
				if (text.toLowerCase().startsWith(prefixLow)) {
					fieldProposals.add(new StyledCompletionProposal(entry,
							region.getOffset(), region.getLength(), labelProvider));				
				}
			}
		}		
		if(methods!=null){
			for(MethProposal entry : methods){
				String text = entry.toString();
				if (text.toLowerCase().startsWith(prefixLow)) {
					if(entry.getBinding().isConstructor()){
						constructorProposals.add(new StyledCompletionProposal(entry,
								region.getOffset(), region.getLength(), labelProvider));
					}
					else{
						methodProposals.add(new StyledCompletionProposal(entry,
								region.getOffset(), region.getLength(), labelProvider));
					}
				}
			}
		}
		if(locals!=null){
			for(VarProposal entry : locals){
				String text = entry.toString();
				if (text.toLowerCase().startsWith(prefixLow)) {
					fieldProposals.add(new StyledCompletionProposal(entry,
							region.getOffset(), region.getLength(), labelProvider));				
				}
			}
		}		
		
		completionProposals.addAll(constructorProposals);
		completionProposals.addAll(localProposals);
		completionProposals.addAll(fieldProposals);
		completionProposals.addAll(methodProposals);
		
		if(completionProposals.isEmpty()){
			String entry = prefix+"no Default Proposals";
			completionProposals.add(new StyledCompletionProposal (entry,
					region.getOffset(), region.getLength(), labelProvider));
		}
		
		return completionProposals.toArray(new ICompletionProposal[completionProposals.size()]);			
	}
	
	/**
	 * This method stores the current AST, generates a new CompletionVisitor and visits 
	 * all of trees ASTNodes to gain information about the AST. After invoking this method,
	 * the visitor will have stored the Positions of current Node and Statementblock.
	 * 
	 * @param offset the offset
	 */
	private void gainASTInformation(int offset){
		//needs to be fired or the Parser File won't be up to date 
		fSyncer.documentChanged();
		
		ast = fSyncer.getAst();
		visitor = new ASTNodeSearchVisitor(ast, offset);
		ast.accept(visitor);
	}
	
	/**
	 * This method calculates a wordList containing the names of all the semantic actions of the Statement at the given offset.
	 * 
	 * @param offset the offset
	 * @return a list of Strings that contains all the names of the semantic actions within the current statement at the given offset
	 * @throws BadLocationException
	 */
	private ArrayList<SemanticAction> calculateSemanticActionWordList(int offset) throws BadLocationException{
		//the Block at the given offset
		ASTNode block = visitor.getBlock();
		//the beginning position of the current block
		int blockStart = block.getStartPosition();
		
		//the wordList containing all the semantic actions
		ArrayList<SemanticAction> wordList = new ArrayList<SemanticAction>();
		
		boolean invalidPos = false;
		while(!invalidPos){
			char c = doc.getChar(offset);
			if (c==' '||c==','||c=='\n'||c=='\t'||c=='\r'||c=='('||c=='['||c=='{'||c=='}'||c=='='){
				//the beginning of the current operation has been found
				return wordList;
			}
			else if (doc.getChar(offset)=='.') {
				//the end of a semantic action has been found
				offset--;
				
				boolean isMethod = false;
				int arrayLevel = 0;
				
				//now the name of that action has to be calculated
				while(fSyncer.mapToJava(offset)>=blockStart && !fIdentDetector.isWordPart(doc.getChar(offset))){
					if(doc.getChar(offset)==' '||c=='\n'||c=='\t'||c=='\r'){
						//ignore blanks and spaces
						offset--;
					}
					else if(doc.getChar(offset)==']'){
						if(isMethod){
							//arrayBraces can not be found between method braces and the method name
							//within a correct operation. Statement is flawed, no proposals should be shown.
							return null;
						}
						arrayLevel++;
						//locate the position before the position of the corresponding '[' character
						offset = getArrayStartPosition(offset, blockStart);
						offset--;
					}
					else if(doc.getChar(offset)==')'){
						if(isMethod){
							//two method braces within one operation are invalid
							//the statement is flawed, no proposals should be shown.
							return null;
						}
						isMethod = true;
						//locate the position before the position the corresponding '(' character
						offset = getMethodStartPosition(offset, blockStart);
						offset--;
					}
					else{
						//invalid start of operation
						return null;
					}
				}
				if(fSyncer.mapToJava(offset)<blockStart){
					//out of bounds (of the block area), will happen if the current java operation is flawed
					//and the number of braces is incorrect.
					invalidPos = true;
				}
				else{
					//the end of a variable/method name has been found
					//extract the word and set the offset to the correct position for further calculations
					ICocoRegion word = wordFinder.findWord(offset);
					String name = wordFinder.extractWord(word);	
					//add the word at the beginning
					SemanticAction sa = new SemanticAction(name, arrayLevel, isMethod);
					wordList.add(0, sa);
					//set the offset to the correct position for the next cycle
					offset = word.getOffset()-1;
				}
			}
			else{
				//invalid start of operation, no proposals should be shown
				return null;
			}
		}
		return null;
	}
	
	/**
	 * This method calculates the position of the corresponding '[' character
	 * of the ']' character at the given offset
	 * 
	 * @param offset the offset
	 * @param blockStart the start position of the statement Block at the given offset
	 * @return the position before the offset of the left brace of the array parameter
	 * @throws BadLocationException
	 */
	private int getArrayStartPosition(int offset, int blockStart) throws BadLocationException{
		//current character is ']' so start at offset-1
		offset--;
		
		//represents the brace level, after the correct '[' has been found, level will be 0
		int level = 1;
		while(fSyncer.mapToJava(offset)>=blockStart && level>0){
			char c = doc.getChar(offset);
			if(c == ']'){
				//if another ']' is found, increase the level
				level++;
			}
			else if(c=='['){
				//if a '[' is found, decrease the level
				level--; 
			}
			offset--;
		}
		//offset will decrease even after finding the correct brace so offset must be increased by one.
		return offset++;
	}
	
	/**
	 * This method calculates the position of the corresponding '(' character
	 * of the ')' character at the given offset
	 * 
	 * @param offset the offset
	 * @param blockStart the start position of the statement Block at the given offset
	 * @return the position before the offset of the left brace of the method parameters
	 * @throws BadLocationException
	 */
	private int getMethodStartPosition(int offset, int blockStart)throws BadLocationException{
		//current character is ')' so start at offset-1
		offset--;
		//represents the brace level, after the correct '(' has been found, level will be 0
		int level = 1;
		while(fSyncer.mapToJava(offset)>=blockStart && level>0){
			char c = doc.getChar(offset);
			if(c == ')'){
				//if another ')' is found, increase the level
				level++;
			}
			else if(c=='('){
				//if a '(' is found, decrease the level
				level--; 
			}
			offset--;
		}
		//offset will decrease even after finding the correct brace so offset must be increased by one.
		return offset++;
	}
	
	/**
	 * This method calculates the class that is currently edited by the user
	 * when code Completion is invoked.Then all the contained fields and methods
	 * of this class are gathered and returned.
	 * 
	 * @return an Array of length 2, containing the Proposal lists
	 */
	private ArrayList[] calculateCurrentFieldsAndMethods(){
		//the root of the ast
		ASTNode root = ast.getRoot();
		//the currently edited ASTNode
		ASTNode current = visitor.getTargetNode();
		
		//List containing all the fields of this class
		ArrayList <VarProposal> fields = new ArrayList<VarProposal>();
		//List containing all the methods of this class
		ArrayList <MethProposal> methods = new ArrayList<MethProposal>();
		//List containing all the local fields of this method
		ArrayList <VarProposal> locals = new ArrayList<VarProposal>();
		
		//calculate the current class
		while(current!=root && current.getNodeType()!=CLASS_DECL){
			if(current.getNodeType()==METHOD_DECL){
				MethodDeclaration methDecl = (MethodDeclaration) current;
				LocalVariableCollector collector = new LocalVariableCollector();
				methDecl.accept(collector);
				locals.addAll(collector.getLocalVariables());
			}
			current = current.getParent();
		}
				
		if(current!=root){
			TypeDeclaration classDecl = (TypeDeclaration) current;
			//the Binding representing the Type
			ITypeBinding classBinding = classDecl.resolveBinding();
			//the name of the class
			String name = classBinding.getName();
			//the Package that class is declared in
			packageBinding = classBinding.getPackage();
			
			//calculate the fields and methods of the class
			IVariableBinding[] vb = classBinding.getDeclaredFields();
			IMethodBinding[] mb = classBinding.getDeclaredMethods();
			
			//insert the fields and methods into the Lists
			for (int i=0; i<vb.length;i++){
				fields.add(new VarProposal(vb[i], name));
			}
			for (int i=0; i<mb.length;i++){
				methods.add(new MethProposal(mb[i], name));
			}
			
			//calculate the global fields and methods of the super classes
			ITypeBinding parent= classBinding.getSuperclass();
			fields.addAll(getGlobalFields(parent, true));
			methods.addAll(getGlobalMethods(parent, true));
			
		}
		ArrayList[] result = {fields, methods, locals}; 
		return result;
	}
	
	/**
	 * This rekursive method calculates the semantic field- and method proposals based on the given semantic wordList.
	 * For every iteration the corresponding semantic action is searched within the proposals of the last iteration 
	 * and the new proposals will be calculated based on the Type of that semantic action.
	 * 
	 * @param iteration the cuttent iteration
	 * @param semanticActions list of Strings containing the semantic actions of the current statement
	 * @param fields a list of Variable Proposals
	 * @param methods a list of Method Proposals
	 * @return an Array of length 2, containing the Proposal lists
	 */
	private ArrayList[] calculateSemanticFieldsAndMethods(int iteration, ArrayList<SemanticAction> semanticActions, ArrayList<VarProposal> fields, ArrayList<MethProposal> methods){
		//array that contains the variable proposal list and the method proposal list
		ArrayList[] completionProposals = null;
		//size of the array
		int size = semanticActions.size()-1;
		
		if(iteration>size){
			//an error has occurred, don't show any proposals
			return null;
		}
		//get the semantic action of the current iteration
		SemanticAction action = semanticActions.get(iteration);
		int arrayLevel = action.getArrayLevel();
		
		if(action.isMethod()){
			//search for the semantic action within the method proposal list
			for(int i=0; i<methods.size();i++){
				if(methods.get(i).getName().equals(action.getName())){
					//proposal has been found
					IMethodBinding b = methods.get(i).getBinding();
					ITypeBinding type = b.getReturnType();
					
					if(arrayLevel > type.getDimensions()){
						//the number of array parameters written at the operation has exceeded the actual array dimension of this type
						return null;
					}
					while (arrayLevel > 0){
						//get the containing element of the array
						type = type.getComponentType();
						arrayLevel--;
					}
					
					//if the return type of the method is void, ignore this Method
					if(!type.getName().equals("void")){
						completionProposals =  calculateSemanticFieldsAndMethds(type);
						if(iteration == size){
							//end of Statement has been reached, return the calculated proposals
							return completionProposals;
						}
						else{
							//calculate the semantic proposals for the next iteration
							completionProposals = calculateSemanticFieldsAndMethods(iteration+1, semanticActions, extractFields(completionProposals), extractMethods(completionProposals));
							if(completionProposals!=null){
								return completionProposals;
							}
						}
					}
				}
			}
		}
		else{ // if action is a Variable
			//search for the semantic action within the variable proposal list
			for(int i=0; i<fields.size(); i++){
				if(fields.get(i).getName().equals(action.getName())){
					//proposal has been found
					IVariableBinding b = fields.get(i).getBinding();
					ITypeBinding type = b.getType();
					if(arrayLevel > type.getDimensions()){
						//the number of array parameters written at the operation has exceeded the actual array dimension of this type
						return null;
					}
					while (arrayLevel > 0){
						//get the containing element of the array
						type = type.getComponentType();
						arrayLevel--;
					}
					//calculate the proposals based on the Type of the found variable
					completionProposals = calculateSemanticFieldsAndMethds(type);
					
					if(iteration == size){
						//end of Statement has been reached, return the calculated proposals
						return completionProposals;
					}
					else{
						//calculate the semantic proposals for the next iteration
						completionProposals = calculateSemanticFieldsAndMethods(iteration+1, semanticActions, extractFields(completionProposals), extractMethods(completionProposals));
						if(completionProposals!=null){
							return completionProposals;
						}
					}
				}
			}
		}
		//name of the variable or method hasn't been found or the return type of the Method is null
		return null;
	}
	
	/**
	 * This method calculates the semantic fields and methods of the given binding.
	 * 
	 * @param b the TypeBinding representing the Type
	 * @return an Array of length 2, containing the Proposal lists
	 */
	private ArrayList[] calculateSemanticFieldsAndMethds(ITypeBinding b){
		//the List containing all the Variable Proposals
		ArrayList <VarProposal> fields = new ArrayList<VarProposal>();
		//The List containing all the Method Proposals
		ArrayList <MethProposal> methods = new ArrayList<MethProposal>();
		
		//add all the fields and methods to the lists
		fields.addAll(getPrivateFields(b));
		fields.addAll(getGlobalFields(b, false));
		methods.addAll(getPrivateMethods(b));
		methods.addAll(getGlobalMethods(b, false));
		
		ArrayList[] result = {fields, methods}; 
		return result;
	}
	
	/**
	 * This method calculates all the global variables declared within the given type as well as inherited from super types.
	 * 
	 * @param b the TypeBinding representing the Type
	 * @return a list of VarProposals that contains all the global fields of the given Type
	 */
	private ArrayList<VarProposal> getGlobalFields(ITypeBinding b, boolean inherited){
		//List containing all the variable proposals
		ArrayList<VarProposal> fields = new ArrayList<VarProposal>();
		
		
		
		while(b!=null){
			//name of the class
			String name = b.getName();
			//array containing all the fields
			IVariableBinding[] vb = b.getDeclaredFields();
			
			//add all the global proposals within the array into the list.
			for(int i = 0; i<vb.length; i++){
				IVariableBinding temp = vb[i];
				int modifier = temp.getModifiers();
				//find out if the current type has been declared in the same package 
				//as the class where code completion has been called
				boolean isSamePackage = false;
				if(temp.getDeclaringClass().getPackage().equals(packageBinding)){
					isSamePackage=true;
				}
				if(Modifier.isPublic(modifier)||(Modifier.isProtected(modifier)&&inherited)||(isSamePackage && !Modifier.isPrivate(modifier) && !Modifier.isProtected(modifier))){
					VarProposal entry = new VarProposal(temp, name);
					
					// if the Proposal has already been added, don't add it a second time.
					if(!entry.isElementOf(fields)){
						fields.add(new VarProposal(temp, name));
					}
				}
			}
			//get the superclass for he next iteration
			//if the current type is an array, the superclass is always java.lang.Object.
			if(b.isArray()){
				b=ast.getAST().resolveWellKnownType("java.lang.Object");
			}
			else{
				b=b.getSuperclass();
			}
		}
		return fields;
	}
	
	/**
	 *  This method calculates all the private variables declared within the given type
	 * 
	 * @param b the TypeBinding representing the Type
	 * @return a list of VarProposals that contains all the private fields of the given Type
	 */
	private ArrayList<VarProposal> getPrivateFields(ITypeBinding b){
		//List containing all the variable proposals
		ArrayList<VarProposal> fields = new ArrayList<VarProposal>();		
		//name of the class
		String name = b.getName();
		//array containing all the fields
		IVariableBinding[] vb = b.getDeclaredFields();
		
		//add all the private proposals within the array into the list.
		for(int i = 0; i<vb.length; i++){
			IVariableBinding temp = vb[i];
			if(Modifier.isPrivate(temp.getModifiers())){
				fields.add(new VarProposal(temp, name));
			}
		}
		return fields;
	}
	
	
	/**
	 * This method calculates all the global methods declared within the given type as well as inherited from super types.
	 * 
	 * @param b the TypeBinding representing the Type
	 * @return a list of MethProposals that contains all the global methods of the given Type
	 */
	private ArrayList<MethProposal> getGlobalMethods(ITypeBinding b, boolean inherited){
		//List containing all the method proposals
		ArrayList<MethProposal> methods = new ArrayList<MethProposal>();
		
		
		
		while(b!=null){
			//name of the class
			String name = b.getName();
			//array containing all the methods
			IMethodBinding[] mb = b.getDeclaredMethods();
			
			//add all the global proposals within the array into the list.
			for(int i = 0; i<mb.length;i++){
				IMethodBinding temp = mb[i];
				int modifier = temp.getModifiers();
				
				//find out if the current type has been declared in the same package 
				//as the class where code completion has been called
				boolean isSamePackage = false;
				if(temp.getDeclaringClass().getPackage().equals(packageBinding)){
					isSamePackage=true;
				}
				
				if(Modifier.isPublic(modifier)||(Modifier.isProtected(modifier) && inherited)||(isSamePackage && !Modifier.isPrivate(modifier) && !Modifier.isProtected(modifier))){
					MethProposal entry = new MethProposal(temp, name);
					// if the Proposal has already been added, don't add it a second time.
					if(!entry.isElementOf(methods) && !name.equals(entry.getName())){
						methods.add(entry);
					}
				}
			}
			//get the superclass for he next iteration
			//if the current type is an array, the superclass is always java.lang.Object.
			if(b.isArray()){
				b=ast.getAST().resolveWellKnownType("java.lang.Object");
			}
			else{
				b=b.getSuperclass();
			}
				
		}		
		return methods;
		
		
	}
	/**
	 * This method calculates all the private methods declared within the given type
	 * 
	 * @param b the TypeBinding representing the Type
	 * @return a list of MethProposals that contains all the global methods of the given Type
	 */
	private ArrayList<MethProposal> getPrivateMethods(ITypeBinding b){
		//List containing all the method proposals
		ArrayList<MethProposal> methods = new ArrayList<MethProposal>();
		
		//name of the class
		String name = b.getName();
		//array containing all the methods
		IMethodBinding[] mb = b.getDeclaredMethods();
		
		//add all the global proposals within the array into the list.
		for(int i = 0; i<mb.length;i++){
			IMethodBinding temp = mb[i];
			if(Modifier.isPrivate(temp.getModifiers())){
				methods.add(new MethProposal(temp, name));
			}
		}
		return methods;
	}
	
	/**
	 * @param list a list containing all the Completion Proposals
	 * @return a list containing all the field proposals of the given Proposal list  
	 */
	private ArrayList<VarProposal> extractFields(ArrayList[] list){
		if(list==null || list.length<1){
			return null;
		}
		else {
			return (ArrayList<VarProposal>) list[0]; 
		}
	}
	
	/**
	 * @param list a list containing all the Completion Proposals
	 * @return a list containing all the method proposals of the given Proposal list  
	 */
	private ArrayList<MethProposal> extractMethods(ArrayList[] list){
		if(list==null || list.length<2){
			return null;
		}
		else {
			return (ArrayList<MethProposal>) list[1]; 
		}
	}
	
	/**
	 * @param list a list containing all the Completion Proposals
	 * @return a list containing all the method proposals of the given Proposal list  
	 */
	private ArrayList<VarProposal> extractLocals(ArrayList[] list){
		if(list==null || list.length<3){
			return null;
		}
		else {
			return (ArrayList<VarProposal>) list[2]; 
		}
	}
}