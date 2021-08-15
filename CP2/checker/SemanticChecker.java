package checker;

import ast.NodeKind.*;
import ast.NodeKind;
import static ast.NodeKind.*;
import typing.Type;
import typing.Conv;
import typing.Conv.Unif;
import typing.Inner.*;
import typing.Inner;

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
import parser.DartParser.IfStatementContext;
import parser.DartParser.BlockContext;
import parser.DartParser.LocalVariableDeclarationContext;
import parser.DartParser.ElementsContext;
import parser.DartParser.ListLiteralContext;
import parser.DartParser.StatementsContext;
import parser.DartParser.LogicalAndExpressionContext;
import parser.DartParser.LogicalOrExpressionContext;
import parser.DartParser.EqExpressionContext;
import parser.DartParser.TypeVoidContext;
import parser.DartParser.AssignExpContext;
import parser.DartParser.TimesAssignContext;
import parser.DartParser.IdExpContext;
import parser.DartParser.TypeNotVoidNotFunctionContext;
import parser.DartParser.ExpressionStatementContext;
import parser.DartParser.OverAssignContext;
import parser.DartParser.PlusAssignContext;
import parser.DartParser.MinusAssignContext;
import parser.DartParser.ComposAssignContext;
import parser.DartParser.AssignContext;



import tables.StrTable;
import tables.VarTable;
import tables.FuncTable;
import tables.Key;
import tables.VarTable.*;
import tables.Tree;

import ast.AST.*;
import ast.AST;


public class SemanticChecker extends DartBaseVisitor<AST> {

	private StrTable st = new StrTable();   // Tabela de strings.
    private VarTable vt = new VarTable();   // Tabela de variáveis.
    private FuncTable ft = new FuncTable(); // Tabela de funcoes.

    Inner lastType; //Variável global com o último tipo declarado
    Token lastVar; //Variável global com o token da ultima variável declarada

    AST root; //Nó raiz da AST

    int id_escopo_atual = 0;
    int id_escopo_counter = 0;

    Tree scopeTree; // Árvore de escopo

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
        if(lastType.getType() == NO_TYPE){
            System.out.printf("SEMANTIC ERROR (%d): incompatible type for var declaration '%s'\n",line,lastVarName);
            System.exit(1);
            return null;
        }
        vt.addVar(lastVarName, line, lastType, id_escopo_atual); 
        AST node = new AST (VAR_DECL_NODE,k,vt.getType(k));
        return node;
    }
    private static void typeError (int line, String op, Inner t1, Inner t2){
        System.out.printf("SEMANTIC ERROR (%d): incompatible types for operator '%s', LHS is '%s' and RHS is '%s'.\n",
    			line, op, t1.toString(), t2.toString());
    	System.exit(1);
    }

    private static void checkBoolExpr(int lineNo, String cmd, Inner t) {
        if (t.getType() != BOOL_TYPE) {
            System.out.printf("SEMANTIC ERROR (%d): conditional expression in '%s' is '%s' instead of '%s'.\n",
                    lineNo, cmd, t.toString(), BOOL_TYPE.toString());
            System.exit(1);
        }
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
        this.root = AST.newSubtree(GLOBAL_NODE,new Inner(NO_TYPE,NO_TYPE));
        while(ctx.metadata(i) != null || ctx.topLevelDefinition(i)!=null){
            scopeTree = new Tree(id_escopo_atual, null); // Cria nó raíz da arvore de escopos (escopo =0)

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
    //Sobrescreve typeNotVoidNotFunction para lidar com listas
    @Override
    public AST visitTypeNotVoidNotFunction(TypeNotVoidNotFunctionContext ctx){
        visit(ctx.typeName());
        if(ctx.typeArguments()!=null){ //se tem argumento de tipo é lista
            visit(ctx.typeArguments());
            Inner listType = new Inner(LIST_TYPE,NO_TYPE);
            listType.defInner(lastType.getType());
            lastType = listType;
        }
        return null;
    } 
    //Salva o tipo declarado
    @Override
    public AST visitTypeId(TypeIdContext ctx){
        String typeCtx = ctx.getText();

        switch(typeCtx){
            case "int":
                lastType = new Inner(INT_TYPE,NO_TYPE);
            break;
            case "double":
                lastType = new Inner(DOUBLE_TYPE,NO_TYPE);
            break;
            case "String":
                lastType = new Inner(STR_TYPE,NO_TYPE);
            break;
            case "bool":
                lastType = new Inner(BOOL_TYPE,NO_TYPE);
            break;
            default:
                lastType = new Inner(NO_TYPE,NO_TYPE); 
        }

        return null;
    }
    //Sobrescrevendo apenas por causa do tipo VOID
    @Override
    public AST visitTypeVoid(TypeVoidContext ctx){
        lastType = new Inner(VOID_TYPE,NO_TYPE);
        return null;
    }
    //Verifica se a operação = está correta
    private static AST checkAssign(int line, AST l, AST r){
        Inner lt = l.type;
        Inner rt = r.type;

        if(lt.getType() == BOOL_TYPE && rt.getType()!= BOOL_TYPE) typeError(line,"=",lt,rt);
        if(lt.getType() == STR_TYPE && rt.getType()!= STR_TYPE) typeError(line,"=",lt,rt);
        if(lt.getType() == INT_TYPE && rt.getType()!= INT_TYPE) typeError(line,"=",lt,rt);
        if(lt.getType() == LIST_TYPE){
            if(rt.getType() == LIST_TYPE){ 
                if(lt.getInner() == BOOL_TYPE && rt.getInner()!=BOOL_TYPE && rt.getInner()!=NO_TYPE) typeError(line,"=",lt,rt);
                if(lt.getInner() == STR_TYPE && rt.getInner()!=STR_TYPE && rt.getInner()!=NO_TYPE) typeError(line,"=",lt,rt);
                if(lt.getInner() == INT_TYPE && (rt.getInner()!=INT_TYPE && rt.getInner()!=NO_TYPE)) typeError(line,"=",lt,rt);
                if(lt.getInner() == DOUBLE_TYPE && rt.getInner()!=DOUBLE_TYPE && rt.getInner()!=NO_TYPE) typeError(line,"=",lt,rt);
            }
            else if(rt.getType() != LIST_TYPE){
                typeError(line,"=",lt,rt);
            }
        }
        if(lt.getType() == DOUBLE_TYPE){
            if(rt.getType() == INT_TYPE){
                r = Conv.createConvNode(I2D,r);
            }
            else if(rt.getType() != DOUBLE_TYPE){
                typeError(line,"=",lt,rt);
            }
        }
        return AST.newSubtree(ASSIGN_NODE,new Inner(NO_TYPE,NO_TYPE),l,r);
    }
    //------------------------- Assign Exp ------------------------------
    @Override
    public AST visitAssignExp(AssignExpContext ctx){
        AST l = visit(ctx.assignableExpression()); //Coleta a arvore da esquerda da variavel
        AST r = visit(ctx.expression()); //Coleta a arvore da direita da expressão
        AST op = visit(ctx.assignmentOperator()); //Coleta a arvore do meio do operador de atribuição

        //Unificação dos tipos para determinar o tipo resultante
        Type lt = l.type.getType();  
        Type rt = r.type.getType();

        Unif unif;
        
        if(op.kind != ASSIGN_PLUS_NODE){
            unif = lt.unifyOtherArith(rt);
        }
        else {
         
            unif = lt.unifyPlus(rt);
       
        }
        if(unif.type == NO_TYPE){
            typeError(ctx.assignmentOperator().getStart().getLine(),op.kind.toString(),new Inner(lt,l.type.getInner()),new Inner(rt,r.type.getInner()));
        }
        
        //Cria os nó de conversão caso exista

        AST lc = Conv.createConvNode(unif.lc,l);
        r = Conv.createConvNode(unif.rc,r);
        
        AST child;
        if(op.kind.toString().equals("*=")){
            child = AST.newSubtree(TIMES_NODE,new Inner(unif.type,r.type.getInner()),lc,r);
            return checkAssign(ctx.assignmentOperator().getStart().getLine(),l,child);
        }
        else if(op.kind.toString().equals("/=")){
            child = AST.newSubtree(OVER_NODE,new Inner(unif.type,r.type.getInner()),lc,r);
            return checkAssign(ctx.assignmentOperator().getStart().getLine(),l,child);
        }
        else if(op.kind.toString().equals("-=")){
            child = AST.newSubtree(MINUS_NODE,new Inner(unif.type,r.type.getInner()),lc,r);
            return checkAssign(ctx.assignmentOperator().getStart().getLine(),l,child);
        }
         else if(op.kind.toString().equals("+=")){
            child = AST.newSubtree(PLUS_NODE,new Inner(unif.type,r.type.getInner()),lc,r);
            return checkAssign(ctx.assignmentOperator().getStart().getLine(),l,child);
        }
   
        return checkAssign(ctx.assignmentOperator().getStart().getLine(),l,r); // caso seja "="
    }
    // ---------------------- idExp (assignableExpression) ---------------
    @Override
    public AST visitIdExp(IdExpContext ctx){
        visit(ctx.identifier());

        AST node = checkVar(lastVar);

        return node;
    }
    // ------------------------ *= ---------------------------
    @Override
    public AST visitTimesAssign(TimesAssignContext ctx){
        return new AST(ASSIGN_TIMES_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    // ------------------------ /= ---------------------------
    @Override
    public AST visitOverAssign(OverAssignContext ctx){
        return new AST(ASSIGN_OVER_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    // ------------------------ += ---------------------------
    @Override
    public AST visitPlusAssign(PlusAssignContext ctx){
        return new AST(ASSIGN_PLUS_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    // ------------------------ -= ---------------------------
    @Override
    public AST visitMinusAssign(MinusAssignContext ctx){
        return new AST(ASSIGN_MINUS_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    // ------------------------ = ---------------------------
    @Override
    public AST visitAssign(AssignContext ctx){
        return new AST(ASSIGN_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    // ----------------------- composAssign ------------------
    @Override
    public AST visitComposAssign(ComposAssignContext ctx){
        return visit(ctx.compoundAssignmentOperator());
    }
    // ---------------------- ExpressionStatement ----------------
    @Override
    public AST visitExpressionStatement(ExpressionStatementContext ctx){
        if(ctx.expression()!=null){
           return visit(ctx.expression());
        }
        return null;
    }

    // ------------------ Declaracao no topLVL -------------------
   //Regra de declaração de variável global

    @Override
    public AST visitTopLevelVarDecl(TopLevelVarDeclContext ctx) {
        visit(ctx.varOrType());
        visit(ctx.identifier());

        AST node  = newVar(lastVar);
        AST node2;
       
        int i = 0;
        
        while(ctx.initializedIdentifier(i) != null)
            visit(ctx.initializedIdentifier(i++));
        
        if (ctx.expression() != null){
            node2 = visit(ctx.expression());
            return AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),node,node2);
        }
        
        return AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),node);
        
    }
    // -------------------------- Declaracao local ----------------
    //Regra de declaração de varável local
    @Override
    public AST visitDeclaredIdentifier (DeclaredIdentifierContext ctx){
        visit(ctx.finalConstVarOrType());
        visit(ctx.identifier());

        AST node = newVar(lastVar);
        AST.printDot(node,vt);

        return node;
    }
    @Override
    public AST visitInitializedVariableDeclaration (InitializedVariableDeclarationContext ctx){
        AST node = visit(ctx.declaredIdentifier());
        AST node2;
       
        int i = 0;
        while(ctx.initializedIdentifier(i)!=null){
            visit(ctx.initializedIdentifier(i++));
        }
        
        if(ctx.expression()!=null){
            node2 = visit(ctx.expression());
            return AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),node,node2);
        }
       
        return AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),node);
    }
    //Regra para salvar também int x, y, b, c, ....;
    @Override
    public AST visitInitializedIdentifier(InitializedIdentifierContext ctx){
        visit(ctx.identifier());
        AST node = newVar(lastVar);
        AST node2;
        if(ctx.expression() != null){
            node2 = visit(ctx.expression());
            return AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),node,node2);
        }
        return AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),node);
    }
    // -------------------- Declaração local regra principal --------------
    @Override
    public AST visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx){
        visit(ctx.metadata());
        AST node = visit(ctx.initializedVariableDeclaration());

        return node;
    }
    // ------------------- Uso de variáveis ----------
    // todas variáveis usadas aciona a chamada --> primary : identifier #primIdentifier
    public AST visitPrimIdentifier(PrimIdentifierContext ctx){
        visit(ctx.identifier());

        AST node = checkVar(lastVar); //Verifica se a variável foi declarada

        return node;
    }

    //--------------------- Novo Escopo ------------------
    @Override
    public AST visitBlock(BlockContext ctx){
        id_escopo_atual = ++id_escopo_counter; // Gera novo escopo: aumenta contador e atribui ao escopo atual
        scopeTree = scopeTree.addChild(id_escopo_atual); // Adiciona um filho ao escopo atual e agora o escopo atual
                                                        // é a referencia pro nó filho
        
        AST node = visit(ctx.statements());


        id_escopo_atual--; // Ao fim, sai desse escopo, logo decrementa o escopo atual
        scopeTree = scopeTree.getPai(); // Nó da arvore de escopo volta ao nó do escopo anterior, que é o pai

        return node;
    }
    //-------------------- Statements -------------------
    @Override
    public AST visitStatements(StatementsContext ctx){
        int i =0;
        AST rt = new AST(BLOCK_NODE,0,new Inner(NO_TYPE,NO_TYPE));
        while(ctx.statement(i)!=null){
            AST node = visit(ctx.statement(i++));
            rt.addChild(node);
        }
        return rt;
    }

    // ----------------- If else --------------------
    @Override
    public AST visitIfStatement(IfStatementContext ctx){
        
        // Analisa expressao booleana
        AST exprNode = visit(ctx.expression());
        checkBoolExpr(ctx.IF().getSymbol().getLine(), "if", exprNode.type);

        // O primeiro statement é o do then
        AST thenNode = visit(ctx.statement(0));

        AST subTreeIFNode;
        //O segundo statement, se existir, é do else
        if(ctx.ELSE() != null){
            AST elseNode = visit(ctx.statement(1));

            subTreeIFNode = AST.newSubtree(IF_NODE, new Inner(NO_TYPE,NO_TYPE), exprNode, thenNode, elseNode);
        }else
            subTreeIFNode = AST.newSubtree(IF_NODE, new Inner(NO_TYPE,NO_TYPE), exprNode, thenNode);

        return subTreeIFNode;
    }

    // ------------------- Operadores -----------------------

    // Lógicos
    @Override
    public AST visitLogicalOrExpression(LogicalOrExpressionContext ctx){
        if (ctx.logicalAndExpression(1) == null)     // quer dizer que nao tem "||"
            return visit(ctx.logicalAndExpression(0));

        AST orNode = AST.newSubtree(OR_NODE, new Inner(BOOL_TYPE,NO_TYPE)); //Cria nó or

        int i = 1;
        AST lastChild = visit(ctx.logicalAndExpression(0)); // Primeira expressao
        orNode.addChild(lastChild);

        while(ctx.logicalAndExpression(i) != null){
            AST child = visit(ctx.logicalAndExpression(i++));

            Type lt = child.type.getType();
            Type rt = lastChild.type.getType();
            Unif unif = lt.unifyBooleans(rt);

            if (unif.type == NO_TYPE)       // Verifica se a expressao anterior OU a expressao atual geram bool
                typeError(434374838, "||", child.type, lastChild.type); // Como pegar a linha???

            orNode.addChild(child);
            lastChild = child;
        }
        return orNode;
    }

    @Override
    public AST visitLogicalAndExpression(LogicalAndExpressionContext ctx){
        if (ctx.equalityExpression(1) == null)     // quer dizer que nao tem "&&"
            return visit(ctx.equalityExpression(0));

        AST andNode = AST.newSubtree(AND_NODE, new Inner(BOOL_TYPE,NO_TYPE)); //Cria nó and

        int i = 1;
        AST lastChild = visit(ctx.equalityExpression(0)); // Primeira expressao
        andNode.addChild(lastChild);

        while(ctx.equalityExpression(i) != null){
            AST child = visit(ctx.equalityExpression(i++));

            Type lt = child.type.getType();
            Type rt = lastChild.type.getType();
            Unif unif = lt.unifyBooleans(rt);

            if (unif.type == NO_TYPE)       // Verifica se a expressao anterior E a expressao atual geram bool
                typeError(434374838, "&&", child.type, lastChild.type); // Como pegar a linha???

            andNode.addChild(child);
            lastChild = child;
        }
        return andNode;
    }

    // Comparacionais
    @Override
    public AST visitEqExpression(EqExpressionContext ctx){
        if (ctx.relationalExpression(1) == null)     // quer dizer que nao tem "==" ou "!="
            return visit(ctx.relationalExpression(0));

        String operator = ctx.equalityOperator().getText();
        NodeKind equalityNode;

        if (operator.equals("=="))
            equalityNode = EQ_NODE;
        else equalityNode = NEQ_NODE;

        AST child1 = visit(ctx.relationalExpression(0));
        AST child2 = visit(ctx.relationalExpression(1));

        // Por causa do tipo void, devemos fazer essa verificação
        Type lt = child1.type.getType();
        Type rt = child2.type.getType();
        Unif unif = lt.unifyEquals(rt);

        if (unif.type == NO_TYPE)
            typeError(434374838, operator, child1.type, child2.type); // Como pegar a linha???

        return AST.newSubtree(equalityNode, new Inner(unif.type,NO_TYPE), child1, child2);
    }


    // --------------------- Literais ----------------------
    @Override
    public AST visitNumVal(NumValContext ctx) {
        String value = ctx.getText();
        AST node;
        if(value.matches("(\\d)*\\.(\\d)*")){
            double num = Double.parseDouble(value);
            node = new AST(DOUBLE_VAL_NODE,num,new Inner(DOUBLE_TYPE,NO_TYPE));
        }
        else{
            int num = Integer.parseInt(value);
            node = new AST(INT_VAL_NODE,num,new Inner(INT_TYPE,NO_TYPE)); 
        }
        
        return node;
    }

    // String de linha unica
    @Override
    public AST visitSingleLineRawStr(SingleLineRawStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),new Inner(STR_TYPE,NO_TYPE));
        return node;
    }
    @Override
    public AST visitSingleLineSQStr(SingleLineSQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),new Inner(STR_TYPE,NO_TYPE));
        return node;
    }
    @Override
    public AST visitSingleLineDQStr(SingleLineDQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),new Inner(STR_TYPE,NO_TYPE));
        return node;
    }
    //String de multiplas linhas
    @Override
    public AST visitMultiLineRawStr(MultiLineRawStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),new Inner(STR_TYPE,NO_TYPE));
        return node;
    }
    @Override
    public AST visitMultiLineSQStr(MultiLineSQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
       AST node = new AST(STR_VAL_NODE,strData.hashCode(),new Inner(STR_TYPE,NO_TYPE));
        return node;
    }
    @Override
    public AST visitMultiLineDQStr(MultiLineDQStrContext ctx) {
        String strData = ctx.getText();
        st.addStr(strData,strData.hashCode());
        AST node = new AST(STR_VAL_NODE,strData.hashCode(),new Inner(STR_TYPE,NO_TYPE));
        return node;
    }

    //Booleanos
    @Override
    public AST visitTrueVal(TrueValContext ctx) {
        // Só serve pra adicionar na ast o nó com intData = 1
        return new AST(BOOL_VAL_NODE,1,new Inner(BOOL_TYPE, NO_TYPE));
    }
    @Override
    public AST visitFalseVal(FalseValContext ctx) {
        // Só serve pra adicionar na ast o nó com intData = 0
        return new AST(BOOL_VAL_NODE,0,new Inner(BOOL_TYPE,NO_TYPE));
    }
    //List val 
    //É mais complexo lidar com ListVal que os tipos básicos primitivos
    //Para isso vamos sobrescrever os seguintes metodos

    @Override
    public AST visitElements(ElementsContext ctx){
        AST rt = AST.newSubtree(LIST_VAL_NODE,new Inner(LIST_TYPE,NO_TYPE)); //Cria o nó raiz como LIST_VAL_NODE do tipo LIST

        int i = 0;
        Type startType=NO_TYPE;
        while(ctx.element(i)!=null){
            
            AST node = visit(ctx.element(i++)); //caminha pelos outros elementos 
            if(i == 1 ) startType = node.type.getType();
            else {
                if (startType!=node.type.getType()){
                    System.out.printf("SEMANTIC ERROR (%d) - Is not possible use a list values with diferent type of values\n",
                    ctx.element(i-1).getStart().getLine());
                    System.exit(1);
                }
            }
            rt.addChild(node); //adiciona eles na lista
        }

        return rt; // retorna o nó de LIST_VAL_NODE com os valores como filho

    }
    @Override //Essa é a principal para valores de lista
    public AST visitListLiteral(ListLiteralContext ctx){
        if(ctx.typeArguments()!=null){
            visit(ctx.typeArguments()); 
        }
        if(ctx.elements()!=null){
           
            AST node = visit(ctx.elements());
            node.type.defInner(node.getChild(0).type.getType());
            AST.printDot(node,vt);
            System.out.println(vt);
            return node;
        
        } 
        
        return new AST(LIST_VAL_NODE,0,new Inner(LIST_TYPE,NO_TYPE));
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