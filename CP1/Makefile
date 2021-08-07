#MAKEFILE
#GABRIEL SOARES XAVIER
#MIKAELLA FERREIRA DA SILVA

JAVA=java
JAVAC=javac



ANTLR_PATH=antlr-4.9.2-complete.jar


# Comandos como descritos na página do ANTLR.
ANTLR4=$(JAVA) -jar $(ANTLR_PATH)
GRUN=$(JAVA) org.antlr.v4.gui.TestRig

# Diretório para os casos de teste
DATA=casosTeste
ERR=$(DATA)/casosComErro

all: antlr javac
	@echo "Done."

antlr: Dart.g4
	$(ANTLR4) -o ./ Dart.g4

javac:
	$(JAVAC) $(CLASS_PATH_OPTION) ./*.java

# Escreva o arquivo que você quer testar
FILE=casosTeste/caso_teste_1.dart
# Ou casosTeste/casosComErro/file
run:
	$(GRUN) Dart libraryDefinition -gui $(FILE)

runallnoerro:
	-for FILE in $(DATA)/*.dart; do \
	 	echo -e "\nRunning $${FILE}" && \
	 	$(GRUN) Dart libraryDefinition $${FILE};\
	done;

runallerro:
	-for FILE in $(ERR)/*.dart; do \
	 	echo -e "\nRunning $${FILE}" && \
	 	$(GRUN) Dart libraryDefinition $${FILE};\
	done;

clean:
	@rm -rf *.class *.java *.interp *.tokens
