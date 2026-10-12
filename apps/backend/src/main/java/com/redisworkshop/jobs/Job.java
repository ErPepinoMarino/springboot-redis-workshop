package com.redisworkshop.jobs;

public class Job {

    private final String id;
    private final JobType type;
    private JobStatus status;
    private String workerId;
    private long durationMs;
    private String error;
    private int attempts;

    public Job(String id, JobType type) {
        this.id = id;
        this.type = type;
        this.status = JobStatus.PENDING;
        this.attempts = 0;
    }

    public String getId() { return id; }

    public JobType getType() { return type; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
}