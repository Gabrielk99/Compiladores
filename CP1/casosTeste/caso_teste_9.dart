//usar generico permite que
//possamos criar uma interface
//que sirva para diversos tipos

abstract class Cache <T> {
T getByKey(String key);
void setByKey(String key, T value);
}
//também podemos definir tipos enumerados 
enum Color {red, green, blue}

void main (){

    print("Sou um programa inutil, assim como seu programador.");

    Cache <String> stringCache; //usando String como tipo do dado
    Cache <int> intCache; //usando item como tipo do dado

    var x = Color.red; //usando o tipo enumerado

    print(x);
}