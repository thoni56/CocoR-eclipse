package at.ssw.coco.ide.features.contentAssist.codeCompletion.semanticCompletion;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Extends a <code>ASTVisitorr</code> and is used for visiting every Node of the AST. 
 * This class is used to store the ASTNode and Block that correspond to the given offset.
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class ASTNodeSearchVisitor extends ASTVisitor {
	
	private int offset;
	private ASTNode targetNode;
	private ASTNode block;
	private ASTNode root;
	
		
	public ASTNodeSearchVisitor(CompilationUnit ast, int offset){
		super();
		this.offset = offset;
		targetNode = null;
		block = null;
		root = ast.getRoot();
	}
	
	
	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
	 */
	public void preVisit(ASTNode node) {
		//if the offset is within this nodes range, store the node.
		int start = node.getStartPosition();
		int end = start + node.getLength();
				
		if((start<=offset)&&(offset<=end)){
			setTargetNode(node);
		}
	}


	/**
	 * @return the ASTNode of the lowest level at the given offset.
	 */
	public ASTNode getTargetNode() {
		if(targetNode==null){
			targetNode = root;
		}
		return targetNode;
	}


	/**
	 * @return the Block at the given offset or the Type if no Block has been found.
	 */
	public ASTNode getBlock() {
		if(block==null){
			block = getTargetNode();
			while(block!=block.getRoot()&&block.getNodeType()!=ASTNode.TYPE_DECLARATION){
				block=block.getParent();
			}
		}
		return block;
	}


	/**
	 * This method stores the given Node as the new targeNode
	 * 
	 * @param targetNode the ASTNode that shall be stored as the current targeNode.
	 */
	private void setTargetNode(ASTNode targetNode) {
		this.targetNode = targetNode;
		if(targetNode.getNodeType()==ASTNode.BLOCK){
			block = targetNode;
		}
	}
	
	
}
