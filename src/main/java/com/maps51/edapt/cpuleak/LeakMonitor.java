package com.maps51.edapt.cpuleak;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class LeakMonitor implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(LeakMonitor.class);

    private int number;
    private RestTemplate restTemplate;
    private ResponseEntity<String> responseEntity;
    private final String url;
    private final HttpEntity<String> entity;

    private int iterationCount;
    private double averageTime;
    private long completedQueries = 1;
    private final double inequalityWeight;
    private double bound;
    private boolean showEveryResult = Boolean.TRUE;
    private int keepAliveAverageCount;

    public LeakMonitor(String url, int number, int numberForAverage, double inequalityWeight, int keepAliveAverageCount) {
        this.number = number;
        this.url = url;
        this.iterationCount = numberForAverage;
        this.inequalityWeight = inequalityWeight;
        this.keepAliveAverageCount = keepAliveAverageCount;
        restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        entity = new HttpEntity<>("{\"sessionLogId\":3800}", headers);
    }

    public void monitor() {
        long time = System.currentTimeMillis();
        responseEntity = restTemplate.postForEntity(url, entity, String.class);
        time = System.currentTimeMillis() - time;
        ++completedQueries;
        if(showEveryResult && completedQueries < iterationCount) {
            LOG.info("Thread #" + number + " get response: " + responseEntity.getStatusCode() + " in " + time  + " milliseconds.");
            averageTime += time;
        } else if (completedQueries == iterationCount) {
            LOG.info("Thread #" + number + " get response: " + responseEntity.getStatusCode() + " in " + time + " milliseconds.");
            if (showEveryResult) {
                showEveryResult = Boolean.FALSE;
                averageTime += time;
                averageTime = averageTime / iterationCount;
                this.bound = inequalityWeight * averageTime;
                LOG.info("Thread #" + number + " average response time from " + iterationCount + " requests is " + averageTime + "ms. Now messages will appear if response time is greater more then in " +  inequalityWeight + " times or if error response status code appear or once in " + keepAliveAverageCount + " requests in order to see that application is still alive.");
                iterationCount = keepAliveAverageCount;
            }

            completedQueries = 1;
        } else if(responseEntity.getStatusCode().value() != 200 || time >= bound) {
            LOG.error("!!!ATTENTION!!! Thread #" + number + " get response: " + responseEntity.getStatusCode() + " in " + time + " milliseconds.");
        }
    }

    @Override
    public void run() {
        monitor();
    }
}