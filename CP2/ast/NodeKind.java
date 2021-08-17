package ast;

// ---------------------------------------------------------------------------------- \\

//Enumeração para os diferentes tipos de nós da AST
//Estamos simplificando a linguagem
//Descartamos classes, objetos, var,let,dynamic, entre outras formas complexas
//De declaração, estamos usando tipagem estatica
//Abordamos

/*
NODES:
Atribuições - ok
Função - ok
If - ok
Operadores Aritméticos - ok
Operadores Logicos - ok
Operadores comparativos - ok
Tipos valores -ok
Bloco - ok
For - ok
While - ok
Conversão de tipo - ok
*/

// ----------------------------------------------------------------------------------- \\

public enum NodeKind {
    GLOBAL_NODE{
        public String toString() {return "global";}
    },
    INSTANCE_VAR_NODE{
        public String toString() {return "instance_var";}
    },
    ASSIGN_NODE {
        public String toString() {return "=";}
    },
    ASSIGN_TIMES_NODE{
        public String toString() {return "*=";}
    },
    ASSIGN_OVER_NODE{
        public String toString() {return "/=";}
    },
    ASSIGN_PLUS_NODE{
        public String toString() {return "+=";}
    },
    ASSIGN_MINUS_NODE{
        public String toString() {return "-=";}
    },
    IF_NODE{
        public String toString() {return "if";}
    },
    INT_VAL_NODE {
        public String toString() {return "";}
    },
    BOOL_VAL_NODE{
        public String toString() {return "";}
    },
    DOUBLE_VAL_NODE{
        public String toString() {return "";}
    },
    VOID_VAL_NODE{
        public String toString() {return "";}
    },
    STR_VAL_NODE{
        public String toString() {return "";}
    },
    FOR_NODE{
        public String toString() {return "for";}
    },
    FOR_PARTS_NODE{
        public String toString() {return "for_parts";}
    },
    PLUS_NODE{
        public String toString() {return "+";}
    },
    MINUS_NODE{
        public String toString() {return "-";}
    },
    TIMES_NODE{
        public String toString() {return "*";}
    },
    OVER_NODE{
        public String toString() {return "/";}
    },
    OVER_INT_NODE{
        public String toString() {return "~/";}
    },
    MOD_NODE{
        public String toString() {return "%";}
    },
    LT_NODE{
        public String toString() {return "<";}
    },
    GT_NODE{
        public String toString() {return ">";}
    },
    LEQ_NODE{
        public String toString() {return "<=";}
    },
    GEQ_NODE{
        public String toString() {return ">=";}
    },
    IS_NODE{                                    
        public String toString() {return "is";} 
    },
    IS_NOT_NODE{
        public String toString() {return "is!";} 
    },
    EQ_NODE{
        public String toString() {return "==";}
    },
    NEQ_NODE{
        public String toString() {return "!=";}
    },
    OR_NODE{
        public String toString() {return "||";}
    },
    AND_NODE{
        public String toString() {return "&&";}
    },
    BLOCK_NODE{
        public String toString() {return "block";}
    },
    WHILE_NODE{
        public String toString() {return "while";}
    },
    DO_WHILE_NODE{
        public String toString() {return "do_while";}
    },
    I2D_NODE{
        public String toString() {return "I2D";}
    },
    VAR_DECL_NODE {
		public String toString() {
            return "var_decl";
        }
	},
    INSTANCE_FUNCTION_NODE {
		public String toString() {
            return "instance_function";
        }
	},
    FUNC_DECL_NODE{
        public String toString(){return "func_signature";}
    },
    FUNCTION_PARAMETER_NODE{
        public String toString(){return "func_parameters";}
    },
    FUNCTION_RETURN_NODE{
        public String toString(){return "return";}
    },
    VAR_USE_NODE {
		public String toString() {
            return "var_use";
        }
	},
    FUNC_USE_NODE {
		public String toString() {
            return "func_use";
        }
    },
    LIST_VAL_NODE{
        public String toString(){
            return "list val";
        }
	};

    public static boolean hasData(NodeKind kind) {
		switch(kind) {
	        case BOOL_VAL_NODE:
	        case INT_VAL_NODE:
	        case DOUBLE_VAL_NODE:
	        case STR_VAL_NODE:
	        case VAR_DECL_NODE:
	        case VAR_USE_NODE:
            case FUNC_DECL_NODE:
            case FUNC_USE_NODE:
	            return true;
	        default:
	            return false;
		}
	}
}