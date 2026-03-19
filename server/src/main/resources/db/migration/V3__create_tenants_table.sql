CREATE TABLE tenants (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_tenants_owner_id ON tenants(owner_id) WHERE deleted = FALSE;
