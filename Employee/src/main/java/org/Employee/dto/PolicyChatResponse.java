package org.Employee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolicyChatResponse {

    private String answer;

    public PolicyChatResponse() {}

    public PolicyChatResponse(String answer) {
        this.answer = answer;
    }
}
