package bg.university.mpr2025.server;

import bg.university.mpr2025.models.ScrapeRequest;
import bg.university.mpr2025.models.ScrapeResult;
import bg.university.mpr2025.scrapper.ParallelScraper;
import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolServer {
    private static final String HOST = "localhost";
    
    private final int port;
    private final int threadPoolSize;
    private final Gson gson = new Gson();
    private volatile boolean running;
    private ServerSocket serverSocket;
    private ExecutorService executor;

    public ThreadPoolServer(int port, int threadPoolSize) {
        this.port = port;
        this.threadPoolSize = threadPoolSize;
    }

    public void start() {
        System.out.println("ThreadPool Server starting on " + HOST + ":" + port);
        running = true;
        executor = Executors.newFixedThreadPool(threadPoolSize);
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdown();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8))) {

            String requestJson = in.readLine();
            if (requestJson == null) {
                return;
            }
            
            ScrapeRequest request = gson.fromJson(requestJson, ScrapeRequest.class);
            
            // Validate request
            if (request == null || request.url == null || request.url.trim().isEmpty()) {
                sendErrorResponse(out, "Invalid request: URL is required");
                return;
            }
            
            // Process the request
            ParallelScraper scraper = new ParallelScraper();
            int threads = request.threads > 0 ? request.threads : 1;
            int rows = request.rows > 0 ? request.rows : 10;
            
            try {
                long startTime = System.currentTimeMillis();
                List<String> results = scraper.scrape(request.url, threads, rows);
                long processingTime = System.currentTimeMillis() - startTime;
                
                ScrapeResult result = new ScrapeResult();
                result.setStatus("success");
                result.setProcessingTimeMs(processingTime);
                result.setResults(results);
                
                out.write(gson.toJson(result) + "\n");
                out.flush();
                
            } catch (Exception e) {
                sendErrorResponse(out, "Error during scraping: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
    
    private void sendErrorResponse(BufferedWriter out, String errorMessage) throws IOException {
        ScrapeResult errorResult = new ScrapeResult();
        errorResult.setStatus("error");
        errorResult.setResults(List.of(errorMessage));
        out.write(gson.toJson(errorResult) + "\n");
        out.flush();
    }
}
