package ast;

import java.util.HashMap;
import java.util.Map;

public class SemanticCheck implements Visitor {

	private HashMap<AstNode,SymbolTable> VarTable;
	private HashMap<AstNode,SymbolTable> methTable;
	Map<String,ClassDecl> className;
	private String refType;
	private boolean isOk;
	
	public SemanticCheck(SymbolTableBuilder symbolTableBuilder) {
		
		this.VarTable=symbolTableBuilder.myVariables;
		this.methTable=symbolTableBuilder.myMethods;
		this.className=new HashMap<String, ClassDecl>();
		this.refType="";
		isOk=true;
	}

	@Override
	public void visit(Program program) {

		String mainName=program.mainClass().name();
		for(ClassDecl classDecl:program.classDecls()) {
			String myName=classDecl.name();
			String parentName=classDecl.superName();
			if(className.containsKey(myName)||(parentName!=null && !(className.containsKey(parentName)))||parentName.equals(mainName))
				isOk= false;
			className.put(myName, classDecl);
				
		}
		
	}

	@Override
	public void visit(ClassDecl classDecl) {
		SymbolTable parent=this.VarTable.get(classDecl).getParentSymbolTable();
		Map<String,VarDecl> fieldName= new HashMap<String,VarDecl>();
		for(VarDecl varDecl: classDecl.fields()) {
			String myName=varDecl.name();
			if(fieldName.containsKey(myName)) // redeclaration in current class
				isOk=false;
			if(parent!=null && parent.lookupVars(myName)!=null) // check redeclaration in upper classes
				isOk=false;
			fieldName.put(myName, varDecl);
		}
		Map<String,MethodDecl> methodName= new HashMap<String,MethodDecl>();
		for(MethodDecl methodDecl: classDecl.methoddecls()) {
			String myName=methodDecl.name();
			if(methodName.containsKey(myName)) // redeclaration in current class
				isOk=false;
			methodName.put(myName, methodDecl);
		}
	}
		

	@Override
	public void visit(MainClass mainClass) {
		mainClass.mainStatement().accept(this);
		
	}

	@Override
	public void visit(MethodDecl methodDecl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(FormalArg formalArg) {
		formalArg.type().accept(this);
		if((formalArg.type() instanceof RefType) && className.get(refType)==null) // no definition
			isOk= false;
	}

	@Override
	public void visit(VarDecl varDecl) {
		
		varDecl.type().accept(this);
		if((varDecl.type() instanceof RefType) && className.get(refType)==null) // no definition
			isOk= false;
		
	}

	@Override
	public void visit(BlockStatement blockStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IfStatement ifStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SysoutStatement sysoutStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AssignStatement assignStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AssignArrayStatement assignArrayStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AndExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LtExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AddExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SubtractExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MultExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ArrayAccessExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ArrayLengthExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MethodCallExpr e) {
		
		
	}

	@Override
	public void visit(IntegerLiteralExpr e) {
		
		
	}

	@Override
	public void visit(TrueExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(FalseExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IdentifierExpr e) {
		SymbolTable myTable=this.VarTable.get(e);
		if(myTable.lookupVars(e.id())==null) // no definition
			isOk=false;
	}

	@Override
	public void visit(ThisExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NewIntArrayExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NewObjectExpr e) {

		if(!(className.containsKey(e.classId()))) // no definition
				isOk=false;
		
	}

	@Override
	public void visit(NotExpr e) {
		// TODO Auto-generated method stub
		
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
	public void visit(ast.RefType t) {
		this.refType=t.id();
		
	}

	}	