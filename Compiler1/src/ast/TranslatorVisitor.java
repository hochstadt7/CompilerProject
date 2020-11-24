package ast;

public class TranslatorVisitor implements Visitor {
	
	public StringBuilder emitted;
	private int ifCounter,whileCounter;
	String lastRegist; // I am not sure how to represent what Roey suggested, but it is good idea
	
	public TranslatorVisitor() {
		emitted=new StringBuilder();
		this.ifCounter=0; this.whileCounter=0;
		this.lastRegist="";
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
		emit("br i1 "+lastRegist+" ,"+ "label %if"+tempIf+", label %if"+tempIf+1);
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
		emit("br i1 "+lastRegist+" ,"+ "label %loop"+tempWhile+1+", label %loop"+tempWhile+2);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IntegerLiteralExpr e) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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

}
