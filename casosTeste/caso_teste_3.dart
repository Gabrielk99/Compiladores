//Um progama simples que aborda for

void main(){

   const lista = ["simbora","beber","simbora","prum bar"];
  
   print("Iterando sobre uma lista com o forEach e função lambda");
   lista.forEach((item)=>
            print('${lista.indexOf(item)}: $item')
   );

   print("Iterando sobre uma lista com um for normal");
   for(int i =0; i <lista.length;i++){
       print('$i: ${lista[i]}');
   }
   //o for each pega o primeiro indice que aparece o elemento
}