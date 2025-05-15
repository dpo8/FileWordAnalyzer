package client;

import utility.BookProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConsumerImpl implements Consumer {
    private final Socket socket;

    public static ConsumerImpl create(String host, int port) {
        return new ConsumerImpl(host, port);
    }

    public ConsumerImpl(String host, int port) {
        this.socket = new Socket();
        try {
            this.socket.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            System.err.println("Error connecting to " + host + ":" + port);
        }
    }

    @Override
    public void start() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                BookProcessor.getInstance().addLine(line);
            }
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        if (this.socket != null)
            socket.close();
    }
}
