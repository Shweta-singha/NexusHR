package org.Employee.controller;

import jakarta.validation.Valid;
import org.Employee.dto.PolicyChatRequest;
import org.Employee.dto.PolicyChatResponse;
import org.Employee.service.PolicyChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr-chat")
public class PolicyChatController {

    private final PolicyChatService policyChatService;

    public PolicyChatController(PolicyChatService policyChatService) {
        this.policyChatService = policyChatService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PolicyChatResponse> ask(@Valid @RequestBody PolicyChatRequest request) {
        String answer = policyChatService.answer(request.getQuestion());
        return ResponseEntity.ok(new PolicyChatResponse(answer));
    }
}
