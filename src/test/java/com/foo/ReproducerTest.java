package com.foo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.ReturnDocument;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.Morphia;
import dev.morphia.query.FindOptions;
import org.bson.Document;
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

    // findAndModify fails - it saves but does not encode nickname so it fails in PRELOAD when attempting to return the entity
    // And it does not create a discriminator
    @Test
    public void findAndModifyCreatesDiscriminator() {
        final MyEntity entity = new MyEntity()
                .setName("test2")
                .setNickname("nick2")
                .setValue(84);


        final MyEntity modified = datastore.find(MyEntity.class, new FindOptions())
                .filter(dev.morphia.query.filters.Filters.eq("name", "test2"))
                .modify(new ModifyOptions().returnDocument(ReturnDocument.AFTER).upsert(true),
                        dev.morphia.query.updates.UpdateOperators.set("name", entity.getName()),
                        dev.morphia.query.updates.UpdateOperators.set("nickname", entity.getNickname()),
                        dev.morphia.query.updates.UpdateOperators.set("value", entity.getValue()));

        assert modified.getValue() == 100;

        // never gets here because of the POSTLOAD error. For now, check database for discriminator
        final Document retrievedByNickname = datastore.getDatabase().getCollection(MyEntity.class.getSimpleName()).find(new Document(
                "name", "test2")).first();
        assert retrievedByNickname.get("_t").equals(MyEntity.class.getSimpleName());
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
        System.out.println("MongoDB Connection String: " + connectionString);

        MongoClient mongoClient = MongoClients.create(builder()
                                                  .uuidRepresentation(UuidRepresentation.STANDARD)
                                                  .applyConnectionString(new ConnectionString(connectionString))
                                                  .build());

        datastore = Morphia.createDatastore(mongoClient, databaseName());
    }
}
