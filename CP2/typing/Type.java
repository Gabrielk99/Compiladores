package typing;

// Enumeração dos tipos primitivos que podem existe em Dart (reduzido)
// Dart pode ter qualquer tipo, ele trata tipos como objetos
// Um curiosidade é que o parser trata os tipos da mesma forma que trata os identificadores
// Para simplificações de projeto vamos assumir apenas os tipos a seguir

public enum Type {

    INT_TYPE {
        public String toString() {return "int";}
    },
    DOUBLE_TYPE{
        public String toString() {return "double";}
    },
    BOOL_TYPE{
        public String toString() {return "bool";}
    },
    STR_TYPE{
        public String toString() {return "string";}
    },
    NULL_TYPE {
        public String toString() {return "null";}
    },
    VOID_TYPE {
        public String toString() {return "void";}
    },
    NUM_TYPE{
        public String toString() {return "num";}
    },
    NO_TYPE{ // usado para indicar erros de tipos.
        public String toString() {return "no_type";}
    };



}