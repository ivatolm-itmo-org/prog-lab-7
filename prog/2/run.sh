TARGET_DIR=bin
CLASSPATH=bin:lib/Pokemon.jar

# echo src/moves/*

javac -d $TARGET_DIR -cp $CLASSPATH \
    src/moves/*     \
    src/pokemons/*  \
    src/Main.java

java -cp $CLASSPATH src/Main
