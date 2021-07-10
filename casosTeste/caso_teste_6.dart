// Operadores e assert

void main() {
    //        Assert com operadores aritmeticos
    assert(2 + 3 == 5);
    assert(2 - 3 == -1);
    assert(2 * 3 == 6);
    assert(5 / 2 == 2.5);
    assert(5 ~/ 2 == 2); 
    assert(5 % 2 == 1); 

    assert('5/2 = ${5 ~/ 2} r ${5 % 2}' == '5/2 = 2 r 1');


    //        Operadores de incremento e decremento
    var a, b;

    a = 0;
    b = ++a; // Incrementa antes
    assert(a == b); // 1 == 1

    a = 0;
    b = a++; // Incrementa depois
    assert(a != b); // 1 != 0

    a = 0;
    b = --a; // Decrementa antes
    assert(a == b); // -1 == -1

    a = 0;
    b = a--; // Decrementa depois
    assert(a != b); // -1 != 0

    //        Operadores relacionais e de igualdade
    assert(2 == 2);
    assert(2 != 3);
    assert(3 > 2);
    assert(2 < 3);
    assert(3 >= 3);
    assert(2 <= 3);

    //        Operadores de checagem de tipo
    
    Funcionario funcionario = new Funcionario();

    (funcionario as Pessoa).primeiroNome = 'Garibaldo';

    if (funcionario is Pessoa) {
        funcionario.primeiroNome = 'Garibaldo';
    }

    if(funcionario is! Cao){
        print('Esse nao eh o mascote da empresa!');
    }

    //       Operadores de atribuicao
     
    b ??= value; // Atribui value a b, se b nao eh null, caso contrario b se mantem
    b += 1;
    a -= 10;
    b /= 2;
    b *= 5;
    a %= 7 - b;
    b ^= 5;
    a ~/= 5;
    b >>= 2;
    a <<= 3;
    a &= 2;	
    a |= b;

    //        Operadores logicos
    final value = 0x22;
    final bitmask = 0x0f;

    assert((value & bitmask) == 0x02); // AND
    assert((value & ~bitmask) == 0x20); // AND NOT
    assert((value | bitmask) == 0x2f); // OR
    assert((value ^ bitmask) == 0x2d); // XOR
    assert((value << 4) == 0x220); // Shift left
    assert((value >> 4) == 0x02); // Shift right

    //        Expressao condicional
    var visibility = isPublic ? 'public' : 'private';
    String playerName(String? name) => name ?? 'Guest';

    
    //        Operador castata
    var paint = Paint()
    ..color = Colors.black
    ..strokeCap = StrokeCap.round
    ..strokeWidth = 5.0;

    //        Operador de acesso a lista
    var numerosDaMega = [2, 5, 13, 17, 24, 42];
    int num = numerosDaMega[3];
}