package ast;

public class SymbolDetails {
	
	private String type;
	
	public SymbolDetails(String type) {
	    this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
			
			System.out.println(" "+this.type+" ");
			
			return "";
		}
	

}
