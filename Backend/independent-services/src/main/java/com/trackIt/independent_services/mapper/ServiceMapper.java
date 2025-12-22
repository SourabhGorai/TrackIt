package com.trackIt.independent_services.mapper;
import com.trackIt.independent_services.dto.ServicesResponse;
import com.trackIt.independent_services.dto.ServicesResponsePublic;
import com.trackIt.independent_services.model.Services;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static String sanitizeName(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            return null;
        }

        return serviceName.trim()
                .replaceAll("\\s+", " ");

    }

    public static List<ServicesResponse> toResponseList(List<Services> services) {
        if (services == null || services.isEmpty()){
            return List.of();
        }
        return services.stream()
                .map(ServiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static ServicesResponse toResponse(Services service) {
        if(service == null){
            return null;
        }

        return ServicesResponse.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .clientCompanyId(service.getClientCompany().getCompanyId())
                .providerCompanyId((service.getProviderCompany().getCompanyId()))
                .build();
    }

    public static List<ServicesResponsePublic> toResponseListPublic(List<Services> services) {
        if (services == null || services.isEmpty()){
            return List.of();
        }
        return services.stream()
                .map(ServiceMapper::toResponsePublic)
                .collect(Collectors.toList());
    }

    public static ServicesResponsePublic toResponsePublic(Services service) {
        if(service == null){
            return null;
        }

        return ServicesResponsePublic.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .build();
    }

}
