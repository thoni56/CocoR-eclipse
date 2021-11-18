package at.ssw.coco.plEditor.model;


import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.BLUE;
import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.GREEN;
import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.RED;
import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.getColorPreferenceField;
import static at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants.getFontStylePreferenceField;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import Coco.Parser;
import Coco.TokenMapGenerator;
import at.ssw.coco.core.CoreUtilities;
import at.ssw.coco.plEditor.EditorPlugin;
import at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants;

/**
 * Extends <Action> and implements <code> IActionDelegate </code> to add a new menu item to 
 * workspace's context menu. The option will be enabled for all files with the <code> ATG </code> extension. <br><br>
 * 
 * Uses the targeted ATG file to create all required sources for a new CLNG Editor
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class EditorAction extends Action implements IActionDelegate {
	
	/** constant used for computing the package name */
	private final String SRC_FOLDER = "src"+'.';

	/** The selected item */
	private ISelection fSelection;
	
	@Override
	public void run(IAction action) {
		if (!(fSelection instanceof IStructuredSelection)) {
			return;
		}
		//the currently selected atg file
		IFile atgFile = (IFile) (((IStructuredSelection) fSelection).getFirstElement());
		
		//compute the paths of the atg file and the containing folder
		String filePath = atgFile.getLocation().toString();		
		String folderPath = atgFile.getParent().getLocation().toString();
		
		
		
		//compute the package for the Scanner and Parser files.
		String packageName = computePackagename(folderPath);
		
		//compute the path of the folder where the configuration files will be stored in		
		String editorPath =  folderPath + Path.SEPARATOR+"editor";
		String configPath = editorPath+Path.SEPARATOR+"config";
		
		
		//compute the name of the atg file. The name will be used as suffix for the editor and configuration files.
		String editorSuffix = atgFile.getName();
		editorSuffix = editorSuffix.substring(0, editorSuffix.indexOf('.'));
		editorSuffix = editorSuffix.toLowerCase();

		IPreferenceStore store = EditorPlugin.getDefault().getPreferenceStore();
		String test = store.getString(PreferenceConstants.fieldName)+Path.SEPARATOR+editorSuffix;	
		File file = new File(test);
		file.mkdirs();
		
		
		File AtgFile = new File(filePath);
		File ScannerFrame = new File(folderPath+Path.SEPARATOR+"Scanner.frame");
		File ParserFrame = new File (folderPath+Path.SEPARATOR+"Parser.frame");
		
		
		File atgCopy = new File(test+Path.SEPARATOR+atgFile.getName());
		File ScannerFrameCopy = new File(test+Path.SEPARATOR+"Scanner.frame");
		File ParserFrameCopy = new File(test+Path.SEPARATOR+"Parser.frame");
		
		copyFile(AtgFile, atgCopy);
		copyFile(ScannerFrame, ScannerFrameCopy);
		copyFile(ParserFrame, ParserFrameCopy);
		
		
		//create Scanner, Parser and configuration files for this new editor	
		Parser parser = CoreUtilities.generateEditor(atgCopy.getAbsolutePath(), test, test, test, null, null);

		
		

		
		
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType type = contentTypeManager.getContentType("at.ssw.cocoE.editor.CocoEContentType");
		
		
		
		boolean isNew = true;
		
		if(type.isAssociatedWith("."+editorSuffix)){
			isNew = false;
		}
		
		else{
			try {
				type.addFileSpec(editorSuffix, IContentType.FILE_EXTENSION_SPEC);
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
		
		
		
		
		
		
		//Coco automatically generates Backup Files for the Scanner and Parser files with the suffix ".old"
		//Remove these unrequested files.
		removeBackupFiles(folderPath);
		
		
		//create new editor file, if it doesn't exist yet.
		File folderFile = new File(editorPath);
		folderFile.mkdirs();
		
		String editorFilePath = editorPath+Path.SEPARATOR+"test."+editorSuffix;
		File editorFile = new File(editorFilePath);
		
		
		try {
//			createConfigFiles(parser, configPath, editorSuffix, packageName);
			createConfigFiles(parser, folderPath, test, editorSuffix, packageName);
			if(!editorFile.exists()){
				editorFile.createNewFile();
			}
						
		} catch (IOException e) {
			
			e.printStackTrace();
		}		
		
		//refresh the workspace
		try {
			atgFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(null, null, null, test+Path.SEPARATOR+"Scanner.java", test+Path.SEPARATOR+"Parser.java");
		
		if(isNew){
			setDefaultPreferences(editorSuffix);
		}
		
	}
	
	

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;		
	}
	
	
	/** compute the package name for the scanner and parser files */
	private String computePackagename(String folderPath){
		String packageName = folderPath.replace(Path.SEPARATOR, '.');
		
		if(packageName.contains(SRC_FOLDER)){
			//the package name is the the files path in relation to the "src" folder.
			packageName = packageName.substring(packageName.indexOf(SRC_FOLDER)+SRC_FOLDER.length());
		}
		else{
			//if the file is placed within the default package return empty String
			packageName = "";
		}
		
		return packageName;
	}
	
	/** delete the automatically generated Scanner and Parser Backup Files */
	private void removeBackupFiles(String path) {
		new File(path, "Scanner.java.old").delete();
		new File(path, "Parser.java.old").delete();
	}
	
	
	private void createConfigFiles(Parser parser, String folderPath, String configPath, String editorSuffix, String packageName) throws IOException{
		ArrayList terminals = parser.tab.terminals;
		ArrayList productions = parser.tab.nonterminals;
		TokenMapGenerator provider = new TokenMapGenerator(terminals, productions);
		
		
		File file = new File(configPath);
		file.mkdirs();
		
		File tokenMap = new File(file.getAbsolutePath()+Path.SEPARATOR+editorSuffix+".cnfg");
		if(tokenMap.exists()){
			tokenMap.delete();
		}
		tokenMap.createNewFile();
		FileWriter writer = new FileWriter(tokenMap);

		
		provider.generateTokenMap(writer);
		writer.close();
		
		File preferenceMap = new File(file.getAbsolutePath()+Path.SEPARATOR+editorSuffix+".pref");
		if(!preferenceMap.exists()){
			preferenceMap.createNewFile();
			writer = new FileWriter(preferenceMap);
			
			provider.generatePreferenceMap(writer);
			writer.close();
		}
		
		String providerPrefix = (new String()+editorSuffix.charAt(0)).toUpperCase()+editorSuffix.substring(1);
		String proposalProviderFilePath = configPath+Path.SEPARATOR+providerPrefix+"_ProposalProvider.java";
		File proposalProviderFile = new File(proposalProviderFilePath);
		
		generateProposalProvider(proposalProviderFile, "", provider, providerPrefix);
		
		File proposalProviderWorkspaceFile = new File(folderPath+Path.SEPARATOR+providerPrefix+"_ProposalProvider.java");
		
		generateProposalProvider(proposalProviderWorkspaceFile, packageName, provider, providerPrefix);
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(null, null, null, proposalProviderFilePath);
		
		
	}
	
	private void generateProposalProvider(File proposalProviderFile, String packageName, TokenMapGenerator provider, String providerPrefix) throws IOException{
		if(!proposalProviderFile.exists()){
			proposalProviderFile.createNewFile();
			FileWriter writer = new FileWriter(proposalProviderFile);
			
			provider.generateProposalMethods(writer, providerPrefix, packageName);
			writer.close();
		}
	}
	
	private void copyFile(File inputFile, File outputFile){
		try{
			FileReader in = new FileReader(inputFile);
		    FileWriter out = new FileWriter(outputFile);
		    int c;
		    
		    while ((c = in.read()) != -1)
		      out.write(c);
	
		    in.close();
		    out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	private void setDefaultPreferences(String editorSuffix){
		IPreferenceStore store = EditorPlugin.getDefault().getPreferenceStore();
		
		String configLocation = EditorPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.fieldName);
		
		ISharedTextColors fSharedColors = EditorsPlugin.getDefault().getSharedTextColors();		
		
		String prefFile = configLocation + Path.SEPARATOR+editorSuffix + Path.SEPARATOR+editorSuffix+".pref";
		PresentationMapGenerator prefGen = new PresentationMapGenerator(prefFile, fSharedColors);
		Map<String, TextAttribute> prefMap = prefGen.getPresentationMap();
		
		Set<Entry<String, TextAttribute>> set = prefMap.entrySet();
		
		for(Entry<String, TextAttribute> e : set){
			String entry = e.getKey();
			TextAttribute attribute = e.getValue();
			
			Color color = attribute.getForeground();
			int font = attribute.getStyle();
			
			store.setValue(getColorPreferenceField(editorSuffix, entry, RED), color.getRed());
			store.setValue(getColorPreferenceField(editorSuffix, entry, GREEN), color.getGreen());
			store.setValue(getColorPreferenceField(editorSuffix, entry, BLUE), color.getBlue());
			
			int fontStyle = 0;
			if(font == SWT.BOLD){
				fontStyle = 1;
			}
			else if (font == SWT.ITALIC){
				fontStyle = 2;
			}
			
			store.setValue(getFontStylePreferenceField(editorSuffix, entry), fontStyle);
			
		}
		
	}
	
	
}
