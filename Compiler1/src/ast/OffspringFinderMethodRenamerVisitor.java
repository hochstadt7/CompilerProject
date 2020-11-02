package ast;

import java.util.HashSet;

public class OffspringFinderMethodRenamerVisitor implements Visitor {
	public int initial;
	public String origMethodName, newMethodName;
	public HashSet<String> OffspringNames;
	private boolean first;
	
	public OffspringFinderMethodRenamerVisitor(int _initial, String _origMethodName, String _newMethodName) {
		initial = _initial;
		origMethodName = _origMethodName;
		newMethodName = _newMethodName;
		OffspringNames = new HashSet<String>();
		first = true;
	}

	@Override
	public void visit(Program program) {
		first = true;
		for (int i = initial; i < program.classDecls().size(); i++) {
			program.classDecls().get(i).accept(this);
		}
	}

	@Override
	public void visit(ClassDecl classDecl) {
		String fatherName = classDecl.superName();
		if (first || (fatherName != null && OffspringNames.contains(fatherName))) {
			OffspringNames.add(classDecl.name());
			for (MethodDecl decl : classDecl.methoddecls()) {
				decl.accept(this);
			}
		}
	}

	@Override
	public void visit(MainClass mainClass) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MethodDecl methodDecl) {
		if (methodDecl.name().equals(origMethodName)) {
			methodDecl.setName(newMethodName);
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
