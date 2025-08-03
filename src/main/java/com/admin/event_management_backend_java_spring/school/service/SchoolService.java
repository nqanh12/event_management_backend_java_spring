package com.admin.event_management_backend_java_spring.school.service;

import com.admin.event_management_backend_java_spring.school.model.School;
import com.admin.event_management_backend_java_spring.payload.ApiResponse;
import com.admin.event_management_backend_java_spring.school.payload.request.SchoolRequest;
import com.admin.event_management_backend_java_spring.school.payload.response.SchoolResponse;
import com.admin.event_management_backend_java_spring.school.repository.SchoolRepository;
import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SchoolService {

    @Autowired
    private SchoolRepository schoolRepository;

    @CacheEvict(value = "schools", allEntries = true)
    public ApiResponse<SchoolResponse> createSchool(SchoolRequest request, String createdBy) {
        log.info("[SCHOOL] Tạo trường học mới: {}", request.getName());
        // Validate unique constraints
        if (schoolRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "School code already exists");
        }

        if (schoolRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "School name already exists");
        }

        School school = new School();
        school.setCode(request.getCode());
        school.setName(request.getName());
        school.setDescription(request.getDescription());
        school.setAddress(request.getAddress());
        school.setPhone(request.getPhone());
        school.setEmail(request.getEmail());
        school.setWebsite(request.getWebsite());
        school.setContactPerson(request.getContactPerson());
        school.setContactPhone(request.getContactPhone());
        school.setContactEmail(request.getContactEmail());
        school.setDefaultTrainingPoints(request.getDefaultTrainingPoints());
        school.setDefaultSocialPoints(request.getDefaultSocialPoints());
        school.setDefaultPenaltyPoints(request.getDefaultPenaltyPoints());
        school.setBillingAddress(request.getBillingAddress());
        school.setTaxId(request.getTaxId());
        school.setPaymentMethod(request.getPaymentMethod());
        // Convert Map<String, String> to Map<String, Object>
        if (request.getCustomSettings() != null) {
            school.setCustomSettings(new java.util.HashMap<>(request.getCustomSettings()));
        }
        school.setCreatedBy(createdBy);
        school.setCreatedAt(new Date());

        School savedSchool = schoolRepository.save(school);
        return new ApiResponse<>(true, "School created successfully", toSchoolResponse(savedSchool));
    }

    @Cacheable(value = "schools", key = "#id")
    public ApiResponse<SchoolResponse> getSchoolById(String id) {
        log.info("[SCHOOL] Lấy thông tin trường học id: {}", id);
        Optional<School> school = schoolRepository.findById(id);
        if (school.isEmpty()) {
            throw new AppException(ErrorCode.SCHOOL_NOT_FOUND, "School not found");
        }
        return new ApiResponse<>(true, "Success", toSchoolResponse(school.get()));
    }

    @Cacheable(value = "schools", key = "'all'")
    public ApiResponse<List<SchoolResponse>> getAllSchools() {
        List<School> schools = schoolRepository.findAll();
        List<SchoolResponse> responses = schools.stream()
            .map(this::toSchoolResponse)
            .toList();
        return new ApiResponse<>(true, "Success", responses);
    }

    @Cacheable(value = "schools", key = "'active'")
    public ApiResponse<List<SchoolResponse>> getActiveSchools() {
        List<School> schools = schoolRepository.findActiveSchools(new Date());
        List<SchoolResponse> responses = schools.stream()
            .map(this::toSchoolResponse)
            .toList();
        return new ApiResponse<>(true, "Success", responses);
    }

    @CacheEvict(value = "schools", allEntries = true)
    public ApiResponse<SchoolResponse> updateSchoolStatus(String id, School.SchoolStatus status, String updatedBy) {
        Optional<School> schoolOpt = schoolRepository.findById(id);
        if (schoolOpt.isEmpty()) {
            throw new AppException(ErrorCode.SCHOOL_NOT_FOUND, "School not found");
        }

        School school = schoolOpt.get();
        school.setStatus(status);
        school.setUpdatedBy(updatedBy);
        school.setUpdatedAt(new Date());

        School savedSchool = schoolRepository.save(school);
        return new ApiResponse<>(true, "School status updated successfully", toSchoolResponse(savedSchool));
    }

    @CacheEvict(value = "schools", allEntries = true)
    public ApiResponse<SchoolResponse> updateSchool(String id, SchoolRequest request) {
        Optional<School> schoolOpt = schoolRepository.findById(id);
        if (schoolOpt.isEmpty()) {
            throw new AppException(ErrorCode.SCHOOL_NOT_FOUND, "School not found");
        }
        School school = schoolOpt.get();
        school.setCode(request.getCode());
        school.setName(request.getName());
        school.setDescription(request.getDescription());
        school.setAddress(request.getAddress());
        school.setPhone(request.getPhone());
        school.setEmail(request.getEmail());
        school.setWebsite(request.getWebsite());
        school.setContactPerson(request.getContactPerson());
        school.setContactPhone(request.getContactPhone());
        school.setContactEmail(request.getContactEmail());
        if (request.getDefaultTrainingPoints() != null) school.setDefaultTrainingPoints(request.getDefaultTrainingPoints());
        if (request.getDefaultSocialPoints() != null) school.setDefaultSocialPoints(request.getDefaultSocialPoints());
        if (request.getDefaultPenaltyPoints() != null) school.setDefaultPenaltyPoints(request.getDefaultPenaltyPoints());
        if (request.getCustomSettings() != null) school.setCustomSettings(new java.util.HashMap<>(request.getCustomSettings()));
        school.setUpdatedAt(new Date());
        School savedSchool = schoolRepository.save(school);
        return new ApiResponse<>(true, "School updated successfully", toSchoolResponse(savedSchool));
    }

    public ApiResponse<List<SchoolResponse>> getExpiredSubscriptions() {
        List<School> schools = schoolRepository.findExpiredSubscriptions(new Date());
        List<SchoolResponse> responses = schools.stream()
            .map(this::toSchoolResponse)
            .toList();
        return new ApiResponse<>(true, "Success", responses);
    }



    private SchoolResponse toSchoolResponse(School school) {
        SchoolResponse response = new SchoolResponse();
        response.setId(school.getId());
        response.setCode(school.getCode());
        response.setName(school.getName());
        response.setDescription(school.getDescription());
        response.setAddress(school.getAddress());
        response.setPhone(school.getPhone());
        response.setEmail(school.getEmail());
        response.setWebsite(school.getWebsite());
        response.setContactPerson(school.getContactPerson());
        response.setContactPhone(school.getContactPhone());
        response.setContactEmail(school.getContactEmail());
        response.setStatus(school.getStatus());



        response.setDefaultTrainingPointsPerHour(school.getDefaultTrainingPoints());
        response.setDefaultSocialPointsPerHour(school.getDefaultSocialPoints());
        response.setDefaultPenaltyPoints(school.getDefaultPenaltyPoints());
        // Convert Map<String, Object> to Map<String, String>
        if (school.getCustomSettings() != null) {
            Map<String, String> stringMap = new java.util.HashMap<>();
            school.getCustomSettings().forEach((key, value) ->
                stringMap.put(key, value != null ? value.toString() : null));
            response.setCustomSettings(stringMap);
        }
        response.setCreatedAt(school.getCreatedAt());
        response.setUpdatedAt(school.getUpdatedAt());
        response.setCreatedBy(school.getCreatedBy());
        response.setUpdatedBy(school.getUpdatedBy());
        return response;
    }
}
