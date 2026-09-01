-- Backs Spring AI's PgVectorStore for the Day 9 RAG chatbot. Hand-written
-- rather than relying on spring.ai.vectorstore.pgvector.initialize-schema=true
-- because Flyway owns schema here (hibernate.ddl-auto=validate) - see V28/V29
-- for the same reasoning applied to the attrition-scoring tables.
--
-- Table/column shape matches what PgVectorStore expects out of the box:
-- id/content/metadata/embedding, with an HNSW cosine index on the embedding
-- column. embedding is 768-wide because Google's gemini-embedding-001 model
-- is configured (see application.properties) to truncate its natively
-- 3072-dim output down to 768 via the `dimensions` option - not the
-- pgvector-default 1536 sized for OpenAI embeddings.
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(768)
);

CREATE INDEX vector_store_embedding_idx ON vector_store
    USING hnsw (embedding vector_cosine_ops);
