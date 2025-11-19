package bg.university.mpr2025.client;

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
            System.out.print("Enter any request (placeholder): ");
            String input = sc.nextLine();

            try (Socket s = new Socket(host, port);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

                out.write(input + "\n");
                out.flush();

                String responseJson = in.readLine();
                ScrapeResult result = gson.fromJson(responseJson, ScrapeResult.class);

                System.out.println("Server returned status: " + result.getStatus());
                System.out.println("Processing time: " + result.getProcessingTimeMs() + "ms");
                System.out.println("Results:");
                for (String r : result.getResults()) {
                    System.out.println(" - " + r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
