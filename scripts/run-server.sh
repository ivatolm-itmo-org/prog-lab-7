./scripts/build.sh

cd server
    mvn exec:java -Dexec.args="../server/src/main/resources/database/test.csv 127.0.0.1 10000"
cd ..
