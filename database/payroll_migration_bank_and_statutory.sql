-- Run this on existing payroll DB to add bank_account and statutory settings.
USE payroll;

-- Bank account for payroll funding / direct deposit (run once; if column exists, ignore error)
ALTER TABLE Employee ADD COLUMN bank_account VARCHAR(50) NULL AFTER hourly_rate;

-- Statutory rates (Pag-IBIG fixed; SSS/PhilHealth can be manual or from tables)
CREATE TABLE IF NOT EXISTS StatutorySetting (
  name VARCHAR(100) PRIMARY KEY,
  value_text VARCHAR(500),
  value_decimal DECIMAL(18,4),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO StatutorySetting (name, value_text, value_decimal) VALUES
  ('pagibig_employee_share', NULL, 200.00),
  ('pagibig_employer_share', NULL, 200.00),
  ('philhealth_rate_pct', NULL, 5.00),
  ('sss_note', 'Use SSS bracket table or manual entry', NULL);
