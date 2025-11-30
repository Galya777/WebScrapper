# Web Scraper Application

A Java-based web scraping application that can run in both client and server modes, allowing for distributed web scraping tasks.

## Prerequisites

- Java 20 or later
- Maven 3.6.0 or later
- Internet connection (for downloading dependencies and scraping)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── bg/university/mpr2025/
│   │       ├── Main.java          # Entry point
│   │       ├── client/            # Client implementation
│   │       ├── models/            # Data models
│   │       └── server/            # Server implementation
│   └── resources/                 # Configuration files (if any)
```

## Building the Project

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd mpr2025_KN_FN_java_linux
   ```

2. Build the project using Maven:
   ```bash
   mvn clean package
   ```
   This will create a `target` directory containing the compiled classes and a runnable JAR file.

## Running the Application

The application can be run in three modes: server mode, client mode, and scraper mode.

### Running the Server

1. Start the server with the following command:
   ```bash
   # Using Maven
   mvn exec:java -Dexec.mainClass="bg.university.mpr2025.Main" -Dexec.args="server"
   
   # Or using the compiled JAR
   java -jar target/mpr2025_KN_FN_java_linux-1.0-SNAPSHOT.jar server
   ```

2. The server will start on port 5555 by default.

### Running the Client

1. In a separate terminal, start the client with:
   ```bash
   # Using Maven
   mvn exec:java -Dexec.mainClass="bg.university.mpr2025.Main" -Dexec.args="client"
   
   # Or using the compiled JAR
   java -jar target/mpr2025_KN_FN_java_linux-1.0-SNAPSHOT.jar client
   ```

2. The client will connect to the server at localhost:5555 and provide an interactive interface.

### Running the Web Scraper Example

1. Run the web scraper with the following command:
   ```bash
   # Using Maven
   mvn exec:java -Dexec.mainClass="bg.university.mpr2025.Main" -Dexec.args="scraper"
   
   # Or using the compiled JAR
   java -jar target/mpr2025_KN_FN_java_linux-1.0-SNAPSHOT.jar scraper
   ```

2. When prompted, you can specify:
   - URL to scrape (default: https://techcrunch.com)
   - Number of threads (default: 4)
   - Number of results to display (default: 10)

Example output:
```
=== Simple Web Scraper Example ===

Enter URL to scrape [https://techcrunch.com]: 
Enter number of threads (1-10) [4]: 
Enter number of rows to return (1-100) [10]: 

Scraping https://techcrunch.com with 4 threads...

=== Scraping Results (10 items) ===

1. Latest Tech News Headlines
2. Breaking technology news and analysis
3. Startup News and Business Insights
...
```

The scraper will extract article titles and content from the specified website and display them in the console.

## Development

### Importing into an IDE

1. **IntelliJ IDEA**:
   - Open the project directory
   - Select "Open as Maven Project" when prompted
   - Wait for dependencies to be downloaded

2. **Eclipse**:
   - Run `mvn eclipse:eclipse`
   - Import as existing Maven project

### Running Tests

To run the test suite:

```bash
mvn test
```

## Configuration

Currently, the application uses the following default configuration:
- Server port: 5555
- Server host: localhost

To modify these settings, you'll need to update the source code in `Main.java`.

## Troubleshooting

- **Port already in use**: If you get a port binding error, make sure no other instance of the server is running or change the port number in the code.
- **Connection refused**: Ensure the server is running before starting the client.
- **Dependency issues**: Try running `mvn clean install` if you encounter dependency-related errors.

## License

[Specify your license here]

## Contributing

[Add contribution guidelines if applicable]
