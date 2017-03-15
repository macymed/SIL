import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class LexicalAnalyzer {
	public static final int LINE_NO = 0;
	public static final int CONSTANT = 1;
	public static final int LITERAL = 2;
	public static final int SPECIAL = 3;
	public static final int IDENTIFIER = 4;
	public static final int RESERVED = 5;
	public static final int EOL = 6;
	public static final int LEFT_PAREN = 7;
	public static final int RIGHT_PAREN = 8;
	public static final int PLUS = 9;
	public static final int MINUS = 10;
	public static final int MULT = 11;
	public static final int DIV = 12;
	public static final int RET = 13;
	
	private static String[] special = {"=", "==", ">", "<", "(", ")", "+", "-", "*",
			"/", "%", ",", "!", "|", "&", "[", "]", "{", "}", "#" , ";" };
	private static Set<String> specialSet = new HashSet<String>(Arrays.asList(special));
	
	private static String[] reserved = {"PRINT", "PRINTLN", "LET", "INTEGER", "INPUT", "END", "IF", "THEN", "GOTO", "GOSUB", "RET"};
	private static Set<String> reservedSet = new HashSet<String>(Arrays.asList(reserved)); 
	private static Map<Integer, String> fileMap= new HashMap<Integer, String>();
	public static Map<Integer, Integer> lineNoMap = new HashMap<Integer, Integer>();
	private static List<Token> tokens = new ArrayList<Token>();
	public static Map<Integer, LinkedList<Token>> lineMap = new HashMap<Integer, LinkedList<Token>>();

	public LexicalAnalyzer(){
		tokens.clear();	
	}
	
	public String getFileName(){
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter file name to analyze.");
		String result = scan.nextLine();
		return result;
	}
	
	public void analyzeFile(){
		for(int i = 1; i <= fileMap.size(); i++){
			analyzeLine(i);
			LinkedList<Token> tokenLL = new LinkedList<Token>();
			
			//first token is line number
			Token lineNumber = tokens.get(0);
			lineNumber.token = Token.LINE_NO;
			tokens.remove(0);		
			tokenLL.addAll(tokens);
			
			//mimicking a bidirectional map, put both mappings in 
			lineNoMap.put(i, Integer.valueOf(lineNumber.text));
			lineNoMap.put(Integer.valueOf(lineNumber.text), i);
			lineMap.put(Integer.valueOf(lineNumber.text), tokenLL);
			tokens.clear();
		}
	}
	public void buildFileMap(String filename) throws IOException{
		int lineCount = 1;
		String line;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		while((line = br.readLine()) != null){
			fileMap.put(lineCount, line);
			lineCount++;
		}
	}
	
	public List<Token> getTokens(){
		return tokens;
	}
	
	
	//returns next char from file
	private static char getChar(int lineNum, int pos){
		String line = fileMap.get(lineNum);
		if(pos >= line.length()){
			return (char) -1;
		}else{
			return fileMap.get(lineNum).charAt(pos);
		}
	}
	
	private static void analyzeLine(int line){
		int pos = 0;
		char current = '0';
		//equality
		while(current != (char) -1){
			current = getChar(line, pos);
			pos += 1;
			//skip whitspace
			while(current == ' '){
				current = getChar(line, pos);
				pos += 1;
			}
			if(current == '='){
				char next = getChar(line, pos);
				if(next == '='){
					tokens.add(new Token(SPECIAL, "=="));
					//System.out.println("==");
					pos += 1;
				}else{
					tokens.add(new Token(SPECIAL,"="));
					//System.out.println("=");
				}
				
			//special chars
			}else if(current != '=' && specialSet.contains(String.valueOf(current))){
				if(current == '+'){
					tokens.add(new Token(PLUS, String.valueOf(current)));
				}else if(current == '-'){
					tokens.add(new Token(MINUS, String.valueOf(current)));
				}else if(current == '*'){
					tokens.add(new Token(MULT, String.valueOf(current)));
				}else if(current == '/'){
					tokens.add(new Token(DIV, String.valueOf(current)));
				}else if(current == '('){
					tokens.add(new Token(LEFT_PAREN, String.valueOf(current)));
				}else if(current == ')'){
					tokens.add(new Token(RIGHT_PAREN, String.valueOf(current)));
				}else{
					tokens.add(new Token(SPECIAL, String.valueOf(current)));
				//System.out.println(current);
				}
			
			//literals
			}else if(current == '"'){
				String result = "\"";
				char next = getChar(line, pos);
				while(next != '"' && next != -1){
					result += next;
					pos +=1;
					next = getChar(line, pos);
				}
				result += "\"";
				pos += 1;
				tokens.add(new Token(LITERAL, result));
				//System.out.println(result);
			
			//identifiers reserved and constants
			}else{
				String result = "";
				char next ='0';
				while(current != ' '){
					result += current;
					next = getChar(line, pos);
					if(next == (char) -1 || specialSet.contains(String.valueOf(next)) || next == '"' ){
						current = next;
						break;
					}else{
						current = next;
						pos +=1 ;
					}
				}
				
				Pattern constant = Pattern.compile("\\d+");
				if(constant.matcher(result).matches()){
					tokens.add(new Token(CONSTANT, result));
				}else if(result.toUpperCase().equals("RET")){
					tokens.add(new Token(RET, result));
				}else if(reservedSet.contains(result.toUpperCase())){
					tokens.add(new Token(RESERVED, result));
				}else{
					tokens.add(new Token(IDENTIFIER, result));
				}
				//tokens.add(new Token(RESERVED, result));
				//System.out.println(result);
	
			}	
		}
	}
}
