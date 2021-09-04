package typing;

import typing.Type.*;
import static typing.Type.LIST_TYPE;

//Classe auxiliar para uso de List
//Caso a lista seja vazia inner = no_type
//Caso o tipo não seja lista, inner = no_type
//Se o tipo é lista e não é vazia, inner = tipo da lista
public class Inner {
    private Type type;
    private Type inner;

    public Inner(Type type, Type inner){
        this.type = type;
        this.inner = inner;
    }

    public void defType(Type type){
        this.type = type;
    }
    public void defInner(Type inner){
        this.inner = inner;
    }
    public Type getType(){
        return this.type;
    }
    public Type getInner(){
        return this.inner;
    }
    @Override
    public boolean equals (Object obj){
        Inner type = (Inner) obj;
        return (type.getType() == getType()) && (type.getInner() == getInner());
    }
    public String toString(){
        if(this.type==LIST_TYPE){
            return this.inner.toString() + this.type.toString();
        }
        else return this.type.toString();
    }

}