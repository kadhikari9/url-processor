package com.ebay.coding.assignment.dto;

/**
 * Dead letter object, this class wraps the failed url or files for retry
 */
public class DeadLetter {
    public enum Type {
        URL, FILE
    }

    private Type type;
    private String value;
    private int attempts;


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
}
