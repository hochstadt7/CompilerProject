package ast;

import java.util.List;

public class SymbolDetails {
	
	private String type;
	private boolean isField;
	private List<String> parameters;
	public SymbolDetails(String type) {
	    this.type = type;
	    isField = false;
	}
	public SymbolDetails(String type, boolean isField) {
	    this.type = type;
	    this.isField = isField;
	}
	
	public SymbolDetails(String type, List<String> params) {
	    this.type = type;
	    this.parameters=params;
	}
	public List<String> getParams() {
		return this.parameters;
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
