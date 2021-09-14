//Caso com if

bool x = true;
void main(){

    if(x){
        print("entrei!");
    }
    else if(false){ 
        print("não vou entrar");
        if(false);
    }
    else {
        if(x);
        print("aqui tbm n entro!");
    }


}