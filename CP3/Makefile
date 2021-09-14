JAVA=java
JAVAC=javac


ANTLR_PATH=antlr-4.9.2-complete.jar
ASM_PTH=asm-9.2.jar
CLASS_PATH_OPTION=-cp .:$(ANTLR_PATH):$(ASM_PTH)

# Comandos como descritos na página do ANTLR.
ANTLR4=$(JAVA) -jar $(ANTLR_PATH)
GRUN=$(JAVA) $(CLASS_PATH_OPTION) org.antlr.v4.gui.TestRig

# Diretório para aonde vão os arquivos gerados pelo ANTLR.
GEN_PATH=parser

# Diretório aonde está a classe com a função main.
MAIN_PATH=checker

# Diretório para os arquivos .class
BIN_PATH=bin

# Diretório para os casos de teste

IN=casosSucesso

all: antlr javac
	@echo "Done."

# Como dito no README, o ANTLR provê duas funcionalidades para se caminhar
# na parse tree: um listener e um visitor. Por padrão, o ANTLR gera um listener
# mas não gera um visitor. Como a gente quer o contrário, temos de passar
# as opções -no-listener -visitor.
antlr: Dart.g4
	$(ANTLR4) -no-listener -visitor -o $(GEN_PATH) Dart.g4

# Compila todos os subdiretórios e joga todos os .class em BIN_PATH pra organizar.
javac:
	rm -fr $(BIN_PATH)
	mkdir $(BIN_PATH)
	$(JAVAC) $(CLASS_PATH_OPTION) -d $(BIN_PATH) */*.java

NAME=caso_12
FILE=casosSucesso/$(NAME).dart

run:
	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $(FILE)
	dot -Tpng resultados/$(NAME).dot > resultados/$(NAME).png

runall:
	-for FILE in $(IN)/*.dart; do \
	 	echo -e "\nRunning $${FILE}" && \
	 	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $${FILE} &&\
		NAME=$${FILE:13:-5} &&\
		dot -Tpng resultados/$${NAME}.dot > resultados/$${NAME}.png ;\
	done;

IN_erro=casosErro
runallerro:
	-for FILE in $(IN_erro)/*.dart; do \
	 	echo -e "\nRunning $${FILE}" && \
	 	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $${FILE} ;\
	done;
clean:
	@rm -rf $(GEN_PATH) $(BIN_PATH) .antlr resultados/*.* *.class
