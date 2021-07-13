// typedef

typedef umTipoFunc<T> = int Function(T a, T b);

int funcao_batata(int a, int b){
    print('batata');
    return 10;
}

typedef IntList = List<int>;
typedef ListMapper<X> = Map<X, List<X>>;

void main(){
    IntList il = [1, 2, 3];

    Map<String, List<String>> m1 = {}; 
    ListMapper<String> m2 = {};

    print(funcao_batata is umTipoFunc<int>); //true
}

// Metadata

@deprecated
void telefonarFixo(){
    //Telefona para numero fixo
}

class Todo {
  final String who;
  final String what;

  const Todo(this.who, this.what);
}

@Todo('seth', 'make this do something')
void doSomething() {
  print('do something');
}