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
    public static void main(String[] args) throws IOException {
                                           // exception may be thrown by yylex
        // test all tokens
        runTest("allTokens.in");
        CharNum.num = 1;
	    
        // test valid strings
	runTest("strings");
	CharNum.num = 1;
	    
	// test invalid strings
	    
	// test numeric
	    
	// test max numeric
	
	// test IDs
	    
	// 

}

/*
* Main driver function for running tests
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
Yylex scanner = new Yylex(inFile);
Symbol token = scanner.next_token();
while (token.sym != sym.EOF) {
    switch (token.sym) {
    case sym.BOOL:
	outFile.println("boolean"); 
	break;
    case sym.INT:
	outFile.println("integer");
	break;
    case sym.VOID:
	outFile.println("void");
	break;
    case sym.RECORD:
	outFile.println("record"); 
	break;
    case sym.IF:
	outFile.println("if");
	break;
    case sym.ELSE:
	outFile.println("else");
	break;
    case sym.WHILE:
	outFile.println("while");
	break;								
    case sym.SCAN:
	outFile.println("scan"); 
	break;
    case sym.PRINT:
	outFile.println("print");
	break;				
    case sym.RETURN:
	outFile.println("return");
	break;
    case sym.TRUE:
	outFile.println("true"); 
	break;
    case sym.FALSE:
	outFile.println("false"); 
	break;
    case sym.ID:
	outFile.println(((IdTokenVal)token.value).idVal);
	break;
    case sym.INTLITERAL:  
	outFile.println(((IntLitTokenVal)token.value).intVal);
	break;
    case sym.STRINGLITERAL: 
	outFile.println(((StrLitTokenVal)token.value).strVal);
	break;    
    case sym.LCURLY:
	outFile.println("{");
	break;
    case sym.RCURLY:
	outFile.println("}");
	break;
    case sym.LPAREN:
	outFile.println("(");
	break;
    case sym.RPAREN:
	outFile.println(")");
	break;
    case sym.SEMICOLON:
	outFile.println(";");
	break;
    case sym.COMMA:
	outFile.println(",");
	break;
    case sym.DOT:
	outFile.println(".");
	break;
    case sym.READ:
	outFile.println("->");
	break;	
    case sym.WRITE:
	outFile.println("<-");
	break;			
    case sym.PLUSPLUS:
	outFile.println("++");
	break;
    case sym.MINUSMINUS:
	outFile.println("--");
	break;	
    case sym.PLUS:
	outFile.println("+");
	break;
    case sym.MINUS:
	outFile.println("-");
	break;
    case sym.TIMES:
	outFile.println("*");
	break;
    case sym.DIVIDE:
	outFile.println("/");
	break;
    case sym.NOT:
	outFile.println("\\");
	break;
    case sym.AND:
	outFile.println("&&");
	break;
    case sym.OR:
	outFile.println("||");
	break;
    case sym.EQUALS:
	outFile.println("==");
	break;
    case sym.NOTEQUALS:
	outFile.println("\\=");
	break;
    case sym.LESS:
	outFile.println("<");
	break;
    case sym.GREATER:
	outFile.println(">");
	break;
    case sym.LESSEQ:
	outFile.println("<=");
	break;
    case sym.GREATEREQ:
	outFile.println(">=");
	break;
    case sym.ASSIGN:
	outFile.println("=");
	break;
    default:
	outFile.println("UNKNOWN TOKEN");
    } // end switch

    token = scanner.next_token();
} // end while
outFile.close();
}


