package com.trackIt.independent_services.service;

import com.trackIt.independent_services.exception.AlreadyExistsException;
import com.trackIt.independent_services.exception.NotFoundException;
import com.trackIt.independent_services.exception.ServiceException;
import com.trackIt.independent_services.mapper.CompanyMapper;
import com.trackIt.independent_services.mapper.PriorityMapper;
import com.trackIt.independent_services.model.Priorities;
import com.trackIt.independent_services.repository.PriorityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriorityService {

    private final PriorityRepository priorityRepository;

    @CacheEvict(value = "priorities", allEntries = true)
    @Transactional
    public Priorities addNew(String priorityLevel){
        if(priorityRepository.existsByPriorityLevel(priorityLevel)){
            log.warn("Priority level \"{}\" already exists", priorityLevel);
            throw new AlreadyExistsException("Priority", priorityLevel);
        }

        String sanitizePriority = CompanyMapper.sanitizeName(priorityLevel);

        Priorities priorities = Priorities.builder()
                .priorityLevel(sanitizePriority)
                .build();

        try {

            Priorities saved = priorityRepository.save(priorities);
            log.info("Successfully added priority: {}", saved.getPriorityLevel());
            return saved;

        } catch (Exception e) {

            log.info("Failed to add priority: {}", sanitizePriority);
            throw new ServiceException("Failed to add priority: " + sanitizePriority, e);

        }
    }

    @Cacheable(value = "priorities")
    @Transactional(readOnly = true)
    public List<Priorities> getAll() {
        log.info("Attempting to fetch all priorities");
        List<Priorities> list = priorityRepository.findAll();
        log.info("Found {} priorities", list.size());
        return PriorityMapper.toResponseList(list);
    }

    @CacheEvict(value = "priorities", allEntries = true)
    @Transactional
    public void deletePriority(Long id) {
        log.info("Request received to delete priority with ID: {}", id);
        try{
            priorityRepository.deleteById(id);
            log.info("Deletion successful for priority ID: {}", id);
        } catch (Exception e) {
            log.info("Failed to delete priority with id: {}", id);
            throw new ServiceException("Failed to delete priority: "+id, e);
        }
    }

    @Transactional
    public Priorities getById(Long id) {
        log.info("Attempting to fetch priority with ID: {}", id);
        try{
            return priorityRepository.findById(id).orElseThrow();
        } catch (Exception e) {
            log.info("Failed to get priority with id: {}", id);
            throw new ServiceException("Failed to get priority: "+id, e);
        }
    }
}