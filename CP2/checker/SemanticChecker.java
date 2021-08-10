import ast.NodeKind.*;
import typing.Type.*;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.DartParser;
import parser.DartBaseVisitor;
import parser.DartParser.NumValContext;
import parser.DartParser.SingleLineRawStrContext;
import parser.DartParser.SingleLineSQStrContext;
import parser.DartParser.SingleLineDQStrContext;
import parser.DartParser.MultiLineRawStrContext;
import parser.DartParser.MultiLineSQStrContext;
import parser.DartParser.MultiLineDQStrContext;
import parser.DartParser.TrueValContext;
import parser.DartParser.FalseValContext;
import parser.DartParser.TypeIdContext;
import parser.DartParser.VarNameContext;
import parser.DartParser.TopLevelVarDeclContext;
import parser.DartParser.DeclaredIdentifierContext;

import tables.StrTable;
import tables.VarTable;
import tables.FuncTable;
import tables.Key;

import ast.AST;

public class SemanticChecker extends DartBaseVisitor<AST> {

	private StrTable st = new StrTable();   // Tabela de strings.
    private VarTable vt = new VarTable();   // Tabela de variáveis.
    private FuncTable ft = new FuncTable(); // Tabela de funcoes.

    Type lastType;
    Token lastVar;

    private boolean passed= true;

    // Checa se o uso da variável está correto
    AST checkVar(Token token){
        String name = token.getText();
        int line = token.getLine();
        int escopo = 0;

        Key k = new Key(name,escopo);

        if(!vt.lookupVar(name,escopo)){
            System.err.printf("SEMANTIC ERROR (%d): variable '%s' was not declared.\n", line, name);
    		
            passed = false;
        }
        return null;
    }

    AST newVar(Token token) {
        String lastVarName = token.getText();
    	int line = token.getLine();
        int escopo = 0; // Mudar aqui

        Key k = new Key(lastVarName, escopo); // Vai ser usada pra ast tambem

        if (!vt.lookupVar(lastVarName, escopo)) {
        	System.err.printf("SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n", 
            line, lastVarName, vt.getLine(k));

            passed=false;
            return null;
        }
        vt.addVar(lastVarName, line, lastType, escopo);
        return null;
    }

    /******************************************************
     -------------------- Visitadores ---------------------
    *******************************************************/

    @Override
    public AST visitVarName (VarNameContext ctx) {
        lastVar = ctx.getSymbol();
        return null;
    }

    @Override
    public AST visitTypeId(TypeIdContext ctx){
        String typeCtx = ctx.getText();

        switch(typeCtx){
            case "int":
                lastType = INT_TYPE;
            break;
            case "double":
                lastType = DOUBLE_TYPE;
            break;
            case "String":
                lastType = STR_TYPE;
            break;
            case "boolean":
                lastType = BOOL_TYPE;
            break;
            case "void":
                lastType = VOID_TYPE;
            default:
                lastType = NO_TYPE; // Lançar um erro? (provavel, você esqueceu do void) eita é verdade hihi
                passed = false;
        }
    }//correto pelo meu ver
    
    // ------------------ Declaracao no topLVL -------------------
    @Override
    public AST visitTopLevelVarDecl(TopLevelVarDeclContext ctx) {
        visit(ctx.varOrType());
        visit(ctx.identifier());

        newVar(lastVar);

        if (ctx.expression() != null)
            visit(ctx.expression());
        if (ctx.initializedIdentifier() != null)
            visit(ctx.initializedIdentifier());
    }
    // -------------------------- Declaracao local ----------------
    @Override
    public AST visitDeclaredIdentifier (DeclaredIdentifierContext ctx){
        visit(ctx.finalConstVarOrType());
        visit(ctx.identifier());

        newVar(lastVar);

        return null;
    }
    @Override
    public AST visitInitializedIdentifier(InitializedIdentifierContext ctx){
        visit(ctx.identifier());
        newVar(lastVar);

        if(ctx.expression() != null){visit(ctx.expression());}
        return null;
    }
    // ------------------- Uso de variáveis ----------
    // todas variáveis usadas aciona a chamada --> primary : identifier #primIdentifier
    public AST visitPrimIdentifier(PrimIdentifier ctx){
        visit(ctx.identifier());

        checkVar(lastVar); //Verifica se a variável foi declarada

        return null;
    }




    // ------------------ Literais ------------------
    @Override
    public AST visitNumVal(NumValContext ctx) {
        String value = ctx.getText();
        if(value.matches("(.*).(.*)")){
            double num = Double.parseDouble(value);
        }
        else{
            int num = Integer.parseInt(value);
        }
        return null;
    }

    // String de linha unica
    @Override
    public AST visitSingleLineRawStr(SingleLineRawStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData);
        return null;
    }
    @Override
    public AST visitSingleLineSQStr(SingleLineSQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData);
        return null;
    }
    @Override
    public AST visitSingleLineDQStr(SingleLineDQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData);
        return null;
    }
    //String de multiplas linhas
    @Override
    public AST visitMultiLineRawStr(MultiLineRawStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData);
        return null;
    }
    @Override
    public AST visitMultiLineSQStr(MultiLineSQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData);
        return null;
    }
    @Override
    public AST visitMultiLineDQStr(MultiLineDQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData);
        return null;
    }

    //Booleanos
    @Override
    public AST visitTrueVal(TrueValContext ctx) {
        // Só serve pra adicionar na ast o nó com intData = 1
        return null;
    }
    @Override
    public AST visitFalseVal(FalseValContext ctx) {
        // Só serve pra adicionar na ast o nó com intData = 0
        return null;
    }

 void printTables() {
        System.out.print("\n\n");
        System.out.print(st);
        System.out.print("\n\n");
    	System.out.print(vt);
    	System.out.print("\n\n");
        System.out.print(ft);
        System.out.print("\n\n");
    }
// Retorna true se os testes passaram.
boolean hasPassed() {
    return passed;
}

}