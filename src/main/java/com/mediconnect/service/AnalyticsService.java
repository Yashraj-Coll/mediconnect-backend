package com.mediconnect.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediconnect.dto.analytics.*;
import com.mediconnect.model.*;
import com.mediconnect.repository.*;

@Service
public class AnalyticsService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    public AppointmentRepository getAppointmentRepository() {
		return appointmentRepository;
	}

	public void setAppointmentRepository(AppointmentRepository appointmentRepository) {
		this.appointmentRepository = appointmentRepository;
	}

	public DoctorRepository getDoctorRepository() {
		return doctorRepository;
	}

	public void setDoctorRepository(DoctorRepository doctorRepository) {
		this.doctorRepository = doctorRepository;
	}

	public PatientRepository getPatientRepository() {
		return patientRepository;
	}

	public void setPatientRepository(PatientRepository patientRepository) {
		this.patientRepository = patientRepository;
	}

	public MedicalRecordRepository getMedicalRecordRepository() {
		return medicalRecordRepository;
	}

	public void setMedicalRecordRepository(MedicalRecordRepository medicalRecordRepository) {
		this.medicalRecordRepository = medicalRecordRepository;
	}

	@Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    /**
     * Get patient trends data
     */
    public PatientTrendsDTO getPatientTrends(Long patientId, LocalDate startDate, LocalDate endDate) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdAndDateRange(
                patientId, 
                startDate.atStartOfDay(), 
                endDate.plusDays(1).atStartOfDay());
        
        PatientTrendsDTO trends = new PatientTrendsDTO();
        trends.setPatientId(patientId);
        
        // Extract vital signs trends
        Map<LocalDate, Double> temperatureData = new HashMap<>();
        Map<LocalDate, Integer> heartRateData = new HashMap<>();
        Map<LocalDate, String> bloodPressureData = new HashMap<>();
        Map<LocalDate, Double> oxygenSaturationData = new HashMap<>();
        
        for (MedicalRecord record : records) {
            LocalDate recordDate = record.getRecordDate().toLocalDate();
            
            if (record.getTemperature() != null) {
                temperatureData.put(recordDate, record.getTemperature());
            }
            
            if (record.getHeartRate() != null) {
                heartRateData.put(recordDate, record.getHeartRate());
            }
            
            if (record.getBloodPressure() != null) {
                bloodPressureData.put(recordDate, record.getBloodPressure());
            }
            
            if (record.getOxygenSaturation() != null) {
                oxygenSaturationData.put(recordDate, record.getOxygenSaturation());
            }
        }
        
        trends.setTemperatureData(temperatureData);
        trends.setHeartRateData(heartRateData);
        trends.setBloodPressureData(bloodPressureData);
        trends.setOxygenSaturationData(oxygenSaturationData);
        
        // Add BMI trend if height and weight available
        if (patient.getHeight() != null && patient.getWeight() != null) {
            trends.setCurrentBMI(calculateBMI(patient.getHeight(), patient.getWeight()));
        }
        
        return trends;
    }
    
    /**
     * Get doctor performance metrics
     */
    public DoctorPerformanceDTO getDoctorPerformance(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDateRange(
                doctorId, startDateTime, endDateTime);
        
        DoctorPerformanceDTO performance = new DoctorPerformanceDTO();
        performance.setDoctorId(doctorId);
        performance.setTotalAppointments(appointments.size());
        
        // Calculate appointment status counts
        Map<Appointment.AppointmentStatus, Long> statusCounts = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
        
        performance.setCompletedAppointments(statusCounts.getOrDefault(Appointment.AppointmentStatus.COMPLETED, 0L));
        performance.setCancelledAppointments(statusCounts.getOrDefault(Appointment.AppointmentStatus.CANCELLED, 0L));
        performance.setNoShowAppointments(statusCounts.getOrDefault(Appointment.AppointmentStatus.NO_SHOW, 0L));
        
        // Calculate appointment type distribution
        Map<Appointment.AppointmentType, Long> typeCounts = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getAppointmentType, Collectors.counting()));
        
        performance.setAppointmentTypeDistribution(typeCounts);
        
        // Calculate average appointment duration
        double avgDuration = appointments.stream()
                .filter(a -> a.getDurationMinutes() != null)
                .mapToInt(Appointment::getDurationMinutes)
                .average()
                .orElse(0);
        
        performance.setAverageAppointmentDuration(avgDuration);
        
        return performance;
    }
    
    /**
     * Get hospital resource utilization metrics
     */
    public ResourceUtilizationDTO getResourceUtilization(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateTimeBetween(
                startDateTime, endDateTime);
        
        ResourceUtilizationDTO utilization = new ResourceUtilizationDTO();
        
        // Calculate total appointment hours
        double totalHours = appointments.stream()
                .filter(a -> a.getDurationMinutes() != null)
                .mapToDouble(a -> a.getDurationMinutes() / 60.0)
                .sum();
        
        utilization.setTotalAppointmentHours(totalHours);
        
        // Calculate appointment distribution by hour of day
        Map<Integer, Long> hourDistribution = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentDateTime().getHour(),
                        Collectors.counting()));
        
        utilization.setAppointmentsByHourOfDay(hourDistribution);
        
        // Calculate appointment distribution by day of week
        Map<Integer, Long> dayDistribution = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentDateTime().getDayOfWeek().getValue(),
                        Collectors.counting()));
        
        utilization.setAppointmentsByDayOfWeek(dayDistribution);
        
        return utilization;
    }
    
    /**
     * Calculate BMI (Body Mass Index)
     */
    private double calculateBMI(Double heightCm, Double weightKg) {
        if (heightCm == null || weightKg == null || heightCm == 0) {
            return 0;
        }
        
        // Convert height from cm to meters
        double heightM = heightCm / 100.0;
        
        // Calculate BMI: weight (kg) / (height (m))²
        return Math.round((weightKg / (heightM * heightM)) * 10) / 10.0;
    }
}