package at.ssw.coco.plEditor.preferences.pluginStore;

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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import at.ssw.coco.plEditor.EditorPlugin;
import at.ssw.coco.plEditor.model.PresentationMapGenerator;
import at.ssw.coco.plEditor.model.TokenMapGenerator;

public class SyntaxColoringPage extends PreferencePage {
	
	private IPreferenceStore store;
	
	private Map<String, Map<String, TextStyle>> editorMap;
	private String[] contentTypes;
	
	private Map<Integer,String> tokenMap;
	private Combo editorChooser;
	private Text filterField;
	
	private  String configLocation;
	
	private SelectedTokenListener listener;
	
	public SyntaxColoringPage(){
		super();
		store = EditorPlugin.getDefault().getPreferenceStore();
		
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType type = contentTypeManager.getContentType("at.ssw.cocoE.editor.CocoEContentType");
		
		contentTypes = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		
		editorMap = loadPreferenceValues();
		
		tokenMap = new HashMap<Integer, String>();
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout());
		
		Composite upperPart = new Composite(content, SWT.NONE);
		upperPart.setLayout(new GridLayout(1, false));
		
		
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType type = contentTypeManager.getContentType("at.ssw.cocoE.editor.CocoEContentType");
		
		String[] contentTypes = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		
		editorChooser = new Combo(upperPart, SWT.NONE);
		
		for(String s : contentTypes){
			editorChooser.add(s);
		}
		
		
		Composite lowerPart = new Composite(content, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.verticalSpacing = 20;
		lowerPart.setLayout(layout);
		
		filterField = new Text(lowerPart, SWT.SINGLE);
		GridData data = new GridData();
		data.widthHint = 100;
		data.heightHint = 20;
		filterField.setLayoutData(data);
	
		
		
		Label l = new Label(lowerPart, SWT.NONE);
		l.setText("Filter");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		l.setLayoutData(data);
	
		
		List tokenList = new List(lowerPart, SWT.V_SCROLL);
		data = new GridData(GridData.FILL_VERTICAL);
		data.widthHint = 150;
		data.heightHint = 200;
		data.horizontalSpan = 2;
		tokenList.setLayoutData(data);
		
//		data = new GridData(GridData.FILL_VERTICAL);
//		tokenList.setLayoutData(data);
		
		Composite rightpart = new Composite(lowerPart, SWT.NONE);
		rightpart.setLayout(new GridLayout());
		
		ColorSelector selector = new ColorSelector(rightpart);

		
		Combo fontStyle = new Combo(rightpart, SWT.NORMAL);
		fontStyle.add("Normal");
		fontStyle.add("Bold");
		fontStyle.add("Italic");
		
	
		
		
		editorChooser.addSelectionListener(new ChosenEditorListener(editorChooser, tokenList));
		filterField.addModifyListener(new FilterListener(tokenList));
		
		listener = (new SelectedTokenListener(tokenList, selector, fontStyle));
		tokenList.addSelectionListener(new SelectedTokenListener(tokenList, selector, fontStyle));		
		selector.addListener(new ColorChangeListener(selector, tokenList));
		fontStyle.addSelectionListener(new FontStyleChangeListener(fontStyle, tokenList));
		
		if(editorChooser.getItemCount()>0){
			editorChooser.select(0);
			generateListContent(tokenList);
		}

		
		
		return content;
	}
	
	private void generateListContent(List l){
		l.removeAll();
	    
		
		
		int selected =  editorChooser.getSelectionIndex();
		if(selected >= 0){
			String editorName = editorChooser.getItem(selected);
		    
		    configLocation = EditorPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.fieldName);
			String configFile = configLocation+Path.SEPARATOR+editorName+Path.SEPARATOR+editorName+".cnfg";
			
			TokenMapGenerator tokenGen = new TokenMapGenerator(configFile);
			tokenMap = tokenGen.getTokenMap();
		}
		
//		Set<Entry<Integer,String>> set = tokenMap.entrySet();
		
		String filter = filterField.getText();
		
		String entry = "cocoE_comment_style";
		if(entry.startsWith(filter)){
			l.add(entry);
		}
		
		for(int i = 1; i<tokenMap.size()-1; i++){
			entry = tokenMap.get(i);
			if(entry.startsWith(filter)){
				l.add(entry);
			}
		}
		if(l.getItemCount()>0){
			l.select(0);
			listener.widgetSelected(null);
		}
	}
	
	
	
	private Map<String, Map<String, TextStyle>> loadPreferenceValues(){
		Map<String, Map<String, TextStyle>> map = new HashMap<String, Map<String, TextStyle>>();
		
		for(String s : contentTypes){
			map.put(s, loadPreferencesForEditor(s));
		}
		
		return map;
	}
	
	
	private Map<String, TextStyle> loadPreferencesForEditor(String editor){
		Map<String, TextStyle> map = new HashMap<String, TextStyle>();
		
		configLocation = EditorPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.fieldName);
		String configFile = configLocation+Path.SEPARATOR+editor+Path.SEPARATOR+editor+".cnfg";
		
		TokenMapGenerator tokenGen = new TokenMapGenerator(configFile);
		tokenMap = tokenGen.getTokenMap();
		
		
		String entry = "cocoE_comment_style";
		
		int red = store.getInt(getColorPreferenceField(editor, entry, RED));
		int green = store.getInt(getColorPreferenceField(editor, entry, GREEN));
		int blue = store.getInt(getColorPreferenceField(editor, entry, BLUE));
		
		RGB color = new RGB(red, green, blue);
		
		int fontStyle = store.getInt(getFontStylePreferenceField(editor, entry));
		
		TextStyle style = new TextStyle(color, fontStyle);
		
		map.put(entry, style);
		
		
		for(int i = 1; i<tokenMap.size()-1; i++){
			entry = tokenMap.get(i);
			
			red = store.getInt(getColorPreferenceField(editor, entry, RED));
			green = store.getInt(getColorPreferenceField(editor, entry, GREEN));
			blue = store.getInt(getColorPreferenceField(editor, entry, BLUE));
			
			color = new RGB(red, green, blue);
			
			fontStyle = store.getInt(getFontStylePreferenceField(editor, entry));
			
			style = new TextStyle(color, fontStyle);
			
			map.put(entry, style);
			
		}
		
		return map;
		
	}
	
	@Override
	protected void performDefaults() {
		int index = editorChooser.getSelectionIndex();
		
		
		if(index>=0){			
			String editor = editorChooser.getItem(index);
			Map<String,TextStyle> styleMap = editorMap.get(editor);
			
			
			ISharedTextColors fSharedColors = EditorsPlugin.getDefault().getSharedTextColors();		
			
			String prefFile = configLocation+Path.SEPARATOR+editor+Path.SEPARATOR+editor+".pref";
			PresentationMapGenerator prefGen = new PresentationMapGenerator(prefFile, fSharedColors);
			Map<String, TextAttribute>prefMap = prefGen.getPresentationMap();
			
			Set<Entry<String,TextStyle>> set = styleMap.entrySet();
			
			for(Entry<String,TextStyle> e : set){				
				String token = e.getKey();
				TextStyle style = e.getValue();
				
				if(prefMap.containsKey(token)){
					TextAttribute attribute = prefMap.get(token);
					style.colorValue = attribute.getForeground().getRGB();
					
					int font = attribute.getStyle();
					int fontStyle = 0;
					if(font == SWT.BOLD){
						fontStyle = 1;
					}
					else if(font == SWT.ITALIC){
						fontStyle = 2;
					}
					style.fontStyle = fontStyle;
					
					
					
				}	
				
			}
			
		}
		listener.widgetSelected(null);
		
		
	}
	
	
	@Override
	public boolean performOk() {
		for(String s : contentTypes){
			storePreferenceValues(s, editorMap.get(s));
		}
		
		
		
		return true;
	}
	
	private void storePreferenceValues(String editor, Map<String, TextStyle> map){
		Set<Entry<String,TextStyle>> set = map.entrySet();
		
		for(Entry<String,TextStyle> e : set){
			String token = e.getKey();
			TextStyle style = e.getValue();
			RGB color = style.colorValue;
			int font = style.fontStyle;
			
			store.setValue(getColorPreferenceField(editor, token, RED), color.red);
			store.setValue(getColorPreferenceField(editor, token, GREEN), color.green);
			store.setValue(getColorPreferenceField(editor, token, BLUE), color.blue);
			
			store.setValue(getFontStylePreferenceField(editor, token), font);
		}
	}
	
	
	class TextStyle {
		RGB colorValue;
		int fontStyle;
		
		public TextStyle(RGB cv, int fs){
			colorValue = cv;
			fontStyle = fs;
		}
		
	}
	
	
	class ChosenEditorListener implements SelectionListener{

		Combo editor;
		List list;
		
		public ChosenEditorListener(Combo fontBox, List l){
			this.editor = fontBox;
			this.list = l;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			generateListContent(list);
			
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			
			
		}
		
	}
	
	class FilterListener implements ModifyListener{

		List list;
		
		public FilterListener(List list){
			this.list = list;
		}
		
		@Override
		public void modifyText(ModifyEvent e) {
			generateListContent(list);
			
		}
		
	}
	
	class SelectedTokenListener implements SelectionListener{

		private List tokenList;
		private ColorSelector selector;
		private Combo fontStyle;
		
		public SelectedTokenListener(List tokenList, ColorSelector selector, Combo fontStyle){
			this.tokenList = tokenList;
			this.selector = selector;
			this.fontStyle = fontStyle;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			int index = tokenList.getSelectionIndex();
			if(index>=0){
				String token = tokenList.getItem(index);
				String editor = editorChooser.getItem(editorChooser.getSelectionIndex());
				
				TextStyle style = editorMap.get(editor).get(token);
				
				selector.setColorValue(style.colorValue);
				fontStyle.select(style.fontStyle);				
			}
			
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}		
	}
	
	class ColorChangeListener implements IPropertyChangeListener{

		private ColorSelector selector;
		private List tokenList;
		
		public ColorChangeListener(ColorSelector selector, List tokenList){
			this.selector = selector;
			this.tokenList = tokenList;
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			int index = editorChooser.getSelectionIndex();
			
			if(index>=0){
				String editor = editorChooser.getItem(index);
				String token = tokenList.getItem(tokenList.getSelectionIndex());
				
				
				TextStyle style = editorMap.get(editor).get(token);
				style.colorValue = selector.getColorValue();
			}			
		}	
	}
	
	class FontStyleChangeListener implements SelectionListener{

		private Combo fontStyle;
		private List tokenList;
		
		
		public FontStyleChangeListener(Combo fontStyle, List tokenList){
			this.fontStyle = fontStyle;
			this.tokenList = tokenList;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			int editorIndex = editorChooser.getSelectionIndex();
			int tokenIndex = tokenList.getSelectionIndex();
			
			if(editorIndex>=0 && tokenIndex>=0){
				String editor = editorChooser.getItem(editorIndex);
				String token = tokenList.getItem(tokenIndex);
				
				
				TextStyle style = editorMap.get(editor).get(token);
				style.fontStyle = fontStyle.getSelectionIndex();
			}			
			
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
