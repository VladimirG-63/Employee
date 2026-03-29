package com.example.demo.DTO;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"id", "name", "surname", "email", "password", "role"})
public class EmployeeResponseDTO {
    private int id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String role;
}