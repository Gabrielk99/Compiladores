package ast;

import static typing.Type.NO_TYPE;

import java.util.ArrayList;
import java.util.List;

import tables.VarTable;
import typing.Type;
import typing.Inner;
import tables.Key;
// Implementação dos nós da AST.
public class AST {

	public  final NodeKind kind;
	public  final int intData;
	public  final double doubleData;
	public  final Inner type;
	public final Key key;
	private final List<AST> children; // Privado para que a manipulação da lista seja controlável.

	// Construtor completo para poder tornar todos os campos finais.
	// Privado porque não queremos os dois campos 'data' preenchidos ao mesmo tempo.
	private AST(NodeKind kind, int intData, double doubleData, Inner type, Key key) {
		this.kind = kind;
		this.intData = intData;
		this.doubleData = doubleData;
		this.type = type;
		this.children = new ArrayList<AST>();
		this.key = key;
	}

	// Cria o nó com um dado inteiro.
	public AST(NodeKind kind, int intData, Inner type) {
		this(kind, intData, 0.0f, type, null);
	}

	// Cria o nó com um dado double.
	public AST(NodeKind kind, double doubleData, Inner type) {
		this(kind, 0, doubleData, type, null);
	}
	// Cria o nó com um dado Key
	public AST(NodeKind kind, Key key, Inner type){
		this(kind, 0, 0.0f, type,key);
	}
	// Adiciona um novo filho ao nó.
	public void addChild(AST child) {
		// A lista cresce automaticamente, então nunca vai dar erro ao adicionar.
		this.children.add(child);
	}

	// Retorna o filho no índice passado.
	// Não há nenhuma verificação de erros!
	public AST getChild(int idx) {
		//Testando se o indice está no intervalo
		if(0<=idx && idx<this.children.size())
	    	return this.children.get(idx);
		else return null;
	}
	public List<AST> getChildren(){
		return this.children;
	}
	// Cria um nó e pendura todos os filhos passados como argumento.
	public static AST newSubtree(NodeKind kind, Inner type, AST... children) {
		AST node = new AST(kind, 0, type);
	    for (AST child: children) {
	    	node.addChild(child);
	    }
	    return node;
	}

	// Variáveis internas usadas para geração da saída em DOT.
	// Estáticas porque só precisamos de uma instância.
	private static int nr;
	private static VarTable vt;

	// Imprime recursivamente a codificação em DOT da subárvore começando no nó atual.
	// Usa stderr como saída para facilitar o redirecionamento, mas isso é só um hack.
	private int printNodeDot() {
		int myNr = nr++;

	    System.err.printf("node%d[label=\"", myNr);
	    if (this.type.getType() != NO_TYPE) {
	    	System.err.printf("(%s) ", this.type.toString());
	    }
	    if (this.kind == NodeKind.VAR_DECL_NODE || this.kind == NodeKind.VAR_USE_NODE ||
            this.kind == NodeKind.FUNC_DECL_NODE || this.kind == NodeKind.FUNC_USE_NODE) {
	    	System.err.printf("%s@", vt.getName(this.key));
	    } else {
	    	System.err.printf("%s", this.kind.toString());
	    }

	    if (NodeKind.hasData(this.kind)) {
	        if (this.kind == NodeKind.DOUBLE_VAL_NODE) {
	        	System.err.printf("%.2f", this.doubleData);
	        } else if (this.kind == NodeKind.STR_VAL_NODE) {
	        	System.err.printf("@%d", this.intData);
	        } else  if(this.kind == NodeKind.BOOL_VAL_NODE){
                if(this.intData == 1) System.err.printf("true");
                else System.err.printf("false");
            } else if(this.kind == NodeKind.VAR_DECL_NODE || this.kind == NodeKind.VAR_USE_NODE){
				System.err.printf("%s", this.key);
			}
			else {
	        	System.err.printf("%d", this.intData);
	        }
	    }
	    System.err.printf("\"];\n");

	    for (int i = 0; i < this.children.size(); i++) {
	        int childNr = this.children.get(i).printNodeDot();
	        System.err.printf("node%d -> node%d;\n", myNr, childNr);
	    }
	    return myNr;
	}

	// Imprime a árvore toda em stderr.
	public static void printDot(AST tree, VarTable table) {
	    nr = 0;
	    vt = table;
	    System.err.printf("digraph {\ngraph [ordering=\"out\"];\n");
	    tree.printNodeDot();
	    System.err.printf("}\n");
	}
}
