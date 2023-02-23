import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol

/**
 * This program is to be used to test the brevis scanner.
 * This version is set up to test all tokens, but more code is needed to test 
 * other aspects of the scanner (e.g., input that causes errors, character 
 * numbers, values associated with tokens)
 */
public class P2 {
    public static void main(String[] args) throws IOException { // exception may be thrown by yylex
        // test all non-dynamic tokens
        runTest("allTokens.in");
        CharNum.num = 1;

		// test numeric

		// test max numeric
		runMaxIntegerTest("maxInteger");
		CharNum.num = 1;
	    
        // test valid strings
		runTest("strings");
		CharNum.num = 1;
			
		// test invalid strings
		runTest("invalidStrings");
		CharNum.num = 1;

		// test multiple tokens per line


}

/* Main driver function for running tests
*
* Open and read from input file
* For each token read, write the corresponding string to <input name>.out
* If the input file contains all tokens, one per line, we can verify
* correctness of the scanner by comparing the input and output files
* (e.g., using a 'diff' command).
*/
private static void runTest(String testName) throws IOException {
	// open input and output files
	FileReader inFile = null;
	PrintWriter outFile = null;
	try {
		inFile = new FileReader(testName + ".in");
		outFile = new PrintWriter(new FileWriter(testName + ".out"));
	} catch (FileNotFoundException ex) {
		System.err.println("File " + testName + ".in not found.");
		System.exit(-1);
	} catch (IOException ex) {
		System.err.println(testName + ".out cannot be opened.");
		System.exit(-1);
	}

	// create and call the scanner
	String tokenVal;
	Yylex scanner = new Yylex(inFile);
	Symbol token = scanner.next_token();
	while (token.sym != sym.EOF) {
		switch (token.sym) {
		case sym.BOOL:
		tokenVal = "boolean"; 
		break;
		case sym.INT:
		tokenVal = "integer";
		break;
		case sym.VOID:
		tokenVal = "void";
		break;
		case sym.RECORD:
		tokenVal = "record"; 
		break;
		case sym.IF:
		tokenVal = "if";
		break;
		case sym.ELSE:
		tokenVal = "else";
		break;
		case sym.WHILE:
		tokenVal = "while";
		break;								
		case sym.SCAN:
		tokenVal = "scan"; 
		break;
		case sym.PRINT:
		tokenVal = "print";
		break;				
		case sym.RETURN:
		tokenVal = "return";
		break;
		case sym.TRUE:
		tokenVal = "true"; 
		break;
		case sym.FALSE:
		tokenVal = "false"; 
		break;
		case sym.ID:
		tokenVal = ((IdTokenVal)token.value).idVal;
		break;
		case sym.INTLITERAL:  
		tokenVal = ((IntLitTokenVal)token.value).intVal;
		break;
		case sym.STRINGLITERAL: 
		tokenVal = ((StrLitTokenVal)token.value).strVal;
		break;    
		case sym.LCURLY:
		tokenVal = "{";
		break;
		case sym.RCURLY:
		tokenVal = "}";
		break;
		case sym.LPAREN:
		tokenVal = "(";
		break;
		case sym.RPAREN:
		tokenVal = ")";
		break;
		case sym.SEMICOLON:
		tokenVal = ";";
		break;
		case sym.COMMA:
		tokenVal = ",";
		break;
		case sym.DOT:
		tokenVal = ".";
		break;
		case sym.READ:
		tokenVal = "->";
		break;	
		case sym.WRITE:
		tokenVal = "<-";
		break;			
		case sym.PLUSPLUS:
		tokenVal = "++";
		break;
		case sym.MINUSMINUS:
		tokenVal = "--";
		break;	
		case sym.PLUS:
		tokenVal = "+";
		break;
		case sym.MINUS:
		tokenVal = "-";
		break;
		case sym.TIMES:
		tokenVal = "*";
		break;
		case sym.DIVIDE:
		tokenVal = "/";
		break;
		case sym.NOT:
		tokenVal = "\\";
		break;
		case sym.AND:
		tokenVal = "&&";
		break;
		case sym.OR:
		tokenVal = "||";
		break;
		case sym.EQUALS:
		tokenVal = "==";
		break;
		case sym.NOTEQUALS:
		tokenVal = "\\=";
		break;
		case sym.LESS:
		tokenVal = "<";
		break;
		case sym.GREATER:
		tokenVal = ">";
		break;
		case sym.LESSEQ:
		tokenVal = "<=";
		break;
		case sym.GREATEREQ:
		tokenVal = ">=";
		break;
		case sym.ASSIGN:
		tokenVal = "=";
		break;
		default:
		tokenVal = "UNKNOWN TOKEN";
		} // end switch

		//Check that the character length is correct and print error if not
		if (CharNum.num != tokenVal.length() + 1) {
			outFile.println("Invalid character incrementation for token: " + tokenVal);
		}

		//Else print out token value so diff succeeds
		else{
			outFile.println(tokenVal)
		}
		token = scanner.next_token();
	} // end while
	outFile.close();
}
private void runInvalidStringsTest(String testName) throws IOException{
	final PrintStream origErr = System.err;  // save original error stream
	PrintStream outFile = null;   // output file you want error messages to go to

	try {
		outFile = new PrintStream("<name of your output file>");
	} catch (FileNotFoundException ex) {
		System.err.println("File ... cannot be opened.");
		System.exit(-1);
	}

	System.setErr(outFile);  // set the error stream to the output file

	// your testing code 

	outFile.close();         // close output file
	System.setErr(origErr);  // set error stream back to original System.err
}
private void runMaxIntegerTest(String testName) throws IOException{
	// create and call the scanner
	String tokenVal;
	Yylex scanner = new Yylex(inFile);
	Symbol token = scanner.next_token();
	while (token.sym != sym.EOF) {
		switch (token.sym) {
		case sym.BOOL:
		tokenVal = "boolean"; 
		break;
		case sym.INT:
		tokenVal = "integer";
		break;
		case sym.VOID:
		tokenVal = "void";
		break;
		case sym.RECORD:
		tokenVal = "record"; 
		break;
		case sym.IF:
		tokenVal = "if";
		break;
		case sym.ELSE:
		tokenVal = "else";
		break;
		case sym.WHILE:
		tokenVal = "while";
		break;								
		case sym.SCAN:
		tokenVal = "scan"; 
		break;
		case sym.PRINT:
		tokenVal = "print";
		break;				
		case sym.RETURN:
		tokenVal = "return";
		break;
		case sym.TRUE:
		tokenVal = "true"; 
		break;
		case sym.FALSE:
		tokenVal = "false"; 
		break;
		case sym.ID:
		tokenVal = ((IdTokenVal)token.value).idVal;
		break;
		case sym.INTLITERAL:  
		tokenVal = ((IntLitTokenVal)token.value).intVal;
		break;
		case sym.STRINGLITERAL: 
		tokenVal = ((StrLitTokenVal)token.value).strVal;
		break;    
		case sym.LCURLY:
		tokenVal = "{";
		break;
		case sym.RCURLY:
		tokenVal = "}";
		break;
		case sym.LPAREN:
		tokenVal = "(";
		break;
		case sym.RPAREN:
		tokenVal = ")";
		break;
		case sym.SEMICOLON:
		tokenVal = ";";
		break;
		case sym.COMMA:
		tokenVal = ",";
		break;
		case sym.DOT:
		tokenVal = ".";
		break;
		case sym.READ:
		tokenVal = "->";
		break;	
		case sym.WRITE:
		tokenVal = "<-";
		break;			
		case sym.PLUSPLUS:
		tokenVal = "++";
		break;
		case sym.MINUSMINUS:
		tokenVal = "--";
		break;	
		case sym.PLUS:
		tokenVal = "+";
		break;
		case sym.MINUS:
		tokenVal = "-";
		break;
		case sym.TIMES:
		tokenVal = "*";
		break;
		case sym.DIVIDE:
		tokenVal = "/";
		break;
		case sym.NOT:
		tokenVal = "\\";
		break;
		case sym.AND:
		tokenVal = "&&";
		break;
		case sym.OR:
		tokenVal = "||";
		break;
		case sym.EQUALS:
		tokenVal = "==";
		break;
		case sym.NOTEQUALS:
		tokenVal = "\\=";
		break;
		case sym.LESS:
		tokenVal = "<";
		break;
		case sym.GREATER:
		tokenVal = ">";
		break;
		case sym.LESSEQ:
		tokenVal = "<=";
		break;
		case sym.GREATEREQ:
		tokenVal = ">=";
		break;
		case sym.ASSIGN:
		tokenVal = "=";
		break;
		default:
		tokenVal = "UNKNOWN TOKEN";
		} // end switch

		if ((token.sym == sym.INTLITERAL) && Integer.parseInt(tokenVal) != Integer.MAXINT){
			System.err.println("Returned non-max integer: " + tokenVal);
		}

		token = scanner.next_token();
	}// end while
}



