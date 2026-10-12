package com.redisworkshop.jobs;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class JobsConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService jobExecutor() {
        return Executors.newFixedThreadPool(JobsConstants.CONSUMERS);
    }

    @Bean
    public ApplicationRunner jobWorkersStarter(JobWorker worker, JobQueue queue, ExecutorService jobExecutor) {
        return args -> {
            for (int i = 1; i <= JobsConstants.CONSUMERS; i++) {
                String workerId = "worker-" + i;
                jobExecutor.submit(() -> worker.work(workerId));
            }
        };
    }
}