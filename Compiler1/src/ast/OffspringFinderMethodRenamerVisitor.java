package ast;

public class OffspringFinderMethodRenamerVisitor extends OffspringFinderVisitor {
	public String origMethodName, newMethodName;
	
	public OffspringFinderMethodRenamerVisitor(int _initial, String _origMethodName, String _newMethodName) {
		super(_initial);
		origMethodName = _origMethodName;
		newMethodName = _newMethodName;
	}

	@Override
	public void visit(MethodDecl methodDecl) {
		if (methodDecl.name().equals(origMethodName)) {
			methodDecl.setName(newMethodName);
		}
	}
}
