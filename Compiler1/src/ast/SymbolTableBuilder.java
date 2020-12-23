package ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SymbolTableBuilder implements Visitor {

	public HashMap<AstNode,SymbolTable> myMethods;
	public HashMap<AstNode,SymbolTable> myVariables;
	
	private SymbolTable currentSymbolTableVar; 
	private SymbolTable ParentSymbolTableVar;
	private SymbolTable currentSymbolTableMeth; 
	private SymbolTable ParentSymbolTableMeth;
	
	private String refType=""; /* type of the variable */
	private String classType=""; /* current class */
	private boolean isField=false;
	
	public SymbolTableBuilder() {
		this.currentSymbolTableVar = null;
		this.ParentSymbolTableVar = null;
		this.currentSymbolTableMeth = null;
		this.ParentSymbolTableMeth = null;
		
		this.myMethods=new HashMap<AstNode,SymbolTable>();
		this.myVariables=new HashMap<AstNode,SymbolTable>();
	}
	
	/* build the classes's symbol table hierarchy according to the inheritance */
	public void setSymbolTableClassHirerachy(Program program,HashMap<AstNode, ArrayList<SymbolTable>> classAst,HashMap<String, ArrayList<SymbolTable>> classNames) {
		
		for (ClassDecl classDecl:program.classDecls()) {
			String parentName=classDecl.superName();
			if(parentName!=null) {	
				classAst.get(classDecl).get(0).setParentSymbolTable(classNames.get(parentName).get(0));
				classAst.get(classDecl).get(1).setParentSymbolTable(classNames.get(parentName).get(1));
			}
		}
		
		
	}
	
	
	/* here start the visitor */
	@Override
	public void visit(Program program) {
		
		HashMap<AstNode,ArrayList<SymbolTable>> classAst=new HashMap<AstNode,ArrayList<SymbolTable>>();
		HashMap<String,ArrayList<SymbolTable>> classNames=new HashMap<String,ArrayList<SymbolTable>>();
		
		if(program.mainClass()!=null) {
			program.mainClass().accept(this);
		}
		
		for (ClassDecl cls : program.classDecls()) {	
			
			SymbolTable currVar=new SymbolTable();
			SymbolTable currMeth=new SymbolTable();
		
			this.currentSymbolTableVar=currVar;
			currVar.setParentSymbolTable(this.ParentSymbolTableVar);
			this.currentSymbolTableMeth=currMeth;
			currMeth.setParentSymbolTable(this.ParentSymbolTableMeth);
			
			classAst.put(cls,new ArrayList<SymbolTable>(Arrays.asList(this.currentSymbolTableVar,this.currentSymbolTableMeth))); /*prepare hashmaps for setSymbolTableClassHirerachy() call*/
			classNames.put(cls.name(),new ArrayList<SymbolTable>(Arrays.asList(this.currentSymbolTableVar,this.currentSymbolTableMeth)));
			
			cls.accept(this);
		}
		
		setSymbolTableClassHirerachy(program,classAst,classNames);
	}
	
	@Override
	public void visit(ClassDecl classDecl) {
		
		this.classType=classDecl.name();
		this.currentSymbolTableVar.addEntery("this", new SymbolVars(this.classType, isField));
		this.isField=true;
		for (VarDecl vDecl: classDecl.fields()) {
		vDecl.accept(this);
		}
		this.isField=false;
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
		this.currentSymbolTableVar.addEntery(mainClass.argsName(), new SymbolVars("String-array", isField));
		
		if(mainClass.mainStatement()!=null) {
			mainClass.mainStatement().accept(this);
		}
		
	}

	@Override
	public void visit(MethodDecl methodDecl) {
		
		List<String> parameters=new ArrayList<String>();
		for(FormalArg formalArg:methodDecl.formals()) {
			formalArg.type().accept(this);
			parameters.add(this.refType);
		}
		methodDecl.returnType().accept(this);
		myMethods.put(methodDecl,this.currentSymbolTableMeth);
		this.currentSymbolTableMeth.addEntery(methodDecl.name(), new SymbolMethods(this.refType,parameters));
		
		
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
		
		methodDecl.ret().accept(this);
		/* retrieve pointers for upper scope */
		this.currentSymbolTableVar=tempVar;
		this.ParentSymbolTableVar=tempVar.getParentSymbolTable();
		
	}

	@Override
	public void visit(FormalArg formalArg) {
		
		formalArg.type().accept(this);
		myVariables.put(formalArg, this.currentSymbolTableVar);
		this.currentSymbolTableVar.addEntery(formalArg.name(), new SymbolVars(this.refType, isField));
		
	}

	@Override
	public void visit(VarDecl varDecl) {
		
		varDecl.type().accept(this);
		myVariables.put(varDecl, this.currentSymbolTableVar);
		this.currentSymbolTableVar.addEntery(varDecl.name(), new SymbolVars(this.refType,this.isField));
	}

	@Override
	public void visit(BlockStatement blockStatement) {
		for(Statement statement:blockStatement.statements()) {
			statement.accept(this);
		}
	}

	@Override
	public void visit(IfStatement ifStatement) {
		
		ifStatement.cond().accept(this);
		ifStatement.thencase().accept(this);
		ifStatement.elsecase().accept(this);
		
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		
		whileStatement.cond().accept(this);
		whileStatement.body().accept(this);
		
	}

	@Override
	public void visit(SysoutStatement sysoutStatement) {
		
		sysoutStatement.arg().accept(this);
	}

	@Override
	public void visit(AssignStatement assignStatement) {
		
		assignStatement.rv().accept(this);
		myVariables.put(assignStatement,this.currentSymbolTableVar);
		
	}

	@Override
	public void visit(AssignArrayStatement assignArrayStatement) {
		
		myVariables.put(assignArrayStatement,this.currentSymbolTableVar);

		assignArrayStatement.rv().accept(this);
		assignArrayStatement.index().accept(this);
	}

	@Override
	public void visit(AndExpr e) {
		
		e.e1().accept(this);
		e.e2().accept(this);
		
	}

	@Override
	public void visit(LtExpr e) {
		
		e.e1().accept(this);
		e.e2().accept(this);
		
	}

	@Override
	public void visit(AddExpr e) {


	e.e1().accept(this);
	e.e2().accept(this);
	}	
	

	@Override
	public void visit(SubtractExpr e) {

		e.e1().accept(this);
		e.e2().accept(this);
		
	}

	@Override
	public void visit(MultExpr e) {

		e.e1().accept(this);
		e.e2().accept(this);
		
	}

	@Override
	public void visit(ArrayAccessExpr e) {

		e.arrayExpr().accept(this);
		e.indexExpr().accept(this);
	}

	@Override
	public void visit(ArrayLengthExpr e) {
		
		e.arrayExpr().accept(this);	
	}

	@Override
	public void visit(MethodCallExpr e) {

	e.ownerExpr().accept(this);
	myMethods.put(e, this.currentSymbolTableMeth);
	
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
		
	}

	@Override
	public void visit(NewIntArrayExpr e) {
		
		e.lengthExpr().accept(this);
	}

	@Override
	public void visit(NewObjectExpr e) {	
		myVariables.put(e, this.currentSymbolTableVar);
	}

	@Override
	public void visit(NotExpr e) {
		
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
			System.out.println("My name is: "+it.getKey()+" at line "+it.getKey().lineNumber+" and my symbol table is:\n");
			System.out.println(it.getValue());
		}
		
		System.out.println("printing methods symbol tables:\n");
		for (Map.Entry<AstNode, SymbolTable> it: methodsSet) {
			System.out.print("My name is: "+it.getKey()+" at line "+it.getKey().lineNumber+"and my symbol table is: \n");
			System.out.println(it.getValue());
		}
		
		return "";
	}

}
