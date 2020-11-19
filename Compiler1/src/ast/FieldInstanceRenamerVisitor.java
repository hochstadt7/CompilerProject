package ast;

public class FieldInstanceRenamerVisitor extends OffspringFinderVisitor {
	public String origVarName, newVarName;
	private VariableInstanceRenamer varRen;
	
	public FieldInstanceRenamerVisitor(int _initial, String _origVarName, String _newVarName, int linenumber) {
		super(_initial);
		varRen = new VariableInstanceRenamer(_origVarName, _newVarName, 1, linenumber);
	}
	
	public void visit(MethodDecl methodDecl) {
		methodDecl.accept(varRen);
	}
}
