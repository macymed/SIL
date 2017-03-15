import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SLInterpreter {
	public static void main(String[] args) throws Exception{
		LexicalAnalyzer lex = new LexicalAnalyzer();
		String filename = lex.getFileName();
		lex.buildFileMap(filename);
		lex.analyzeFile();

		AnotherParser parser = new AnotherParser(lex.lineMap, lex.lineNoMap);
		parser.parseFile();
		//System.out.println(parser.symbols.toString());
		
	}

}
