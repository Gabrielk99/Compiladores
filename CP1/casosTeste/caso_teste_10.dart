// Asynchronous

void main(){
    lerMensagem();
}

Future<void> lerMensagem() async {
    var msg = await receberMensagem();
    print(msg);
}