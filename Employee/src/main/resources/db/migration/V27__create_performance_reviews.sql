CREATE TABLE performance_reviews
(
    id            BIGSERIAL    PRIMARY KEY,
    employee_id   BIGINT       NOT NULL,
    reviewer      VARCHAR(100) NOT NULL,
    review_period VARCHAR(20)  NOT NULL,
    rating        INT          NOT NULL,
    comments      TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_review_employee
        FOREIGN KEY (employee_id) REFERENCES employees (employee_id) ON DELETE CASCADE,

    CONSTRAINT chk_review_rating
        CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_reviews_employee ON performance_reviews (employee_id);
