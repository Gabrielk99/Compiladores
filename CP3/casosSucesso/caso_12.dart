void falar(String a){
  print(a);
  return;
}

String falarTudo(List<String> fala,int tam){
  String falaTotal="";
  for(int i=0; i<tam; i++){
    falaTotal+=fala[i]+" ";
  }
  print(falaTotal);
  return "fim";
}

void main(){
  List<String> congrats = ["Parabens", "pra", "voce"];

  falar(congrats[0]);
  String r = falarTudo(congrats,3);
}