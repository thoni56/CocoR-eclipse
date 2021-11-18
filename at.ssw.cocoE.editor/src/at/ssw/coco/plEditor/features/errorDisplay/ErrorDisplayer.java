package at.ssw.coco.plEditor.features.errorDisplay;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import at.ssw.coco.plEditor.model.CocoClassLoader;

/**
 * Uses Java Reflection to create a new Coco Parser and parse the Document's content for errors. <br><br>
 * 
 * The errors are added as new annotations by creating and adding new Markers 
 * <code>(org.eclipse.core.resources.IMarker)</code> to given target file
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class ErrorDisplayer {
	
	private Object fScanner;
	private Object fParser;
	
	private IDocument fDocument;
	private IFile fFile;
	
	/** All Constructors of the Scanner Class */
	private Constructor<?>[] fScannerConstructors;
	/** All Constructors of the Parser Class */
	private Constructor<?>[] fParserConstructors;
//	/** All Methods of the Scanner Class */
//	private Method[] fParserMethods;
	
	private Method ParseErrorsMethod;
	
	private Class<?> Parser;

	/**
	 * The Constructor
	 * @param loader The CocoClassLoader of the CLNGeditor
	 * @param document The content of the CLNGEditor
	 * @param file The file of the CLNGEditor
	 */
	public ErrorDisplayer(CocoClassLoader loader, IDocument document, IFile file){
		fDocument = document;
		fFile = file;
		
		fScannerConstructors = loader.getScannerConstructors();
		fParserConstructors =  loader.getParserConstructors();
//		fParserMethods = loader.getParserMethods();
		
		Parser = loader.getParser();
		
		try {
			ParseErrorsMethod = Parser.getMethod("ParseErrors");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * computes the errors within the content of the CLNGEditor and displays them
	 */
	public void computeErrors(){
		String errors = "";
		try {
			//create new scanner and Parser
			InputStream stream = new ByteArrayInputStream(fDocument.get().getBytes("UTF-8"));
						
			fScanner = instantiateScanner(stream);
			fParser = instantiateParser(fScanner);
			
			//use parser to calculate the errors
			errors = parseErrors();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		//display errors
		createMarkers(errors);
	}
	
	/**
	 * creates error markers, that will automatically be displayed by eclipse
	 * @param errorsString a String containing all errors, that have been found in the CLNGEditor's content
	 */
	private void createMarkers(String errorsString){
		try {
			//first delete all old markers
			fFile.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
	
			//if the error String isn't empty, create new markers.
			if(!errorsString.isEmpty()){
				//parse String into Array of Errors
				String[] errorList = errorsString.split("\n");
				Error[] errors = new Error[errorList.length];
				
				for(int i = 0; i < errors.length; i++){
					errors[i] = new Error(errorList[i]);
				}
				
				//create a new marker for every Error within the array.
				for(Error e : errors){
					 IMarker m = fFile.createMarker(IMarker.PROBLEM);
					 //set the line
					 m.setAttribute(IMarker.LINE_NUMBER, e.line);
					 //set the text, that should be displayed
					 m.setAttribute(IMarker.MESSAGE, "col " + e.col + ": " + e.message);
					 //priority defines wheither the markers will be displayed on top or below other markers.
					 m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
					 //set the icon that should be displayed next to the marker
					 m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				}
			}
		
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}
	
	
	/* 
	 * The following methods are used to create the Coco Scanner 
	 * and invoke it's methods using Java Reflection 
	 */
	
	/** Use Java Reflection to instantiate a new Scanner */
	private Object instantiateScanner(InputStream stream) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Object o = null;
		for (Constructor<?> c : fScannerConstructors) {
			Class<?>[] parameters = c.getParameterTypes();
			if (parameters.length == 1
					&& parameters[0].equals(InputStream.class)) {
				o = c.newInstance(stream);
			}
		}
		return o;
	}
	
	/** Use Java Reflection to instantiate a new Parser */
	private Object instantiateParser(Object scanner) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
		Object o = null;
		for (Constructor<?> c : fParserConstructors) {
			Class<?>[] parameters = c.getParameterTypes();
			if (parameters.length == 1
					&& parameters[0].equals(scanner.getClass())) {
				o = c.newInstance(scanner);
			}
		}
		return o;
	}
	
	/** Use Java Reflection to invoke the "ParseErrors" method of fParser */
	private String parseErrors() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String errors = "";
		errors = ParseErrorsMethod.invoke(fParser).toString();
		return errors;
	}
	
	
	
	class Error{
		/** Constant used to parse the Error String */
		private final char COLON = ':';
		/** Constant used to parse the Error String */
		private final String LINE = "line ";
		/** Constant used to parse the Error String */
		private final String COL = " col ";
		
		/** The message of the Error */
		final String message;
		/** The line of the Error */
		final int line;
		/** The column of the Error */
		final int col;
		
		/**
		 * The Constructor
		 * @param input The input string used to calculate line, column and message. 
		 */
		public Error(String input){			
			line = Integer.parseInt(input.substring(input.indexOf(LINE)+LINE.length(), input.indexOf(COL)));
			col = Integer.parseInt(input.substring(input.indexOf(COL) + COL.length(), input.indexOf(COLON)));
			message = input.substring(input.indexOf(COLON)+2, input.length()-1);
		}
	}

}
