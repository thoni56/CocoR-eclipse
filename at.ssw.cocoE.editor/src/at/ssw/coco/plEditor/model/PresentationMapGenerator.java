package at.ssw.coco.plEditor.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

/**
 * Creates a new Token Map, that maps Token IDs to Token Names, from the given preference file
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class PresentationMapGenerator {
	
	/** constant used for parsing the configuration file */
	private final String ARROW = "->";

	/** the desired result which maps Token names to the corresponding TextAttributes */
	private Map<String, TextAttribute> fPresentationMap;
	
	/** Manager for SWT Color Objects */
	private ISharedTextColors fColors;
	
	
	/**
	 * The Constructor
	 * @param fileName The path and name of the .pref file, from where the PresentationMap should be calculated
	 * @param colors
	 */
	public PresentationMapGenerator(String fileName, ISharedTextColors colors){
		//initialize the presentation map and colors
		fPresentationMap = new HashMap<String, TextAttribute>();
		this.fColors = colors;
		try {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			//fill the presentation map
			generateMap(getFileContent(reader));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}


	/**
	 * Transform the input of the cnfg file into a String
	 * @param reader the reader, that is used to read the content of the pref file
	 * @return a String representation of the pref file.
	 * @throws IOException
	 */
	private String getFileContent(BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		String content;
		
		while((content = reader.readLine())!=null){

			sb.append(content);
			sb.append("\n");
		}
		return sb.toString();
	}


	/**
	 * Fills the Presentation map
	 * @param string String representation of the content of the pref file
	 */
	private void generateMap(String string){
		//Each line is a separate entry in the presentation map.
		String[] lines = string.split("\n");
		for(int i = 0; i<lines.length; i++){
			generateEntry(lines[i]);
		}
		
	}
	
	/**
	 * generate a single entry for the PresentationMap
	 * @param line the line from which the entry will be calculated
	 */
	private void generateEntry(String line){
		// each line has to have the following pattern:
		// name->R/G/B/char
		if(line.contains(ARROW)){
			//parse name
			String[] entry = line.split(ARROW);
			String key = entry[0];
			
			//create TextAttribute from String
			TextAttribute value = generateTextAttribute(entry[1]);
			
			//create new entry
			if(value!=null){
				fPresentationMap.put(key, value);
			}
		}
	}


	/**
	 * generate a new TextAttribute from a String
	 * @param s String representation of a TextAttribute containing the ForeGround Colors and the Style
	 * @return the calculated TextAttribute
	 */
	private TextAttribute generateTextAttribute(String s){		
		// the string representation has to have the following pattern:
		// name->R/G/B/char
		String[] content = s.split("/");
		if(content.length == 4){
			//parse RGB Values and Font Style
			int red = Integer.parseInt(content[0]);
			int green = Integer.parseInt(content[1]);
			int blue = Integer.parseInt(content[2]);
			int x = getStyle(content[3]);
			
			//create ner TextAttribute with the calcualted values
			return new TextAttribute(fColors.getColor(new RGB(red,green,blue)), null, x);	
		}
		return null;
	}
	
	
	/**
	 * get the SWT Font Style corresponding to the String representation
	 * @param String representation of the Font Style
	 * @return the corresponding SWT Font Style
	 */
	private int getStyle(String s){
		//3 Font Styles are possible
		//B -> Bold
		//I -> Italic
		//N -> Normal
		//return normal style for illegal arguments
		if(s.equals("B")){
			return SWT.BOLD;
		}
		if(s.equals("I")){
			return SWT.ITALIC;
		}
		return SWT.NORMAL;
	}
	
	/**
	 * @return the PresentationMap
	 */
	public Map<String, TextAttribute> getPresentationMap() {
		return fPresentationMap;
	}

}
