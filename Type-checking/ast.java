import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a brevis program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and identifiers contain line and character 
// number information; for string literals and identifiers, they also 
// contain a string; for integer literals, they also contain an integer 
// value.
//
// Here are all the different kinds of AST nodes and what kinds of 
// children they have.  All of these kinds of AST nodes are subclasses
// of "ASTnode".  Indentation indicates further subclassing:
//
//     Subclass              Children
//     --------              --------
//     ProgramNode           DeclListNode
//     DeclListNode          linked list of DeclNode
//     DeclNode:
//       VarDeclNode         TypeNode, IdNode, int
//       FnDeclNode          TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode      TypeNode, IdNode
//       RecordDeclNode      IdNode, DeclListNode
//
//     StmtListNode          linked list of StmtNode
//     ExpListNode           linked list of ExpNode
//     FormalsListNode       linked list of FormalDeclNode
//     FnBodyNode            DeclListNode, StmtListNode
//
//     TypeNode:
//       BoolNode            --- none ---
//       IntNode             --- none ---
//       VoidNode            --- none ---
//       RecordNode          IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignExpNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       TrueNode            --- none ---
//       FalseNode           --- none ---
//       IdNode              --- none ---
//       IntLitNode          --- none ---
//       StrLitNode          --- none ---
//       DotAccessNode       ExpNode, IdNode
//       AssignExpNode       ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         LessEqNode
//         GreaterNode
//         GreaterEqNode
//         AndNode
//         OrNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, 
// or internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
//        BoolNode,  IntNode,     VoidNode,   TrueNode,  FalseNode,
//        IdNode,    IntLitNode,  StrLitNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, StmtListNode, ExpListNode, FormalsListNode
//
// (3) Internal nodes with fixed numbers of children:
//        ProgramNode,     VarDeclNode,     FnDeclNode,    FormalDeclNode,
//        RecordDeclNode,  FnBodyNode,      RecordNode,    AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, IfStmtNode,    IfElseStmtNode,
//        WhileStmtNode,   ReadStmtNode,    WriteStmtNode, CallStmtNode,
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode, CallExpNode,
//        UnaryExpNode,    UnaryMinusNode,  NotNode,       BinaryExpNode,   
//        PlusNode,        MinusNode,       TimesNode,     DivideNode,
//        EqualsNode,      NotEqualsNode,   LessNode,      LessEqNode,
//        GreaterNode,     GreaterEqNode,   AndNode,       OrNode
//
// **********************************************************************

// **********************************************************************
//   ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }
}

// **********************************************************************
//   ProgramNode,  DeclListNode, StmtListNode, ExpListNode, 
//   FormalsListNode, FnBodyNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /***
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, record defintions, and functions in the program.
     ***/
    public void nameAnalysis() {
        SymTab symTab = new SymTab();
        myDeclList.nameAnalysis(symTab);
    }
    
    public Type typeCheck() {
    	Type t = myDeclList.typeCheck();
    	return(t);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // one child
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     ***/
    public void nameAnalysis(SymTab symTab) {
        nameAnalysis(symTab, symTab);
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing record names in variable decls), process all of the 
     * decls in the list.
     ***/    
    public void nameAnalysis(SymTab symTab, SymTab globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode)node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }
    
    public Type typeCheck() {
	Type t = null;
	Type r = null;
    	for (DeclNode node : myDecls) {
	  t = node.typeCheck();
	  if (t != null){
	 	 if (t.isErrorType()) {
	  		r = t;
		 }
	  }
	}
	if (r != null) return(r);
	return(t);
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of children (DeclNodes)
    private List<DeclNode> myDecls;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     ***/
    public void nameAnalysis(SymTab symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    } 

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }      
    }

    public Type typeCheck() {
	Type t = null;
	Type r = null;
    	for (StmtNode node : myStmts) {
		t = node.typeCheck();
		if (t.isErrorType()) {
                	r = t;
          	}
	}
	if (r != null) return(r);
	return(t);
    }

    public Type typeCheck(Type fnType) {
    	Type t = null;
	for (StmtNode node : myStmts) {
		if (node instanceof ReturnStmtNode) {
			t = ((ReturnStmtNode)node).typeCheck(fnType);
		}
		else {  
			t = node.typeCheck();
		}
	}
	return(t);
    }

    // list of children (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     ***/
    public void nameAnalysis(SymTab symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }

    public Type typeCheck(List<Type> paramTypes) {
	int i = 0;
	Type r = null;
	Type t = null;
    	for (ExpNode node : myExps) {
		t = node.typeCheck();
		if (t.isErrorType()) {
			r = t;
		}
		else if (!t.equals(paramTypes.get(i))) {
			ErrMsg.fatal(node.lineNum(), node.charNum(),
			   "Type of actual does not match type of formal");
			r = new ErrorType();		
		}
		i++;
	}
	if (r != null) return(r);
	return(t);
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    public int getNum() {
    	return(myExps.size());
    }

    // list of children (ExpNodes)
    private List<ExpNode> myExps;
}
class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     ***/
    public List<Type> nameAnalysis(SymTab symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : myFormals) {
            Sym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }    
    
    //need to edit
    public Type typeCheck() {
        Type t = null;
	Type r = null;
	for (FormalDeclNode node : myFormals) {
		t = node.typeCheck();
		if (t.isErrorType()) {
                	r = t;
         	}
	}
	if (r != null) return(r);
	return(t);
    }

    /***
     * Return the number of formals in this list.
     ***/
    public int length() {
        return myFormals.size();
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of children (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }
    
    public Type typeCheck(Type fnType) {
    	return(myStmtList.typeCheck(fnType));
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     ***/
    public void nameAnalysis(SymTab symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }

    public Type typeCheck() {
    	Type t;
	t = myStmtList.typeCheck();
	return(t);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // two children
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}


// **********************************************************************
// ****  DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /***
     * Note: a formal decl needs to return a sym
     ***/
    abstract public Sym nameAnalysis(SymTab symTab);
    abstract public Type typeCheck();
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public Type typeCheck() {
    	return(myType.type());
    }

    /***
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a record type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for record field decls)
     * globalTab is global symbol table (for record type names)
     * symTab and globalTab can be the same
     ***/
    public Sym nameAnalysis(SymTab symTab) {
        return nameAnalysis(symTab, symTab);
    }
    
    public Sym nameAnalysis(SymTab symTab, SymTab globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        Sym sym = null;
        IdNode recordId = null;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        else if (myType instanceof RecordNode) {
            recordId = ((RecordNode)myType).idNode();
			try {
				sym = globalTab.lookupGlobal(recordId.name());
            
				// if the name for the record type is not found, 
				// or is not a record type
				if (sym == null || !(sym instanceof RecordDefSym)) {
					ErrMsg.fatal(recordId.lineNum(), recordId.charNum(), 
								"Name of record type invalid");
					badDecl = true;
				}
				else {
					recordId.link(sym);
				}
			} catch (SymTabEmptyException ex) {
				System.err.println("Unexpected SymTabEmptyException " +
								    " in VarDeclNode.nameAnalysis");
				System.exit(-1);
			} 
        }
        
		try {
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
							"Identifier multiply-declared");
				badDecl = true;            
			}
		} catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in VarDeclNode.nameAnalysis");
            System.exit(-1);
        } 
        
        if (!badDecl) {  // insert into symbol table
            try {
                if (myType instanceof RecordNode) {
                    sym = new RecordSym(recordId);
                }
                else {
                    sym = new Sym(myType.type());
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (SymDuplicationException ex) {
                System.err.println("Unexpected SymDuplicationException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (SymTabEmptyException ex) {
                System.err.println("Unexpected SymTabEmptyException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    } 

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.println(";");
    }

    // three children
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NON_RECORD if this is not a record type

    public static int NON_RECORD = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public Type typeCheck() {
    	Type t = myBody.typeCheck(myType.type());
    	return(t);
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     ***/
    public Sym nameAnalysis(SymTab symTab) {
        String name = myId.name();
        FnSym sym = null;
        try {
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(),
							"Identifier multiply-declared");
			}
        
			else { // add function name to local symbol table
				try {
					sym = new FnSym(myType.type(), myFormalsList.length());
					symTab.addDecl(name, sym);
					myId.link(sym);
				} catch (SymDuplicationException ex) {
					System.err.println("Unexpected SymDuplicationException " +
									" in FnDeclNode.nameAnalysis");
					System.exit(-1);
				} catch (SymTabEmptyException ex) {
					System.err.println("Unexpected SymTabEmptyException " +
									" in FnDeclNode.nameAnalysis");
					System.exit(-1);
				}
			}
		} catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        } 
        
        symTab.addScope();  // add a new scope for locals and params
        
        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }
        
        myBody.nameAnalysis(symTab); // process the function body
        
        try {
            symTab.removeScope();  // exit scope
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }
        
        return null;
    } 

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}");
    }

    // 4 children
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public Type typeCheck() {
	//TODO remove
    	System.out.println("Formal");
	return(null);
    }
  
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     ***/
    public Sym nameAnalysis(SymTab symTab) {
        String name = myId.name();
        boolean badDecl = false;
        Sym sym = null;
        
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        try { 
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
							"Identifier multiply-declared");
				badDecl = true;
			}
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in FormalDeclNode.nameAnalysis");
            System.exit(-1);
        } 
        
        if (!badDecl) {  // insert into symbol table
            try {
                sym = new Sym(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (SymDuplicationException ex) {
                System.err.println("Unexpected SymDuplicationException " +
                                   " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (SymTabEmptyException ex) {
                System.err.println("Unexpected SymTabEmptyException " +
                                   " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
    }

    // two children
    private TypeNode myType;
    private IdNode myId;
}

class RecordDeclNode extends DeclNode {
    public RecordDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public Type typeCheck() {
    	return(myId.typeCheck());
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this record definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this record
     ***/
    public Sym nameAnalysis(SymTab symTab) {
        String name = myId.name();
        boolean badDecl = false;
        try {
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
							"Identifier multiply-declared");
				badDecl = true;            
			}
		} catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in RecordDeclNode.nameAnalysis");
            System.exit(-1);
        } 

        SymTab recordSymTab = new SymTab();
        
        // process the fields of the record
        myDeclList.nameAnalysis(recordSymTab, symTab);
        
        if (!badDecl) {
            try {   // add entry to symbol table
                RecordDefSym sym = new RecordDefSym(recordSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (SymDuplicationException ex) {
                System.err.println("Unexpected SymDuplicationException " +
                                   " in RecordDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (SymTabEmptyException ex) {
                System.err.println("Unexpected SymTabEmptyException " +
                                   " in RecordDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return null;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("record ");
        p.print(myId.name());
        p.println("(");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println(");\n");
    }

    // two children
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// ****  TypeNode and its subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /***
     * type
     ***/
    public Type type() {
        return new BoolType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("boolean");
    }
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /***
     * type
     ***/
    public Type type() {
        return new IntType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("integer");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }
    
    /***
     * type
     ***/
    public Type type() {
        return new VoidType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class RecordNode extends TypeNode {
    public RecordNode(IdNode id) {
        myId = id;
    }
 
    public IdNode idNode() {
        return myId;
    }
       
    /***
     * type
     ***/
    public Type type() {
        return new RecordType(myId);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("record ");
        p.print(myId.name());
    }
    
    // one child
    private IdNode myId;
}

// **********************************************************************
// ****  StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTab symTab);
    public abstract Type typeCheck(); 
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignExpNode assign) {
        myAssign = assign;
    }

    public Type typeCheck() {
    	return(myAssign.typeCheck());
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTab symTab) {
        myAssign.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // one child
    private AssignExpNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public Type typeCheck() {
    	Type t = myExp.typeCheck();

	if (t.isErrorType()){
		return(new ErrorType());
	}

	if (!t.isIntType()){
		ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
			"Arithmetic operator applied to non-numeric operand");
		return(new ErrorType());
	}
	return(new IntType());
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // one child
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    public Type typeCheck() {
	Type t = myExp.typeCheck();

        if (t.isErrorType()){
                return(new ErrorType());
        }

        if (!t.isIntType()){
                ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
                        "Arithmetic operator applied to non-numeric operand");
                return(new ErrorType());
        }
        return(t);
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    // one child
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }
    
    public Type typeCheck() {
	Type expType = myExp.typeCheck();
	Type stmtType = myStmtList.typeCheck();

	if (expType.isErrorType()){
		return (new ErrorType());
	}
	
	if (!expType.isBoolType()){
		ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
                                        "Non-boolean expression used as if condition");
	}

        return(stmtType);
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     ***/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");        
    }

    // three children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }
    
    public Type typeCheck() {
        Type expType = myExp.typeCheck();
        Type thenStmtType = myThenStmtList.typeCheck();
	Type elseStmtType = myElseStmtList.typeCheck();

        if (expType.isErrorType()){ 
                return (new ErrorType());
        }

        if (!expType.isBoolType()){
                ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
                                        "Non-boolean expression used as if condition");
        }

        return(thenStmtType);
    }

    
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     ***/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}"); 
    }

    // five children
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    public Type typeCheck() {
        Type expType = myExp.typeCheck();
        Type smtType = myStmtList.typeCheck();

        if (expType.isErrorType()){
                return (new ErrorType());
        }

        if (!expType.isBoolType()){
                ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
                                        "Non-boolean expression used as while condition");
        }

        return(smtType);
    }
 
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     ***/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // three children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    public Type typeCheck() {
    	Type t = myExp.typeCheck();
    	if (t.isRecordType()) {
		ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
					"Attempt to read a record variable");
		return(new ErrorType());
	}
	else if (t.isRecordDefType()) {
                ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
                                        "Attempt to read a record name");
		return(new ErrorType());	
	}
	else if (t.isFnType()) {
                        ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
                                        "Attempt to read a function");
                return(new ErrorType());	
	}
	return(t);

    }


    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("scan -> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // one child (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    public Type typeCheck() {
        Type t = myExp.typeCheck();
        if (t.isRecordType()) {
               ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
                                        "Attempt to write to a record variable");
                return(new ErrorType());
        }
        else if (t.isRecordDefType()) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                                        "Attempt to write to a record name");
                return(new ErrorType());
        }
        else if (t.isFnType()) {
                if (myExp instanceof IdNode) {
                        ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
                                        "Attempt to write to a function");
                	return(new ErrorType());
		}
		else if (myExp instanceof CallExpNode) {
			if(((CallExpNode)myExp).checkVoid()) {
                		return(new ErrorType());	
			}
		}
		else {
			System.out.println("Weirdness in WriteStmt");
		}
        }
        return(t);

    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("print <- ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // one child
    private ExpNode myExp;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }
    
    public Type typeCheck() {
        return(myCall.typeCheck());
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTab symTab) {
        myCall.nameAnalysis(symTab);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // one child
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public Type typeCheck(Type fnType) {
    	if (myExp == null) {
		if (!fnType.isVoidType()) {
			ErrMsg.fatal(0,0,"Missing return value");
			return(new ErrorType());
		}
		return(fnType);
	}
	Type t = myExp.typeCheck();
	if (fnType.isVoidType()) {
		ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
				"Return with a value in a void function");
		return(new ErrorType());
	}
	if (t.isErrorType()) {
		return(t);
	}
	if (!t.equals(fnType)) {
		ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
				"Bad return value");
		return(new ErrorType());
	}
	return(fnType);
    }

    public Type typeCheck(){
	    return(new VoidType());
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     ***/
    public void nameAnalysis(SymTab symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // one child
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ****  ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /***
     * Default version for nodes with no names
     ***/
    abstract public int lineNum();
    abstract public int charNum();
    public void nameAnalysis(SymTab symTab) { }
    public abstract Type typeCheck();
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public int lineNum() {
    	return(myLineNum);
    }

    public int charNum() {
    	return(myCharNum);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }
    
    public Type typeCheck() {
    	return(new BoolType());
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    public int lineNum() {
        return(myLineNum);
    }
    
    public int charNum() {
        return(myCharNum);
    }
    public Type typeCheck() {
    	return(new BoolType());
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /***
     * Link the given symbol to this ID.
     ***/
    public void link(Sym sym) {
        mySym = sym;
    }
    
    /***
     * Return the name of this ID.
     ***/
    public String name() {
        return myStrVal;
    }
    
    /***
     * Return the symbol associated with this ID.
     ***/
    public Sym sym() {
        return mySym;
    }
    
    /***
     * Return the line number for this ID.
     ***/
    public int lineNum() {
        return myLineNum;
    }
    
    /***
     * Return the char number for this ID.
     ***/
    public int charNum() {
        return myCharNum;
    }    
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     ***/
    public void nameAnalysis(SymTab symTab) {
		try {
            Sym sym = symTab.lookupGlobal(myStrVal);
            if (sym == null) {
                ErrMsg.fatal(myLineNum, myCharNum, "Identifier undeclared");
            } else {
                link(sym);
            }
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IdNode.nameAnalysis");
            System.exit(-1);
        } 
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("[" + mySym + "]");
        }
    }
    
    public Type typeCheck() {
	return(mySym.getType());
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym;
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }
    
    public Type typeCheck() {
    	return(new IntType());
    }

    public int lineNum() {
        return(myLineNum);
    }

    public int charNum() {
        return(myCharNum);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }
    
    public Type typeCheck() {
    	return(new StringType());
    }

    public int lineNum() {
        return(myLineNum);
    }

    public int charNum() {
        return(myCharNum);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;    
        myId = id;
    }
    
    public Type typeCheck() {
    	return(myId.typeCheck());
    }

     /***
     * Return the symbol associated with this dot-access node.
     ***/
    public Sym sym() {
        return mySym;
    }    
    
    /***
     * Return the line number for this dot-access node. 
     * The line number is the one corresponding to the RHS of the dot-access.
     ***/
    public int lineNum() {
        return myId.lineNum();
    }
    
    /***
     * Return the char number for this dot-access node.
     * The char number is the one corresponding to the RHS of the dot-access.
     ***/
    public int charNum() {
        return myId.charNum();
    }
    
    /***
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the dot-access
     * - process the RHS of the dot-access
     * - if the RHS is of a record type, set the sym for this node so that
     *   a dot-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate record definition
     ***/
    public void nameAnalysis(SymTab symTab) {
        badAccess = false;
        SymTab recordSymTab = null; // to lookup RHS of dot-access
        Sym sym = null;
        
        myLoc.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode)myLoc;
            sym = id.sym();
            
            // check ID has been declared to be of a record type
            
            if (sym == null) { // ID was undeclared
                badAccess = true;
            }
            else if (sym instanceof RecordSym) { 
                // get symbol table for record type
                Sym tempSym = ((RecordSym)sym).getRecordType().sym();
                recordSymTab = ((RecordDefSym)tempSym).getSymTab();
            } 
            else {  // LHS is not a record type
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Dot-access of non-record type");
                badAccess = true;
            }
        }
        
        // if myLoc is really a dot-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a record type, or
        // a link to the Sym for the record type RHSid was declared to be
        else if (myLoc instanceof DotAccessExpNode) {
            DotAccessExpNode loc = (DotAccessExpNode)myLoc;
            
            if (loc.badAccess) {  // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this dot-access
            }
            else { //  no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) {  // no record in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), 
                                 "Dot-access of non-record type");
                    badAccess = true;
                }
                else {  // get the record's symbol table in which to lookup RHS
                    if (sym instanceof RecordDefSym) {
                        recordSymTab = ((RecordDefSym)sym).getSymTab();
                    }
                    else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of dot-access in the record's symbol table
        if (!badAccess) {
			try {
				sym = recordSymTab.lookupGlobal(myId.name()); // lookup
				if (sym == null) { // not found - RHS is not a valid field name
					ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
								"Record field name invalid");
					badAccess = true;
				}
            
				else {
					myId.link(sym);  // link the symbol
					// if RHS is itself as record type, link the symbol for its record 
					// type to this dot-access node (to allow chained dot-access)
					if (sym instanceof RecordSym) {
						mySym = ((RecordSym)sym).getRecordType().sym();
					}
				}
			} catch (SymTabEmptyException ex) {
				System.err.println("Unexpected SymTabEmptyException " +
								" in DotAccessExpNode.nameAnalysis");
				System.exit(-1);
			} 
        }
    }    
    
    // **** unparse ****
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print(").");
        myId.unparse(p, 0);
    }

    // two children
    private ExpNode myLoc;    
    private IdNode myId;
    private Sym mySym;          // link to Sym for record type
    private boolean badAccess;  // to prevent multiple, cascading errors
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }
    
    public Type typeCheck() {
    	Type t = myLhs.typeCheck();
	Type rhsType = myExp.typeCheck();

	if (rhsType.isErrorType() || t.isErrorType()){
		return (new ErrorType());
	}
	
	if (rhsType.isFnType()){
		CallExpNode fnCall = (CallExpNode) myExp;
		rhsType = fnCall.getReturnType();
	}

	if (!t.equals(rhsType)){
		ErrMsg.fatal(myLhs.lineNum(),myLhs.charNum(),
					"Type mismatch");
		return(new ErrorType()); 
	}

	if (t.isRecordDefType()){
		ErrMsg.fatal(myLhs.lineNum(),myLhs.charNum(),
					"Record name assignment");
		return(new ErrorType());
	}

	if (t.isRecordType()){
		ErrMsg.fatal(myLhs.lineNum(),myLhs.charNum(),
					"Record variable assignment");
		return(new ErrorType());
	}

	if (t.isFnType()){
		ErrMsg.fatal(myLhs.lineNum(),myLhs.charNum(),
					"Function assignment");
		return(new ErrorType());
	}
			
	return(rhsType);
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     ***/
    public void nameAnalysis(SymTab symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }
    
    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");       
    }
     
    public int lineNum() {
        return(myLhs.lineNum());
    }
    
    public int charNum() {
        return(myLhs.charNum());
    }

    // two children
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    public boolean checkVoid() {
    	FnSym S = (FnSym)myId.sym();
	if (S == null) {
		return(true);
	}
	if (S.getReturnType().isVoidType()) {
		ErrMsg.fatal(myId.lineNum(), myId.charNum(),
				"Attempt to write to void");
		return(true);
	}
	return(false);
    }

    public Type typeCheck() {
    	Type t = myId.typeCheck();
	if (t.isFnType() == false) {
		ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
				"Attempt to call a non-function");
		return(new ErrorType());
	}
	FnSym S = (FnSym)myId.sym();
	if (S == null) {
	}
	if (S.getNumParams() != myExpList.getNum()) {
		ErrMsg.fatal(myId.lineNum(), myId.charNum(),
				"Function call with wrong number of args");
		return(new ErrorType());
	}
        Type t2 = myExpList.typeCheck(S.getParamTypes());
	if (t2 != null && t2.isErrorType()) return(t2);
	return(t);
    }
    
    public Type getReturnType(){
	FnSym S = (FnSym)myId.sym();
	return (S.getReturnType());
    }

    public int lineNum() {
        return(myId.lineNum());
    }
    
    public int charNum() {
        return(myId.charNum());
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     ***/
    public void nameAnalysis(SymTab symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");       
    }

    // two children
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ***/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    public int lineNum() {
        return(myExp.lineNum());
    }
    
    public int charNum() {
        return(myExp.charNum());
    }

    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }
   
    /***
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     ***/
    public void nameAnalysis(SymTab symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }
    
    public int lineNum() {
        return(myExp1.lineNum());
    }

    public int charNum() {
        return(myExp1.charNum());
    }

    // two children
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// *****  Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
	Type t = myExp.typeCheck();
	if (t.isErrorType()){
		return(new ErrorType());
	}

        if (t.isFnType()){
               if (myExp instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp;
                        t = fnCall.getReturnType();
               }

        }

	if (!t.isIntType()){
		ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
					"Arithmetic operator applied to non-numeric operand");
		return(new ErrorType());
	}
	return(t);
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(\\");
        myExp.unparse(p, 0);
        p.print(")");
    }
   
    public Type typeCheck(){
        Type t = myExp.typeCheck();
	
	if (t.isErrorType()){
		return(new ErrorType());
	}
       
	if (t.isFnType()){
               if (myExp instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp;
                        t = fnCall.getReturnType();
               }

        }

	if (!t.isBoolType()){
                ErrMsg.fatal(myExp.lineNum(),myExp.charNum(),
                                        "Logical operator applied to non-boolean operand");
                return(new ErrorType());
        }
        return(t);
    }

}

// **********************************************************************
// ****  Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    public Type typeCheck(){
        Type t = new IntType();
        Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        exp2Type = fnCall.getReturnType();
               }

        }
	
        if(exp1Type.isErrorType()){
                t = new ErrorType();
        }

        else if(!exp1Type.isIntType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Arithmetic operator applied to non-numeric operand");
                t = new ErrorType();
        }

        if(exp2Type.isErrorType()){
                t = new ErrorType();

        }
        else if(!exp2Type.isIntType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Arithmetic operator applied to non-numeric operand");
                t = new ErrorType();
        }

        return(t);

    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
        Type t = new IntType();
        Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        exp2Type = fnCall.getReturnType();
               }

        }
	
        if(exp1Type.isErrorType()){
                t = new ErrorType();
        }

        else if(!exp1Type.isIntType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Arithmetic operator applied to non-numeric operand");
                t = new ErrorType();
        }

        if(exp2Type.isErrorType()){
                t = new ErrorType();

        }
        else if(!exp2Type.isIntType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Arithmetic operator applied to non-numeric operand");
                t = new ErrorType();
        }

        return(t);

    }

}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
        Type t = new IntType();
        Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        exp2Type = fnCall.getReturnType();
               }

        }
	
        if(exp1Type.isErrorType()){
                t = new ErrorType();
        }

        else if(!exp1Type.isIntType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Arithmetic operator applied to non-numeric operand");
                t = new ErrorType();
        }

        if(exp2Type.isErrorType()){
                t = new ErrorType();

        }
        else if(!exp2Type.isIntType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Arithmetic operator applied to non-numeric operand");
                t = new ErrorType();
        }

        return(t);

    }

}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public Type typeCheck(){
        Type t = new IntType();
        Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        exp2Type = fnCall.getReturnType();
               }

        }
	
        if(exp1Type.isErrorType()){
                t = new ErrorType();
        }

        else if(!exp1Type.isIntType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Arithmetic operator applied to non-numeric operand");
                t = new ErrorType();
        }

        if(exp2Type.isErrorType()){
                t = new ErrorType();

        }
        else if(!exp2Type.isIntType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Arithmetic operator applied to non-numeric operand");
                t = new ErrorType();
        }

        return(t);

    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
	Type type1 = myExp1.typeCheck();
	Type type2 = myExp2.typeCheck();
	Type type1Main = type1;
	    
	if(type1.isErrorType() | 
			type2.isErrorType()){
		return(new ErrorType());
	}
	    
	//Set Exp1 to the function return type if its a function
        if (type1.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        type1 = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (type2.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        type2 = fnCall.getReturnType();
               }

        }

        if(!type1.equals(type2)){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Type mismatch");
                return(new ErrorType());
        }
	
	if (type1Main.isFnType()){
        	if (myExp1 instanceof IdNode) {
                	ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                		"Equality operator applied to function names");
            	    return(new ErrorType());
        	}	
        
		else if (myExp1 instanceof CallExpNode) {
			Type t = ((CallExpNode)myExp1).getReturnType();
        		if(t.isVoidType()) {
                		ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                               	        "Equality operator applied to void function calls");
				return(new ErrorType());
                	}
        	}
        	else {
        		System.out.println("Weirdness in EqualsNode");
        	}
	}
	if(type1Main.isRecordDefType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Equality operator applied to record names");
                return(new ErrorType());
	}
	if(type1Main.isRecordType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Equality operator applied to record variables");
                return(new ErrorType());
	}
        return(new BoolType());

    }

}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" \\= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
    
    public Type typeCheck(){
  	Type type1 = myExp1.typeCheck();
	Type type2 = myExp2.typeCheck();
	Type type1Main = type1;
	    
	if(type1.isErrorType() | 
			type2.isErrorType()){
		return(new ErrorType());
	}
	    
	//Set Exp1 to the function return type if its a function
        if (type1.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        type1 = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (type2.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        type2 = fnCall.getReturnType();
               }

        }

        if(!type1.equals(type2)){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Type mismatch");
                return(new ErrorType());
        }
	
	if (type1Main.isFnType()){
        	if (myExp1 instanceof IdNode) {
                	ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                		"Equality operator applied to function names");
            	    return(new ErrorType());
        	}	
        
		else if (myExp1 instanceof CallExpNode) {
			Type t = ((CallExpNode)myExp1).getReturnType();
        		if(t.isVoidType()) {
                		ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                               	        "Equality operator applied to void function calls");
				return(new ErrorType());
                	}
        	}
        	else {
        		System.out.println("Weirdness in EqualsNode");
        	}
	}
	if(type1Main.isRecordDefType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Equality operator applied to record names");
                return(new ErrorType());
	}
	if(type1Main.isRecordType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Equality operator applied to record variables");
                return(new ErrorType());
	}
        return(new BoolType());

    }

}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
	Type t = new BoolType();
	Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        exp2Type = fnCall.getReturnType();
               }

        }
	
	//Exp1 check
	if(exp1Type.isErrorType()){  
		t = new ErrorType();
	}

	else if(!exp1Type.isIntType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Relational operator applied to non-numeric operand");
                t = new ErrorType();
        }
	
	//Exp2 check
	if(exp2Type.isErrorType()){ 
		t = new ErrorType();
	}

	else if(!exp2Type.isIntType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Relational operator applied to non-numeric operand");
                t = new ErrorType();
        }

        return(t);

    }

}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
 	Type t = new BoolType();
	Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        exp2Type = fnCall.getReturnType();
               }

        }
	
	//Exp1 check
	if(exp1Type.isErrorType()){  
		t = new ErrorType();
	}

	else if(!exp1Type.isIntType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Relational operator applied to non-numeric operand");
                t = new ErrorType();
        }
	
	//Exp2 check
	if(exp2Type.isErrorType()){ 
		t = new ErrorType();
	}

	else if(!exp2Type.isIntType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Relational operator applied to non-numeric operand");
                t = new ErrorType();
        }

        return(t);

    }

}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
 	Type t = new BoolType();
	Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        exp2Type = fnCall.getReturnType();
               }

        }
	
	//Exp1 check
	if(exp1Type.isErrorType()){  
		t = new ErrorType();
	}

	else if(!exp1Type.isIntType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Relational operator applied to non-numeric operand");
                t = new ErrorType();
        }
	
	//Exp2 check
	if(exp2Type.isErrorType()){ 
		t = new ErrorType();
	}

	else if(!exp2Type.isIntType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Relational operator applied to non-numeric operand");
                t = new ErrorType();
        }

        return(t);

    }

}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
	Type t = new BoolType();
	Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        exp2Type = fnCall.getReturnType();
               }

        }
	
	//Exp1 check
	if(exp1Type.isErrorType()){  
		t = new ErrorType();
	}

	else if(!exp1Type.isIntType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Relational operator applied to non-numeric operand");
                t = new ErrorType();
        }
	
	//Exp2 check
	if(exp2Type.isErrorType()){ 
		t = new ErrorType();
	}

	else if(!exp2Type.isIntType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Relational operator applied to non-numeric operand");
                t = new ErrorType();
        }

        return(t);

    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
	Type t = new BoolType();
	Type exp1Type = myExp1.typeCheck();
	Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }
        
	//Set Exp2 to the function return type if its a function	
	if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
		        CallExpNode fnCall = (CallExpNode) myExp2;
			exp2Type = fnCall.getReturnType();
	       }

	}

	//Exp1
	if(exp1Type.isErrorType()){  
		t = new ErrorType();
	}
	else if(!exp1Type.isBoolType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Logical operator applied to non-boolean operand");
                t = new ErrorType();
        }

	//Exp2
	if(exp2Type.isErrorType()){  
		t = new ErrorType();
	}
	else if(!exp2Type.isBoolType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Logical operator applied to non-boolean operand");
                t = new ErrorType();
        }

        return(t);

    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public Type typeCheck(){
        Type t = new BoolType();
        Type exp1Type = myExp1.typeCheck();
        Type exp2Type = myExp2.typeCheck();
	    
	//Set Exp1 to the function return type if its a function
        if (exp1Type.isFnType()){
               if (myExp1 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp1;
                        exp1Type = fnCall.getReturnType();
               }

        }

        //Set Exp2 to the function return type if its a function
        if (exp2Type.isFnType()){
               if (myExp2 instanceof CallExpNode) {
                        CallExpNode fnCall = (CallExpNode) myExp2;
                        exp2Type = fnCall.getReturnType();
               }

        }

        //Exp1
        if(exp1Type.isErrorType()){
                t = new ErrorType();
        }
        else if(!exp1Type.isBoolType()){
                ErrMsg.fatal(myExp1.lineNum(),myExp1.charNum(),
                                        "Logical operator applied to non-boolean operand");
                t = new ErrorType();
        }

        //Exp2
        if(exp2Type.isErrorType()){
                t = new ErrorType();
        }
        else if(!exp2Type.isBoolType()){
                ErrMsg.fatal(myExp2.lineNum(),myExp2.charNum(),
                                        "Logical operator applied to non-boolean operand");
                t = new ErrorType();
        }

        return(t);

    }

}
