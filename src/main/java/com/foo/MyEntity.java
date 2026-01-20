package com.foo;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.EntityListeners;
import dev.morphia.annotations.Id;

import java.util.Objects;

@EntityListeners(MyEntityListener.class)
@Entity
public class MyEntity {
    @Id
    private String name;
    private int value;
    private String nickname;

    public String getName() {
        return name;
    }

    public MyEntity setName(final String name) {
        this.name = name;
        return this;
    }

    public int getValue() {
        return value;
    }

    public MyEntity setValue(final int value) {
        this.value = value;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public MyEntity setNickname(final String nickname) {
        this.nickname = nickname;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof MyEntity)) return false;
        final MyEntity myEntity = (MyEntity) o;
        return getValue() == myEntity.getValue() && Objects.equals(getName(), myEntity.getName()) && Objects.equals(getNickname(), myEntity.getNickname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue(), getNickname());
    }

    @Override
    public String toString() {
        return "MyEntity{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
