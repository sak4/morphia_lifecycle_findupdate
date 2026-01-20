package com.foo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.MongoDBContainer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.mongodb.MongoClientSettings.builder;

public class ReproducerTest {

    private MongoDBContainer mongoDBContainer;
    private String connectionString;

    private Datastore datastore;

    @Test
    public void reproduce() {
        final MyEntity entity = new MyEntity()
                .setName("test")
                .setNickname("nick")
                .setValue(42);

        datastore.save(entity);

        final MyEntity retrieved = datastore.find(MyEntity.class)
                .filter(dev.morphia.query.filters.Filters.eq("name", "test"))
                .first();

        assert entity.equals(retrieved);
        assert entity.getNickname().equals("nick");

        // This fails because the nickname is not encoded when querying by it
        // Same issue with update, findAndModify, findAndUpdate etc.
        final MyEntity retrievedBuNickname = datastore.find(MyEntity.class)
                .filter(dev.morphia.query.filters.Filters.eq("nickname", "nick"))
                .first();
        assert entity.equals(retrievedBuNickname);
        assert entity.getNickname().equals("nick");
    }

    @NotNull
    public String databaseName() {
        return "morphia_repro";
    }

    @NotNull
    public String dockerImageName() {
        return "mongo:7";
    }

    @BeforeClass
    private void setup() {
        mongoDBContainer = new MongoDBContainer(dockerImageName());
        mongoDBContainer.start();
        connectionString = mongoDBContainer.getReplicaSetUrl(databaseName());

        MongoClient mongoClient = MongoClients.create(builder()
                                                  .uuidRepresentation(UuidRepresentation.STANDARD)
                                                  .applyConnectionString(new ConnectionString(connectionString))
                                                  .build());

        datastore = Morphia.createDatastore(mongoClient, databaseName());
    }
}
