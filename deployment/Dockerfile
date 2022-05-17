FROM openjdk:11

COPY certs relayloadtest/certs
COPY lib relayloadtest/lib
COPY event-sample-client-11.0.1-SNAPSHOT.jar relayloadtest/.
COPY runloadtest.sh relayloadtest/runtest.sh 

WORKDIR "relayloadtest"

RUN chmod +x runtest.sh

ENTRYPOINT ./runtest.sh
