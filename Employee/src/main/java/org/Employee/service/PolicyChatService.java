package org.Employee.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * RAG chatbot over the HR policy docs ingested by HrPolicyIngestionRunner.
 * QuestionAnswerAdvisor handles retrieval (similarity search against the
 * pgvector-backed VectorStore) and prompt augmentation automatically - no
 * manual retrieval loop needed.
 */
@Service
public class PolicyChatService {

    private static final Logger log = LoggerFactory.getLogger(PolicyChatService.class);

    // Shared with the AttritionController/etc error style, but deliberately
    // not a GlobalExceptionHandler 500 - hr-chat is additive, not core HR
    // functionality, so both "never ingested" and "Gemini call failed right
    // now" should read the same to the frontend: a plain, calm answer, not
    // an error state.
    private static final String DEGRADED_MESSAGE =
            "The HR chatbot is temporarily unavailable. Please try again later or contact HR directly.";

    private final ChatClient chatClient;
    private final JdbcTemplate jdbcTemplate;

    public PolicyChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
        this.jdbcTemplate = jdbcTemplate;
    }

    public String answer(String question) {
        Long ingested = jdbcTemplate.queryForObject("SELECT count(*) FROM vector_store", Long.class);
        if (ingested == null || ingested == 0) {
            log.warn("hr-chat request received but vector_store is empty - HrPolicyIngestionRunner "
                    + "never succeeded (or hasn't run yet)");
            return DEGRADED_MESSAGE;
        }

        try {
            return chatClient.prompt(question).call().content();
        } catch (Exception e) {
            log.error("hr-chat request failed calling Gemini", e);
            return DEGRADED_MESSAGE;
        }
    }
}
