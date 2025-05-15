package utility;

import java.util.concurrent.*;

public final class Utility {
    public static CompletableFuture<Void> shutdown(ExecutorService executor, Runnable runnable) {
        CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();


        System.out.println("press ^C to stop");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down");
            runnable.run();

            executor.shutdown();
            try {
                if (executor.awaitTermination(10, TimeUnit.SECONDS))
                    executor.shutdownNow();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                shutdownFuture.complete(null);
            }
        }));
        return shutdownFuture;
    }

    public static void waitToFinishTasks(Future<?>... tasks) {
        for (Future<?> task : tasks) {
            try {
                task.get(); // Waits until the task completes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                System.err.println("Task was interrupted: " + e.getMessage());
            } catch (ExecutionException e) {
                System.err.println("Task execution failed: " + e.getCause());
            }
        }
    }
}
