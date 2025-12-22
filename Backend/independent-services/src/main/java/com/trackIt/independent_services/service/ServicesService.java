package com.trackIt.independent_services.service;

import com.trackIt.independent_services.dto.ServicesRequest;
import com.trackIt.independent_services.dto.ServicesResponse;
import com.trackIt.independent_services.dto.ServicesResponsePublic;
import com.trackIt.independent_services.exception.NotFoundException;
import com.trackIt.independent_services.exception.ServiceException;
import com.trackIt.independent_services.mapper.ServiceMapper;
import com.trackIt.independent_services.model.Companies;
import com.trackIt.independent_services.model.Services;
import com.trackIt.independent_services.repository.CompanyRepository;
import com.trackIt.independent_services.repository.ServicesRepository;
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
public class ServicesService {

    private final ServicesRepository servicesRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    @CacheEvict(
            value = {
                    "services",
                    "servicesPublic",
                    "servicesByClient",
                    "servicesByProvider",
                    "servicesByName"
            },
            allEntries = true
    )
    public ServicesResponse addService(ServicesRequest request) {

        // Fetch client company
        Companies clientCompany = companyRepository.findById(request.getClientCompanyId())
                .orElseThrow(() -> new RuntimeException("Client company not found"));

        if(!clientCompany.getCompanyType().equals("CLIENT")){
            throw new ServiceException(String.format("No client company exists with ID: %d",
                    request.getClientCompanyId()));
        }

        log.info("Attempting to create service: {}, for client: {}",
                request.getServiceName(), clientCompany.getCompanyName());

        boolean exists = servicesRepository
                .existsByServiceNameAndClientCompany_CompanyIdAndProviderCompany_CompanyId(
                        request.getServiceName(),
                        request.getClientCompanyId(),
                        request.getProviderCompanyId()
                );

        if (exists) {
            throw new ServiceException(
                    "Service already exists for this client-provider pair"
            );
        }

        // Fetch provider company
        Companies providerCompany = companyRepository.findById(request.getProviderCompanyId())
                .orElseThrow(() -> new RuntimeException("Provider company not found"));

        if(!providerCompany.getCompanyType().equals("PROVIDER")){
            throw new ServiceException(String.format("No provider company exists with ID: %d",
                    request.getClientCompanyId()));
        }

        log.info(
                "Received request to add new Service '{}' for client company '{}' (ID={})",
                request.getServiceName(),
                clientCompany.getCompanyName(),
                clientCompany.getCompanyId()
        );

        // Create entity
        Services service = Services.builder()
                .serviceName(request.getServiceName())
                .clientCompany(clientCompany)
                .providerCompany(providerCompany)
                .build();

        try {

            Services saved = servicesRepository.save(service);
            log.info("Successfully added service with name: {}", saved.getServiceName());
            return ServiceMapper.toResponse(saved);

        } catch (Exception e) {

            log.info("Failed to add service: {}", request.getServiceName());
            throw new ServiceException("Failed to add service: " +request.getServiceName(), e);

        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "services")
    public List<ServicesResponse> getAll() {

        log.info("Received request to fetch all services");
        List<Services> list = servicesRepository.findAll();
        return ServiceMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "servicesPublic")
    public List<ServicesResponsePublic> getAllPublic() {
        log.info("Received request to fetch all services in public view");
        List<Services> list = servicesRepository.findAll();
        return ServiceMapper.toResponseListPublic(list);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "servicesById", key = "#serviceId")
    public ServicesResponse getById(Long serviceId) {

        log.info("Received request to fetch data for service with id: {}", serviceId);

        try{
            Services service = servicesRepository.findById(serviceId).orElseThrow();
            return ServiceMapper.toResponse(service);
        }catch(Exception e){
            log.info("Failed to get service with request id: {}", serviceId);
            throw new NotFoundException("Services", serviceId.toString());
        }
    }


    @CacheEvict(
            value = {
                    "services",
                    "servicesPublic",
                    "servicesById",
                    "servicesByClient",
                    "servicesByProvider",
                    "servicesByName"
            },
            allEntries = true
    )
    @Transactional
    public void deleteService(Long id) {
        log.info("Request received to delete service with ID: {}", id);

        try{
            servicesRepository.deleteById(id);
            log.info("Service deleted successfully");
        }catch(Exception e){
            log.info("Failed to delete service with request id: {}", id);
            throw new ServiceException("Failed to delete service with ID: "
                    +id, e);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "servicesByClient", key = "#id")
    public List<ServicesResponse> getListWRTClient(Long id) {
        log.info("Attempting to fetch services related to client with company ID: {}", id);

        List<Services> list = servicesRepository.findByClientCompany_CompanyId(id);

        if (list.isEmpty()) {
            log.info("No services found for Client Company id: {}", id);
            throw new NotFoundException("Services", id.toString());
        }

        log.info("Fetched services successfully");
        return ServiceMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "servicesByProvider", key = "#id")
    public List<ServicesResponse> getListWRTProvider(Long id) {
        log.info("Attempting to fetch services provided by company with ID: {}", id);

        List<Services> list = servicesRepository.findByProviderCompany_CompanyId(id);

        if (list.isEmpty()) {
            log.info("No services found for Provider Company id: {}", id);
            throw new NotFoundException("Services", id.toString());
        }

        log.info("Fetched services successfully.");
        return ServiceMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "servicesByName", key = "#sanitizeName")
    public List<ServicesResponse> getAllByName(String sanitizeName) {
        log.info("Attempting to fetch services by name: {}", sanitizeName);
        List<Services> list = servicesRepository.findByServiceName(sanitizeName);
        if(list.isEmpty()){
            log.info("No services found by name: {}", sanitizeName);
            throw new NotFoundException("Services", sanitizeName);
        }

        log.info("Successfully fetched {} services", list.size());
        return ServiceMapper.toResponseList(list);
    }
}
