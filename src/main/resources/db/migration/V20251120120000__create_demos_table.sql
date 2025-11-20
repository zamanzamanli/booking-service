-- Create demos table
CREATE TABLE demos (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL
);

-- Create index on status for faster queries
CREATE INDEX idx_demos_status ON demos(status);

