//Diferentes formas de declarar variavel e tipos em dart

void main(){

//Uma maneira de criar uma variavel com referencia de tipo 
//sem declaração explicita
var var1 = "Scambuh";
//Se tentarmos modificar var1 para qualquer outro tipo isso lançara um erro
//pois o tipo de var1 foi inferido como String
//Para criar uma variável de tipo dinâmico podemos simplesmente fazer
dynamic var2 = true;
print(var2);
var2 = "Eita rapaz, mudei de tipo, sou não unário";
print(var2);

//Os valores default de variaveis não declaradas
//em dart são nulos devido a elas serem objetos
//para poder trabalhar com valores nulos
//definimos as variaveis com "?" depois do tipo

int? lineCount;
print(lineCount);

//Caso não use o ? você precisa inicializar a variável antes de usar

int aux = 0;

// Você não consegue printar uma variavel não inicializada sem o uso de "?"

/*
    int aux;
    print(aux);
    
    output >>> print(aux);
                    ^^^^^
*/ 

// A palavra chave late é usada para definir que uma variável
// Será usada mais adiante

late String sleep;

if (true ){
    sleep = "acordei!";
}

print(sleep);

// Uma forma mais poderosa de usar late é usar como uma inicialização preguiçosa
// podemos fazer isso da forma late int value = _readSensor();
// se value nunca for usado, então _readSensor(); nunca sera chamado.

late int value = 4;

// o compilador vai saber que value tem que ser 4, mas só vai fazer isso
// quando o conteudo de value for requisitado

var result = value *2;

print(result);

//Para o caso de usar uma variável que nunca vai mudar
//Podemos usar "final" ou "const"
//Uma variável do tipo final é inicializada no momento em que é usada
//Já uma variavel do tipo const é inicializada em tempo de compilação

const copile_time = "HEHEHE JA EXISTO!";
final used_time = "DROGA SÓ VOU SAIR DAQUI QUANDO ME CHAMAREM AFF!";

print(used_time);

//outro exemplo de uso de const é usar valores constantes
//variáveis de valores constantes podem mudar

var posso_mudar = const []; 

print(posso_mudar);

posso_mudar = [1,2];

print(posso_mudar);

//Podemos declarar numeros (int ou double)
//usando valores inteiros como 3, 3.4, ou usando valores hexadecimais
//tais como 0xDEADBEEF ou  usando expoente 8e5 

int inteiro = 4;
int hex = 0xDEADBEEF;
var expoente = 10e4;
double flutuante = 1.3;
double expoente_fl = 1.43e5;

print("$inteiro, $hex, $expoente, $flutuante, $expoente_fl");

//Se você declarar uma variavel com "num" ela vai ter tanto valores inteiros como double

num x = 4;
x+= 5.3;

print(x);

//Podemos declarar listas em dart com os simbolos [ ] 

var list = [1,2,3];

//Podemos usar o operador ... para inserir uma lista dentro de outra

var list2 = [0,...list];

//Se estiver usando uma lista que pode ser nula use ...?list

var list3;

var list4 = [0,...?list3];

print(list4);

//podemos criar também, listas a partir de collection if, onde uma lista pode ter 3 ou quatro elementos

var nav = ['home,furniture','plants',if(false) 'Outlet'];

print(nav);

//Também podemos usar um for para criar uma lista

var list5 = [1,2,3];

var list6 = [

        '#0',
        for (var i in list5) '#$i'
];

print(list6);

//Também podemos criar conjuntos 
//por inferencia é um conjunto de string

var halogens = {'fluorine', 'clhorine','bromine','iodine','astatine'};

//Usando <type> definimos um conjunto do tipo definido

var names = <String>{};

//Podemos definir maps assim como mostrado embaixo

var estados = {

        'ES':'Espirito Santo',
        'BA':'Bahia',
        'SP': 'São Paulo'
};

estados['RJ']="Rio de Janeiro";

}