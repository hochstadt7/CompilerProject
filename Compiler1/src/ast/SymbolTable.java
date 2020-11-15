package ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
	
	public Symbol lookup(String name) {
		Symbol res = entries.get(name);
		if (res != null) {
			return res;
		}
		if (parent == null) {
			return null;
		}
		return parent.lookup(name);
	}
@Override
public String toString() {
		
		System.out.println("Symbol table content:\n");
		Set<Entry<String,Symbol>> symbolContent=this.entries.entrySet();
		for (Map.Entry<String, Symbol> it: symbolContent) {
			System.out.println("My name is: "+it.getKey()+" and my type:\n");
			System.out.println(it.getValue());
		}
		
		return "";
	}

}
