package com.redisworkshop.jobs;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class JobService {

    private final JobStore store;
    private final JobQueue queue;

    public JobService(JobStore store, JobQueue queue) {
        this.store = store;
        this.queue = queue;
    }

    public Job enqueue(JobType type) {
        Job job = new Job(UUID.randomUUID().toString(), type);
        store.save(job);
        queue.enqueue(job.getId());
        return job;
    }

    public Optional<Job> get(String id) {
        return store.find(id);
    }
}