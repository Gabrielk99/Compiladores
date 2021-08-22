package checker;

import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.DartParser;
import parser.DartLexer;

public class Main {
    public static void main(String[] args) throws IOException {

        if(args.length==0){
            System.out.printf("ERROR, no file is passed to compile!\n");
            System.exit(1);
        }
    
        CharStream input = CharStreams.fromFileName(args[0]);

        DartLexer lexer = new DartLexer(input);

        CommonTokenStream tokens = new CommonTokenStream (lexer);

        DartParser parser = new DartParser(tokens);

        ParseTree tree = parser.libraryDefinition();

        if(parser.getNumberOfSyntaxErrors() != 0)
            return;

        SemanticChecker checker = new SemanticChecker();
        checker.addBultin();
        checker.visit(tree);

        if (!checker.findMain()) {
            System.err.printf("SEMANTIC ERROR: No 'main' method found.\n");
            return;
        }

        checker.printTables();
        checker.printAST();
    }
}