package code;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Scanner;

import ast.AST;

import ast.ASTBaseVisitor;

import typing.*;
import static typing.Type.*;

import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.FileOutputStream;
import tables.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Scanner;
import java.io.InputStream;
public class CodeGenerator extends ASTBaseVisitor <Void>{

    public final VarTable vt; //tabela de variáveis
    public final FuncTable ft; //tabela de funções
    public final StrTable st; //tabela de strings
    public final String name; //nome do arquivo de teste (usado para gerar o binario final name.class)
    public final ClassWriter cw =  new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS); //classe principal do programa (gera os metodos e campos)
    
    MethodVisitor mv; //Variável global para construir funções
    boolean incrementalValueIsneed=false; //para saber quando colocar o valor de um ++ ou -- na pilha
    Hashtable <Key,Boolean> tabelaDeDefinicoes = new Hashtable <Key,Boolean> ();//auxiliar para anasile de uso de variável sem valor

    //Construtor
    public CodeGenerator(StrTable st, VarTable vt, FuncTable ft,String name){
        this.vt = vt;
        this.ft = ft;
        this.st = st;
        this.name = name;
        cw.visit(V1_8,ACC_PUBLIC+ACC_STATIC,this.name,null,"java/lang/Object",null); //Cria a classe principal com nome do arquivo teste
        cw.visitField(ACC_PUBLIC+ACC_FINAL+ACC_STATIC,"in","Ljava/util/Scanner;",null,null).visitEnd(); //Atributo scanner para auxiliar na leitura de dados do teclado

    }

    @Override
    protected Void visitGlobal(AST node){ //Visita o nó raiz global
        // Cria a main no global para englobar todas as variaveis e funcoes globais
        mv = cw.visitMethod(ACC_PUBLIC+ACC_STATIC, "main","([Ljava/lang/String;)V",null,null);

        mv.visitCode();//Sinaliza que vai construir o corpo da função
        
        //Inicializando o Scanner para leitura de entrada
        mv.visitTypeInsn(NEW,"java/util/Scanner"); //Instanciando um novo objeto do tipo scanner
        mv.visitInsn(DUP); //duplicando o valor da referencia, um vai servir para a chamada do construtor, o outro vai ser para retorno do objeto
        mv.visitFieldInsn(GETSTATIC,"java/lang/System","in","Ljava/io/InputStream;"); //Parametro System.in
        mv.visitMethodInsn(INVOKESPECIAL,"java/util/Scanner","<init>","(Ljava/io/InputStream;)V",false); //Chamo o construtor do Scanner que espera o System.in
        mv.visitFieldInsn(PUTSTATIC,this.name,"in","Ljava/util/Scanner;"); //Atribuo o objeto para o atributo in de leitura da classe principal

        concatIntListCreatStruct();//cria uma função em baixo nível de concatenar listas de inteiros
        concatDoubleListCreatStruct();//cria uma função em baixo nível de concatena listas de double
        concatStringListCreatStruct();//cria uma função em baixo nível de concatena listas de string
        concatBoolListCreatStruct();//cria uma função em baixo nível de concatena listas de bool
        int i = 0;
        while(node.getChild(i)!=null){ 
            visit(node.getChild(i++)); //enquanto houver filhos 
        }

        mv.visitInsn(RETURN); // retorno void
        mv.visitEnd(); //Sinalizando a finalização do código da main
        mv.visitMaxs(-1,-1); //Como foi escolhido a computação automatica isso vai ser ignorado
        //API obriga a chamada do metodo

        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        try(FileOutputStream stream = new FileOutputStream("".concat(name).concat(".class"))){
            stream.write(bytes);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;


    }

    String nameFunction; //Global para armazenar o nome da função
    Key chaveFunction;//Global para armazenar a chave da função (acesso a tabela)


    @Override
    protected Void visitInstanceFunc(AST node){
        MethodVisitor oldMV = mv; //Salvando a referência do escritor de funções antigo
                                //como podemos ter funções locais, sempre que for declarar função
                                //é interessante salvar a referência antiga para recuperar após a finalização
        String oldNameFunction = nameFunction;//salvando o nome da função anterior (tratamentos)
        Key oldChaveFunction = chaveFunction;//salvando a chave da função anterior (tratamentos)

        visit(node.getChild(0)); //visita a declaração (nome e tipo de retorno)       
      
        Inner typeRetorno = ft.getType(chaveFunction); //pega o tipo de retorno
        ArrayList <Inner> parameters = ft.getParameters(chaveFunction); //pega os tipos dos parametross

        String metodoD = metodoDescriptor(parameters,typeRetorno); //gera o method descriptor

        if(nameFunction.equals("main") && chaveFunction.getId()==0){ //Caso a função declarada seja a main
                                                            //vamos apenas visitar o corpo
                                                            //estamos tratando a main no visitGlobal
                                                            //hacker para tratar de todas operações globais com a api

            visit(node.getChild(1)); // Visita parametros
            visit(node.getChild(2)); //Visita o corpo da função

            return null;
           
        }
        else{ //caso não seja a main de escopo global o nome do metodo usa o ID (para diferenciar funções locais de globais)
            nameFunction = nameFunction.concat(Integer.toString(chaveFunction.getId()));
            mv = cw.visitMethod(ACC_PUBLIC+ACC_STATIC, nameFunction,metodoD,null,null);
            mv.visitCode();//Sinaliza que vai construir o corpo da função
        }

        visit(node.getChild(1)); //visita os parametros (adicionar na hash de variáveis locais da função)
                    // exemplo media(int x,int y) varlocal = {x:1,y:2}
                    // Doubles e Objetos são armazenados em duas posições
        
        visit(node.getChild(2)); //Visita o corpo da função
        
        if(ft.getType(chaveFunction).getType()==VOID_TYPE){
            mv.visitInsn(RETURN);
        }
        
        mv.visitEnd(); //Sinalizando a finalização do código (função)

        mv.visitMaxs(-1,-1); //Como foi escolhido a computação automatica isso vai ser ignorado
                            //API obriga a chamada do metodo

        mv = oldMV; //Recupera a referência do método antigo
        nameFunction=oldNameFunction;//recupera o nome da função antiga
        chaveFunction=oldChaveFunction;//recupera a chave da função antiga

        return null;
    
    }

   
    //visita o nó de declaração de função conte a chave de busca
    @Override
    protected Void visitFuncDecl(AST node){

        nameFunction = ft.getName(node.key); //salva o nome da função
        chaveFunction = node.key; //salva a chave

        return null;

    }

    //como definimos que todas variaveis incluindo parametros fazem parte do corpo da classe principal
    //assim que chamarmos o método vamos puxar os valores e colocar eles nos campos da classe
    @Override
    protected Void visitFuncParameter(AST node){
        int i = 0;
        
        while(node.getChild(i)!=null){
            visit(node.getChild(i));//declaração do parametro

            AST child = node.getChild(i);

            switch(child.type.getType()){ //Verifica o tipo antes de carregar na pilha
                case DOUBLE_TYPE:
                    mv.visitVarInsn(DLOAD,i);
                    break;
                case STR_TYPE:
                case LIST_TYPE: //lista e strings são objetos
                    mv.visitVarInsn(ALOAD,i);
                    break;
                default: //boolean e int são inteiros
                    mv.visitVarInsn(ILOAD,i); 
                    break;

            }
           
            //visitFieldInsn faz uma operação no membro de uma classe (atributos)
            //Nesse caso estamos "setando" um valor a um atributo
            //Onde o atributo é um parametro da função atual
            //Como definimos que todas variáveis usadas pelo programa
            //Vai ser tratada como um atributo da classe
            //Para questões de funções aninhadas e etc
            //A cada chamada de função
            //Temos que atualizar os parametros que estão como atributos da classe
            mv.visitFieldInsn(PUTSTATIC,this.name,child.key.getName().concat(Integer.toString(child.key.getId())),typeDescriptor(vt.getType(child.key)));
            
            i++;
       }
       return null;
    }

    @Override
    protected Void visitBlock(AST node){ //bloco não armazena nada apenas chama outros visitadores
        int i = 0;

        while (node.getChild(i)!=null){
            visit(node.getChild(i++));
        }
        return null;
    }


    @Override
    protected Void visitInstanceVar(AST node){
        visit(node.getChild(0)); // Visita variavel

        if (node.getChild(1)!=null) { // Variavel foi inicializada com valor
            Key k = node.getChild(0).key;
            visit(node.getChild(1));
            String descriptor = typeDescriptor(vt.getType(k));
            mv.visitFieldInsn(PUTSTATIC, this.name, k.getName().concat(Integer.toString(k.getId())),
                   descriptor);
        }
        else tabelaDeDefinicoes.replace(node.getChild(0).key,false);//se não é criado com valor, vamos trocar para falso
                                                                    //pois ao adicionar uma variavel na tabela sempre é adicionada com true
        return null;
    }
    @Override
    protected Void visitVarList(AST node){
        int i = 0;

        while(node.getChild(i) != null)
            visit(node.getChild(i++));

        return null;
    }
    @Override
    protected Void visitAssign(AST node){

        incrementalValueIsneed=true;//Numa atribuição se há incremental, o valor na pilha não faz mal.

        Key k = node.getChild(0).key;
        
        if(!tabelaDeDefinicoes.get(k)) tabelaDeDefinicoes.replace(k,true); //se houver atribuição em variável sem valor trocamos então para true

        String descriptor = typeDescriptor(vt.getType(k));

        if(vt.getType(k).getType() == LIST_TYPE && node.getChild(0).getChild(0)!=null ) {
            mv.visitFieldInsn(GETSTATIC, this.name, k.getName().concat(Integer.toString(k.getId())), descriptor); // Coloca referencia da lista na pilha
            visit(node.getChild(0).getChild(0)); // (no lista -> no indice) coloca indice na pilha
            visit(node.getChild(1)); // Visita a expressao da atribuicao (valor resultante fica na pilha)

            // PILHA:  --> ..., lista, indice, valor

            switch (node.type.getInner()) { //o tipo de ASTORE muda para o tipo da lista
                case DOUBLE_TYPE:
                    mv.visitInsn(DASTORE); //guarda o valor em lista[indice], pilha --> ...
                    break;
                case STR_TYPE:
                    mv.visitInsn(AASTORE); //guarda o valor em lista[indice], pilha --> ...
                    break;
                case BOOL_TYPE:
                    mv.visitVarInsn(ISTORE, 2); //guarda o valor primitivo
                    mv.visitTypeInsn(NEW, "java/lang/Boolean"); //cria instancia de Boolean
                    mv.visitInsn(DUP); //duplica para ter a referencia de retorno
                    mv.visitVarInsn(ILOAD, 2); //carrega o valor primitivo
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V", false); //chama o construtor
                    mv.visitInsn(AASTORE); //guarda o valor em lista[indice], pilha --> ...
                    break;
                default:
                    mv.visitInsn(IASTORE); //guarda o valor em lista[indice], pilha --> ...
                    break;
            }
        }else {
            visit(node.getChild(1)); // Visita a expressao da atribuicao (valor resultante fica na pilha)

            mv.visitFieldInsn(PUTSTATIC, this.name, k.getName().concat(Integer.toString(k.getId())),
                    descriptor);
        }

        incrementalValueIsneed=false;//Sair é equivalente a não possuir.
        return null;
    }
    //todos os nó abaixo foram reduzidos a atribuições comuns com expressões como valores, resolvido a cima.
    @Override
    protected Void visitAssignTimes(AST node){
        return null;
    }
    @Override
    protected Void visitAssignOver(AST node){
        return null;
    }
    @Override
    protected Void visitAssignPlus(AST node){
        return null;
    }
    @Override
    protected Void visitAssignMinus(AST node){
        return null;
    }
    @Override
    protected Void visitIf(AST node){
        visit(node.getChild(0)); //Visita expressao do if

        Label else_ = new Label();
        Label end = new Label();

        mv.visitJumpInsn(IFEQ, else_); // Se for falso (IF EQual 0), pula para as instruções else
        //THEN
        if(node.getChild(1) != null) // Para o caso de if(condicao); -- inutil, masss...
            visit(node.getChild(1));

        mv.visitJumpInsn(GOTO, end); // Finalizou o then, pula pro fim
        // ELSE
        mv.visitLabel(else_);
        if(node.getChild(2) != null)
            visit(node.getChild(2));

        mv.visitLabel(end);
        return null;
    }

    @Override
    protected Void visitIntVal(AST node){
        mv.visitLdcInsn(node.intData);
        return null;
    }
    @Override
    protected Void visitBoolVal(AST node){
        mv.visitLdcInsn(node.intData);
        return null;
    }
    @Override
    protected Void visitDoubleVal(AST node){
        mv.visitLdcInsn(node.doubleData);
        return null;
    }
    @Override
    protected Void visitVoidVal(AST node){ //não tem valor
        return null;
    }
    @Override
    protected Void visitStrVal(AST node){
        int key = node.intData;
        String value = st.getString(key);
        mv.visitLdcInsn(value.substring(1,value.length()-1));
        return null;
    }
    @Override
    protected Void visitFor(AST node){
        Label start = new Label();
        Label end = new Label();

        AST for_parts = node.getChild(0);
        if(for_parts.getChild(0) != null && for_parts.getChild(0).type.getType() == NO_TYPE)
            visit(for_parts.getChild(0));   // visita a primeira parte do for que é atribuição/declaração de variavel

        // Corpo do for
        mv.visitLabel(start);
        visit(for_parts); // Visita a condição de parada
        mv.visitJumpInsn(IFEQ,end); // se a condição for falsa, encera

        int i = 1;
        while(node.getChild(i) != null)
            visit(node.getChild(i++));

        mv.visitJumpInsn(GOTO,start); // pula pra um novo loop do for
        mv.visitLabel(end);
        return null;
    }
    @Override
    protected Void visitForPart(AST node){ // visita apenas a condição de parada
        int i = 0;
        boolean condition = false; // flag para indicar se o for possui condição de parada

        while(node.getChild(i) != null){
            AST child = node.getChild(i++);

            if(child.type.getType() == BOOL_TYPE){
                condition = true;
                visit(child);
            }
        }

        if(!condition) // coloca valor verdadeira na pilha, já que o loop não tem condição de parada
            mv.visitLdcInsn(1);
        return null;
    }

    @Override
    protected Void visitForExp(AST node){
        return null;
    }

    @Override
    protected Void visitPlus(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));

        //Verifica o tipo para chamar corretamente a operação
        switch(node.type.getType()){
            case INT_TYPE:
                mv.visitInsn(IADD);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DADD);
                break;
            case LIST_TYPE:
                mv.visitVarInsn(ASTORE,1); //desempilha a lista 2
                mv.visitVarInsn(ASTORE,0); //desempila a lista 1

                mv.visitVarInsn(ALOAD,0); //pilha : ..., lista_1
                mv.visitInsn(ARRAYLENGTH); //pilha: ...,len(lista_1)

                mv.visitVarInsn(ALOAD,1); //pilha: ...,len(lista_1),lista_2 
                mv.visitInsn(ARRAYLENGTH);//pilha: ...,len(lista_1),len(lista_2)
                mv.visitInsn(IADD); //pilha : ...,len(lista_1)+lista_2
                mv.visitInsn(DUP); //pilha:...,len(lista_1)+lista_2,len(lista_1)+lista_2
                mv.visitVarInsn(ISTORE,2); //guarda o tamanho na var local 2
                
                //Como bool é int, e precisamos trabalhar com concatenação de lista de booleanos
                //Ao invés do tipo primitivo nós criamos a lista como Objeto
                //Dessa forma conseguimos manipular livremente
                if(node.type.getInner()==BOOL_TYPE)  mv.visitMultiANewArrayInsn("[Ljava/lang/Boolean;",1);
                else mv.visitMultiANewArrayInsn(typeDescriptor(node.type),1);
                
                //pilha:...,lista_r

                mv.visitVarInsn(ASTORE,3);//guarda a referencia na var local 3
                mv.visitLdcInsn(0); //coloca 0 na pilha
                mv.visitVarInsn(ISTORE,4); //carrega o indice na var local 4
                
                mv.visitVarInsn(ALOAD,0);//pilha: ...,lista_1
                mv.visitVarInsn(ALOAD,1);//pilha: ...,lista_1,lista_2
                mv.visitVarInsn(ILOAD,2);//pilha: ...,lista_1,lista_2,len(lista_r)
                mv.visitVarInsn(ALOAD,3);//pilha: ...,lista_1,lista_2,len(lista_r),lista_r
                mv.visitVarInsn(ILOAD,4);//pilha: ...,lista_1,lista_2,len(lista_r),lista_r,i=0

                //chama a função de concatenar em baixo nível referente ao tipo de lista
                switch(node.type.getInner()){
                    case INT_TYPE:
                    case VOID_TYPE:
                        mv.visitMethodInsn(INVOKESTATIC,this.name,"concatIntList","([I[II[II)[I",false);
                        break;
                    case DOUBLE_TYPE:
                        mv.visitMethodInsn(INVOKESTATIC,this.name,"concatDoubleList","([D[DI[DI)[D",false);
                        break;
                    case BOOL_TYPE:
                        mv.visitMethodInsn(INVOKESTATIC,this.name,"concatBoolList","([Ljava/lang/Boolean;[Ljava/lang/Boolean;I[Ljava/lang/Boolean;I)[Ljava/lang/Boolean;",false);
                        break;
                    case STR_TYPE:
                        mv.visitMethodInsn(INVOKESTATIC,this.name,"concatStringList","([Ljava/lang/String;[Ljava/lang/String;I[Ljava/lang/String;I)[Ljava/lang/String;",false);
                        break;

                }
                break;
            //concatenar string
            case STR_TYPE:
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/String","concat","(Ljava/lang/String;)Ljava/lang/String;",false);
                break;
             
                
        }
        return null;
    }
    @Override
    protected Void visitMinus(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));
        switch(node.type.getType()){
            case INT_TYPE:
                mv.visitInsn(ISUB);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DSUB);
                break;
        }
        return null;
    }
    @Override
    protected Void visitTimes(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));
        switch(node.type.getType()){
            case INT_TYPE:
                mv.visitInsn(IMUL);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DMUL);
                break;
        }
        return null;
    }
    @Override
    protected Void visitOver(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));
        switch(node.type.getType()){
            case INT_TYPE:
                mv.visitInsn(IDIV);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DDIV);
                break;
        }
        return null;
    }
    @Override
    protected Void visitOverInt(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));
        switch(node.getChild(0).type.getType()){
            case INT_TYPE:
                mv.visitInsn(IDIV);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DDIV);
                break;
        }
        mv.visitInsn(D2I);
        return null;
    }
    @Override
    protected Void visitMod(AST node){
        visit(node.getChild(0));
        visit(node.getChild(1));
        switch(node.type.getType()){
            case INT_TYPE:
                mv.visitInsn(IREM);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DREM);
                break;
        }
        return null;
    }
    @Override
    protected Void visitLT(AST node){
        
        incrementalValueIsneed=true;//Sempre que tiver em comparação incremental expressão é salvo então

        visit(node.getChild(0));
        visit(node.getChild(1));
        Label LT = new Label();
        Label end = new Label();
        switch(node.getChild(0).type.getType()){
            case INT_TYPE:          
                mv.visitJumpInsn(IF_ICMPLT,LT);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(LT);
                mv.visitLdcInsn(true);
                mv.visitLabel(end);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DCMPL);
                mv.visitJumpInsn(IFLT,LT);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(LT);
                mv.visitLdcInsn(true);

                mv.visitLabel(end);
                
                break;

        }
        incrementalValueIsneed=false;
        return null;
    }
    @Override
    protected Void visitGT(AST node){

        incrementalValueIsneed=true;//Sempre que tiver em comparação incremental expressão é salvo então

        visit(node.getChild(0));
        visit(node.getChild(1));
        Label GT = new Label();
        Label end = new Label();

        switch(node.getChild(0).type.getType()){
            case INT_TYPE:          
                
                mv.visitJumpInsn(IF_ICMPGT,GT);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(GT);
                mv.visitLdcInsn(true);
                mv.visitLabel(end);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DCMPL);

                mv.visitJumpInsn(IFGT,GT);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(GT);
                mv.visitLdcInsn(true);

                mv.visitLabel(end);
                
                break;

        }

        incrementalValueIsneed=false;

        return null;
    }
    @Override
    protected Void visitLEQ(AST node){

        incrementalValueIsneed=true;//Sempre que tiver em comparação incremental expressão é salvo então

        visit(node.getChild(0));
        visit(node.getChild(1));
        Label LEQ = new Label();
        Label end = new Label();
        switch(node.getChild(0).type.getType()){
            case INT_TYPE:          
                mv.visitJumpInsn(IF_ICMPLE,LEQ);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(LEQ);
                mv.visitLdcInsn(true);
                mv.visitLabel(end);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DCMPL);

                mv.visitJumpInsn(IFLE,LEQ);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(LEQ);
                mv.visitLdcInsn(true);

                mv.visitLabel(end);
                
                break;

        }
        incrementalValueIsneed=false;
        return null;
    }
    @Override
    protected Void visitGEQ(AST node){

        incrementalValueIsneed=true;//Sempre que tiver em comparação incremental expressão é salvo então

        visit(node.getChild(0));
        visit(node.getChild(1));
        Label GEQ = new Label();
        Label end = new Label();
        switch(node.getChild(0).type.getType()){
            case INT_TYPE:          
            
                mv.visitJumpInsn(IF_ICMPGE,GEQ);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(GEQ);
                mv.visitLdcInsn(true);
                mv.visitLabel(end);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DCMPL);
                mv.visitJumpInsn(IFGE,GEQ);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(GEQ);
                mv.visitLdcInsn(true);

                mv.visitLabel(end);
                
                break;

        }
        incrementalValueIsneed=false;
        return null;
    }
    @Override
    protected Void visitIs(AST node){//não implementamos no checker (descartado)
        return null;
    }
    @Override
    protected Void visitIsNot(AST node){ //O mesmo que o de cima
        return null;
    }
    @Override
    protected Void visitEQ(AST node){ 

        incrementalValueIsneed=true;//Sempre que tiver em comparação incremental expressão é salvo então

        Label EQ = new Label();
        Label end = new Label();
        typing.Type tipo = node.getChild(1).type.getType();
        switch(node.getChild(0).type.getType()){
            case INT_TYPE:
            case BOOL_TYPE:
                if(tipo==STR_TYPE || tipo==LIST_TYPE){
                    mv.visitTypeInsn(NEW,"java/lang/Integer");
                    mv.visitInsn(DUP);
                    visit(node.getChild(0));
                    mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Integer","<init>","(I)V",false);
                    visit(node.getChild(1));

                    mv.visitJumpInsn(IF_ACMPEQ,EQ);
                    mv.visitLdcInsn(false);
                    mv.visitJumpInsn(GOTO,end);

                    mv.visitLabel(EQ);
                    mv.visitLdcInsn(true);
                    mv.visitLabel(end);
                    break;
                }     
                else{
                    visit(node.getChild(0));    
                    visit(node.getChild(1));            
                    mv.visitJumpInsn(IF_ICMPEQ,EQ);
                    mv.visitLdcInsn(false);
                    mv.visitJumpInsn(GOTO,end);

                    mv.visitLabel(EQ);
                    mv.visitLdcInsn(true);
                    mv.visitLabel(end);
                    break;
                }
            case DOUBLE_TYPE:
                if(tipo==STR_TYPE || tipo==LIST_TYPE){
                    mv.visitTypeInsn(NEW,"java/lang/Double");
                    mv.visitInsn(DUP);
                    visit(node.getChild(0));
                    mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Double","<init>","(D)V",false);
                    visit(node.getChild(1));

                    mv.visitJumpInsn(IF_ACMPEQ,EQ);
                    mv.visitLdcInsn(false);
                    mv.visitJumpInsn(GOTO,end);

                    mv.visitLabel(EQ);
                    mv.visitLdcInsn(true);
                    mv.visitLabel(end);
                    break;
                }
                else{   
                    visit(node.getChild(0));
                    visit(node.getChild(1));
                    mv.visitInsn(DCMPL);

                    mv.visitJumpInsn(IFEQ,EQ);
                    mv.visitLdcInsn(false);
                    mv.visitJumpInsn(GOTO,end);

                    mv.visitLabel(EQ);
                    mv.visitLdcInsn(true);

                    mv.visitLabel(end);
                    
                    break;
                }
            case STR_TYPE:
            case LIST_TYPE:
                visit(node.getChild(0));
                if(tipo==INT_TYPE || tipo==BOOL_TYPE){
                    mv.visitTypeInsn(NEW,"java/lang/Integer");
                    mv.visitInsn(DUP);
                    visit(node.getChild(1));
                    mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Integer","<init>","(I)V",false);  
                }
                else if(tipo == DOUBLE_TYPE){
                    mv.visitTypeInsn(NEW,"java/lang/Double");
                    mv.visitInsn(DUP);
                    visit(node.getChild(1));
                    mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Double","<init>","(D)V",false);  
                }
                else visit(node.getChild(1));
                mv.visitJumpInsn(IF_ACMPEQ,EQ);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(EQ);
                mv.visitLdcInsn(true);
                mv.visitLabel(end);
                break;

        }
        incrementalValueIsneed=false;
        return null;
    }
    @Override
    protected Void visitNEQ(AST node){

        incrementalValueIsneed=true;//Sempre que tiver em comparação incremental expressão é salvo então

        Label Neq = new Label();
        Label end = new Label();
        typing.Type tipo = node.getChild(1).type.getType();
        switch(node.getChild(0).type.getType()){
            case INT_TYPE:
            case BOOL_TYPE:
                if(tipo==STR_TYPE || tipo==LIST_TYPE){
                    mv.visitTypeInsn(NEW,"java/lang/Integer");
                    mv.visitInsn(DUP);
                    visit(node.getChild(0));
                    mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Integer","<init>","(I)V",false);
                    visit(node.getChild(1));

                    mv.visitJumpInsn(IF_ACMPNE,Neq);
                    mv.visitLdcInsn(false);
                    mv.visitJumpInsn(GOTO,end);

                    mv.visitLabel(Neq);
                    mv.visitLdcInsn(true);
                    mv.visitLabel(end);
                    break;
                }     
                else{
                    visit(node.getChild(0));    
                    visit(node.getChild(1));            
                    mv.visitJumpInsn(IF_ICMPNE,Neq);
                    mv.visitLdcInsn(false);
                    mv.visitJumpInsn(GOTO,end);

                    mv.visitLabel(Neq);
                    mv.visitLdcInsn(true);
                    mv.visitLabel(end);
                    break;
                }
            case DOUBLE_TYPE:
                if(tipo==STR_TYPE || tipo==LIST_TYPE){
                    mv.visitTypeInsn(NEW,"java/lang/Double");
                    mv.visitInsn(DUP);
                    visit(node.getChild(0));
                    mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Double","<init>","(D)V",false);
                    visit(node.getChild(1));

                    mv.visitJumpInsn(IF_ACMPNE,Neq);
                    mv.visitLdcInsn(false);
                    mv.visitJumpInsn(GOTO,end);

                    mv.visitLabel(Neq);
                    mv.visitLdcInsn(true);
                    mv.visitLabel(end);
                    break;
                }
                else{   
                    visit(node.getChild(0));
                    visit(node.getChild(1));
                    mv.visitInsn(DCMPL);

                    mv.visitJumpInsn(IFNE,Neq);
                    mv.visitLdcInsn(false);
                    mv.visitJumpInsn(GOTO,end);

                    mv.visitLabel(Neq);
                    mv.visitLdcInsn(true);

                    mv.visitLabel(end);
                    
                    break;
                }
            case STR_TYPE:
            case LIST_TYPE:
                visit(node.getChild(0));
                if(tipo==INT_TYPE || tipo==BOOL_TYPE){
                    mv.visitTypeInsn(NEW,"java/lang/Integer");
                    mv.visitInsn(DUP);
                    visit(node.getChild(1));
                    mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Integer","<init>","(I)V",false);  
                }
                else if(tipo == DOUBLE_TYPE){
                    mv.visitTypeInsn(NEW,"java/lang/Double");
                    mv.visitInsn(DUP);
                    visit(node.getChild(1));
                    mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Double","<init>","(D)V",false);  
                }
                else visit(node.getChild(1));
                mv.visitJumpInsn(IF_ACMPNE,Neq);
                mv.visitLdcInsn(false);
                mv.visitJumpInsn(GOTO,end);

                mv.visitLabel(Neq);
                mv.visitLdcInsn(true);
                mv.visitLabel(end);
                break;

        }
        incrementalValueIsneed=false;
        return null;
    }
    @Override
    protected Void visitOr(AST node){
        int i = 1;

        visit(node.getChild(0));
        while(node.getChild(i) != null){
            visit(node.getChild(i++));
            mv.visitInsn(IOR);
        }

        return null;
    }
    @Override
    protected Void visitAnd(AST node){
        int i = 1;

        visit(node.getChild(0));
        while(node.getChild(i) != null){
            visit(node.getChild(i++));
            mv.visitInsn(IAND);
        }
        return null;
    }
    @Override
    protected Void visitWhile(AST node){
        Label start = new Label();
        Label end = new Label();

        mv.visitLabel(start); // Começo do while
        visit(node.getChild(0)); // Visita expressão antes de executar o loop
        mv.visitJumpInsn(IFEQ, end); //Se for falso, encerra o while

        if(node.getChild(1) != null) // Para o caso de while(condicao);
            visit(node.getChild(1));  // se for verdadeiro, visita o corpo

        mv.visitJumpInsn(GOTO, start);
        mv.visitLabel(end);

        return null;
    }
    @Override
    protected Void visitDoWhile(AST node){
        Label start = new Label();
        Label end = new Label();

        mv.visitLabel(start);//do
        visit(node.getChild(0));//body
        
        visit(node.getChild(1));//expressão booleana do loop na pilha
        mv.visitJumpInsn(IFNE,start);//se for verdadeiro vai realizar novamente o loop

        mv.visitLabel(end);
        
        return null;
    }
    @Override
    protected Void visitI2D(AST node){
        visit(node.getChild(0));
        mv.visitInsn(I2D);
        return null;
    }
    @Override
    protected Void visitVarDecl(AST node){
        Key k = node.key;
        
        tabelaDeDefinicoes.put(k,true);//Inicializo primeiramente todas como se tivessem sido definidas
                                        //isso me garante que os parametros de funções sempre vão ser true
                                        //parametros sempre tem valores associados
        
        String descriptor = typeDescriptor(vt.getType(k));
        cw.visitField(ACC_PRIVATE+ACC_STATIC,vt.getName(k).concat(Integer.toString(k.getId())),descriptor,null,null).visitEnd();//Escreve o atributo (a variável)
                                                                                                            //visitEnd(); sinaliza a finalização do campo
        return null;
    }
    //Retorna a função
    @Override
    protected Void visitFuncReturn(AST node){
        if(node.getChild(0)==null || (nameFunction.equals("main") && chaveFunction.getId()==0)) return null; //caso não tenha filho é return; (não fazemos nada)
                                    //Por que o (RETURN) ta sendo feito no instanceFunction

        else{ //se tiver um filho, ele retorna uma expressão
            visit(node.getChild(0)); //resolve a expressão e coloca na pilha
            
            switch(ft.getType(chaveFunction).getType()){ //verifica o tipo de retorno
                case DOUBLE_TYPE:
                    mv.visitInsn(DRETURN);
                    break;
                case STR_TYPE:
                case LIST_TYPE:
                    mv.visitInsn(ARETURN);
                    break;
                default:
                    mv.visitInsn(IRETURN);
                    break;
            }
        }
        return null;
    }
    //coloca o valor da variável na pilha
    @Override
    protected Void visitVarUse(AST node){
        Key k = node.key; //pega a chave de vt
       
        if(!tabelaDeDefinicoes.get(k)){
            System.err.printf("Variable %s in line %d need been declared to be used\n",vt.getName(k),vt.getLine(k));
            System.exit(1);
        }
        
        String name = vt.getName(k).concat(Integer.toString(k.getId())); //pega o nome da variável
        String descriptor = typeDescriptor(vt.getType(k));

        if(node.getChild(0)!=null){          
            mv.visitFieldInsn(GETSTATIC,this.name,name,descriptor); //pega o atributo da classe e coloca o valor na pilha
            visit(node.getChild(0));
            switch(node.type.getType()){
                case INT_TYPE:    
                    mv.visitInsn(IALOAD);
                    break;
                case DOUBLE_TYPE:
                    mv.visitInsn(DALOAD);
                    break;
                case STR_TYPE:
                    mv.visitInsn(AALOAD);
                    break;
                case BOOL_TYPE:
                    mv.visitInsn(AALOAD);
                    mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/Boolean","booleanValue","()Z",false);
                    break;

            }
        }
        else  mv.visitFieldInsn(GETSTATIC,this.name,name,descriptor); //pega o atributo da classe e coloca o valor na pilha
        return null;
    }
    //realiza a chamada de metodo
    @Override
    protected Void visitFuncUse(AST node){

        incrementalValueIsneed=true; //Quando usamos ++ ou -- em um parametro de função
        Key k = node.key;
        if(ft.getName(k).equals("print")){ //função bultin
            if(node.getChild(0).type.getType()==LIST_TYPE){ //tratamento diferente pra listas (conversão pra String)
                if(node.getChild(0).type.getInner()!=STR_TYPE && node.getChild(0).type.getInner()!=BOOL_TYPE ){
                    mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");//carrega System.out
                    visit(node.getChild(0)); //Coloca a lista na pilha
                    Inner tipo = node.getChild(0).type; //pega o tipo da lista int list/double list/bool list/String list...
                    //Converte a lista para String
                    mv.visitMethodInsn(INVOKESTATIC,"java/util/Arrays","toString","(".concat(typeDescriptor(tipo)).concat(")Ljava/lang/String;"),false);
                    mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/String;)V",false);//printa a lista System.out.println(valor)
                    incrementalValueIsneed=false; //saindo da chamada de função desativar é a solução
                    return null;
                }
                else{
                    mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");//carrega System.out
                    visit(node.getChild(0)); //Coloca a lista na pilha
                    Inner tipo = node.getChild(0).type; //pega o tipo da lista int list/double list/bool list/String list...
                    //Converte a lista para String
                    //mv.visitMethodInsn(INVOKESTATIC,"java/util/Arrays","asList","(".concat(typeDescriptor(node.getChild(0).type)).concat(")Ljava/util/List;"),false);
                    //mv.visitMethodInsn(INVOKEVIRTUAL,"java/util/List","toString","()Ljava/lang/String;",false);
                    mv.visitMethodInsn(INVOKESTATIC,"java/util/Arrays","toString","([Ljava/lang/Object;)Ljava/lang/String;",false);
                    mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/String;)V",false);//printa a lista System.out.println(valor)
                    incrementalValueIsneed=false; //saindo da chamada de função desativar é a solução
                    return null;
                }
            }
            else{ //caso não seja lista, chamada direta
                mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");//carrega System.out
                visit(node.getChild(0)); //coloca o valor na pilha
                Inner tipo = node.getChild(0).type; //pega o tipo
                //printa o valor
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream","println","(".concat(typeDescriptor(tipo)).concat(")V"),false);
                incrementalValueIsneed=false; //saindo da chamada de função desativar é a solução
                return null;
            }   
        }
        //Lendo da entrada
        else if (ft.getName(k).equals("readLine")){
            mv.visitFieldInsn(GETSTATIC,this.name,"in","Ljava/util/Scanner;");
            mv.visitMethodInsn(INVOKEVIRTUAL,"java/util/Scanner","nextLine","()Ljava/lang/String;",false);
            incrementalValueIsneed=false; //saindo da chamada de função desativar é a solução
            return null;
        }
        else if (ft.getName(k).equals("readInt")){
            mv.visitFieldInsn(GETSTATIC,this.name,"in","Ljava/util/Scanner;");
            mv.visitMethodInsn(INVOKEVIRTUAL,"java/util/Scanner","nextInt","()I",false);
            incrementalValueIsneed=false; //saindo da chamada de função desativar é a solução
            return null;
        }
        else if (ft.getName(k).equals("readDouble")){
            mv.visitFieldInsn(GETSTATIC,this.name,"in","Ljava/util/Scanner;");
            mv.visitMethodInsn(INVOKEVIRTUAL,"java/util/Scanner","nextDouble","()D",false);
            incrementalValueIsneed=false; //saindo da chamada de função desativar é a solução
            return null;
        }
        else if (ft.getName(k).equals("readBool")){
            mv.visitFieldInsn(GETSTATIC,this.name,"in","Ljava/util/Scanner;");
            mv.visitMethodInsn(INVOKEVIRTUAL,"java/util/Scanner","nextBoolean","()Z",false);
            incrementalValueIsneed=false; //saindo da chamada de função desativar é a solução
            return null;
        }
        else{ //não bultin
            int i = 0;
            while(node.getChild(i)!=null){
                visit(node.getChild(i++)); //empilhando os parametros na pilha
            }
            ArrayList <Inner> parametros = ft.getParameters(k);
            Inner tipoRetorno = ft.getType(k);
            mv.visitMethodInsn(INVOKESTATIC,this.name,ft.getName(k).concat(Integer.toString(k.getId())),metodoDescriptor(parametros,tipoRetorno),false);
        }

        incrementalValueIsneed=false; //saindo da chamada de função desativar é a solução
        return null;
    }
    @Override
    protected Void visitFuncUseArg(AST node){//não sera mudado
        return null;
    }
    @Override
    protected Void visitListUse(AST node){//não sera mudado
        return null;
    }
    //empilha a lista na pilha
    @Override
    protected Void visitListVal(AST node){
        int length = node.getChildren().size(); //tamanho da lista
        mv.visitLdcInsn(length); //coloca o tamanho na pilha
        
        mv.visitMultiANewArrayInsn(typeDescriptor(node.type),1);//cria uma lista do tipo descrito com o tamanho que estava no topo da lista
                                                                //topo da pilha é agora a referência para a lista
        mv.visitVarInsn(ASTORE,1); //Guardar a referencia da lista localmente (operações de atribuição some com a referencia da pilha)
        for(int i = 0; i<length;i++){
            mv.visitVarInsn(ALOAD,1); //joga a referência da lista na pilha
            mv.visitLdcInsn(i); //pilha --> ...,lista,indice
            visit(node.getChild(i)); //adiciona o valor do filho (valor da posição i da lista do nó) na pilha
                                    //pilha --> ...,lista,indice,valor

            switch(node.type.getInner()){ //o tipo de ASTORE muda para o tipo da lista
                case DOUBLE_TYPE:
                    mv.visitInsn(DASTORE); //guarda o valor em lista[indice], pilha --> ...
                    break;
                case STR_TYPE:
                    mv.visitInsn(AASTORE); //guarda o valor em lista[indice], pilha --> ...
                    break;
                case BOOL_TYPE:
                    mv.visitVarInsn(ISTORE,2); //guarda o valor primitivo
                    mv.visitTypeInsn(NEW,"java/lang/Boolean"); //cria instancia de Boolean
                    mv.visitInsn(DUP); //duplica para ter a referencia de retorno
                    mv.visitVarInsn(ILOAD,2); //carrega o valor primitivo
                    mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Boolean","<init>","(Z)V",false); //chama o construtor
                    mv.visitInsn(AASTORE); //guarda o valor em lista[indice], pilha --> ...
                    break;   
                default:
                    mv.visitInsn(IASTORE); //guarda o valor em lista[indice], pilha --> ...
                    break;

            }
            
        }
        mv.visitVarInsn(ALOAD,1);//jogar a referência novamente ao topo da pilha 
                                //agora com todos valores atualizados
        return null;
    }
    @Override
    protected Void visitPlusPostInc(AST node){
        AST var = node.getChild(0);
        
        if(incrementalValueIsneed) visit(var);//coloca o valor na pilha pra usar na expressão

        Key k = var.key;
        String descriptor = typeDescriptor(vt.getType(k));

        //pega a referencia da variável
        mv.visitFieldInsn(GETSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
        
        switch(vt.getType(k).getType()){
            case INT_TYPE:
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IADD);
                mv.visitFieldInsn(PUTSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DCONST_1);
                mv.visitInsn(DADD);
                mv.visitFieldInsn(PUTSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
                break;
            case LIST_TYPE:
                visit(var.getChild(0));
                if(vt.getType(k).getInner()==INT_TYPE){
                    mv.visitInsn(DUP2);
                    mv.visitInsn(IALOAD);
                    mv.visitInsn(ICONST_1);
                    mv.visitInsn(IADD);
                    mv.visitInsn(IASTORE);
                }
                else if(vt.getType(k).getInner()==DOUBLE_TYPE){
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DALOAD);
                    mv.visitInsn(DCONST_1);
                    mv.visitInsn(DADD);
                    mv.visitInsn(DASTORE);
                }
                break;
        }

        return null;
    }
    @Override
    protected Void visitPlusPreInc(AST node){
        AST var = node.getChild(0);

        Key k = var.key;
        String descriptor = typeDescriptor(vt.getType(k));

        //pega a referencia da variável
        mv.visitFieldInsn(GETSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
        
        switch(vt.getType(k).getType()){
            case INT_TYPE:
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IADD);
                mv.visitFieldInsn(PUTSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DCONST_1);
                mv.visitInsn(DADD);
                mv.visitFieldInsn(PUTSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
                break;
            case LIST_TYPE:
                visit(var.getChild(0));
                if(vt.getType(k).getInner()==INT_TYPE){
                    mv.visitInsn(DUP2);
                    mv.visitInsn(IALOAD);
                    mv.visitInsn(ICONST_1);
                    mv.visitInsn(IADD);
                    mv.visitInsn(IASTORE);
                }
                else if(vt.getType(k).getInner()==DOUBLE_TYPE){
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DALOAD);
                    mv.visitInsn(DCONST_1);
                    mv.visitInsn(DADD);
                    mv.visitInsn(DASTORE);
                }
                break;
        }
        if(incrementalValueIsneed) visit(var);//coloca o valor na pilha pra usar na expressão
        return null;
    }
    @Override
    protected Void visitMinusPostInc(AST node){
        AST var = node.getChild(0);
        
        if(incrementalValueIsneed) visit(var);//coloca o valor na pilha pra usar na expressão

        Key k = var.key;
        String descriptor = typeDescriptor(vt.getType(k));

        //pega a referencia da variável
        mv.visitFieldInsn(GETSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
        
        switch(vt.getType(k).getType()){
            case INT_TYPE:
                mv.visitInsn(ICONST_1);
                mv.visitInsn(ISUB);
                mv.visitFieldInsn(PUTSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DCONST_1);
                mv.visitInsn(DSUB);
                mv.visitFieldInsn(PUTSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
                break;
            case LIST_TYPE:
                visit(var.getChild(0));
                if(vt.getType(k).getInner()==INT_TYPE){
                    mv.visitInsn(DUP2);
                    mv.visitInsn(IALOAD);
                    mv.visitInsn(ICONST_1);
                    mv.visitInsn(ISUB);
                    mv.visitInsn(IASTORE);
                }
                else if(vt.getType(k).getInner()==DOUBLE_TYPE){
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DALOAD);
                    mv.visitInsn(DCONST_1);
                    mv.visitInsn(DSUB);
                    mv.visitInsn(DASTORE);
                }
                break;
        }

        return null;
    }
    @Override
    protected Void visitMinusPreInc(AST node){
        AST var = node.getChild(0);
    
        Key k = var.key;
        String descriptor = typeDescriptor(vt.getType(k));

        //pega a referencia da variável
        mv.visitFieldInsn(GETSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
        
        switch(vt.getType(k).getType()){
            case INT_TYPE:
                mv.visitInsn(ICONST_1);
                mv.visitInsn(ISUB);
                mv.visitFieldInsn(PUTSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
                break;
            case DOUBLE_TYPE:
                mv.visitInsn(DCONST_1);
                mv.visitInsn(DSUB);
                mv.visitFieldInsn(PUTSTATIC,this.name,vt.getName(k).concat(Integer.toString(k.getId())),descriptor);
                break;
            case LIST_TYPE:
                visit(var.getChild(0));
                if(vt.getType(k).getInner()==INT_TYPE){
                    mv.visitInsn(DUP2);
                    mv.visitInsn(IALOAD);
                    mv.visitInsn(ICONST_1);
                    mv.visitInsn(ISUB);
                    mv.visitInsn(IASTORE);
                }
                else if(vt.getType(k).getInner()==DOUBLE_TYPE){
                    mv.visitInsn(DUP2);
                    mv.visitInsn(DALOAD);
                    mv.visitInsn(DCONST_1);
                    mv.visitInsn(DSUB);
                    mv.visitInsn(DASTORE);
                }
                break;
        }
        if(incrementalValueIsneed) visit(var);//coloca o valor na pilha pra usar na expressão
        return null;
    }
    @Override
    protected Void visitNot(AST node){
        visit(node.getChild(0));

        mv.visitInsn(INEG);
        return null;
    }
    //Converte o tipo para o descritor esperado pela JVM
    String typeDescriptor(Inner tipo){ 

        switch (tipo.getType()){
            case DOUBLE_TYPE : return "D";
            case INT_TYPE : return "I";
            case STR_TYPE : return "Ljava/lang/String;";
            case BOOL_TYPE : return "Z";
            case VOID_TYPE : return "V";
            case LIST_TYPE :
                switch(tipo.getInner()){
                    case DOUBLE_TYPE : return "[D";
                    case INT_TYPE : return "[I";
                    case STR_TYPE : return "[Ljava/lang/String;";
                    case BOOL_TYPE : return "[Ljava/lang/Boolean;";
                    case VOID_TYPE : return "[I";
                }
            default:
	            System.err.printf("Invalid type: %s!\n", tipo.getType().toString());
	            System.exit(1);
	            return null;
        }

    }
    //Gera o descritor de metodo
    String metodoDescriptor(ArrayList <Inner> parameters, Inner retorno){

        String metodoD = "(";

        for (Inner parametro : parameters){
           metodoD = metodoD.concat(typeDescriptor(parametro));
        }

        metodoD = metodoD.concat(")");
        metodoD = metodoD.concat(typeDescriptor(retorno));

        return metodoD;
    }
    
    
    void concatIntListCreatStruct(){
        MethodVisitor oldMV = mv;
        mv = cw.visitMethod(ACC_PUBLIC+ACC_STATIC,"concatIntList","([I[II[II)[I",null,null);
        
        Label concat = new Label();
        Label list2 = new Label();
        Label verify = new Label();
        Label end = new Label();

        mv.visitLabel(concat);
   
        mv.visitVarInsn(ILOAD,4); //carrega o indice
        mv.visitVarInsn(ALOAD,0); //carrega a referencia da lista 1
        mv.visitInsn(ARRAYLENGTH); //pega o tamanho da lista 1
        mv.visitJumpInsn(IF_ICMPGE,list2); //verifica se o indice é maior que o tamanho se sim pula pra lista 2
     
        mv.visitVarInsn(ALOAD,3); //carrega referencia da lista resultante
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual
        mv.visitVarInsn(ALOAD,0); //se não carrega novamente a referencia da lista 1
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual
        mv.visitInsn(IALOAD); //carrega o valor da lista 1 na pilha
        mv.visitInsn(IASTORE); //guarda o valor na lista resultante 
        mv.visitJumpInsn(GOTO,verify); //pula para verificação
        
        mv.visitLabel(list2); //elementos da lista 2
        mv.visitVarInsn(ALOAD,1); //carrega referencia da lista 2 pilha : ...,lista_2
        mv.visitInsn(ARRAYLENGTH); //pilha : ..., len(lista_2)
        mv.visitLdcInsn(0); //pilha : ...,len(lista_2),0 
        mv.visitJumpInsn(IF_ICMPEQ,end);//se o tamanho da lista 2 for igual a zero pula pro fim
             
        mv.visitVarInsn(ALOAD,3); //carrega referencia da lista resultante
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual
        mv.visitVarInsn(ALOAD,1); // pilha : ...,lista_r,indice,lista_2
        mv.visitVarInsn(ILOAD,4); // pilha : ...,lista_r,indice,lista_2,indice
        mv.visitVarInsn(ALOAD,0); //pilha : ...,lista_r,indice,lista_2,indice,lista_1
        mv.visitInsn(ARRAYLENGTH);//pilha : ...,lista_r,indice,lista_2,indice,len(lista_1)
        mv.visitInsn(ISUB);//pilha : ...,lista_r,indice,lista_2, indice - len(lista_1)
        mv.visitInsn(IALOAD); //pilha : ...,lista_r,indice,lista_2[indice-len(lista_1)]
        mv.visitInsn(IASTORE); // pilha : ..., 


        mv.visitLabel(verify); //pilha : ...,
        mv.visitVarInsn(ILOAD,4);//pilha : ...,indice
        mv.visitLdcInsn(1); //pilha : ...,indice, 1
        mv.visitInsn(IADD); //pilha : ...,indice+1
        mv.visitInsn(DUP); //pilha : ...,indice+1, indice+1
        mv.visitVarInsn(ISTORE,4);//pilha : ...,indice+1
        mv.visitVarInsn(ILOAD,2); //pilha : ...,indice+1,len(lista_r)
        mv.visitJumpInsn(IF_ICMPGE,end);//se for igual ou maior pula para o fim
        mv.visitJumpInsn(GOTO,concat); //se não continua
        
        mv.visitLabel(end);
        mv.visitVarInsn(ALOAD,3);
        
        mv.visitInsn(ARETURN);
        mv.visitEnd();
        mv.visitMaxs(-1,-1);

        mv = oldMV;
    }
     
    void concatDoubleListCreatStruct(){
        MethodVisitor oldMV = mv;
        mv = cw.visitMethod(ACC_PUBLIC+ACC_STATIC,"concatDoubleList","([D[DI[DI)[D",null,null);
        
        Label concat = new Label();
        Label list2 = new Label();
        Label verify = new Label();
        Label end = new Label();

        mv.visitLabel(concat);

        mv.visitVarInsn(ILOAD,4); //carrega o indice
        mv.visitVarInsn(ALOAD,0); //carrega a referencia da lista 1
        mv.visitInsn(ARRAYLENGTH); //pega o tamanho da lista 1
        mv.visitJumpInsn(IF_ICMPGE,list2); //verifica se o indice é maior que o tamanho se sim pula pra lista 2
        
        mv.visitVarInsn(ALOAD,3); //carrega referencia da lista resultante
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual

        mv.visitVarInsn(ALOAD,0); //se não carrega novamente a referencia da lista 1
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual
        mv.visitInsn(DALOAD); //carrega o valor da lista 1 na pilha
        mv.visitInsn(DASTORE); //guarda o valor na lista resultante 
        mv.visitJumpInsn(GOTO,verify); //pula para verificação
        
        mv.visitLabel(list2); //elementos da lista 2
        mv.visitVarInsn(ALOAD,1); //carrega referencia da lista 2 pilha : ...,lista_r,indice,lista_2
        mv.visitInsn(ARRAYLENGTH); //pilha : ...,lista_r,indice, len(lista_2)
        mv.visitLdcInsn(0); //pilha : ...,lista_r,indice, len(lista_2),0      
        mv.visitJumpInsn(IF_ICMPEQ,end);//se o tamanho da lista 2 for igual a zero pula pro fim
             
        mv.visitVarInsn(ALOAD,3); //carrega referencia da lista resultante
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual
        mv.visitVarInsn(ALOAD,1); // pilha : ...,lista_r,indice,lista_2
        mv.visitVarInsn(ILOAD,4); // pilha : ...,lista_r,indice,lista_2,indice
        mv.visitVarInsn(ALOAD,0); //pilha : ...,lista_r,indice,lista_2,indice,lista_1
        mv.visitInsn(ARRAYLENGTH);//pilha : ...,lista_r,indice,lista_2,indice,len(lista_1)
        mv.visitInsn(ISUB);//pilha : ...,lista_r,indice,lista_2, indice - len(lista_1)
        mv.visitInsn(DALOAD); //pilha : ...,lista_r,indice,lista_2[indice-len(lista_1)]
        mv.visitInsn(DASTORE); // pilha : ..., 


        mv.visitLabel(verify); //pilha : ...,
        mv.visitVarInsn(ILOAD,4);//pilha : ...,indice
        mv.visitLdcInsn(1); //pilha : ...,indice, 1
        mv.visitInsn(IADD); //pilha : ...,indice+1
        mv.visitInsn(DUP); //pilha : ...,indice+1, indice+1
        mv.visitVarInsn(ISTORE,4);//pilha : ...,indice+1
        mv.visitVarInsn(ILOAD,2); //pilha : ...,indice+1,len(lista_r)
        mv.visitJumpInsn(IF_ICMPGE,end);//se for igual ou maior pula para o fim
        mv.visitJumpInsn(GOTO,concat); //se não continua
        
        mv.visitLabel(end);
        mv.visitVarInsn(ALOAD,3);
        
        mv.visitInsn(ARETURN);
        mv.visitEnd();
        mv.visitMaxs(-1,-1);

        mv = oldMV;
    }
    void concatStringListCreatStruct(){
        MethodVisitor oldMV = mv;
        mv = cw.visitMethod(ACC_PUBLIC+ACC_STATIC,"concatStringList","([Ljava/lang/String;[Ljava/lang/String;I[Ljava/lang/String;I)[Ljava/lang/String;",null,null);
        
        Label concat = new Label();
        Label list2 = new Label();
        Label verify = new Label();
        Label end = new Label();

        mv.visitLabel(concat);

        mv.visitVarInsn(ILOAD,4); //carrega o indice
        mv.visitVarInsn(ALOAD,0); //carrega a referencia da lista 1
        mv.visitInsn(ARRAYLENGTH); //pega o tamanho da lista 1
        mv.visitJumpInsn(IF_ICMPGE,list2); //verifica se o indice é maior que o tamanho se sim pula pra lista 2
        
        mv.visitVarInsn(ALOAD,3); //carrega referencia da lista resultante
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual

        mv.visitVarInsn(ALOAD,0); //se não carrega novamente a referencia da lista 1
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual
        mv.visitInsn(AALOAD); //carrega o valor da lista 1 na pilha
        mv.visitInsn(AASTORE); //guarda o valor na lista resultante 
        mv.visitJumpInsn(GOTO,verify); //pula para verificação
        
        mv.visitLabel(list2); //elementos da lista 2
        mv.visitVarInsn(ALOAD,1); //carrega referencia da lista 2 pilha : ...,lista_r,indice,lista_2
        mv.visitInsn(ARRAYLENGTH); //pilha : ...,lista_r,indice, len(lista_2)
        mv.visitLdcInsn(0); //pilha : ...,lista_r,indice, len(lista_2),0      
        mv.visitJumpInsn(IF_ICMPEQ,end);//se o tamanho da lista 2 for igual a zero pula pro fim
             
        mv.visitVarInsn(ALOAD,3); //carrega referencia da lista resultante
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual
        mv.visitVarInsn(ALOAD,1); // pilha : ...,lista_r,indice,lista_2
        mv.visitVarInsn(ILOAD,4); // pilha : ...,lista_r,indice,lista_2,indice
        mv.visitVarInsn(ALOAD,0); //pilha : ...,lista_r,indice,lista_2,indice,lista_1
        mv.visitInsn(ARRAYLENGTH);//pilha : ...,lista_r,indice,lista_2,indice,len(lista_1)
        mv.visitInsn(ISUB);//pilha : ...,lista_r,indice,lista_2, indice - len(lista_1)
        mv.visitInsn(AALOAD); //pilha : ...,lista_r,indice,lista_2[indice-len(lista_1)]
        mv.visitInsn(AASTORE); // pilha : ..., 


        mv.visitLabel(verify); //pilha : ...,
        mv.visitVarInsn(ILOAD,4);//pilha : ...,indice
        mv.visitLdcInsn(1); //pilha : ...,indice, 1
        mv.visitInsn(IADD); //pilha : ...,indice+1
        mv.visitInsn(DUP); //pilha : ...,indice+1, indice+1
        mv.visitVarInsn(ISTORE,4);//pilha : ...,indice+1
        mv.visitVarInsn(ILOAD,2); //pilha : ...,indice+1,len(lista_r)
        mv.visitJumpInsn(IF_ICMPGE,end);//se for igual ou maior pula para o fim
        mv.visitJumpInsn(GOTO,concat); //se não continua
        
        mv.visitLabel(end);
        mv.visitVarInsn(ALOAD,3);
        
        mv.visitInsn(ARETURN);
        mv.visitEnd();
        mv.visitMaxs(-1,-1);

        mv = oldMV;
    }
    void concatBoolListCreatStruct(){
        MethodVisitor oldMV = mv;
        mv = cw.visitMethod(ACC_PUBLIC+ACC_STATIC,"concatBoolList","([Ljava/lang/Boolean;[Ljava/lang/Boolean;I[Ljava/lang/Boolean;I)[Ljava/lang/Boolean;",null,null);
        
        Label concat = new Label();
        Label list2 = new Label();
        Label verify = new Label();
        Label end = new Label();

        mv.visitLabel(concat);

        mv.visitVarInsn(ILOAD,4); //carrega o indice
        mv.visitVarInsn(ALOAD,0); //carrega a referencia da lista 1
        mv.visitInsn(ARRAYLENGTH); //pega o tamanho da lista 1
        mv.visitJumpInsn(IF_ICMPGE,list2); //verifica se o indice é maior que o tamanho se sim pula pra lista 2
        
        mv.visitVarInsn(ALOAD,3); //carrega referencia da lista resultante
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual

        mv.visitVarInsn(ALOAD,0); //se não carrega novamente a referencia da lista 1
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual
        mv.visitInsn(AALOAD); //carrega o valor da lista 1 na pilha
        mv.visitInsn(AASTORE); //guarda o valor na lista resultante 
        mv.visitJumpInsn(GOTO,verify); //pula para verificação
        
        mv.visitLabel(list2); //elementos da lista 2
        mv.visitVarInsn(ALOAD,1); //carrega referencia da lista 2 pilha : ...,lista_r,indice,lista_2
        mv.visitInsn(ARRAYLENGTH); //pilha : ...,lista_r,indice, len(lista_2)
        mv.visitLdcInsn(0); //pilha : ...,lista_r,indice, len(lista_2),0      
        mv.visitJumpInsn(IF_ICMPEQ,end);//se o tamanho da lista 2 for igual a zero pula pro fim
             
        mv.visitVarInsn(ALOAD,3); //carrega referencia da lista resultante
        mv.visitVarInsn(ILOAD,4); //carrega o indice atual
        mv.visitVarInsn(ALOAD,1); // pilha : ...,lista_r,indice,lista_2
        mv.visitVarInsn(ILOAD,4); // pilha : ...,lista_r,indice,lista_2,indice
        mv.visitVarInsn(ALOAD,0); //pilha : ...,lista_r,indice,lista_2,indice,lista_1
        mv.visitInsn(ARRAYLENGTH);//pilha : ...,lista_r,indice,lista_2,indice,len(lista_1)
        mv.visitInsn(ISUB);//pilha : ...,lista_r,indice,lista_2, indice - len(lista_1)
        mv.visitInsn(AALOAD); //pilha : ...,lista_r,indice,lista_2[indice-len(lista_1)]
        mv.visitInsn(AASTORE); // pilha : ..., 


        mv.visitLabel(verify); //pilha : ...,
        mv.visitVarInsn(ILOAD,4);//pilha : ...,indice
        mv.visitLdcInsn(1); //pilha : ...,indice, 1
        mv.visitInsn(IADD); //pilha : ...,indice+1
        mv.visitInsn(DUP); //pilha : ...,indice+1, indice+1
        mv.visitVarInsn(ISTORE,4);//pilha : ...,indice+1
        mv.visitVarInsn(ILOAD,2); //pilha : ...,indice+1,len(lista_r)
        mv.visitJumpInsn(IF_ICMPGE,end);//se for igual ou maior pula para o fim
        mv.visitJumpInsn(GOTO,concat); //se não continua
        
        mv.visitLabel(end);
        mv.visitVarInsn(ALOAD,3);
        
        mv.visitInsn(ARETURN);
        mv.visitEnd();
        mv.visitMaxs(-1,-1);

        mv = oldMV;
    }

}
