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
	
	public SymbolVars lookupVars(String name) {
		SymbolVars res = (SymbolVars) entries.get(name);
		if (res != null) {
			return res;
		}
		if (parent == null) {
			return null;
		}
		return parent.lookupVars(name);
	}
	//used to check if a given class in an ancestor of the class represented by the symboltable
	public boolean IsDaughterClass(String ancestorType) {
		String res = entries.get("this").getType();
		if (res != null) {
			if(res.equals(ancestorType))
				return true;
		}
		if (parent == null) {
			return false;
		}
		return parent.IsDaughterClass(ancestorType);
	}
	public SymbolMethods lookupMethods(String name) {
		SymbolMethods res = (SymbolMethods) entries.get(name);
		if (res != null) {
			return res;
		}
		if (parent == null) {
			return null;
		}
		return parent.lookupMethods(name);
	}
	public SymbolMethods lookupMethodOverride(String name) {
		if (parent == null) {
			return null;
		}
		return parent.lookupMethods(name);
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
