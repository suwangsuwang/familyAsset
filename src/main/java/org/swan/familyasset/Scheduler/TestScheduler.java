package org.swan.familyasset.Scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestScheduler {

    @Scheduled(fixedRate = 5000)
    public void test() {
        System.out.println("定时任务执行了:" + System.currentTimeMillis());
    }

    @Scheduled(fixedDelay = 5000)
    public void dispatch() {

    }
}
