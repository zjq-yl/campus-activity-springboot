-- Apply once after 001_points.sql when upgrading an existing database.
ALTER TABLE sys_user
  ADD COLUMN gender VARCHAR(10),
  ADD COLUMN enrollment_year INT,
  ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;
