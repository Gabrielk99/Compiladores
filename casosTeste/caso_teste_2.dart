// Main com loop while

void main() {
	while (ehValido()) {
		fazAlgumaCoisa();
		print('Estou fazendo algo');
	}

	do{
		fazOutraCoisa();
		print('Estou fazendo outra coisa infinitamente');
	}while (true);
}