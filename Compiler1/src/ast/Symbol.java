package ast;

public class Symbol {
	
	private String type;
	
	public Symbol(String type) {
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
