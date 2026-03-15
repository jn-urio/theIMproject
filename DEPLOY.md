# Deploying the Payroll System

## Prerequisites

- **Java**: JDK 8 or higher (JRE is enough on client PCs if you distribute a JAR).
- **MySQL**: Server running (e.g. XAMPP MySQL) with the `payroll` database and schema applied.

---

## Option 1: Single PC (current setup)

1. Install XAMPP and start MySQL.
2. Create the database and run the schema: `database/payroll_schema_mysql.sql` (and any migrations).
3. Put MySQL Connector/J in `lib/` (e.g. `lib/mysql-connector-j-8.0.33.jar`).
4. Run the app:
   - **With login**: `run-login.bat` (starts LoginPage, then main app).
   - **Without login**: `run.bat` (starts main app with default admin session).

No `config.properties` is required; the app uses `localhost`, `root`, and no password by default.

---

## Option 2: LAN – one server, multiple client PCs

### On the server (PC where MySQL runs)

1. Install XAMPP (or MySQL only) and create the `payroll` database + schema.
2. Let MySQL accept remote connections:
   - In `C:\xampp\mysql\bin\my.ini`, set `bind-address = 0.0.0.0`.
   - Restart MySQL from XAMPP Control Panel.
3. Open Windows Firewall for MySQL:
   - Windows Security → Firewall → Advanced settings → Inbound Rules → New Rule.
   - Port → TCP **3306** → Allow → apply to your profile (e.g. Private).
4. Create a MySQL user for remote access (e.g. in phpMyAdmin or MySQL command line):
   ```sql
   CREATE USER 'payroll_user'@'%' IDENTIFIED BY 'your_strong_password';
   GRANT ALL ON payroll.* TO 'payroll_user'@'%';
   FLUSH PRIVILEGES;
   ```
5. Note the server’s IP (e.g. `192.168.1.10`). Clients will use this.

### On each client PC

1. Copy the **entire project folder** (or a built package – see Option 3) to the PC.
2. Ensure **Java** is installed (JRE or JDK).
3. Create `config.properties` in the same folder as the JAR or the folder from which you run the app (e.g. project root):
   - Copy `config.properties.example` to `config.properties`.
   - Edit:
     ```properties
     db.host=192.168.1.10
     db.port=3306
     db.name=payroll
     db.user=payroll_user
     db.password=your_strong_password
     ```
4. Put **MySQL Connector/J** in `lib/` (e.g. `lib/mysql-connector-j-8.0.33.jar`).
5. Run the app:
   - With login: `run-login.bat`
   - Without login: `run.bat`

The working directory when running must be the folder that contains `config.properties` (and `lib/`). The batch files already `cd` to the script directory, so run them from the project root.

---

## Option 3: Build a JAR for distribution

Building a single runnable JAR makes it easier to deploy on client PCs without the full source.

### Build the JAR

1. Compile and pack everything (including the MySQL driver) into one JAR. From the project root:

   **Windows (PowerShell):**
   ```powershell
   cd "C:\Users\Drew\OneDrive\Documents\GitHub\theIMproject"
   javac -encoding UTF-8 -cp ".;lib/*" *.java
   # Create a manifest that sets the main class (e.g. LoginPage for login-first, or page for direct app)
   jar cfm payroll-app.jar manifest.txt *.class
   # Add the MySQL driver into the JAR (if you want a single JAR)
   jar uf payroll-app.jar -C lib .
   ```
   Creating a “fat” JAR with the driver inside requires a manifest with `Main-Class: LoginPage` (or `page`) and adding the contents of the connector JAR. Alternatively use a build tool (see below).

2. **Simpler approach without a fat JAR:**  
   Keep using the batch files and classpath `.;lib/*`. Distribute:
   - The compiled `.class` files (or a JAR containing only your code),
   - The `lib/` folder with `mysql-connector-j-*.jar`,
   - `config.properties` (or `config.properties.example` for users to copy and edit),
   - `run-login.bat` and `run.bat`,
   - `elements/` (e.g. logo) if the app expects it.

   Then on each PC, set `config.properties` and run `run-login.bat` or `run.bat`.

### Using a build tool (optional)

- **Maven / Gradle**: Add the MySQL driver as a dependency and use the Maven Shade plugin or Gradle’s `jar` task to build a single runnable JAR. Then run:
  ```text
  java -jar payroll-app.jar
  ```
  Ensure `config.properties` is in the current directory when you run this (or next to the JAR).

---

## Option 4: Windows executable (.exe)

**Build a standalone .exe (no Java needed on user PCs):** Install JDK 14+, put MySQL Connector/J in `lib/`, then run **`build-exe.bat`**. The executable and bundled JRE are in `build\output\Payroll\`. Run `Payroll.exe`. Copy `config.properties.example` to `config.properties` in that folder to configure the database. To build a single installer file instead, edit the script and use `--type exe` or `--type msi`.

- **jpackage** (JDK 14+): Package the JAR into a Windows installer (.msi) or executable, so users get a normal “Install” and shortcut. The installer can bundle a JRE so users don’t need to install Java.
- **Inno Setup / NSIS**: Create an installer that copies the app folder (JAR + lib + config.example + batch files), creates shortcuts, and optionally sets the path. Users still need Java unless you bundle a JRE separately.

---

## Checklist for deployment

| Step | Single PC | LAN (server) | LAN (client) |
|------|-----------|--------------|---------------|
| MySQL installed & running | ✓ | ✓ | — |
| Database `payroll` + schema | ✓ | ✓ | — |
| bind-address = 0.0.0.0 | — | ✓ | — |
| Firewall port 3306 open | — | ✓ | — |
| Remote MySQL user created | — | ✓ | — |
| Java on machine | ✓ | ✓ | ✓ |
| Project (or JAR) + lib/ | ✓ | ✓ | ✓ |
| config.properties (db.host = server IP, etc.) | — | — | ✓ |

---

## Security notes

- Use a **strong password** for the MySQL user, especially when using `'%'` (any host).
- In production, avoid `root` with no password; create a dedicated user with only the needed privileges.
- Keep `config.properties` out of version control (add it to `.gitignore`). Ship `config.properties.example` instead and let deployers copy and edit it.
