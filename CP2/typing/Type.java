package typing;
import java.util.Formatter;
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
    VOID_TYPE {
        public String toString() {return "void";}
    },
    LIST_TYPE{
        public String toString() {
            return getInner()+" list";
        }
    },
    NO_TYPE{ // usado para indicar erros de tipos.
        public String toString() {return "no_type";}
    };        
    private Type innerType;

    public void defineInner(Type inner){
        this.innerType = inner;
    }
    public Type getInner(){
        return innerType;
    }

    // Tabela de unificação de tipos primitivos para "+"
    private static Type plus[][] = {
        {INT_TYPE,DOUBLE_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE}, //int
        {DOUBLE_TYPE,DOUBLE_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE}, //double
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE}, //bool
        {NO_TYPE,NO_TYPE,NO_TYPE,STR_TYPE,NO_TYPE,NO_TYPE}, //string
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE}, //void
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,LIST_TYPE}//list
    };

    public Type unifyPlus(Type that){return plus [this.ordinal()][that.ordinal()];}

    //Tabela de unificação dos tipos primitivos para os 
    //outros operadores aritméticos

    private static Type other[][] = {
        {INT_TYPE,DOUBLE_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {DOUBLE_TYPE,DOUBLE_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE}
    };

    public Type unifyOtherArith(Type that) {return other[this.ordinal()][that.ordinal()];};

    //Tabela de unificação para "=="

    private static Type equals[][] = {
        {BOOL_TYPE,BOOL_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {BOOL_TYPE,BOOL_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,BOOL_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,BOOL_TYPE}
    };

    public Type unifyEquals(Type that){ return equals[this.ordinal()][that.ordinal()];}

    //Tabela de unificação para outros operadores de comparação

    private static Type comp[][] = {
        {BOOL_TYPE,BOOL_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {BOOL_TYPE,BOOL_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE},
        {NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE,NO_TYPE}
    };

    public Type unifyComp(Type that){ return comp[this.ordinal()][that.ordinal()];}
}