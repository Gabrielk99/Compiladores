//Operadores Logicos
bool a = true;
bool b = false;

void main(){

    bool x = a && b;
    bool y = b || a;

    if(x || b){
        x = y;
        print("primeiro if");
        print(x);
    }
    else if(y && a && true || false){
        y = b;
        print("segundo if");
        print(y);
    }
    else {
        print("else");
        y = y && y;
        x = x && x;
        print(x);
        print(y);
    }




}