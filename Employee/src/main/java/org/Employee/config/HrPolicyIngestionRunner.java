package org.Employee.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ingests the HR policy docs (src/main/resources/hr-policies/*.md) into the
 * pgvector-backed VectorStore on startup, for PolicyChatService's RAG
 * retrieval. Guarded by an emptiness check on the vector_store table (see
 * V30__create_hr_policy_vector_store.sql) so restarts don't re-ingest and
 * duplicate every chunk.
 */
@Component
public class HrPolicyIngestionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HrPolicyIngestionRunner.class);

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final Resource[] policyDocs;

    public HrPolicyIngestionRunner(VectorStore vectorStore, JdbcTemplate jdbcTemplate,
                                    @Value("classpath:hr-policies/*.md") Resource[] policyDocs) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.policyDocs = policyDocs;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long existing = jdbcTemplate.queryForObject("SELECT count(*) FROM vector_store", Long.class);
        if (existing != null && existing > 0) {
            log.info("HR policy vector store already populated ({} chunks) - skipping ingestion", existing);
            return;
        }

        TokenTextSplitter splitter = new TokenTextSplitter();
        for (Resource policyDoc : policyDocs) {
            List<Document> parsed = new TextReader(policyDoc).read();
            List<Document> chunks = splitter.split(parsed);
            vectorStore.add(chunks);
            log.info("Ingested {} chunks from {}", chunks.size(), policyDoc.getFilename());
        }
    }
}
