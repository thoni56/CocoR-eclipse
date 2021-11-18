package at.ssw.coco.ide.features.contentAssist.codeCompletion.semanticCompletion;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Extends a <code>ASTVisitorr</code> and is used for visiting every Childnode of given Method. 
 * This class is used to collect the local Variables of the Method, and store them within a List..
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class LocalVariableCollector extends ASTVisitor{
	
	/**
	 * List containing the local Variables
	 */
	private ArrayList<VarProposal> localVariables;
	
	public LocalVariableCollector(){
		super();
		localVariables = new ArrayList<VarProposal>();
	}

	
	/** (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {
		VariableDeclarationFragment vdf = (VariableDeclarationFragment) node;
		IVariableBinding binding = vdf.resolveBinding();
		localVariables.add(new VarProposal(binding, "Local"));
		return true;
	}

	/**
	 * @return the list with the local Variables.
	 */
	public ArrayList<VarProposal> getLocalVariables() {
		return localVariables;
	}
}
