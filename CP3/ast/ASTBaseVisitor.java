package ast;

public abstract class ASTBaseVisitor<T> {

    //Visita a raiz e desencadeia as visitações

    public void execute(AST root){
        visit(root);
    }


    protected T visit(AST node){
        switch(node.kind){
            case GLOBAL_NODE : return visitGlobal(node);
            case INSTANCE_VAR_NODE : return visitInstanceVar(node);
            case VAR_LIST_NODE : return visitVarList(node);
            case ASSIGN_NODE : return visitAssign(node);
            case ASSIGN_TIMES_NODE : return visitAssignTimes(node);
            case ASSIGN_OVER_NODE : return visitAssignOver(node);
            case ASSIGN_PLUS_NODE : return visitAssignPlus(node);
            case ASSIGN_MINUS_NODE : return visitAssignMinus(node);
            case IF_NODE : return visitIf(node);
            case INT_VAL_NODE : return visitIntVal(node);
            case BOOL_VAL_NODE : return visitBoolVal(node);
            case DOUBLE_VAL_NODE : return visitDoubleVal(node);
            case VOID_VAL_NODE : return visitVoidVal(node); //provavel que não façamos nada
            case STR_VAL_NODE : return visitStrVal(node);
            case FOR_NODE : return visitFor(node);
            case FOR_PARTS_NODE : return visitForPart(node);
            case FOR_EXP_NODE : return visitForExp(node); //provavel que não façamos nada esse é um nó auxiliar
                                                        // não aparece na AST
            case PLUS_NODE : return visitPlus(node);
            case MINUS_NODE : return visitMinus(node);
            case TIMES_NODE : return visitTimes(node);
            case OVER_NODE : return visitOver(node);
            case OVER_INT_NODE : return visitOverInt(node);
            case MOD_NODE : return visitMod(node);
            case LT_NODE : return visitLT(node);
            case GT_NODE : return visitGT(node);
            case LEQ_NODE : return visitLEQ(node);
            case GEQ_NODE : return visitGEQ(node);
            case IS_NODE : return visitIs(node); //Nao implementamos is (não vai acontecer)
            case IS_NOT_NODE : return visitIsNot(node); //Nao implementamos is not (não vai acontecer)
            case EQ_NODE : return visitEQ(node);
            case NEQ_NODE : return visitNEQ(node);
            case OR_NODE : return visitOr(node);
            case AND_NODE : return visitAnd(node);
            case BLOCK_NODE : return visitBlock(node);
            case WHILE_NODE : return visitWhile(node);
            case DO_WHILE_NODE : return visitDoWhile(node);
            case I2D_NODE : return visitI2D(node);
            case VAR_DECL_NODE : return visitVarDecl(node);
            case INSTANCE_FUNCTION_NODE : return visitInstanceFunc(node);
            case FUNC_DECL_NODE : return visitFuncDecl(node);
            case FUNCTION_PARAMETER_NODE : return visitFuncParameter(node);
            case FUNCTION_RETURN_NODE : return visitFuncReturn(node);
            case VAR_USE_NODE : return visitVarUse(node);
            case FUNC_USE_NODE : return visitFuncUse(node);
            case  FUNC_USE_ARGUMENTS_NODE : return visitFuncUseArg(node); //Não vamos precisar provavel, pois é um nó auxiliar de identificação
            case LIST_USE : return visitListUse(node); //Também é um nó auxiliar de uso
            case LIST_VAL_NODE : return visitListVal(node);
            case PLUS_POST_INCR_VAL_NODE : return visitPlusPostInc(node);
            case PLUS_PRE_INCR_VAL_NODE : return visitPlusPreInc(node);
            case MINUS_POST_INCR_VAL_NODE : return visitMinusPostInc(node);
            case MINUS_PRE_INCR_VAL_NODE : return visitMinusPreInc(node);
            case NOT_NODE : return visitNot(node);
        
        
            default:
	            System.err.printf("Invalid kind: %s!\n", node.kind.toString());
	            System.exit(1);
	            return null;


        }
    }

    protected abstract T visitGlobal(AST node);
    protected abstract T visitInstanceVar(AST node);
    protected abstract T visitVarList(AST node);
    protected abstract T visitAssign(AST node);
    protected abstract T visitAssignTimes(AST node);
    protected abstract T visitAssignOver(AST node);
    protected abstract T visitAssignPlus(AST node);
    protected abstract T visitAssignMinus(AST node);
    protected abstract T visitIf(AST node);
    protected abstract T visitIntVal(AST node);
    protected abstract T visitBoolVal(AST node);
    protected abstract T visitDoubleVal(AST node);
    protected abstract T visitVoidVal(AST node);
    protected abstract T visitStrVal(AST node);
    protected abstract T visitFor(AST node);
    protected abstract T visitForPart(AST node);
    protected abstract T visitForExp(AST node);
    protected abstract T visitPlus(AST node);
    protected abstract T visitMinus(AST node);
    protected abstract T visitTimes(AST node);
    protected abstract T visitOver(AST node);
    protected abstract T visitOverInt(AST node);
    protected abstract T visitMod(AST node);
    protected abstract T visitLT(AST node);
    protected abstract T visitGT(AST node);
    protected abstract T visitLEQ(AST node);
    protected abstract T visitGEQ(AST node);
    protected abstract T visitIs(AST node);
    protected abstract T visitIsNot(AST node);
    protected abstract T visitEQ(AST node);
    protected abstract T visitNEQ(AST node);
    protected abstract T visitOr(AST node);
    protected abstract T visitAnd(AST node);
    protected abstract T visitBlock(AST node);
    protected abstract T visitWhile(AST node);
    protected abstract T visitDoWhile(AST node);
    protected abstract T visitI2D(AST node);
    protected abstract T visitVarDecl(AST node);
    protected abstract T visitInstanceFunc(AST node);
    protected abstract T visitFuncDecl(AST node);
    protected abstract T visitFuncParameter(AST node);
    protected abstract T visitFuncReturn(AST node);
    protected abstract T visitVarUse(AST node);
    protected abstract T visitFuncUse(AST node);
    protected abstract T visitFuncUseArg(AST node);
    protected abstract T visitListUse(AST node);
    protected abstract T visitListVal(AST node);
    protected abstract T visitPlusPostInc(AST node);
    protected abstract T visitPlusPreInc(AST node);
    protected abstract T visitMinusPostInc(AST node);
    protected abstract T visitMinusPreInc(AST node);
    protected abstract T visitNot(AST node);


}