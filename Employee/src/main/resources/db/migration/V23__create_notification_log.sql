CREATE TABLE notification_log
(
    id               BIGSERIAL     PRIMARY KEY,
    recipient_email  VARCHAR(255)  NOT NULL,
    channel          VARCHAR(20)   NOT NULL,
    subject          VARCHAR(255),
    status           VARCHAR(20)   NOT NULL,
    attempts         INT           NOT NULL DEFAULT 0,
    last_attempt_at  TIMESTAMP,
    created_at       TIMESTAMP     NOT NULL DEFAULT now()
);
