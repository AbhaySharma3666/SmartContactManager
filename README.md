# Smart Contact Manager

A cloud-based contact management system built with Spring Boot for storing and managing contacts efficiently.

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Data JPA**
- **Thymeleaf**
- **MySQL**
- **Maven**
- **Lombok**

## Prerequisites

- Java 21 or higher
- MySQL 5.x or higher
- Maven 3.x

## Configuration

Set the following environment variables or update `application.properties`:

### Database
- `MYSQL_HOST` (default: localhost)
- `MYSQL_PORT` (default: 3306)
- `MYSQL_DB` (default: scm)
- `MYSQL_USER` (default: root)
- `MYSQL_PASSWORD` (default: mysql)

### Optional Features
- Email: `EMAIL_USERNAME`, `EMAIL_PASSWORD`
- OAuth2: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`
- Cloudinary: `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`

## Installation

1. Clone the repository
2. Create MySQL database: `CREATE DATABASE scm;`
3. Configure environment variables
4. Run: `./mvnw spring-boot:run`
5. Access: http://localhost:8080

## Build

```bash
./mvnw clean install
```

## License

This project is available under standard licensing terms.
