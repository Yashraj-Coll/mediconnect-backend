package com.mediconnect.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.mediconnect.exception.ReportGenerationException;
import com.mediconnect.model.AiDiagnosisResult;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Payment;
import com.mediconnect.model.Prescription;
import com.mediconnect.repository.AiDiagnosisResultRepository;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.MedicalRecordRepository;
import com.mediconnect.repository.PaymentRepository;
import com.mediconnect.repository.PrescriptionRepository;

@Service
public class ReportService {

    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private PrescriptionRepository prescriptionRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private AiDiagnosisResultRepository aiDiagnosisResultRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Generate patient medical history report
     */
    public byte[] generatePatientMedicalHistoryReport(Long patientId) {
        try {
            // Fetch patient's medical records
            List<MedicalRecord> records = medicalRecordRepository.findLatestRecordsByPatientId(patientId);
            
            if (records.isEmpty()) {
                throw new ReportGenerationException("No medical records found for patient");
            }
            
            Patient patient = records.get(0).getPatient();
            
            // Prepare data model for template
            Map<String, Object> variables = new HashMap<>();
            variables.put("patientName", patient.getUser().getFirstName() + " " + patient.getUser().getLastName());
            variables.put("patientId", patient.getId());
            variables.put("dateOfBirth", patient.getDateOfBirth().format(DATE_FORMATTER));
            variables.put("gender", patient.getGender());
            variables.put("bloodGroup", patient.getBloodGroup());
            variables.put("allergies", patient.getAllergies());
            variables.put("chronicDiseases", patient.getChronicDiseases());
            variables.put("medicalRecords", records);
            variables.put("generatedDate", LocalDateTime.now().format(DATE_TIME_FORMATTER));
            
            return generatePdfFromTemplate("patient-medical-history", variables);
        } catch (Exception e) {
            throw new ReportGenerationException("Failed to generate patient medical history report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate doctor appointment summary report
     */
    public byte[] generateDoctorAppointmentReport(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // Fetch doctor's appointments
            List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDateRange(doctorId, startDate, endDate);
            
            if (appointments.isEmpty()) {
                throw new ReportGenerationException("No appointments found for the specified period");
            }
            
            Doctor doctor = appointments.get(0).getDoctor();
            
            // Prepare data model for template
            Map<String, Object> variables = new HashMap<>();
            variables.put("doctorName", "Dr. " + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());
            variables.put("doctorId", doctor.getId());
            variables.put("specialization", doctor.getSpecialization());
            variables.put("startDate", startDate.format(DATE_FORMATTER));
            variables.put("endDate", endDate.format(DATE_FORMATTER));
            variables.put("appointments", appointments);
            variables.put("appointmentCount", appointments.size());
            variables.put("generatedDate", LocalDateTime.now().format(DATE_TIME_FORMATTER));
            
            // Calculate statistics
            long completedCount = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .count();
            long cancelledCount = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                    .count();
            long noShowCount = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.NO_SHOW)
                    .count();
            
            variables.put("completedCount", completedCount);
            variables.put("cancelledCount", cancelledCount);
            variables.put("noShowCount", noShowCount);
            
            return generatePdfFromTemplate("doctor-appointment-summary", variables);
        } catch (Exception e) {
            throw new ReportGenerationException("Failed to generate doctor appointment report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate prescription report
     */
    public byte[] generatePrescriptionReport(Long prescriptionId) {
        try {
            Prescription prescription = prescriptionRepository.findById(prescriptionId)
                    .orElseThrow(() -> new ReportGenerationException("Prescription not found"));
            
            // Prepare data model for template
            Map<String, Object> variables = new HashMap<>();
            variables.put("prescriptionId", prescription.getId());
            variables.put("patientName", prescription.getPatient().getUser().getFirstName() + " " + 
                    prescription.getPatient().getUser().getLastName());
            variables.put("doctorName", "Dr. " + prescription.getDoctor().getUser().getFirstName() + " " + 
                    prescription.getDoctor().getUser().getLastName());
            variables.put("prescriptionDate", prescription.getPrescriptionDate().format(DATE_FORMATTER));
            variables.put("validUntil", prescription.getValidUntil() != null ? 
                    prescription.getValidUntil().format(DATE_FORMATTER) : "Not specified");
            variables.put("isRefillable", prescription.isRefillable());
            variables.put("refillCount", prescription.getRefillCount());
            variables.put("specialInstructions", prescription.getSpecialInstructions());
            variables.put("medications", prescription.getPrescriptionItems());
            variables.put("isDigitallySigned", prescription.isDigitallySigned());
            variables.put("generatedDate", LocalDateTime.now().format(DATE_TIME_FORMATTER));
            
            return generatePdfFromTemplate("prescription-report", variables);
        } catch (Exception e) {
            throw new ReportGenerationException("Failed to generate prescription report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate AI diagnosis report
     */
    public byte[] generateAiDiagnosisReport(Long aiDiagnosisId) {
        try {
            AiDiagnosisResult diagnosis = aiDiagnosisResultRepository.findById(aiDiagnosisId)
                    .orElseThrow(() -> new ReportGenerationException("AI diagnosis result not found"));
            
            // Prepare data model for template
            Map<String, Object> variables = new HashMap<>();
            variables.put("diagnosisId", diagnosis.getId());
            variables.put("patientName", diagnosis.getMedicalRecord().getPatient().getUser().getFirstName() + " " + 
                    diagnosis.getMedicalRecord().getPatient().getUser().getLastName());
            variables.put("doctorName", "Dr. " + diagnosis.getMedicalRecord().getDoctor().getUser().getFirstName() + " " + 
                    diagnosis.getMedicalRecord().getDoctor().getUser().getLastName());
            variables.put("symptoms", diagnosis.getSymptoms());
            variables.put("analysisReport", diagnosis.getAnalysisReport());
            variables.put("recommendedTests", diagnosis.getRecommendedTests());
            variables.put("treatmentSuggestions", diagnosis.getTreatmentSuggestions());
            variables.put("specialNotes", diagnosis.getSpecialNotes());
            variables.put("confidenceScore", String.format("%.2f", diagnosis.getConfidenceScore()));
            variables.put("predictions", diagnosis.getPredictions());
            variables.put("modelVersion", diagnosis.getModelVersion());
            variables.put("reviewedByDoctor", diagnosis.getReviewedByDoctor());
            variables.put("doctorFeedback", diagnosis.getDoctorFeedback());
            variables.put("analyzedAt", diagnosis.getAnalyzedAt().format(DATE_TIME_FORMATTER));
            variables.put("generatedDate", LocalDateTime.now().format(DATE_TIME_FORMATTER));
            
            return generatePdfFromTemplate("ai-diagnosis-report", variables);
        } catch (Exception e) {
            throw new ReportGenerationException("Failed to generate AI diagnosis report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate payment invoice
     */
    public byte[] generatePaymentInvoice(Long paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ReportGenerationException("Payment not found"));
            
            Appointment appointment = payment.getAppointment();
            
            // Prepare data model for template
            Map<String, Object> variables = new HashMap<>();
            variables.put("invoiceNumber", "INV-" + payment.getId());
            variables.put("paymentId", payment.getId());
            variables.put("patientName", appointment.getPatient().getUser().getFirstName() + " " + 
                    appointment.getPatient().getUser().getLastName());
            variables.put("patientEmail", appointment.getPatient().getUser().getEmail());
            variables.put("doctorName", "Dr. " + appointment.getDoctor().getUser().getFirstName() + " " + 
                    appointment.getDoctor().getUser().getLastName());
            variables.put("appointmentDate", appointment.getAppointmentDateTime().format(DATE_TIME_FORMATTER));
            variables.put("appointmentType", appointment.getAppointmentType());
            variables.put("amount", payment.getAmount());
            variables.put("currency", payment.getCurrency());
            variables.put("status", payment.getStatus());
            variables.put("paymentDate", payment.getCompletedAt() != null ? 
                    payment.getCompletedAt().format(DATE_TIME_FORMATTER) : "Pending");
            variables.put("transactionId", payment.getPaymentIntentId());
            variables.put("generatedDate", LocalDateTime.now().format(DATE_TIME_FORMATTER));
            
            return generatePdfFromTemplate("payment-invoice", variables);
        } catch (Exception e) {
            throw new ReportGenerationException("Failed to generate payment invoice: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate PDF from Thymeleaf template
     */
    private byte[] generatePdfFromTemplate(String templateName, Map<String, Object> variables) throws IOException {
        // Process the Thymeleaf template to HTML
        Context context = new Context();
        context.setVariables(variables);
        String htmlContent = templateEngine.process(templateName, context);
        
        // Convert HTML to PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        
        ConverterProperties converterProperties = new ConverterProperties();
        HtmlConverter.convertToPdf(htmlContent, pdfDocument, converterProperties);
        
        return outputStream.toByteArray();
    }
}