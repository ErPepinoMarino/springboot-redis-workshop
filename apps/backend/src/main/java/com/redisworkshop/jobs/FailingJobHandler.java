package com.redisworkshop.jobs;

import org.springframework.stereotype.Component;

@Component
public class FailingJobHandler implements JobHandler {

    @Override
    public JobType type() {
        return JobType.FAILING;
    }

    @Override
    public JobOutcome handle(Job job) {
        sleep(JobsConstants.FAILING_MS);
        //Simulamos el error, esto supongo que fingira un runtime error.
        throw new RuntimeException("Fallo simulado del trabajo");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}