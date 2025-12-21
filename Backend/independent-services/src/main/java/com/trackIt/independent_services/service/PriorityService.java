package com.trackIt.independent_services.service;

import com.trackIt.independent_services.exception.AlreadyExistsException;
import com.trackIt.independent_services.exception.ServiceException;
import com.trackIt.independent_services.mapper.PriorityMapper;
import com.trackIt.independent_services.model.Priorities;
import com.trackIt.independent_services.repository.PriorityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriorityService {

    private final PriorityRepository priorityRepository;

    @Transactional
    public Priorities addNew(String priorityLevel){
        if(priorityRepository.existsByPriorityLevel(priorityLevel)){
            log.warn("Priority level \"{}\" already exists", priorityLevel);
            throw new AlreadyExistsException("Priority", priorityLevel);
        }

        Priorities priorities = Priorities.builder()
                .priorityLevel(priorityLevel)
                .build();

        try {

            Priorities saved = priorityRepository.save(priorities);
            log.info("Successfully added priority: {}", saved.getPriorityLevel());
            return saved;

        } catch (Exception e) {

            log.info("Failed to add priority: {}", priorityLevel);
            throw new ServiceException("Failed to add priority: " + priorityLevel, e);

        }
    }

    @Transactional
    public List<Priorities> getAll() {
        List<Priorities> list = priorityRepository.findAll();
        return PriorityMapper.toResponseList(list);
    }

    public void deletePriority(Long id) {
        log.info("Request received to delete priority: {}",
                priorityRepository.findById(id).orElseThrow());
        try{
            priorityRepository.deleteById(id);
            log.info("Deletion Successful");
        } catch (Exception e) {
            log.info("Failed to delete priority with id: {}", id);
            throw new ServiceException("Failed to delete priority: "+id, e);
        }
    }
}
