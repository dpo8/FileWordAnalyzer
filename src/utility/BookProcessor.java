package utility;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class BookProcessor {
    private static volatile BookProcessor bookProcessor = null;

    private final Pattern pattern = Pattern.compile("\\b\\w+\\b");
    private final Map<String, Integer> wordCounts;
    private final BlockingQueue<String> queueofLines;
    private final ExecutorService dispatcher;
    private final ExecutorService worker;
    private final Future<?> dispatcherTask;
    private volatile boolean stopped;

    public static BookProcessor getInstance() {
        if (bookProcessor == null) {
            synchronized (BookProcessor.class) {
                if (bookProcessor == null) {
                    bookProcessor = new BookProcessor();
                }
            }
        }
        return bookProcessor;
    }

    private BookProcessor() {
        wordCounts = new ConcurrentHashMap<>();
        queueofLines = new LinkedBlockingDeque<>(10000);
        dispatcher = Executors.newSingleThreadExecutor(new NamedThreadFactory("bookProcessorDispatcher", Thread.NORM_PRIORITY));
        worker = Executors.newCachedThreadPool(new NamedThreadFactory("bookProcessorWorker", Thread.NORM_PRIORITY));
        dispatcherTask = dispatcher.submit(this::processLine);
    }

    public void addLine(String line) {
        try {
            queueofLines.offer(line, 1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("Unable to process line: " + line);
        }
    }

    public void stopProcessing() {
        try {
            stopped = true;
            Utility.waitToFinishTasks(dispatcherTask);
            dispatcher.shutdownNow();
            worker.shutdown();
            if (!worker.awaitTermination(10, TimeUnit.SECONDS)) {
                worker.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("Stopping processing interrupted");
        }
    }

    private void processLine() {
        try {
            String line;
            while (((line = queueofLines.poll()) != null) || !stopped) {
                if (line != null) {
                    String l = line.toLowerCase(Locale.ROOT);
                    worker.execute(() -> {
                        Matcher matcher = pattern.matcher(l);
                        while (matcher.find()) {
                            wordCounts.merge(matcher.group(), 1, Integer::sum);
                        }
                    });
                }
            }
        } catch (Exception e) {
            //done
        }
    }

    public List<Map.Entry<String, Integer>> getTopUsedWords(int top) {
        return wordCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(top).collect(Collectors.toList());
    }
}
