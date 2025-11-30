package bg.university.mpr2025.client;

import bg.university.mpr2025.models.ScrapeRequest;
import com.google.gson.Gson;
import bg.university.mpr2025.models.ScrapeResult;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final String host;
    private final int port;
    private final Gson gson = new Gson();

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void runInteractive() {
        try (Scanner sc = new Scanner(System.in)) {
            // Get user input
            System.out.print("Enter URL: ");
            String url = sc.nextLine();
            
            System.out.print("Enter number of threads: ");
            int threads = Integer.parseInt(sc.nextLine());
            
            System.out.print("Enter number of rows to return: ");
            int rows = Integer.parseInt(sc.nextLine());

            // Create request object
            ScrapeRequest req = new ScrapeRequest();
            req.url = url;
            req.threads = threads;
            req.rows = rows;

            // Convert to JSON and send to server
            try (Socket s = new Socket(host, port);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

                String json = gson.toJson(req);
                System.out.println("Sending request: " + json);
                
                out.write(json + "\n");
                out.flush();

                // Read and parse the response
                String responseJson = in.readLine();
                ScrapeResult result = gson.fromJson(responseJson, ScrapeResult.class);

                // Display results
                System.out.println("\nServer returned status: " + result.getStatus());
                System.out.println("Processing time: " + result.getProcessingTimeMs() + "ms");
                System.out.println("Results (" + result.getResults().size() + " items):");
                for (String r : result.getResults()) {
                    System.out.println(" - " + r);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Please enter valid numbers for threads and rows");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
