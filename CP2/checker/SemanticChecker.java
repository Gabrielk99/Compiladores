import ast.NodeKind.*;
import typing.Type.*;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.Dart.IntValContext;
import parser.Dart.DoubleValContext;
import parser.Dart.DoubleValOnlyDecimalContext;
import parser.Dart.MultiLineStringContext;
import parser.Dart.SingleLineStringContext;
import parser.Dart.TrueValContext;
import parser.Dart.FalseValContext;
import parser.Dart.TypeIdContext;
import parser.Dart.VarNameContext;
import parser.Dart.TopLevelVarDeclContext;

import tables.Key;

import ast.AST;

public class SemanticChecker extends DartBaseVisitor<AST> {

	private StrTable st = new StrTable();   // Tabela de strings.
    private VarTable vt = new VarTable();   // Tabela de variáveis.
    private FuncTable ft = new FuncTable(); // Tabela de funcoes.

    Type lastType;
    Token lastVar;

    AST newVar(Token token) {
        String lastVarName = token.getText();
    	int line = token.getLine();
        int escopo = 0; // Mudar aqui

        Key k = new Key(lastVarName, escopo); // Vai ser usada pra ast tambem

        if (!vt.lookupVar(lastVarName, escopo)) {
        	System.err.printf("SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n", 
            line, lastVarName, vt.getLine(k));

        	System.exit(1);
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

        switch(typeCtx):
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
        default:
            lastType = NO_TYPE; // Lançar um erro?
    }

    @Override
    public AST visitTopLevelVarDecl(TopLevelVarDeclContext ctx) {
        visit(ctx.varOrType());
        visit(ctx.identifier());

        newVar(lastVar);

        if (ctx.expression() != null)
            visit(ctx.expression());
    }

    // ------------------ Literais ------------------
    @Override
    public AST visitIntVal(IntValContext ctx) {
        int intData = Integer.parseInt(ctx.getText());
        return null;
    }
    @Override
    public AST visitDoubleVal(DoubleValContext ctx) {
        double doubleData = Double.parseDouble(ctx.getText());
        return null;
    }
    @Override
    public AST visitDoubleValOnlyDecimal(DoubleValOnlyDecimalContext ctx) {
        double doubleData = Double.parseDouble(ctx.getText());
        return null;
    }
    @Override
    public AST visitMultiLineString(MultiLineStringContext ctx) {
        String strData = ctx.RAW_MULTI_LINE_STRING().getText();
        st.addStr(strData);

        return null;
    }
    @Override
    public AST visitSingleLineString(SingleLineStringContext ctx) {
        String strData = ctx.RAW_SINGLE_LINE_STRING().getText();
        st.addStr(strData);

        return null;
    }
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
}