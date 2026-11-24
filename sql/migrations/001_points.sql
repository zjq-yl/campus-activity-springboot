-- Apply once to databases created before the points feature.
ALTER TABLE sys_user
  ADD COLUMN student_no VARCHAR(30) UNIQUE,
  ADD COLUMN college VARCHAR(100),
  ADD COLUMN major VARCHAR(100),
  ADD COLUMN class_name VARCHAR(100);

ALTER TABLE activity
  ADD COLUMN point_category VARCHAR(30) NOT NULL DEFAULT '思想成长' AFTER category,
  ADD COLUMN points INT NOT NULL DEFAULT 1 AFTER point_category,
  ADD COLUMN checkin_required BOOLEAN NOT NULL DEFAULT TRUE AFTER points,
  ADD CONSTRAINT ck_points CHECK (points >= 0);
