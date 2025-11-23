package bg.university.mpr2025.server;

import bg.university.mpr2025.models.ScrapeResult;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

public class Server {
    private static final int BUFFER_SIZE = 1024;
    private static final String HOST = "localhost";

    private final int port;
    private final Gson gson = new Gson();

    private volatile boolean running;
    private Selector selector;
    private ByteBuffer buffer;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Server starting on " + HOST + ":" + port);
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            running = true;

            while (running) {
                try {
                    int ready = selector.select();
                    if (ready == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            accept(selector, key);
                        } else if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String request = readFromClient(clientChannel);
                            if (request == null) {
                                continue; // client closed
                            }
                            System.out.println("Received request: " + request.trim());

                            String response = buildResponseJson();
                            writeToClientAndClose(clientChannel, response + "\n");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error while processing client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to start server", e);
        }
    }

    public void stop() {
        running = false;
        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String readFromClient(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead < 0) {
            clientChannel.close();
            return null;
        }
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    private void writeToClientAndClose(SocketChannel clientChannel, String output) throws IOException {
        byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
        ByteBuffer out = ByteBuffer.wrap(bytes);
        while (out.hasRemaining()) {
            clientChannel.write(out);
        }
        clientChannel.close();
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = serverChannel.accept();
        if (client != null) {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("Client connected: " + client.getRemoteAddress());
        }
    }

    private String buildResponseJson() {
        // Build a placeholder scrape result similar to the previous blocking implementation
        ScrapeResult result = new ScrapeResult();
        result.setStatus("ok");
        result.setProcessingTimeMs(50);
        result.setResults(Arrays.asList("Result 1", "Result 2", "Result 3"));
        return gson.toJson(result);
    }
}
