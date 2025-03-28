# Med-Hub

## 🏥 Introduction

Med-Hub is a web application designed for registering and managing medical visits. Med-Hub allows users to create accounts, book appointments, manage medical staff and services, and oversee visit schedules all in one intuitive platform.

## 📌 Table of Contents

- [Installation](#installation)
- [Usage and Features](#usage-and-features)
- [Dependencies](#dependencies)
- [Contact](#contact)
- [Database](#database)
- [Swagger API Endpoints](#swagger-api-endpoints)

## 🚀 Installation

To install Med-Hub, follow these steps:

1. Clone the repository:
```
    
```
2. Start the backend application:
- Navigate to the `backend` directory.
- Run the command:
```
./gradlew clean build
```
3. Start the frontend application:
- Navigate to the `frontend/med-hub` directory.
- Run the following command:
  ```
  npm install
  ```
4. Set up the infrastructure with Docker to start the application:

- Run this command in the root folder of the project.
```
docker-compose up --build
```
## ⚡ Usage and Features

#### Med-Hub offers several key features, including:

#### 🔑 User Management
- **Create a new user account**
- **Create an employee account**
- **Log in and log out of the platform**

#### 🏥 Medical Facility & Specialist Management
- **Add and remove medical facilities**
- **Add and remove specialists or services**

#### 📅 Appointment Scheduling
- **Book and cancel appointments**
- **Choose a specialist or service**
- **View appointment schedules**
- **View appointment history**
- **Manage appointments efficiently**

## Dependencies

#### Med-Hub is built using a variety of technologies and frameworks:

- **Backend:** Java 17, Spring Boot 3, Spring Framework 6, Gradle
- **Database:** PostgreSQL, Liquibase
- **Infrastructure:** Docker
- **Frontend:** React.js, HTML, CSS

#### Ensure you have these technologies installed and properly configured on your system to run Med-Hub!

## Database

The project employs PostgreSQL and Liquibase for database management, providing easy and secure tools for database creation, management, and migration. Utilizing infrastructure as code, it ensures control over database structure changes, using XML format for organized database schema management.

## Swagger API Endpoints

## Contact
Created by @liseeek - contact me!
- Email: lisszymon.contact@gmail.com
- Linkedin: https://www.linkedin.com/in/lis-szymon/
