package com.mediconnect.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LabTestBookingDTO {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long testId;

    @NotBlank(message = "Test name is required")
    private String testName;

    private String testDescription;

    @NotNull(message = "Test price is required")
    @DecimalMin(value = "0.0", message = "Test price must be positive")
    private BigDecimal testPrice;

    @NotNull(message = "Registration fee is required")
    @DecimalMin(value = "0.0", message = "Registration fee must be positive")
    private BigDecimal registrationFee;

    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.0", message = "Tax amount must be positive")
    private BigDecimal taxAmount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount must be positive")
    private BigDecimal totalAmount;

    private String sampleType;

    private String processingTime;

    private Boolean homeCollection = false;

    private LocalDateTime scheduledDate;

    private String patientAddress;

    private String patientPhone;

    private String patientEmail;

    private String notes;

    private String bookingType = "LAB_TEST";

    // Additional fields for better data handling
    private String patientName;
    private String specialInstructions;
    private String preferredDate;
    private String preferredTime;
    private String collectionType;
    private String address;
    private String status = "PENDING";

    // Nested class for fee details (optional, for frontend convenience)
    public static class FeeDetails {
        private BigDecimal testPrice;
        private BigDecimal registrationFee;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;

        // Getters and setters
        public BigDecimal getTestPrice() { return testPrice; }
        public void setTestPrice(BigDecimal testPrice) { this.testPrice = testPrice; }

        public BigDecimal getRegistrationFee() { return registrationFee; }
        public void setRegistrationFee(BigDecimal registrationFee) { this.registrationFee = registrationFee; }

        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }

    private FeeDetails fees;

    // Getters and setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public BigDecimal getTestPrice() {
        return testPrice;
    }

    public void setTestPrice(BigDecimal testPrice) {
        this.testPrice = testPrice;
    }

    public BigDecimal getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(BigDecimal registrationFee) {
        this.registrationFee = registrationFee;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }

    public Boolean getHomeCollection() {
        return homeCollection;
    }

    public void setHomeCollection(Boolean homeCollection) {
        this.homeCollection = homeCollection;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public FeeDetails getFees() {
        return fees;
    }

    public void setFees(FeeDetails fees) {
        this.fees = fees;
    }

    // Additional getters and setters
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}