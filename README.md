# Philippine Payroll (theIMproject)

Desktop payroll app (Java Swing) using **MySQL** via XAMPP.  
You run the database schema and write your own queries; the app uses `Database.getConnection()` to get a JDBC connection.

---

## 1. XAMPP setup

1. **Start MySQL in XAMPP**  
   Open XAMPP Control Panel → start **MySQL**.

2. **Create the database**  
   - Open **http://localhost/phpmyadmin**  
   - Click **New** → database name: `payroll` → Create.  
   - Or in SQL: `CREATE DATABASE payroll;`

3. **Run your schema**  
   Create tables by running your own SQL (e.g. in phpMyAdmin → `payroll` → SQL).  
   The `database/` folder has reference scripts (written for H2); for MySQL you may need to adjust types (e.g. `CLOB` → `LONGTEXT`, `BOOLEAN` → `TINYINT(1)`).

4. **Seed data (optional)**  
   Run your insert statements so the app has at least one user (e.g. admin).  
   Example: insert into `HRUser` a row with username `admin`, password_hash `password123`, and the right `employee_role_id`.

---

## 2. Java project setup

1. **MySQL Connector/J**  
   - Download: https://dev.mysql.com/downloads/connector/j/ (Platform Independent, e.g. `mysql-connector-j-8.0.33.jar`).  
   - Put the JAR in the project **`lib/`** folder.

2. **Connection settings**  
   Edit `Database.java` if your MySQL user/password/port differ:
   - `JDBC_URL` – default `jdbc:mysql://localhost:3306/payroll?useSSL=false&allowPublicKeyRetrieval=true`
   - `USER` – default `root`
   - `PASS` – default `""`

3. **VS Code**  
   In `.vscode/settings.json` (create if needed):
   ```json
   {
     "java.project.referencedLibraries": ["lib/**/*.jar"]
   }
   ```
   Reload the window, then run `LoginPage.java`.

4. **Run**  
   Run `LoginPage` → log in with the user you inserted (e.g. admin / password123).

---

## 3. When the app is “deployed” (XAMPP vs production)

- **XAMPP** is for **local development**. MySQL runs on your machine; the app connects to `localhost:3306`.

- **Deployed app** (e.g. on a server):
  - You usually **do not** install XAMPP on the server.
  - You install **MySQL** (or MariaDB) as a service and create the same `payroll` database (and run your schema there).
  - You point the app to that server by changing `Database.java`: set `JDBC_URL` to the server host (e.g. `jdbc:mysql://your-server:3306/payroll`) and use the right `USER`/`PASS` for that environment.

So: **same Java code**, **different URL/user/password** for local (XAMPP) vs deployed (server MySQL).

---

## Project layout

- `Database.java` – MySQL connection only (no schema run; you write/run SQL yourself).
- `LoginPage.java` – Login; then opens main app.
- `page.java` – Main window (Employee, Deductions, Compensation, etc.).
- `*Dao.java` – Use `Database.getConnection()`; you can change the queries inside them.
- `database/` – Reference SQL (H2-style); adapt and run in MySQL as you like.

---

## Note: DTR table column name

**Label:** Schema compatibility for attendance / DTR export.

The app’s Employee attendance (Present / Late / Absent) and “Export DTR to CSV” use the **DTR** table. The code in `DTRDao` expects the date column to be named **`date_val`**. If your schema uses **`dtr_date`** instead (e.g. from an ERD that names it `dtr_date`), update the SQL in `DTRDao` for `getStatusForDate` and `setAttendanceStatus` to use `dtr_date` instead of `date_val`.
