package at.ssw.coco.plEditor.features.contentAssist.codeCompletion;

import java.util.LinkedList;

import Coco.NodeProvider;

public class State {
	
	/** The node represented by the current state */
	private NodeProvider node;
	
	/** Contains all parent Scopes that have been opened to this point. */
	private LinkedList<NodeProvider> stack;
	
	public State(NodeProvider n){
		node = n;
		stack = new LinkedList<NodeProvider>();
	}
	
	public State(NodeProvider n, LinkedList<NodeProvider> s){
		this(n);
		stack = (LinkedList<NodeProvider>) s.clone();
	}

	public NodeProvider getNode() {
		return node;
	}

	public LinkedList<NodeProvider> getStack() {
		return stack;
	}
	
	public NodeProvider getNext(){
		return node.getNext();
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + node.hashCode();
		
		for(NodeProvider np : stack){
			hash = hash * 31 + np.hashCode();
		}
		
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.hashCode() == obj.hashCode();
	}

}
