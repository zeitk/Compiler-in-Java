///////////////////////////////////////////////////////////////////////////////
//                   ALL STUDENTS COMPLETE THESE SECTIONS
// Title:        	P2.java   
// Files:          	P2.java
// Semester:        Spring 2023
//
// Author: 			Cory Sterner
// Email: 			cdsterner@wisc.edu
// CS Login:		sterner
// Lecturer's Name:	Beck Hasti
// Lab Section:   	
//
//////////////////// PAIR PROGRAMMERS COMPLETE THIS SECTION ////////////////////
//
//                   CHECK ASSIGNMENT PAGE TO see IF PAIR-PROGRAMMING IS ALLOWED
//                   If pair programming is allowed:
//                   1. Read PAIR-PROGRAMMING policy  
//                   2. choose a partner wisely
//                   3. REGISTER THE TEAM BEFORE YOU WORK TOGETHER 
//                      a. one partner creates the team
//                      b. the other partner must join the team
//                   4. complete this section for each program file.
//
// Pair Partner:     
// Email:            
// CS Login:         
// Lecturer's Name:   
// Lab Section:      (your partner's lab section number)
//
//////////////////// STUDENTS WHO GET HELP FROM OTHER THAN THEIR PARTNER //////
//                   must fully acknowledge and credit those sources of help.
//                   Instructors and TAs do not have to be credited here,
//                   but tutors, roommates, relatives, strangers, etc do.
//
// Persons:          Identify persons by name, relationship to you, and email.
//                   Describe in detail the the ideas and help they provided.
//
// Online sources:   avoid web searches to solve your problems, but if you do
//                   search, be sure to include Web URLs and description of 
//                   of any information you find.
//////////////////////////// 80 columns wide //////////////////////////////////
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
        runTest("allTokens", true);
        CharNum.num = 1;

		// test identifiers
		runTest("ids", true);
		CharNum.num = 1;

		// test max numeric
		runMaxIntegerTest("maxInteger");
		CharNum.num = 1;
	    
        // test valid strings
		runTest("strings", true);
		CharNum.num = 1;
			
		// test invalid strings
		runInvalidStringsTest("invalidStrings");
		CharNum.num = 1;

		// test multiple tokens per line
		runTest("multi", false);
		CharNum.num = 1;
	}

	/* Main driver function for running tests
	*
	* Open and read from input file
	* For each token read, write the corresponding string to <input name>.out
	* If the input file contains all tokens, one per line, we can verify
	* correctness of the scanner by comparing the input and output files
	* (e.g., using a 'diff' command).
	* 
	* testname: the name of the test to be used for the file input and output names
	* checkCharIncrement: determines whether the driver will test the character increment or not
	*/
	private static void runTest(String testName, boolean checkCharIncrement) throws IOException {
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
			tokenVal = Integer.toString(((IntLitTokenVal)token.value).intVal);
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
			if (checkCharIncrement && (CharNum.num != tokenVal.length() + 1)) {
				outFile.println("Invalid character incrementation for token: " + tokenVal);
			}

			//Else print out token value so diff succeeds
			else{
				outFile.println(tokenVal);
			}
			token = scanner.next_token();
		} // end while
		outFile.close();
	}
	/* Driver for invalid strings test cases
	* 
	* Will open the file and write the errors to 
	* the specified output file. That output file
	* can then be diffed with an expected error output
	* file to ensure errors are logged correctly
	* 
	* testName: name of the test used for the input and output file names
	*/
	private static void runInvalidStringsTest(String testName) throws IOException{
		final PrintStream origErr = System.err;  // save original error stream
		PrintStream outFile = null;   // output file you want error messages to go to
		FileReader inFile = null;

		try {
			outFile = new PrintStream(testName + ".out");
		} catch (FileNotFoundException ex) {
			System.err.println(testName + ".out cannot be opened.");
			System.exit(-1);
		} 
		try {
			inFile = new FileReader(testName + ".in");
		} catch (IOException ex) {
			System.err.println(testName + ".in cannot be opened.");
			System.exit(-1);
		}

		System.setErr(outFile);  // set the error stream to the output file

		// create and call the scanner
		String tokenVal;
		Yylex scanner = new Yylex(inFile);
		Symbol token = scanner.next_token();
		while (token.sym != sym.EOF) { 
			if (token.sym != sym.STRINGLITERAL){
				System.err.println("Error: Non-string token returned");
			}
			if (CharNum.num != 0){
				System.err.println("Error: Character count not reset correctly");
			}
			token = scanner.next_token();
		}

		outFile.close();         // close output file
		System.setErr(origErr);  // set error stream back to original System.err
	}
	/* Driver for testing that integers over the maximum are
	*  handled appropriately
	* 
	* Opens a file of integers that are equal to or over the 
	* java maximum integer. Prints an error if they are not parsed
	* by the scanner as Integer.MAX_VALUE. Ignores all non-integer tokens.
	*
	* testName: the name used for the input file
	*/
	private static void runMaxIntegerTest(String testName) throws IOException{
		FileReader inFile = null;
		
		try {
			inFile = new FileReader(testName + ".in");
		} catch (FileNotFoundException ex) {
			System.err.println("File " + testName + ".in not found.");
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
			tokenVal = Integer.toString(((IntLitTokenVal)token.value).intVal);
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

			if ((token.sym == sym.INTLITERAL) && Integer.parseInt(tokenVal) != Integer.MAX_VALUE){
				System.err.println("Returned non-max integer: " + tokenVal);
			}

			token = scanner.next_token();
		}// end while
	}
}

