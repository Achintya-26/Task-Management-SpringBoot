# Task Management Backend

This is a Spring Boot application for managing tasks, teams, and user activities. The application provides RESTful APIs for authentication, user management, team management, activity tracking, domain management, and notifications.

## Features

- User authentication and authorization
- Team management
- Activity tracking
- Domain management
- Real-time notifications using WebSocket
- File uploads

## Technologies Used

- Java 8
- Spring Boot
- Maven
- PostgreSQL (or any other database of your choice)
- WebSocket for real-time communication

## Project Structure

```
task-management-backend
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── taskmanagement
│   │   │           ├── TaskManagementApplication.java
│   │   │           ├── config
│   │   │           │   ├── DatabaseConfig.java
│   │   │           │   └── WebSocketConfig.java
│   │   │           ├── controller
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── UserController.java
│   │   │           │   ├── TeamController.java
│   │   │           │   ├── ActivityController.java
│   │   │           │   ├── DomainController.java
│   │   │           │   └── NotificationController.java
│   │   │           ├── service
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── UserService.java
│   │   │           │   ├── TeamService.java
│   │   │           │   ├── ActivityService.java
│   │   │           │   ├── DomainService.java
│   │   │           │   └── NotificationService.java
│   │   │           ├── repository
│   │   │           │   ├── UserRepository.java
│   │   │           │   ├── TeamRepository.java
│   │   │           │   ├── ActivityRepository.java
│   │   │           │   ├── DomainRepository.java
│   │   │           │   └── NotificationRepository.java
│   │   │           ├── model
│   │   │           │   ├── User.java
│   │   │           │   ├── Team.java
│   │   │           │   ├── Activity.java
│   │   │           │   ├── Domain.java
│   │   │           │   └── Notification.java
│   │   │           └── dto
│   │   │               ├── AuthRequest.java
│   │   │               ├── AuthResponse.java
│   │   │               └── ApiResponse.java
│   │   └── resources
│   │       ├── application.properties
│   │       └── static
│   │           └── uploads
│   └── test
│       └── java
│           └── com
│               └── taskmanagement
│                   └── TaskManagementApplicationTests.java
├── pom.xml
└── README.md
```

## Getting Started

1. Clone the repository:
   ```
   git clone <repository-url>
   ```

2. Navigate to the project directory:
   ```
   cd task-management-backend
   ```

3. Update the `application.properties` file with your database configuration.

4. Build the project using Maven:
   ```
   mvn clean install
   ```

5. Run the application:
   ```
   mvn spring-boot:run
   ```

6. Access the API at `http://localhost:8080/api`.

## API Documentation

Refer to the individual controller classes for detailed API endpoints and their usage.

## License

This project is licensed under the MIT License.