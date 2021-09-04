void falar(String a){
  print(a);
  return;
}

String falarTudo(List<String> fala){
  print(fala[0]);
  print(fala[1]);
  print(fala[2]);

  return "fim";
}

void main(){
  List<String> congrats = ["Parabens", "pra", "voce"];

  falar(congrats[0]);
  String r = falarTudo(congrats);
}