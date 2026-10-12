package com.redisworkshop.jobs;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


//Busca trabajos que no han sido marcados como terminados en un tiempo suficiente como para llamarlos "orphans".
//Los reencola para que otro worker los procese.
@Component
public class JobReclaimer {

    private final JobQueue queue;
    private final JobStore store;

    public JobReclaimer(JobQueue queue, JobStore store) {
        this.queue = queue;
        this.store = store;
    }

    @Scheduled(fixedDelay = 2_000)
    public void reclaim() {
        List<MapRecord<String, String, String>> orphans = queue.reclaim("reclaimer");
        for (MapRecord<String, String, String> record : orphans) {
            requeue(record);
            queue.ack(record.getId());
        }
    }

    private void requeue(MapRecord<String, String, String> record) {
        String jobId = record.getValue().get("jobId");
        store.find(jobId).ifPresent(job -> {
            job.setStatus(JobStatus.PENDING);
            store.save(job);
        });
        queue.enqueue(jobId);
    }
}