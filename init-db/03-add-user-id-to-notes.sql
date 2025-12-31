-- Add user_id column to notes table
ALTER TABLE notes ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Create foreign key constraint
ALTER TABLE notes 
ADD CONSTRAINT fk_notes_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Create index on user_id for faster queries
CREATE INDEX IF NOT EXISTS idx_notes_user_id ON notes(user_id);

-- Update semantic search query to include user_id filter
-- Note: This will be handled in the application code, but we ensure the column exists

