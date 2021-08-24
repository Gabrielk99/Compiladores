package checker;

import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.DartParser;
import parser.DartLexer;
import java.util.regex.*;
import java.io.*;
public class Main {
    public static void main(String[] args) throws IOException {

        if(args.length==0){
            System.out.printf("ERROR, no file was passed to compile!\n");
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

        final Pattern tag_regex = Pattern.compile("/(.+?)\\.dart",Pattern.DOTALL);
        final Matcher matcher = tag_regex.matcher(args[0]);
        String nameOfFile="";
        if(matcher.find())
            nameOfFile = matcher.group(1);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream newPrintStream = new PrintStream(outputStream);

        PrintStream old = System.out;

        System.setErr(newPrintStream);
        checker.printAST();

        System.err.flush();
        System.setErr(old);

        BufferedWriter writer = new BufferedWriter(new FileWriter("resultados/".concat(nameOfFile).concat(".dot")));
        writer.write(outputStream.toString());

        writer.close();
    }
}