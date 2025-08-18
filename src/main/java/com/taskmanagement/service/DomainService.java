package com.taskmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.taskmanagement.repository.DomainRepository;
import com.taskmanagement.model.Domain;

import java.util.List;

@Service
public class DomainService {

    @Autowired
    private DomainRepository domainRepository;

    public List<Domain> getAllDomains() {
        return domainRepository.findAll();
    }

    public Domain getDomainById(Long id) {
        return domainRepository.findById(id).orElse(null);
    }

    public Domain createDomain(Domain domain) {
        return domainRepository.save(domain);
    }

    public Domain updateDomain(Long id, Domain domainDetails) {
        Domain domain = domainRepository.findById(id).orElse(null);
        if (domain != null) {
            domain.setName(domainDetails.getName());
            // Update other fields as necessary
            return domainRepository.save(domain);
        }
        return null;
    }

    public void deleteDomain(Long id) {
        domainRepository.deleteById(id);
    }
}