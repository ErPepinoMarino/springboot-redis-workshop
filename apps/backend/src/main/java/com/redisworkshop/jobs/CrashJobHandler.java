package com.redisworkshop.jobs;

import org.springframework.stereotype.Component;

@Component
public class CrashJobHandler implements JobHandler {

    @Override
    public JobType type() {
        return JobType.CRASH;
    }

    @Override
    public JobOutcome handle(Job job) {
        sleep(JobsConstants.CRASH_MS);
        if(job.getAttempts() == 1) {
            return JobOutcome.ABANDON;
        }else{
            return JobOutcome.DONE;
        }     
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}