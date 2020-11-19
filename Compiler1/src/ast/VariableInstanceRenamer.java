package ast;


public class VariableInstanceRenamer implements Visitor {
	private String origVarName, newVarName;//old and new names of the var
	private int external; //is the var declared in or out of the method (1=external)
	private int line_found = -1; //used to check the line the decl was found in
	
	private int line_number; //linenumber which was passed as arg

	
	public VariableInstanceRenamer(String _origVarName, String _newVarName, int _external, int _line_number) {
		origVarName = _origVarName;
		newVarName = _newVarName;	
		external = _external;
		line_number = _line_number;
	}

	@Override
	public void visit(Program program) {
		
	}

	@Override
	public void visit(ClassDecl classDecl) {
		
	}

	@Override
	public void visit(MainClass mainClass) {

	}

	@Override
	public void visit(MethodDecl methodDecl) {
		//check if the var is declared as a formal or var
        for (FormalArg formal : methodDecl.formals()) {
            formal.accept(this);
        }
        for (VarDecl varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }
        //check if the var was overridden from outside or not of the scope
        if(((external == 0) && line_found == line_number) || ((external == 1) && line_found == -1))
	        for (Statement stmt : methodDecl.body()) {
	            stmt.accept(this);
	        }
        methodDecl.ret().accept(this);
	}

	@Override
	public void visit(FormalArg formalArg) {
		if(formalArg.name().equals(origVarName))//var is formal
		{
			line_found = formalArg.lineNumber;
			if(external == 0 && line_found == line_number)
				formalArg.setName(newVarName);
		}

	}

	@Override
	public void visit(VarDecl varDecl) {
		if(varDecl.name().equals(origVarName))//var declared inside
		{
			line_found = varDecl.lineNumber;
			if(external == 0 && line_found == line_number)
				varDecl.setName(newVarName);
		}

	}

	@Override
	public void visit(BlockStatement blockStatement) {
		for (Statement s : blockStatement.statements()) {
			s.accept(this);
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
		if(assignStatement.lv() == origVarName)
			assignStatement.setLv(newVarName);

	}

	@Override
	public void visit(AssignArrayStatement assignArrayStatement) {
		assignArrayStatement.rv().accept(this);
		if(assignArrayStatement.lv() == origVarName)
			assignArrayStatement.setLv(newVarName);
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
		for(Expr arg: e.actuals())
		{
			arg.accept(this);
		}

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
		if(e.id() == origVarName)
			e.setId(newVarName);

	}

	@Override
	public void visit(ThisExpr e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NewIntArrayExpr e) {
		e.lengthExpr().accept(this);

	}

	@Override
	public void visit(NewObjectExpr e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NotExpr e) {
		e.e().accept(this);

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

}