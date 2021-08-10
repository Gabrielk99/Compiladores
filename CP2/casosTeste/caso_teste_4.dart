// Classe, atributos e metodos

class Carro{
  String _modelo;
  String _fabricante;
  double _valor;
  static int qtdCarros = 0;
  
  Carro(this._modelo, this._fabricante, this._valor){
    qtdCarros++;
  }
  
  andar(double x){
    print('vrum vrum');
  }
  
}

void main() {
  var carro = new Carro('Gol', 'Volkswagen', 20000.0);
  carro.andar(10);
}