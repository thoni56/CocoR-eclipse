package at.ssw.coco.ide.features.contentAssist.codeCompletion.semanticCompletion;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * This class represents a Method Copletion Proposal
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class MethProposal{
	//the name of the owning class
	private String name;
	//the Binding that represents the Method
	private IMethodBinding binding;
	
	public MethProposal(IMethodBinding b, String n){
		binding = b;
		name = n;
	}
	
	/**
	 * @param list a list of MethProposals
	 * @return true if this MethProposal is an Element of the given list and false otherwise
	 */
	public boolean isElementOf(ArrayList<MethProposal> list){
		for(int i = 0; i< list.size(); i++){
			String s = list.get(i).getContentOf();
			if(getContentOf().equals(s)){
				return true;
			}
		}
		return false;
	}
	
	public IMethodBinding getBinding() {
		return binding;
	}

	/**
	 * This method returns a similar String as the method toString, but the returned String doesn't
	 * contain the Name of the declaring class. Thus this method can be used for comparing VarProposals.
	 * 
	 * @return a String representing the VarProposal
	 */
	public String getContentOf(){
		String result = binding.getName() + "(";
		ITypeBinding[] parameters = binding.getParameterTypes();
				
		for(int i=0; i<parameters.length; i++){
			String pType = parameters[i].getName();
			//String pName = parameters[i].getQualifiedName();
			result += pType + " ";// + pName;
			if((i+1)<parameters.length){
				result+=", ";
			}
		}
		
		result+=") : ";
		result += binding.getReturnType().getName();
		return result;
	}
	
	public String getName() {
		return binding.getName();
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
