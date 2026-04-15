CREATE TABLE IF NOT EXISTS oauth2_authorized_client (
  client_registration_id VARCHAR(100) NOT NULL,
  principal_name VARCHAR(200) NOT NULL,
  access_token_type VARCHAR(100) NOT NULL,
  access_token_value BYTEA NOT NULL,
  access_token_issued_at TIMESTAMP NOT NULL,
  access_token_expires_at TIMESTAMP NOT NULL,
  access_token_scopes VARCHAR(1000) DEFAULT NULL,
  refresh_token_value BYTEA DEFAULT NULL,
  refresh_token_issued_at TIMESTAMP DEFAULT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (client_registration_id, principal_name)
);

-- Webhook Fields for ProjectRepositoryMapping
-- Auto-created by Hibernate, documented here for reference
-- ALTER TABLE project_repository_mapping ADD COLUMN webhook_id BIGINT NULL;
-- ALTER TABLE project_repository_mapping ADD COLUMN webhook_enabled BOOLEAN DEFAULT FALSE;
-- ALTER TABLE project_repository_mapping ADD COLUMN webhook_created_at TIMESTAMP NULL;

-- Repository Lock Field for Projects
-- Auto-created by Hibernate but documented here for reference
-- ALTER TABLE projects ADD COLUMN github_repo_locked BOOLEAN DEFAULT FALSE;
