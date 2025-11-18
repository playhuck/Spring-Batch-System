CREATE TABLE victims (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255),
                         process_id VARCHAR(50),
                         terminated_at TIMESTAMP,
                         status VARCHAR(20)
);