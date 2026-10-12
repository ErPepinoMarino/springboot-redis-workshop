package com.redisworkshop.jobs;

public record JobResponse(
        String id,
        JobType type,
        JobStatus status,
        String workerId,
        long durationMs,
        String error,
        int attempts) {

    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getType(),
                job.getStatus(),
                job.getWorkerId(),
                job.getDurationMs(),
                job.getError(),
                job.getAttempts());
    }
}