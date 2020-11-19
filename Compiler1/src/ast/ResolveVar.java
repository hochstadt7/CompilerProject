package ast;

public class ResolveVar implements Visitor {
	
	public int lineNumber;
	public String name;
	public String newName;
	public int classIndex;
	public int methodIndex; // -1 => field
	
	private boolean found = false;
	
	
	public ResolveVar(int linenumber, String name, String newName) {	
		this.lineNumber=linenumber;
		this.name=name;
		this.newName = newName;
	}

	@Override
	public void visit(Program program) {
		
		for (classIndex = 0; classIndex < program.classDecls().size(); classIndex++) {
			program.classDecls().get(classIndex).accept(this);
			if (found) {
				return;
			}
        }
		this.classIndex=-1;/*in case not found*/
	}

	@Override
	public void visit(ClassDecl classDecl) {
		for (VarDecl varDecl : classDecl.fields()) {
			if (varDecl.lineNumber == lineNumber && varDecl.name().equals(name)) {
				varDecl.setName(newName);
				found = true;
				methodIndex = -1;
				return;
			}
		}
		for (methodIndex = 0; methodIndex < classDecl.methoddecls().size(); methodIndex++) {
			classDecl.methoddecls().get(methodIndex).accept(this);
			if (found) {
				return;
			}
		}
	}

	@Override
	public void visit(MainClass mainClass) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MethodDecl methodDecl) {
		for (FormalArg formalArg : methodDecl.formals()) {
			if (formalArg.lineNumber == lineNumber && formalArg.name().equals(name)) {
				found = true;
				return;
			}
		}
		for (VarDecl varDecl : methodDecl.vardecls()) {
			if (varDecl.lineNumber == lineNumber && varDecl.name().equals(name)) {
				found = true;
				return;
			}
		}
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

}
