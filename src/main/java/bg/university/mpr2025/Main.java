package bg.university.mpr2025;

import bg.university.mpr2025.server.Server;
import bg.university.mpr2025.client.Client;
import bg.university.mpr2025.scrapper.ParallelScraper;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -jar <jar> [server|client|scraper]");
            System.out.println("  server - Start the server");
            System.out.println("  client - Start the client");
            System.out.println("  scraper - Run the web scraper example");
            return;
        }
        
        switch (args[0].toLowerCase()) {
            case "server":
                startServer();
                break;
            case "client":
                startClient();
                break;
            case "scraper":
                runWebScraperExample();
                break;
            default:
                System.out.println("Unknown command. Use 'server', 'client', or 'scraper'.");
        }
    }
    
    private static void startServer() throws Exception {
        System.out.println("Starting server on port 5555...");
        Server server = new Server(5555);
        server.start();
    }
    
    private static void startClient() {
        System.out.println("Starting client. Connecting to localhost:5555...");
        Client client = new Client("localhost", 5555);
        client.runInteractive();
    }
    
    private static void runWebScraperExample() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Simple Web Scraper Example ===\n");
        
        // Default configuration
        String defaultUrl = "https://techcrunch.com";
        int defaultThreads = 4;
        int defaultRows = 10;
        
        try {
            // Get URL from user or use default
            System.out.print("Enter URL to scrape [" + defaultUrl + "]: ");
            String urlInput = scanner.nextLine().trim();
            String url = urlInput.isEmpty() ? defaultUrl : urlInput;
            
            // Get number of threads from user or use default
            System.out.print("Enter number of threads (1-10) [" + defaultThreads + "]: ");
            String threadsInput = scanner.nextLine().trim();
            int threads = threadsInput.isEmpty() ? defaultThreads : Integer.parseInt(threadsInput);
            threads = Math.max(1, Math.min(10, threads)); // Clamp between 1 and 10
            
            // Get number of rows from user or use default
            System.out.print("Enter number of rows to return (1-100) [" + defaultRows + "]: ");
            String rowsInput = scanner.nextLine().trim();
            int rows = rowsInput.isEmpty() ? defaultRows : Integer.parseInt(rowsInput);
            rows = Math.max(1, Math.min(100, rows)); // Clamp between 1 and 100
            
            System.out.println("\nScraping " + url + " with " + threads + " threads...\n");
            
            // Create and run the scraper
            ParallelScraper scraper = new ParallelScraper();
            List<String> results = scraper.scrape(url, threads, rows);
            
            // Display results
            System.out.println("=== Scraping Results (" + results.size() + " items) ===\n");
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }
            
            System.out.println("\n=== Scraping completed successfully! ===");
            
        } catch (NumberFormatException e) {
            System.err.println("Error: Please enter valid numbers for threads and rows");
        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
