# Portfolio Backend - Installation Guide (Live)

This guide provides instructions for setting up the portfolio backend project locally.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java JDK 21 (LTS)**: [Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21)
- **Apache Maven 3.9+**: [Maven Downloads](https://maven.apache.org/download.cgi)
- **IDE**: [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/) or any Java-compatible IDE
- **Git**: [Git for Windows](https://gitforwindows.org/)

## Environment Setup

### 1. Java Installation
- Install JDK 21.
- Set `JAVA_HOME` environment variable to your JDK installation path.
- Add `%JAVA_HOME%\bin` to your `PATH`.
- Verify: `java -version`

### 2. Maven Installation
- Extract the Maven archive to a permanent location (e.g., `C:\Program Files\Maven`).
- Set `MAVEN_HOME` environment variable.
- Add `%MAVEN_HOME%\bin` to the top of your `PATH`.
- Verify: `mvn -v`

## Project Configuration

### 1. Clone the Repository
```bash
git clone https://github.com/AgnesVelemi/portfolio-backend.git
cd portfolio-backend
```

### 2. Build the Project
Run the following command to compile and build the application:
```bash
mvn clean install
```

### 3. Run the Application
You can start the Spring Boot application using Maven:
```bash
mvn spring-boot:run
```
The backend server will start on `http://localhost:8080`.

## Portfolio Stack Integration

This backend is designed to work with the **Angular Frontend**.
- **Frontend URL**: `http://localhost:4200`
- **Frontend Repository**: [portfolio-frontend](https://github.com/AgnesVelemi/portfolio-frontend.git)

## Troubleshooting

### Lombok & JDK Compatibility
If you encounter `java.lang.ExceptionInInitializerError` related to `TypeTag.UNKNOWN`, ensure you are using **Lombok 1.18.32** or newer, as it provides stable support for Java 21+.

---
*Back to [README.md](../../README.md)*
