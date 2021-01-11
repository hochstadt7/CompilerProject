
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
 "if" { return symbol(sym.IF); }
 "else" { return symbol(sym.ELSE); }
 "while" { return symbol(sym.WHILE); } 
 "true" { return symbol(sym.TRUE); } 
 "false" { return symbol(sym.FALSE); }
 "extends" { return symbol(sym.EXTENDS); }
 "System.out.println" { return symbol(sym.SYSO); }
 "length" { return symbol(sym.LENGTH); } 
 "new" { return  symbol(sym.NEW); } 
 "static" { return  symbol(sym.STATIC); }
 "return" { return symbol(sym.RETURN); }
 "public" { return symbol(sym.PUBLIC); }
 "main" { return symbol(sym.MAIN); }
 "class" { return symbol(sym.CLASS); }

/* types */
 "this" { return symbol(sym.THIS); }
 "int"  { return symbol(sym.INT); }
 "boolean" { return symbol(sym.BOOLEAN); }
 "String"  { return symbol(sym.STRING); }
 "void" { return symbol(sym.VOID); }

/* punctuations */
 ";" { return symbol(sym.SEMICOLON); }
 "," { return symbol(sym.COMMA); }
 "." { return symbol(sym.DOT); }

/* brackets */
 "(" { return symbol(sym.LP); }
 ")" { return symbol(sym.RP); }
 "[" { return symbol(sym.LC); }
 "]" { return symbol(sym.RC); }
 "{" { return symbol(sym.LB); }
 "}" { return symbol(sym.RB); }

/* operators */
 "=" { return symbol(sym.EQUAL); }
 "<" { return symbol(sym.LT); }
 "!" { return symbol(sym.NEG); }
 "&&" { return symbol(sym.AND); }
 "*" { return symbol(sym.MULT); }
 "+" { return symbol(sym.PLUS); }
 "-" { return symbol(sym.MINUS); }
 
 /* my macros */
 {WhiteSpace} {}
 {INTEGER} { return symbol(sym.NUMBER, Integer.parseInt(yytext()));}
 {Identifier} { return symbol(sym.IDENTIFIER, new String(yytext()));}
 
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


	