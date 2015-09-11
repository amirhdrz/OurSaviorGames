package com.oursaviorgames.backend.test;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public final class KeysOnlyCursorTestRootEntity {

    @Id
    public Long id;

    public KeysOnlyCursorTestRootEntity() {

    }

}