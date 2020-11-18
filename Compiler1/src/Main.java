import ast.*;

import java.io.*;
import java.util.HashMap;

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
                    astPrinter.visit(prog);
                    outFile.write(astPrinter.getString());

                } else if (action.equals("semantic")) {
                    throw new UnsupportedOperationException("TODO - Ex. 3");

                } else if (action.equals("compile")) {
                    throw new UnsupportedOperationException("TODO - Ex. 2");

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
                    symbolTableBuilder.visit(prog);
                 
                    if(isMethod) {
                    ResolveMethod resolveMethod=new ResolveMethod(Integer.parseInt(originalLine),originalName);
                    resolveMethod.visit(prog);
                    AncestorFinderVisitor ancestorFinderVisitor=new AncestorFinderVisitor(resolveMethod.classIndex,originalName);
                    ancestorFinderVisitor.visit(prog);
                    OffspringFinderMethodRenamerVisitor offspringFinderVisitor=new OffspringFinderMethodRenamerVisitor(ancestorFinderVisitor.classIndex, originalName, newName);
                    offspringFinderVisitor.visit(prog);
                    MethodInstanceRenamer methodInstanceRenamer=new MethodInstanceRenamer(originalName,newName,symbolTableBuilder.myVariables,offspringFinderVisitor.OffspringNames);
                    methodInstanceRenamer.visit(prog);
                    }
                    else {
                    	// NEED TO GET PLACE FROM EYAL
                    	VariableInstanceRenamer variableInstanceRenamer=new VariableInstanceRenamer(originalName,newName,/*?*/);
                    	variableInstanceRenamer.visit(/*?*/);
                    	FieldInstanceRenamerVisitor new fieldInstanceRenamerVisitor(/*?*/,originalName,newName);
                    }
                    

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
