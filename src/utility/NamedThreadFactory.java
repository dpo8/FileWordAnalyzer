package utility;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final String name;
    private final int priority;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public NamedThreadFactory(String name, int priority) {
        this.name = name;
        this.priority = priority;

        SecurityManager s = System.getSecurityManager();

        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, name + "-" + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon())
            t.setDaemon(false);

        if (t.getPriority() != priority)
            t.setPriority(priority);
        return t;
    }
}
