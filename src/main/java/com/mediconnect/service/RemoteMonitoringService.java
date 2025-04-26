package com.mediconnect.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.DeviceReadingDTO;
import com.mediconnect.exception.MonitoringException;
import com.mediconnect.model.AlertRule;
import com.mediconnect.model.DeviceReading;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.model.MonitoringDevice;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.AlertRuleRepository;
import com.mediconnect.repository.DeviceReadingRepository;
import com.mediconnect.repository.MedicalRecordRepository;
import com.mediconnect.repository.MonitoringDeviceRepository;
import com.mediconnect.repository.PatientRepository;

@Service
public class RemoteMonitoringService {

    @Autowired
    private MonitoringDeviceRepository deviceRepository;
    
    @Autowired
    private DeviceReadingRepository readingRepository;
    
    @Autowired
    private AlertRuleRepository alertRuleRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Register a new monitoring device for a patient
     */
    @Transactional
    public MonitoringDevice registerDevice(Long patientId, String deviceType, String deviceId, String deviceName) {
        // Check if patient exists
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new MonitoringException("Patient not found"));
        
        // Check if device already registered
        if (deviceRepository.findByDeviceId(deviceId).isPresent()) {
            throw new MonitoringException("Device already registered");
        }
        
        // Create new device
        MonitoringDevice device = new MonitoringDevice();
        device.setPatient(patient);
        device.setDeviceType(deviceType);
        device.setDeviceId(deviceId);
        device.setDeviceName(deviceName);
        device.setActive(true);
        device.setRegisteredAt(LocalDateTime.now());
        
        return deviceRepository.save(device);
    }
    
    /**
     * Process readings from a monitoring device
     */
    @Transactional
    public DeviceReading processReading(DeviceReadingDTO readingDTO) {
        // Validate device
        MonitoringDevice device = deviceRepository.findByDeviceId(readingDTO.getDeviceId())
                .orElseThrow(() -> new MonitoringException("Device not registered"));
        
        if (!device.isActive()) {
            throw new MonitoringException("Device is inactive");
        }
        
        // Create reading record
        DeviceReading reading = new DeviceReading();
        reading.setDevice(device);
        reading.setReadingType(readingDTO.getReadingType());
        reading.setValue(readingDTO.getValue());
        reading.setUnit(readingDTO.getUnit());
        reading.setTimestamp(readingDTO.getTimestamp() != null ? 
                readingDTO.getTimestamp() : LocalDateTime.now());
        reading.setMetadata(readingDTO.getMetadata());
        
        DeviceReading savedReading = readingRepository.save(reading);
        
        // Check alert rules
        checkAlertRules(savedReading);
        
        // Update patient vital signs if appropriate
        updatePatientVitals(device.getPatient().getId(), readingDTO);
        
        return savedReading;
    }
    
    /**
     * Check if reading triggers any alert rules
     */
    private void checkAlertRules(DeviceReading reading) {
        List<AlertRule> rules = alertRuleRepository.findByPatientIdAndReadingType(
                reading.getDevice().getPatient().getId(), 
                reading.getReadingType());
        
        for (AlertRule rule : rules) {
            boolean isTriggered = false;
            
            switch (rule.getConditionType()) {
                case "ABOVE":
                    isTriggered = reading.getValue() > rule.getThresholdValue();
                    break;
                case "BELOW":
                    isTriggered = reading.getValue() < rule.getThresholdValue();
                    break;
                case "EQUAL":
                    isTriggered = Math.abs(reading.getValue() - rule.getThresholdValue()) < 0.0001;
                    break;
                case "CHANGE":
                    // Get previous reading
                    DeviceReading previousReading = readingRepository
                            .findTopByDeviceAndReadingTypeOrderByTimestampDesc(
                                    reading.getDevice(), reading.getReadingType())
                            .orElse(null);
                    
                    if (previousReading != null) {
                        double change = Math.abs((reading.getValue() - previousReading.getValue()) / 
                                previousReading.getValue());
                        isTriggered = change > rule.getThresholdValue();
                    }
                    break;
            }
            
            if (isTriggered) {
                // Send alert
                Map<String, Object> alertData = new HashMap<>();
                alertData.put("patientId", reading.getDevice().getPatient().getId());
                alertData.put("readingType", reading.getReadingType());
                alertData.put("value", reading.getValue());
                alertData.put("unit", reading.getUnit());
                alertData.put("timestamp", reading.getTimestamp());
                alertData.put("ruleName", rule.getRuleName());
                alertData.put("alertLevel", rule.getAlertLevel());
                
                notificationService.sendMonitoringAlert(alertData);
                
                // Log alert
                rule.setLastTriggeredAt(LocalDateTime.now());
                rule.setTriggerCount(rule.getTriggerCount() + 1);
                alertRuleRepository.save(rule);
            }
        }
    }
    
    /**
     * Update patient vital signs based on device readings
     */
    private void updatePatientVitals(Long patientId, DeviceReadingDTO reading) {
        // Get most recent medical record
        List<MedicalRecord> records = medicalRecordRepository.findLatestRecordsByPatientId(patientId);
        
        if (records.isEmpty()) {
            return; // No medical record to update
        }
        
        MedicalRecord record = records.get(0);
        boolean updated = false;
        
        // Update appropriate vital sign based on reading type
        switch (reading.getReadingType().toUpperCase()) {
            case "HEART_RATE":
                record.setHeartRate((int) Math.round(reading.getValue()));
                updated = true;
                break;
            case "BLOOD_PRESSURE":
                if (reading.getMetadata() != null) {
                    String systolic = reading.getMetadata().get("systolic");
                    String diastolic = reading.getMetadata().get("diastolic");
                    if (systolic != null && diastolic != null) {
                        record.setBloodPressure(systolic + "/" + diastolic);
                        updated = true;
                    }
                }
                break;
            case "TEMPERATURE":
                record.setTemperature(reading.getValue());
                updated = true;
                break;
            case "SPO2":
            case "OXYGEN_SATURATION":
                record.setOxygenSaturation(reading.getValue());
                updated = true;
                break;
        }
        
        if (updated) {
            medicalRecordRepository.save(record);
        }
    }
    
    /**
     * Create alert rule for a patient
     */
    @Transactional
    public AlertRule createAlertRule(Long patientId, String readingType, String conditionType, 
                                   double thresholdValue, String alertLevel) {
        // Check if patient exists
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new MonitoringException("Patient not found"));
        
        // Create alert rule
        AlertRule rule = new AlertRule();
        rule.setPatient(patient);
        rule.setReadingType(readingType);
        rule.setRuleName(readingType + " " + conditionType + " " + thresholdValue);
        rule.setConditionType(conditionType);
        rule.setThresholdValue(thresholdValue);
        rule.setAlertLevel(alertLevel);
        rule.setTriggerCount(0);
        rule.setCreatedAt(LocalDateTime.now());
        
        return alertRuleRepository.save(rule);
    }
    
    /**
     * Get readings for a patient within a date range
     */
    public List<DeviceReading> getPatientReadings(Long patientId, String readingType, 
                                            LocalDateTime startDate, LocalDateTime endDate) {
        return readingRepository.findByPatientIdAndReadingTypeAndDateRange(
                patientId, readingType, startDate, endDate);
    }
    
    /**
     * Analyze device readings for trends
     */
    public Map<String, Object> analyzeReadingTrends(Long patientId, String readingType, int days) {
        // Get readings for the specified period
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<DeviceReading> readings = readingRepository.findByPatientIdAndReadingTypeAndDateRange(
                patientId, readingType, startDate, endDate);
        
        Map<String, Object> analysis = new HashMap<>();
        
        if (readings.isEmpty()) {
            analysis.put("status", "insufficient_data");
            return analysis;
        }
        
        // Calculate basic statistics
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (DeviceReading reading : readings) {
            sum += reading.getValue();
            min = Math.min(min, reading.getValue());
            max = Math.max(max, reading.getValue());
        }
        
        double average = sum / readings.size();
        
        // Calculate trend (simple linear regression)
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;
        
        // Convert timestamps to hours from start
        long startTimeMillis = readings.get(0).getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli();
        
        for (int i = 0; i < readings.size(); i++) {
            DeviceReading reading = readings.get(i);
            double x = (reading.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli() - startTimeMillis) / 
                      (1000.0 * 60 * 60); // hours
            double y = reading.getValue();
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        
        double n = readings.size();
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        
        // Determine trend direction
        String trendDirection = "stable";
        if (slope > 0.01) {
            trendDirection = "increasing";
        } else if (slope < -0.01) {
            trendDirection = "decreasing";
        }
        
        // Prepare analysis result
        analysis.put("status", "completed");
        analysis.put("readingType", readingType);
        analysis.put("count", readings.size());
        analysis.put("average", average);
        analysis.put("min", min);
        analysis.put("max", max);
        analysis.put("trend", trendDirection);
        analysis.put("slope", slope);
        analysis.put("unit", readings.get(0).getUnit());
        
        // Add interpretation based on reading type
        addInterpretation(analysis, readingType);
        
        return analysis;
    }
    
    /**
     * Add interpretation of readings based on type
     */
    private void addInterpretation(Map<String, Object> analysis, String readingType) {
        String interpretation = "";
        boolean isNormal = true;
        
        switch (readingType.toUpperCase()) {
            case "HEART_RATE":
                double hr = (double) analysis.get("average");
                if (hr < 60) {
                    interpretation = "Heart rate is below normal range (60-100 bpm). This may indicate bradycardia.";
                    isNormal = false;
                } else if (hr > 100) {
                    interpretation = "Heart rate is above normal range (60-100 bpm). This may indicate tachycardia.";
                    isNormal = false;
                } else {
                    interpretation = "Heart rate is within normal range (60-100 bpm).";
                }
                break;
                
            case "BLOOD_PRESSURE":
                // Assuming we store systolic/diastolic in metadata
                String trend = (String) analysis.get("trend");
                interpretation = "Blood pressure trend is " + trend + ". ";
                if ("increasing".equals(trend)) {
                    interpretation += "Monitor closely as increasing blood pressure may indicate hypertension.";
                    isNormal = false;
                }
                break;
                
            case "TEMPERATURE":
                double temp = (double) analysis.get("average");
                if (temp < 36.1) {
                    interpretation = "Temperature is below normal range (36.1-37.2°C). This may indicate hypothermia.";
                    isNormal = false;
                } else if (temp > 37.2) {
                    interpretation = "Temperature is above normal range (36.1-37.2°C). This may indicate fever.";
                    isNormal = false;
                } else {
                    interpretation = "Temperature is within normal range (36.1-37.2°C).";
                }
                break;
                
            case "SPO2":
            case "OXYGEN_SATURATION":
                double spo2 = (double) analysis.get("average");
                if (spo2 < 95) {
                    interpretation = "Oxygen saturation is below normal range (95-100%). This may indicate hypoxemia.";
                    isNormal = false;
                } else {
                    interpretation = "Oxygen saturation is within normal range (95-100%).";
                }
                break;
        }
        
        analysis.put("interpretation", interpretation);
        analysis.put("isNormal", isNormal);
    }
}