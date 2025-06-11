package com.mediconnect.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.PrescriptionDTO;
import com.mediconnect.dto.PrescriptionItemDTO;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Prescription;
import com.mediconnect.model.PrescriptionItem;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.PrescriptionItemRepository;
import com.mediconnect.repository.PrescriptionRepository;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionItemRepository prescriptionItemRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReminderService reminderService;

    // ===== DTO MAPPING =====
    public static PrescriptionDTO mapToDTO(Prescription p) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(p.getId());
        if (p.getDoctor() != null) {
            dto.setDoctorId(p.getDoctor().getId());
            dto.setDoctorName(
                p.getDoctor().getUser() != null ?
                p.getDoctor().getUser().getFirstName() + " " + p.getDoctor().getUser().getLastName()
                : null);
        }
        if (p.getPatient() != null) {
            dto.setPatientId(p.getPatient().getId());
            dto.setPatientName(
                p.getPatient().getUser() != null ?
                p.getPatient().getUser().getFirstName() + " " + p.getPatient().getUser().getLastName()
                : null);
        }
        dto.setAppointmentId(p.getAppointment() != null ? p.getAppointment().getId() : null);
        dto.setPrescriptionDate(p.getPrescriptionDate());
        dto.setValidUntil(p.getValidUntil());
        dto.setIsRefillable(p.getIsRefillable());
        dto.setRefillCount(p.getRefillCount());
        dto.setSpecialInstructions(p.getSpecialInstructions());
        dto.setNotes(p.getNotes());
        dto.setIsDigitallySigned(p.getIsDigitallySigned());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        // Items
        if (p.getPrescriptionItems() != null) {
            dto.setPrescriptionItems(
                p.getPrescriptionItems().stream().map(item -> {
                    PrescriptionItemDTO itemDto = new PrescriptionItemDTO();
                    itemDto.setId(item.getId());
                    itemDto.setMedicationName(item.getMedicationName());
                    itemDto.setDosage(item.getDosage());
                    itemDto.setFrequency(item.getFrequency());
                    itemDto.setDuration(item.getDuration());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setRoute(item.getRoute());
                    itemDto.setInstructions(item.getInstructions());
                    itemDto.setBeforeMeal(item.getBeforeMeal());
                    return itemDto;
                }).collect(Collectors.toList())
            );
        }
        return dto;
    }

    // ========== DTO returning methods ==========

    public List<PrescriptionDTO> getAllPrescriptionDTOs() {
        return prescriptionRepository.findAll()
                .stream().map(PrescriptionService::mapToDTO)
                .collect(Collectors.toList());
    }

    public PrescriptionDTO getPrescriptionDTOById(Long id) {
        return mapToDTO(getPrescriptionById(id));
    }

    public List<PrescriptionDTO> getPrescriptionDTOsByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId)
                .stream().map(PrescriptionService::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PrescriptionDTO> getActivePrescriptionDTOsForPatient(Long patientId) {
        LocalDateTime now = LocalDateTime.now();
        return prescriptionRepository.findByPatientIdAndValidUntilAfter(patientId, now)
                .stream().map(PrescriptionService::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PrescriptionDTO> getPrescriptionDTOsByDoctorId(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByPrescriptionDateDesc(doctorId)
                .stream().map(PrescriptionService::mapToDTO)
                .collect(Collectors.toList());
    }

    public PrescriptionDTO getPrescriptionDTOByAppointmentId(Long appointmentId) {
        return mapToDTO(getPrescriptionByAppointmentId(appointmentId));
    }

    // ========== Entity methods (POST/PUT/DELETE will use these) ==========

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
    }

    public List<Prescription> getPrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId);
    }

    public List<Prescription> getActivePrescriptionsForPatient(Long patientId) {
        LocalDateTime now = LocalDateTime.now();
        return prescriptionRepository.findByPatientIdAndValidUntilAfter(patientId, now);
    }

    public List<Prescription> getPrescriptionsByDoctorId(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByPrescriptionDateDesc(doctorId);
    }

    public Prescription getPrescriptionByAppointmentId(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found for appointment with id: " + appointmentId));
    }

    @Transactional
    public Prescription createPrescription(PrescriptionDTO prescriptionDTO) {
        Doctor doctor = doctorRepository.findById(prescriptionDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + prescriptionDTO.getDoctorId()));

        Patient patient = patientRepository.findById(prescriptionDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + prescriptionDTO.getPatientId()));

        Appointment appointment = null;
        if (prescriptionDTO.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(prescriptionDTO.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + prescriptionDTO.getAppointmentId()));
        }

        Prescription prescription = new Prescription();
        prescription.setDoctor(doctor);
        prescription.setPatient(patient);
        prescription.setAppointment(appointment);
        prescription.setPrescriptionDate(prescriptionDTO.getPrescriptionDate() != null ?
                prescriptionDTO.getPrescriptionDate() : LocalDateTime.now());
        prescription.setValidUntil(prescriptionDTO.getValidUntil());
        prescription.setIsRefillable(prescriptionDTO.getIsRefillable() != null ?
                prescriptionDTO.getIsRefillable() : false);
        prescription.setRefillCount(prescriptionDTO.getRefillCount());
        prescription.setSpecialInstructions(prescriptionDTO.getSpecialInstructions());
        prescription.setNotes(prescriptionDTO.getNotes());
        prescription.setIsDigitallySigned(prescriptionDTO.getIsDigitallySigned() != null ?
                prescriptionDTO.getIsDigitallySigned() : false);

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        if (prescriptionDTO.getPrescriptionItems() != null && !prescriptionDTO.getPrescriptionItems().isEmpty()) {
            for (PrescriptionItemDTO itemDTO : prescriptionDTO.getPrescriptionItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setPrescription(savedPrescription);
                item.setMedicationName(itemDTO.getMedicationName());
                item.setDosage(itemDTO.getDosage());
                item.setFrequency(itemDTO.getFrequency());
                item.setDuration(itemDTO.getDuration());
                item.setQuantity(itemDTO.getQuantity());
                item.setRoute(itemDTO.getRoute());
                item.setInstructions(itemDTO.getInstructions());
                item.setBeforeMeal(itemDTO.getBeforeMeal() != null ? itemDTO.getBeforeMeal() : false);

                prescriptionItemRepository.save(item);
            }
        }

        try {
            Patient p = savedPrescription.getPatient();
            String prescriptionUrl = "https://mediconnect.com/prescriptions/" + savedPrescription.getId();
            notificationService.sendPrescriptionNotificationToPatient(p, prescriptionUrl);
        } catch (Exception e) {
            System.err.println("Error sending prescription notification: " + e.getMessage());
        }

        return savedPrescription;
    }

    @Transactional
    public Prescription updatePrescription(Long id, PrescriptionDTO prescriptionDTO) {
        Prescription prescription = getPrescriptionById(id);

        if (prescriptionDTO.getValidUntil() != null) {
            prescription.setValidUntil(prescriptionDTO.getValidUntil());
        }

        if (prescriptionDTO.getIsRefillable() != null) {
            prescription.setIsRefillable(prescriptionDTO.getIsRefillable());
        }

        if (prescriptionDTO.getRefillCount() != null) {
            prescription.setRefillCount(prescriptionDTO.getRefillCount());
        }

        if (prescriptionDTO.getSpecialInstructions() != null) {
            prescription.setSpecialInstructions(prescriptionDTO.getSpecialInstructions());
        }

        if (prescriptionDTO.getNotes() != null) {
            prescription.setNotes(prescriptionDTO.getNotes());
        }

        if (prescriptionDTO.getIsDigitallySigned() != null) {
            prescription.setIsDigitallySigned(prescriptionDTO.getIsDigitallySigned());
        }

        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public void deletePrescription(Long id) {
        Prescription prescription = getPrescriptionById(id);
        prescriptionItemRepository.deleteByPrescriptionId(id);
        prescriptionRepository.delete(prescription);
    }

    @Transactional
    public PrescriptionItem addPrescriptionItem(Long prescriptionId, PrescriptionItemDTO itemDTO) {
        Prescription prescription = getPrescriptionById(prescriptionId);

        PrescriptionItem item = new PrescriptionItem();
        item.setPrescription(prescription);
        item.setMedicationName(itemDTO.getMedicationName());
        item.setDosage(itemDTO.getDosage());
        item.setFrequency(itemDTO.getFrequency());
        item.setDuration(itemDTO.getDuration());
        item.setQuantity(itemDTO.getQuantity());
        item.setRoute(itemDTO.getRoute());
        item.setInstructions(itemDTO.getInstructions());
        item.setBeforeMeal(itemDTO.getBeforeMeal() != null ? itemDTO.getBeforeMeal() : false);

        return prescriptionItemRepository.save(item);
    }

    @Transactional
    public PrescriptionItem updatePrescriptionItem(Long itemId, PrescriptionItemDTO itemDTO) {
        PrescriptionItem item = prescriptionItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription item not found with id: " + itemId));

        if (itemDTO.getMedicationName() != null) item.setMedicationName(itemDTO.getMedicationName());
        if (itemDTO.getDosage() != null) item.setDosage(itemDTO.getDosage());
        if (itemDTO.getFrequency() != null) item.setFrequency(itemDTO.getFrequency());
        if (itemDTO.getDuration() != null) item.setDuration(itemDTO.getDuration());
        if (itemDTO.getQuantity() != null) item.setQuantity(itemDTO.getQuantity());
        if (itemDTO.getRoute() != null) item.setRoute(itemDTO.getRoute());
        if (itemDTO.getInstructions() != null) item.setInstructions(itemDTO.getInstructions());
        if (itemDTO.getBeforeMeal() != null) item.setBeforeMeal(itemDTO.getBeforeMeal());

        return prescriptionItemRepository.save(item);
    }

    @Transactional
    public void deletePrescriptionItem(Long itemId) {
        PrescriptionItem item = prescriptionItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription item not found with id: " + itemId));
        prescriptionItemRepository.delete(item);
    }

    public List<PrescriptionItem> getPrescriptionItems(Long prescriptionId) {
        return prescriptionItemRepository.findByPrescriptionId(prescriptionId);
    }

    @Transactional
    public Prescription processPrescriptionRefill(Long prescriptionId) {
        Prescription prescription = getPrescriptionById(prescriptionId);

        if (!prescription.getIsRefillable()) {
            throw new BadRequestException("This prescription is not refillable");
        }
        if (prescription.getRefillCount() <= 0) {
            throw new BadRequestException("No refills remaining for this prescription");
        }
        if (prescription.getValidUntil() != null && prescription.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This prescription has expired and cannot be refilled");
        }

        prescription.setRefillCount(prescription.getRefillCount() - 1);

        if (prescription.getValidUntil() != null) {
            List<PrescriptionItem> items = prescriptionItemRepository.findByPrescriptionId(prescriptionId);
            if (!items.isEmpty()) {
                PrescriptionItem firstItem = items.get(0);
                String duration = firstItem.getDuration();
                if (duration != null && !duration.isEmpty()) {
                    try {
                        int daysToAdd = parseDurationToDays(duration);
                        LocalDateTime newValidUntil = LocalDateTime.now().plusDays(daysToAdd);
                        if (prescription.getValidUntil() == null || newValidUntil.isAfter(prescription.getValidUntil())) {
                            prescription.setValidUntil(newValidUntil);
                        }
                    } catch (Exception e) {
                        prescription.setValidUntil(LocalDateTime.now().plusDays(30));
                    }
                } else {
                    prescription.setValidUntil(LocalDateTime.now().plusDays(30));
                }
            } else {
                prescription.setValidUntil(LocalDateTime.now().plusDays(30));
            }
        }

        Prescription updatedPrescription = prescriptionRepository.save(prescription);

        try {
            Patient p = updatedPrescription.getPatient();
            String prescriptionUrl = "https://mediconnect.com/prescriptions/" + updatedPrescription.getId();
            notificationService.sendPrescriptionNotificationToPatient(p, prescriptionUrl);
        } catch (Exception e) {
            System.err.println("Error sending refill notification: " + e.getMessage());
        }

        return updatedPrescription;
    }

    @Transactional
    public Prescription signPrescription(Long prescriptionId) {
        Prescription prescription = getPrescriptionById(prescriptionId);
        prescription.setIsDigitallySigned(true);
        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public Prescription extendPrescriptionValidity(Long prescriptionId, int daysToExtend) {
        if (daysToExtend <= 0) {
            throw new BadRequestException("Days to extend must be greater than 0");
        }

        Prescription prescription = getPrescriptionById(prescriptionId);

        LocalDateTime newValidUntil;
        if (prescription.getValidUntil() != null && prescription.getValidUntil().isAfter(LocalDateTime.now())) {
            newValidUntil = prescription.getValidUntil().plusDays(daysToExtend);
        } else {
            newValidUntil = LocalDateTime.now().plusDays(daysToExtend);
        }

        prescription.setValidUntil(newValidUntil);
        return prescriptionRepository.save(prescription);
    }

    public int calculateRemainingDays(Long prescriptionId) {
        Prescription prescription = getPrescriptionById(prescriptionId);

        if (prescription.getValidUntil() == null) {
            return 9999;
        }
        LocalDateTime now = LocalDateTime.now();

        if (prescription.getValidUntil().isBefore(now)) {
            return 0;
        }

        return (int) ChronoUnit.DAYS.between(now, prescription.getValidUntil());
    }

    public boolean setPrescriptionReminder(Long prescriptionId) {
        Prescription prescription = getPrescriptionById(prescriptionId);
        Patient patient = prescription.getPatient();

        List<PrescriptionItem> items = prescriptionItemRepository.findByPrescriptionId(prescriptionId);

        for (PrescriptionItem item : items) {
            try {
                List<String> reminderTimes = parseFrequencyToReminderTimes(item.getFrequency(), item.getBeforeMeal());
                for (String reminderTime : reminderTimes) {
                    reminderService.scheduleReminderForMedication(
                            patient.getId(),
                            item.getId(),
                            item.getMedicationName(),
                            item.getDosage(),
                            reminderTime,
                            prescription.getValidUntil());
                }
            } catch (Exception e) {
                System.err.println("Error scheduling reminder for medication: " + item.getMedicationName() + ": " + e.getMessage());
            }
        }
        return true;
    }

    public boolean cancelPrescriptionReminder(Long prescriptionId) {
        List<PrescriptionItem> items = prescriptionItemRepository.findByPrescriptionId(prescriptionId);
        for (PrescriptionItem item : items) {
            reminderService.cancelReminderForMedication(item.getId());
        }
        return true;
    }

    // ===== Helper methods =====

    private int parseDurationToDays(String duration) {
        String normalized = duration.toLowerCase().trim();
        String[] parts = normalized.split("\\s+");
        if (parts.length < 2) return 30;
        try {
            int value = Integer.parseInt(parts[0]);
            String unit = parts[1];
            if (unit.startsWith("day")) return value;
            else if (unit.startsWith("week")) return value * 7;
            else if (unit.startsWith("month")) return value * 30;
            else if (unit.startsWith("year")) return value * 365;
            else return value;
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    private List<String> parseFrequencyToReminderTimes(String frequency, Boolean beforeMeal) {
        String normalized = frequency != null ? frequency.toLowerCase().trim() : "";
        String beforeMealTime = "30 minutes before meal";
        String afterMealTime = "with meal";
        String mealModifier = beforeMeal != null && beforeMeal ? beforeMealTime : afterMealTime;

        if (normalized.contains("once") && normalized.contains("day")) {
            if (normalized.contains("morning")) return List.of("08:00 AM, " + mealModifier);
            else if (normalized.contains("night") || normalized.contains("evening")) return List.of("08:00 PM, " + mealModifier);
            else return List.of("09:00 AM, " + mealModifier);
        } else if (normalized.contains("twice") && normalized.contains("day")) {
            return List.of("09:00 AM, " + mealModifier, "09:00 PM, " + mealModifier);
        } else if (normalized.contains("three") && normalized.contains("day")) {
            return List.of("09:00 AM, " + mealModifier, "02:00 PM, " + mealModifier, "09:00 PM, " + mealModifier);
        } else if (normalized.contains("four") && normalized.contains("day")) {
            return List.of("08:00 AM, " + mealModifier, "12:00 PM, " + mealModifier, "04:00 PM, " + mealModifier, "08:00 PM, " + mealModifier);
        } else if (normalized.contains("every") && normalized.contains("hour")) {
            return List.of("Every few hours as prescribed");
        } else if (normalized.contains("week")) {
            return List.of("Once a week on Monday, " + mealModifier);
        } else if (normalized.contains("month")) {
            return List.of("Once a month on the 1st, " + mealModifier);
        } else if (normalized.contains("as needed") || normalized.contains("prn")) {
            return List.of("As needed");
        } else {
            return List.of("As prescribed");
        }
    }
}
