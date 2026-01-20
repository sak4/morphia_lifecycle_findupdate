package com.foo;

import dev.morphia.annotations.PostPersist;
import dev.morphia.annotations.PreLoad;
import org.bson.Document;

import java.util.Base64;

public class MyEntityListener {

    @PostPersist
    public void postPersist(final Object entity, final Document document) {
        if (!(entity instanceof MyEntity)) {
            return;
        }
        final MyEntity myEntity = (MyEntity) entity;
        document.put("nickname", Base64.getEncoder().encodeToString(myEntity.getNickname().getBytes()));

        System.out.println("Entity persisted: " + entity);
    }


    @PreLoad
    public void preLoad(final Object entity, final Document document) {
        if (!(entity instanceof MyEntity)) {
            return;
        }
        final String decodedNickname = new String(Base64.getDecoder().decode((String) document.get("nickname")));
        document.put("nickname", decodedNickname);

        System.out.println("Entity about to be loaded: " + document.toJson());
    }
}
