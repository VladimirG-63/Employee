package com.example.demo.DTO;

import lombok.Data;

@Data
public class EmployeeRequestDTO {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String role;
}