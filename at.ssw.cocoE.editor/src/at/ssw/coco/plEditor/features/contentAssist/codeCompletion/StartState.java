package at.ssw.coco.plEditor.features.contentAssist.codeCompletion;

import Coco.NodeProvider;

public class StartState extends State {
	
	NodeProvider start;
	
	public StartState(NodeProvider n) {
		super(n);
		start = n;
	}
	
	@Override
	public NodeProvider getNext(){
//		return start.getSymbol().getGraph();
		return start;
	}

}
