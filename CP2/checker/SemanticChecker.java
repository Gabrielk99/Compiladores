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
import parser.DartParser.LibraryDefinitionContext;
import parser.DartParser.InitializedVariableDeclarationContext;

import tables.StrTable;
import tables.VarTable;
import tables.FuncTable;
import tables.Key;
import tables.VarTable.*;

import ast.AST.*;
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

            return null; //Nunca vai executar
        }
        return new AST(VAR_USE_NODE,k,vt.getType(k));
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
        AST node = new AST (VAR_DECL_NODE,k,vt.getType(k));
        return node;
    }
    private static void typeError (int line, String op, Type t1, Type t2){
        System.out.printf("SEMANTIC ERROR (%d): incompatible types for operator '%s', LHS is '%s' and RHS is '%s'.\n",
    			line, op, t1.toString(), t2.toString());
    	System.exit(1);
    }
    /******************************************************
     -------------------- Visitadores ---------------------
    *******************************************************/

    //Visita a regra principal que deriva as outras partes do programa
    @Override
    public AST visitLibraryDefinition(LibraryDefinitionContext ctx){
        if(ctx.libraryName()!=null) visit(ctx.libraryName()); // Isso nunca vai rolar, já que não usamos bibliotecas, mas...
        int i = 0;
        while(ctx.importOrExport(i)!=null) visit(ctx.importOrExport(i++)); //Também não vai rolar
        i=0;
        while (ctx.partDirective(i)!=null) visit(ctx.partDirective(i++)); //Também não vai rolar
        i=0;
        this.root = AST.newSubtree(GLOBAL_NODE,NO_TYPE);
        while(ctx.metadata(i) != null || ctx.topLevelDefinition(i)!=null){
            visit(ctx.metadata(i)); //Desnecessário para a AST
            AST node = visit(ctx.topLevelDefinition(i++)); //Os nó local
            this.root.addChild(node);
        }
        return this.root;
    }


    //Salva o nome da variável
    @Override
    public AST visitVarName (VarNameContext ctx) {
        lastVar = ctx.IDENTIFIER().getSymbol();
        return null;
    }

    //Salva o tipo declarado
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
    //Verifica se a operação = está correta
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
   //Regra de declaração de variável global

    @Override
    public AST visitTopLevelVarDecl(TopLevelVarDeclContext ctx) {
        visit(ctx.varOrType());
        visit(ctx.identifier());

        AST node  = newVar(lastVar);
        AST node2;
        if (ctx.expression() != null){
            node2 = visit(ctx.expression());
            return AST.newSubtree(INSTANCE_VAR_NODE,NO_TYPE,node,node2);
        }
        int i = 0;
        while(ctx.initializedIdentifier(i) != null)
            visit(ctx.initializedIdentifier(i++));
        return AST.newSubtree(INSTANCE_VAR_NODE,NO_TYPE,node);
        
    }
    // -------------------------- Declaracao local ----------------
    //Regra de declaração de varável local
    @Override
    public AST visitDeclaredIdentifier (DeclaredIdentifierContext ctx){
        visit(ctx.finalConstVarOrType());
        visit(ctx.identifier());

        AST node = newVar(lastVar);

        return node;
    }
    @Override
    public AST visitInitializedVariableDeclaration (InitializedVariableDeclarationContext ctx){
        AST node = visit(ctx.declaredIdentifier());
        AST node2;
        if(ctx.expression()!=null){
            node2 = visit(ctx.expression());
            return AST.newSubtree(INSTANCE_VAR_NODE,NO_TYPE,node,node2);
        }
        int i = 0;
        while(ctx.initializedIdentifier(i)!=null){
            visit(ctx.initializedIdentifier(i++));
        }
        return AST.newSubtree(INSTANCE_VAR_NODE,NO_TYPE,node);
    }
    //Regra para salvar também int x, y, b, c, ....;
    @Override
    public AST visitInitializedIdentifier(InitializedIdentifierContext ctx){
        visit(ctx.identifier());
        AST node = newVar(lastVar);
        AST node2;
        if(ctx.expression() != null){
            node2 = visit(ctx.expression());
            return AST.newSubtree(INSTANCE_VAR_NODE,NO_TYPE,node,node2);
        }
        return AST.newSubtree(INSTANCE_VAR_NODE,NO_TYPE,node);
    }
    // ------------------- Uso de variáveis ----------
    // todas variáveis usadas aciona a chamada --> primary : identifier #primIdentifier
    public AST visitPrimIdentifier(PrimIdentifierContext ctx){
        visit(ctx.identifier());

        AST node = checkVar(lastVar); //Verifica se a variável foi declarada

        return node;
    }




    // ------------------ Literais ------------------
    @Override
    public AST visitNumVal(NumValContext ctx) {
        String value = ctx.getText();
        AST node;
        if(value.matches("(.*).(.*)")){
            double num = Double.parseDouble(value);
            node = new AST(DOUBLE_VAL_NODE,num,DOUBLE_TYPE);
        }
        else{
            int num = Integer.parseInt(value);
            node = new AST(INT_VAL_NODE,num,INT_TYPE); 
        }
        
        return node;
    }

    // String de linha unica
    @Override
    public AST visitSingleLineRawStr(SingleLineRawStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),STR_TYPE);
        return node;
    }
    @Override
    public AST visitSingleLineSQStr(SingleLineSQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),STR_TYPE);
        return node;
    }
    @Override
    public AST visitSingleLineDQStr(SingleLineDQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),STR_TYPE);
        return node;
    }
    //String de multiplas linhas
    @Override
    public AST visitMultiLineRawStr(MultiLineRawStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),STR_TYPE);
        return node;
    }
    @Override
    public AST visitMultiLineSQStr(MultiLineSQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
       AST node = new AST(STR_VAL_NODE,strData.hashCode(),STR_TYPE);
        return node;
    }
    @Override
    public AST visitMultiLineDQStr(MultiLineDQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),STR_TYPE);
        return node;
    }

    //Booleanos
    @Override
    public AST visitTrueVal(TrueValContext ctx) {
        // Só serve pra adicionar na ast o nó com intData = 1
        return new AST(BOOL_VAL_NODE,1,BOOL_TYPE);
    }
    @Override
    public AST visitFalseVal(FalseValContext ctx) {
        // Só serve pra adicionar na ast o nó com intData = 0
        return new AST(BOOL_VAL_NODE,0,BOOL_TYPE);
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