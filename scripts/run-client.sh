./scripts/build.sh

cd client
    mvn exec:java -Dexec.args="127.0.0.1 10000"
cd ..
