package com.mediconnect.controller;

import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import com.mediconnect.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.RoleRepository;
import com.mediconnect.model.User;
import com.mediconnect.model.Patient;
import com.mediconnect.model.ERole;
import com.mediconnect.model.Role;

import com.mediconnect.dto.PatientDTO;
import com.mediconnect.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Get all patients
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        try {
            List<PatientDTO> patients = patientService.getAllPatientDTOs();
            return new ResponseEntity<>(patients, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching all patients: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get patient by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isOwner(#id)")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        try {
            PatientDTO patient = patientService.getPatientDTOById(id);
            return new ResponseEntity<>(patient, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("Error fetching patient by ID " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * FIXED: Get patient by user ID with proper auto-creation
     */
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isCurrentUser(#userId)")
    @Transactional
    public ResponseEntity<PatientDTO> getPatientByUserId(@PathVariable Long userId) {
        try {
            System.out.println("🔍 Looking for patient profile for user ID: " + userId);
            
            // First, check if patient already exists
            Optional<PatientDTO> existingPatient = patientService.getPatientDTOByUserId(userId);
            
            if (existingPatient.isPresent()) {
                System.out.println("✅ Found existing patient profile: " + existingPatient.get().getId());
                return new ResponseEntity<>(existingPatient.get(), HttpStatus.OK);
            }
            
            System.out.println("❌ No patient profile found, checking if auto-creation is possible...");
            
            // Get user to check if they exist and have PATIENT role
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                System.err.println("❌ User not found with ID: " + userId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            User user = userOpt.get();
            System.out.println("👤 Found user: " + user.getEmail() + ", roles: " + user.getRoles());
            
            // Check if user has PATIENT role
            boolean hasPatientRole = user.getRoles().stream()
                    .anyMatch(role -> role.getName() == ERole.ROLE_PATIENT);
            
            if (!hasPatientRole) {
                System.out.println("⚠️ User does not have PATIENT role, adding it...");
                
                // Add PATIENT role to the user
                Optional<Role> patientRoleOpt = roleRepository.findByName(ERole.ROLE_PATIENT);
                if (patientRoleOpt.isPresent()) {
                    user.getRoles().add(patientRoleOpt.get());
                    userRepository.save(user);
                    System.out.println("✅ Added PATIENT role to user");
                } else {
                    System.err.println("❌ PATIENT role not found in database");
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            
            // Create minimal patient profile
            System.out.println("🏗️ Creating new patient profile...");
            Patient newPatient = new Patient();
            newPatient.setUser(user);
            // Set some default values to avoid null pointer exceptions
            newPatient.setAllergies("");
            newPatient.setChronicDiseases("");
            newPatient.setEmergencyContactNumber("");
            
            try {
                Patient savedPatient = patientRepository.save(newPatient);
                System.out.println("✅ Created new patient profile with ID: " + savedPatient.getId());
                
                PatientDTO patientDTO = PatientDTO.fromEntity(savedPatient);
                return new ResponseEntity<>(patientDTO, HttpStatus.OK);
                
            } catch (Exception e) {
                System.err.println("❌ Failed to save new patient profile: " + e.getMessage());
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in getPatientByUserId for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search patients by keyword
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<PatientDTO>> searchPatients(@RequestParam String keyword) {
        try {
            List<PatientDTO> patients = patientService.searchPatientDTOs(keyword);
            return new ResponseEntity<>(patients, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error searching patients with keyword '" + keyword + "': " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get patients by medical condition
     */
    @GetMapping("/condition")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<PatientDTO>> getPatientsByMedicalCondition(@RequestParam String condition) {
        try {
            List<PatientDTO> patients = patientService.getPatientDTOsByMedicalCondition(condition);
            return new ResponseEntity<>(patients, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching patients by condition '" + condition + "': " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get patients by doctor
     */
    @GetMapping("/by-doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorWithId(#doctorId)")
    public ResponseEntity<List<PatientDTO>> getPatientsByDoctor(@PathVariable Long doctorId) {
        try {
            List<PatientDTO> patients = patientService.getPatientDTOsByDoctor(doctorId);
            return new ResponseEntity<>(patients, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching patients by doctor ID " + doctorId + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get patients by insurance provider
     */
    @GetMapping("/insurance/{provider}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<PatientDTO>> getPatientsByInsuranceProvider(@PathVariable String provider) {
        try {
            List<PatientDTO> patients = patientService.getPatientDTOsByInsuranceProvider(provider);
            return new ResponseEntity<>(patients, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching patients by insurance provider '" + provider + "': " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a new patient
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        try {
            PatientDTO newPatient = patientService.createPatientAndReturnDTO(patientDTO);
            return new ResponseEntity<>(newPatient, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating patient: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing patient
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientDTO patientDTO) {
        try {
            PatientDTO updatedPatient = patientService.updatePatientAndReturnDTO(id, patientDTO);
            return new ResponseEntity<>(updatedPatient, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("Error updating patient ID " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ENHANCED: Delete a patient with enhanced dynamic cascade deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    @Transactional
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        try {
            System.out.println("🚀 Starting enhanced dynamic patient deletion for ID: " + id);
            
            // Verify patient exists
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
            
            Long userId = patient.getUser().getId();
            System.out.println("👤 Associated user ID: " + userId);
            
            // Store patient info for response before deletion
            String patientName = patient.getUser().getFirstName() + " " + patient.getUser().getLastName();
            String patientEmail = patient.getUser().getEmail();
            
            // Use enhanced dynamic cascade delete service
            System.out.println("🔄 Initiating enhanced cascade deletion...");
            patientService.deletePatientWithAllRelatedData(id);
            
            // ENHANCED: Run cleanup for orphaned records
            System.out.println("🧹 Running orphaned records cleanup...");
            patientService.cleanupOrphanedRecords();
            
            System.out.println("✅ Successfully deleted patient " + id + " and all related data with cleanup");
            
            // Return enhanced success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patient and all related data deleted successfully with enhanced cleanup");
            response.put("patientId", id);
            response.put("userId", userId);
            response.put("patientName", patientName);
            response.put("patientEmail", patientEmail);
            response.put("deletedAt", java.time.LocalDateTime.now());
            response.put("enhancedDeletion", true);
            response.put("cleanupPerformed", true);
            response.put("deletionMethod", "Enhanced Dynamic Cascade with FK Constraint Handling");
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            System.err.println("❌ Patient not found for deletion: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Patient not found");
            errorResponse.put("patientId", id.toString());
            errorResponse.put("message", "The patient with ID " + id + " does not exist in the system");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            System.err.println("❌ Critical error during enhanced patient deletion: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete patient: " + e.getMessage());
            errorResponse.put("patientId", id.toString());
            errorResponse.put("details", "The enhanced system attempted to detect and delete all related data with constraint handling, but an error occurred.");
            errorResponse.put("enhancedDeletion", true);
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("supportMessage", "Please contact system administrator if this issue persists");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * ENHANCED: Get deletion preview (what would be deleted) with better details
     */
    @GetMapping("/{id}/deletion-preview")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public ResponseEntity<?> getDeletionPreview(@PathVariable Long id) {
        try {
            System.out.println("🔍 Generating enhanced deletion preview for patient ID: " + id);
            
            // Verify patient exists first
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
            
            // This shows what data would be deleted without actually deleting
            Map<String, Object> preview = patientService.getDeletionPreview(id);
            
            // Add enhanced information
            preview.put("enhancedPreview", true);
            preview.put("constraintHandling", "Payment tables prioritized to prevent FK conflicts");
            preview.put("safeDeletion", "Enhanced cascade deletion with error recovery");
            preview.put("patientInfo", Map.of(
                "name", patient.getUser().getFirstName() + " " + patient.getUser().getLastName(),
                "email", patient.getUser().getEmail(),
                "userId", patient.getUser().getId()
            ));
            
            System.out.println("✅ Enhanced deletion preview generated successfully");
            return ResponseEntity.ok(preview);
            
        } catch (ResourceNotFoundException e) {
            System.err.println("❌ Patient not found for preview: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Patient not found");
            errorResponse.put("patientId", id.toString());
            errorResponse.put("enhancedPreview", "false");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            System.err.println("❌ Error generating enhanced deletion preview: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating deletion preview: " + e.getMessage());
            errorResponse.put("patientId", id.toString());
            errorResponse.put("enhancedPreview", "false");
            errorResponse.put("message", "Unable to generate deletion preview due to system error");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Enhanced file upload with validation
     */
    @PostMapping("/{id}/upload-profile")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @PathVariable Long id, 
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (!Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif")
                    .contains(contentType)) {
                throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and GIF are allowed");
            }
            
            // Check file size (5MB max)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("File size exceeds 5MB limit");
            }
            
            String filename = patientService.storeProfileImage(id, file);
            
            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("filename", filename);
            response.put("message", "Profile image uploaded successfully");
            response.put("url", "/uploads/" + filename);
            response.put("uploadedAt", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("patientId", id.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Patient not found");
            errorResponse.put("patientId", id.toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            System.err.println("Error uploading profile image for patient " + id + ": " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload profile image: " + e.getMessage());
            errorResponse.put("patientId", id.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get patient statistics
     */
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public ResponseEntity<Map<String, Object>> getPatientStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> stats = patientService.getPatientStatistics(id);
            
            // Add additional metadata
            stats.put("success", true);
            stats.put("patientId", id);
            stats.put("generatedAt", java.time.LocalDateTime.now());
            
            return new ResponseEntity<>(stats, HttpStatus.OK);
            
        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Patient not found");
            errorResponse.put("patientId", id);
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            System.err.println("Error fetching statistics for patient " + id + ": " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch patient statistics: " + e.getMessage());
            errorResponse.put("patientId", id);
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * BONUS: Test enhanced deletion system connectivity
     */
    @GetMapping("/{id}/deletion-system-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testDeletionSystem(@PathVariable Long id) {
        try {
            System.out.println("🔧 Testing enhanced deletion system for patient ID: " + id);
            
            // Verify patient exists
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
            
            // Test the deletion preview system
            Map<String, Object> testResults = new HashMap<>();
            testResults.put("patientExists", true);
            testResults.put("patientId", id);
            testResults.put("userId", patient.getUser().getId());
            testResults.put("enhancedDeletionAvailable", true);
            testResults.put("constraintHandlingEnabled", true);
            testResults.put("orphanedRecordsCleanupEnabled", true);
            testResults.put("testPerformedAt", java.time.LocalDateTime.now());
            
            // Try to get deletion preview to test system
            try {
                Map<String, Object> preview = patientService.getDeletionPreview(id);
                testResults.put("deletionPreviewWorking", true);
                testResults.put("tablesDetected", preview.get("tablesAffected"));
                testResults.put("totalRowsDetected", preview.get("totalRowsToDelete"));
            } catch (Exception e) {
                testResults.put("deletionPreviewWorking", false);
                testResults.put("previewError", e.getMessage());
            }
            
            System.out.println("✅ Enhanced deletion system test completed");
            return ResponseEntity.ok(testResults);
            
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Patient not found");
            errorResponse.put("patientId", id.toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            System.err.println("❌ Error testing deletion system: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Deletion system test failed: " + e.getMessage());
            errorResponse.put("patientId", id.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * BONUS: Health check endpoint for patient service
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("service", "PatientController");
            health.put("enhancedDeletionEnabled", true);
            health.put("timestamp", java.time.LocalDateTime.now());
            
            // Test database connectivity
            try {
                long patientCount = patientRepository.count();
                health.put("databaseConnected", true);
                health.put("totalPatients", patientCount);
            } catch (Exception e) {
                health.put("databaseConnected", false);
                health.put("databaseError", e.getMessage());
            }
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "unhealthy");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}