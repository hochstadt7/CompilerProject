package ast;

public class SymbolVars extends SymbolDetails {

	private boolean isField;
	public SymbolVars(String type, boolean isField) {
		super(type);
		this.setField(isField);
	}
	public boolean isField() {
		return isField;
	}
	public void setField(boolean isField) {
		this.isField = isField;
	}

}
