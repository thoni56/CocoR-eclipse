package at.ssw.coco.plEditor.model;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

import at.ssw.coco.plEditor.EditorPlugin;
import at.ssw.coco.plEditor.editor.CLNGEditor;
import at.ssw.coco.plEditor.preferences.pluginStore.PreferenceConstants;

/**
 * This Class is used to load the Scanner and Parser classes from the user's 
 * project folder which are used by the way of Java Reflection to implement the 
 * CLNG Editor's features, such as Syntax Highlighting and Code Completion <br><br>
 * 
 * Provides the Classes for Scanner and Parser, the corresponding Constructors and Methods as well as
 * additional information needed for Java Reflection.
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class CocoClassLoader {
	
	/** Constant used to parse the URL of the Class Files*/
	private final String SOURCE_FOLDER = "src.";
	
	/** used to check if the files have already been initiated */
	private boolean initialized;
	
	/** The corresponding CLNG Editor */
	private CLNGEditor fEditor;
	/** The Suffix of the current CLNGEditor. Also used as Suffix for the configuration files */
	private String editorSuffix;
	/** The project relative path of the Scanner and Parser's package */
	private String packageName;
	/** The absolute path of the Scanner and Parser Class Files */
	private String packagePath;
	/** The absolute path of the Scanner and Parser Class Files */
	private String compilerPath;
	/** The absolute Path of the config folder*/
	private String configPath;
	
	
	/** The desired Scanner class */
	private Class<?> Scanner;
	/** All Constructors of the Scanner Class */
	private Constructor<?>[] fScannerConstructors;
	/** All Methods of the Scanner Class */
	private Method[] fScannerMethods;

	/** The desired Parser class */
	private Class<?> Parser;
	/** All Constructors of the Parser Class */
	private Constructor<?>[] fParserConstructors;
	/** All Methods of the Parser Class */
	private Method[] fParserMethods;
	
	private Class<?> ProposalProvider;
	
	private Method[] fProposalProviderMethods;
	
		
		
	/**
	 * The Constructor
	 * @param editor The CLNG Editor, that this CocoClassLoader belongs to.
	 */
	public CocoClassLoader(CLNGEditor editor){
		fEditor = editor;
		//the field have not been initialized yet
		initialized = false;
	}
	
	/**
	 * Initializes the fields
	 * Since the contents of fEditor are null until the constructor is finished, 
	 * the initialization must be done later and thus a separate method is needed.
	 */
	private void init(){
		computePathsAndNames();
		loadClasses();
		computeFunctions();
		
		initialized = true;
	}
	
	
	
	/**
	 * Calculate editorSuffix, packageName, compilerPath and configPath 
	 */
	private void computePathsAndNames(){
		IFile editorFile = (IFile)fEditor.getEditorInput().getAdapter(IFile.class);
		
		//Suffix of editor and configuration files
		editorSuffix = editorFile.getName();
		editorSuffix = editorSuffix.substring(editorSuffix.indexOf('.')+1);
		editorSuffix = editorSuffix.toLowerCase();
		
		String editorPath = editorFile.getParent().getLocation().toString();	
		
		//path of ATG Editor
		packagePath = editorPath.substring(0, editorPath.lastIndexOf(Path.SEPARATOR));
		
		//path of configuration folder
		configPath = editorPath + Path.SEPARATOR+"config";	
		
		
		//path of Scanner and parser files
		String projectPath = editorFile.getProject().getLocation().toString();
		compilerPath = projectPath + Path.SEPARATOR + "bin";
		
		
		//path of the package for the generated Scanner and parser java files
		packageName = editorFile.getParent().getParent().getProjectRelativePath().toString();
		packageName = packageName.replace('/', '.');
		
		
		if(packageName.contains(SOURCE_FOLDER)){
			packageName = packageName.substring(packageName.indexOf(SOURCE_FOLDER)+SOURCE_FOLDER.length());
			packageName+='.';
		}
		else{
			//If the Scanner and Parser are within the default package return an empty string.
			packageName = "";
		}
	}
	
	/**
	 * Load the Scanner and Parser classes
	 */
	private void loadClasses(){
		//ClassLoader used to load the Scanner and Parser classes
		URLClassLoader loader;
		//Array of URLs that the loader will use to load the Classes.
		URL[] classUrl;
		
		//The folder containing the Scanner and Parser files
		File compilerFolder = new File(compilerPath);

		String configPackage = packageName+"editor.config.";
		
		try {
						
		
			
			String string = EditorPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.fieldName)+Path.SEPARATOR+editorSuffix;
			File f = new File(string);
			//calculate the URL of the folder
	
			URL url = f.toURI().toURL();
			
			//set the URLs, the loader will load from
			classUrl = new URL[] { url };
			loader = new URLClassLoader(classUrl);
						
			String providerPrefix = (new String()+editorSuffix.charAt(0)).toUpperCase()+editorSuffix.substring(1);
			
			//load the Scanner and Parser Classes
			Scanner = loader.loadClass("Scanner");
			Parser = loader.loadClass("Parser");
			
			ProposalProvider = loader.loadClass(providerPrefix+"_ProposalProvider");
			
			
		
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * calculate the Constructors and Functions of the Scanner and Parser Classes
	 */
	private void computeFunctions(){
		fScannerConstructors = Scanner.getConstructors();
		fScannerMethods = Scanner.getMethods();
		
		fParserConstructors = Parser.getConstructors();
		fParserMethods = Parser.getMethods();
		
		fProposalProviderMethods = ProposalProvider.getMethods();
	}
	
	/**
	 * if the fields haven't been initialized yet, do so.
	 */
	private void checkInit(){
		if(!initialized){
			init();
		}
	}

	/**
	 * @return the project relative path of the package containing the Scanner and Parser files
	 */
	public String getPackageName() {
		checkInit();
		return packageName;
	}
	
	/**
	 * @return the absolute path of the package containing the ATGEditor file
	 */
	public String getPackagePath() {
		checkInit();
		return packagePath;
	}

	/**
	 * @return the absolute path of the folder containing the Scanner and Parser files
	 */
	public String getCompilerPath() {
		checkInit();
		return compilerPath;
	}

	/**
	 * @return the suffix used for the editor and configuration files
	 */
	public String getEditorSuffix() {
		checkInit();
		return editorSuffix;
	}

	/**
	 * @return the absolute path of the folder containing the configuration files
	 */
	public String getConfigPath() {
		checkInit();
		return configPath;
	}

	/**
	 * @return the Class of the Scanner
	 */
	public Class<?> getScanner() {
		checkInit();
		return Scanner;
	}


	/**
	 * @return the Class of the Parser
	 */
	public Class<?> getParser() {
		checkInit();
		return Parser;
	}
	
	/**
	 * @return the Class of the ProposalProvider
	 */
	public Class<?> getProposalProvider() {
		checkInit();
		return ProposalProvider;
	}
	
	/**
	 * @return an array containing all constructors of the Scanner Class
	 */
	public Constructor<?>[] getScannerConstructors() {
		return fScannerConstructors;
	}
	
	/**
	 * @return an array containing all constructors of the Parser Class
	 */
	public Constructor<?>[] getParserConstructors() {
		return fParserConstructors;
	}
	
	/**
	 * @return an array containing all methods of the Scanner Class
	 */
//	public Method[] getScannerMethods() {
//		return fScannerMethods;
//	}
	
	/**
	 * @return an array containing all methods of the Parser Class
	 */
//	public Method[] getParserMethods() {
//		return fParserMethods;
//	}
	
	/**
	 * @return an array containing all methods of the ProposalProvider Class
	 */
//	public Method[] getProposalProviderMethods() {
//		return fProposalProviderMethods;
//	}

}
