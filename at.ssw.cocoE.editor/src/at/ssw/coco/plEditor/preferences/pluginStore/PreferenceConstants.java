package at.ssw.coco.plEditor.preferences.pluginStore;

public class PreferenceConstants {
	
	
	public static String comboBoxName = "Editors";
	
	public static String comboBoxLabel = "EditorLabel";
	
	public static String fieldName = "LocationField";
	public static String fieldLabel = "Location of the Editor config files: ";
	
	
	
	
	public static String RED = "red";
	public static String GREEN = "green";
	public static String BLUE = "blue";
	
	public static String getColorPreferenceField(String editorName, String tokenName, String color){
		return editorName+"."+tokenName+"."+"COLOR"+"."+color;
	}

	
	public static String getFontStylePreferenceField(String editorName, String tokenName){
		return editorName+"."+tokenName+"."+"FONT_STYLE";
	}
}
