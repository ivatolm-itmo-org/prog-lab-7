MANIFEST=manifest.mf
TARGET_DIR=bin
CLASSPATH=bin:lib/Pokemon.jar

javac -d $TARGET_DIR -cp $CLASSPATH \
    src/moves/*     \
    src/pokemons/*  \
    src/Main.java

jar cmf $MANIFEST main.jar \
    bin/src/moves/*     \
    bin/src/pokemons/*  \
    bin/src/Main.class

#java -cp $CLASSPATH src/Main

