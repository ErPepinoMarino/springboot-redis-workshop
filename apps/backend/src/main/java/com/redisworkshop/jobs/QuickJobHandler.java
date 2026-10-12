package com.redisworkshop.jobs;

import org.springframework.stereotype.Component;

@Component
public class QuickJobHandler implements JobHandler {

    @Override
    public JobType type() {
        return JobType.QUICK;
    }

    @Override
    public JobOutcome handle(Job job) {
        sleep(JobsConstants.QUICK_MS);
        return JobOutcome.DONE;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}