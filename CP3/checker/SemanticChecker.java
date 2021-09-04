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
import parser.DartParser.RelationalExpContext;
import parser.DartParser.WhileStatementContext;
import parser.DartParser.DoStatementContext;
import parser.DartParser.AdditiveExpressionContext;
import parser.DartParser.PlusOpContext;
import parser.DartParser.MinusOpContext;
import parser.DartParser.TimesOpContext;
import parser.DartParser.OverOpContext;
import parser.DartParser.ModOpContext;
import parser.DartParser.OverIntOpContext;
import parser.DartParser.MultiplicativeExpressionContext;
import parser.DartParser.ForStatementContext;
import parser.DartParser.ForLoopContext;
import parser.DartParser.DeclaredVariableForPartsContext;
import parser.DartParser.ExpForPartsContext;
import parser.DartParser.ExpressionListContext;
import parser.DartParser.NormalFormalParametersContext;
import parser.DartParser.FunctionSignatureContext;
import parser.DartParser.FuncTopLevelContext;
import parser.DartParser.DeclaredIDParameterContext;
import parser.DartParser.SimpleParameterContext;
import parser.DartParser.NormalFormalParameterContext;
import parser.DartParser.BlockFunctionContext;
import parser.DartParser.ReturnStatementContext;
import parser.DartParser.PrimExpressionContext;
import parser.DartParser.FormalParameterPartContext;
import parser.DartParser.NothingParametersContext;
import parser.DartParser.NormalFormalContext;
import parser.DartParser.ArgumentPartSelectorContext;
import parser.DartParser.ArgumentsContext;
import parser.DartParser.ExpressionArgumentsContext;
import parser.DartParser.ArgumentPartContext;
import parser.DartParser.IdentifierSelectorContext;
import parser.DartParser.LocalFunctionDeclarationContext;
import parser.DartParser.VectorAcessContext;
import parser.DartParser.AssignableExpPostfixContext;
import parser.DartParser.PrefixUnaryExpressionContext;
import parser.DartParser.NotMinusUnaryExpressionContext;
import parser.DartParser.AssignSelectExpContext;


import tables.StrTable;
import tables.VarTable;
import tables.FuncTable;
import tables.Key;
import tables.VarTable.*;
import tables.Tree;

import ast.AST.*;
import ast.AST;

import java.util.List;
import java.util.ArrayList;

public class SemanticChecker extends DartBaseVisitor<AST> {

	public final StrTable st = new StrTable();   // Tabela de strings.
    public final VarTable vt = new VarTable();   // Tabela de variáveis.
    public final FuncTable ft = new FuncTable(); // Tabela de funcoes.

    //Adicionando as funções bultin de leitura e impressão de dados
    void addBultin(){
        ft.addFunc("print",0,new Inner(STR_TYPE,NO_TYPE),0,new ArrayList<Inner>(),true);
        ft.addFunc("readLine",0,new Inner(STR_TYPE,NO_TYPE),0,new ArrayList<Inner>(),true);
    }

    Inner lastType; //Variável global com o último tipo declarado
    Token lastVar; //Variável global com o token da ultima variável declarada

    ArrayList <Inner> lastParameters ; //Variável global para armazenar os tipos dos argumentos das funções
    String lastFuncion; //Variável global para armazenar o nome da ultima função (para buscar na etapa de retorno)
                        // isso é usado para verificar se o retorno é valido pelo tipo da função
    
    AST root; //Nó raiz da AST

    int id_escopo_atual = 0;
    int id_escopo_counter = 0;

    Tree scopeTree; // Árvore de escopo

    // Checa se o uso da variável está correto
    AST checkVar(Token token){
        String name = token.getText();
        int line = token.getLine();

        Tree aux_tree = scopeTree;

        while(aux_tree != null && !vt.lookupVar(name,aux_tree.getIdEscopo())) //Busca nos escopos externos
            aux_tree = aux_tree.getPai();

        if(aux_tree == null){  // Se nao encontrar
            System.err.printf("SEMANTIC ERROR (%d): variable '%s' was not declared.\n", line, name);

            System.exit(1);//Aborta o compilador caso haja erro de semantica
            return null; //Nunca executa
        }

        Key k = new Key(name,aux_tree.getIdEscopo());
        return new AST(VAR_USE_NODE,k,vt.getType(k));
    }
    // Checa se o uso da função está correto
    AST checkFunc(Token token, ArrayList<Inner> parameters, AST parametros){ 
        //parametros é a ast com os valores passados
        //parameters é lista dos tipos passados para a função

        String name = token.getText();
        int line = token.getLine();

        Tree aux_tree = scopeTree;
       
        while(aux_tree!= null && !ft.lookupFunc(name,aux_tree.getIdEscopo())){

            aux_tree = aux_tree.getPai();

        
        }
        if(aux_tree == null){ // se não encontrar a função
            System.err.printf("SEMANTIC ERROR (%d): function '%s' was not declared.\n", line, name);

            System.exit(1);//Aborta o compilador caso haja erro de semantica
            return null; //Nunca executa
        }

        Key k = new Key(name,aux_tree.getIdEscopo());
        
        
        int i = 0;

        AST rt = new AST(FUNC_USE_NODE,k,ft.getType(k));

        ArrayList <Inner> parametersOfFunction = ft.getParameters(k);
        if(name.equals("print")){
            if(parametros.getChildren().size()==1)
                rt.addChild(parametros.getChild(0));
            else {
                System.err.printf("SEMANTIC ERROR (%d): Expected just one argument\n",line);
                System.exit(1);
            }
            return rt;
        }
        if(parametersOfFunction.size()!=parameters.size()) { //se o tamanho não é igual então já ta errado
             System.err.printf("SEMANTIC ERROR(%d): Arguments types is incorrect, types passaed is '%s', types waited is '%s'.\n",
                line,parameters, ft.getParameters(k));
                System.exit(1);
                return null;
        }
        while(i<parametersOfFunction.size()){ //verifica cada parametro e o tipo se é double e int para converter 
            if(!parametersOfFunction.get(i).equals(parameters.get(i)) &&  parametersOfFunction.get(i).getType()==DOUBLE_TYPE){
                if(parameters.get(i).getType() == INT_TYPE ) rt.addChild(Conv.createConvNode(I2D,parametros.getChild(i)));
                else {
                    System.err.printf("SEMANTIC ERROR(%d): Arguments types is incorrect, types passaed is '%s', types waited is '%s'.\n",
                    line,parameters, ft.getParameters(k));
                    System.exit(1);
                    return null;
                }
            }
            else if(!parametersOfFunction.get(i).equals(parameters.get(i))){
                System.err.printf("SEMANTIC ERROR(%d): Arguments types is incorrect, types passaed is '%s', types waited is '%s'.\n",
                line,parameters, ft.getParameters(k));
                System.exit(1);
                return null;
            }
            else rt.addChild(parametros.getChild(i));
         i++;
        }
        

        return rt ; //retorma o uso de função 

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
            System.err.printf("SEMANTIC ERROR (%d): incompatible type for var declaration '%s'\n",line,lastVarName);
            System.exit(1);
            return null;
        }
        vt.addVar(lastVarName, line, lastType, id_escopo_atual); 
        AST node = new AST (VAR_DECL_NODE,k,vt.getType(k));
        return node;
    }
    //Adiciona uma nova função na tabela e retorna um nó FUNC_SIGNATURE
    AST newFunction(Token token, boolean bultin){
        String lastFunctionName = token.getText();
        int line = token.getLine();
        ArrayList <Inner> parameters = new ArrayList <Inner> (lastParameters); //cria um novo objeto para diferenciar as referencias

        Key k = new Key(lastFunctionName, id_escopo_atual);

        if(ft.lookupFunc(lastFunctionName,id_escopo_atual) || lastFunctionName.equals("print") || lastFunctionName.equals("readLine")){
            System.err.printf("SEMANTIC ERROR (%d): function '%s' already declared at line %d.\n", 
            line,lastFunctionName,ft.getLine(k));

            System.exit(1);
            return null;
        }

        if(lastType.getType()==NO_TYPE){
            System.err.printf("SEMANTIC ERROR (%d): incompatible type for function delcaration '%s'\n",
            line, lastFunctionName);
            System.exit(1);
            return null;
        }

        ft.addFunc(lastFunctionName, line, lastType, id_escopo_atual, parameters,bultin);
        
        AST node = new AST (FUNC_DECL_NODE,k,ft.getType(k));
        return node;
    }
    
    private static void typeError (int line, String op, Inner t1, Inner t2){
        System.err.printf("SEMANTIC ERROR (%d): incompatible types for operator '%s', LHS is '%s' and RHS is '%s'.\n",
    			line, op, t1.toString(), t2.toString());
    	System.exit(1);
    }

    private static void checkBoolExpr(int lineNo, String cmd, Inner t) {
        if (t.getType() != BOOL_TYPE) {
            System.err.printf("SEMANTIC ERROR (%d): conditional expression in '%s' is '%s' instead of '%s'.\n",
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
            if(listType.equals(new Inner(LIST_TYPE,LIST_TYPE))){
                System.err.printf("SEMANTIC ERROR(%d): List can be declared just with one dimension\n",
                ctx.getStart().getLine());
                System.exit(1);
            }
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
        if(lt.getType() == VOID_TYPE && rt.getType()!= VOID_TYPE) typeError(line,"=",lt,rt);
        if(lt.getType() == LIST_TYPE){
            if(rt.getType() == LIST_TYPE){ 
                if(lt.getInner() == BOOL_TYPE && rt.getInner()!=BOOL_TYPE && rt.getInner()!=NO_TYPE) typeError(line,"=",lt,rt);
                if(lt.getInner() == STR_TYPE && rt.getInner()!=STR_TYPE && rt.getInner()!=NO_TYPE) typeError(line,"=",lt,rt);
                if(lt.getInner() == INT_TYPE && (rt.getInner()!=INT_TYPE && rt.getInner()!=NO_TYPE)) typeError(line,"=",lt,rt);
                if(lt.getInner() == DOUBLE_TYPE && rt.getInner()!=DOUBLE_TYPE && rt.getInner()!=NO_TYPE) typeError(line,"=",lt,rt);
                if(lt.getInner() == VOID_TYPE && rt.getInner()!=VOID_TYPE && rt.getInner()!=NO_TYPE) typeError(line,"=",lt,rt);
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
        AST op = visit(ctx.assignmentOperator()); //Coleta a arvore do meio do operador de atribuição (sem tipo definido, apenas para verificar a seguir o operador)

        //Unificação dos tipos para determinar o tipo resultante
        Type lt = l.type.getType();  
        Type rt = r.type.getType();

        Unif unif;
        
        //Faz a unificação de outros aritmeticos apenas se for atribuições aritimeticas sem ser PLUS pela diferença
        if(op.kind != ASSIGN_PLUS_NODE && op.kind != ASSIGN_NODE){ //se for += tem comportamento diferente (concatena Lista e String)
            unif = lt.unifyOtherArith(rt);
        }
        //Faz a unificação se for PLUS (entretanto se for somente "=" ele tbm faz)
        else {
         
            unif = lt.unifyPlus(rt);
       
        }
        //Por isso só vamos lançar o erro se o operador for diferente de "="
        if(unif.type == NO_TYPE && op.kind!= ASSIGN_NODE){
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
   
        return checkAssign(ctx.assignmentOperator().getStart().getLine(),l,r); // caso seja "=" (tratamos o erro aqui)
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
        AST list_var = new AST(VAR_LIST_NODE,0,new Inner(NO_TYPE,NO_TYPE));
        while(ctx.initializedIdentifier(i) != null)
            list_var.addChild((visit(ctx.initializedIdentifier(i++))));
        
        if (ctx.expression() != null){
            node2 = visit(ctx.expression());
            AST check = checkAssign(ctx.getStart().getLine(),node,node2);
            AST var = AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),check.getChild(0),check.getChild(1));
            if(ctx.initializedIdentifier().size()!=0){
                list_var.addChild(var);
                return list_var;
            }
            else return var;
        }
        
        AST var = AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),node);
        if(ctx.initializedIdentifier().size()!=0){
            list_var.addChild(var);
            return list_var;
        }
        return var;
    }
    //Regra de declaração de função no nível global
    @Override
    public AST visitFuncTopLevel(FuncTopLevelContext ctx){
        lastParameters = new ArrayList <Inner> ();
        String old_name_function = lastFuncion;
        AST node1 = visit(ctx.functionSignature()); //pega a assinatura tipo nome e parametros
        AST node2 = visit(ctx.functionBody()); //pega o corpo da função

        List <AST> children = node1.getChildren(); //é passado um nó block fake para pegar func_signature_node
                                                //e func_parameters_node

        AST root = new AST (INSTANCE_FUNCTION_NODE, 0 ,new Inner(NO_TYPE,NO_TYPE));
        for(AST child : children){
            root.addChild(child);
        }

        root.addChild(node2);
        
        lastFuncion = old_name_function;

        return root;
    }
    //Assinatura da função
    @Override
    public AST visitFunctionSignature(FunctionSignatureContext ctx){
        if(ctx.type()!=null) visit(ctx.type());
        visit(ctx.identifierNotFUNCTION());
        AST node2 = visit(ctx.formalParameterPart());
        AST node1 = newFunction(lastVar,false);

        lastFuncion = lastVar.getText();
        
        return AST.newSubtree(BLOCK_NODE,new Inner(NO_TYPE,NO_TYPE),node1,node2);//apenas para juntar na declaração    
    }
    //Parametros
    @Override //função raiz que chama os parametros
    public AST visitFormalParameterPart(FormalParameterPartContext ctx){
        if(ctx.typeParameters()!=null) visit(ctx.typeParameters());
        return visit(ctx.formalParameterList());
    }
    @Override// #nothingParameters "type name_function () {} "
    public AST visitNothingParameters(NothingParametersContext ctx){
        return new AST(FUNCTION_PARAMETER_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    @Override // #normalFormal parametros normais "type name_function (int x, int y) {} "
    public AST visitNormalFormal(NormalFormalContext ctx){
        return visit(ctx.normalFormalParameters());
    }
    @Override //deriva os parametros formais normais 
    public AST visitNormalFormalParameters(NormalFormalParametersContext ctx){
        Inner typeOfFunction = lastType;//salvando o tipo da função
        Token lastNameFunction = lastVar; //salvando o nome da função

        AST rt = new AST (FUNCTION_PARAMETER_NODE,0,new Inner(NO_TYPE,NO_TYPE));
        
        
        
        int old_id_escopo = id_escopo_atual; //salvando o escopo atual


        id_escopo_atual = id_escopo_counter+1; //gerando o prox escopo forçado para os parametros

        scopeTree = scopeTree.addChild(id_escopo_atual);
        
        int i = 0;
        while(ctx.normalFormalParameter(i)!=null){
            AST node = visit(ctx.normalFormalParameter(i++));
            rt.addChild(node); //adicionando os parametros na tabela e como filhos
            lastParameters.add(lastType); //adicionando os tipos dos parametros
        }


        id_escopo_atual = old_id_escopo; //retornando o escopo atual
        scopeTree = scopeTree.getPai(); //retortando a arvore de escopo

        lastType = typeOfFunction; //retornando o tipo da função
        lastVar = lastNameFunction; //retornando o nome da função

        return rt;
    }
    //Sobrescrevendo normalFormalParameter para passar AST de newvar
    @Override
    public AST visitNormalFormalParameter(NormalFormalParameterContext ctx){
        return visit(ctx.normalFormalParameterNoMetadata());
    }
    //Sobrescrevendo simpleFormalParameter #simpleParameter
    @Override
    public AST visitSimpleParameter(SimpleParameterContext ctx){
        return visit(ctx.simpleFormalParameter());
    }
    //Sobrescrevendo simplerFormalParameter : declaredIdentifier #declaredIDParameter
    @Override
    public AST visitDeclaredIDParameter(DeclaredIDParameterContext ctx){
        return visit(ctx.declaredIdentifier());
    }
    //Pegando o corpo da função
    @Override
    public AST visitBlockFunction(BlockFunctionContext ctx){
        AST node =  visit(ctx.block());
        return node;
    }
    //Pegando retorno
    @Override
    public AST visitReturnStatement(ReturnStatementContext ctx){
        AST retorno = new AST(FUNCTION_RETURN_NODE,0,new Inner(NO_TYPE,NO_TYPE)); 
        
        Tree aux_scope = scopeTree;
        int id_func=0;

        while(aux_scope!=null && !ft.lookupFunc(lastFuncion,aux_scope.getIdEscopo())) aux_scope=aux_scope.getPai();
        
        if(aux_scope!=null)
            id_func = aux_scope.getIdEscopo();
        
        Key k = new Key (lastFuncion,id_func); //pegando a chave para verificar questões de tipo de retorno

        if(ctx.expression()==null && ft.getType(k).getType()!=VOID_TYPE){ //retornos sem expressão só é permitido em void function
            System.err.printf("SEMANTIC ERROR (%d): return with not type just can used in void type function and type is '%s' from function '%s' \n",
            ctx.getStart().getLine(), ft.getType(k), lastFuncion
            );
            System.exit(1);
            return null;
        }

        if(ctx.expression()!=null) {
         
            AST child = visit(ctx.expression());

            if(child.type.getType() == INT_TYPE && ft.getType(k).getType()==DOUBLE_TYPE){ //se o tipo de retorno for int 
                                                                                    //mas o tipo da função for double, convertemos o retorno

                AST conv = Conv.createConvNode(I2D,child);
                retorno.addChild(conv);

            }
            
            else retorno.addChild(child);

            if(retorno.getChild(0).type.getType() != ft.getType(k).getType()) { //retornos devem ser do mesmo tipo da função
                System.err.printf("SEMANTIC ERROR (%d): return type '%s' is invalid to '%s' type from function '%s' \n",
                ctx.expression().getStart().getLine(), retorno.getChild(0).type.getType(), ft.getType(k), lastFuncion
                );
                System.exit(1);
                return null;
            } 
        
        
        }
        

        return retorno;
    }
    //(expression) #primExpression
    @Override
    public AST visitPrimExpression(PrimExpressionContext ctx){
        return visit(ctx.expression());
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
       
        int i = 0;
        AST list_var = new AST(VAR_LIST_NODE,0,new Inner(NO_TYPE,NO_TYPE));

        while(ctx.initializedIdentifier(i)!=null){
            list_var.addChild(visit(ctx.initializedIdentifier(i++)));
        }
        
        if(ctx.expression()!=null){
            node2 = visit(ctx.expression());
            AST check = checkAssign(ctx.getStart().getLine(),node,node2);
            AST var = AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),check.getChild(0),check.getChild(1));
            if(ctx.initializedIdentifier().size()!=0){
                list_var.addChild(var);
                return list_var;
            }
            else return var;
        }
        AST var =  AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),node);
        if(ctx.initializedIdentifier().size()!=0){
            list_var.addChild(var);
            return list_var;
        }
        return var;
    }
    //Regra para salvar também int x, y, b, c, ....;
    @Override
    public AST visitInitializedIdentifier(InitializedIdentifierContext ctx){
        visit(ctx.identifier());
        AST node = newVar(lastVar);
        AST node2;
        if(ctx.expression() != null){
            node2 = visit(ctx.expression());
            AST check = checkAssign(ctx.getStart().getLine(),node,node2);
            return AST.newSubtree(INSTANCE_VAR_NODE,new Inner(NO_TYPE,NO_TYPE),check.getChild(0),check.getChild(1));
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
    // -------------------- Declaração local de função -------------
    @Override
    public AST visitLocalFunctionDeclaration(LocalFunctionDeclarationContext ctx){
        lastParameters = new ArrayList <Inner> ();
        String old_name_function = lastFuncion;
        AST child1 = visit(ctx.functionSignature());
        AST child2 = visit(ctx.functionBody());

        AST rt = new AST(INSTANCE_FUNCTION_NODE,0,new Inner(NO_TYPE,NO_TYPE));

        List <AST> children = child1.getChildren();

        for(AST child:children){
            rt.addChild(child);
        }
        rt.addChild(child2);

        lastFuncion = old_name_function;
        return rt;
    }
    // ------------------- Uso de variáveis ----------------
    // todas variáveis usadas aciona a chamada --> primary : identifier #primIdentifier
    public AST visitPrimIdentifier(PrimIdentifierContext ctx){
        visit(ctx.identifier());

        return null;
    } // incorporado logo abaixo pois tanto funções e variáveis são chamadas pela mesma raiz
    // ------------------- Uso de funções/Lista/Variáveis/primary (expressões etc) -----------------
    public AST visitIdentifierSelector(IdentifierSelectorContext ctx){
        AST verify = visit(ctx.primary());
        int i = 0;
        
        if(ctx.selector()!=null && ctx.selector().size()>1){

            AST node = visit(ctx.selector(0));
            if(node.kind==FUNC_USE_ARGUMENTS_NODE){
                System.err.printf("SEMANTIC ERROR (%d): function call is incorrect, just use 'name_function(arguments);'\n",
                ctx.getStart().getLine());
                System.exit(1);
            }
            else if(node.kind==LIST_USE){
                System.err.printf("SEMANTIC ERROR (%d): List is just one dimension, this mean, use 'name_var[index];'\n",
                ctx.getStart().getLine());
                System.exit(1);
            }
            else {
                System.err.printf("SEMANTIC ERROR (%d): undefined use\n",
                ctx.getStart().getLine());
                System.exit(1);
            }
        }

        if(ctx.selector()!=null && ctx.selector().size()==1){ //verifico se existe um selector
            Token lastFunc = lastVar;
            AST node = visit(ctx.selector(0));
            if(node.kind==FUNC_USE_ARGUMENTS_NODE){ //e se ele é chamada de função
                AST rt = checkFunc(lastFunc,lastParameters,node); //cria o no de uso de função checando os parametros
                
                return rt;
            }
            else if(node.kind==LIST_USE){
                AST rt = checkVar(lastVar);
                if(rt.type.getType()!=LIST_TYPE){
                    System.err.printf("SEMANTIC ERROR (%d): the variable '%s' is not a List\n",
                    ctx.getStart().getLine(),vt.getName(rt.key));
                    System.exit(1);
                }
                rt = new AST(rt.kind,rt.key,new Inner(rt.type.getInner(),NO_TYPE));
                rt.addChild(node.getChild(0));

                return rt;
            }
        }
        if(verify!=null){
            return verify;
        } 

        return checkVar(lastVar);
    }

    //argumentos do uso de funçao selector : argumentPart 
    @Override
    public AST visitArgumentPartSelector(ArgumentPartSelectorContext ctx){
        AST node = visit(ctx.argumentPart()); //pega um no bloco que reune expressões
                                            // é um nó falso para manusear
        
        AST argumentList = new AST(FUNC_USE_ARGUMENTS_NODE,0,new Inner(NO_TYPE,NO_TYPE)); //cria o nó de argumentos
       lastParameters = new ArrayList<Inner>();
        if (node != null){ //se não houver argumentos é nulo
            List<AST> children = node.getChildren(); 

            for(AST child:children){
                argumentList.addChild(child); //adiciona os arguemntos
                lastParameters.add(child.type);
            }
        }
        return argumentList;
    }
    //argumentPart body
    @Override
    public AST visitArgumentPart(ArgumentPartContext ctx){
      
        if(ctx.typeArguments()!=null) visit(ctx.typeArguments());
        return visit(ctx.arguments());
    }
    //arguments body
    @Override
    public AST visitArguments(ArgumentsContext ctx){
        if(ctx.argumentList()!=null)
            return visit(ctx.argumentList());
        
        return null;
    }
    //argumentList body
    @Override
    public AST visitExpressionArguments(ExpressionArgumentsContext ctx){
        int i = 0;
        while (ctx.namedArgument(i)!=null) visit(ctx.namedArgument(i++));
        return visit(ctx.expressionList());
    }

    //---------------------- USO DE LISTA ----------------
    @Override 
    public AST visitVectorAcess(VectorAcessContext ctx){
        AST rt = new AST(LIST_USE,0, new Inner(NO_TYPE,NO_TYPE));
        rt.addChild(visit(ctx.expression()));
        return rt;
    }



    //--------------------- Novo Escopo -------------------
    @Override
    public AST visitBlock(BlockContext ctx){
        int old_id_escopo = id_escopo_atual;//salva escopo atual
        
        id_escopo_atual = ++id_escopo_counter; // Gera novo escopo: aumenta contador e atribui ao escopo atual
        scopeTree = scopeTree.addChild(id_escopo_atual); // Adiciona um filho ao escopo atual e agora o escopo atual
                                                        // é a referencia pro nó filho
        
        AST node = visit(ctx.statements());


        id_escopo_atual = old_id_escopo; // Ao fim, sai desse escopo, logo decrementa o escopo atual
        
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
        if (thenNode == null) return AST.newSubtree(IF_NODE, new Inner(NO_TYPE,NO_TYPE), exprNode);

        AST subTreeIFNode;
        //O segundo statement, se existir, é do else
        if(ctx.ELSE() != null){
            AST elseNode = visit(ctx.statement(1));

            subTreeIFNode = AST.newSubtree(IF_NODE, new Inner(NO_TYPE,NO_TYPE), exprNode, thenNode, elseNode);
        }else
            subTreeIFNode = AST.newSubtree(IF_NODE, new Inner(NO_TYPE,NO_TYPE), exprNode, thenNode);

        return subTreeIFNode;
    }

    // ------------------- WHILE E DO WHILE --------------------
    @Override
    public AST visitWhileStatement(WhileStatementContext ctx){

        // Analisa expressao booleana
        AST exprNode = visit(ctx.expression());
        checkBoolExpr(ctx.WHILE().getSymbol().getLine(), "while", exprNode.type);

        AST whileNode = visit(ctx.statement());
        if (whileNode == null) return AST.newSubtree(WHILE_NODE, new Inner(NO_TYPE,NO_TYPE), exprNode);

        return AST.newSubtree(WHILE_NODE, new Inner(NO_TYPE, NO_TYPE), exprNode, whileNode);
    }

    @Override
    public AST visitDoStatement(DoStatementContext ctx){
        // Analisa expressao booleana
        AST exprNode = visit(ctx.expression());
        checkBoolExpr(ctx.WHILE().getSymbol().getLine(), "do-while", exprNode.type);

        AST doWhileNode = visit(ctx.statement());

        return AST.newSubtree(DO_WHILE_NODE, new Inner(NO_TYPE, NO_TYPE), doWhileNode, exprNode);
    }
    
    // --------------------- FOR ----------------------------
    @Override
    public AST visitForStatement(ForStatementContext ctx){

        int old_id_escopo = id_escopo_atual; //salvando escopo atual

        id_escopo_atual = id_escopo_counter+1; //apenas forçando que o parametro do for caso seja declarado
                        //dentro do () seja do escopo interno do for
        scopeTree = scopeTree.addChild(id_escopo_atual);       
        
        AST child1 = visit(ctx.forLoopParts());
        AST child2 = visit(ctx.statement());
        
        if((child2!=null && child2.kind==INSTANCE_VAR_NODE)||(child1.getChildren().size()!=0 && child1.getChild(0).kind==INSTANCE_VAR_NODE && (child2==null || (child2!= null && child2.kind!=BLOCK_NODE)))) {
            id_escopo_counter++;
            scopeTree = scopeTree.getPai();
        }
        else if((child2!=null && child2.kind!=BLOCK_NODE)||child2==null) scopeTree = scopeTree.getPai();
        id_escopo_atual = old_id_escopo; //retornando ao escopo atual correto
        
        if(child1.getChildren().size()!=0){
            AST node;
            AST child1New = new AST(FOR_PARTS_NODE,0,new Inner(NO_TYPE,NO_TYPE));
            for (AST child : child1.getChildren()){
                if (child.kind==FOR_EXP_NODE){
                    node = child;
                    if(child2==null){
                        child1 = child1New;
                        AST block = new AST(BLOCK_NODE,0,new Inner(NO_TYPE,NO_TYPE));

                        for(AST n:node.getChildren()){
                            block.addChild(n);
                        }
                        return AST.newSubtree(FOR_NODE,new Inner(NO_TYPE,NO_TYPE),child1,block);
                    }
                    else {
                        for(AST n:node.getChildren()){
                            child2.addChild(n);
                        }
                    }
                }
                else child1New.addChild(child);
            }   
            child1 = child1New;
        }
        if(child2==null)
            return AST.newSubtree(FOR_NODE,new Inner(NO_TYPE,NO_TYPE),child1);
            
        return AST.newSubtree(FOR_NODE,new Inner(NO_TYPE,NO_TYPE),child1,child2);
       
    }
    //For (parameters)
    @Override
    public AST visitForLoop(ForLoopContext ctx){

        AST child1 = visit(ctx.forInitializerStatement());
        AST child2 = null;
        AST child3 = null;

        if(ctx.expression()!=null) {
            child2 = visit(ctx.expression());
            
            if(child2.type.getType()!= BOOL_TYPE){
                System.out.printf("SEMANTIC ERROR (%d): Second part of parameters to for loop need be a boolean expression!\n",
                ctx.getStart().getLine());
                System.exit(1);
            }
        }

        if(ctx.expressionList()!=null)  child3 = visit(ctx.expressionList());
        
        
        if(child2!=null && child3!=null && child1!=null) return AST.newSubtree(FOR_PARTS_NODE,new Inner(NO_TYPE,NO_TYPE),child1,child2,child3);
   
        else if(child2!=null && child1!=null && child3==null) return AST.newSubtree(FOR_PARTS_NODE,new Inner(NO_TYPE,NO_TYPE),child1,child2);
        else if(child3!=null && child1!=null && child2==null) return AST.newSubtree(FOR_PARTS_NODE,new Inner(NO_TYPE,NO_TYPE),child1,child3);
      
        else if(child2!=null && child3!=null && child1==null)return  AST.newSubtree(FOR_PARTS_NODE,new Inner(NO_TYPE,NO_TYPE),child2,child3);
  
        else if(child1!= null && child2==null && child3==null) return AST.newSubtree(FOR_PARTS_NODE,new Inner(NO_TYPE,NO_TYPE),child1);
        else if(child2!=null && child1==null && child3==null) return AST.newSubtree(FOR_PARTS_NODE,new Inner(NO_TYPE,NO_TYPE),child2);
        else if(child3!=null && child1==null && child2==null) return AST.newSubtree(FOR_PARTS_NODE,new Inner(NO_TYPE,NO_TYPE),child3);
     

        return new AST(FOR_PARTS_NODE,0,new Inner(NO_TYPE,NO_TYPE));

    }
    //parameter for 1
    @Override
    public AST visitDeclaredVariableForParts(DeclaredVariableForPartsContext ctx){
        AST node = visit(ctx.localVariableDeclaration());

        return node;
    }
    @Override 
    public AST visitExpForParts(ExpForPartsContext ctx){
        if(ctx.expression()!=null) return visit(ctx.expression());
        return null;
    }
    //parameter for 2 is expression 

    //parameter for 3
    @Override
    public AST visitExpressionList(ExpressionListContext ctx){
        AST node =  new AST(FOR_EXP_NODE,0,new Inner(NO_TYPE,NO_TYPE));//Usado para transferir
                                                                //As expressões pro body do for

        int i = 0;
        while (ctx.expression(i)!=null){
            node.addChild(visit(ctx.expression(i++)));
        }
        return node;
    }
    

    // ------------------- Operadores -----------------------
    
    //Aritmeticos

    //PLUS / MINUS
    @Override
    public AST visitAdditiveExpression (AdditiveExpressionContext ctx){

        AST node1 = visit(ctx.multiplicativeExpression(0)); //pega o primeiro elemento
                                                            // que pode ser um derivada de multiplicação ou uma variavel ou literal
 
        if(ctx.additiveOperator(0)==null){ //se não tem + ou -, então é multiplicação
           return node1;
        }
        
        int i = ctx.additiveOperator().size(); //pega o tamanho das somas 
        AST old_node2 = null; 
        while(i>=1){ //É uma operação de adição 
            AST op = visit(ctx.additiveOperator(i-1)); //pega o operador de adição
          

            if (i == ctx.additiveOperator().size()){ //se for o ultimo
                AST node2 = visit(ctx.multiplicativeExpression(i)); //pega o ultimo valor
                AST nd1 = visit(ctx.multiplicativeExpression(i-1)); //pega o penultimo valor

                //realiza unificação
                Type lt = nd1.type.getType();
                Type rt = node2.type.getType();
                Unif unif;

                if(op.kind == PLUS_NODE){
                    unif = lt.unifyPlus(rt);
                }
                else {
                    unif = lt.unifyOtherArith(rt);
                }
                if(unif.type == NO_TYPE) typeError(ctx.additiveOperator(i-1).getStart().getLine(),op.kind.toString(),nd1.type,node2.type);
                //realiza as conversões
                nd1 = Conv.createConvNode(unif.lc,nd1);
                node2 = Conv.createConvNode(unif.rc,node2);
                //atualiza o nó raiz
                old_node2 = AST.newSubtree(op.kind,new Inner(unif.type,node2.type.getInner()),nd1,node2);
            }
            else { //se não for o ultimo
            AST nd1 = visit(ctx.multiplicativeExpression(i-1)); //vai pegando o valor anterior

            //Faz a unificação
            Type lt = nd1.type.getType();   
            Type rt = old_node2.type.getType();
            Unif unif;

            if(op.kind == PLUS_NODE){
                unif = lt.unifyPlus(rt);
            }
            else {
                unif = lt.unifyOtherArith(rt);
            }
            if(unif.type == NO_TYPE) typeError(ctx.additiveOperator(i-1).getStart().getLine(),op.kind.toString(),nd1.type,old_node2.type);
            //Conversão
            nd1 = Conv.createConvNode(unif.lc,nd1);
            old_node2 = Conv.createConvNode(unif.rc,old_node2);

            //Atualiza o nó raiz
            old_node2 = AST.newSubtree(op.kind,new Inner(unif.type,old_node2.type.getInner()),nd1,old_node2);

            }
            i--;
        }
        return old_node2;
    } 

    //Operadores +, - 

    @Override
    public AST visitPlusOp(PlusOpContext ctx){
        return new AST(PLUS_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    @Override
    public AST visitMinusOp(MinusOpContext ctx){
        return new AST(MINUS_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }

    //OVER / TIMES / MOD / OVER_INT
    //Mesmo esquema que o additiveMethod 
    @Override
    public AST visitMultiplicativeExpression(MultiplicativeExpressionContext ctx){
        AST node1 = visit(ctx.unaryExpression(0));

        if(ctx.multiplicativeOperator(0)==null){
           return node1;
        }
        
        int i = ctx.multiplicativeOperator().size();
        AST old_node2 = null;
        while(i>=1){
            AST op = visit(ctx.multiplicativeOperator(i-1));
          

            if (i == ctx.multiplicativeOperator().size()){
                AST node2 = visit(ctx.unaryExpression(i));
                AST nd1 = visit(ctx.unaryExpression(i-1));

                Type lt = nd1.type.getType();
                Type rt = node2.type.getType();
                Unif unif;

                unif = lt.unifyOtherArith(rt);
                Type tipo = NO_TYPE;
                if(op.kind==OVER_INT_NODE) tipo = INT_TYPE;
                else if(unif.type == NO_TYPE)typeError(ctx.multiplicativeOperator(i-1).getStart().getLine(),op.kind.toString(),nd1.type,node2.type);
                else tipo = unif.type;
                nd1 = Conv.createConvNode(unif.lc,nd1);
                node2 = Conv.createConvNode(unif.rc,node2);

                old_node2 = AST.newSubtree(op.kind,new Inner(tipo,node2.type.getInner()),nd1,node2);
            }
            else {
            AST nd1 = visit(ctx.unaryExpression(i-1));

            Type lt = nd1.type.getType();
            Type rt = old_node2.type.getType();
            Unif unif;

        
            unif = lt.unifyOtherArith(rt);
            Type tipo = NO_TYPE;
            if(op.kind==OVER_INT_NODE) tipo = INT_TYPE;
            else if(unif.type == NO_TYPE) typeError(ctx.multiplicativeOperator(i-1).getStart().getLine(),op.kind.toString(),nd1.type,old_node2.type);
            else tipo = unif.type;
            nd1 = Conv.createConvNode(unif.lc,nd1);
            old_node2 = Conv.createConvNode(unif.rc,old_node2);

            old_node2 = AST.newSubtree(op.kind,new Inner(tipo,old_node2.type.getInner()),nd1,old_node2);

            }
            i--;
        }
        return old_node2;
    }
    // *, / , % , ~/
    @Override
    public AST visitTimesOp(TimesOpContext ctx){
        return new AST(TIMES_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    @Override
    public AST visitOverOp(OverOpContext ctx){
        return new AST(OVER_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    @Override
    public AST visitModOp(ModOpContext ctx){
        return new AST(MOD_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }
    @Override
    public AST visitOverIntOp(OverIntOpContext ctx){
        return new AST(OVER_INT_NODE,0,new Inner(NO_TYPE,NO_TYPE));
    }

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
                typeError(ctx.getStart().getLine(), "||", lastChild.type, child.type);

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
                typeError(ctx.getStart().getLine(), "&&", lastChild.type, child.type);

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
            typeError(ctx.getStart().getLine(), operator, child1.type, child2.type);

        return AST.newSubtree(equalityNode, new Inner(unif.type,NO_TYPE), child1, child2);
    }

    @Override
    public AST visitRelationalExp(RelationalExpContext ctx){
        if (ctx.bitwiseOrExpression(1) == null)     // quer dizer que nao tem operador
            return visit(ctx.bitwiseOrExpression(0));

        String operator = ctx.relationalOperator().getText();
        NodeKind comparationalNode = null;

        switch (operator){
            case ">":
                comparationalNode = GT_NODE;
                break;
            case ">=":
                comparationalNode = GEQ_NODE;
                break;
            case "<":
                comparationalNode = LT_NODE;
                break;
            case "<=":
                comparationalNode = LEQ_NODE;
                break;
        }

        AST child1 = visit(ctx.bitwiseOrExpression(0));
        AST child2 = visit(ctx.bitwiseOrExpression(1));

        // Por causa do tipo void, devemos fazer essa verificação
        Type lt = child1.type.getType();
        Type rt = child2.type.getType();
        Unif unif = lt.unifyComp(rt);

        if (unif.type == NO_TYPE)
            typeError(ctx.getStart().getLine(), operator, child1.type, child2.type);

        return AST.newSubtree(comparationalNode, new Inner(unif.type,NO_TYPE), child1, child2);
    }

    //      Operadores Unários

    // Incrementais

    @Override
    public AST visitAssignableExpPostfix(AssignableExpPostfixContext ctx){
        AST varNode = visit(ctx.assignableExpression());
        Type var_type = varNode.type.getType();

        // Pega qual operador que é: ++ ou --
        String operator = ctx.postfixOperator().incrementOperator().getText();
        NodeKind opKind;

        if(operator.equals("++")) opKind = PLUS_POST_INCR_VAL_NODE;
        else opKind = MINUS_POST_INCR_VAL_NODE;

        // Unificacao de tipo
        Unif unif = var_type.unifyPlus(INT_TYPE); // Porque incrementa ou decrementa 1
        if (unif.type == NO_TYPE)
            typeError(ctx.getStart().getLine(), operator, varNode.type, new Inner(INT_TYPE,NO_TYPE));

        return AST.newSubtree(opKind, new Inner(unif.type,NO_TYPE), varNode);
    }

    @Override
    public AST visitPrefixUnaryExpression(PrefixUnaryExpressionContext ctx){
        AST varNode = visit(ctx.assignableExpression());
        Type var_type = varNode.type.getType();

        // Pega qual é o operador: ++ ou --
        String operator = ctx.incrementOperator().getText();
        NodeKind opKind;

        if(operator.equals("++")) opKind = PLUS_PRE_INCR_VAL_NODE;
        else opKind = MINUS_PRE_INCR_VAL_NODE;

        // Unificacao de tipo
        Unif unif = var_type.unifyPlus(INT_TYPE); // Porque incrementa ou decrementa 1
        if (unif.type == NO_TYPE)
            typeError(ctx.getStart().getLine(), operator, new Inner(INT_TYPE,NO_TYPE), varNode.type);

        return AST.newSubtree(opKind, new Inner(unif.type,NO_TYPE), varNode);
    }

    // Negação booleana e numerica

    @Override
    public AST visitNotMinusUnaryExpression(NotMinusUnaryExpressionContext ctx){
        AST varNode = visit(ctx.unaryExpression());
        Type var_type = varNode.type.getType();

        // Pega qual é o operador: ! ou -
        String operator = ctx.prefixOperator().getText();
        NodeKind opKind;
        Unif unif;
        Inner opType;

        if(operator.equals("!")){
            opKind = NOT_NODE;
            unif = var_type.unifyBooleans(BOOL_TYPE); // Esse operador vale apenas para booleanos
            opType = new Inner(BOOL_TYPE,NO_TYPE);
        }else {
            opKind = MINUS_NODE;
            unif = var_type.unifyOtherArith(INT_TYPE); // Esse operador vale apenas para tipos numericos
            opType = new Inner(INT_TYPE,NO_TYPE);
        }

        // Verificando se a unificação é valida
        if (unif.type == NO_TYPE)
            typeError(ctx.getStart().getLine(), operator, opType, varNode.type);

        return AST.newSubtree(opKind, new Inner(unif.type,NO_TYPE), varNode);
    }

    //AssignSelector outra regra que deriva uso de lista

    @Override
    public AST visitAssignSelectExp(AssignSelectExpContext ctx){
        AST node = visit(ctx.primary());
        AST node2 = visit(ctx.assignableSelectorPart());

        if(node2.kind==LIST_USE){
           AST rt = checkVar(lastVar);
                if(rt.type.getType()!=LIST_TYPE){
                    System.err.printf("SEMANTIC ERROR (%d): the variable '%s' is not a List\n",
                    ctx.getStart().getLine(),vt.getName(rt.key));
                    System.exit(1);
                }
                rt = new AST(rt.kind,rt.key,new Inner(rt.type.getInner(),NO_TYPE));
                rt.addChild(node2.getChild(0));

                return rt;
            
        }
    
        return node;

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
                    System.err.printf("SEMANTIC ERROR (%d) - Is not possible use a list values with diferent type of values\n",
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

            if(node.type.equals(new Inner(LIST_TYPE,LIST_TYPE))){
                System.err.printf("SEMANTIC ERROR (%d): Is not possible use List of more one dimension\n",
                ctx.getStart().getLine());
                System.exit(1);
            }

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
        AST.printDot(root,vt,ft);
    }

    boolean findMain(){
        return ft.lookupFunc("main", 0);
    }
    public AST getAST(){
        return root;
    }
}