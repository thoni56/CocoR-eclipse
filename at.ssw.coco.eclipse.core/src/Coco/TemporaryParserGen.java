package Coco;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TemporaryParserGen extends ParserGen {

	private StringWriter stringWriter; 
	public TemporaryParserGen(Parser parser) {
		super(parser);
		stringWriter = new StringWriter();
	}

	@Override
	protected PrintWriter initGen(Generator g) {
		gen = new PrintWriter(stringWriter, true);
		g.OpenGen(gen);
		return gen;
	}
	
	public String getParser(){
		return stringWriter.toString();
	}
}
