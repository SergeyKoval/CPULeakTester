package com.maps51.edapt.cpuleak;

import org.quartz.SchedulerException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeakService {

    final ScheduledExecutorService schExService;

    public LeakService(int threadCount) {
        schExService = Executors.newScheduledThreadPool(threadCount);
    }

    public void destroy() throws SchedulerException {
        schExService.shutdown();
    }

    public void start(int threadNumber, String url, int averageCount, double inequalityWeight, long delay, int keepAliveCount, Double expectAverageTime) {
        final Runnable obj = expectAverageTime == null ?
                new AdaptiveLeakMonitor(url, threadNumber, inequalityWeight, keepAliveCount, averageCount) :
                new AveragedLeakMonitor(url, threadNumber, inequalityWeight, keepAliveCount, expectAverageTime);
        schExService.scheduleWithFixedDelay(obj, delay * threadNumber, delay, TimeUnit.MILLISECONDS);
    }
}
