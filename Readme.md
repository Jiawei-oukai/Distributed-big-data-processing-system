# Distributed Big Data Processing System

## Overview

Distributed Big Data Processing System is an advanced web application that efficiently handles routing and processing of requests related to plans within a distributed big data environment. It is equipped with a robust controller class and leverages modern technologies for seamless data processing.

## Technologies Used

- **Programming Language**: Java
- **Framework**: Spring Boot
- **Database**: MongoDB
- **Messaging**: RabbitMQ

## Features

- Robust handling of routing and processing requests related to plans.
- CRUD operations for managing plans.
- Utilization of MongoDB for data storage.
- Messaging and communication through RabbitMQ.
- Extensible and scalable architecture to support distributed processing.

## Project Structure

The project is structured mainly around the controller class which handles the routing and processing of requests. Alongside, it has configuration files for setting up MongoDB and RabbitMQ connections and other dependencies:

- `Controller Class`: Handles the routing and processing of requests.
- `application.properties`: Contains configuration for MongoDB and RabbitMQ.
- `Maven/Gradle`: Used for building the project.

## Getting Started

### Prerequisites

- Java Development Kit (JDK)
- Spring Boot
- MongoDB
- RabbitMQ

### Setting Up and Running the Project

1. Clone the project repository from GitHub:

    ```bash
    git clone <repository_url>
    ```

2. Open the project in your preferred Java IDE.

3. Configure the MongoDB connection details in the `application.properties` file.

    ```properties
    spring.data.mongodb.uri=YOUR_MONGODB_URI
    ```

4. Configure the RabbitMQ connection details in the `application.properties` file.

    ```properties
    spring.rabbitmq.host=YOUR_RABBITMQ_HOST
    spring.rabbitmq.port=YOUR_RABBITMQ_PORT
    spring.rabbitmq.username=YOUR_RABBITMQ_USERNAME
    spring.rabbitmq.password=YOUR_RABBITMQ_PASSWORD
    ```

5. Build the project using Maven or Gradle.

    ```bash
    mvn clean install
    ```

6. Run the application.

    ```bash
    java -jar target/your-artifact-name.jar
    ```

7. Access the homepage by navigating to `http://localhost:<port>/home` in your web browser.

## API Endpoints

- `GET /plan/find/{key}`: Retrieves a plan by its key.
- `POST /plan/save`: Creates a new plan.
- `DELETE /plan/delete/{key}`: Deletes a plan by its key.
- `PATCH /plan/patch/{id}`: Updates a plan partially.
- `PUT /plan/put/{id}`: Updates a plan completely.

## Customization

You can extend the application by adding more functionalities, integrating additional technologies, or scaling it to handle a larger dataset.

## Contributing

Contributions are welcome! Feel free to fork this project and submit your enhancements through pull requests.


## License

This project is open source and available under the MIT License. Please see the `LICENSE` file for more details.

Happy coding!
