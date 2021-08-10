// Switch case e exceptions

var humor = 'TRISTE';

void main(){
    var cadeira = new Cadeira();

    switch (command) {
    case 'FELIZ':
        dancar();
        break;
    case 'TRISTE':
        chorar();
        break;
    case 'RAIVA':
        throw cadeira; //Pode lancar qualquer objeto
        break;
    case 'FOME':
        try{
            comer(cadeira);
        }on Exception catch(e){
            print(e);
            humor = 'RAIVA';
        }finally{
            print('Comer eh sempre bom!!');
        }
        break;
    default:
        print('Tudo ok!');
    }
}

void comer(var alimento){
    if (!ehComestivel(alimento)) throw Exception('Isso nao eh comestivel');

    print('Alimento consumido!');
    humor = 'OK';
}
