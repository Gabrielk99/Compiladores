String b;
int x = 0;
void main() {
	bool v = false && false;
	double d=1.1;
	int b = 3;
	
	b+=2;
	b-=1;
	b*=4;
	b/=1;
	
	// geram erros int = double
	//b+=1.2;
	//b-=1.1;
    //b*=0.9;
	//b/=0.5;

	d+=1.3;
	d-=0.5;
	d*=4;
	d/=2.2;

	b = 5;
	
	List <int> c = [1,2,3];
	
	c+=[4,5,6];
	c+=[];
	//c+=[1.2]; gera erro int list concatenado com double list gera erro
	

	if(true == 1){
		double x = 11.5;
	}else{
		int a = 10;
	}

}
