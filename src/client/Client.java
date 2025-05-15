package client;

import utility.BookProcessor;
import utility.NamedThreadFactory;
import utility.Utility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage : java server.Server <host> <port> <host> <port>");
            System.exit(1);
        }
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(2, new NamedThreadFactory("Client", Thread.NORM_PRIORITY));
        try (Consumer consumer = ConsumerImpl.create(args[0], Integer.parseInt(args[1]));
             Consumer consumer1 = ConsumerImpl.create(args[2], Integer.parseInt(args[3]));) {
            Future<?> task = executor.submit(consumer::start);
            Future<?> task2 = executor.submit(consumer1::start);
            Utility.waitToFinishTasks(task, task2);
            BookProcessor.getInstance().stopProcessing( );

            System.out.println(BookProcessor.getInstance().getTopUsedWords(5));

            System.out.println("Done in " + (System.currentTimeMillis() - startTime) + "ms");
        } catch (Exception e) {
            System.err.println("Exception caught: " + e);
        } finally {
            executor.shutdown();
        }
    }
}
