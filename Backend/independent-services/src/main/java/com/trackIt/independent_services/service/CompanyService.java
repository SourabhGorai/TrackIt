package com.trackIt.independent_services.service;

import com.trackIt.independent_services.dto.CompanyRequest;
import com.trackIt.independent_services.dto.CompanyResponse;
import com.trackIt.independent_services.exception.AlreadyExistsException;
import com.trackIt.independent_services.exception.NotFoundException;
import com.trackIt.independent_services.exception.ServiceException;
import com.trackIt.independent_services.mapper.CompanyMapper;
import com.trackIt.independent_services.model.Companies;
import com.trackIt.independent_services.repository.CompanyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;

    @CacheEvict(
            value = {"allCompanies", "activeCompanies", "deletedCompanies"},
            allEntries = true
    )
    @Transactional
    public CompanyResponse addCompany(CompanyRequest request) {

        String sanitizedCompanyName = CompanyMapper.sanitizeName(request.getCompanyName());
        String sanitizedCompanyType = CompanyMapper.sanitizeName(request.getCompanyType());
        log.info("Attempt to create company with name: {}", sanitizedCompanyName);

        if (companyRepository.existsByCompanyNameAndIsDeletedFalse(sanitizedCompanyName)) {
            log.warn("{} already exists in the table", sanitizedCompanyName);
            throw new AlreadyExistsException("Company", sanitizedCompanyName);
        }

        if(companyRepository.existsByCompanyNameAndIsDeletedTrue(sanitizedCompanyName)){
            log.info("Re-activating previously deleted company: {}", sanitizedCompanyName);
            Companies companies = companyRepository.findByCompanyNameAndIsDeletedTrue(sanitizedCompanyName)
                    .orElseThrow(() -> new NotFoundException("Company",sanitizedCompanyName));

            companies.restore();

            Companies reactivated = companyRepository.save(companies);
            log.info("Successfully reactivated company: {}", sanitizedCompanyName);
            return CompanyMapper.toResponse(reactivated);
        }

        Companies company = Companies.builder()
                .companyName(sanitizedCompanyName)
                .companyType(sanitizedCompanyType)
                .isDeleted(false)
                .build();

        try {

            Companies saved = companyRepository.save(company);
            log.info("Successfully added company: {}", saved.getCompanyName());
            return CompanyMapper.toResponse(saved);

        } catch (Exception e) {

            log.info("Failed to add company: {}", sanitizedCompanyName);
            throw new ServiceException("Failed to add company: " + sanitizedCompanyName, e);

        }
    }


    @Cacheable("allCompanies")
    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllIncludingDeleted() {

        log.info("Attempt to get all Company details");
        List<Companies> list = companyRepository.findAll();
        log.info("Found {} companies", list.size());
        return CompanyMapper.toResponseList(list);

    }

    @Cacheable("activeCompanies")
    @Transactional(readOnly = true)
    public List<CompanyResponse> getActiveCompanies() {

        log.info("Attempt to get all the active Company details");
        List<Companies> list = companyRepository.findByIsDeletedFalse();
        log.info("Found {} companies", list.size());
        return CompanyMapper.toResponseList(list);

    }

    @Cacheable("deletedCompanies")
    @Transactional(readOnly = true)
    public List<CompanyResponse> getDeletedCompanies() {

        log.info("Attempt to get all deleted Company details");
        List<Companies> list = companyRepository.findByIsDeletedTrue();
        log.info("Found {} companies", list.size());
        return CompanyMapper.toResponseList(list);

    }


    @CacheEvict(
            value = {"allCompanies", "activeCompanies", "deletedCompanies"},
            allEntries = true
    )
    @Transactional
    public void delete(String companyName) {

        String sanitizedName = CompanyMapper.sanitizeName(companyName);
        log.info("Attempt to delete company: {}", sanitizedName);

        try{
            Companies company = companyRepository.findByCompanyName(sanitizedName)
                    .orElseThrow(() -> new NotFoundException("Company", sanitizedName));

            company.softDelete();
            companyRepository.save(company);
            log.info("Deleted company: {}", sanitizedName);

        }catch(Exception e){
            log.info("Failed to delete company: {}", sanitizedName);
            throw new ServiceException("Failed to delete company: "+sanitizedName, e);
        }

    }

    @Transactional(readOnly = true)
    public CompanyResponse validateCompany(Long companyId) {
        Companies companies = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company", companyId.toString()));
        return CompanyMapper.toResponse(companies);
    }


    public Companies getById(Long id) {
        log.info("Trying to fetch company with ID: {}", id);
        try{
            return companyRepository.findById(id).orElseThrow();
        }catch(Exception e){
            log.info("Failed to fetch company with Id: {}", id);
            throw new NotFoundException("Services", id.toString());
        }
    }

    public List<CompanyResponse> getClients() {
        log.info("Attempting to fetch all the Clients");
        try{
            List<Companies> companies = companyRepository.findByCompanyType("CLIENT");
            return CompanyMapper.toResponseList(companies);
        }catch(Exception e){
            log.info("Failed to fetch clients");
            throw new ServiceException("Failed to get clients", e);
        }
    }

    public List<CompanyResponse> getProviders() {
        log.info("Attempting to fetch all the providers");
        try{
            List<Companies> companies = companyRepository.findByCompanyType("PROVIDER");
            return CompanyMapper.toResponseList(companies);
        }catch(Exception e){
            log.info("Failed to fetch providers");
            throw new ServiceException("Failed to get providers", e);
        }
    }
}