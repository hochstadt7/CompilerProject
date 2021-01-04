
import java_cup.runtime.*;

%%
%class Lexer
%public
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
Identifier	 	= {Letters}({Letters} | {INTEGER} | _)*

%state COMMENT1
%state COMMENT2

%%


/* flow control */
<YYINITIAL> "if" { return symbol(sym.IF); }
<YYINITIAL> "else" { return symbol(sym.ELSE); }
<YYINITIAL> "while" { return symbol(sym.WHILE); } 
<YYINITIAL> "true" { return symbol(sym.TRUE); } 
<YYINITIAL> "false" { return symbol(sym.FALSE); }
<YYINITIAL> "extends" { return symbol(sym.EXTENDS); }
<YYINITIAL> "System.out.println" { return symbol(sym.SYSO); }
<YYINITIAL> "length" { return symbol(sym.LENGTH); } 
<YYINITIAL> "new" { return  symbol(sym.NEW); } 
<YYINITIAL> "null" { return  symbol(sym.NULL); }
<YYINITIAL> "static" { return  symbol(sym.STATIC); }
<YYINITIAL> "return" { return symbol(sym.RETURN); }
<YYINITIAL> "public" { return symbol(sym.PUBLIC); }
<YYINITIAL> "main" { return symbol(sym.MAIN); }
<YYINITIAL> "class" { return symbol(sym.CLASS); }

/* types */
<YYINITIAL> "this" { return symbol(sym.THIS); }
<YYINITIAL> "int"  { return symbol(sym.INT); }
<YYINITIAL> "boolean" { return symbol(sym.BOOLEAN); }
<YYINITIAL> "String"  { return symbol(sym.STRING); }
<YYINITIAL> "void" { return symbol(sym.VOID); }

/* punctuations */
<YYINITIAL> ";" { return symbol(sym.SEMICOLON); }
<YYINITIAL> "," { return symbol(sym.COMMA); }
<YYINITIAL> "." { return symbol(sym.DOT); }

/* brackets */
<YYINITIAL> "(" { return symbol(sym.LP); }
<YYINITIAL> ")" { return symbol(sym.RP); }
<YYINITIAL> "[" { return symbol(sym.LC); }
<YYINITIAL> "]" { return symbol(sym.RC); }
<YYINITIAL> "{" { return symbol(sym.LB); }
<YYINITIAL> "}" { return symbol(sym.RB); }

/* operators */
<YYINITIAL> "=" { return symbol(sym.EQUAL); }
<YYINITIAL> "<" { return symbol(sym.LT); }
<YYINITIAL> "!" { return symbol(sym.NEG); }
<YYINITIAL> "&&" { return symbol(sym.AND); }
<YYINITIAL> "*" { return symbol(sym.MULT); }
<YYINITIAL> "+" { return symbol(sym.PLUS); }
<YYINITIAL> "-" { return symbol(sym.MINUS); }
 
 /* my macros */
<YYINITIAL> {WhiteSpace} {}
<YYINITIAL> {INTEGER} {return symbol(sym.NUMBER, Integer.parseInt(yytext()));}
<YYINITIAL> {Identifier} {return symbol(sym.IDENTIFIER, new String(yytext()));}
 
 /* comments */
<YYINITIAL> "//" { yybegin(COMMENT1); }
<YYINITIAL> "/*" { yybegin(COMMENT2); }
 

 <COMMENT1> {
  [\n]			  		 { yybegin(YYINITIAL); }
  [^] 							 { }
}

 <COMMENT2> {
  "*/" 						 { yybegin(YYINITIAL); }
  [^]						 {}
}

<<EOF>> { 
		if (yystate() == COMMENT2){ // comment wasn't closed
				System.out.println("Syntax error at line "+yyline+" of input.");
				System.exit(1);
			}
		else
			return symbol(sym.EOF,"EOF"); 
		}
 
 /* error fallback */
	[^]                              { System.out.println("Syntax error at line "+yyline+" of input.");
				System.exit(1); }
	