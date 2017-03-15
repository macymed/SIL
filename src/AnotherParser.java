import java.util.LinkedList;
import java.awt.SystemTray;
import java.util.*;

public class AnotherParser {
	public Map<Integer, LinkedList<Token>> tokens;
	public Map<Integer, Integer> lineNos;
	public SymbolTable symbols;
	private Token current;
	private LinkedList<Token> currentLine;
	private int goTo;
	private int goSub;
	private Stack<Integer> retLines;
	private boolean inGoSub;
	private int fileLine;
	
	public AnotherParser(Map<Integer, LinkedList<Token>> tokens, Map<Integer, Integer> lineNos){
		this.tokens = tokens;
		this.lineNos = lineNos;
		symbols = new SymbolTable();
		goTo = 0;
		goSub = 0;
		fileLine = 1;
		retLines = new Stack<Integer>();
	}

	public LinkedList<Token> getLine(int line){
		return tokens.get(line);		
	}
	
	public void parseFile() throws Exception{
		//System.out.println(fileLine);
		int line = lineNos.get(fileLine);

		//System.out.println("CURRENT LINE : " + line);
		//System.out.println(goSub + " " + goTo);
		if(goTo==0 && goSub == 0){
			//System.out.println(fileLine);
			parseLine(line);			
		}
		if(goTo != 0){
			int oldGoTo = goTo;
			goTo = 0;
			fileLine = lineNos.get(oldGoTo);
			parseLine(oldGoTo);
			
		}if(goSub != 0){
			//System.out.println(goSub);
			int oldGoSub = goSub;
			retLines.add(lineNos.get(fileLine));
			fileLine = lineNos.get(oldGoSub);
			goSub = 0;
			//System.out.println(fileLine);
			parseLine(oldGoSub);
			fileLine += 1;
			//System.out.println(fileLine);
			parseFile();
			
		}
		if(goTo == 0 && goSub == 0){
			fileLine +=1;
			//System.out.println(fileLine);
				if(fileLine <= (lineNos.size()/2)){
					parseFile();
				}
		}else{
			parseFile();
		}
	}

	
	public void parseLine(int line) throws Exception{
		LinkedList<Token> currentLineCopy = new LinkedList<Token>();
		currentLineCopy = getLine(line);
		currentLine = (LinkedList) currentLineCopy.clone();
		//System.out.println(currentLine);
		current = currentLine.getFirst();
		statement();
	}
	
	private void nextToken(){
		currentLine.pop();
		if(currentLine.isEmpty()){
			current = new Token(Token.EOL,"");
		}else{
			current = currentLine.getFirst();
		}
	}
	
	private void statement() throws Exception{
		//System.out.println(currentLine);
		if(current.token == Token.IDENTIFIER){
			//id statement
			idStatement();
		}else if(current.token== Token.CONSTANT){
			//expression statement
			int result = evalExpress(currentLine);
		}else if(current.token == Token.RESERVED){
			if(current.text.equalsIgnoreCase("PRINT")){
				//print statement
				nextToken();
				printStatement();
			}else if(current.text.equalsIgnoreCase("PRINTLN")){
				//println statement
				nextToken();
				printStatement();
				System.out.println();
			}else if(current.text.equalsIgnoreCase("LET")){
				//let statement
				nextToken();
				statement();
			}else if(current.text.equalsIgnoreCase("INTEGER")){
				nextToken();
				//integer statement
				integerStatement();
			}else if(current.text.equalsIgnoreCase("INPUT")){
				//input statement
				inputStatement();
			}else if(current.text.equalsIgnoreCase("END")){
				System.exit(0);
			}else if(current.text.equalsIgnoreCase("IF")){
				ifStatement();
			}else if(current.text.equalsIgnoreCase("GOTO")){
				goToStatement();
			}else if(current.text.equalsIgnoreCase("GOSUB")){
				goSubStatement();
			}
		}else if(current.token == Token.RET){
			//System.out.println(retLines.peek() + " retline and " + fileLine);
			fileLine = lineNos.get(retLines.pop()) + 1;
			parseFile();
		}
	}
	
	

	
	private void goToStatement() throws Exception{
		nextToken();
		goTo = Integer.valueOf(current.text);
		//System.out.println("GO TO : " + goTo);
		if(!lineNos.containsValue(goTo)){
			throw new Exception("invalid go to line #");
		}
	}
	
	private void goSubStatement() throws Exception{
		nextToken();
		//System.out.println(goSub);
		goSub = Integer.valueOf(current.text);
		if(!lineNos.containsValue(goSub)){
			throw new Exception("invalid go sub line #");
		}
	}
	
	private void ifStatement() throws Exception{
		nextToken();
		LinkedList<Token> lExpr = new LinkedList<Token>();
		LinkedList<Token> rExpr = new LinkedList<Token>();
		while(!current.text.equals("=") && !current.text.equals("<") && !current.text.equals(">")){
			lExpr.add(current);
			nextToken();
		}
		if(currentLine.isEmpty()){
			throw new Exception("incorrectly formatted if statement, no operand, 2nd expr, or then");
		}
		Token operand = current;
		nextToken();
		if(currentLine.isEmpty()){
			throw new Exception("incorrectly formatted if statement, no 2nd expr or then");
		}
		while(!current.text.equalsIgnoreCase("THEN")){
			if(currentLine.isEmpty()){
				throw new Exception("incorrectly formatted if statement, no then statement");
			}
			rExpr.add(current);
			nextToken();
		}
		nextToken();
		if(currentLine.isEmpty()){
			throw new Exception("no statement following then");
		}
		int a = evalExpress(lExpr);
		int b = evalExpress(rExpr);
		if(operand.text.equals("=")){
			if (a == b){
				statement();
			}else{
				return;
			}
		}else if(operand.text.equals("<")){
			if (a < b){
				statement();
			}else{
				return;
			}
		}else if(operand.text.equals(">")){
			if (a > b){
				statement();
			}else{
				return;
			}
		}
		
	}
	
	private void inputStatement(){
		nextToken();
		Scanner input = new Scanner(System.in);
		while(!currentLine.isEmpty()){
			if(!current.text.equals(",")){
				
				int value = input.nextInt();
				symbols.update(symbols.integers.get(current.text), value);
			}
			currentLine.pop();
			if(!currentLine.isEmpty()){
				current = currentLine.getFirst();
			}
		}
		
	}
	
	private void integerStatement(){
		while(!currentLine.isEmpty() ){
			if(!current.text.equals(",")){
				symbols.add(new IntegerDescriptor(current.text, -1));
			}
			currentLine.pop();
			if(!currentLine.isEmpty()){
				current= currentLine.getFirst();
			}
		}
	}
	
	private void idStatement() throws Exception{
		String id = current.text;
		//System.out.println(id);
		nextToken(); //move on to =
		nextToken(); //move to token after '='
		if(current.text.equals("=")){
			nextToken();//move onto token second = if necessary
		}
		LinkedList<Token> idExpr = new LinkedList<Token>();
		while(!current.text.equals(",") && !currentLine.isEmpty()){
			idExpr.add(current);
			//System.out.println(current.text);
			nextToken();
		}	
		int value = evalExpress(idExpr);
		symbols.add(new IntegerDescriptor(id, value));
	}
	
	
	
	public void printStatement() throws Exception{
		//System.out.println("current token is : " + current);
		if(current.token == Token.EOL || current.text.equalsIgnoreCase("END")){
			return;
		}else if(current.token == Token.LITERAL){
			System.out.print(current.text.subSequence(1, current.text.length()-1));
			//System.out.println(currentLine);
			printHelper();


		}else if(current.text.equals(",")){
			//printHelper();
			nextToken();
			printStatement();
		}else if(current.token == Token.CONSTANT || current.token == Token.IDENTIFIER){
			LinkedList<Token> expr = new LinkedList<Token>();
			//System.out.println(current.text);
			//System.out.println("hi "+ current);
			while(!current.text.equals(",") && !currentLine.isEmpty()){
				expr.add(current);
				if(!currentLine.isEmpty()){
					nextToken();
				}
				//System.out.println(expr);
			}

			//System.out.println(expr);
			//System.out.println("current after expr " + current);
			int result = evalExpress(expr);
			System.out.print(result);
			if(!currentLine.isEmpty()){
				printStatement();
			}
		}
	}
	
	private void printHelper() throws Exception{
		if(!currentLine.isEmpty()){
			nextToken();
		}
		if(current.text.equals(",")){
			nextToken();
			printStatement();			
		}else{
			current = new Token(Token.EOL, "");
		}
	}
	
	
	public int evalExpress(LinkedList<Token> expr) throws Exception{
		//System.out.println(expr);
		Token exprCurr = expr.getFirst();
		LinkedList<Token> leftExpr = new LinkedList<Token>();
		if(expr.size() == 1){
			leftExpr.add(exprCurr);
			return evalNum(leftExpr);
		}else if(exprCurr.token == Token.LEFT_PAREN){
			expr.pop();
			exprCurr = expr.getFirst();
			while(exprCurr.token != Token.RIGHT_PAREN){
				leftExpr.add(exprCurr);
				expr.pop();
				if(!expr.isEmpty()){
					exprCurr = expr.getFirst();

				}
			}
			if(expr.isEmpty()){
				return evalExpress(leftExpr);
			}else{

				expr.pop();
				exprCurr = expr.getFirst();
				Token operand = exprCurr;
				//System.out.println(operand.text);
				expr.pop();
				exprCurr = expr.getFirst();
				if(operand.token == Token.PLUS){
					return evalExpress(leftExpr) + evalExpress(expr);
				}else if(operand.token == Token.MINUS){
					return evalExpress(leftExpr) - evalExpress(expr);
				}else if(operand.token == Token.MULT){
					return evalExpress(leftExpr) * evalExpress(expr);
				}else{
					return evalExpress(leftExpr) / evalExpress(expr);
				}
			}
			
		}else{
			while(!exprCurr.text.equals("+") && !exprCurr.text.equals("-") && !expr.isEmpty()){
				leftExpr.add(exprCurr);
				expr.pop();
				if(!expr.isEmpty()){
					exprCurr= expr.getFirst();
				}
			}
			if(expr.isEmpty()){
				return evalMultExpr(leftExpr); 	
			}else{
				Token operand = exprCurr;
				expr.pop();
				exprCurr = expr.getFirst();
				LinkedList<Token> rightExpr = new LinkedList<Token>();
				while(!expr.isEmpty()){
					rightExpr.add(exprCurr);
					expr.pop();
					if(!expr.isEmpty()){
						exprCurr = expr.getFirst();
					}
				}
				
				if(operand.text.equals("+")){
					return evalMultExpr(leftExpr) + evalExpress(rightExpr);
				}else{
					return evalMultExpr(leftExpr) - evalExpress(rightExpr);
				}
			}
		}
		
	}
	
	
	public int evalMultExpr(LinkedList<Token> multExpr) throws Exception{
		Token multExprCurr = multExpr.getFirst();
		LinkedList<Token> leftMultExpr = new LinkedList<Token>();
		if(multExpr.size() == 1){
			leftMultExpr.add(multExprCurr);
			return evalNum(leftMultExpr);
		}else{
			
			while(!multExprCurr.text.equals("*") && !multExprCurr.text.equals("/") && !multExpr.isEmpty()){
				leftMultExpr.add(multExprCurr);
				multExpr.pop();
				multExprCurr= multExpr.getFirst();
			}
			Token operand = multExprCurr;
			multExpr.pop();
			multExprCurr = multExpr.getFirst();
		
			LinkedList<Token> rightMultExpr = new LinkedList<Token>();
			while(!multExprCurr.text.equals("*") && !multExprCurr.text.equals("/") && !multExpr.isEmpty()){
				rightMultExpr.add(multExprCurr);
				multExpr.pop();
				if(!multExpr.isEmpty()){
					multExprCurr = multExpr.getFirst();
				}
			}
			
			if(operand.text.equals("*")){
				Token leftSum = new Token(Token.CONSTANT, String.valueOf(evalMultExpr(leftMultExpr)*evalMultExpr(rightMultExpr)));
				multExpr.addFirst(leftSum);
				return evalMultExpr(multExpr) ;
				
			}else{
				Token leftSum = new Token(Token.CONSTANT, String.valueOf(evalMultExpr(leftMultExpr)/evalMultExpr(rightMultExpr)));
				multExpr.addFirst(leftSum);
				return evalMultExpr(multExpr) ;
			}
		}
		
	}
	
	public int evalNum(LinkedList<Token> atom) throws Exception{
		Token atomCurr = atom.getFirst();
		//single variable or constant
		if (atom.size() == 1){
			if(atomCurr.token == Token.CONSTANT){
				return Integer.parseInt(atomCurr.text);
			}else if(atomCurr.token == Token.IDENTIFIER){
				if(!symbols.integers.containsKey(atomCurr.text)){
					throw new Exception("identifier " + atomCurr.text + " not defined");
				}else{
					return symbols.integers.get(atomCurr.text).value;
				}
			}else{
				return -1;
			}
		//paren expression
		}else{
			LinkedList<Token> parenExpr = new LinkedList<Token>();
			if(atomCurr.token == Token.LEFT_PAREN){
				atom.pop();
				atomCurr = atom.getFirst();
			}
			while(atomCurr.token != Token.RIGHT_PAREN){
				parenExpr.add(atomCurr);
				atom.pop();
				if(atom.isEmpty()){
					atomCurr = atom.getFirst();
				}
			}
			
			return evalExpress(parenExpr);
		}
		
	}
}
