package com.mediconnect.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediconnect.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;
    
    /**
     * Generate patient medical history report
     */
    @GetMapping("/patient/{patientId}/medical-history")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPatientWithId(#patientId) or hasRole('DOCTOR')")
    public ResponseEntity<byte[]> generatePatientMedicalHistoryReport(@PathVariable Long patientId) {
        byte[] pdfBytes = reportService.generatePatientMedicalHistoryReport(patientId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "patient-medical-history.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    /**
     * Generate doctor appointment summary report
     */
    @GetMapping("/doctor/{doctorId}/appointments")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorWithId(#doctorId)")
    public ResponseEntity<byte[]> generateDoctorAppointmentReport(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        byte[] pdfBytes = reportService.generateDoctorAppointmentReport(doctorId, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "doctor-appointment-summary.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    /**
     * Generate prescription report
     */
    @GetMapping("/prescription/{prescriptionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessPrescription(#prescriptionId)")
    public ResponseEntity<byte[]> generatePrescriptionReport(@PathVariable Long prescriptionId) {
        byte[] pdfBytes = reportService.generatePrescriptionReport(prescriptionId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "prescription.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    /**
     * Generate AI diagnosis report
     */
    @GetMapping("/ai-diagnosis/{aiDiagnosisId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessAiDiagnosis(#aiDiagnosisId)")
    public ResponseEntity<byte[]> generateAiDiagnosisReport(@PathVariable Long aiDiagnosisId) {
        byte[] pdfBytes = reportService.generateAiDiagnosisReport(aiDiagnosisId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ai-diagnosis-report.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    /**
     * Generate payment invoice
     */
    @GetMapping("/payment/{paymentId}/invoice")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessPayment(#paymentId)")
    public ResponseEntity<byte[]> generatePaymentInvoice(@PathVariable Long paymentId) {
        byte[] pdfBytes = reportService.generatePaymentInvoice(paymentId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}