package ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SymbolTableBuilder implements Visitor {

	public HashMap<AstNode,SymbolTable> myVariables;
	
	private SymbolTable currentSymbolTableVar; 
	private SymbolTable ParentSymbolTableVar;
	
	private String refType=""; /* type of the variable */
	private String classType=""; /* current class */
	private boolean isField = false;
	
	public SymbolTableBuilder() {
		this.currentSymbolTableVar = null;
		this.ParentSymbolTableVar = null;

		this.myVariables=new HashMap<AstNode,SymbolTable>();
	}
	
	/* build the classes's symbol table hierarchy according to the inheritance */
	public void setSymbolTableClassHirerachy(Program program,HashMap<AstNode,SymbolTable> classAst,HashMap<String,SymbolTable> classNames) {
		
		for (ClassDecl classDecl:program.classDecls()) {
			String parentName=classDecl.superName();
			if(parentName!=null) {	
				classAst.get(classDecl).setParentSymbolTable(classNames.get(parentName));
			}
		}
		
		
	}
	
	
	/* here start the visitor */
	@Override
	public void visit(Program program) {
		
		HashMap<AstNode,SymbolTable> classAst=new HashMap<AstNode,SymbolTable>();
		HashMap<String,SymbolTable> classNames=new HashMap<String,SymbolTable>();
		
		if(program.mainClass()!=null) {
			program.mainClass().accept(this);
		}
		
		for (ClassDecl cls : program.classDecls()) {	
			
			SymbolTable currVar=new SymbolTable();
		
			this.currentSymbolTableVar=currVar;
			currVar.setParentSymbolTable(this.ParentSymbolTableVar);
			
			classAst.put(cls,this.currentSymbolTableVar); /*prepare hashmaps for setSymbolTableClassHirerachy() call*/
			classNames.put(cls.name(),this.currentSymbolTableVar);
			
			cls.accept(this);
		}
		
		setSymbolTableClassHirerachy(program,classAst,classNames);
	}
	
	@Override
	public void visit(ClassDecl classDecl) {
		
		this.classType=classDecl.name();
		this.isField = true;
		for (VarDecl vDecl: classDecl.fields()) {
		vDecl.accept(this);
		}
		this.isField = false;
		for (MethodDecl methodDecl: classDecl.methoddecls()) {
			methodDecl.accept(this);
		}	
	}

	@Override
	public void visit(MainClass mainClass) {
		this.classType=mainClass.name();
		SymbolTable currVar=new SymbolTable();
		
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);

		
		myVariables.put(mainClass,this.currentSymbolTableVar );
		this.currentSymbolTableVar.addEntery(mainClass.argsName(), new SymbolDetails("String-array"));
		
		if(mainClass.mainStatement()!=null) {
			mainClass.mainStatement().accept(this);
		}
		
	}

	@Override
	public void visit(MethodDecl methodDecl) {
		
		if(methodDecl.returnType()!=null) {
		methodDecl.returnType().accept(this);
		}
		
		SymbolTable tempVar=this.currentSymbolTableVar;
		SymbolTable currVar=new SymbolTable();
		
		this.ParentSymbolTableVar=this.currentSymbolTableVar;
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);
		
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
			methodDecl.ret().accept(this);
		}
		/* retrieve pointers for upper scope */
		this.currentSymbolTableVar=tempVar;
		this.ParentSymbolTableVar=tempVar.getParentSymbolTable();
		
	}

	@Override
	public void visit(FormalArg formalArg) {
		
		if(formalArg.type()!=null) {
		formalArg.type().accept(this);
		myVariables.put(formalArg, this.currentSymbolTableVar);
		this.currentSymbolTableVar.addEntery(formalArg.name(), new SymbolDetails(this.refType));
		}
	}

	@Override
	public void visit(VarDecl varDecl) {
		
		if(varDecl.type()!=null) {
		varDecl.type().accept(this);
		myVariables.put(varDecl, this.currentSymbolTableVar);
		this.currentSymbolTableVar.addEntery(varDecl.name(), new SymbolDetails(this.refType, this.isField));
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
		
		if(ifStatement.cond()!=null)
			ifStatement.cond().accept(this);
		if(ifStatement.thencase()!=null)
			ifStatement.thencase().accept(this);
		if(ifStatement.elsecase()!=null)
			ifStatement.elsecase().accept(this);
		
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		
		if(whileStatement.cond()!=null)
			whileStatement.cond().accept(this);
		if(whileStatement.body()!=null)
			whileStatement.body().accept(this);
		
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
		if(assignArrayStatement.index()!=null) {
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
	public void visit(ThisExpr e) {
		myVariables.put(e, currentSymbolTableVar);
		this.currentSymbolTableVar.addEntery("this", new SymbolDetails(this.classType));
	}

	@Override
	public void visit(NewIntArrayExpr e) {
		
		if(e.lengthExpr()!=null)
			e.lengthExpr().accept(this);
	}

	@Override
	public void visit(NewObjectExpr e) {	
		myVariables.put(e, this.currentSymbolTableVar);
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
		
		System.out.println("printing variables symbol tables:");
		for (Map.Entry<AstNode, SymbolTable> it: varsSet) {
			System.out.println("My name is: "+it.getKey()+" at line "+it.getKey().lineNumber+" and my symbol table is:\n");
			System.out.println(it.getValue());
		}
		
		return "";
	}

}
