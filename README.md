
TO RUN CONTAINERs
docker run -d --name proxy-server -p 9090:9090 proxy-server:latest

docker run -d --name proxy-client -p 8080:8080 proxy-client:latest


CURL command
curl -x http://localhost:8080 http://httpforever.com/


DOCKER BUILD
docker build -t proxy-server:latest .


docker build -t proxy-client:latest .


JAR RUN COMMAND
java -jar target/proxy-server-1.0-SNAPSHOT.jar


By default, Docker containers don’t share localhost unless they are on the same network.
creating a network

-docker network create proxy-network

Run both containers in same network exposing required ports

-docker run -d --name proxy-server --network=proxy-network -p 9090:9090 proxy-server:latest
-docker run -d --name proxy-client --network=proxy-network -p 8080:8080 proxy-client:latest
