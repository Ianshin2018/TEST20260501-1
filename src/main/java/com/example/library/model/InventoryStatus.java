package com.example.library.model;

public enum InventoryStatus {
    AVAILABLE,
    BORROWED;

    public String value() {
        return name();
    }
}
