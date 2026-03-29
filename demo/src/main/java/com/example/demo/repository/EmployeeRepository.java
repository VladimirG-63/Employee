package com.example.demo.repository;

import com.example.demo.Employee;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final List<Employee> employees = new ArrayList<>();
    private int id = 1;

    public Employee addEmployee(Employee employee) {
        if (employee.getId() == 0) {
            employee.setId(id++);
            employees.add(employee);
        }
        return employee;
    }

    public List<Employee> findAll() {
        return employees;
    }

    public Optional<Employee> findById(int id) {
        return employees.stream()
                .filter(e -> e.getId() == id)
                .findFirst();
    }

    public void deleteEmployee(Employee employee) {
        employees.remove(employee);
    }

    public boolean existsByEmailAndIdNot(String email, Integer excludeId) {
        return employees.stream()
                .anyMatch(e -> e.getEmail().equalsIgnoreCase(email)
                        && (excludeId == null || e.getId() != excludeId));
    }
}