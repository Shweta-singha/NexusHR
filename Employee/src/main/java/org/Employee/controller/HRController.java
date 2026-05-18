package org.Employee.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr")
public class HRController {

    @GetMapping("/panel")
    @PreAuthorize("hasRole('HR_MANAGER')")
    public String hrPanel() {

        return "Welcome HR Manager!";
    }
}