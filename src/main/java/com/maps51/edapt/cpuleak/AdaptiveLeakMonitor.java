package com.maps51.edapt.cpuleak;

public class AdaptiveLeakMonitor extends LeakMonitor {

    private int iterationCount;
    private double averageTime;
    private boolean showEveryResult = Boolean.TRUE;

    public AdaptiveLeakMonitor(String url, int number, double inequalityWeight, int keepAliveCount, int numberForAverage) {
        super(url, number, inequalityWeight, keepAliveCount);
        this.iterationCount = numberForAverage;
    }

    @Override
    public void monitor() {
        long time = System.currentTimeMillis();
        responseEntity = restTemplate.postForEntity(url, entity, String.class);
        time = System.currentTimeMillis() - time;

        completedQueries++;
        if (showEveryResult && completedQueries < iterationCount) {
            LOG.info("Thread #" + number + " get response: " + responseEntity.getStatusCode() + " in " + time + " milliseconds.");
            averageTime += time;
        } else if (completedQueries == iterationCount) {
            LOG.info("Thread #" + number + " get response: " + responseEntity.getStatusCode() + " in " + time + " milliseconds.");
            if (showEveryResult) {
                countAverageTime(time);
            }
            completedQueries = 0;
        } else if (responseEntity.getStatusCode().value() != 200 || time >= bound) {
            LOG.error("!!!ATTENTION!!! Thread #" + number + " get response: " + responseEntity.getStatusCode() + " in " + time + " milliseconds.");
        }
    }

    private void countAverageTime(long lastTimestamp) {
        showEveryResult = Boolean.FALSE;
        averageTime += lastTimestamp;
        averageTime = averageTime / iterationCount;
        this.bound = inequalityWeight * averageTime;
        LOG.info("Thread #" + number + " average response time from " + iterationCount + " requests is " + averageTime + "ms. Now messages will appear if response time is greater more then in " + inequalityWeight + " times or if error response status code appear or once in " + keepAliveCount + " requests in order to see that application is still alive.");
        iterationCount = keepAliveCount;
    }

    @Override
    public void run() {
        monitor();
    }
}
