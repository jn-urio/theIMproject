-- Drop all payroll tables (run this first to start fresh)
-- Order: drop tables that reference others first, then the rest

USE payroll;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS RegUser;
DROP TABLE IF EXISTS HRUser;
DROP TABLE IF EXISTS Ledger;
DROP TABLE IF EXISTS Payroll;
DROP TABLE IF EXISTS Deduction;
DROP TABLE IF EXISTS Compensation;
DROP TABLE IF EXISTS DTR;
DROP TABLE IF EXISTS `Leave`;
DROP TABLE IF EXISTS OffsetRequest;
DROP TABLE IF EXISTS OffsetBalance;
DROP TABLE IF EXISTS EmployeeRole;
DROP TABLE IF EXISTS Employee;
DROP TABLE IF EXISTS Department;
DROP TABLE IF EXISTS PayrollPeriod;
DROP TABLE IF EXISTS `Position`;
DROP TABLE IF EXISTS StatutorySetting;

SET FOREIGN_KEY_CHECKS = 1;
