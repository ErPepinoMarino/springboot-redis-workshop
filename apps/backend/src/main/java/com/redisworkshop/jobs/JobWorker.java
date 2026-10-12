package com.redisworkshop.jobs;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JobWorker {

    private final JobQueue queue;
    private final JobStore store;
    private final List<JobHandler> handlers;

    public JobWorker(JobQueue queue, JobStore store, List<JobHandler> handlers) {
        this.queue = queue;
        this.store = store;
        this.handlers = handlers;
    }

    public void work(String workerId) {
        while (!Thread.currentThread().isInterrupted()) {
            for (MapRecord<String, String, String> record : queue.read(workerId)) {
                process(record, workerId);
            }
        }
    }

    private void process(MapRecord<String, String, String> record, String workerId) {
        String jobId = record.getValue().get("jobId");
        Optional<Job> found = store.find(jobId);
        if (found.isEmpty()) {
            queue.ack(record.getId());
            return;
        }

        Job job = found.get();
        job.setStatus(JobStatus.PROCESSING);
        job.setWorkerId(workerId);
        job.setAttempts(job.getAttempts() + 1);
        store.save(job);

        long start = System.currentTimeMillis();
        JobOutcome outcome;
        try {
            outcome = handlerFor(job.getType()).handle(job);
        } catch (Exception e) {
            outcome = JobOutcome.FAILED;
            job.setError(e.getMessage());
        }
        job.setDurationMs(System.currentTimeMillis() - start);

        switch (outcome) {
            case DONE -> job.setStatus(JobStatus.DONE);
            case FAILED -> job.setStatus(JobStatus.FAILED);
            case ABANDON -> job.setStatus(JobStatus.ORPHANED);
        }
        store.save(job);

        if (outcome != JobOutcome.ABANDON) {
            queue.ack(record.getId());
        }
    }

    private JobHandler handlerFor(JobType type) {
        return handlers.stream()
                .filter(handler -> handler.type() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Sin handler para " + type));
    }
}