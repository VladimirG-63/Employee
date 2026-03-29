package com.example.demo.service;

import com.example.demo.DTO.EmployeeRequestDTO;
import com.example.demo.DTO.EmployeeResponseDTO;
import com.example.demo.Employee;
import com.example.demo.RoleType;
import com.example.demo.exception_handling.ResourceNotFoundException;
import com.example.demo.exception_handling.ValidationException;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO dto) {
        validateRequiredFields(dto);
        validateEmail(dto.getEmail(), null);
        validatePassword(dto.getPassword());

        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setSurname(dto.getSurname());
        employee.setEmail(dto.getEmail());
        employee.setPassword(dto.getPassword());
        employee.setRole(RoleType.fromString(dto.getRole()));

        Employee saved = employeeRepository.addEmployee(employee);
        return mapToResponseDTO(saved);
    }

    public List<EmployeeResponseDTO> getAll() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public EmployeeResponseDTO getById(int id) {
        Employee employee = getEmployeeEntityById(id);
        return mapToResponseDTO(employee);
    }

    public EmployeeResponseDTO update(int id, EmployeeRequestDTO dto) {
        Employee employee = getEmployeeEntityById(id);

        if (dto.getName() != null) {
            if (dto.getName().trim().isEmpty()) throw new ValidationException("Имя не может быть пустым");
            employee.setName(dto.getName());
        }
        if (dto.getSurname() != null) {
            if (dto.getSurname().trim().isEmpty()) throw new ValidationException("Фамилия не может быть пустой");
            employee.setSurname(dto.getSurname());
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            validateEmail(dto.getEmail(), id);
            employee.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            validatePassword(dto.getPassword());
            employee.setPassword(dto.getPassword());
        }
        if (dto.getRole() != null && !dto.getRole().trim().isEmpty()) {
            employee.setRole(RoleType.fromString(dto.getRole()));
        }

        return mapToResponseDTO(employee);
    }

    public void delete(int id) {
        Employee employee = getEmployeeEntityById(id);
        employeeRepository.deleteEmployee(employee);
    }


    private Employee getEmployeeEntityById(int id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сотрудник с ID " + id + " не найден"));
    }

    private void validateRequiredFields(EmployeeRequestDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Имя не может быть пустым");
        }
        if (dto.getSurname() == null || dto.getSurname().trim().isEmpty()) {
            throw new ValidationException("Фамилия не может быть пустой");
        }
    }

    private void validateEmail(String email, Integer excludeId) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (employeeRepository.existsByEmailAndIdNot(email, excludeId)) {
            throw new ValidationException("Сотрудник с таким Email уже существует");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new ValidationException("Пароль должен содержать минимум 6 символов");
        }
    }

    private EmployeeResponseDTO mapToResponseDTO(Employee employee) {
        EmployeeResponseDTO response = new EmployeeResponseDTO();
        response.setId(employee.getId());
        response.setName(employee.getName());
        response.setSurname(employee.getSurname());
        response.setEmail(employee.getEmail());
        response.setPassword(employee.getPassword());
        if (employee.getRole() != null) {
            response.setRole(employee.getRole().name());
        } else {
            response.setRole(null);
        }
        return response;
    }
}