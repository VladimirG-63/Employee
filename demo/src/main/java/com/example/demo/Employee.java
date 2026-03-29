package com.example.demo;

import lombok.Data;

@Data
public class Employee {
    private int id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private RoleType role;
}


