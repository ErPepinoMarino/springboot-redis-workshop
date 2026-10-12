package com.redisworkshop.jobs;

public interface JobHandler {

    JobType type();

    JobOutcome handle(Job job);
}