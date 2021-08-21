//Operadores Logicos
bool a = true;
bool b = false;

void main(){

    bool x = a && b;
    bool y = b || a;

    if(x || b){
        x = y;
    }
    else if(y && a && true || false){
        y = b;
    }
    else {
        y = y && y;
        x = x && x;
    }




}