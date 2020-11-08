package ast;

import javax.xml.bind.annotation.XmlElement;

public abstract class AstNode {
    @XmlElement(required = false)
    public Integer lineNumber;
    //private SymbolTable symbolTable;

    public AstNode() {
        lineNumber = null;
    }

    public AstNode(int lineNumber) {
        this.lineNumber = lineNumber;
       // this.symbolTable=null;
    }
    
    /*public void setSymbolTable(SymbolTable symbolTable) {
    	this.symbolTable=symbolTable;
    }
    
      public SymbolTable getSymbolTable() {
    	return this.symbolTable;
    } */

    abstract public void accept(Visitor v);
}
