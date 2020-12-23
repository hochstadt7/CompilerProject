package ast;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class SemanticCheck implements Visitor {

	private HashMap<AstNode,SymbolTable> VarTable;
	private HashMap<AstNode,SymbolTable> methTable;
	Map<String,ClassDecl> className;
	private String refType;
	private boolean isOk;
	private Set<String> uninit;
	
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
		SymbolMethods ancestorMethod=null;
		SymbolTable possibleParent=methTable.get(methodDecl).getParentSymbolTable();
		if(possibleParent!=null)
			ancestorMethod=possibleParent.lookupMethods(methodDecl.name());
		Set<String> formal_names = new HashSet<String>(); 
		uninit = new HashSet<String>();
		String returnType;
		//(#24 formal check)
		for(FormalArg formal:methodDecl.formals())
		{
			if(formal_names.contains(formal.name()))
				isOk = false;
			formal_names.add(formal.name());
		}
		for(VarDecl varDecl: methodDecl.vardecls()) {
			String myName=varDecl.name();
			if(localName.containsKey(myName)) // redeclaration in current method
				isOk=false;
			// possible that same name of local var will appear as field of class
			localName.put(myName, varDecl);
			uninit.add(myName);
		}
		//checking for correct override (#6)
		if (ancestorMethod != null)
		{
			methodDecl.returnType().accept(this);
			if(!IsDaughterClass(refType,ancestorMethod.getType()))
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
		for (Statement statement : methodDecl.body()) {
			statement.accept(this);
		}
		//(#18)
		methodDecl.returnType().accept(this);
		returnType = refType;
		methodDecl.ret().accept(this);
		
		if(!IsDaughterClass(this.refType,returnType))
			isOk = false;
		
			
		
	}
	
	public boolean IsDaughterClass(String retType,String returnType) {
		if(retType.equals(returnType))
			return true;
		ClassDecl classDecl=className.get(returnType);
		if(classDecl==null)//returnType is not even a ref type
			return false;
		String parent=classDecl.superName();
		if(parent==null)
			return false;
		return IsDaughterClass(retType,parent);
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
		for (Statement s : blockStatement.statements()) {
			s.accept(this);
		}
	}

	@Override
	public void visit(IfStatement ifStatement) {
		//checking if condition is boolean (#17)
		ifStatement.cond().accept(this);
		checkType("boolean");
		Set<String> elseClone = new HashSet<String>(uninit);
		ifStatement.thencase().accept(this);
		Set<String> thenClone = new HashSet<String>(uninit);
		uninit = elseClone;
		ifStatement.elsecase().accept(this);
		uninit.addAll(thenClone);
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		//checking while condition is boolean (#17)
		whileStatement.cond().accept(this);
		checkType("boolean");
		Set<String> clone = new HashSet<String>(uninit);
		whileStatement.body().accept(this);
		uninit = clone;
	}

	@Override
	public void visit(SysoutStatement sysoutStatement) {
		//print arg is int (#20)
		sysoutStatement.arg().accept(this);
		checkType("int");
	}

	@Override
	public void visit(AssignStatement assignStatement) {
		// We probably need to do more than that.
		uninit.remove(assignStatement.lv());
		String leftType = VarTable.get(assignStatement).lookupVars(assignStatement.lv()).getType();
		assignStatement.rv().accept(this);
		if(!IsDaughterClass(this.refType,leftType))
			isOk=false;
	}

	@Override
	public void visit(AssignArrayStatement assignArrayStatement) {
		//(#15)
		if (uninit.contains(assignArrayStatement.lv())) {
			isOk = false;
		}
		//(#23)
		refType = VarTable.get(assignArrayStatement).lookupVars(assignArrayStatement.lv()).getType();
		checkType("int-array");
		assignArrayStatement.index().accept(this);
		checkType("int");
		assignArrayStatement.rv().accept(this);
		checkType("int");
	}
	private void binaryOperator(BinaryExpr e, String inputType, String outputType)
	{
		String firstType;
		e.e1().accept(this);
		firstType = refType;
		e.e2().accept(this);
		if(!refType.equals(inputType) || !firstType.equals(inputType))
			isOk = false;
		refType = outputType;
	}
	
	private void checkType(String type)
	{
		if(!refType.equals(type))
			isOk = false;
	}
	@Override
	public void visit(AndExpr e) {
		//(#21)
		binaryOperator(e, "boolean", "boolean");
	}

	@Override
	public void visit(LtExpr e) {
		//(#21)
		binaryOperator(e, "int", "boolean");
	}

	@Override
	public void visit(AddExpr e) {
		//(#21)
		binaryOperator(e, "int", "int");
	}

	@Override
	public void visit(SubtractExpr e) {
		//(#21)
		binaryOperator(e, "int", "int");
	}

	@Override
	public void visit(MultExpr e) {
		//(#21)
		binaryOperator(e, "int", "int");
	}

	@Override
	public void visit(ArrayAccessExpr e) {
		//(#22)
		e.arrayExpr().accept(this);
		checkType("int-array");
		e.indexExpr().accept(this);
		checkType("int");
	}

	@Override
	public void visit(ArrayLengthExpr e) {
		//checking caller of length is array (#13)
		e.arrayExpr().accept(this);
		checkType("int-array");
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
		if(ownerClass != null) {
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
		//(#15)
		if (uninit.contains(e.id())) {
			isOk = false;
		}
	}

	@Override
	public void visit(ThisExpr e) {
		this.refType= VarTable.get(e).lookupVars("this").getType();
		
	}

	@Override
	public void visit(NewIntArrayExpr e) {
		//(#25)
		e.lengthExpr().accept(this);
		checkType("int");
		refType="int-array";
		
	}

	@Override
	public void visit(NewObjectExpr e) {
		
		this.refType=e.classId();
		if(!(className.containsKey(this.refType))) // no definition
				isOk=false;
		
	}
	@Override
	public void visit(NotExpr e) {
		//(#21)
		e.e().accept(this);
		checkType("boolean");
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
