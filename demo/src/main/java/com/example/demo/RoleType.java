package com.example.demo;

public enum RoleType {
    MANAGER,
    ADMINISTRATOR;

    public static RoleType fromString(String role) {
        if (role == null) return null;
        String normalized = role.trim().toLowerCase();
        if (normalized.equals("менеджер") || normalized.equals("manager")) {
            return MANAGER;
        }
        if (normalized.equals("администратор") || normalized.equals("administrator")) {
            return ADMINISTRATOR;
        }
        throw new IllegalArgumentException("Роль должна быть Менеджер или Администратор");
    }
}
