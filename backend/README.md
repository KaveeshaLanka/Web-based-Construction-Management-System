# LankaBuild Construction Management System — Backend

Backend for the **Project Management** and **Task Assignment & Tracking** modules,
built for LankaBuild Construction (Pvt) Ltd.

**Stack (deliberately framework-free):**
- Plain Java 17 (no Spring, no Java EE)
- Built-in `com.sun.net.httpserver.HttpServer` for the REST API
- Plain JDBC for MySQL (no ORM)
- Maven only to manage 4 small libraries: MySQL driver, Gson (JSON), jBCrypt (password hashing), JJWT (login tokens)

---

## 1. Prerequisites

Install these once:

1. **JDK 17+** — https://adoptium.net/ (Temurin 17 is fine)
2. **Maven** — https://maven.apache.org/download.cgi
3. **MySQL Server 8.x** — https://dev.mysql.com/downloads/mysql/
4. **Visual Studio Code** with the **"Extension Pack for Java"** (Microsoft) installed from the Extensions marketplace.

Verify installs in a terminal:
```bash
java -version
mvn -version
mysql --version
```

---

## 2. Open the project in VS Code

1. Unzip the project folder.
2. In VS Code: `File > Open Folder...` → select `construction-management-backend`.
3. VS Code should auto-detect it as a Maven project (you'll see a Maven icon in the sidebar). If prompted to install Java extensions, accept.

---

## 3. Set up the database

1. Start your local MySQL server.
2. Open a terminal and run the provided schema script:
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
   This creates the `lankabuild_cms` database and the `users`, `projects`, `milestones`, and `tasks` tables.
3. Open `src/main/resources/db.properties` and update it with **your own** MySQL username/password:
   ```properties
   db.url=jdbc:mysql://localhost:3306/lankabuild_cms?useSSL=false&serverTimezone=UTC
   db.username=root
   db.password=your_mysql_password
   ```

That's the entire database setup — no ORM configuration, no migrations tool. The app talks to MySQL directly through JDBC.

---

## 4. Build and run

From the project root:
```bash
mvn clean package
java -jar target/cms-backend.jar
```

You should see:
```
=================================================
 LankaBuild Construction Management System API
 Server running at: http://localhost:8080
=================================================
```

Or, in VS Code, just open `Main.java` and click **Run** above the `main` method — Maven dependencies are resolved automatically by the Java extension.

---

## 5. Project structure

```
construction-management-backend/
├── pom.xml                     # Maven dependencies (MySQL, Gson, jBCrypt, JJWT)
├── sql/schema.sql               # Run once to create DB + tables
├── src/main/resources/
│   └── db.properties            # Your DB connection settings
└── src/main/java/com/lankabuild/cms/
    ├── Main.java                 # Starts the HTTP server, registers routes
    ├── config/DBConfig.java      # JDBC connection helper
    ├── model/                    # Plain Java objects: User, Project, Milestone, Task + enums
    ├── dao/                      # Raw JDBC CRUD per table (UserDao, ProjectDao, MilestoneDao, TaskDao)
    ├── service/                  # Business rules + role-based authorization
    ├── handler/                  # HTTP route handlers (controllers)
    ├── util/                     # JWT, password hashing, JSON, HTTP helpers
    └── exception/ApiException.java
```

---

## 6. API reference

All endpoints return JSON. Protected endpoints require header:
```
Authorization: Bearer <token>
```
(token comes from `/api/auth/login`).

### Auth
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/signup` | Public |
| POST | `/api/auth/login` | Public |

**Signup body:**
```json
{
  "fullName": "Nimal Perera",
  "email": "nimal@lankabuild.com",
  "password": "secret123",
  "role": "PROJECT_MANAGER"
}
```
Valid roles: `OPERATIONS_MANAGER`, `PROJECT_MANAGER`, `SITE_SUPERVISOR`, `FINANCE_OFFICER`, `PROCUREMENT_OFFICER`, `CONSTRUCTION_WORKER`.

**Login body:**
```json
{ "email": "nimal@lankabuild.com", "password": "secret123" }
```
Returns `{ "token": "..." }`.

### Project Management (Project Managers create/edit; Operations Managers & Site Supervisors can view)
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/projects` | Project Manager |
| GET | `/api/projects` | Project Manager, Operations Manager, Site Supervisor |
| GET | `/api/projects/{id}` | same as above |
| PUT | `/api/projects/{id}` | Project Manager |
| DELETE | `/api/projects/{id}` | Project Manager |
| POST | `/api/projects/{id}/milestones` | Project Manager |
| GET | `/api/projects/{id}/milestones` | Project Manager, Operations Manager, Site Supervisor |
| PUT | `/api/milestones/{id}` | Project Manager |
| DELETE | `/api/milestones/{id}` | Project Manager |

### Task Assignment & Tracking (Project Managers & Site Supervisors assign; Workers view/update their own)
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/tasks` | Project Manager, Site Supervisor |
| GET | `/api/tasks` | Project Manager, Site Supervisor, Operations Manager |
| GET | `/api/tasks/my-tasks` | Construction Worker (their own tasks) |
| GET | `/api/tasks/project/{projectId}` | any logged-in user |
| GET | `/api/tasks/{id}` | any logged-in user (workers only their own) |
| PUT | `/api/tasks/{id}` | Project Manager, Site Supervisor |
| PATCH | `/api/tasks/{id}/status` | Worker (own tasks), Project Manager, Site Supervisor |
| DELETE | `/api/tasks/{id}` | Project Manager, Site Supervisor |

**PATCH status body:** `{ "status": "IN_PROGRESS" }` (or `PENDING`, `COMPLETED`, `DELAYED`)

---

## 7. Quick test with curl

```bash
# Sign up a Project Manager
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Nimal Perera","email":"nimal@lankabuild.com","password":"secret123","role":"PROJECT_MANAGER"}'

# Log in
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nimal@lankabuild.com","password":"secret123"}'
# -> copy the "token" value from the response

# Create a project (replace TOKEN)
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"name":"Colombo Tower","description":"20-floor commercial tower","clientName":"ABC Holdings","location":"Colombo","startDate":"2026-01-10","endDate":"2027-06-30","budget":500000000}'
```

---

## Notes

- No cloud hosting is set up here — this runs entirely on `localhost`, as you requested.
- The JWT secret in `JwtUtil.java` is hardcoded for local development. Change it to something private if you ever deploy this beyond your machine.
- Next step (when you're ready): the frontend, which will call these same endpoints from the browser.
