package ast;

public class TranslatorVisitor implements Visitor {
	
	public StringBuilder emitted;
	private int ifCounter,whileCounter, registerCounter, andCounter;
	String lastResult; // I am not sure how to represent what Roee suggested, but it is good idea
	
	public TranslatorVisitor() {
		emitted=new StringBuilder();
		this.ifCounter=0; this.whileCounter=0;
		this.registerCounter = 0;
		this.andCounter = 0;
		this.lastResult="";
	}

	@Override
	public void visit(Program program) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ClassDecl classDecl) {
		// TODO Auto-generated method stub
		
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
		
		int tempIf=this.ifCounter+=2;
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

		int tempWhile=this.whileCounter+=2;
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
		e.e1().accept(this);
		emit("br i1 " + lastResult + ", label %and" + andCounter + ", label %and" + (andCounter + 1));
		emit("and" + andCounter + ":");
		e.e2().accept(this);
		emit("br label %and" + (andCounter + 2));
		emit("and" + (andCounter + 1) + ":");
		emit("br label %and" + (andCounter + 2));
		emit("and" + (andCounter + 2) + ":");
		String result = newReg();
		emit(result + " = phi i1 [" + lastResult + ", %and" + andCounter + "], [0, %and" + (andCounter + 1) + "]");
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ArrayLengthExpr e) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NewObjectExpr e) {
		// TODO Auto-generated method stub
		
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

}
