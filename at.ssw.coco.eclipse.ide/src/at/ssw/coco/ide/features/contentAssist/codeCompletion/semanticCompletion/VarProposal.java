package at.ssw.coco.ide.features.contentAssist.codeCompletion.semanticCompletion;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * This class represents a Variable Completion Proposal
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class VarProposal{
	//the name of the owning class
	private String name;
	//the Binding that represents the Variable Type
	private IVariableBinding binding;
	
	public VarProposal(IVariableBinding b, String n){
		binding = b;
		name = n;
	}
	
	/**
	 * @param list a list of VarProposals
	 * @return true if this VarProposal is an Element of the given list and false otherwise
	 */
	public boolean isElementOf(ArrayList<VarProposal> list){
		for(int i = 0; i< list.size(); i++){
			String s = list.get(i).getContentOf();
			if(getContentOf().equals(s)){
				return true;
			}
		}
		return false;
	}
	
	public IVariableBinding getBinding() {
		return binding;
	}

	public String getName() {
		return binding.getName();
	}

	/**
	 * This method returns a similar String as the method toString, but the returned String doesn't
	 * contain the Name of the declaring class. Thus this method can be used for comparing VarProposals.
	 * 
	 * @return a String representing the VarProposal
	 */
	public String getContentOf(){
		String result = binding.getName();
		String type = binding.getType().getName();
		result += " : " + type;
		return result;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String result = getContentOf();
		result += " - " + name;
 		return result;
	}

}
