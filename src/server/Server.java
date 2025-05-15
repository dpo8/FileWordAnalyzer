package server;

import utility.NamedThreadFactory;
import utility.Utility;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {



    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage : java server.Server <port> <file> <port> <file>");
            System.exit(1);
        }
        ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory("Server", Thread.NORM_PRIORITY));

        try (Producer producer = ProducerImpl.create(Integer.parseInt(args[0]), Path.of(args[1]));
             Producer producer2 = ProducerImpl.create(Integer.parseInt(args[2]), Path.of(args[3]))) {

            executor.submit(producer::start);
            executor.submit(producer2::start);

            CompletableFuture<Void> shutdown = Utility.shutdown(executor, () -> {
                producer.stop();
                producer2.stop();
            });
            shutdown.join();
        } catch (Exception e) {
            System.err.println("Exception occured: ");
            e.printStackTrace();
        }
    }



}
