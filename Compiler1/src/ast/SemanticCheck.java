package ast;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
		for(ClassDecl classDecl:program.classDecls()) {
			classDecl.accept(this);
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
		Map<String,VarDecl> localName= new HashMap<String,VarDecl>();
		SymbolMethods ancestorMethod= methTable.get(methodDecl).lookupMethodOverride(methodDecl.name());
		for(VarDecl varDecl: methodDecl.vardecls()) {
			String myName=varDecl.name();
			if(localName.containsKey(myName)) // redeclaration in current method
				isOk=false;
			// possible that same name of local var will appear as field of class
			localName.put(myName, varDecl);
		}
		//checking for correct override (#6)
		if (ancestorMethod != null)
		{
			methodDecl.returnType().accept(this);
			if(!refType.equals(ancestorMethod.getType())) // same return type
				isOk=false;
			if(methodDecl.vardecls().size() == ancestorMethod.getParameters().size()) // same number of args
			{
				//arg type check
				ListIterator<String> iter = ancestorMethod.getParameters().listIterator();
				for(VarDecl varDecl: methodDecl.vardecls()) {
					varDecl.type().accept(this);
					if(!refType.equals(iter.next()))
						isOk=false;
				}
			}
			else
				isOk=false;
		}
		
	}

	@Override
	public void visit(FormalArg formalArg) {
		
		//before or after, Tom will check redeclaration
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
		//checking if condition is boolean (#17)
		ifStatement.cond().accept(this);
		if(refType.equals("boolean"))
			isOk = false;
		ifStatement.thencase().accept(this);
		ifStatement.elsecase().accept(this);
		
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		//checking while condition is boolean (#17)
		whileStatement.cond().accept(this);
		if(refType.equals("boolean"))
			isOk = false;
		whileStatement.body().accept(this);
		
	}

	@Override
	public void visit(SysoutStatement sysoutStatement) {
		
		
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
		//checking caller of length is array (#13)
		e.accept(this);
		if(!refType.equals("int-array"))
			isOk = false;
		
	}

	@Override
	public void visit(MethodCallExpr e) {
		String type ="";
		ClassDecl ownerClass;
		boolean inClass = false;
		List<String> parameters;
		ListIterator<String> iter;
		e.ownerExpr().accept(this);
		type = refType;
		//checking the caller is of new, this, or identifier (#12) 
		if(!(e.ownerExpr() instanceof NewObjectExpr || e.ownerExpr() instanceof ThisExpr || e.ownerExpr() instanceof IdentifierExpr))
			isOk = false;
		//checking the type of the caller is valid (#10)
		if(type.equals("int") || type.equals("boolean") || type.equals("int-array"))
		{
			isOk = false;
		}
		ownerClass = className.get(type);
		//checking for correct method call (#11)
		if(ownerClass != null)
			for (MethodDecl methdecl: ownerClass.methoddecls())
			{
				if(methdecl.name().equals(e.methodId()))//a method with such name exists in the class
				{
					inClass = true;
					parameters = methTable.get(methdecl).lookupMethods(methdecl.name()).getParameters();
					iter = parameters.listIterator();
					if(e.actuals().size() == parameters.size())
					{
						for(Expr expr: e.actuals())//checking arg types
						{
							expr.accept(this);
							if(refType.equals(iter.next()))
								isOk = false;
						}
					}
					else
						isOk = false;
				}
			}
	else
		isOk = false;
	isOk = isOk && inClass;
	}
	

	@Override
	public void visit(IntegerLiteralExpr e) {
		refType = "int";
		
	}

	@Override
	public void visit(TrueExpr e) {
		refType = "boolean";
		
	}

	@Override
	public void visit(FalseExpr e) {
		refType = "boolean";
		
	}

	@Override
	public void visit(IdentifierExpr e) {
		SymbolTable myTable=this.VarTable.get(e);
		if(myTable.lookupVars(e.id())==null) // no definition
			isOk=false;
		else
			refType = myTable.lookupVars(e.id()).getType();
	}

	@Override
	public void visit(ThisExpr e) {
		this.refType= VarTable.get(e).lookupVars("this").getType();
		
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
		refType = "boolean";
		
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
