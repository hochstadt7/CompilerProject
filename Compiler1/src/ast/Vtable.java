package ast;

import java.util.HashMap;
import java.util.Map;

public class Vtable {

	private Map<MethodDecl, Integer> methodOffset;
	private Map<VarDecl, Integer> fieldOffset;
	private int VtableSize;
	
	public Vtable() {
		methodOffset=new HashMap<MethodDecl,Integer>();
		fieldOffset=new HashMap<VarDecl,Integer>();
		VtableSize=0;
	}
	
	/* the offset for the pointer of vtable is 0*/
	void addMethod(MethodDecl methodDecl) {
		methodOffset.put(methodDecl, methodOffset.size());
	}
	
	void addField(VarDecl varDecl) {
		
		int offset=figureOffset(varDecl);
		VtableSize+=offset;
		fieldOffset.put(varDecl, VtableSize);
		
	}
	
	public Map<MethodDecl, Integer> getMethodOffset(){
		return this.methodOffset;
	}
	
	public Map<VarDecl, Integer> getFieldOffset(){
		return this.fieldOffset;
	}
	
	public int getVtableSize(){
		return this.VtableSize;
	}
	
	public int figureOffset(VarDecl varDecl) {
		
		if (varDecl.type() instanceof IntAstType)
			return 4;
		else if(varDecl.type() instanceof BoolAstType)
			return 1;
		else
			return 8;
	}
}
