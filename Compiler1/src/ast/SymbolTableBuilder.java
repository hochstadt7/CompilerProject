package ast;

import java.util.HashMap;
import java.util.Map;

public class SymbolTableBuilder implements Visitor {

	private Map<AstNode,SymbolTable> myMethods;
	private Map<AstNode,SymbolTable> myVariables;
	
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
	}
	
	/*build the classes's symbol table hirerarchy according to the inheritence*/
	public void setSymbolTableClassHirerachy(Program program) {//maybe this isn't the class for this code. not usefull code, O(n^3). this function will be done after all the build finish
		for (ClassDecl classDecl:program.classDecls()) {
			String father=classDecl.superName();
			if(father!=null) {
				for(ClassDecl iter:program.classDecls()) {
					if(father.equals(iter.name())) {
						myVariables.get(classDecl).setParentSymbolTable(myVariables.get(iter));
						myMethods.get(classDecl).setParentSymbolTable(myMethods.get(iter));
					}
				}
				
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
		
	}
	
	@Override
	public void visit(ClassDecl classDecl) {
		
		SymbolTable currVar=new SymbolTable();
		SymbolTable currMeth=new SymbolTable();
	
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);
		this.currentSymbolTableMeth=currMeth;
		currMeth.setParentSymbolTable(this.ParentSymbolTableMeth);
		
		for (VarDecl vDecl: classDecl.fields()) {
		vDecl.accept(this);
		}
		
		for (MethodDecl methodDecl: classDecl.methoddecls()) {
			methodDecl.accept(this);
		}	
	}

	@Override
	public void visit(MainClass mainClass) {
		
		SymbolTable tempVar=this.currentSymbolTableVar;
		SymbolTable tempMeth=this.currentSymbolTableVar;
		SymbolTable currVar=new SymbolTable();
		SymbolTable currMeth=new SymbolTable();
		
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);
		this.currentSymbolTableMeth=currMeth;
		currMeth.setParentSymbolTable(this.ParentSymbolTableMeth);
		
		if(mainClass.mainStatement()!=null) {
			mainClass.mainStatement().accept(this);
		}
		this.currentSymbolTableVar=tempVar;
		this.ParentSymbolTableVar=tempVar.getParentSymbolTable();
		this.currentSymbolTableMeth=tempMeth;
		this.ParentSymbolTableMeth=tempMeth.getParentSymbolTable();
	}

	@Override
	public void visit(MethodDecl methodDecl) {
	
		if(methodDecl.returnType()!=null) {
		methodDecl.returnType().accept(this);
		this.currentSymbolTableMeth.addEntery(methodDecl.name(), new Symbol(this.refType));
		myMethods.put(methodDecl,this.currentSymbolTableMeth);
		}
		
		SymbolTable tempVar=this.currentSymbolTableVar;//to remember pointers when we go back to the upper scope
		SymbolTable tempMeth=this.currentSymbolTableVar;//to remember pointers when we go back to the upper scope
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
		SymbolTable tempMeth=this.currentSymbolTableVar;
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
		SymbolTable tempMeth=this.currentSymbolTableVar;
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
		if(assignArrayStatement.rv()!=null) {
		assignArrayStatement.rv().accept(this);
		myVariables.put(assignArrayStatement,this.currentSymbolTableVar);
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
	this.currentSymbolTableVar.addEntery(e.methodId(), new Symbol(this.refType));
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
		//if needed i will add an entery for this
	}

	@Override
	public void visit(NewIntArrayExpr e) {
		
		if(e.lengthExpr()!=null)
			e.lengthExpr().accept(this);
		
	}

	@Override
	public void visit(NewObjectExpr e) {	// nothing here?
		return;
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

}
