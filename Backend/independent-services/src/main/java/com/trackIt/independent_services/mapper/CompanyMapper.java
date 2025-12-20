package com.trackIt.independent_services.mapper;

import com.trackIt.independent_services.dto.CompanyResponse;
import com.trackIt.independent_services.model.Companies;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompanyMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static String sanitizeName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return null;
        }

        return companyName.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();
    }

    public static List<CompanyResponse> toResponseList(List<Companies> companies) {
        if (companies == null || companies.isEmpty()){
            return List.of();
        }
        return companies.stream()
                .map(CompanyMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static CompanyResponse toResponse(Companies company) {
        if(company == null){
            return null;
        }

        return CompanyResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyType(company.getCompanyType())
                .build();
    }

}
