package com.maps51.edapt.cpuleak;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public abstract class LeakMonitor implements Runnable {
    protected static final Logger LOG = LoggerFactory.getLogger(LeakMonitor.class);

    protected final int number;
    protected final String url;
    protected final HttpEntity<String> entity;
    protected RestTemplate restTemplate;
    protected ResponseEntity<String> responseEntity;

    protected final double inequalityWeight;
    protected final int keepAliveCount;
    protected double bound;
    protected long completedQueries = 0;

    public LeakMonitor(String url, int number, double inequalityWeight, int keepAliveCount) {
        this.number = number;
        this.url = url;
        this.inequalityWeight = inequalityWeight;
        this.keepAliveCount = keepAliveCount;
        restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        entity = new HttpEntity<>("{\"sessionLogId\":3800}", headers);
    }

    public abstract void monitor();

    @Override
    public abstract void run();
}