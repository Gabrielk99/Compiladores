package checker;

import ast.NodeKind.*;
import static ast.NodeKind.*;
import typing.Type;
import typing.Conv;
import typing.Conv.Unif;
import static typing.Type.INT_TYPE;
import static typing.Type.DOUBLE_TYPE;
import static typing.Type.STR_TYPE;
import static typing.Type.BOOL_TYPE;
import static typing.Type.VOID_TYPE;
import static typing.Type.NO_TYPE;
import static typing.Type.LIST_TYPE;
import static typing.Conv.I2D;

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
import parser.DartParser.InitializedIdentifierContext;
import parser.DartParser.PrimIdentifierContext;

import tables.StrTable;
import tables.VarTable;
import tables.FuncTable;
import tables.Key;

import ast.AST;

public class SemanticChecker extends DartBaseVisitor<AST> {

	private StrTable st = new StrTable();   // Tabela de strings.
    private VarTable vt = new VarTable();   // Tabela de variáveis.
    private FuncTable ft = new FuncTable(); // Tabela de funcoes.

    Type lastType; //Variável global com o último tipo declarado
    Token lastVar; //Variável global com o token da ultima variável declarada

    AST root; //Nó raiz da AST

    int id_escopo_atual = 0;
    int id_escopo_counter = 0;


    // Checa se o uso da variável está correto
    AST checkVar(Token token){
        String name = token.getText();
        int line = token.getLine();

        Key k = new Key(name,id_escopo_atual);

        if(!vt.lookupVar(name,id_escopo_atual)){
            System.err.printf("SEMANTIC ERROR (%d): variable '%s' was not declared.\n", line, name);
    		
            System.exit(1);//Aborta o compilador caso haja erro de semantica

            return null; //Nuva vai executar
        }
        return null;//new AST(VAR_USE_NODE,k,vt.getType(k));
    }

    //Adiciona uma nova variável na tabela e retorna um nó de VAR_DECL
    AST newVar(Token token) {
        String lastVarName = token.getText();
    	int line = token.getLine();

        Key k = new Key(lastVarName, id_escopo_atual); // Vai ser usada pra ast tambem

        if (vt.lookupVar(lastVarName, id_escopo_atual)) {
        	System.err.printf("SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n", 
            line, lastVarName, vt.getLine(k));

            System.exit(1);
            return null;
        }
        //Erro caso declara variável com um tipo não aceito
        if(lastType == NO_TYPE){
            System.out.printf("SEMANTIC ERROR (%d): incompatible type for var declaration '%s'\n",line,lastVarName);
            System.exit(1);
            return null;
        }
        vt.addVar(lastVarName, line, lastType, id_escopo_atual);
        return null;//new AST(VAR_DECL_NODE,k,vt.getType(k));
    }
    private static void typeError (int line, String op, Type t1, Type t2){
        System.out.printf("SEMANTIC ERROR (%d): incompatible types for operator '%s', LHS is '%s' and RHS is '%s'.\n",
    			line, op, t1.toString(), t2.toString());
    	System.exit(1);
    }
    /******************************************************
     -------------------- Visitadores ---------------------
    *******************************************************/

    @Override
    public AST visitVarName (VarNameContext ctx) {
        lastVar = ctx.IDENTIFIER().getSymbol();
        return null;
    }

    @Override
    public AST visitTypeId(TypeIdContext ctx){
        String typeCtx = ctx.getText();

        switch(typeCtx){
            case "int":
                if(lastType == LIST_TYPE){
                    lastType.defineInner(INT_TYPE);
                }
                else
                    lastType = INT_TYPE;
            break;
            case "double":
                if(lastType==LIST_TYPE)
                    lastType.defineInner(DOUBLE_TYPE);
                else
                    lastType = DOUBLE_TYPE;
            break;
            case "String":
                if(lastType == LIST_TYPE)
                    lastType.defineInner(STR_TYPE);
                else
                    lastType = STR_TYPE;
            break;
            case "bool":
                if(lastType == LIST_TYPE)
                    lastType.defineInner(BOOL_TYPE);
                else
                    lastType = BOOL_TYPE;
            break;
            case "void":
                lastType = VOID_TYPE;
            break;
            case "List":
                lastType = LIST_TYPE;
            break;
            default:
                lastType = NO_TYPE; 
        }

        return null;
    }
    private static AST checkAssign(int line, AST l, AST r){
        Type lt = l.type;
        Type rt = r.type;

        if(lt == BOOL_TYPE && rt!= BOOL_TYPE) typeError(line,"=",lt,rt);
        if(lt == STR_TYPE && rt!= STR_TYPE) typeError(line,"=",lt,rt);
        if(lt == INT_TYPE && rt!= INT_TYPE) typeError(line,"=",lt,rt);
        if(lt == LIST_TYPE){
            if(rt == LIST_TYPE){ 
                if(lt.getInner() == BOOL_TYPE && rt.getInner()!=BOOL_TYPE) typeError(line,"=",lt,rt);
                if(lt.getInner() == STR_TYPE && rt.getInner()!=STR_TYPE ) typeError(line,"=",lt,rt);
                if(lt.getInner() == INT_TYPE && rt.getInner()!=INT_TYPE) typeError(line,"=",lt,rt);
                if(lt.getInner() == DOUBLE_TYPE && rt.getInner()!=DOUBLE_TYPE) typeError(line,"=",lt,rt);
            }
            else if(rt != LIST_TYPE){
                typeError(line,"=",lt,rt);
            }
        }
        if(lt == DOUBLE_TYPE){
            if(rt == INT_TYPE){
                r = Conv.createConvNode(I2D,r);
            }
            else if(rt != DOUBLE_TYPE){
                typeError(line,"=",lt,rt);
            }
        }
        return null;//AST.newSubtree(ASSIGN_NODE,NO_TYPE,l,r);
    }
    // ------------------ Declaracao no topLVL -------------------
    @Override
    public AST visitTopLevelVarDecl(TopLevelVarDeclContext ctx) {
        visit(ctx.varOrType());
        visit(ctx.identifier());

        newVar(lastVar);

        if (ctx.expression() != null)
            visit(ctx.expression());
        
        int i = 0;
        while(ctx.initializedIdentifier(i) != null)
            visit(ctx.initializedIdentifier(i++));
        
        return null;
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
    public AST visitPrimIdentifier(PrimIdentifierContext ctx){
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
void printAST(){
    AST.printDot(root,vt);
}
}