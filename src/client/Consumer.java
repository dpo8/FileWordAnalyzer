package client;

public interface Consumer extends AutoCloseable {
    void start();
}
