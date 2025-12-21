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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServicesService {

    private final ServicesRepository servicesRepository;
    private final CompanyRepository companyRepository;

    public ServicesResponse addService(ServicesRequest request) {

        // Fetch client company
        Companies clientCompany = companyRepository.findById(request.getClientCompanyId())
                .orElseThrow(() -> new RuntimeException("Client company not found"));

        // Fetch provider company
        Companies providerCompany = companyRepository.findById(request.getProviderCompanyId())
                .orElseThrow(() -> new RuntimeException("Provider company not found"));

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

    public List<ServicesResponse> getAll() {

        log.info("Received request to fetch all services");
        List<Services> list = servicesRepository.findAll();
        return ServiceMapper.toResponseList(list);
    }

    public List<ServicesResponsePublic> getAllPublic() {
        log.info("Received request to fetch all services in public view");
        List<Services> list = servicesRepository.findAll();
        return ServiceMapper.toResponseListPublic(list);
    }

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
}
