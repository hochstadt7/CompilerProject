package ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SymbolTable {
	
	private Map<String,SymbolDetails> entries;
	private SymbolTable parent;
	
	public SymbolTable() {
		this.entries=new HashMap<String,SymbolDetails>();
		this.parent=null;
	}
	
	public Map<String,SymbolDetails> getEnteries() {
		return this.entries;
	}
	
	public void addEntery(String key,SymbolDetails enterie) {
		this.entries.put(key, enterie);
	}
	
	public boolean hasEntery(String key) {
		return this.entries.containsKey(key);
	}
	
	public SymbolDetails getEntry(String key) {
		  return this.entries.get(key);
	  }
	
	public SymbolTable getParentSymbolTable() {
		  return this.parent;
	 }
	
	public void setParentSymbolTable(SymbolTable parentSymbolTable) {
		  this.parent = parentSymbolTable;
	  }
	
	public SymbolDetails lookup(String name) {
		SymbolDetails res = entries.get(name);
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
		Set<Entry<String,SymbolDetails>> symbolContent=this.entries.entrySet();
		for (Map.Entry<String, SymbolDetails> it: symbolContent) {
			System.out.println("My name is: "+it.getKey()+" and my type:\n");
			System.out.println(it.getValue());
		}
		
		return "";
	}

}
