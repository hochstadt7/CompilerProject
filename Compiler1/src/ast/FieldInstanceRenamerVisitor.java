package ast;

public class FieldInstanceRenamerVisitor extends OffspringFinderVisitor {
	public String origVarName, newVarName;
	private VariableInstanceRenamer varRen;
	
	public FieldInstanceRenamerVisitor(int _initial, String _origVarName, String _newVarName) {
		super(_initial);
		varRen = new VariableInstanceRenamer(_origVarName, _newVarName, 1);
	}
	
	public void visit(MethodDecl methodDecl) {
		methodDecl.accept(varRen);
	}
}
