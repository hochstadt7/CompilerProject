package ast;

public interface BoolVisitor {
	public boolean visit(Program program);
    public boolean visit(ClassDecl classDecl);
    public boolean visit(MainClass mainClass);
    public boolean visit(MethodDecl methodDecl);
    public boolean visit(FormalArg formalArg);
    public boolean visit(VarDecl varDecl);

    public boolean visit(BlockStatement blockStatement);
    public boolean visit(IfStatement ifStatement);
    public boolean visit(WhileStatement whileStatement);
    public boolean visit(SysoutStatement sysoutStatement);
    public boolean visit(AssignStatement assignStatement);
    public boolean visit(AssignArrayStatement assignArrayStatement);

    public boolean visit(AndExpr e);
    public boolean visit(LtExpr e);
    public boolean visit(AddExpr e);
    public boolean visit(SubtractExpr e);
    public boolean visit(MultExpr e);
    public boolean visit(ArrayAccessExpr e);
    public boolean visit(ArrayLengthExpr e);
    public boolean visit(MethodCallExpr e);
    public boolean visit(IntegerLiteralExpr e);
    public boolean visit(TrueExpr e);
    public boolean visit(FalseExpr e);
    public boolean visit(IdentifierExpr e);
    public boolean visit(ThisExpr e);
    public boolean visit(NewIntArrayExpr e);
    public boolean visit(NewObjectExpr e);
    public boolean visit(NotExpr e);

    public boolean visit(IntAstType t);
    public boolean visit(BoolAstType t);
    public boolean visit(IntArrayAstType t);
    public boolean visit(RefType t);
}
