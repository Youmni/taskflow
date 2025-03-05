package org.taskflow.enums;

public enum Permission {
    READ,
    WRITE,
    DELETE;

    public boolean includes(Permission other) {
        return this.ordinal() >= other.ordinal();
    }
}
