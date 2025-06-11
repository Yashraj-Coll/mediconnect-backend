package com.mediconnect.service;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.LabTestBooking;
import com.mediconnect.model.RazorpayPayment;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    @Value("${mediconnect.pdf.output-path:${java.io.tmpdir}/receipts/}")
    private String pdfOutputPath;

    @Value("${mediconnect.company.name:MediConnect Health}")
    private String companyName;

    @Value("${mediconnect.company.address:Healthcare Excellence Center, Tech City, India}")
    private String companyAddress;

    @Value("${mediconnect.company.phone:+91-1800-MEDICONNECT}")
    private String companyPhone;

    @Value("${mediconnect.company.email:support@mediconnect.com}")
    private String companyEmail;

    @Value("${mediconnect.company.gstin:19MEDICONNECT1Z9}")
    private String companyGstin;

    public String generatePaymentReceipt(RazorpayPayment payment, Appointment appointment) {
        try {
            // Create output directory if not exists
            Path outputDir = Paths.get(pdfOutputPath);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // Generate unique filename
            String fileName = "receipt_" + payment.getId() + "_" + System.currentTimeMillis() + ".pdf";
            String filePath = pdfOutputPath + fileName;

            // Create XHTML content
            String htmlContent = generateReceiptHtml(payment, appointment);

            // Generate PDF
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                renderer.createPDF(fos);
            }

            log.info("PDF receipt generated successfully: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Error generating PDF receipt: ", e);
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }

    private String generateReceiptHtml(RazorpayPayment payment, Appointment appointment) {
        String logoSvg = getMediConnectLogoSvg();

        // Format dates
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

        // Safe value methods to avoid null
        String patientName = getPatientName(appointment);
        String patientId = "MED" + (appointment.getId() != null ? appointment.getId() : "");
        String patientPhone = getPatientPhone(appointment);
        String patientEmail = getPatientEmail(appointment);
        String appointmentDateTime = "";
        if (appointment.getAppointmentDateTime() != null) {
            try {
                // Convert to IST timezone for PDF
                ZonedDateTime istDateTime = appointment.getAppointmentDateTime().atZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                appointmentDateTime = istDateTime.format(dateFormatter) + " " + istDateTime.format(timeFormatter);
                log.info("PDF appointment datetime for ID {}: {}", appointment.getId(), appointmentDateTime);
            } catch (Exception e) {
                log.error("Error formatting PDF datetime: ", e);
                appointmentDateTime = "Date/Time to be confirmed";
            }
        }
        String billNo = "INV-" + (payment.getId() != null ? payment.getId() : "");
        String paymentDate = payment.getCompletedAt() != null
                ? payment.getCompletedAt().format(fullFormatter)
                : (payment.getCreatedAt() != null ? payment.getCreatedAt().format(fullFormatter) : "");
        String razorpayPaymentId = payment.getRazorpayPaymentId() != null ? payment.getRazorpayPaymentId() : "";
        String razorpayOrderId = payment.getRazorpayOrderId() != null ? payment.getRazorpayOrderId() : "";
        String doctorName = getDoctorName(appointment);
        String appointmentType = (appointment.getAppointmentType() != null && appointment.getAppointmentType().equals(Appointment.AppointmentType.video))
                ? "Video" : "In-Person";
        String consultTypeText = appointmentType + " Consultation - Dr. " + doctorName;
        String consultDate = appointment.getAppointmentDateTime() != null ? appointment.getAppointmentDateTime().format(dateFormatter) : "";
        double consultationFee = getConsultationFee(appointment);
        double registrationFee = getRegistrationFee(appointment);
        String regFeeDate = consultDate;
        double total = consultationFee + registrationFee;
        double gst = getTaxAmount(appointment);
        double amountPaid = payment.getAmount() != null ? payment.getAmount().doubleValue() : total + gst;
        String gstin = companyGstin != null ? companyGstin : "";
        String paymentStatus = payment.getStatus() != null ? payment.getStatus().toString() : "";

        // Now, XHTML-compliant template:
        return String.format(
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
"<head>\n" +
"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
"    <title>Payment Receipt</title>\n" +
"    <style type=\"text/css\">\n" +
"        body {\n" +
"            font-family: Arial, sans-serif;\n" +
"            margin: 0;\n" +
"            padding: 20px;\n" +
"            font-size: 12px;\n" +
"            color: #333;\n" +
"        }\n" +
"        .header {\n" +
"            display: flex;\n" +
"            align-items: center;\n" +
"            border-bottom: 2px solid #7C3AED;\n" +
"            padding-bottom: 15px;\n" +
"            margin-bottom: 20px;\n" +
"        }\n" +
"        .logo {\n" +
"            margin-right: 20px;\n" +
"        }\n" +
"        .company-info {\n" +
"            flex: 1;\n" +
"        }\n" +
"        .company-name {\n" +
"            font-size: 24px;\n" +
"            font-weight: bold;\n" +
"            color: #7C3AED;\n" +
"            margin-bottom: 5px;\n" +
"        }\n" +
"        .company-subtitle {\n" +
"            font-size: 14px;\n" +
"            color: #666;\n" +
"            margin-bottom: 3px;\n" +
"        }\n" +
"        .bill-title {\n" +
"            text-align: center;\n" +
"            font-size: 18px;\n" +
"            font-weight: bold;\n" +
"            margin: 20px 0;\n" +
"        }\n" +
"        .bill-info {\n" +
"            width: 100%%;\n" +
"            display: flex;\n" +
"            justify-content: space-between;\n" +
"            margin-bottom: 20px;\n" +
"        }\n" +
"        .patient-info, .bill-details {\n" +
"            width: 48%%;\n" +
"        }\n" +
"        .info-row {\n" +
"            margin-bottom: 5px;\n" +
"        }\n" +
"        .label {\n" +
"            font-weight: bold;\n" +
"            display: inline-block;\n" +
"            width: 120px;\n" +
"        }\n" +
"        .services-table {\n" +
"            width: 100%%;\n" +
"            border-collapse: collapse;\n" +
"            margin: 20px 0;\n" +
"        }\n" +
"        .services-table th, .services-table td {\n" +
"            border: 1px solid #ddd;\n" +
"            padding: 8px;\n" +
"            text-align: left;\n" +
"        }\n" +
"        .services-table th {\n" +
"            background-color: #7C3AED;\n" +
"            color: white;\n" +
"            font-weight: bold;\n" +
"        }\n" +
"        .amount-section {\n" +
"            margin: 20px 0;\n" +
"            border: 1px solid #7C3AED;\n" +
"            padding: 15px;\n" +
"            background-color: #f8f9ff;\n" +
"        }\n" +
"        .amount-row {\n" +
"            display: flex;\n" +
"            justify-content: space-between;\n" +
"            margin-bottom: 5px;\n" +
"        }\n" +
"        .total-row {\n" +
"            font-weight: bold;\n" +
"            font-size: 14px;\n" +
"            border-top: 1px solid #7C3AED;\n" +
"            padding-top: 5px;\n" +
"            margin-top: 10px;\n" +
"        }\n" +
"        .footer {\n" +
"            margin-top: 30px;\n" +
"            border-top: 1px solid #ddd;\n" +
"            padding-top: 15px;\n" +
"            font-size: 10px;\n" +
"            color: #666;\n" +
"        }\n" +
"        .qr-section {\n" +
"            text-align: center;\n" +
"            margin-top: 20px;\n" +
"        }\n" +
"    </style>\n" +
"</head>\n" +
"<body>\n" +
"    <div class=\"header\">\n" +
"        <div class=\"logo\">\n" +
"            %s\n" +
"        </div>\n" +
"        <div class=\"company-info\">\n" +
"            <div class=\"company-name\">%s</div>\n" +
"            <div class=\"company-subtitle\">Advanced Healthcare Solutions</div>\n" +
"            <div class=\"company-subtitle\">%s</div>\n" +
"            <div class=\"company-subtitle\">Phone: %s | Email: %s</div>\n" +
"        </div>\n" +
"    </div>\n" +
"\n" +
"    <div class=\"bill-title\">BILL AND RECEIPT</div>\n" +
"\n" +
"    <div class=\"bill-info\">\n" +
"        <div class=\"patient-info\">\n" +
"            <div class=\"info-row\"><span class=\"label\">Patient Name:</span> %s</div>\n" +
"            <div class=\"info-row\"><span class=\"label\">Patient ID:</span> %s</div>\n" +
"            <div class=\"info-row\"><span class=\"label\">Phone No:</span> %s</div>\n" +
"            <div class=\"info-row\"><span class=\"label\">Email:</span> %s</div>\n" +
"            <div class=\"info-row\"><span class=\"label\">Appointment:</span> %s</div>\n" +
"        </div>\n" +
"        <div class=\"bill-details\">\n" +
"            <div class=\"info-row\"><span class=\"label\">Bill No:</span> %s</div>\n" +
"            <div class=\"info-row\"><span class=\"label\">Date:</span> %s</div>\n" +
"            <div class=\"info-row\"><span class=\"label\">Payment ID:</span> %s</div>\n" +
"            <div class=\"info-row\"><span class=\"label\">Order ID:</span> %s</div>\n" +
"            <div class=\"info-row\"><span class=\"label\">Consultant:</span> Dr. %s</div>\n" +
"        </div>\n" +
"    </div>\n" +
"\n" +
"    <table class=\"services-table\">\n" +
"        <thead>\n" +
"            <tr>\n" +
"                <th>Particulars</th>\n" +
"                <th>Date</th>\n" +
"                <th>Qty</th>\n" +
"                <th>Unit Rate</th>\n" +
"                <th>Amount (Rs)</th>\n" +
"            </tr>\n" +
"        </thead>\n" +
"        <tbody>\n" +
"            <tr>\n" +
"                <td>%s</td>\n" +
"                <td>%s</td>\n" +
"                <td>1</td>\n" +
"                <td>%.2f</td>\n" +
"                <td>%.2f</td>\n" +
"            </tr>\n" +
"            <tr>\n" +
"                <td>Registration Fee</td>\n" +
"                <td>%s</td>\n" +
"                <td>1</td>\n" +
"                <td>%.2f</td>\n" +
"                <td>%.2f</td>\n" +
"            </tr>\n" +
"        </tbody>\n" +
"    </table>\n" +
"\n" +
"    <div class=\"amount-section\">\n" +
"        <div class=\"amount-row\">\n" +
"            <span>Total Hospital Charges:</span>\n" +
"            <span>₹%.2f</span>\n" +
"        </div>\n" +
"        <div class=\"amount-row\">\n" +
"            <span>GST (18%%):</span>\n" +
"            <span>₹%.2f</span>\n" +
"        </div>\n" +
"        <div class=\"amount-row total-row\">\n" +
"            <span>Total Amount:</span>\n" +
"            <span>₹%.2f</span>\n" +
"        </div>\n" +
"        <div class=\"amount-row\">\n" +
"            <span>Amount Paid:</span>\n" +
"            <span>₹%.2f</span>\n" +
"        </div>\n" +
"        <div class=\"amount-row total-row\">\n" +
"            <span>Balance:</span>\n" +
"            <span>₹0.00</span>\n" +
"        </div>\n" +
"    </div>\n" +
"\n" +
"    <div class=\"footer\">\n" +
"        <p><strong>GSTIN:</strong> %s</p>\n" +
"        <p><strong>Payment Status:</strong> %s</p>\n" +
"        <p>Thank you for choosing MediConnect Health. Your health is our priority.</p>\n" +
"        <div class=\"qr-section\">\n" +
"            \n" +
"        </div>\n" +
"    </div>\n" +
"</body>\n" +
"</html>",
            logoSvg,
            companyName,
            companyAddress,
            companyPhone, companyEmail,
            patientName,
            patientId,
            patientPhone,
            patientEmail,
            appointmentDateTime,
            billNo,
            paymentDate,
            razorpayPaymentId,
            razorpayOrderId,
            doctorName,
            consultTypeText, consultDate, consultationFee, consultationFee,
            regFeeDate, registrationFee, registrationFee,
            total,
            gst,
            amountPaid,
            amountPaid,
            gstin,
            paymentStatus
        );
    }

    private String getMediConnectLogoSvg() {
        return "<svg width=\"200\" height=\"50\" viewBox=\"0 0 200 50\" xmlns=\"http://www.w3.org/2000/svg\">" +
               "<defs>" +
               "<linearGradient id=\"textGradient\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">" +
               "<stop offset=\"0%\" style=\"stop-color:#7C3AED;stop-opacity:1\" />" +
               "<stop offset=\"100%\" style=\"stop-color:#EC4899;stop-opacity:1\" />" +
               "</linearGradient>" +
               "</defs>" +
               // Purple solid background box with rounded corners
               "<rect x=\"5\" y=\"8\" width=\"40\" height=\"40\" rx=\"8\" fill=\"#8B5CF6\"/>" +
               // White "M" letter centered
               "<text x=\"25\" y=\"34\" font-family=\"Arial, sans-serif\" font-size=\"24\" font-weight=\"bold\" fill=\"white\" text-anchor=\"middle\">M</text>" +
               // Orange "+" symbol at top-right corner of the box
               "<text x=\"37\" y=\"20\" font-family=\"Arial, sans-serif\" font-size=\"14\" font-weight=\"bold\" fill=\"#FB923C\">+</text>" +
               // "MediConnect" text with gradient, positioned right after the box
               "<text x=\"55\" y=\"25\" font-family=\"Arial, sans-serif\" font-size=\"18\" font-weight=\"bold\" fill=\"url(#textGradient)\">MediConnect</text>" +
               // "HEALTH" text in orange, smaller, positioned after MediConnect
               "<text x=\"175\" y=\"25\" font-family=\"Arial, sans-serif\" font-size=\"12\" font-weight=\"600\" fill=\"#F97316\">HEALTH</text>" +
               "</svg>";
    }

    // Helper methods
    private String getPatientName(Appointment appointment) {
        if (appointment.getPatient() != null && appointment.getPatient().getUser() != null) {
            return appointment.getPatient().getUser().getFirstName() + " " +
                    appointment.getPatient().getUser().getLastName();
        }
        return "Patient";
    }

    private String getPatientPhone(Appointment appointment) {
        if (appointment.getPatient() != null && appointment.getPatient().getUser() != null) {
            return appointment.getPatient().getUser().getPhoneNumber() != null ?
                    appointment.getPatient().getUser().getPhoneNumber() : "Not provided";
        }
        return "Not provided";
    }

    private String getPatientEmail(Appointment appointment) {
        if (appointment.getPatient() != null && appointment.getPatient().getUser() != null) {
            return appointment.getPatient().getUser().getEmail();
        }
        return "Not provided";
    }

    private String getDoctorName(Appointment appointment) {
        if (appointment.getDoctor() != null && appointment.getDoctor().getUser() != null) {
            return appointment.getDoctor().getUser().getFirstName() + " " +
                    appointment.getDoctor().getUser().getLastName();
        }
        return "Doctor";
    }

    private Double getConsultationFee(Appointment appointment) {
        return appointment.getDoctor() != null && appointment.getDoctor().getConsultationFee() != null ?
                appointment.getDoctor().getConsultationFee() : 500.0;
    }

    private Double getRegistrationFee(Appointment appointment) {
        // Fixed registration fee since not in Doctor model
        return 350.0;
    }

    private Double getTaxAmount(Appointment appointment) {
        return (getConsultationFee(appointment) + getRegistrationFee(appointment)) * 0.18;
    }
    
 // Add this method to your existing PdfGenerationService.java

 public String generateLabTestReceipt(RazorpayPayment payment, LabTestBooking labTestBooking) {
     try {
         // Create output directory if not exists
         Path outputDir = Paths.get(pdfOutputPath);
         if (!Files.exists(outputDir)) {
             Files.createDirectories(outputDir);
         }

         // Generate unique filename
         String fileName = "lab_test_receipt_" + payment.getId() + "_" + System.currentTimeMillis() + ".pdf";
         String filePath = pdfOutputPath + fileName;

         // Create XHTML content
         String htmlContent = generateLabTestReceiptHtml(payment, labTestBooking);

         // Generate PDF
         ITextRenderer renderer = new ITextRenderer();
         renderer.setDocumentFromString(htmlContent);
         renderer.layout();

         try (FileOutputStream fos = new FileOutputStream(filePath)) {
             renderer.createPDF(fos);
         }

         log.info("Lab test PDF receipt generated successfully: {}", filePath);
         return filePath;

     } catch (Exception e) {
         log.error("Error generating lab test PDF receipt: ", e);
         throw new RuntimeException("Failed to generate lab test PDF receipt", e);
     }
 }

 private String generateLabTestReceiptHtml(RazorpayPayment payment, LabTestBooking labTestBooking) {
     String logoSvg = getMediConnectLogoSvg();

     // Format dates
     DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
     DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
     DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

     // Safe value methods to avoid null
     String patientName = getLabTestPatientName(labTestBooking);
     String patientId = "LAB" + (labTestBooking.getId() != null ? labTestBooking.getId() : "");
     String patientPhone = getLabTestPatientPhone(labTestBooking);
     String patientEmail = getLabTestPatientEmail(labTestBooking);
     
     // Simple date formatting without timezone conversion - just like your appointment method
     String bookingDateTime = "Sample collection to be scheduled";
     if (labTestBooking.getScheduledDate() != null) {
         try {
             bookingDateTime = labTestBooking.getScheduledDate().format(dateFormatter) + " " + 
                             labTestBooking.getScheduledDate().format(timeFormatter);
             log.info("PDF lab test datetime for ID {}: {}", labTestBooking.getId(), bookingDateTime);
         } catch (Exception e) {
             log.error("Error formatting PDF lab test datetime: ", e);
             bookingDateTime = "Date/Time to be confirmed";
         }
     }
     
     String billNo = "LAB-INV-" + (payment.getId() != null ? payment.getId() : "");
     String paymentDate = payment.getCompletedAt() != null
             ? payment.getCompletedAt().format(fullFormatter)
             : (payment.getCreatedAt() != null ? payment.getCreatedAt().format(fullFormatter) : "");
     String razorpayPaymentId = payment.getRazorpayPaymentId() != null ? payment.getRazorpayPaymentId() : "";
     String razorpayOrderId = payment.getRazorpayOrderId() != null ? payment.getRazorpayOrderId() : "";
     String testName = labTestBooking.getTestName() != null ? labTestBooking.getTestName() : "Lab Test";
     String sampleType = labTestBooking.getSampleType() != null ? labTestBooking.getSampleType() : "Blood";
     String collectionType = labTestBooking.getHomeCollection() ? "Home Collection" : "Lab Visit";
     String testDate = labTestBooking.getScheduledDate() != null ? labTestBooking.getScheduledDate().format(dateFormatter) : "";
     double testPrice = labTestBooking.getTestPrice() != null ? labTestBooking.getTestPrice().doubleValue() : 0.0;
     double registrationFee = labTestBooking.getRegistrationFee() != null ? labTestBooking.getRegistrationFee().doubleValue() : 50.0;
     String regFeeDate = testDate;
     double total = testPrice + registrationFee;
     double gst = labTestBooking.getTaxAmount() != null ? labTestBooking.getTaxAmount().doubleValue() : total * 0.18;
     double amountPaid = payment.getAmount() != null ? payment.getAmount().doubleValue() : total + gst;
     String gstin = companyGstin != null ? companyGstin : "";
     String paymentStatus = payment.getStatus() != null ? payment.getStatus().toString() : "";

     // Same HTML template as your appointment receipt but for lab tests
     return String.format(
 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
 "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
 "<head>\n" +
 "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
 "    <title>Lab Test Receipt</title>\n" +
 "    <style type=\"text/css\">\n" +
 "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; font-size: 12px; color: #333; }\n" +
 "        .header { display: flex; align-items: center; border-bottom: 2px solid #7C3AED; padding-bottom: 15px; margin-bottom: 20px; }\n" +
 "        .logo { margin-right: 20px; }\n" +
 "        .company-info { flex: 1; }\n" +
 "        .company-name { font-size: 24px; font-weight: bold; color: #7C3AED; margin-bottom: 5px; }\n" +
 "        .company-subtitle { font-size: 14px; color: #666; margin-bottom: 3px; }\n" +
 "        .bill-title { text-align: center; font-size: 18px; font-weight: bold; margin: 20px 0; }\n" +
 "        .bill-info { width: 100%%; display: flex; justify-content: space-between; margin-bottom: 20px; }\n" +
 "        .patient-info, .bill-details { width: 48%%; }\n" +
 "        .info-row { margin-bottom: 5px; }\n" +
 "        .label { font-weight: bold; display: inline-block; width: 120px; }\n" +
 "        .services-table { width: 100%%; border-collapse: collapse; margin: 20px 0; }\n" +
 "        .services-table th, .services-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n" +
 "        .services-table th { background-color: #7C3AED; color: white; font-weight: bold; }\n" +
 "        .amount-section { margin: 20px 0; border: 1px solid #7C3AED; padding: 15px; background-color: #f8f9ff; }\n" +
 "        .amount-row { display: flex; justify-content: space-between; margin-bottom: 5px; }\n" +
 "        .total-row { font-weight: bold; font-size: 14px; border-top: 1px solid #7C3AED; padding-top: 5px; margin-top: 10px; }\n" +
 "        .footer { margin-top: 30px; border-top: 1px solid #ddd; padding-top: 15px; font-size: 10px; color: #666; }\n" +
 "        .qr-section { text-align: center; margin-top: 20px; }\n" +
 "    </style>\n" +
 "</head>\n" +
 "<body>\n" +
 "    <div class=\"header\">\n" +
 "        <div class=\"logo\">%s</div>\n" +
 "        <div class=\"company-info\">\n" +
 "            <div class=\"company-name\">%s</div>\n" +
 "            <div class=\"company-subtitle\">Advanced Healthcare Solutions</div>\n" +
 "            <div class=\"company-subtitle\">%s</div>\n" +
 "            <div class=\"company-subtitle\">Phone: %s | Email: %s</div>\n" +
 "        </div>\n" +
 "    </div>\n" +
 "    <div class=\"bill-title\">LAB TEST BILL AND RECEIPT</div>\n" +
 "    <div class=\"bill-info\">\n" +
 "        <div class=\"patient-info\">\n" +
 "            <div class=\"info-row\"><span class=\"label\">Patient Name:</span> %s</div>\n" +
 "            <div class=\"info-row\"><span class=\"label\">Patient ID:</span> %s</div>\n" +
 "            <div class=\"info-row\"><span class=\"label\">Phone No:</span> %s</div>\n" +
 "            <div class=\"info-row\"><span class=\"label\">Email:</span> %s</div>\n" +
 "            <div class=\"info-row\"><span class=\"label\">Collection:</span> %s</div>\n" +
 "        </div>\n" +
 "        <div class=\"bill-details\">\n" +
 "            <div class=\"info-row\"><span class=\"label\">Bill No:</span> %s</div>\n" +
 "            <div class=\"info-row\"><span class=\"label\">Date:</span> %s</div>\n" +
 "            <div class=\"info-row\"><span class=\"label\">Payment ID:</span> %s</div>\n" +
 "            <div class=\"info-row\"><span class=\"label\">Order ID:</span> %s</div>\n" +
 "            <div class=\"info-row\"><span class=\"label\">Sample Type:</span> %s</div>\n" +
 "        </div>\n" +
 "    </div>\n" +
 "    <table class=\"services-table\">\n" +
 "        <thead><tr><th>Particulars</th><th>Date</th><th>Qty</th><th>Unit Rate</th><th>Amount (₹)</th></tr></thead>\n" +
 "        <tbody>\n" +
 "            <tr><td>%s - %s</td><td>%s</td><td>1</td><td>%.2f</td><td>%.2f</td></tr>\n" +
 "            <tr><td>Lab Registration Fee</td><td>%s</td><td>1</td><td>%.2f</td><td>%.2f</td></tr>\n" +
 "        </tbody>\n" +
 "    </table>\n" +
 "    <div class=\"amount-section\">\n" +
 "        <div class=\"amount-row\"><span>Total Lab Charges:</span><span>₹%.2f</span></div>\n" +
 "        <div class=\"amount-row\"><span>GST (18%%):</span><span>₹%.2f</span></div>\n" +
 "        <div class=\"amount-row total-row\"><span>Total Amount:</span><span>₹%.2f</span></div>\n" +
 "        <div class=\"amount-row\"><span>Amount Paid:</span><span>₹%.2f</span></div>\n" +
 "        <div class=\"amount-row total-row\"><span>Balance:</span><span>₹0.00</span></div>\n" +
 "    </div>\n" +
 "    <div class=\"footer\">\n" +
 "        <p><strong>GSTIN:</strong> %s</p>\n" +
 "        <p><strong>Payment Status:</strong> %s</p>\n" +
 "        <p>Thank you for choosing MediConnect Health. Your health is our priority.</p>\n" +
 "        <div class=\"qr-section\"><p></div>\n" +
 "    </div>\n" +
 "</body>\n" +
 "</html>",
         logoSvg, companyName, companyAddress, companyPhone, companyEmail,
         patientName, patientId, patientPhone, patientEmail, bookingDateTime,
         billNo, paymentDate, razorpayPaymentId, razorpayOrderId, sampleType,
         testName, collectionType, testDate, testPrice, testPrice,
         regFeeDate, registrationFee, registrationFee,
         total, gst, amountPaid, amountPaid, gstin, paymentStatus
     );
 }

 // Helper methods for lab test
 private String getLabTestPatientName(LabTestBooking labTestBooking) {
     if (labTestBooking.getPatient() != null && labTestBooking.getPatient().getUser() != null) {
         return labTestBooking.getPatient().getUser().getFirstName() + " " +
                 labTestBooking.getPatient().getUser().getLastName();
     }
     return "Patient";
 }

 private String getLabTestPatientPhone(LabTestBooking labTestBooking) {
     if (labTestBooking.getPatientPhone() != null) {
         return labTestBooking.getPatientPhone();
     }
     if (labTestBooking.getPatient() != null && labTestBooking.getPatient().getUser() != null) {
         return labTestBooking.getPatient().getUser().getPhoneNumber() != null ?
                 labTestBooking.getPatient().getUser().getPhoneNumber() : "Not provided";
     }
     return "Not provided";
 }

 private String getLabTestPatientEmail(LabTestBooking labTestBooking) {
     if (labTestBooking.getPatientEmail() != null) {
         return labTestBooking.getPatientEmail();
     }
     if (labTestBooking.getPatient() != null && labTestBooking.getPatient().getUser() != null) {
         return labTestBooking.getPatient().getUser().getEmail();
     }
     return "Not provided";
 }
}
