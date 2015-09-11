package com.oursaviorgames.backend.test;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@Entity
public final class KeysOnlyCursorTestEntity {

    @Id
    public Long id;

    @Parent
    public Key<KeysOnlyCursorTestRootEntity> rootKey;

    @Index
    public String testString;

    public KeysOnlyCursorTestEntity() {

    }

    public KeysOnlyCursorTestEntity(final Key<KeysOnlyCursorTestRootEntity> rootKey,
	    final String testString) {
	this.rootKey = rootKey;
	this.testString = testString;
    }

}