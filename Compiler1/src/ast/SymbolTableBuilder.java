package ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SymbolTableBuilder implements Visitor {

	public Map<AstNode,SymbolTable> myMethods;
	public Map<AstNode,SymbolTable> myVariables;
	
	private SymbolTable currentSymbolTableVar; 
	private SymbolTable ParentSymbolTableVar;
	private SymbolTable currentSymbolTableMeth; 
	private SymbolTable ParentSymbolTableMeth;
	
	private String refType=""; /* type of the variable */
	private String classType=""; /* current class */
	
	public SymbolTableBuilder() {
		this.currentSymbolTableVar = null;
		this.ParentSymbolTableVar = null;
		this.currentSymbolTableMeth = null;
		this.ParentSymbolTableMeth = null;
		
		this.myMethods=new HashMap<AstNode,SymbolTable>();
		this.myVariables=new HashMap<AstNode,SymbolTable>();
	}
	
	/* build the classes's symbol table hierarchy according to the inheritance */
	public void setSymbolTableClassHirerachy(Program program,Map<AstNode,ArrayList<SymbolTable>> classAst,Map<String,ArrayList<SymbolTable>> classNames) {
		
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
		
		Map<AstNode,ArrayList<SymbolTable>> classAst=new HashMap<AstNode,ArrayList<SymbolTable>>();
		Map<String,ArrayList<SymbolTable>> classNames=new HashMap<String,ArrayList<SymbolTable>>();
		
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
		for (VarDecl vDecl: classDecl.fields()) {
		vDecl.accept(this);
		}
		
		for (MethodDecl methodDecl: classDecl.methoddecls()) {
			methodDecl.accept(this);
		}	
	}

	@Override
	public void visit(MainClass mainClass) {
		this.classType=mainClass.name();
		SymbolTable currVar=new SymbolTable();
		SymbolTable currMeth=new SymbolTable();
		
		this.currentSymbolTableVar=currVar;
		currVar.setParentSymbolTable(this.ParentSymbolTableVar);
		this.currentSymbolTableMeth=currMeth;
		currMeth.setParentSymbolTable(this.ParentSymbolTableMeth);
		
		myVariables.put(mainClass,this.currentSymbolTableVar );
		this.currentSymbolTableVar.addEntery(mainClass.argsName(), new Symbol("String-array"));
		
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
		this.currentSymbolTableVar.addEntery("this", new Symbol(this.classType));
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
