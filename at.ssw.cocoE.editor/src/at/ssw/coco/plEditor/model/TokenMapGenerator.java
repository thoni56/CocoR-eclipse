package at.ssw.coco.plEditor.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a new Preference Map, that maps Token Names to TextAttrubutes, from the given configuration file. <br>
 * 
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 */
public class TokenMapGenerator {
	
	/** constant used for parsing the configuration file */
	private final String ARROW = "->";

	/** the desired result which maps Token IDs to the corresponding Token Names */
	private Map<Integer, String> fTokenMap;

	/**
	 * The Constructor
	 * @param fileName The path and name of the .cnfg file, from where the TokenMap should be calculated
	 */
	public TokenMapGenerator(String fileName) {
		//initialize the token map
		fTokenMap = new HashMap<Integer, String>();
		try {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			//fill the token map
			generateMap(getFileContent(reader));
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	
	/** Transform the input of the cnfg file into a String
	 * @param reader the reader, that is used to read the content of the cnfg file
	 * @return a String representation of the cnfg file.
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
	 * Fills the Token map
	 * @param string String representation of the content of the cnfg file
	 */
	private void generateMap(String string){
		//Each line is a separate entry in the token map.
		String[] lines = string.split("\n");
		for(int i = 0; i<lines.length; i++){
			generateEntry(lines[i]);
		}
	}
	
	/**
	 * generate a single entry for the TokenMap
	 * @param line the line from which the entry will be calculated
	 */
	private void generateEntry(String line){
		// each line has to have the following pattern:
		// id->name
		if(line.contains(ARROW)){
			//parse id and name
			String[] entry = line.split(ARROW);			
			int key = Integer.parseInt(entry[0]);
			String value = entry[1];
			//create new entry
			fTokenMap.put(key, value);
		}
	}
	
	/**
	 * @return the tokenMap
	 */
	public Map<Integer, String> getTokenMap() {
		return fTokenMap;
	}
}
