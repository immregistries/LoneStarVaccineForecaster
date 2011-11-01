-- Original table:
-- 
-- CREATE TABLE actual_result (
--   software_id  INT NOT NULL,
--   case_id      INT NOT NULL,
--   line_code VARCHAR(5) NOT NULL,
--   dose_number  VARCHAR(5) NOT NULL,
--   valid_date   DATE,
--   due_date     DATE,
--   overdue_date DATE,
--   PRIMARY KEY (software_id, case_id, line_code)
-- );
-- 
-- Adding new key so it's easier to reference
-- 
ALTER TABLE actual_result DROP PRIMARY KEY;
ALTER TABLE actual_result ADD COLUMN actual_result_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY;
 
CREATE TABLE actual_result_status (
  actual_result_id  INT NOT NULL,
  entity_id         INT NOT NULL,
  status_code  VARCHAR(4) DEFAULT 'UNKN',
  PRIMARY KEY (actual_result_id, entity_id)
);


REPLACE INTO test_status (status_code, status_label) 
VALUES 
('UNKN', 'Unknown'),
('PASS', 'Pass'),
('RES', 'Research'),
('FAIL', 'Fail'),
('FIX', 'Fixed'),
('ACC', 'Accept'),
('A', 'Great'),
('B', 'Good'),
('C', 'Okay'),
('D', 'Poor'),
('E', 'Problem');

ALTER TABLE test_user ADD COLUMN entity_id INT DEFAULT 2;
