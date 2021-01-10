
import java_cup.runtime.*;

%%
%class Lexer
%line
%column
%cup

%{
	/* code from example- recitation 11 */
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}
  
	public int getLine()    { return yyline + 1; }
	public int getCharPos() { return yycolumn;   }
	
%}


LineTerminator	= \r|\n|\r\n
WhiteSpace		= [\t ] | {LineTerminator}
INTEGER			= 0 | [1-9][0-9]*
Letters			= [a-zA-Z]
Identifier	 	= {Letters}({Letters} | {INTEGER} |_)*

%state COMMENT1
%state COMMENT2

%%


/* flow control */
<YYINITIAL>{
<<EOF>> { 
		if (yystate() == COMMENT2){ // comment wasn't closed
				System.out.println("Syntax error at line "+yyline+" of input.");
				System.exit(1);
			}
		else{
			return symbol(sym.EOF,"EOF");} 
		}
 "if" { System.out.println("if"); return symbol(sym.IF); }
 "else" {System.out.println("else"); return symbol(sym.ELSE); }
 "while" {System.out.println("while"); return symbol(sym.WHILE); } 
 "true" {System.out.println("true"); return symbol(sym.TRUE); } 
 "false" {System.out.println("false"); return symbol(sym.FALSE); }
 "extends" {System.out.println("extends"); return symbol(sym.EXTENDS); }
 "System.out.println" {System.out.println("syso"); return symbol(sym.SYSO); }
 "length" {System.out.println("length"); return symbol(sym.LENGTH); } 
 "new" {System.out.println("new"); return  symbol(sym.NEW); } 
 "static" {System.out.println("static"); return  symbol(sym.STATIC); }
 "return" {System.out.println("return"); return symbol(sym.RETURN); }
 "public" {System.out.println("public"); return symbol(sym.PUBLIC); }
 "main" {System.out.println("main"); return symbol(sym.MAIN); }
 "class" {System.out.println("class"); return symbol(sym.CLASS); }

/* types */
 "this" {System.out.println("this"); return symbol(sym.THIS); }
 "int"  {System.out.println("int"); return symbol(sym.INT); }
 "boolean" {System.out.println("boolean"); return symbol(sym.BOOLEAN); }
 "String"  {System.out.println("string"); return symbol(sym.STRING); }
 "void" {System.out.println("void"); return symbol(sym.VOID); }

/* punctuations */
 ";" {System.out.println("semicolon"); return symbol(sym.SEMICOLON); }
 "," {System.out.println("comma"); return symbol(sym.COMMA); }
 "." {System.out.println("dot"); return symbol(sym.DOT); }

/* brackets */
 "(" {System.out.println("lp"); return symbol(sym.LP); }
 ")" {System.out.println("rp"); return symbol(sym.RP); }
 "[" {System.out.println("lc"); return symbol(sym.LC); }
 "]" {System.out.println("rc"); return symbol(sym.RC); }
 "{" {System.out.println("lb"); return symbol(sym.LB); }
 "}" {System.out.println("rb"); return symbol(sym.RB); }

/* operators */
 "=" {System.out.println("equal"); return symbol(sym.EQUAL); }
 "<" {System.out.println("lt"); return symbol(sym.LT); }
 "!" {System.out.println("neg"); return symbol(sym.NEG); }
 "&&" {System.out.println("and"); return symbol(sym.AND); }
 "*" {System.out.println("mult"); return symbol(sym.MULT); }
 "+" {System.out.println("plus"); return symbol(sym.PLUS); }
 "-" {System.out.println("minus"); return symbol(sym.MINUS); }
 
 /* my macros */
 {WhiteSpace} {}
 {INTEGER} {System.out.println("integer"); return symbol(sym.NUMBER, Integer.parseInt(yytext()));}
 {Identifier} {System.out.println("identifier"); return symbol(sym.IDENTIFIER, new String(yytext()));}
 
 /* comments */
 "//" { yybegin(COMMENT1); }
 "/*" { yybegin(COMMENT2); }
 }
 

 <COMMENT1> {
  [\n]			  		 { yybegin(YYINITIAL); }
  [^] 							 { }
}

 <COMMENT2> {
  "*/" 						 { yybegin(YYINITIAL); }
  [^]						 {}
}


	