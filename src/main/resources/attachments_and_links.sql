-- Create attachments table
CREATE TABLE IF NOT EXISTS attachments (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    activity_id BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
);

-- Create activity_links table
CREATE TABLE IF NOT EXISTS activity_links (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(2000) NOT NULL,
    title VARCHAR(255),
    activity_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_attachments_activity_id ON attachments(activity_id);
CREATE INDEX IF NOT EXISTS idx_activity_links_activity_id ON activity_links(activity_id);
