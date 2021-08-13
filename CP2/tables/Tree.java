package tables;

public class Tree{
    private int id_escopo;
    private Tree pai;

    public Tree(int id_escopo, Tree pai){
        this.id_escopo= id_escopo;
        this.pai = pai;
    }

    public Tree addChild(int id_escopo){
        Tree child = new Tree(id_escopo, this);
        return child;
    }

    public getIdEscopo(){
        return id_escopo;
    }

    public getPai(){
        return pai;
    }
}