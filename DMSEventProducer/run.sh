java -DnumberOfThreads=$numberOfThreads -DnumberOfMessages=$numberOfMessages -DnumberOfBatchesPerThread=$numberOfBatchesPerThread -DeventType=$eventType -DserverCertificate=/Users/maherburhan/tmp/msprelayclient/certs/develop_vault_built_with_solar.pem -Dhost=us.stg.relay.system-monitor.com -cp "/Users/maherburhan/Documents/development/java/relayloadtest/DMSEventProducer/sample/msp-relay-sample/libs/*:./sample/msp-relay-sample/target/event-sample-client-11.0.1-SNAPSHOT.jar" com.solarwinds.msp.ncentral.eventproduction.sample.MspRelaySampleExceutor
