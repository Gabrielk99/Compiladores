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
Operadores Aritméticos
Operadores Logicos
Tipos valores -ok
Bloco
For - ok
While
Conversão de tipo
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
    NUM_VAL_NODE{
        public String toString() {return "";}
    },
    FOR_NODE{
        public String toString() {return "for";}
    };









}