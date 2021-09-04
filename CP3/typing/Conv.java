package typing;

import static ast.NodeKind.I2D_NODE;

import ast.AST;
import static typing.Type.DOUBLE_TYPE;
import static typing.Type.NO_TYPE;
import typing.Inner;
public enum Conv {
    I2D, //INT TO DOUBLE
    NONE; // No type conversion
    
	// Cria e retorna um novo nó de conversão da AST segundo o parâmetro 'conv' passado.
	// O parâmetro 'n' é o nó que será pendurado como filho do nó de conversão.
	// Caso a conversão indicada seja 'NONE', a função simplesmente retorna o próprio
	// nó passado como argumento.
	public static AST createConvNode(Conv conv, AST n) {
	    switch(conv) {
	        case I2D:  return AST.newSubtree(I2D_NODE,new Inner(DOUBLE_TYPE,NO_TYPE),n);
	        case NONE: return n;
	        default:
	            System.err.printf("INTERNAL ERROR: invalid conversion of types!\n");
	            // A partir de agora vou abortar no primeiro erro para facilitar.
	            System.exit(1);
	            return null; // Never reached...
	    }
	}
	
	// Classe que define as informações de unificação para os tipos em expressões.
    public static final class Unif {
    	
    	public final Type type; // Tipo unificado
		public final Conv lc; 	// Conversão do lado esquerdo
		public final Conv rc; 	// Conversão do lado direito
    	
		public Unif(Type type, Conv lc, Conv rc) {
			this.type = type;
			this.lc = lc;
			this.rc = rc;
		}
		
	}
}
