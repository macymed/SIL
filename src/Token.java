
public class Token {
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
	
	public int token;
	public String text;
	
	public Token(int token, String text){
		super();
		this.token = token;
		this.text = text;
	}
	
	public String toString(){
		return token + ": " + text;
	}

}
