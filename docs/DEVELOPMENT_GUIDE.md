# Web Scraper Development Guide

## Table of Contents
1. [Development Environment Setup](#development-environment-setup)
2. [Project Structure](#project-structure)
3. [Build Process](#build-process)
4. [Running and Testing](#running-and-testing)
5. [Code Style and Standards](#code-style-and-standards)
6. [Version Control Workflow](#version-control-workflow)
7. [Debugging](#debugging)
8. [Performance Optimization](#performance-optimization)
9. [Security Considerations](#security-considerations)
10. [Deployment](#deployment)

## Development Environment Setup

### Prerequisites
- Java Development Kit (JDK) 20 or later
- Maven 3.6.0 or later
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

### IDE Configuration

#### IntelliJ IDEA
1. Import as Maven project
2. Enable annotation processing
3. Configure Java compiler compliance level to 20
4. Install and enable Lombok plugin if using Lombok

#### VS Code
1. Install Java Extension Pack
2. Configure Java home in settings
3. Install Maven for Java extension

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── bg/university/mpr2025/
│   │       ├── Main.java          # Application entry point
│   │       ├── client/            # Client implementation
│   │       │   ├── WebScraperClient.java
│   │       │   └── handlers/      # Client-side request handlers
│   │       ├── models/            # Data transfer objects
│   │       │   ├── ScraperRequest.java
│   │       │   └── ScraperResponse.java
│   │       ├── server/            # Server implementation
│   │       │   ├── Server.java
│   │       │   ├── ThreadPoolServer.java
│   │       │   └── SelectorServer.java
│   │       └── utils/             # Utility classes
│   └── resources/                 # Configuration files
│       └── log4j2.xml             # Logging configuration
├── test/                          # Test files
└── benchmark/                     # Performance testing
```

## Build Process

### Building the Project
```bash
# Clean and build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Build with specific Java version
mvn clean install -Djava.version=20
```

### Creating a Fat JAR
```bash
mvn clean package
```

## Running and Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TestClassName

# Run with debug output
mvn test -Dmaven.surefire.debug
```

### Code Coverage
```bash
mvn clean test jacoco:report
# Report will be available at target/site/jacoco/index.html
```

## Code Style and Standards

### Naming Conventions
- Package names: lowercase, e.g., `bg.university.mpr2025.client`
- Class names: PascalCase, e.g., `WebScraperClient`
- Method names: camelCase, e.g., `processRequest`
- Constants: UPPER_SNAKE_CASE, e.g., `MAX_RETRY_ATTEMPTS`

### Code Formatting
- Use 4 spaces for indentation
- Line length: 120 characters
- Use Unix-style line endings (LF)
- Use `@author` tag for class-level documentation
- Add Javadoc for public methods and classes

## Version Control Workflow

### Branching Strategy
- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: Feature branches
- `bugfix/*`: Bug fixes
- `release/*`: Release preparation

### Commit Message Format
```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

## Debugging

### Common Issues
1. **Connection Refused**
   - Verify server is running
   - Check firewall settings
   - Verify port availability

2. **Memory Issues**
   - Monitor heap usage with JVisualVM
   - Configure JVM memory settings

### Remote Debugging
```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar app.jar
```

## Performance Optimization

### JVM Tuning
```bash
# Example JVM options
-Xms512m -Xmx2G -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### Profiling
- Use Java Flight Recorder (JFR)
- Use VisualVM or YourKit for analysis

## Security Considerations

### Input Validation
- Validate all user inputs
- Sanitize URLs before processing
- Implement rate limiting

### Secure Communication
- Use HTTPS for client-server communication
- Validate SSL certificates
- Implement authentication/authorization

## Deployment

### Prerequisites
- Java Runtime Environment (JRE) 20+
- Sufficient system resources

### Installation
1. Copy the JAR file to the target server
2. Create a configuration file (if needed)
3. Set up environment variables

### Service Configuration (systemd)
```ini
[Unit]
Description=Web Scraper Service
After=network.target

[Service]
User=appuser
WorkingDirectory=/opt/web-scraper
ExecStart=/usr/bin/java -jar /opt/web-scraper/app.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

### Monitoring
- Log all important events
- Set up health checks
- Monitor resource usage

## License
[Your License Here]

## Contributing
[Your contribution guidelines here]
