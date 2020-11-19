package ast;

import java.util.HashSet;
import java.util.HashMap;

public class MethodInstanceRenamer implements Visitor {
	private String origMethodName, newMethodName;
	public HashSet<String> OffspringNames;
	private HashMap<AstNode,SymbolTable> sTable;
	
	public MethodInstanceRenamer(String _origMethodName, String _newMethodName, HashMap<AstNode,SymbolTable> _sTable, HashSet<String> _OffspringNames) {
		origMethodName = _origMethodName;
		newMethodName = _newMethodName;
		OffspringNames = _OffspringNames;
		sTable = _sTable;
		
	}

	@Override
	public void visit(Program program) {
		program.mainClass().accept(this);
		for (ClassDecl classdecl : program.classDecls())
            classdecl.accept(this);
	}

	@Override
	public void visit(ClassDecl classDecl) {
		for (MethodDecl methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
		}
	}

	@Override
	public void visit(MainClass mainClass) {
		mainClass.mainStatement().accept(this);

	}

	@Override
	public void visit(MethodDecl methodDecl) {

        for (Statement stmt : methodDecl.body()) {
            stmt.accept(this);
        }
        methodDecl.ret().accept(this);
	}

	@Override
	public void visit(FormalArg formalArg) {
		// funcs can't be called at decl

	}

	@Override
	public void visit(VarDecl varDecl) {
		// funcs can't be called at decl

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

	}

	@Override
	public void visit(AssignArrayStatement assignArrayStatement) {
		assignArrayStatement.rv().accept(this);
		assignArrayStatement.index().accept(this);

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
		String type = "";
		
		if(origMethodName.equals(e.methodId()))//Rename Instance
		{
			if(e.ownerExpr() instanceof ThisExpr)
				type = sTable.get(e.ownerExpr()).lookup("this").getType();
			else if(e.ownerExpr() instanceof NewObjectExpr)
				type = sTable.get(e.ownerExpr()).lookup(((NewObjectExpr) e.ownerExpr()).classId()).getType();
			else if(e.ownerExpr() instanceof IdentifierExpr)
				type = sTable.get(e.ownerExpr()).lookup(((IdentifierExpr) e.ownerExpr()).id()).getType();	
			if(OffspringNames.contains(type))
				e.setMethodId(this.newMethodName);
		}
		for(Expr arg: e.actuals())//check args
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
		// TODO Auto-generated method stub

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
