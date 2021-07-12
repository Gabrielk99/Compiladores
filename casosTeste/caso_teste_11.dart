//Quando precisamos produzir valores
//de forma gradual, para que não 
//explodimos a maquina, trabalhamos
//com generators

//Os generators podem ser assincronos ou sincronos

//um generator sincrono é construido como se segue abaixo
Iterable <int> naturalsTo(int n) sync* {
    int k = 0;
    while (k < n) yield k++;

}

//Um generator assincrono é demonstrado a seguir
Stream <int> asynchNaturalsTo(int n) async*{
    int k = 0;
    while (k<n) yield k++;
}


//também é possível construir generator de forma recursiva

Iterable <int> naturalsDownFrom (int n) sync* {
    if(n>0){

        yield n;
        yield* naturalsDownFrom(n-1);

    }

}

void main(){
    print(naturalsDownFrom(8));
}