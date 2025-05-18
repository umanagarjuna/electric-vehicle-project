# Electric Vehicle Population Data API

This project provides a complete solution for managing electric vehicle population data. It includes a robust Spring Boot API service, a Java command-line client, PostgreSQL database integration, and Kubernetes deployment capabilities using Helm.

## Table of Contents

- [Project Overview](#project-overview)
- [System Architecture](#system-architecture)
- [Prerequisites](#prerequisites)
- [Running the Services](#running-the-services)
  - [Building the Docker Image](#building-the-docker-image)
  - [Kubernetes Deployment with Helm](#kubernetes-deployment-with-helm)
  - [Verifying the Deployment](#verifying-the-deployment)
  - [Accessing the Application](#accessing-the-application)
- [API Documentation](#api-documentation)
- [Testing with the Java API Client](#testing-with-the-java-api-client)
- [Batch Operations](#batch-operations)
- [Observability](#observability)
- [Project Structure](#project-structure)
- [Testing](#testing)

## Project Overview

This application provides a complete data management solution for electric vehicle population data. The data source is the [Electric Vehicle Population Data](https://catalog.data.gov/dataset/electric-vehicle-population-data) from data.gov, which contains information about electric vehicles registered in Washington state.

Key features:
- PostgreSQL database with PostGIS extension for spatial data
- RESTful API for CRUD operations
- Java command-line client for API interaction
- Batch update capabilities
- Kubernetes deployment with Helm
- Comprehensive observability with logging, metrics, and distributed tracing

## System Architecture

The system consists of the following components:

1. **ev-api-service**: Spring Boot application that:
   - Provides REST endpoints for CRUD operations
   - Connects to PostgreSQL database
   - Handles CSV data loading
   - Implements logging, metrics, and tracing

2. **ev-api-client-java**: Java command-line client that:
   - Allows interaction with the API from the command line
   - Supports all CRUD operations
   - Enables batch updates

3. **PostgreSQL with PostGIS**: Database that:
   - Stores electric vehicle data with spatial capabilities
   - Supports efficient geospatial queries
   - Manages data integrity

4. **Helm deployment**: Kubernetes deployment that:
   - Packages the application and dependencies
   - Simplifies deployment and scaling
   - Configures environment-specific settings

## Prerequisites

To run this application, you'll need:

- Docker and Docker Compose
- Kubernetes cluster (Minikube, Kind, or a cloud provider's Kubernetes service)
- kubectl command-line tool configured to interact with your cluster
- Helm package manager
- Java 17 or later
- Maven

## Running the Services

The services are designed to be run as Docker containers and deployed via Kubernetes using Helm.

### Building the Docker Image

First, build the Docker image for the `ev-api-service`:

```powershell
# Navigate to the project root
cd electric-vehicle-project

# Go to the API service directory
cd ev-api-service

# Build the Spring Boot application
mvn clean package

# Build the Docker image
docker build -t ev-api-service:latest .

# Return to the project root
cd ..
```

### Kubernetes Deployment with Helm

After building the Docker image, deploy the application using the provided Helm chart:

```powershell
# Create a namespace for the deployment
kubectl create namespace dev

# Install the Helm chart
helm install ev-api-service ./helm/ev-api-service -n dev --set image.repository=ev-api-service --set image.pullPolicy=IfNotPresent
```

This command:
- Installs the chart named `ev-api-service` from the `./helm/ev-api-service` directory
- Deploys it to the `dev` namespace
- Sets the image repository to `ev-api-service`
- Sets the image pull policy to `IfNotPresent` (uses the local Docker image if it exists)

### Verifying the Deployment

Check the status of your deployment:

```powershell
# Check pod status
kubectl get pods -n dev

# Wait until pods are running
kubectl get pods -n dev -w
```

Wait until both the PostgreSQL pod and your API service pod are in the "Running" state.

### Accessing the Application

Once the pods are running, port-forward the service to access it from your local machine:

```powershell
kubectl port-forward service/ev-api-service 8080:80 -n dev
```

This command forwards local port 8080 to port 80 on the `ev-api-service` Kubernetes service.

## API Documentation

Once the service is running and port-forwarded, you can access the API documentation (Swagger UI) in your browser:

```
http://localhost:8080/swagger-ui/index.html
```

This interface allows you to explore and test the API endpoints interactively.

The API provides the following endpoints:

- `GET /api/v1/vehicles`: Get all vehicles (paginated)
- `GET /api/v1/vehicles/{vin}`: Get a vehicle by VIN
- `POST /api/v1/vehicles`: Create a new vehicle
- `PUT /api/v1/vehicles/{vin}`: Update a vehicle
- `DELETE /api/v1/vehicles/{vin}`: Delete a vehicle
- `PATCH /api/v1/vehicles/batch/msrp`: Update MSRP for vehicles by make and model

Additionally, data loading endpoints:
- `POST /api/v1/data-loader/load-csv`: Upload and process CSV data(upload sample data csv file downloaded from the website)
- `GET /api/v1/data-loader/job-status/{jobId}`: Check status of data loading job

## CSV Data Loading Implementation

The application includes a robust data loading feature designed to handle the Electric Vehicle Population Data CSV file from the data.gov source.

### Asynchronous Processing Approach

The `/api/v1/data-loader/load-csv` endpoint enables efficient processing of large CSV files through:

- **Multipart File Upload**: Accepts CSV files via standard multipart/form-data requests
- **Immediate Response**: Returns a job ID immediately rather than blocking until completion
- **Configurability**: Allows adjustment of batch size to optimize for different environments

### Key Implementation Features

1. **Asynchronous Processing**: Background processing using Spring's async capabilities
2. **Job Management**: Tracking system using UUIDs to monitor each upload job
3. **Batch Processing**: Chunked CSV processing to manage memory efficiently
4. **Smart Data Handling**: UPSERT operations to handle duplicate records gracefully
5. **Real-time Progress Tracking**: Live updates on processing status and completion percentage

### Job Status Monitoring

After submitting a CSV file, clients can track progress via the status endpoint:
```
GET /api/v1/data-loader/job-status/{jobId}
```

The status response provides comprehensive information including processing state, record counts, and progress percentage.

### Usage Workflow

1. Download the Electric Vehicle Population Data CSV from data.gov
2. Upload via Swagger UI or Postman, specifying desired batch size
3. Receive job ID for tracking
4. Monitor progress via status endpoint
5. Once completed, access the imported data through the vehicle API endpoints

This implementation provides an efficient solution for loading large datasets with immediate feedback to users, solving the common timeout issues associated with synchronous approaches.

## Testing with the Java API Client

A Java-based API client is provided to test the service's functionalities:

```powershell
# Navigate to the API client directory
cd electric-vehicle-project\ev-api-client-java

# Build the client if needed
mvn clean package

# List existing vehicles
java -jar target/ev-api-client-java-1.0-SNAPSHOT-jar-with-dependencies.jar list

# Get details of a specific vehicle
java -jar target/ev-api-client-java-1.0-SNAPSHOT-jar-with-dependencies.jar get 3MW39FF06P

# Create a new vehicle
java -jar target/ev-api-client-java-1.0-SNAPSHOT-jar-with-dependencies.jar create --vin "TEST123ABC" --make "TESLA" --model "Model 3" --year 2023 --dol-id 123456789 --county "King" --city "Seattle" --state "WA" --zip "98101" --ev-type "Battery Electric Vehicle (BEV)" --range 310 --msrp 45000.00

# Verify the new vehicle is created
java -jar target/ev-api-client-java-1.0-SNAPSHOT-jar-with-dependencies.jar get TEST123ABC

# Update the vehicle's MSRP along with others of the same make and model
java -jar target/ev-api-client-java-1.0-SNAPSHOT-jar-with-dependencies.jar update-msrp-batch --make "TESLA" --model "Model 3" --new-msrp 47500.00

# Verify the update worked
java -jar target/ev-api-client-java-1.0-SNAPSHOT-jar-with-dependencies.jar get TEST123ABC

# Delete the vehicle
java -jar target/ev-api-client-java-1.0-SNAPSHOT-jar-with-dependencies.jar delete TEST123ABC

# Verify the deletion
java -jar target/ev-api-client-java-1.0-SNAPSHOT-jar-with-dependencies.jar get TEST123ABC
# This should return a 404 Not Found response
```

## Batch Operations

### Updating MSRP for Tesla Model Y vehicles

To update the Base MSRP for all Tesla Model Y vehicles, you can use the batch update endpoint through the API client:

```powershell
java -jar target/ev-api-client-java-1.0-SNAPSHOT-jar-with-dependencies.jar update-msrp-batch --make "TESLA" --model "Model Y" --new-msrp 52990.00
```

**Assumptions:**
1. Make and model names are case-insensitive (e.g., "TESLA" will match "Tesla" in the database)
2. All vehicles of the specified make and model should have their MSRP updated to the same value
3. The update is performed as a single database transaction
4. The operation is idempotent - running it multiple times with the same parameters produces the same result
5. This batch operation is more efficient than updating vehicles individually, especially for large datasets

## Observability

The application implements comprehensive observability features:

### Logging

- Structured logging with log levels (DEBUG, INFO, WARN, ERROR)
- Privacy-aware logging with PII (Personally Identifiable Information) masking
- Integration with Spring Boot Actuator for log level management
- Sensitive fields (like VIN, location data) are masked in logs to protect privacy

### Metrics

- Micrometer metrics collection
- Prometheus metrics endpoint at `/actuator/prometheus`
- Custom metrics for API operations, database performance, and more
- Counters for tracking vehicle creation, updates, and deletions
- Timers for measuring operation durations and identifying bottlenecks

### Distributed Tracing

- Integration with Zipkin through Micrometer Tracing
- Trace correlation across services
- Performance monitoring for critical operations
- Detailed operation tracking with tagged spans

## Project Structure

The project is organized as follows:

```
electric-vehicle-project/
├── ev-api-service/                # Spring Boot API service
│   ├── src/                       # Source code
│   │   ├── main/                  # Application code
│   │   │   ├── java/              # Java source files
│   │   │   └── resources/         # Application resources
│   │   └── test/                  # Test code
│   ├── Dockerfile                 # Docker configuration
│   └── pom.xml                    # Maven configuration
├── ev-api-client-java/            # Java API client
│   ├── src/                       # Source code
│   │   ├── main/                  # Application code
│   │   │   ├── java/              # Java source files
│   │   │   └── resources/         # Application resources
│   │   └── test/                  # Test code
│   └── pom.xml                    # Maven configuration
├── helm/                          # Helm charts for Kubernetes deployment
│   └── ev-api-service/            # Chart for the API service
│       ├── templates/             # Chart templates
│       ├── Chart.yaml             # Chart metadata
│       └── values.yaml            # Chart values
├── README.md                      # This file
└── pom.xml                        # Parent Maven configuration
```

## Testing

The project includes comprehensive testing:

### Unit Tests

- Test individual components in isolation
- Mock dependencies using Mockito
- Test edge cases and error handling

### Integration Tests

- Test component interactions
- Use TestContainers for PostgreSQL testing
- Verify end-to-end flows

### Repository Tests

- Test database operations
- Verify query performance
- Test transactions and rollbacks

To run the tests:

```powershell
# Run all tests
mvn test

# Run specific test classes in module(e.g., ev-api-service)
mvn test -Dtest=ElectricVehicleServiceTest

# Run with coverage reports
mvn test jacoco:report
```

Coverage reports are generated in `target/site/jacoco/index.html`.
