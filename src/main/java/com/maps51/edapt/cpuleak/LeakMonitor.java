package com.maps51.edapt.cpuleak;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public abstract class LeakMonitor implements Runnable {
    protected static final Logger LOG = LoggerFactory.getLogger(LeakMonitor.class);
    private static final int READ_TIMEOUT = 3 * 60 * 1000;
    private static final int CONNECTION_TIMEOUT = 3 * 60 * 1000;

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
        restTemplate = new RestTemplate(clientHttpRequestFactory());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        entity = new HttpEntity<>("{\"sessionLogId\":3800}", headers);
    }

    public abstract void monitor();

    @Override
    public void run() {
        try {
            monitor();
        } catch (RestClientException e) {
            LOG.error("!!!ERROR!!! Thread #{}: something went wrong: {}", number, e.getLocalizedMessage());
        }
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(READ_TIMEOUT);
        factory.setConnectTimeout(CONNECTION_TIMEOUT);
        return factory;
    }
}
