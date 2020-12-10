package ast;

import java.util.HashMap;
import java.util.Map;

public class Vtable {

	private Map<MethodDecl, Integer> methodOffset;
	private Map<VarDecl, Integer> fieldOffset;
	private Map<Integer,VarDecl> fieldOrderInsertion;//new field
	private int VtableSize,order;
	
	public Vtable() {
		methodOffset=new HashMap<MethodDecl,Integer>();
		fieldOffset=new HashMap<VarDecl,Integer>();
		fieldOrderInsertion=new HashMap<Integer,VarDecl>();
		VtableSize=8; /* first 8 bytes for Vtable pointer */
		order=0;
	}
	
	/* the offset for the pointer of vtable is 0*/
	void addMethod(MethodDecl methodDecl) {
		methodOffset.put(methodDecl, methodOffset.size());
	}
	
	void addField(VarDecl varDecl) {
		
		int lastOffset=figureOffset(varDecl);
		fieldOffset.put(varDecl, VtableSize);
		VtableSize+=lastOffset;
		fieldOrderInsertion.put(order,varDecl);
		order++;
		
	}
	
	public Map<MethodDecl, Integer> getMethodOffset(){
		return this.methodOffset;
	}
	
	public Map<VarDecl, Integer> getFieldOffset(){
		return this.fieldOffset;
	}
	
	public Map<Integer,VarDecl> getFieldOrder(){
		return this.fieldOrderInsertion;
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
