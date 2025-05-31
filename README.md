# AchieveSync Platform Implementation

An event-driven microservices platform for consistent goal achievement built with Spring Boot, Axon Framework, and Flutter Web.

## Architecture

- **User Service**: Manages user authentication, profiles, and consistency points
- **Goal Service**: Handles goal creation, progress tracking, and streak maintenance
- **Frontend**: Flutter Web application providing responsive UI
- **Database**: PostgreSQL for event store and read models
- **Communication**: gRPC for inter-service communication, Event Bus for async messaging

## Technology Stack

### Backend
- Java 17+
- Spring Boot 3.x
- Axon Framework 4.x
- PostgreSQL
- gRPC
- Maven

### Frontend
- Dart
- Flutter Web
- Material Design

## Project Structure

```
implementation/
├── backend/
│   ├── user-service/     # User management microservice
│   ├── goal-service/     # Goal management microservice
│   └── shared/           # Shared models and utilities
├── frontend/             # Flutter web application
└── docker/              # Docker configuration files
```

## Getting Started

1. Set up PostgreSQL database
2. Start backend services (User Service, Goal Service)
3. Launch Flutter web frontend
4. Access application at http://localhost:8080

## Features

- Smart goal setting with SMART criteria
- Progress tracking and visualization
- Streak maintenance for consistency
- Consistency points system (gamification)
- Personalized analytics and insights
- Social features for accountability
- Cross-platform responsive design