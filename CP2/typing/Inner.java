package typing;

import typing.Type.*;
import static typing.Type.LIST_TYPE;


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

    public String toString(){
        if(this.type==LIST_TYPE){
            return this.inner.toString() + this.type.toString();
        }
        else return this.type.toString();
    }

}