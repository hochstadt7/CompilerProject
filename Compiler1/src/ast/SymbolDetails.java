package ast;

public class SymbolDetails {
	
	private String type;
	private boolean isField;
	public SymbolDetails(String type) {
	    this.type = type;
	    isField = false;
	}
	public SymbolDetails(String type, boolean isField) {
	    this.type = type;
	    this.isField = isField;
	}
	
	public String getType() {
		return this.type;
	}
	public boolean getIsField() {
		return this.isField;
	}
	
	@Override
	public String toString() {
			
			System.out.println(" "+this.type+" ");
			
			return "";
		}
	

}
