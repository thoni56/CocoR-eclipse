package at.ssw.coco.plEditor.features.contentAssist.codeCompletion;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.text.IDocument;

import Coco.NodeProvider;
import Coco.Tab;
import at.ssw.coco.plEditor.model.CocoClassLoader;

public class CodeCompletionStrategy {

	private int stopPosition;
	
	private String currentWord;
	
	private Class<?> Scanner;
	private Class<?> Parser;
	
	private Method ScanMethod;
	private Method PeekMethod;
	private Method ResetPeekMethod;
	private Method GetPeekTokenOffsetMethod;
	private Method GetPeekTokenKindMethod;
	private Method GetPeekTokenValMethod;
	
	private Method ParseErrorsMethod;
	private Method GetCodeCompletionProposalsMethod;
	
	
	/** All Constructors of the Coco generated Scanner Class */
	private Constructor<?>[] fScannerConstructors;
	/** All Mentods of the Coco generated Scanner Class */
//	private Method[] fScannerMethods;
	
	
	/** All Constructors of the Coco generated Scanner Class */
	private Constructor<?>[] fParserConstructors;
	/** All Mentods of the Coco generated Scanner Class */
//	private Method[] fParserMethods;


	/** Instance of the Coco generated Scanner */
	private Object fScanner;
	
	/** Tuple of states from the non deterministic automaton, representing the current state of this deterministic power automaton
	 *  Contains only terminal Symbols */
	private Set<State> currentStates;
	
	/** Tuple of states from the non deterministic automaton, representing the next state of this deterministic power automaton 
	 *  Contains only terminal Symbols */
	private Set<State> followerStates;
	
	/** Sorted list of the token completion proposals */
	private SortedSet<NodeProvider> tokenProposals;
	
	/** Sorted list of the production completion proposals */
	private SortedSet<NodeProvider> productionProposals;
	
	/** Sorted list of the production completion proposals */
	private SortedSet<String> userProposals;
	
	
	
	/**
	 * The Constructor
	 * 
	 * @param document the document representation of the editors input.
	 * @param offset current cursor position
	 * @param tab Symbol table generated from Coco
	 * @param loader ClassLoader used to load the Coco generated Scanner and Parser files for Reflection
	 */
	public CodeCompletionStrategy(CocoClassLoader loader){
		
		
		
		Scanner = loader.getScanner();
		Parser = loader.getParser();
		
		//load Coco generated Scanner
//		fScannerMethods = loader.getScannerMethods();
		fScannerConstructors = loader.getScannerConstructors();
		
		fParserConstructors = loader.getParserConstructors();
//		fParserMethods = loader.getParserMethods();
				
		try {
			
			ScanMethod = Scanner.getMethod("Scan");
			PeekMethod = Scanner.getMethod("Peek");
			ResetPeekMethod = Scanner.getMethod("ResetPeek");
			GetPeekTokenOffsetMethod = Scanner.getMethod("getPeekTokenOffset");
			GetPeekTokenKindMethod = Scanner.getMethod("getPeekTokenKind");
			GetPeekTokenValMethod = Scanner.getMethod("getPeekTokenVal");
			
			ParseErrorsMethod = Parser.getMethod("ParseErrors");
			GetCodeCompletionProposalsMethod = Parser.getMethod("getCodeCompletionProposals");
			
			
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	public void start(IDocument document, int offset, Tab tab){
		currentWord = new String();
		
		//instantiate new Proposal lists
		tokenProposals = new TreeSet<NodeProvider>();
		productionProposals = new TreeSet<NodeProvider>();
		userProposals = new TreeSet<String>();
		
		//instantiate the state sets
		currentStates = new HashSet<State>();		
		followerStates = new HashSet<State>();
		
		//enter the start Symbol of the language
		followerStates.add(new StartState(new NodeProvider(tab.gramSy)));
		
		
		
		//set stop position to offset -1, so that unfinished written tokens aren't mistaken for another similar token (most likely as ident)
//		stopPosition = offset-1;
		stopPosition = offset;	
		
		
		//instantiate the scanner
				InputStream stream;
				try {
					stream = new ByteArrayInputStream(document.get().getBytes("UTF-8"));
					fScanner = instantiateScanner(stream);
					scanTocCurrentPosition();
					
					int stopPos = getPeekTokenOffset();
					String doc = document.get();
					
					if(offset>=stopPos){
						currentWord = doc.substring(stopPos, offset);
					}
					
					calculateProposals();
					
//					peek();
					
					String parseString = doc.substring(0,stopPos);
					
					
					
					List<Object> parserlist = new LinkedList<Object>();
					
					for(NodeProvider np : tokenProposals){
						InputStream new_stream = new ByteArrayInputStream(parseString.getBytes());
						
						Object proposalScanner = instantiateScanner(new_stream);
						int n = np.getSymbol().getNumber();
						
						Object parser = instantiateParser(proposalScanner, n , stopPos);
						parse(parser);
							
						parserlist.add(parser);
						
						
						
						
					}
					
					for(Object p : parserlist){
						List<String> proposals = getSemanticProposals(p);
						if(proposals!=null){
							userProposals.addAll(proposals);
						}
					}
					
//					Object proposalScanner = instantiateScanner(new_stream);
//					Object Parser = instantiateParser(proposalScanner, 52, stopPos);
//					parse(Parser);
//					
//					List<String> proposals = getSemanticProposals(Parser);
//					if(proposals == null){
//						System.out.println("null");
//					}
//					else{
//						System.out.println(proposals.size());
//					}
					
				}catch (Exception e) {
					e.printStackTrace();
					
				}		
	}


	/**
	 * Uses the Coco generated Scanner to scan to the current cursor position while updating the current state of the automaton
	 */
	private void scanTocCurrentPosition() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		
		peek();
		while(getPeekTokenOffset()+getPeekTokenVal().length()<stopPosition){
			scan();
			moveToNextState();
			peek();
		}
//		resetPeek();
	}
	
	
	/**
	 * Calculates the possible (terminal) follower states of the current state set.
	 */
	private void moveToNextState() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		currentStates = new HashSet<State>();

		for(State s: followerStates){			
			State temp = getNextState(s);
			currentStates.add(temp);	
			
		}
		
		followerStates = new HashSet<State>();
		for(State s : currentStates){
			followerStates.addAll(calculateFollowers(s));
			
		}
	}
	
	
	/**
	 * Function calculates the direct successor state. (only next is used, sub and down are ignored) 
	 * 
	 * @param s the current state of whom the successor should be calculated
	 * @return the state which the next pointer of s points at.
	 * 		   the next pointer of the parent production if the next pointer of s is null, 
	 * 		   null, if the next pointer is null and the stack is empty (the end of the grammar has been reached), 
	 */
	private State getNextState(State s){
		NodeProvider next = s.getNext();
		LinkedList<NodeProvider> stack = s.getStack();
		
		if(next.getNode() == null){
			if(stack.isEmpty()){
				return null;
			}
			
			else{
				NodeProvider node = stack.removeLast();				
				State parent = new State(node, stack);	
				
				return getNextState(parent);
			}
		}
		return new State(next, stack);
	}
	
	
	/**
	 * Calculates all possible terminal follower states from the given state.
	 * 
	 * @param s the current state of whom the followers should be calculated
	 * @return a HashSet containing all possible terminal followers of s
	 */
	private HashSet<State> calculateFollowers(State s) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		HashSet<State> followers = new HashSet<State>();
		if(s==null){
			
		}
		else{
			NodeProvider node = s.getNode();
			LinkedList<NodeProvider> stack = s.getStack();
			
			int nodeType = node.getType();
			switch(nodeType){
				case NodeProvider.eps:					
					State follower2 = getNextState(s);
					followers.addAll(calculateFollowers(follower2));
					break;
			
				case NodeProvider.opt:
				case NodeProvider.iter:
					
					
					State sub = new State(node.getSub(), stack);
					State next = getNextState(s);
					
					followers.addAll(calculateFollowers(sub));
					
					
					followers.addAll(calculateFollowers(next));
					break;
				case NodeProvider.alt:
					State alt1 = new State(node.getSub(), stack);
					State alt2 = new State(node.getDown(), stack);
					
					
					followers.addAll(calculateFollowers(alt1));			
					
					if(alt2.getNode().getNode()!=null){
						followers.addAll(calculateFollowers(alt2));
					}
					

					break;
					
				case NodeProvider.nt:
					
					
					//only continue if the peek token is a terminal follower of the nt
//					BitSet terminalFollowers = node.getSymbol().getTerminalFollowers();
//					BitSet nonTerminalFollowers = node.getSymbol().getNonTerminalFollowers();
					BitSet startSymbols = node.getSymbol().getStartSymbols();
//					System.out.println(getPeekTokenKind() + " " + node.getSymbol().getName());
//					for(int i = 0; i < startSymbols.size(); i++){
//						System.out.print(i + " " +startSymbols.get(i) + " ");						
//					}
//					System.out.println();
					if(startSymbols.get(getPeekTokenKind())){
						stack.add(node);
						State first = new State(node.getSymbol().getGraph(), stack);
						followers.addAll(calculateFollowers(first));
					}
					else if(node.getSymbol().isDeletable()){
						State over = getNextState(s);
						followers.addAll(calculateFollowers(over));
					}
					
					
					
					break;
				case NodeProvider.t:
				case NodeProvider.wt:
										
					if(getPeekTokenKind() == node.getSymbol().getNumber()){
						followers.add(s);
					}
					break;
					
				default:
					State follower = getNextState(s);
					followers.addAll(calculateFollowers(follower));
					break;
			}
		}
		return followers;
	}
	
	
	
	/**
	 * Calculates the (grammatical based) completion proposals
	 */
	private void calculateProposals() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		currentStates = new HashSet<State>();
		

		for(State s: followerStates){	
			State temp = getNextState(s);
			currentStates.add(temp);	
			
		}
		
		followerStates = new HashSet<State>();
		for(State s : currentStates){
			collextProductionsAndTokens(s);
			
		}
		
	}
	
	
	/**
	 * collects all terminal and non terminal followers of the given state
	 * @param s the current state of whom the completion proposals should be calculated
	 */
	private void collextProductionsAndTokens(State s) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		if(s==null){
			
		}
		
		else{
			NodeProvider node = s.getNode();
			LinkedList<NodeProvider> stack = s.getStack();
			
			int nodeType = node.getType();
			switch(nodeType){
				case NodeProvider.eps:					
					State follower2 = getNextState(s);
					collextProductionsAndTokens(follower2);
					break;
			
				case NodeProvider.opt:
				case NodeProvider.iter:
					
					
					State sub = new State(node.getSub(), stack);
					State next = getNextState(s);
					
					collextProductionsAndTokens(sub);
					
					
					collextProductionsAndTokens(next);
					break;
				case NodeProvider.alt:
					State alt1 = new State(node.getSub(), stack);
					State alt2 = new State(node.getDown(), stack);
					
					
					collextProductionsAndTokens(alt1);			
					
					if(alt2.getNode().getNode()!=null){
						collextProductionsAndTokens(alt2);
					}
					

					break;
					
				case NodeProvider.nt:
					productionProposals.add(s.getNode());
					
					stack.add(node);
					State first = new State(node.getSymbol().getGraph(), stack);
					collextProductionsAndTokens(first);
					
					
					break;
				case NodeProvider.t:
				case NodeProvider.wt:
					
					
					tokenProposals.add(s.getNode());
					break;
					
				default:
					State follower = getNextState(s);
					collextProductionsAndTokens(follower);
					break;
			}
		}
		
	}
	
	
	
	
	
	//Getter
	
	public SortedSet<NodeProvider> getTokenProposals() {
		return tokenProposals;
	}


	public SortedSet<NodeProvider> getProductionProposals() {
		return productionProposals;
	}
	
	public SortedSet<String> getUserProposals() {
		return userProposals;
	}
	
	public String getCurrentWord() {
		return currentWord;
	}
	
	
	
	/* 
	 * The following methods are used to create the Coco Scanner 
	 * and invoke it's methods using Java Reflection 
	 */
	

	/** Use Java Reflection to instantiate a new Scanner */
	private Object instantiateScanner(InputStream stream) throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Object o = null;
		for (Constructor<?> c : fScannerConstructors) {
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
	
	/** Use Java Reflection to instantiate a new Parser */
	private Object instantiateParser(Object scanner, int proposalToken, int stopPosition) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
		Object o = null;
		for (Constructor<?> c : fParserConstructors) {
			Class<?>[] parameters = c.getParameterTypes();
			if (parameters.length == 3
					&& parameters[0].equals(scanner.getClass())
					&& parameters[1].equals(int.class)
					&& parameters[2].equals(int.class)) {
				o = c.newInstance(scanner,proposalToken,stopPosition);
			}
		}
		return o;
	}
	
	/** Use Java Reflection to invoke the "ParseErrors" method of fParser */
	private String parse(Object parser) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String errors = "";
		errors = ParseErrorsMethod.invoke(parser).toString();
		return errors;
	}
	
	private List<String> getSemanticProposals(Object parser) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		List<String> proposals = null;
		proposals = (List<String>) GetCodeCompletionProposalsMethod.invoke(parser);
		return proposals;
	}

}
