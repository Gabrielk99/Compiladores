String b,g,s;

void main(){
  List<int> k = [1,2,3,4,5];
  print("k:");
  print(k);

  k += [6, 7];
  print("k:");
  print(k);

  int x = 10;
  print("x:");
  print(x);

  double y = 5.2, z;

  y -= x + 5;
  print("y:");
  print(y);

  z = 235.34 + x;
  print("z:");
  print(z);
  
  z /= y+x;
  print("z:");
  print(z);

  x *= 5;
  print("x:");
  print(x);

  b = "Batata quente quente quente quente";
  print("b:");
  print(b);

  b+= " quente";
  print("b:");
  print(b);
}