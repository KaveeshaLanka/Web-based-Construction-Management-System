# Web-based-Construction-Management-System🚧
Web-Based Construction Management System is designed to provide a  centralized platform for managing the major activities involved in construction project  management.

# Project Description 📌
The system will provide different functionalities for Operations Managers, Project Managers, Site Supervisors, Finance Officers, Procurement Officers, and Construction Workers. Each user will be able to access the functions relevant to their role. 

For example, a Project Manager will be able to create and manage projects, schedules, milestones, and resources. A Site Supervisor will be able to assign tasks to workers, update construction progress, and report site issues. A Finance Officer will be able to manage project budgets, expenses, and payments, while a Procurement Officer will manage material requests, suppliers, and inventory information. Construction Workers will mainly use the system to view their assigned tasks, check deadlines, and update task statuses. Operations Managers will be able to view project information and reports to monitor overall company performance.

# Development Technologies👨🏽‍💻

### Backend
- **Java 17**: core application logic
- **JDK built-in HttpServer** (`com.sun.net.httpserver`): REST API without a framework
- **JDBC + MySQL Connector/J**: database access through DAO classes
- **Gson**: JSON serialization and parsing
- **jBCrypt**: password hashing
- **JJWT (JSON Web Tokens)**: authentication and role-based access
- **Apache Maven**: dependency management and packaging (Shade plugin)

### Database
- **MySQL 8 (Community Server)**: stores users, projects, milestones, and tasks

### Frontend
- **HTML5**: page structure
- **CSS3**: styling and responsive layout
- **JavaScript (vanilla, no framework)**: application logic
- **Fetch API**: communication with the backend REST endpoints
- **Browser localStorage**: login token and role storage

### Architecture and Design
- **RESTful API**: JSON over HTTP
- **Layered architecture**: Handler, Service, and DAO layers
- **Design patterns**: DAO, Service Layer, Front Controller, Factory Method, Exception Translation
- **Role-based access control**: six user roles enforced on the server

### Tools
- **Visual Studio Code** with the Extension Pack for Java
- **VS Code Live Server** or **Python http.server**: local frontend serving
- **Graphviz**: UML class diagram generation
- **curl and web browser**: manual API and UI testing
- **Git and GitHub**: version control

# Backend Run
java -cp "target\classes;target\dependency\*" com.lankabuild.cms.Main

# Stop the currently running server first with Ctrl+C, then:

cd backend
mvn clean package
java -jar target/cms-backend.jar

# Database check
SHOW DATABASES;

USE (database_name);

SHOW TABLES;

DESCRIBE projects;
SELECT * FROM projects;

DESCRIBE milestones;
SELECT * FROM milestones;

DESCRIBE tasks;
SELECT * FROM tasks;

DESCRIBE users;
SELECT * FROM users;
