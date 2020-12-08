package ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TranslatorVisitor implements Visitor {
	
	public StringBuilder emitted;
	private int ifCounter,whileCounter, registerCounter, andCounter, arrayCounter;
	String lastResult;
	AstNode currentClass;
	Map<ClassDecl, Vtable> ClassTable; /*classes and their Vtable*/
	Map<String,ClassDecl> className;
	private HashMap<AstNode,SymbolTable> sTable; /* variable symbol table */
	
	public TranslatorVisitor(HashMap<AstNode,SymbolTable> _sTable) {
		emitted=new StringBuilder();
		this.ifCounter=0; 
		this.whileCounter=0;
		this.registerCounter = 0;
		this.andCounter = 0;
		arrayCounter = 0;
		this.lastResult="";
		ClassTable=new HashMap<ClassDecl, Vtable>(); 
		className=new HashMap<String,ClassDecl>();
		sTable = _sTable;
	}

	
	@Override
	public void visit(Program program) {
		
		for (ClassDecl classDecl : program.classDecls())
			className.put(classDecl.name(), classDecl);
		
		for (ClassDecl classDecl : program.classDecls()) {
			Vtable myTable=BuildVtable(classDecl);
			if(myTable.getMethodOffset().keySet().size()!=0) { /* at least one method in Vtable */
			String prefix="@."+classDecl.name()+"_vtable = global ["+ myTable.getMethodOffset().size()+" x i8*] ";
			StringBuilder sufix=new StringBuilder();
			for (MethodDecl methodDecl:myTable.getMethodOffset().keySet()) {
				sufix.append("[i8* bitcast (");
				methodDecl.returnType().accept(this);
				sufix.append(lastResult);
				sufix.append(" (i8*, ");
				StringBuilder formalArgs=new StringBuilder();
				for (FormalArg formalArg: methodDecl.formals()) {
					formalArg.type().accept(this);
					formalArgs.append(lastResult+", ");
				}
				sufix.append(formalArgs.toString());
				sufix.setLength(sufix.length()-2);
				sufix.append(")* @"+classDecl.name()+"."+methodDecl.name()+" to i8*), ");
			}
			
			sufix.setLength(sufix.length()-2);
			sufix.append("]");
			emit(prefix+sufix.toString()+"\n");
			
	}
		}
		emit("");
		print_helpers_methods();
		emit("");
		program.mainClass().accept(this);
		for (ClassDecl classDecl : program.classDecls()) {
			classDecl.accept(this);
		}
		
	}

	@Override
	public void visit(ClassDecl classDecl) {
		currentClass = classDecl;
		for (MethodDecl methodDecl : classDecl.methoddecls()) {
			methodDecl.accept(this);
		}
		emit("");
	}

	@Override
	public void visit(MainClass mainClass) {
		emit("define i32 @main() {");
		mainClass.mainStatement().accept(this);
		emit("	ret i32 0"); /* do we need this*/
		emit("}\n");
	}

	@Override
	public void visit(MethodDecl methodDecl) {
		this.ifCounter=0; this.whileCounter=0; /* every method new counters- works? */
		String ret_type = "";
		String formals = "";
		methodDecl.returnType().accept(this);
		ret_type = lastResult;
		for(FormalArg formalArg: methodDecl.formals())
		{
			formalArg.type().accept(this);
			formals += ", " + lastResult + " %."+formalArg.name();
			
		}
		emit("define "+ret_type+" @" + methodDecl.name() + "(i8* %this" + formals +") {");
		for(FormalArg formalArg: methodDecl.formals())
		{
			formalArg.type().accept(this);
			emit("	%"+ formalArg.name() +" = alloca " + lastResult);
		}
		for(VarDecl varDecl: methodDecl.vardecls())
			varDecl.accept(this);
		for(Statement statement: methodDecl.body())
		{
			statement.accept(this);
		}
		methodDecl.ret().accept(this);
		ret_type=lastResult;
		emit("ret "+ translateType(ret_type)+ " "+lastResult); /* assuming regular functions doesn't return void type */
		emit("}\n");
	}

	@Override
	public void visit(FormalArg formalArg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(VarDecl varDecl) {
		varDecl.type().accept(this);
		boolean isField = sTable.get(varDecl).lookup(varDecl.name()).getIsField();
		if(!isField)
		{
			emit("	%"+ varDecl.name() +" = alloca " + lastResult);
		}
		
	}

	@Override
	public void visit(BlockStatement blockStatement) {
		
		for(Statement statement: blockStatement.statements())
		{
			statement.accept(this);
		}
		
	}

	@Override
	public void visit(IfStatement ifStatement) {
		int tempIf=this.ifCounter;
		this.ifCounter+=3;
		ifStatement.cond().accept(this);
		emit("	br i1 "+lastResult+", "+ "label %if"+tempIf+", label %if"+(tempIf+1));
		emit("	if"+tempIf+":");
		ifStatement.thencase().accept(this);
		emit("	br label %if"+(tempIf+2));
		emit("	if"+(tempIf+1)+":");
		ifStatement.elsecase().accept(this);
		emit("	br label %if"+(tempIf+2));
		emit("	if"+(tempIf+2)+":");
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		int tempWhile=this.whileCounter;
		this.whileCounter+=3;
		emit("	br label %loop"+tempWhile);
		emit("	loop"+tempWhile+":");
		whileStatement.cond().accept(this);
		emit("	br i1 "+lastResult+", "+ "label %loop"+(tempWhile+1)+", label %loop"+(tempWhile+2));
		emit("	loop"+(tempWhile+1)+":");
		whileStatement.body().accept(this);
		emit("	br label %loop"+(tempWhile+2));
		emit("	loop"+(tempWhile+2)+":");
	}

	@Override
	public void visit(SysoutStatement sysoutStatement) {
		sysoutStatement.arg().accept(this);
		emit("	call void (i32) @print_int(i32 "+lastResult+")");
		
	}
	
	private String translateType(String type) {
		switch (type) {
		case "int":
			return "i32";
		case "boolean":
			return "i1";
		case "int-array":
			return "i32*";
		default:
			return "i8*";
		}
	}
	
	private String getVariablePtr(String name, String type, SymbolTable table) {
		Vtable tempVTable = ClassTable.get(currentClass);
		boolean isField = table.lookup(name).getIsField();
		String reg, last;
		int fieldLocation = 0;
		if(isField) // If lv is a field, we have to load/store it differently.
		{
			for(VarDecl varDecl: tempVTable.getFieldOffset().keySet())
			{
				if(varDecl.name().equals(name))
					fieldLocation = tempVTable.getFieldOffset().get(varDecl);
			}
			reg = newReg();
			emit("	"+reg +" = getelementptr i8, i8* %this, " + type + " " + fieldLocation);
			last = reg;
			reg = newReg();
			emit("	"+reg + " = bitcast i8* " + last + " to " + type + "*");
			return "%" + reg;
		}
		else
		{
			return "%" + name;
		}
	}

	@Override
	public void visit(AssignStatement assignStatement) {
		String type = sTable.get(assignStatement).lookup(assignStatement.lv()).getType();
		type = translateType(type);
		String ptr = getVariablePtr(assignStatement.lv(), type, sTable.get(assignStatement));
		assignStatement.rv().accept(this);
		emit("	store "+ type + " " + lastResult + ", " + type + "* " + ptr);
	}
	
	private void branchCallThrowOob(String ok) {
		// There is no possibility of nesting here so no temp is needed.
		emit("	br i1 " + ok + ", label %arr" + (arrayCounter + 1) + ", label %arr" + arrayCounter);
		emit("	arr" + arrayCounter + ":");
		emit("	call void @throw_oob()");
		//emit("br label %arr" + (arrayCounter + 1)); // Do we really need this?
		emit("	arr" + (arrayCounter + 1) + ":");
		arrayCounter += 2;
	}
	
	private String getVariable(String type, String name, SymbolTable table) {
		String ptr = getVariablePtr(name, type, table);
		String reg = newReg();
		emit("	"+reg +" = load " + type + ", " + type + "* " + ptr);
		return reg;
	}
	
	private String arrayAccess(String arr, String index) {
		String length = newReg();
		emit("	"+length + " = load i32, i32* " + arr);
		String nonnegative = newReg();
		emit("	"+nonnegative + " = icmp sle i32 0, " + index);
		branchCallThrowOob(nonnegative);
		String small = newReg();
		emit("	"+small + " = icmp sgt i32 " + length + ", " + index);
		branchCallThrowOob(small);
		String ahYesArraysActuallyStartAt1 = newReg();
		emit("	"+ahYesArraysActuallyStartAt1 + " = add i32 1, " + index);
		String res = newReg();
		emit("	"+res + " = getelementptr i32, i32* " + arr + ", i32 " + ahYesArraysActuallyStartAt1);
		return res;
	}

	@Override
	public void visit(AssignArrayStatement assignArrayStatement) {
		String arr = getVariable("i32*", assignArrayStatement.lv(), sTable.get(assignArrayStatement));
		assignArrayStatement.index().accept(this);
		String place = arrayAccess(arr, lastResult);
		assignArrayStatement.rv().accept(this);
		emit("	store i32 " + lastResult + ", i32* " + place);
	}

	@Override
	public void visit(AndExpr e) {
		int tempAnd=this.andCounter;
		this.andCounter+=3;
		e.e1().accept(this);
		emit("	br i1 " + lastResult + ", label %and" + tempAnd + ", label %and" + (tempAnd + 1));
		emit("	and" + tempAnd + ":");
		e.e2().accept(this);
		emit("	br label %and" + (tempAnd + 2));
		emit("	and" + (tempAnd + 1) + ":");
		emit("	br label %and" + (tempAnd + 2));
		emit("	and" + (tempAnd + 2) + ":");
		String result = newReg();
		emit("	"+result + " = phi i1 [" + lastResult + ", %and" + tempAnd + "], [0, %and" + (tempAnd + 1) + "]");
		lastResult = result;
	}

	@Override
	public void visit(LtExpr e) {
		e.e1().accept(this);
		String firstArg = lastResult;
		e.e2().accept(this);
		String result = newReg();
		emit("	"+result + " = icmp slt i32 " + firstArg + ", " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(AddExpr e) {
		e.e1().accept(this);
		String firstArg = lastResult;
		e.e2().accept(this);
		String result = newReg();
		emit("	"+result + " = add i32 " + firstArg + ", " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(SubtractExpr e) {
		e.e1().accept(this);
		String firstArg = lastResult;
		e.e2().accept(this);
		String result = newReg();
		emit("	"+result + " = sub i32 " + firstArg + ", " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(MultExpr e) {
		e.e1().accept(this);
		String firstArg = lastResult;
		e.e2().accept(this);
		String result = newReg();
		emit("	"+result + " = mul i32 " + firstArg + ", " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(ArrayAccessExpr e) {
		e.arrayExpr().accept(this);
		String arr = lastResult;
		e.indexExpr().accept(this);
		String place = arrayAccess(arr, lastResult);
		lastResult = newReg();
		emit("	"+lastResult + " = load i32, i32* " + place);
	}

	@Override
	public void visit(ArrayLengthExpr e) {
		e.arrayExpr().accept(this);
		String res = newReg();
		emit("	"+res + " = load i32, i32* " + lastResult);
		lastResult = res;
	}

	@Override
	public void visit(MethodCallExpr e) {
		int i = 0;
		String type = ""; //used to store the type of the caller
		Vtable tempVTable;
		int offset = 0;
		String caller;
		String return_val;
		String func_reg;
		ArrayList<String> arg_type_list = new ArrayList<String>(); //used to store the type of the args
		String arg_types = "";
		String actuals = "i8*, ";
		//get the class ID of the caller
		if(e.ownerExpr() instanceof ThisExpr)
			type = sTable.get(e.ownerExpr()).lookup("this").getType();
		else if(e.ownerExpr() instanceof NewObjectExpr)
			type = ((NewObjectExpr) e.ownerExpr()).classId();
		else if(e.ownerExpr() instanceof IdentifierExpr)
			type = sTable.get(e.ownerExpr()).lookup(((IdentifierExpr) e.ownerExpr()).id()).getType();
		//get Vtable of class
		tempVTable=ClassTable.get(className.get(type));
		//using the Vtable to get the actual types
		for(MethodDecl methodDecl:tempVTable.getMethodOffset().keySet())
		{
			
			if(methodDecl.name().equals(e.methodId()))
			{
				offset = tempVTable.getMethodOffset().get(methodDecl);
				for (FormalArg formalArg: methodDecl.formals())
				{
					formalArg.type().accept(this);
					arg_type_list.add(lastResult);
					arg_types += lastResult + ",";
				}
				methodDecl.returnType().accept(this);
				arg_types = arg_types.substring(0, arg_types.length() - 1);
			}
		}
		return_val = lastResult;
		e.ownerExpr().accept(this);
		caller = lastResult;
		String ptr = newReg();
		// need to store here before?? according to examples..
		emit("	"+ptr + " = load i8*, i8** " + caller);
		String last = ptr;
		ptr = newReg();
		emit("	"+ptr + " = bitcast i8* " + last + " to i8***");
		last = ptr;
		ptr = newReg();
		emit("	"+ptr + " = load i8**, i8*** " + last);
		last = ptr;
		ptr = newReg();
		emit("	"+ptr + " = getelementptr i8*, i8** " + last + ", i32 " + offset);
		last = ptr;
		ptr = newReg();
		emit("	"+ptr + " = load i8*, i8** " + last);
		last = ptr;
		ptr = newReg();
		func_reg = ptr;
		emit("	"+ptr + " = bitcast i8* " + last + " to " + return_val + " (" + arg_types + ")*");
		last = ptr;
		ptr = newReg();
		actuals += "i8* " + caller;
		//store actuals and build actual string
		for(Expr arg : e.actuals())
		{
			actuals += ", " + arg_type_list.get(i) + " ";
			arg.accept(this);
			ptr = newReg();
			if(!(arg instanceof IntegerLiteralExpr))//no need to store int literal
				emit("	"+ptr + " = load "+arg_type_list.get(i)+", "+arg_type_list.get(i)+"* " + lastResult);
			actuals += lastResult;
		}
		ptr = newReg();
		emit("	"+ptr + " = call "+return_val+" " + func_reg +"("+actuals+")");
		lastResult = ptr;
		
	}

	@Override
	public void visit(IntegerLiteralExpr e) {
		lastResult = Integer.toString(e.num());
	}

	@Override
	public void visit(TrueExpr e) {
		lastResult = "1";
	}

	@Override
	public void visit(FalseExpr e) {
		lastResult = "0";
	}

	@Override
	public void visit(IdentifierExpr e) {
		String type = sTable.get(e).lookup(e.id()).getType();
		type = translateType(type);
		lastResult = getVariable(type, e.id(), sTable.get(e));
	}

	@Override
	public void visit(ThisExpr e) {
		lastResult = "%this";
		
	}

	@Override
	public void visit(NewIntArrayExpr e) {
		e.lengthExpr().accept(this);
		String actualLen = newReg();
		emit("	"+actualLen + " = add i32 1, " + lastResult);
		String ptr = newReg();
		emit("	"+ptr + " = call i8* @calloc(i32 " + actualLen + ", i32 4)");
		lastResult = newReg();
		emit("	"+lastResult + " = bitcast i8* " + ptr + " to i32*");
	}

	@Override
	public void visit(NewObjectExpr e) {
		Vtable tempVTable=ClassTable.get(className.get(e.classId()));
		int numMethod=tempVTable.getMethodOffset().size();
		/* need to check if no methods at all? */
		emit("	"+newReg()+" = call i8* @calloc(i32 1, i32 "+tempVTable.getVtableSize()+")");
		emit("	"+newReg()+" = bitcast i8* %_"+(registerCounter-2)+" to i8***");
		emit("	"+newReg()+" = getelementptr ["+numMethod+" x i8*], ["+numMethod+" x i8*]* @."+e.classId()+"_vtable, i32 0, i32 0");
		emit("	Store i8** %_"+(registerCounter-1)+", i8*** %_"+(registerCounter-2));
		
	}

	@Override
	public void visit(NotExpr e) {
		e.e().accept(this);
		String result = newReg();
		emit("	"+result + " = xor i1 1, " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(IntAstType t) {
		lastResult = "i32";
		
	}

	@Override
	public void visit(BoolAstType t) {
		lastResult = "i1";
		
	}

	@Override
	public void visit(IntArrayAstType t) {
		lastResult = "i32*";
		
	}

	@Override
	public void visit(RefType t) {
		lastResult = "i8*";
		
	}
	
	public void emit(String s) {
		emitted.append(s+"\n");
	}
	
	public String newReg() {
		return "%_" + registerCounter++;
	}
	
	public boolean hasMethodName(String name,ClassDecl classDecl) {
		for(MethodDecl methodDecl:classDecl.methoddecls())
		{
			if(methodDecl.name().equals(name))
				return true;
		}
		return false;
	}
	
	/*build Vtable for classDecl*/
	public Vtable BuildVtable(ClassDecl classDecl) {
		
		Vtable vtable=new Vtable();
		if(classDecl.superName()!=null) {
			Vtable parentTable=ClassTable.get(className.get(classDecl.superName()));
			for(MethodDecl methodDecl:parentTable.getMethodOffset().keySet()) {
				if(!hasMethodName(methodDecl.name(),classDecl))
					vtable.addMethod(methodDecl);
			}
			for(VarDecl varDecl:parentTable.getFieldOffset().keySet()) {/*fields can't be "overriden"*/
				vtable.addField(varDecl);
			}
		}
		
		for (MethodDecl methodDecl : classDecl.methoddecls()) {
			vtable.addMethod(methodDecl);
		}
		for (VarDecl field : classDecl.fields()) {
			vtable.addField(field);
		}
		ClassTable.put(classDecl, vtable);
		return vtable;
	}
	
	public void print_helpers_methods() {
		emit("declare i8* @calloc(i32, i32)");
		emit("declare i32 @printf(i8*, ...)");
		emit("declare void @exit(i32)");
		emit("@_cint = constant [4 x i8] c\"%d\\0a\\00\"");
		emit("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"");
		emit("");
		emit("define void @print_int(i32 %i) {");
		emit("	%_str = bitcast [4 x i8]* @_cint to i8*");
		emit("	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)");
		emit("	ret void");
		emit("}\n");
		emit("define void @throw_oob() {");
		emit("	%_str = bitcast [15 x i8]* @_cOOB to i8*");
		emit("	call i32 (i8*, ...) @printf(i8* %_str)");
		emit("	call void @exit(i32 1)");
		emit("	ret void");
		emit("}\n");
	}

}
