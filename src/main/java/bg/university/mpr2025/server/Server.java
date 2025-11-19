package bg.university.mpr2025.server;

import bg.university.mpr2025.models.ScrapeResult;
import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {
    private final int port;
    private final Gson gson = new Gson();

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        System.out.println("Server started on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Client connected: " + client.getInetAddress());

                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

                    // Четем заявката (за сега няма нужда от парсинг)
                    String request = in.readLine();
                    System.out.println("Received request: " + request);

                    // Създаваме фиктивен резултат
                    ScrapeResult result = new ScrapeResult();
                    result.setStatus("ok");
                    result.setProcessingTimeMs(50);
                    result.setResults(Arrays.asList("Result 1", "Result 2", "Result 3"));

                    String jsonResponse = gson.toJson(result);
                    out.write(jsonResponse + "\n");
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    client.close();
                }
            }
        }
    }
}
