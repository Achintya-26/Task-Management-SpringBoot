package com.taskmanagement.controller;

import com.taskmanagement.model.Domain;
import com.taskmanagement.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domains")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class DomainController {

    @Autowired
    private DomainService domainService;

    @GetMapping
    public ResponseEntity<List<Domain>> getAllDomains() {
    	System.out.println("Domain route hit!");
        List<Domain> domains = domainService.getAllDomains();
        return ResponseEntity.ok(domains);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Domain> getDomainById(@PathVariable Long id) {
        Domain domain = domainService.getDomainById(id);
        return ResponseEntity.ok(domain);
    }

    @PostMapping
    public ResponseEntity<Domain> createDomain(@RequestBody Domain domain) {
        Domain createdDomain = domainService.createDomain(domain);
        return ResponseEntity.status(201).body(createdDomain);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Domain> updateDomain(@PathVariable Long id, @RequestBody Domain domain) {
        Domain updatedDomain = domainService.updateDomain(id, domain);
        return ResponseEntity.ok(updatedDomain);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDomain(@PathVariable Long id) {
        domainService.deleteDomain(id);
        return ResponseEntity.noContent().build();
    }
}