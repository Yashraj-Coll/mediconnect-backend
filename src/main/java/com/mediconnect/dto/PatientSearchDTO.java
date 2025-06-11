package com.mediconnect.dto;

import com.mediconnect.model.Patient;  // Import Patient to access Patient.Gender

public class PatientSearchDTO {

    private String name;
    private String email;
    private Patient.Gender gender;  // Changed from Gender to Patient.Gender
    private String bloodGroup;
    private String condition;
    private String insuranceProvider;
    private String phoneNumber;
    private String insurancePolicyNumber;
    private Integer minAge;
    private Integer maxAge;
    private Double minWeight;
    private Double maxWeight;
    private Double minHeight;
    private Double maxHeight;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "ASC";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Patient.Gender getGender() { return gender; }  // Changed return type
    public void setGender(Patient.Gender gender) { this.gender = gender; }  // Changed parameter type

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }
    public void setInsurancePolicyNumber(String insurancePolicyNumber) { this.insurancePolicyNumber = insurancePolicyNumber; }

    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }

    public Integer getMaxAge() { return maxAge; }
    public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }

    public Double getMinWeight() { return minWeight; }
    public void setMinWeight(Double minWeight) { this.minWeight = minWeight; }

    public Double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(Double maxWeight) { this.maxWeight = maxWeight; }

    public Double getMinHeight() { return minHeight; }
    public void setMinHeight(Double minHeight) { this.minHeight = minHeight; }

    public Double getMaxHeight() { return maxHeight; }
    public void setMaxHeight(Double maxHeight) { this.maxHeight = maxHeight; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}