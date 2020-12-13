package ast;

import java.util.HashMap;
import java.util.Map;

public class SemanticCheck implements BoolVisitor {

	private HashMap<AstNode,SymbolTable> VarTable;
	private HashMap<AstNode,SymbolTable> methTable;
	
	public SemanticCheck(SymbolTableBuilder symbolTableBuilder) {
		
		this.VarTable=symbolTableBuilder.myVariables;
		this.methTable=symbolTableBuilder.myMethods;
	}

	@Override
	public boolean visit(Program program) {
		Map<String,ClassDecl> className=new HashMap<String,ClassDecl>();
		for(ClassDecl classDecl:program.classDecls()) {
			String myName=classDecl.name();
			String parentName=classDecl.superName();
			if(className.containsKey(myName)||(parentName!=null && !(className.containsKey(parentName))))
				return false;
			
				
		}
		return false;
	}

	@Override
	public boolean visit(ClassDecl classDecl) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(MainClass mainClass) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(MethodDecl methodDecl) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(FormalArg formalArg) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(VarDecl varDecl) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(BlockStatement blockStatement) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(IfStatement ifStatement) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(WhileStatement whileStatement) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(SysoutStatement sysoutStatement) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(AssignStatement assignStatement) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(AssignArrayStatement assignArrayStatement) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(AndExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(LtExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(AddExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(SubtractExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(MultExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(ArrayAccessExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(ArrayLengthExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(MethodCallExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(IntegerLiteralExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(TrueExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(FalseExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(IdentifierExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(ThisExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(NewIntArrayExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(NewObjectExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(NotExpr e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(IntAstType t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(BoolAstType t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(IntArrayAstType t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(RefType t) {
		// TODO Auto-generated method stub
		return false;
	}

}	