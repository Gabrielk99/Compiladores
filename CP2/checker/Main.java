package chacker;

import java.io.IOException;


import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.DartParser;
import parser.DartLexer;

public class Main {
    public static void main(String[] args) throws IOException {

    CharStream input = CharStreams.fromFileName(args[0]);

    DartLexer lexer = new DartLexer(input);

    CommonTokenStream tokens = new CommonTokenStream (lexer);

    DartParser parser = new DartParser(tokens);

    ParseTree tree = parser.libraryDefinition();

    if(parser.getNumberOfSyntaxErrors() != 0){

        return;
    }

    SemanticChecker checker = new SemanticChecker();

    checker.visit(tree);

    if(checker.hasPassed ()){
        System.out.println("PARSER SUCCESSFULL!");;
        checker.printTables();
    }


    }
}