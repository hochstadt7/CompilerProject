package ast;

import java.util.HashMap;
import java.util.Map;

public class TranslatorVisitor implements Visitor {
	
	public StringBuilder emitted;
	private int ifCounter,whileCounter, registerCounter, andCounter, arrayCounter;
	String lastResult; // I am not sure how to represent what Roee suggested, but it is good idea
	Map<ClassDecl, Vtable> ClassTable; /*classes and their Vtable*/
	Map<String,ClassDecl> className;
	
	public TranslatorVisitor() {
		emitted=new StringBuilder();
		this.ifCounter=0; this.whileCounter=0;
		this.registerCounter = 0;
		this.andCounter = 0;
		arrayCounter = 0;
		this.lastResult="";
		ClassTable=new HashMap<ClassDecl, Vtable>(); 
		className=new HashMap<String,ClassDecl>();
	}

	@Override
	public void visit(Program program) {
		
		for (ClassDecl classDecl : program.classDecls())
			className.put(classDecl.name(), classDecl);
		
		for (ClassDecl classDecl : program.classDecls()) {
			
			BuildVtable(classDecl);
			Vtable myTable=ClassTable.get(classDecl);
			String prefix="@."+classDecl.name()+"_vtable = global ["+ myTable.getMethodOffset().size()+" x i8*] ";
			StringBuilder sufix=new StringBuilder();
			for (MethodDecl methodDecl:myTable.getMethodOffset().keySet()) {
				sufix.append("[i8* bitcast (i32 (i8*, i32)* @"+classDecl.name()+"."+methodDecl.name()+" to i8*), ");
			}
			
			sufix.setLength(sufix.length()-2);
			sufix.append("]");
			emit(prefix+sufix.toString());
	}
		print_helpers_methods();
		for (ClassDecl classDecl : program.classDecls()) {
			classDecl.accept(this);
		}
		
	}

	@Override
	public void visit(ClassDecl classDecl) {
		
	}

	@Override
	public void visit(MainClass mainClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MethodDecl methodDecl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(FormalArg formalArg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(VarDecl varDecl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BlockStatement blockStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IfStatement ifStatement) {
		int tempIf=this.ifCounter;
		this.ifCounter+=3;
		ifStatement.cond().accept(this);
		emit("br i1 "+lastResult+" ,"+ "label %if"+tempIf+", label %if"+tempIf+1);
		emit("if"+tempIf+":");
		ifStatement.thencase().accept(this);
		emit("br label %if"+tempIf+2);
		emit("if"+tempIf+1+":");
		ifStatement.elsecase().accept(this);
		emit("br label %if"+tempIf+2);
		emit("if"+tempIf+2+":");
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		int tempWhile=this.whileCounter;
		this.whileCounter+=3;
		emit("br label %loop"+tempWhile);
		whileStatement.cond().accept(this);
		emit("br i1 "+lastResult+" ,"+ "label %loop"+tempWhile+1+", label %loop"+tempWhile+2);
		emit("loop"+tempWhile+1+":");
		whileStatement.body().accept(this);
		emit("br label %loop"+tempWhile+2);
		emit("loop"+tempWhile+2+":");
	}

	@Override
	public void visit(SysoutStatement sysoutStatement) {
		sysoutStatement.arg().accept(this);
		emit("call void (i32) @print_int(i32 "+lastResult+")");
		
	}

	@Override
	public void visit(AssignStatement assignStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AssignArrayStatement assignArrayStatement) {
		// TODO Auto-generated method stub
		// Roee: I need to see hew we handle AssignStatement to write this.
	}

	@Override
	public void visit(AndExpr e) {
		int tempAnd=this.andCounter;
		this.andCounter+=3;
		e.e1().accept(this);
		emit("br i1 " + lastResult + ", label %and" + tempAnd + ", label %and" + (tempAnd + 1));
		emit("and" + tempAnd + ":");
		e.e2().accept(this);
		emit("br label %and" + (tempAnd + 2));
		emit("and" + (tempAnd + 1) + ":");
		emit("br label %and" + (tempAnd + 2));
		emit("and" + (tempAnd + 2) + ":");
		String result = newReg();
		emit(result + " = phi i1 [" + lastResult + ", %and" + tempAnd + "], [0, %and" + (tempAnd + 1) + "]");
		lastResult = result;
	}

	@Override
	public void visit(LtExpr e) {
		e.e1().accept(this);
		String firstArg = lastResult;
		e.e2().accept(this);
		String result = newReg();
		emit(result + " = icmp slt i32 " + firstArg + ", " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(AddExpr e) {
		e.e1().accept(this);
		String firstArg = lastResult;
		e.e2().accept(this);
		String result = newReg();
		emit(result + " = add i32 " + firstArg + ", " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(SubtractExpr e) {
		e.e1().accept(this);
		String firstArg = lastResult;
		e.e2().accept(this);
		String result = newReg();
		emit(result + " = sub i32 " + firstArg + ", " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(MultExpr e) {
		e.e1().accept(this);
		String firstArg = lastResult;
		e.e2().accept(this);
		String result = newReg();
		emit(result + " = mul i32 " + firstArg + ", " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(ArrayAccessExpr e) {
		e.arrayExpr().accept(this);
		String arr = lastResult;
		e.indexExpr().accept(this);
		String length = newReg();
		emit(length + " = load i32, i32* " + arr);
		String nonnegative = newReg();
		emit(nonnegative + " = icmp sle i32 0, " + lastResult);
		// There is no possibility of nesting here so no temp is needed.
		emit("br i1 " + nonnegative + ", label %arr" + (arrayCounter + 1) + ", label %arr" + arrayCounter);
		emit("arr" + arrayCounter + ":");
		emit("call void @throw_oob()");
		//emit("br label %arr" + (arrayCounter + 1)); // Do we really need this?
		emit("arr" + (arrayCounter + 1) + ":");
		arrayCounter += 2; // Cause I feel like it. Also, I will never need these labels again so why not?
		String small = newReg();
		emit(small + " = icmp sgt i32 " + length + ", " + lastResult);
		emit("br i1 " + small + ", label %arr" + (arrayCounter + 1) + ", label %arr" + arrayCounter);
		emit("arr" + arrayCounter + ":");
		emit("call void @throw_oob()");
		//emit("br label %arr" + (arrayCounter + 1)); // Do we really need this?
		emit("arr" + (arrayCounter + 1) + ":");
		String ahYesArraysActuallyStartAt1 = newReg();
		emit(ahYesArraysActuallyStartAt1 + " = add i32 1, " + lastResult);
		lastResult = newReg();
		emit(lastResult + " = getelementptr i32, i32* " + arr + ", i32 " + ahYesArraysActuallyStartAt1);
	}

	@Override
	public void visit(ArrayLengthExpr e) {
		e.arrayExpr().accept(this);
		String res = newReg();
		emit(res + " = load i32, i32* " + lastResult);
		lastResult = res;
	}

	@Override
	public void visit(MethodCallExpr e) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ThisExpr e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NewIntArrayExpr e) {
		e.lengthExpr().accept(this);
		String actualLen = newReg();
		emit(actualLen + " = add i32 1, " + lastResult);
		String ptr = newReg();
		emit(ptr + " = call i8* @calloc(i32 " + actualLen + ", i32 4)");
		lastResult = newReg();
		emit(lastResult + " = bitcast i8* " + ptr + " to i32*");
	}

	@Override
	public void visit(NewObjectExpr e) {
		Vtable tempVTable=ClassTable.get(className.get(e.classId()));/* e.classId returns the right class?*/
		int numMethod=tempVTable.getMethodOffset().size();
		emit(newReg()+" = call i8* @calloc(i32 1, i32 "+tempVTable.getVtableSize()+")");
		emit(newReg()+" = bitcast i8* %_"+(registerCounter-1)+" to i8***");
		emit(newReg()+" = getelementptr ["+numMethod+" x i8*], ["+numMethod+" x i8*]* @."+e.classId()+"_vtable, i32 0, i32 0");
		emit("Store i8** %_"+(registerCounter-1)+", i8*** %_"+(registerCounter-2));
		emit("Store i8* %_"+(registerCounter-3)+", i8** %"+lastResult);
	}

	@Override
	public void visit(NotExpr e) {
		e.e().accept(this);
		String result = newReg();
		emit(result + " = xor i1 1, " + lastResult);
		lastResult = result;
	}

	@Override
	public void visit(IntAstType t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BoolAstType t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IntArrayAstType t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RefType t) {
		// TODO Auto-generated method stub
		
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
	public void BuildVtable(ClassDecl classDecl) {
		
		Vtable vtable=new Vtable();
		if(classDecl.superName()!=null) {
			Vtable parentTable=ClassTable.get(className.get(classDecl.superName()));
			for(MethodDecl methodDecl:parentTable.getMethodOffset().keySet()) {
				if(!hasMethodName(methodDecl.name(),classDecl)) /* add methods from parent's Vtable that not appear in our class. I am not sure this is enough check*/
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
	}
	
	public void print_helpers_methods() {
		emit("declare i8* @calloc(i32, i32)");
		emit("declare i32 @printf(i8*, ...)");
		emit("declare void @exit(i32)");
		emit("@_cint = constant [4 x i8] c\"%d\\0a\\00\"");
		emit("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"");
		emit("define void @print_int(i32 %i) {");
		emit("%_str = bitcast [4 x i8]* @_cint to i8*");
		emit("call i32 (i8*, ...) @printf(i8* %_str, i32 %i)");
		emit("ret void");
		emit("}");
		emit("define void @throw_oob() {");
		emit("%_str = bitcast [15 x i8]* @_cOOB to i8*");
		emit("call i32 (i8*, ...) @printf(i8* %_str)");
		emit("call void @exit(i32 1)");
		emit("ret void");
		emit("}");
	}

}
