package org.taskflow.model;

public enum Permission {
    READ,
    WRITE,
    DELETE;

    public boolean includes(Permission other) {
        return this.ordinal() >= other.ordinal();
    }
}
