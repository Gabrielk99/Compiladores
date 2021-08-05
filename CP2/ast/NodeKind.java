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
Tipos valores -ok
Bloco - ok
For - ok
While - ok
Conversão de tipo - ok
*/

// ----------------------------------------------------------------------------------- \\

public enum NodeKing {

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
    FUNCTION_NODE{
        public String toString() {return "function";}
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
        public String toString() {return "=!";}
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
    I2D_NODE{
        public String toString() {return "I2D";}
    },
    VAR_DECL_NODE {
		public String toString() {
            return "var_decl";
        }
	},
    FUNC_DECL_NODE {
		public String toString() {
            return "func_decl";
        }
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
	};
}