double quad(double x){
  return x*x;
}

void main(){
  double x = 15;
  int y = 2, k = 13;
  
  x = 14* 5/2 - 2*10 *(x+y/4.1);
  print("x:");
  print(x);

  double dec;
  dec = x % (5 + y);
  print("dec:");
  print(dec);

  int resultado;
  resultado = (k -3.56) ~/ quad(y);
  print("resultado:");
  print(resultado);
}