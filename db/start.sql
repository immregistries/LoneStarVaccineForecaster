CREATE DATABASE forecastValidation;

USE forecastValidation;

CREATE USER forecastUser IDENTIFIED BY 'goldenroot';

GRANT SELECT, INSERT, DELETE, UPDATE, LOCK TABLES, EXECUTE ON forecastValidation.* TO 'forecastUser'@'%';


CREATE TABLE test_group (
  group_code  VARCHAR(5) NOT NULL PRIMARY KEY,
  group_label VARCHAR(30) NOT NULL
);

REPLACE INTO test_group(group_code, group_label) 
  VALUES 
  ('HepB', 'HepB'),
  ('Var', 'Varicella'),
  ('3Hib', '3 Dose Hib'),
  ('4Hib', '4 Dose Hib'),
  ('DTaP', 'DTaP-DT-Tdap'),
  ('Ped', 'Pediarix'),
  ('Tri', 'Trihibit'),
  ('HepA', 'Hep A'),
  ('Flu', 'Influenza'),
  ('IPV', 'IPV'), 
  ('Meni', 'Meningococcal'), 
  ('MMR', 'MMR-MMRV'),
  ('Pneu', 'Pneumococcal'),
  ('Rota', 'Rotavirus'),
  ('HPV', 'HVP');

CREATE TABLE forecast_line (
  line_code   VARCHAR(5) NOT NULL PRIMARY KEY,
  line_label  VARCHAR(30) NOT NULL
);

REPLACE INTO forecast_line(line_code, line_label) 
  VALUES
  ('HepB', 'HepB'), 
  ('Var', 'Varicella'), 
  ('Hib', 'Hib'), 
  ('DTaP', 'DTaP'), 
  ('HepA', 'HepA'), 
  ('Polio', 'Polio'),
  ('Flu', 'Influenza'),
  ('Meni', 'Meningococcal'), 
  ('MMR', 'MMR'), 
  ('Pneu', 'Pneumococcal'), 
  ('Rota', 'Rotavirus'), 
  ('HPV', 'HPV');
  
CREATE TABLE test_case ( 
  case_id          INT           NOT NULL PRIMARY KEY, 
  case_label       VARCHAR(100)  NOT NULL,
  case_description VARCHAR(4000),
  case_source      VARCHAR(100),
  group_code       VARCHAR(5),
  patient_last     VARCHAR(30),
  patient_first    VARCHAR(30),
  patient_dob      DATE,
  patient_sex      VARCHAR(30),
  forecast_date    DATE DEFAULT '2011-07-12',
  status_code  VARCHAR(4) DEFAULT 'UNKN'
);

CREATE TABLE expecting_entity (
  entity_id       INT  NOT NULL PRIMARY KEY,
  entity_label    VARCHAR(30) NOT NULL
);

REPLACE INTO expecting_entity(entity_id, entity_label)
VALUES
  (1, 'CT'),
  (2, 'TCH');

CREATE TABLE test_note (
  case_id         INT NOT NULL,
  entity_id       INT NOT NULL,
  user_name       VARCHAR(40) NOT NULL DEFAULT 'Not Known',
  note_text       VARCHAR(4000) NOT NULL,
  note_date       DATETIME NOT NULL
);

--ALTER TABLE test_note ADD COLUMN( user_name VARCHAR(40) NOT NULL DEFAULT 'Not Known' );

CREATE TABLE test_vaccine (
  case_id   INT NOT NULL,
  cvx_code  VARCHAR(5) NOT NULL,
  admin_date DATE NOT NULL,
  mvx_code VARCHAR(5),
  PRIMARY KEY (case_id, cvx_code, admin_date)
);

CREATE TABLE vaccine_cvx (
  cvx_code VARCHAR(5) NOT NULL PRIMARY KEY,
  cvx_label VARCHAR(50) NOT NULL,
  vaccine_id INT
);

alter TABLE vaccine_cvx add (
  vaccine_id INT
);

CREATE TABLE vaccine_tch (
  vaccine_id INT NOT NULL PRIMARY KEY,
  vaccine_label VARCHAR(50) NOT NULL,
  cvx_code  VARCHAR(5)
);


CREATE TABLE vaccine_flags (
  flag_code VARCHAR(5) NOT NULL PRIMARY KEY,
  flag_label VARCHAR(50) NOT NULL
);

CREATE TABLE test_flag (
  case_id  INT NOT NULL,
  flag_code VARCHAR(5) NOT NULL,
  flag_date DATE NOT NULL,
  PRIMARY KEY (case_id, flag_code, flag_date)
);

CREATE TABLE expected_result (
  case_id INT NOT NULL,
  entity_id INT NOT NULL,
  line_code VARCHAR(5) NOT NULL,
  dose_number VARCHAR(5) NOT NULL,
  valid_date DATE,
  due_date DATE,
  overdue_date DATE,
  PRIMARY KEY (case_id, entity_id, line_code)
);

CREATE TABLE forecasting_software (
  software_id  INT NOT NULL PRIMARY KEY,
  software_label VARCHAR(30)
);

REPLACE INTO forecasting_software (software_id, software_label) VALUES 
('1', 'TCH'),
('2', 'MCIR'),
('3', 'STC');

CREATE TABLE actual_result (
  software_id  INT NOT NULL,
  case_id      INT NOT NULL,
  line_code VARCHAR(5) NOT NULL,
  dose_number  VARCHAR(5) NOT NULL,
  valid_date   DATE,
  due_date     DATE,
  overdue_date DATE,
  PRIMARY KEY (software_id, case_id, line_code)
);

CREATE TABLE test_status (
  status_code  VARCHAR(4) NOT NULL PRIMARY KEY,
  status_label VARCHAR(30) NOT NULL
);

REPLACE INTO test_status (status_code, status_label) 
VALUES 
('UNKN', 'Unknown'),
('PASS', 'Pass'),
('RES', 'Research'),
('FAIL', 'Fail'),
('FIX', 'Fixed'),
('ACC', 'Accept');


CREATE TABLE forecast_antigen (
  forecast_code VARCHAR(30) NOT NULL PRIMARY KEY,
  forecast_label VARCHAR(30) NOT NULL,
  sort_order INT
);

INSERT INTO forecast_antigen (forecast_code, forecast_label, sort_order) VALUES
('Influenza', 'Influenza', 16),
('HepB', 'HepB', 1),
('Diphtheria', 'DTaP/Tdap', 2),
('Pertussis', 'Pertussis', 4),
('Hib', 'Hib', 5),
('Pneumo', 'PCV7', 6),
('Polio', 'IPV', 7),
('Rotavirus', 'Rota', 8),
('Measles', 'Measles', 9),
('Mumps', 'Mumps', 10),
('Rubella', 'Rubella', 11),
('Varicella', 'Var', 12),
('Mening', 'MCV4', 13),
('HepA', 'HepA', 14),
('HPV', 'HPV', 15);


create table test_user (
user_name VARCHAR(40)
);