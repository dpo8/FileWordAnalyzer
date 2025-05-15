package server;

import utility.NamedThreadFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProducerImpl implements Producer {
    private final ExecutorService executor;
    private final Path dataFile;
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    public static Producer create(int port, Path dataFile) throws IOException {
        return new ProducerImpl(port, dataFile);
    }

    public ProducerImpl(int port, Path dataFile) throws IOException {
        this.dataFile = dataFile;
        this.serverSocket = new ServerSocket(port);
        this.executor = Executors.newCachedThreadPool(new NamedThreadFactory("Producer", Thread.NORM_PRIORITY));
    }

    @Override
    public void start() {
        try {
            while (running) {
                try {
                    System.out.println("Waiting for connection...");
                    Socket client = serverSocket.accept(); // waiting for a client
                    System.out.println("Accepted connection from " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
                    executor.submit(() -> handleClient(client));
                } catch (SocketException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                    running = false;
                }

            }
        } catch (IOException e) {
            System.err.println("Error while starting producer: " + e.getMessage());
        }


    }

    private void handleClient(Socket client) {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile.toFile()));
             PrintWriter writer = new PrintWriter(client.getOutputStream(), true)) {
            reader.lines().forEach(writer::println);
        } catch (IOException e) {
            System.err.println("Error handling client: " + client.getInetAddress().getHostAddress() + " due " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void close() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server: " + e.getMessage());
            }
        }

        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }


}
