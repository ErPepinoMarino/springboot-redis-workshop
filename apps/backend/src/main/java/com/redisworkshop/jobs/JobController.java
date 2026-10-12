package com.redisworkshop.jobs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService service;

    public JobController(JobService service) {
        this.service = service;
    }

    @PostMapping
    public JobResponse enqueue(@RequestBody JobRequest request) {
        return JobResponse.from(service.enqueue(request.type()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> get(@PathVariable String id) {
        return service.get(id)
                .map(JobResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}