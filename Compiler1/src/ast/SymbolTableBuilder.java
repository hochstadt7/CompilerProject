package ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SymbolTableBuilder implements Visitor {

	private Map<AstNode,SymbolTable> myMethods;
	private Map<AstNode,SymbolTable> myVariables;
	private Map<String,ArrayList<SymbolTable>> className;
	
	private SymbolTable currentSymbolTableVar; 
	private SymbolTable ParentSymbolTableVar;
	private SymbolTable currentSymbolTableMeth; 
	private SymbolTable ParentSymbolTableMeth;
	
	private String refType="";
	
	public SymbolTableBuilder() {
		this.currentSymbolTableVar = null;
		this.ParentSymbolTableVar = null;
		this.currentSymbolTableMeth = null;
		this.ParentSymbolTableMeth = null;
		
		this.myMethods=new HashMap<AstNode,SymbolTable>();
		this.myVariables=new HashMap<AstNode,SymbolTable>();
		this.className=new HashMap<String,ArrayList<SymbolTable>>();
	}
	
	/*build the classes's symbol table hirerarchy according to the inheritence*/
	public void setSymbolTableClassHirerachy(Program program) {
		for (ClassDecl classDecl:program.classDecls()) {
			String parentName=classDecl.superName();
			if(parentName!=null) {	
				myVariables.get(classDecl).setParentSymbolTable(className.get(parentName).get(0));
				myMethods.get(classDecl).setParentSymbolTable(className.get(parentName).get(1));
			}
		}
		
	}
	
	
	/*here start the visitor*/
	@Override
	public void visit(Program program) {
		
		if(program.mainClass()!=null) {
			program.mainClass().accept(this);
		}
		
		for (ClassDecl cls : program.classDecls()) {	
			cls.accept(this);
		}
		
		setSymbolTableClassHirerachy(program);
	}
	
	@Override
	public void visit(ClassDecl classDecl) {
		
		SymbolTable currVar=new SymbolTable();
		SymbolTable currMeth=new SymbolTable();
	
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);
		this.currentSymbolTableMeth=currMeth;
		currMeth.setParentSymbolTable(this.ParentSymbolTableMeth);
		className.put(classDecl.name(),new ArrayList<SymbolTable>(Arrays.asList(this.currentSymbolTableVar,this.currentSymbolTableMeth))) ;// set className mapping to symbol tables
		
		for (VarDecl vDecl: classDecl.fields()) {
		vDecl.accept(this);
		}
		
		for (MethodDecl methodDecl: classDecl.methoddecls()) {
			methodDecl.accept(this);
		}	
	}

	@Override
	public void visit(MainClass mainClass) {
		
		SymbolTable currVar=new SymbolTable();
		SymbolTable currMeth=new SymbolTable();
		
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);
		this.currentSymbolTableMeth=currMeth;
		currMeth.setParentSymbolTable(this.ParentSymbolTableMeth);
		
		myVariables.put(mainClass,this.currentSymbolTableVar );// is it ok??????
		this.currentSymbolTableVar.addEntery(mainClass.name(), new Symbol("String-array"));//????
		
		if(mainClass.mainStatement()!=null) {
			mainClass.mainStatement().accept(this);
		}
		
	}

	@Override
	public void visit(MethodDecl methodDecl) {
	
		if(methodDecl.returnType()!=null) {
		methodDecl.returnType().accept(this);
		myMethods.put(methodDecl,this.currentSymbolTableMeth);
		this.currentSymbolTableMeth.addEntery(methodDecl.name(), new Symbol(this.refType));
		}
		
		SymbolTable tempVar=this.currentSymbolTableVar;//to remember pointers when we go back to the upper scope
		SymbolTable tempMeth=this.currentSymbolTableMeth;//to remember pointers when we go back to the upper scope
		SymbolTable currVar=new SymbolTable();
		SymbolTable currMeth=new SymbolTable();
		
		this.ParentSymbolTableVar=this.currentSymbolTableVar;
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);
		this.ParentSymbolTableMeth=this.currentSymbolTableMeth;
		this.currentSymbolTableMeth=currMeth;
		currMeth.setParentSymbolTable(this.ParentSymbolTableMeth);
		
		for (VarDecl vDecl: methodDecl.vardecls()) {
			vDecl.accept(this);
		}
		for (FormalArg formalArg: methodDecl.formals()) {
			formalArg.accept(this);
		}
		for (Statement statement: methodDecl.body()) {
			statement.accept(this);
		}
		
		if(methodDecl.ret()!=null) {
			myVariables.put(methodDecl.ret(), this.currentSymbolTableVar);	
		}
		this.currentSymbolTableVar=tempVar;
		this.ParentSymbolTableVar=tempVar.getParentSymbolTable();
		this.currentSymbolTableMeth=tempMeth;
		this.ParentSymbolTableMeth=tempMeth.getParentSymbolTable();
		
	}

	@Override
	public void visit(FormalArg formalArg) {
		
		if(formalArg.type()!=null) {
		formalArg.type().accept(this);
		myVariables.put(formalArg, this.currentSymbolTableVar);
		this.currentSymbolTableVar.addEntery(formalArg.name(), new Symbol(this.refType));
		}
	}

	@Override
	public void visit(VarDecl varDecl) {
		
		if(varDecl.type()!=null) {
		varDecl.type().accept(this);
		myVariables.put(varDecl, this.currentSymbolTableVar);
		this.currentSymbolTableVar.addEntery(varDecl.name(), new Symbol(this.refType));
		}
	}

	@Override
	public void visit(BlockStatement blockStatement) {
		for(Statement statement:blockStatement.statements()) {
			statement.accept(this);
		}
	}

	@Override
	public void visit(IfStatement ifStatement) {
		
		SymbolTable tempVar=this.currentSymbolTableVar;
		SymbolTable tempMeth=this.currentSymbolTableMeth;
		SymbolTable currVar=new SymbolTable();
		SymbolTable currMeth=new SymbolTable();
		this.ParentSymbolTableVar=this.currentSymbolTableVar;
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);
		this.ParentSymbolTableMeth=this.currentSymbolTableMeth;
		this.currentSymbolTableMeth=currMeth;
		currMeth.setParentSymbolTable(this.ParentSymbolTableMeth);
		
		if(ifStatement.cond()!=null)
			ifStatement.cond().accept(this);
		if(ifStatement.thencase()!=null)
			ifStatement.thencase().accept(this);
		if(ifStatement.elsecase()!=null)
			ifStatement.elsecase().accept(this);
		this.currentSymbolTableVar=tempVar;
		this.ParentSymbolTableVar=tempVar.getParentSymbolTable();
		this.currentSymbolTableMeth=tempMeth;
		this.ParentSymbolTableMeth=tempMeth.getParentSymbolTable();
		
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		
		SymbolTable tempVar=this.currentSymbolTableVar;
		SymbolTable tempMeth=this.currentSymbolTableMeth;
		SymbolTable currVar=new SymbolTable();
		SymbolTable currMeth=new SymbolTable();
		
		this.ParentSymbolTableVar=this.currentSymbolTableVar;
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);
		this.ParentSymbolTableMeth=this.currentSymbolTableMeth;
		this.currentSymbolTableMeth=currMeth;
		currMeth.setParentSymbolTable(this.ParentSymbolTableMeth);
		
		//check this
		if(whileStatement.cond()!=null)
			whileStatement.cond().accept(this);
		if(whileStatement.body()!=null)
			whileStatement.body().accept(this);
		this.currentSymbolTableVar=tempVar;
		this.ParentSymbolTableVar=tempVar.getParentSymbolTable();
		this.currentSymbolTableMeth=tempMeth;
		this.ParentSymbolTableMeth=tempMeth.getParentSymbolTable();
		
	}

	@Override
	public void visit(SysoutStatement sysoutStatement) {
		if(sysoutStatement.arg()!=null) {
		sysoutStatement.arg().accept(this);
		}
	}

	@Override
	public void visit(AssignStatement assignStatement) {
		if(assignStatement.rv()!=null) {
		assignStatement.rv().accept(this);
		myVariables.put(assignStatement,this.currentSymbolTableVar);
		}
	}

	@Override
	public void visit(AssignArrayStatement assignArrayStatement) {
		
		myVariables.put(assignArrayStatement,this.currentSymbolTableVar);
		if(assignArrayStatement.rv()!=null) {
		assignArrayStatement.rv().accept(this);
		}
		if(assignArrayStatement.index()!=null) {//where do i insert index to the hashmap (cuz maybe i need). what is this object?
			assignArrayStatement.index().accept(this);
		}
	}

	@Override
	public void visit(AndExpr e) {
		
		if(e.e1()!=null)
			e.e1().accept(this);
		if(e.e2()!=null)
			e.e2().accept(this);
		
	}

	@Override
	public void visit(LtExpr e) {
		if(e.e1()!=null)
			e.e1().accept(this);
		if(e.e2()!=null)
			e.e2().accept(this);
		
	}

	@Override
	public void visit(AddExpr e) {

	if(e.e1()!=null)
		e.e1().accept(this);
	if(e.e2()!=null)
		e.e2().accept(this);
		
	}

	@Override
	public void visit(SubtractExpr e) {

		if(e.e1()!=null)
			e.e1().accept(this);
		if(e.e2()!=null)
			e.e2().accept(this);
		
	}

	@Override
	public void visit(MultExpr e) {

		if(e.e1()!=null)
			e.e1().accept(this);
		if(e.e2()!=null)
			e.e2().accept(this);
		
	}

	@Override
	public void visit(ArrayAccessExpr e) {

		if(e.arrayExpr()!=null)
			e.arrayExpr().accept(this);
		if(e.indexExpr()!=null)
			e.indexExpr().accept(this);
	}

	@Override
	public void visit(ArrayLengthExpr e) {
		
		if(e.arrayExpr()!=null)
			e.arrayExpr().accept(this);	
	}

	@Override
	public void visit(MethodCallExpr e) {

	if(e.ownerExpr()!=null)
		e.ownerExpr().accept(this);
	
	myMethods.put(e, this.currentSymbolTableMeth);
	this.currentSymbolTableMeth.addEntery(e.methodId(), new Symbol(this.refType));// i am not sure this is the real returned type of the this method. tom need to check it himself
	for(Expr expr: e.actuals()) {
		expr.accept(this);
	}
		
	}

	@Override
	public void visit(IntegerLiteralExpr e) {
		
		myVariables.put(e, currentSymbolTableVar);
	}

	@Override
	public void visit(TrueExpr e) {
		return;
		
	}

	@Override
	public void visit(FalseExpr e) {
		return;
	}

	@Override
	public void visit(IdentifierExpr e) {
		
		myVariables.put(e, currentSymbolTableVar);
	}

	@Override
	public void visit(ThisExpr e) {// this expression needs to be in the symbol table? if so, maybe i need global variable to save what is "this"
		myMethods.put(e, currentSymbolTableVar);
		//if needed i will add an entery for this. i doubt.
	}

	@Override
	public void visit(NewIntArrayExpr e) {
		
		if(e.lengthExpr()!=null)
			e.lengthExpr().accept(this);
	}

	@Override
	public void visit(NewObjectExpr e) {	
		myVariables.put(e, this.currentSymbolTableVar);
		// do i need to insert to symbol table?
	}

	@Override
	public void visit(NotExpr e) {
		if(e.e()!=null)
			e.e().accept(this);
	}

	@Override
	public void visit(IntAstType t) {
		this.refType="int";
	}

	@Override
	public void visit(BoolAstType t) {
		this.refType="boolean";
		
	}

	@Override
	public void visit(IntArrayAstType t) {
		this.refType="int-array";
		
	}

	@Override
	public void visit(RefType t) {
		this.refType=t.id();
		
	}
	
	@Override
	public String toString() {
		
		System.out.println("All the symbol tables:\n");
		Set<Entry<AstNode, SymbolTable>> varsSet=this.myVariables.entrySet();
		Set<Entry<AstNode, SymbolTable>> methodsSet=this.myMethods.entrySet();
		
		System.out.println("printing variables symbol tables:");
		for (Map.Entry<AstNode, SymbolTable> it: varsSet) {
			System.out.println("My name is: "+it.getKey()+" and my symbol table is:\n");
			System.out.println(it.getValue());
		}
		
		System.out.println("printing methods symbol tables:");
		for (Map.Entry<AstNode, SymbolTable> it: methodsSet) {
			System.out.println("My name is: "+it.getKey()+"and my symbol table is:\n");
			System.out.println(it.getValue());
		}
		
		return "";
	}

}
