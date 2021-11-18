package at.ssw.coco.ide.features.contentAssist.codeCompletion.semanticCompletion;

/**
 * This class represents a semantic action. 
 * 
 * @author Andreas Greilinger
 */
public class SemanticAction {
	
	/** The name of the semantic action*/
	private String name;
	/** The array Level of the semantic action*/
	private int arrayLevel;
	/** Stores if the action is a field or a methode*/
	private boolean isMethod;
	
	public SemanticAction(String name, int arrayLevel, boolean isMethod){
		this.name = name;
		this.arrayLevel = arrayLevel;
		this.isMethod = isMethod;
	}

	/**
	 * @return the name of the action
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the arrayLevel of the action. If the action contains no array access, than 0 is returned
	 */
	public int getArrayLevel() {
		return arrayLevel;
	}

	/**
	 * @return if the action is a field or a method invoke
	 */
	public boolean isMethod() {
		return isMethod;
	}
}
