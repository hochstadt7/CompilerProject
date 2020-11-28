package ast;

import java.util.HashMap;
import java.util.Map;

public class Vtable {

	Map<MethodDecl, Integer> methodOffset;
	Map<VarDecl, Integer> fieldOffset;
	
	public Vtable() {
		methodOffset=new HashMap<MethodDecl,Integer>();
		fieldOffset=new HashMap<VarDecl,Integer>();
	}
	
	/* the offset for the pointer of vtable is 0*/
	int addMethod(MethodDecl methodDecl) {
		if(!methodOffset.containsKey(methodDecl))
			methodOffset.put(methodDecl, methodOffset.size());
		return methodOffset.get(methodDecl);
	}
	
	int addField(VarDecl varDecl) {
		if(!fieldOffset.containsKey(varDecl))
			fieldOffset.put(varDecl, fieldOffset.size()*4+8);/* in the llvm, the offset is the value*4+8 (8 bytes for vtable pointer, 4 for a field)*/
		return fieldOffset.get(varDecl);
	}
	
	public Map<MethodDecl, Integer> getMethodOffset(){
		return this.methodOffset;
	}
	
	public Map<VarDecl, Integer> getFieldOffset(){
		return this.fieldOffset;
	}
}
