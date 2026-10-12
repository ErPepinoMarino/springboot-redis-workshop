package com.redisworkshop.jobs;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class JobStore {

    private final StringRedisTemplate redis;

    public JobStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void save(Job job) {
        String key = key(job.getId());
        redis.opsForHash().putAll(key, toMap(job));
        redis.expire(key, JobsConstants.JOB_TTL);
    }

    public Optional<Job> find(String id) {
        Map<Object, Object> fields = redis.opsForHash().entries(key(id));
        if (fields.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromMap(id, fields));
    }

    private String key(String id) {
        return JobsConstants.JOB_KEY_PREFIX + id;
    }

    private Map<String, String> toMap(Job job) {
        Map<String, String> m = new HashMap<>();
        m.put("type", job.getType().name());
        m.put("status", job.getStatus().name());
        m.put("workerId", job.getWorkerId() == null ? "" : job.getWorkerId());
        m.put("durationMs", Long.toString(job.getDurationMs()));
        m.put("error", job.getError() == null ? "" : job.getError());
        m.put("attempts", Integer.toString(job.getAttempts()));
        return m;
    }

    private Job fromMap(String id, Map<Object, Object> fields) {
        Job job = new Job(id, JobType.valueOf(str(fields, "type")));
        job.setStatus(JobStatus.valueOf(str(fields, "status")));
        job.setWorkerId(blankToNull(str(fields, "workerId")));
        job.setDurationMs(Long.parseLong(str(fields, "durationMs")));
        job.setError(blankToNull(str(fields, "error")));
        job.setAttempts(Integer.parseInt(str(fields, "attempts")));
        return job;
    }

    private String str(Map<Object, Object> fields, String key) {
        Object value = fields.get(key);
        return value == null ? "" : value.toString();
    }

    private String blankToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}