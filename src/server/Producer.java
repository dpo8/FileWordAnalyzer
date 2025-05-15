package server;

public interface Producer extends AutoCloseable {
    void start();
    void stop();
}
