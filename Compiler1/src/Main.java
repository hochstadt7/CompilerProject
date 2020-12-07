import ast.*;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            var inputMethod = args[0];
            var action = args[1];
            var filename = args[args.length - 2];
            var outfilename = args[args.length - 1];

            Program prog;

            if (inputMethod.equals("parse")) {
                throw new UnsupportedOperationException("TODO - Ex. 4");
            } else if (inputMethod.equals("unmarshal")) {
                AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                prog = xmlSerializer.deserialize(new File(filename));
            } else {
                throw new UnsupportedOperationException("unknown input method " + inputMethod);
            }

            var outFile = new PrintWriter(outfilename);
            try {

                if (action.equals("marshal")) {
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else if (action.equals("print")) {
                    AstPrintVisitor astPrinter = new AstPrintVisitor();
                    prog.accept(astPrinter);
                    outFile.write(astPrinter.getString());

                } else if (action.equals("semantic")) {
                    throw new UnsupportedOperationException("TODO - Ex. 3");

                } else if (action.equals("compile")) {
                	SymbolTableBuilder symbolTableBuilder=new SymbolTableBuilder();
                    prog.accept(symbolTableBuilder);
                    TranslatorVisitor translatorVisitor= new TranslatorVisitor(symbolTableBuilder.myVariables);
                    prog.accept(translatorVisitor);
                    outFile.write(translatorVisitor.emitted.toString());

                } else if (action.equals("rename")) {
                    var type = args[2];
                    var originalName = args[3];
                    var originalLine = args[4];
                    var newName = args[5];

                    boolean isMethod;
                    if (type.equals("var")) {
                        isMethod = false;
                    } else if (type.equals("method")) {
                        isMethod = true;
                    } else {
                        throw new IllegalArgumentException("unknown rename type " + type);
                    }
                    
                    //my add
                    
                    SymbolTableBuilder symbolTableBuilder=new SymbolTableBuilder();
                    prog.accept(symbolTableBuilder);
                    int linenumber = Integer.parseInt(originalLine);
                    if(isMethod) {
	                    ResolveMethod resolveMethod=new ResolveMethod(linenumber,originalName);
	                    prog.accept(resolveMethod);
	                    AncestorFinderVisitor ancestorFinderVisitor=new AncestorFinderVisitor(resolveMethod.classIndex,originalName);
	                    prog.accept(ancestorFinderVisitor);
	                    OffspringFinderMethodRenamerVisitor offspringFinderVisitor=new OffspringFinderMethodRenamerVisitor(ancestorFinderVisitor.classIndex, originalName, newName);
	                    prog.accept(offspringFinderVisitor);
	                    MethodInstanceRenamer methodInstanceRenamer=new MethodInstanceRenamer(originalName,newName,symbolTableBuilder.myVariables,offspringFinderVisitor.OffspringNames);
	                    prog.accept(methodInstanceRenamer);
                    }
                    else { // it is a variable
                    	ResolveVar resolveVar = new ResolveVar(linenumber, originalName, newName);
                    	prog.accept(resolveVar);
                    	if (resolveVar.methodIndex == -1) { // field
                    		FieldInstanceRenamerVisitor fieldInstanceRenamerVisitor = new FieldInstanceRenamerVisitor(resolveVar.classIndex, originalName, newName, linenumber);
                    		prog.accept(fieldInstanceRenamerVisitor);
                    	} else { // local / parameter
                    		AstXMLSerializer xmlSerializer = new AstXMLSerializer();
	                    	VariableInstanceRenamer variableInstanceRenamer=new VariableInstanceRenamer(originalName,newName, 0, linenumber);
	                    	MethodDecl method = prog.classDecls().get(resolveVar.classIndex).methoddecls().get(resolveVar.methodIndex);
	                    	method.accept(variableInstanceRenamer);
                    	}
                    }
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else {
                    throw new IllegalArgumentException("unknown command line action " + action);
                }
            } finally {
                outFile.flush();
                outFile.close();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("General error: " + e);
            e.printStackTrace();
        }
    }
}
