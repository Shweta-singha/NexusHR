package org.Employee.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * RAG chatbot over the HR policy docs ingested by HrPolicyIngestionRunner.
 * QuestionAnswerAdvisor handles retrieval (similarity search against the
 * pgvector-backed VectorStore) and prompt augmentation automatically - no
 * manual retrieval loop needed.
 */
@Service
public class PolicyChatService {

    private final ChatClient chatClient;

    public PolicyChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
    }

    public String answer(String question) {
        return chatClient.prompt(question).call().content();
    }
}
