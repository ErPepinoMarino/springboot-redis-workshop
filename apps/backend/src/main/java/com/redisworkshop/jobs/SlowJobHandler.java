package com.redisworkshop.jobs;

import org.springframework.stereotype.Component;

@Component
public class SlowJobHandler implements JobHandler {

    @Override
    public JobType type() {
        return JobType.SLOW;
    }

    @Override
    public JobOutcome handle(Job job) {
        sleep(JobsConstants.SLOW_MS);
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