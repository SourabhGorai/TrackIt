package com.trackIt.independent_services.controller;

import com.trackIt.independent_services.dto.ApiResponse;
import com.trackIt.independent_services.dto.CompanyRequest;
import com.trackIt.independent_services.dto.CompanyResponse;
import com.trackIt.independent_services.dto.RolesResponse;
import com.trackIt.independent_services.model.Companies;
import com.trackIt.independent_services.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/companies")
@Slf4j
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> addCompany(@RequestBody CompanyRequest request){
        log.info("REST received to create company with name: {}", request.getCompanyName());

        CompanyResponse resp = companyService.addCompany(request);

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Company with name %s created", resp.getCompanyName()), resp)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getAllCompanies(){
        log.info("REST received to display all the companies");

        List<CompanyResponse> resp = companyService.getAllIncludingDeleted();

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Got %d companies", resp.size()), resp)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getActiveCompanies(){
        log.info("REST received to display all active the companies");

        List<CompanyResponse> resp = companyService.getActiveCompanies();

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Got %d companies", resp.size()), resp)
        );
    }

    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getDeletedCompanies(){
        log.info("REST received to display all deleted the companies");

        List<CompanyResponse> resp = companyService.getDeletedCompanies();

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Got %d companies", resp.size()), resp)
        );
    }

    @DeleteMapping("/{companyName}")
    public ResponseEntity<ApiResponse<?>> deleteCompany(@PathVariable String companyName){
        log.info("REST received to delete company: {}", companyName);

        companyService.delete(companyName);
        return ResponseEntity.ok(
                ApiResponse.success(String.format("%s removed from the table successfully", companyName))
        );
    }

    @GetMapping("/validate/{companyId}")
    public ResponseEntity<?> validateRole(@PathVariable Long companyId){
        log.info("REST request received to validate company with id: {}", companyId);
        CompanyResponse resp = companyService.validateCompany(companyId);
        System.out.println(resp);
        return ResponseEntity.ok(resp);
    }
}
