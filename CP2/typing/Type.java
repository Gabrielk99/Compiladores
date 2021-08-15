package typing;
import java.util.Formatter;
import typing.Conv.Unif;
import static typing.Conv.I2D;
import static typing.Conv.NONE;
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
    private static Unif plus[][] = {
        {new Unif(INT_TYPE,NONE,NONE),new Unif(DOUBLE_TYPE,I2D,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}, //int
        {new Unif(DOUBLE_TYPE,NONE,I2D),new Unif(DOUBLE_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}, //double
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}, //bool
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(STR_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}, //string
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}, //void
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(LIST_TYPE,NONE,NONE)}//list
    };

    public Unif unifyPlus(Type that){return plus [this.ordinal()][that.ordinal()];}

    //Tabela de unificação dos tipos primitivos para os 
    //outros operadores aritméticos

    private static Unif other[][] = {
        {new Unif(INT_TYPE,NONE,NONE),new Unif(DOUBLE_TYPE,I2D,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(DOUBLE_TYPE,NONE,I2D),new Unif(DOUBLE_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}
    };

    public Unif unifyOtherArith(Type that) {return other[this.ordinal()][that.ordinal()];};

    //Tabela de unificação para "==" e "!="

    private static Unif equals[][] = {
        {new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE)},
        {new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE)},
        {new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE)},
        {new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE)},
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}, //void
        {new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE)}
    };

    public Unif unifyEquals(Type that){ return equals[this.ordinal()][that.ordinal()];}

    //Tabela de unificação para outros operadores de comparação

    private static Unif comp[][] = {
        {new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(BOOL_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
        {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}
    };

    public Unif unifyComp(Type that){ return comp[this.ordinal()][that.ordinal()];}

    //Tabela de unificação para operadores booleanos

    private static Unif booleans[][] = {
            {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
            {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
            {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(BOOL_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}, //bool
            {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
            {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)},
            {new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE),new Unif(NO_TYPE,NONE,NONE)}
    };

    public Unif unifyBooleans(Type that){ return booleans[this.ordinal()][that.ordinal()];}
}