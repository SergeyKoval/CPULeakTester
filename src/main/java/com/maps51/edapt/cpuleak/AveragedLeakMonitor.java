package com.maps51.edapt.cpuleak;

import org.springframework.web.client.RestClientException;

public class AveragedLeakMonitor extends LeakMonitor {

    public AveragedLeakMonitor(String url, int number, double inequalityWeight, int keepAliveCount, Double expectAverageTime) {
        super(url, number, inequalityWeight, keepAliveCount);
        this.bound = expectAverageTime * inequalityWeight;
        LOG.info("Thread #" + number + " got expected average response time for " + expectAverageTime + " ms. So, messages will appear if response time is greater than " + inequalityWeight + " times or if error response status code appear or once in " + keepAliveCount + " requests in order to see that application is still alive.");
    }

    @Override
    public void monitor() throws RestClientException {
        long time = System.currentTimeMillis();
        responseEntity = restTemplate.postForEntity(url, entity, String.class);
        time = System.currentTimeMillis() - time;
        completedQueries++;
        if (responseEntity.getStatusCode().value() != 200 || time >= bound) {
            LOG.error("!!!ATTENTION!!! Thread #" + number + " get response: " + responseEntity.getStatusCode() + " in " + time + " milliseconds.");
        } else if (completedQueries == keepAliveCount) {
            LOG.info("Thread #" + number + " get response: " + responseEntity.getStatusCode() + " in " + time + " milliseconds.");
            completedQueries = 0;
        }
    }
}
