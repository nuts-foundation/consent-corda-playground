DEPRECATED. Corda is now actively used in Nuts. 

## How to run the example

### Deploy and start the Corda nodes
```
./gradlew deployNodes
```

Each node can then be started from `build/nodes/PartyX` with:
```
java -jar corda.jar
```

And the Notary from `build/nodes/Notary` with the same command as above.

### Start the Corda Nuts bridge

The bridge will use the Corda RPC client to connect to Corda and transfer any events to ZeroMQ. 
This component will be a part of the Nuts node in the future. 

```
./gradlew :bridge:bootRun
```

### Start the example client

The client is an example of a possible integration with a production network.

```
./gradlew :client:run
```

### Add a consent record

Run the following from the PartyA console

```
flow start GiveAccess patientId: "1", professionalId: "2", organisationId: "3", purpose: "care", source: "doc", parties: ["O=PartyB, L=New York, C=US"]
```

this will create a Consent record for the given data. `PartyB` in this example would be the Nuts node for the professional and `PartyA` would be the node 
where the organisation and the data of the Patient lives. 

