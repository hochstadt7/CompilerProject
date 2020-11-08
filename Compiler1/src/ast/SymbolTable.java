package ast;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	
	private Map<String,Symbol> entries;
	private SymbolTable parent;
	
	public SymbolTable() {
		this.entries=new HashMap<String,Symbol>();
		this.parent=null;
	}
	
	public Map<String,Symbol> getEnteries() {
		return this.entries;
	}
	
	public void addEntery(String key,Symbol enterie) {
		this.entries.put(key, enterie);
	}
	
	public boolean hasEntery(String key) {
		return this.entries.containsKey(key);
	}
	
	public Symbol getEntry(String key) {
		  return this.entries.get(key);
	  }
	
	public SymbolTable getParentSymbolTable() {
		  return this.parent;
	 }
	
	public void setParentSymbolTable(SymbolTable parentSymbolTable) {
		  this.parent = parentSymbolTable;
	  }

}
