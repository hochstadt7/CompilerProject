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
	public boolean isOk;
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
			if(className.containsKey(myName)||(parentName!=null && (!(className.containsKey(parentName))||parentName.equals(mainName)))) {
				isOk= false; return;
			}
			className.put(myName, classDecl);
		}
		for(ClassDecl classDecl:program.classDecls()) {
			classDecl.accept(this);
			if(!isOk)
				return;
		}
		program.mainClass().accept(this);
	}

	@Override
	public void visit(ClassDecl classDecl) {
		SymbolTable parent=this.VarTable.get(classDecl).getParentSymbolTable();
		Map<String,VarDecl> fieldName= new HashMap<String,VarDecl>();
		for(VarDecl varDecl: classDecl.fields()) {
			String myName=varDecl.name();
			if(fieldName.containsKey(myName)) { // redeclaration in current class
				isOk=false; return;
			}
			if(parent!=null && parent.lookupVars(myName)!=null) { // redeclaration in upper classes (possible in java, not in minijava)
				isOk=false; return;
			}
			fieldName.put(myName, varDecl);
		}
		Map<String,MethodDecl> methodName= new HashMap<String,MethodDecl>();
		for(MethodDecl methodDecl: classDecl.methoddecls()) {
			String myName=methodDecl.name();
			if(methodName.containsKey(myName)) { // redeclaration in current class
				isOk=false; return;
			}
			methodName.put(myName, methodDecl);
		}
		for (VarDecl varDecl: classDecl.fields()) {
			varDecl.accept(this);
			if(!isOk)
				return;
		}
		for(MethodDecl methodDecl: classDecl.methoddecls()) {
			methodDecl.accept(this);
			if(!isOk)
				return;
		}
		
	}
		

	@Override
	public void visit(MainClass mainClass) {
		mainClass.mainStatement().accept(this); // nothing else
		
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
			if(formal_names.contains(formal.name())) {
				isOk = false; return;
			}
			formal_names.add(formal.name());
		}
		for(VarDecl varDecl: methodDecl.vardecls()) {
			String myName=varDecl.name();
			if(localName.containsKey(myName)) { // redeclaration in current method
				isOk=false; return;
			}
			// possible that same name of local var will appear as field of class
			localName.put(myName, varDecl);
			uninit.add(myName);
		}
		//checking for correct override (#6)
		if (ancestorMethod != null)
		{
			methodDecl.returnType().accept(this);
			if(!IsDaughterClass(refType,ancestorMethod.getType())) {
				isOk=false; return;
			}
			if(methodDecl.vardecls().size() == ancestorMethod.getParameters().size()) // same number of args
			{
				//arg type check
				ListIterator<String> iter = ancestorMethod.getParameters().listIterator();
				for(VarDecl varDecl: methodDecl.vardecls()) {
					varDecl.type().accept(this);
					if(!isOk)
						return;
					
					if(!refType.equals(iter.next())) {
						isOk=false; return;
					}
				}
			}
			else {
				isOk=false; return;
			}
		}
		//(#18)
		methodDecl.returnType().accept(this);
		if(!isOk)
			return;
		returnType = refType;
		methodDecl.ret().accept(this);
		if(!isOk)
			return;
		if(!IsDaughterClass(this.refType,returnType)) {
			isOk = false; return;
		}
		for (Statement statement : methodDecl.body()) {
			statement.accept(this);
			if(!isOk)
				return;
		}
		for(FormalArg formalArg: methodDecl.formals()) {
			formalArg.accept(this);
			if(!isOk)
				return;
		}
		for (VarDecl varDecl: methodDecl.vardecls()) {
			varDecl.accept(this);
			if(!isOk)
				return;
		}
		
			
		
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
		
		if((formalArg.type() instanceof RefType) && className.get(refType)==null) // no definition
			isOk= false;
	}

	@Override
	public void visit(VarDecl varDecl) {
		
		if((varDecl.type() instanceof RefType) && className.get(refType)==null) // no definition
			isOk= false;
		
	}

	@Override
	public void visit(BlockStatement blockStatement) {
		for (Statement s : blockStatement.statements()) {
			s.accept(this);
			if(!isOk)
				return;
		}
	}

	@Override
	public void visit(IfStatement ifStatement) {
		//checking if condition is boolean (#17)
		ifStatement.cond().accept(this);
		if(!isOk)
			return;
		checkType("boolean");
		if(!isOk)
			return;
		Set<String> elseClone = new HashSet<String>(uninit);
		ifStatement.thencase().accept(this);
		if(!isOk)
			return;
		Set<String> thenClone = new HashSet<String>(uninit);
		uninit = elseClone;
		ifStatement.elsecase().accept(this);
		if(!isOk)
			return;
		uninit.addAll(thenClone);
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		//checking while condition is boolean (#17)
		whileStatement.cond().accept(this);
		if(!isOk)
			return;
		checkType("boolean");
		if(!isOk)
			return;
		Set<String> clone = new HashSet<String>(uninit);
		whileStatement.body().accept(this);
		if(!isOk)
			return;
		uninit = clone;
	}

	@Override
	public void visit(SysoutStatement sysoutStatement) {
		//print arg is int (#20)
		sysoutStatement.arg().accept(this);
		if(!isOk)
			return;
		checkType("int");
	}

	@Override
	public void visit(AssignStatement assignStatement) {
		SymbolDetails sym = VarTable.get(assignStatement).lookupVars(assignStatement.lv());
		if (sym == null) {
			isOk = false; return;
		}
		String leftType = sym.getType();
		assignStatement.rv().accept(this);
		if(!isOk)
			return;
		if(!IsDaughterClass(this.refType,leftType)) {
			isOk=false; return;
		}
		uninit.remove(assignStatement.lv());
	}

	@Override
	public void visit(AssignArrayStatement assignArrayStatement) {
		//(#15)
		if (uninit.contains(assignArrayStatement.lv())) {
			isOk = false; return;
		}
		//(#23)
		refType = VarTable.get(assignArrayStatement).lookupVars(assignArrayStatement.lv()).getType();
		checkType("int-array");
		if(!isOk)
			return;
		assignArrayStatement.index().accept(this);
		if(!isOk)
			return;
		checkType("int");
		if(!isOk)
			return;
		assignArrayStatement.rv().accept(this);
		if(!isOk)
			return;
		checkType("int");
		
	}
	private void binaryOperator(BinaryExpr e, String inputType, String outputType)
	{
		String firstType;
		e.e1().accept(this);
		if(!isOk)
			return;
		firstType = refType;
		e.e2().accept(this);
		if(!isOk)
			return;
		if(!refType.equals(inputType) || !firstType.equals(inputType)) {
			isOk = false; return;
		}
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
		if(!isOk)
			return;
		checkType("int-array");
		if(!isOk)
			return;
		e.indexExpr().accept(this);
		if(!isOk)
			return;
		checkType("int");
	}

	@Override
	public void visit(ArrayLengthExpr e) {
		 //checking caller of length is array (#13)
		e.arrayExpr().accept(this);
		if(!isOk)
			return;
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
		if(!isOk)
			return;
		type = refType;
		//checking the caller is of new, this, or identifier (#12) 
		if(!(e.ownerExpr() instanceof NewObjectExpr || e.ownerExpr() instanceof ThisExpr || e.ownerExpr() instanceof IdentifierExpr)) {
			isOk = false; return;
		}
		//checking the type of the caller is valid (#10)
		if(type.equals("int") || type.equals("boolean") || type.equals("int-array"))
		{
			isOk = false; return;
		}
		ownerClass = className.get(type);
		//checking for correct method call (#11)
		if(ownerClass != null) {
			for (MethodDecl methdecl: ownerClass.methoddecls())
			{
				if(methdecl.name().equals(e.methodId()))//a method with this name exists in the class
				{
					inClass = true;
					parameters = methTable.get(methdecl).lookupMethods(methdecl.name()).getParameters();
					iter = parameters.listIterator();
					if(e.actuals().size() == parameters.size())
					{
						for(Expr expr: e.actuals())//checking arg types
						{
							expr.accept(this);
							if(!isOk)
								return;
							if(refType.equals(iter.next())) {
								isOk = false; return;
							}
						}
					}
					else {
						isOk = false;
						return;
					}
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
		SymbolVars definition=myTable.lookupVars(e.id());
		if(definition==null) { // no definition
			isOk=false; return;
		}
		else
			refType = definition.getType();
		//(#15)
		if (uninit.contains(e.id())) {
			isOk = false; return;
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
		if(!isOk)
			return;
		checkType("int");
		refType="int-array";
		
	}

	@Override
	public void visit(NewObjectExpr e) {
		
		this.refType=e.classId();
		if(!(className.containsKey(this.refType))) { // no definition
				isOk=false; return;
		}
		
	}
	@Override
	public void visit(NotExpr e) {
		//(#21)
		e.e().accept(this);
		if(!isOk)
			return;
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
