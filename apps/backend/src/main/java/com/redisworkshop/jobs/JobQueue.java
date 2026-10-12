package com.redisworkshop.jobs;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JobQueue {

    private final StreamOperations<String, String, String> stream;

    public JobQueue(StringRedisTemplate redis) {
        this.stream = redis.<String, String>opsForStream();
    }

    @PostConstruct 
    public void ensureGroup() {
        try {
            stream.createGroup(JobsConstants.STREAM, ReadOffset.from("0"), JobsConstants.GROUP);
        } catch (RedisSystemException ignored) {
            // BUSYGROUP: el grupo ya existe
        }
    }

    public void enqueue(String jobId) {
        stream.add(JobsConstants.STREAM, Map.of("jobId", jobId));
    }
    // Esto es para la llamada a la funcion internad, que no depende de nosotros, stream.read.
    // ¿Por qué? Porque espera un "StreamOffset<String> y crear una array de objetos genericos no se permite en Java.
    // Esto no es culpa nuestra, la libreria es así. Asi que SuppresedWarnings y a correr.
    @SuppressWarnings("unchecked")
    public List<MapRecord<String, String, String>> read(String consumer) {
        List<MapRecord<String, String, String>> records = stream.read(
                Consumer.from(JobsConstants.GROUP, consumer),
                StreamReadOptions.empty().count(1).block(Duration.ofMillis(JobsConstants.BLOCK_MS)),
                StreamOffset.create(JobsConstants.STREAM, ReadOffset.lastConsumed()));
        return records == null ? List.of() : records;
    }

    public void ack(RecordId id) {
        stream.acknowledge(JobsConstants.STREAM, JobsConstants.GROUP, id);
    }

    public List<MapRecord<String, String, String>> reclaim(String consumer) {
        PendingMessages pending = stream.pending(
                JobsConstants.STREAM, JobsConstants.GROUP,
                Range.unbounded(), 100,
                Duration.ofMillis(JobsConstants.RECLAIM_IDLE_MS));
        if (pending == null || pending.isEmpty()) {
            return List.of();
        }
        List<RecordId> ids = new ArrayList<>();
        for (PendingMessage message : pending) {
            ids.add(message.getId());
        }
        List<MapRecord<String, String, String>> claimed = stream.claim(
                JobsConstants.STREAM, JobsConstants.GROUP, consumer,
                Duration.ofMillis(JobsConstants.RECLAIM_IDLE_MS),
                ids.toArray(new RecordId[0]));
        return claimed == null ? List.of() : claimed;
    }
}