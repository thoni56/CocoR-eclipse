package at.ssw.coco.plEditor.features.syntaxHighlighting;

import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.BLUE;
import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.GREEN;
import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.RED;
import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.getColorPreferenceField;
import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.getFontStylePreferenceField;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import at.ssw.coco.plEditor.EditorPlugin;
import at.ssw.coco.plEditor.editor.CLNGEditor;
import at.ssw.coco.plEditor.model.CocoClassLoader;
import at.ssw.coco.plEditor.model.TokenMapGenerator;
import at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants;



/**
 * This class is used to create the correct <code> TextAttribute </code> to the corresponding Token ID<br><br>
 * 
 * The necessary information (Token Map and User configuration) is loaded from the CLNGEditor's configuration and preference files.
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class SyntaxManager {
	
	/** The Name of the default token*/
	private final String DEFAULT_TOKEN = "cocoE_default_style";
	/** The Name of the default comment token*/
	private final String COMMENT_TOKEN = "cocoE_comment_style";
	
	/** The Default Color */
	private final Color DEFAULT_COLOR; 
	/** The Default Text Presentation (Foreground color, Background color, Font style) */
	private final TextAttribute DEFAULT_STYLE;
	
	/** Default Color for Comments*/
	private final Color DEFAULT_COMMENT_COLOR; 
	/** Default Text Presentation (Foreground color, Background color, Font style) for Comments*/
	private final TextAttribute DEFAULT_COMMENT_STYLE;
	
	/** Manager for SWT Color Objects */
	private ISharedTextColors sharedColors;
	
	/** Maps token.kind to the according String value (name) of the token */
	private Map<Integer,String> tokenMap;
	/** Maps the token name to the according Text Attribute*/
	private Map<String, TextAttribute> presentationMap;
	
	/** the CLNGEditor, this Syntax Manager belongs to */
	private CLNGEditor fEditor;
	
	/** The suffix associated with the current editor */
	private String editorName;
	
	/** the editorsmpreference store */
	private IPreferenceStore preferenceStore;
	
	
	/**
	 * The Constructor <br>
	 * Important: before using other methods of the SyntaxManager, call the init() method!
	 * @param colors the SWT Color Manager of the Editor
	 * @param editor owner of the Syntax Manager
	 */
	public SyntaxManager (ISharedTextColors colors, CLNGEditor editor){
		fEditor = editor;	
		
		// set color manager
		sharedColors = colors;
		
		// The default Style has a black text color and is neither italic nor bold.
		DEFAULT_COLOR = sharedColors.getColor(new RGB(200,0,0));
		DEFAULT_STYLE = new TextAttribute(DEFAULT_COLOR);
		
		// The default comment Style has a grey text color and is italic.
		DEFAULT_COMMENT_COLOR = sharedColors.getColor(new RGB(100,100,100));	
		DEFAULT_COMMENT_STYLE = new TextAttribute(DEFAULT_COMMENT_COLOR, null, SWT.ITALIC);
	}
	
	/**
	 * Initializes the fields
	 * Since the contents of fEditor are null until the constructor is finished, 
	 * the initialization must be done later and thus a separate method is needed.
	 */
	public void init(){
		CocoClassLoader loader = fEditor.getClassLoader();
		
		editorName = loader.getEditorSuffix();
		
//		preferenceStore = new PreferenceStore("Coco_Editor.properties");
		preferenceStore = EditorPlugin.getDefault().getPreferenceStore();

//		try {
//			preferenceStore.load();
//		} catch (IOException e) {
//			// Ignore
//		}
//		

		String configPath = EditorPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.fieldName)+Path.SEPARATOR+editorName;
		
		//generate new Token Map
		TokenMapGenerator generator = new TokenMapGenerator(configPath+Path.SEPARATOR+editorName+".cnfg");
		tokenMap = generator.getTokenMap();
			
		presentationMap = new HashMap<String, TextAttribute>();
		
//		//genrate new Presentation Map
//		PresentationMapGenerator gen = new PresentationMapGenerator(configPath+Path.SEPARATOR+editorName+".pref", sharedColors);
//		presentationMap = gen.getPresentationMap();
		
		
		loadPreferences();
		
		//if the presentation map doesn't contain styles for the default or comment token, 
		//add the corresponding default styles to map
		if(!presentationMap.containsKey(DEFAULT_TOKEN)){
			presentationMap.put(DEFAULT_TOKEN, DEFAULT_STYLE);
		}
		if(!presentationMap.containsKey(COMMENT_TOKEN)){
			presentationMap.put(COMMENT_TOKEN, DEFAULT_COMMENT_STYLE);
		}
		
	}
	
	private void loadPreferences(){
		Set<Entry<Integer, String>> set = tokenMap.entrySet();
		
		for(Entry<Integer, String> e : set){
			String token = e.getValue();
			
			int red = preferenceStore.getInt(getColorPreferenceField(editorName, token, RED));
			int green = preferenceStore.getInt(getColorPreferenceField(editorName, token, GREEN));
			int blue = preferenceStore.getInt(getColorPreferenceField(editorName, token, BLUE));
			
			int font = SWT.NORMAL;
			int comboSelection = preferenceStore.getInt(getFontStylePreferenceField(editorName, token));
			if(comboSelection == 1){
				font = SWT.BOLD;
			}
			else if(comboSelection == 2){
				font = SWT.ITALIC;
			}
			
			
			RGB rgb = new RGB(red,green,blue);
			Color color = sharedColors.getColor(rgb);
			
			TextAttribute attribute = new TextAttribute(color, null, font);
			
			presentationMap.put(token, attribute);
		}
	}
	
	/**
	 * @param kind a token from the Coco generated Scanner
	 * @return the Presentation Style for the given Token.kind
	 */
	public TextAttribute getPresentationStyle(int kind){
		if(tokenMap.containsKey(kind)){
//			try {
//				preferenceStore.load();
//			} catch (IOException e) {
//				// Ignore
//			}
			
			
			
			String token = tokenMap.get(kind);
			
			
//			int red = preferenceStore.getInt(getColorPreferenceField(editorName, token, RED));
//			int green = preferenceStore.getInt(getColorPreferenceField(editorName, token, GREEN));
//			int blue = preferenceStore.getInt(getColorPreferenceField(editorName, token, BLUE));
//			
//			int font = SWT.NORMAL;
//			int comboSelection = preferenceStore.getInt(getFontStylePreferenceField(editorName, token));
//			if(comboSelection == 1){
//				font = SWT.BOLD;
//			}
//			else if(comboSelection == 2){
//				font = SWT.ITALIC;
//			}
//			
//			
//			RGB rgb = new RGB(red,green,blue);
//			Color color = sharedColors.getColor(rgb);
//			return new TextAttribute(color, null, font);
			if(presentationMap.containsKey(token)){
				return presentationMap.get(token);
			}
		}
		
		return presentationMap.get(DEFAULT_TOKEN);
	}
	
	
	/**
	 * @return the Presentation Style for the comments 
	 */
	public TextAttribute getCommentsPresentationStyle(){
		
		
		return presentationMap.get(COMMENT_TOKEN);
		
//		String entry = COMMENT_TOKEN;
//		
//		int red = preferenceStore.getInt(getColorPreferenceField(editorName, entry, RED));
//		int green = preferenceStore.getInt(getColorPreferenceField(editorName, entry, GREEN));
//		int blue = preferenceStore.getInt(getColorPreferenceField(editorName, entry, BLUE));
//		
//		int font = SWT.NORMAL;
//		int comboSelection = preferenceStore.getInt(getFontStylePreferenceField(editorName, entry));
//		if(comboSelection == 1){
//			font = SWT.BOLD;
//		}
//		else if(comboSelection == 2){
//			font = SWT.ITALIC;
//		}
//		
//		
//		RGB rgb = new RGB(red,green,blue);
//		Color color = sharedColors.getColor(rgb);
//		return new TextAttribute(color, null, font);
	}
	
	/**
	 * @return the CocoClassLoader of the CLNG Scanner
	 */
	public CocoClassLoader getClassLoader(){
		return fEditor.getClassLoader();
	}

	
//	private void createTestPresentationMap(){
//		presentationMap.put("ident", new TextAttribute(sharedColors.getColor(new RGB(255,0,0)), null, SWT.NORMAL));
//		presentationMap.put("number", new TextAttribute(sharedColors.getColor(new RGB(0,150,0)), null, SWT.ITALIC));
//		presentationMap.put("\"program\"", new TextAttribute(sharedColors.getColor(new RGB(0,0,255)), null, SWT.BOLD));
//		ResourcesPlugin.getPlugin().getWorkspace();
//		
////		IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(IConsoleConstants.ID_CONSOLE_VIEW);
////      if (view != null) {
////      	((TextConsole)((IConsoleView)view).getConsole()).clearConsole();
////      }
//	}
	
	
}
