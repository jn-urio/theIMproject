-- Offset system: balance (earned from OT converted), requests (use offset to cover short hours), DTR.offset_hours_used
USE payroll;

-- DTR: hours covered by offset on this day (so effective regular = regular_hours + offset_hours_used; no absence)
ALTER TABLE DTR ADD COLUMN offset_hours_used DECIMAL(10,2) DEFAULT 0;

-- One row per employee: current offset balance (hours)
CREATE TABLE IF NOT EXISTS OffsetBalance (
  employee_id    INT PRIMARY KEY,
  balance_hours  DECIMAL(10,2) NOT NULL DEFAULT 0,
  updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- Request to use offset: employee requests to use X hours on date Y (e.g. leave early); when approved, balance -= X and DTR for that date gets offset_hours_used += X
CREATE TABLE IF NOT EXISTS OffsetRequest (
  request_id     INT AUTO_INCREMENT PRIMARY KEY,
  employee_id    INT NOT NULL,
  hours_to_use   DECIMAL(10,2) NOT NULL,
  date_to_apply  DATE NOT NULL,
  status         VARCHAR(20) NOT NULL DEFAULT 'pending',
  requested_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  approved_by    INT,
  approved_at    DATETIME,
  notes          VARCHAR(500),
  FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);
