package bg.university.mpr2025.server;

import bg.university.mpr2025.models.ScrapeRequest;
import bg.university.mpr2025.models.ScrapeResult;
import bg.university.mpr2025.scrapper.ParallelScraper;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectorServer {
    private static final String HOST = "localhost";
    private static final int BUFFER_SIZE = 4096;
    
    private final int port;
    private final int workerThreads;
    private final Gson gson = new Gson();
    private volatile boolean running;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private ExecutorService workerPool;

    public SelectorServer(int port, int workerThreads) {
        this.port = port;
        this.workerThreads = workerThreads;
    }

    public void start() {
        System.out.println("SelectorServer starting on " + HOST + ":" + port);
        running = true;
        workerPool = Executors.newFixedThreadPool(workerThreads);
        
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(HOST, port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            while (running) {
                try {
                    selector.select();
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectedKeys.iterator();
                    
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        
                        if (!key.isValid()) {
                            continue;
                        }
                        
                        if (key.isAcceptable()) {
                            acceptClient(key);
                        } else if (key.isReadable()) {
                            readFromClient(key);
                        }
                    }
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error in selector loop: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start SelectorServer: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(BUFFER_SIZE));
            System.out.println("Client connected: " + clientChannel.getRemoteAddress());
        }
    }
    
    private void readFromClient(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        
        try {
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                clientChannel.close();
                return;
            }
            
            String requestData = new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8);
            if (requestData.endsWith("\n")) {
                buffer.clear();
                workerPool.submit(() -> processRequest(clientChannel, requestData.trim()));
            }
        } catch (IOException e) {
            try {
                clientChannel.close();
            } catch (IOException ex) {
                System.err.println("Error closing client channel: " + ex.getMessage());
            }
        }
    }
    
    private void processRequest(SocketChannel clientChannel, String requestJson) {
        try {
            ScrapeRequest request = gson.fromJson(requestJson, ScrapeRequest.class);
            
            if (request == null || request.url == null || request.url.trim().isEmpty()) {
                sendErrorResponse(clientChannel, "Invalid request: URL is required");
                return;
            }
            
            ParallelScraper scraper = new ParallelScraper();
            int threads = request.threads > 0 ? request.threads : 1;
            int rows = request.rows > 0 ? request.rows : 10;
            
            try {
                long startTime = System.currentTimeMillis();
                var results = scraper.scrape(request.url, threads, rows);
                long processingTime = System.currentTimeMillis() - startTime;
                
                ScrapeResult result = new ScrapeResult();
                result.setStatus("success");
                result.setProcessingTimeMs(processingTime);
                result.setResults(results);
                
                String response = gson.toJson(result) + "\n";
                clientChannel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
                
            } catch (Exception e) {
                sendErrorResponse(clientChannel, "Error during scraping: " + e.getMessage());
            }
            
        } catch (Exception e) {
            sendErrorResponse(clientChannel, "Invalid request format: " + e.getMessage());
        } finally {
            try {
                clientChannel.close();
            } catch (IOException e) {
                System.err.println("Error closing client channel: " + e.getMessage());
            }
        }
    }
    
    private void sendErrorResponse(SocketChannel channel, String errorMessage) {
        try {
            ScrapeResult errorResult = new ScrapeResult();
            errorResult.setStatus("error");
            errorResult.setResults(java.util.List.of(errorMessage));
            String response = gson.toJson(errorResult) + "\n";
            channel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            System.err.println("Error sending error response: " + e.getMessage());
        }
    }
    
    public void stop() {
        running = false;
        if (selector != null && selector.isOpen()) {
            try {
                selector.close();
            } catch (IOException e) {
                System.err.println("Error closing selector: " + e.getMessage());
            }
        }
        if (workerPool != null) {
            workerPool.shutdown();
        }
    }
}
