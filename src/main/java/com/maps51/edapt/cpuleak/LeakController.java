package com.maps51.edapt.cpuleak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Controller
@RequestMapping("/")
public class LeakController {

    private static final Logger LOG = LoggerFactory.getLogger(LeakController.class);

    @Value("${url}")
    private String url;

    @Value("${delay}")
    private int delay;

    @Value("${threads}")
    private int threadCount;

    @Value("${inequality}")
    private double inequalityWeight;

    @Value("${average-count}")
    private int averageCount;

    @Value("${keep-alive-count}")
    private int keepAliveCount;

    @Value("${average-time}")
    private String averageTime;

    private LeakService service;

    @RequestMapping(method = RequestMethod.GET)
    public String printWelcome(ModelMap model) {
        model.addAttribute("message", "Much cool DDOS application.");
        return "hello";
    }

    @PostConstruct
    public void initService() {
        Double averageTimeValue = averageTime.length() > 0 ? Double.valueOf(averageTime) : null;
        service = new LeakService(threadCount);
        for (int i = 0; i < threadCount; i++) {
            service.start(i, url, averageCount, inequalityWeight, delay, keepAliveCount, averageTimeValue);
        }

    }

    @PreDestroy
    public void destroyService() {
        service.destroy();
    }
}