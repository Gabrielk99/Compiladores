/*void printaAlgo(){
    print("rapaz parece funcionar!");
    void fazAlgo(bool x,double y){
        print(x);
        print(y);
    }
    fazAlgo(true,1);
    return;
}


void main(){
    print("sera que funciona?");
    printaAlgo();
}

 */
int k = 2;

void main(){
    bool v = true || true & false;
    if(v && true && true) {
        print("to no then");
    }else {
        print("to no else");
    }

    if(!v)
        print("esse nao tem else");

    if(v && false)
        print("ih nesse then eu nao entro");
    else{
        print("opa, esse entrei no else");
    }

    print("fim");
}
