package org.Employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolicyChatRequest {

    @NotBlank(message = "Question is required")
    private String question;
}
