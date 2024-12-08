package org.taskflow;

@FunctionalInterface
public interface Inputvalidator {
    boolean isValid(String input);
}
