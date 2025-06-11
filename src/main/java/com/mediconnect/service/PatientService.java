package com.mediconnect.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mediconnect.dto.PatientDTO;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.ERole;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.RoleRepository;
import com.mediconnect.repository.UserRepository;

@Service
public class PatientService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Get all patients (entity version)
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
    
    /**
     * Get all patients (DTO version)
     */
    public List<PatientDTO> getAllPatientDTOs() {
        return patientRepository.findAll().stream()
                .map(PatientDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get patient by ID (entity version)
     */
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }
    
    /**
     * Get patient by ID (DTO version)
     */
    public PatientDTO getPatientDTOById(Long id) {
        Patient patient = getPatientById(id);
        return PatientDTO.fromEntity(patient);
    }
    
    /**
     * Get patient by user ID (entity version)
     */
    public Optional<Patient> getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId);
    }
    
    /**
     * Get patient by user ID (DTO version)
     */
    public Optional<PatientDTO> getPatientDTOByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .map(PatientDTO::fromEntity);
    }
    
    /**
     * Search patients (entity version)
     */
    public List<Patient> searchPatients(String keyword) {
        return patientRepository.searchPatients(keyword);
    }
    
    /**
     * Search patients (DTO version)
     */
    public List<PatientDTO> searchPatientDTOs(String keyword) {
        return patientRepository.searchPatients(keyword).stream()
                .map(PatientDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get patients by medical condition (entity version)
     */
    public List<Patient> getPatientsByMedicalCondition(String condition) {
        return patientRepository.findByMedicalCondition(condition);
    }
    
    /**
     * Get patients by medical condition (DTO version)
     */
    public List<PatientDTO> getPatientDTOsByMedicalCondition(String condition) {
        return patientRepository.findByMedicalCondition(condition).stream()
                .map(PatientDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get patients by doctor (entity version)
     */
    public List<Patient> getPatientsByDoctor(Long doctorId) {
        return patientRepository.findPatientsByDoctor(doctorId);
    }
    
    /**
     * Get patients by doctor (DTO version)
     */
    public List<PatientDTO> getPatientDTOsByDoctor(Long doctorId) {
        return patientRepository.findPatientsByDoctor(doctorId).stream()
                .map(PatientDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get patients by insurance provider (entity version)
     */
    public List<Patient> getPatientsByInsuranceProvider(String provider) {
        return patientRepository.findByInsuranceProvider(provider);
    }
    
    /**
     * Get patients by insurance provider (DTO version)
     */
    public List<PatientDTO> getPatientDTOsByInsuranceProvider(String provider) {
        return patientRepository.findByInsuranceProvider(provider).stream()
                .map(PatientDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a new patient
     */
    @Transactional
    public Patient createPatient(PatientDTO patientDTO) {
        // Get or create the user
        User user = userRepository.findById(patientDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + patientDTO.getUserId()));
        
        // Check if user already has a patient profile
        if (patientRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("User already has a patient profile");
        }
        
        // Add PATIENT role to user if not already present
        Role patientRole = roleRepository.findByName(ERole.ROLE_PATIENT)
                .orElseThrow(() -> new RuntimeException("Error: Patient Role not found"));
        
        if (!user.getRoles().contains(patientRole)) {
            user.getRoles().add(patientRole);
            userRepository.save(user);
        }
        
        // Create new patient profile
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(patientDTO.getGender());
        patient.setBloodGroup(patientDTO.getBloodGroup());
        patient.setAllergies(patientDTO.getAllergies());
        patient.setChronicDiseases(patientDTO.getChronicDiseases());
        patient.setEmergencyContactNumber(patientDTO.getEmergencyContactNumber());
        
        return patientRepository.save(patient);
    }
    
    /**
     * Create a new patient and return DTO
     */
    @Transactional
    public PatientDTO createPatientAndReturnDTO(PatientDTO patientDTO) {
        Patient patient = createPatient(patientDTO);
        return PatientDTO.fromEntity(patient);
    }
    
    /**
     * Update an existing patient
     */
    @Transactional
    public Patient updatePatient(Long id, PatientDTO patientDTO) {
        Patient patient = getPatientById(id);
        
        // Update patient fields
        if (patientDTO.getDateOfBirth() != null) {
            patient.setDateOfBirth(patientDTO.getDateOfBirth());
        }
        
        if (patientDTO.getGender() != null) {
            patient.setGender(patientDTO.getGender());
        }
        
        if (patientDTO.getBloodGroup() != null) {
            patient.setBloodGroup(patientDTO.getBloodGroup());
        }
        
        if (patientDTO.getAllergies() != null) {
            patient.setAllergies(patientDTO.getAllergies());
        }
        
        if (patientDTO.getChronicDiseases() != null) {
            patient.setChronicDiseases(patientDTO.getChronicDiseases());
        }
        
        if (patientDTO.getEmergencyContactNumber() != null) {
            patient.setEmergencyContactNumber(patientDTO.getEmergencyContactNumber());
        }
        
        if (patientDTO.getProfileImage() != null) {
            patient.setProfileImage(patientDTO.getProfileImage());
        }
        
        return patientRepository.save(patient);
    }
    
    /**
     * Update an existing patient and return DTO
     */
    @Transactional
    public PatientDTO updatePatientAndReturnDTO(Long id, PatientDTO patientDTO) {
        Patient patient = updatePatient(id, patientDTO);
        return PatientDTO.fromEntity(patient);
    }
    
    /**
     * Delete a patient
     */
    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }

    /**
     * Store profile image for a patient
     */
    public String storeProfileImage(Long patientId, MultipartFile file) {
        try {
            String filename = patientId + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get("uploads").resolve(filename);
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath);

            Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
            patient.setProfileImage(filename);
            patientRepository.save(patient);

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
    
    /**
     * Get patient statistics
     */
    public Map<String, Object> getPatientStatistics(Long patientId) {
        Patient patient = getPatientById(patientId);
        Map<String, Object> stats = new HashMap<>();
        
        // Calculate statistics
        stats.put("totalAppointments", patient.getAppointments().size());
        stats.put("totalMedicalRecords", patient.getMedicalRecords().size());
        stats.put("accountAge", ChronoUnit.DAYS.between(patient.getCreatedAt().toLocalDate(), LocalDate.now()));
        
        return stats;
    }

    // ========== ENHANCED DYNAMIC CASCADE DELETE METHODS ==========
    
    /**
     * ENHANCED: Dynamically delete patient and all related data by discovering foreign key relationships
     */
    /**
     * FIXED: Dynamically delete patient and all related data
     */
    /**
     * FIXED: Complete patient and user deletion with proper error handling
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePatientWithAllRelatedData(Long patientId) {
        try {
            System.out.println("🔍 Starting FIXED complete deletion for patient ID: " + patientId);
            
            // STEP 1: Get patient and user info BEFORE deletion
            String getUserIdSql = "SELECT user_id FROM patients WHERE id = ?";
            Long userId = jdbcTemplate.queryForObject(getUserIdSql, Long.class, patientId);
            
            if (userId == null) {
                throw new RuntimeException("Patient with ID " + patientId + " does not exist or has no associated user");
            }
            
            System.out.println("📋 Patient ID: " + patientId + ", User ID: " + userId);
            
            // STEP 2: Delete payment records FIRST (your existing fix)
            deletePaymentRecordsFirst(patientId);
            
            // STEP 3: Get all foreign key relationships
            List<ForeignKeyRelation> foreignKeys = getAllForeignKeyRelationships();
            Set<String> patientRelatedTables = findAllPatientRelatedTables(foreignKeys);
            
            // STEP 4: FIXED deletion order - ensure users table is included
            List<String> deletionOrder = calculateFixedDeletionOrderWithUser(foreignKeys, patientRelatedTables, patientId, userId);
            
            System.out.println("📋 FIXED deletion order: " + deletionOrder);
            
            // STEP 5: Track deletion progress
            Map<String, Integer> deletionResults = new HashMap<>();
            
            // STEP 6: Delete from each table (skip razorpay_payments as already handled)
            for (String tableName : deletionOrder) {
                if (tableName.equals("razorpay_payments")) {
                    System.out.println("⏭️ Skipping razorpay_payments (already handled)");
                    continue;
                }
                
                int deletedRows = deleteFromTableSafe(tableName, patientId, userId, foreignKeys);
                deletionResults.put(tableName, deletedRows);
            }
            
            // STEP 7: FORCE delete user if still exists
            forceDeleteUser(userId, deletionResults);
            
            System.out.println("✅ FIXED deletion results: " + deletionResults);
            System.out.println("✅ FIXED complete deletion completed successfully");
            
            // STEP 8: Verify deletion
            verifyCompleteDeleteion(patientId, userId);
            
        } catch (Exception e) {
            System.err.println("❌ Error in FIXED complete deletion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to completely delete patient data: " + e.getMessage(), e);
        }
    }
    
    /**
     * FIXED: Calculate deletion order without razorpay_payments conflicts
     */
    private List<String> calculateFixedDeletionOrder(List<ForeignKeyRelation> foreignKeys, 
                                                   Set<String> relevantTables, Long patientId) {
        
        // Find tables with actual data to delete
        Set<String> tablesWithData = new HashSet<>();
        for (String tableName : relevantTables) {
            // Skip razorpay_payments as we handle it separately
            if (tableName.equals("razorpay_payments")) {
                continue;
            }
            
            try {
                String deleteCondition = findDeleteCondition(tableName, patientId, foreignKeys);
                if (deleteCondition != null) {
                    String countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + deleteCondition;
                    Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
                    if (count != null && count > 0) {
                        tablesWithData.add(tableName);
                        System.out.println("📊 Table " + tableName + " has " + count + " rows to delete");
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not check data in table " + tableName + ": " + e.getMessage());
            }
        }
        
        // Create a simple deletion order
        List<String> deletionOrder = new ArrayList<>();
        
        // Detail/child tables first
        String[] detailTables = {
            "prescription_items", "health_risk_assessments", "video_sessions", 
            "treatment_recommendations", "diagnosis_predictions", "image_analysis_results",
            "device_readings", "ai_diagnosis_results", "genomic_risk_factors", 
            "genomic_data", "medical_record_blockchain", "vital_signs", "alert_rules",
            "triage_records", "medical_documents", "reviews", "chat_sessions"
        };
        for (String table : detailTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // Core patient data tables
        String[] coreDataTables = {
            "medical_records", "prescriptions", "lab_test_bookings", 
            "appointments", "monitoring_devices"
        };
        for (String table : coreDataTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // User-related tables
        String[] userTables = {"sessions", "user_roles"};
        for (String table : userTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // Core entity tables (delete last)
        String[] coreTables = {"patients", "users"};
        for (String table : coreTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // Add any remaining tables
        deletionOrder.addAll(tablesWithData);
        
        return deletionOrder;
    }
    
    /**
     * SAFE: Delete from table with simple error handling
     */
    /**
     * ENHANCED: Delete from table with both patient and user handling
     */
    private int deleteFromTableSafe(String tableName, Long patientId, Long userId, List<ForeignKeyRelation> foreignKeys) {
        try {
            String deleteCondition = findDeleteConditionForTable(tableName, patientId, userId, foreignKeys);
            
            if (deleteCondition != null) {
                // Count what will be deleted
                String countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + deleteCondition;
                Integer rowCount = jdbcTemplate.queryForObject(countSql, Integer.class);
                
                if (rowCount != null && rowCount > 0) {
                    // Perform the deletion
                    String deleteSql = "DELETE FROM " + tableName + " WHERE " + deleteCondition;
                    int deletedRows = jdbcTemplate.update(deleteSql);
                    
                    System.out.println("🗑️ Deleted " + deletedRows + " rows from " + tableName + " (expected: " + rowCount + ")");
                    return deletedRows;
                } else {
                    System.out.println("ℹ️ No rows to delete from " + tableName);
                    return 0;
                }
            } else {
                System.out.println("⚠️ No delete condition found for " + tableName);
                return 0;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting from " + tableName + ": " + e.getMessage());
            
            // For critical tables, re-throw the exception
            if (tableName.equals("patients") || tableName.equals("users")) {
                throw new RuntimeException("Failed to delete from critical table " + tableName + ": " + e.getMessage(), e);
            }
            
            return 0;
        }
    }
    
    /**
     * Get all foreign key relationships in the database (MySQL VERSION)
     */
    private List<ForeignKeyRelation> getAllForeignKeyRelationships() {
        String sql = """
            SELECT 
                kcu.table_name as child_table,
                kcu.column_name as child_column,
                kcu.referenced_table_name as parent_table,
                kcu.referenced_column_name as parent_column
            FROM 
                information_schema.key_column_usage kcu
            WHERE 
                kcu.referenced_table_name IS NOT NULL
                AND kcu.table_schema = DATABASE()
            ORDER BY kcu.table_name, kcu.ordinal_position
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new ForeignKeyRelation(
                rs.getString("child_table"),
                rs.getString("child_column"),
                rs.getString("parent_table"),
                rs.getString("parent_column")
            )
        );
    }
    
    /**
     * Find all tables that are related to patients (directly or indirectly)
     */
    private Set<String> findAllPatientRelatedTables(List<ForeignKeyRelation> foreignKeys) {
        Set<String> relatedTables = new HashSet<>();
        Queue<String> toProcess = new LinkedList<>();
        
        // Start with patients table
        relatedTables.add("patients");
        toProcess.add("patients");
        
        // Find all tables that reference patient data (directly or indirectly)
        while (!toProcess.isEmpty()) {
            String currentTable = toProcess.poll();
            
            // Find all tables that reference this table
            for (ForeignKeyRelation fk : foreignKeys) {
                if (fk.parentTable.equals(currentTable) && !relatedTables.contains(fk.childTable)) {
                    relatedTables.add(fk.childTable);
                    toProcess.add(fk.childTable);
                }
            }
        }
        
        // Also add user-related tables (since patient references user)
        relatedTables.add("users");
        relatedTables.add("user_roles");
        relatedTables.add("sessions");
        
        System.out.println("🔗 Found patient-related tables: " + relatedTables);
        return relatedTables;
    }
    
    /**
     * ENHANCED: Calculate deletion order with better constraint handling and payment table priority
     */
    private List<String> calculateEnhancedDeletionOrder(List<ForeignKeyRelation> foreignKeys, 
                                                       Set<String> relevantTables, Long patientId) {
        
        // First, identify tables with actual data to delete
        Set<String> tablesWithData = new HashSet<>();
        for (String tableName : relevantTables) {
            try {
                String deleteCondition = findDeleteCondition(tableName, patientId, foreignKeys);
                if (deleteCondition != null) {
                    String countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + deleteCondition;
                    Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
                    if (count != null && count > 0) {
                        tablesWithData.add(tableName);
                        System.out.println("📊 Table " + tableName + " has " + count + " rows to delete");
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not check data in table " + tableName + ": " + e.getMessage());
            }
        }
        
        // Create a priority-based deletion order
        List<String> deletionOrder = new ArrayList<>();
        
        // HIGH PRIORITY: Payment and financial tables (delete first to avoid FK conflicts)
        String[] paymentTables = {"razorpay_payments", "payments"};
        for (String table : paymentTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // MEDIUM PRIORITY: Detail/child tables
        String[] detailTables = {
            "prescription_items", "health_risk_assessments", "video_sessions", 
            "treatment_recommendations", "diagnosis_predictions", "image_analysis_results",
            "device_readings", "ai_diagnosis_results", "genomic_risk_factors", 
            "genomic_data", "medical_record_blockchain"
        };
        for (String table : detailTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // MEDIUM-LOW PRIORITY: Core patient data tables
        String[] patientDataTables = {
            "lab_test_bookings", "triage_records", "medical_documents", 
            "medical_records", "prescriptions", "appointments", "vital_signs", 
            "alert_rules", "monitoring_devices", "reviews", "chat_sessions"
        };
        for (String table : patientDataTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // LOW PRIORITY: User-related tables (delete after patient data)
        String[] userTables = {"sessions", "user_roles"};
        for (String table : userTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // LOWEST PRIORITY: Core entity tables (delete last)
        String[] coreTables = {"patients", "users"};
        for (String table : coreTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // Add any remaining tables
        deletionOrder.addAll(tablesWithData);
        
        System.out.println("🎯 Enhanced deletion order prioritizes constraints: " + deletionOrder);
        return deletionOrder;
    }
    
    /**
     * Enhanced delete from table with better error handling and constraint management
     */
    private int deleteFromTableEnhanced(String tableName, Long patientId, List<ForeignKeyRelation> foreignKeys) {
        try {
            String deleteCondition = findDeleteCondition(tableName, patientId, foreignKeys);
            
            if (deleteCondition != null) {
                // First count what will be deleted
                String countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + deleteCondition;
                Integer rowCount = jdbcTemplate.queryForObject(countSql, Integer.class);
                
                if (rowCount != null && rowCount > 0) {
                    // ENHANCED: Special handling for payment tables
                    if (tableName.contains("payment")) {
                        return deletePaymentTableSafely(tableName, deleteCondition, rowCount);
                    }
                    
                    // Perform the deletion
                    String deleteSql = "DELETE FROM " + tableName + " WHERE " + deleteCondition;
                    int deletedRows = jdbcTemplate.update(deleteSql);
                    
                    System.out.println("🗑️ Deleted " + deletedRows + " rows from " + tableName + " (expected: " + rowCount + ")");
                    return deletedRows;
                } else {
                    System.out.println("ℹ️ No rows to delete from " + tableName);
                    return 0;
                }
            } else {
                System.out.println("⚠️ No delete condition found for " + tableName);
                return 0;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting from " + tableName + ": " + e.getMessage());
            
            // For critical tables, re-throw the exception
            if (tableName.equals("patients") || tableName.equals("users")) {
                throw new RuntimeException("Failed to delete from critical table " + tableName + ": " + e.getMessage(), e);
            }
            
            // For payment tables, try alternative deletion strategies
            if (tableName.contains("payment")) {
                return handlePaymentTableDeletionError(tableName, patientId, foreignKeys, e);
            }
            
            // For non-critical tables, log and continue
            return 0;
        }
    }
    
    /**
     * ENHANCED: Safely delete from payment tables with FK constraint handling
     */
    private int deletePaymentTableSafely(String tableName, String deleteCondition, int expectedCount) {
        try {
            System.out.println("💳 Safely deleting from payment table " + tableName);
            
            // For razorpay_payments, we need to be extra careful
            if (tableName.equals("razorpay_payments")) {
                return deleteRazorpayPaymentsSafely(deleteCondition, expectedCount);
            }
            
            // Standard deletion for other payment tables
            String deleteSql = "DELETE FROM " + tableName + " WHERE " + deleteCondition;
            int deletedRows = jdbcTemplate.update(deleteSql);
            
            System.out.println("✅ Successfully deleted " + deletedRows + " rows from " + tableName);
            return deletedRows;
            
        } catch (Exception e) {
            System.err.println("❌ Error in safe payment deletion from " + tableName + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * ENHANCED: Handle razorpay_payments deletion with FK constraint resolution
     */
    private int deleteRazorpayPaymentsSafely(String deleteCondition, int expectedCount) {
        try {
            System.out.println("🏦 Deleting razorpay_payments with FK constraint handling");
            
            // NEW STRATEGY: Delete payments based on appointment and lab test booking IDs
            int totalDeleted = 0;
            
            // Delete payments for appointments
            try {
                String appointmentPaymentsSql = """
                    DELETE FROM razorpay_payments 
                    WHERE appointment_id IN (
                        SELECT id FROM appointments WHERE patient_id = ?
                    )
                """;
                // Extract patient ID from deleteCondition
                Long patientId = extractPatientIdFromCondition(deleteCondition);
                if (patientId != null) {
                    int deletedAppointmentPayments = jdbcTemplate.update(appointmentPaymentsSql, patientId);
                    totalDeleted += deletedAppointmentPayments;
                    System.out.println("✅ Deleted " + deletedAppointmentPayments + " appointment payments");
                }
            } catch (Exception e) {
                System.err.println("❌ Error deleting appointment payments: " + e.getMessage());
            }
            
            // Delete payments for lab test bookings
            try {
                String labPaymentsSql = """
                    DELETE FROM razorpay_payments 
                    WHERE lab_test_booking_id IN (
                        SELECT id FROM lab_test_bookings WHERE patient_id = ?
                    )
                """;
                Long patientId = extractPatientIdFromCondition(deleteCondition);
                if (patientId != null) {
                    int deletedLabPayments = jdbcTemplate.update(labPaymentsSql, patientId);
                    totalDeleted += deletedLabPayments;
                    System.out.println("✅ Deleted " + deletedLabPayments + " lab test payments");
                }
            } catch (Exception e) {
                System.err.println("❌ Error deleting lab payments: " + e.getMessage());
            }
            
            System.out.println("✅ Successfully deleted " + totalDeleted + " razorpay_payments rows");
            return totalDeleted;
            
        } catch (Exception e) {
            System.err.println("❌ Payment deletion failed: " + e.getMessage());
            return 0; // Don't throw exception, just continue
        }
    }
    
    /**
     * Extract patient ID from delete condition string
     */
    private Long extractPatientIdFromCondition(String deleteCondition) {
        try {
            if (deleteCondition != null && deleteCondition.contains("patient_id = ")) {
                String[] parts = deleteCondition.split("patient_id = ");
                if (parts.length > 1) {
                    String idStr = parts[1].trim();
                    // Remove any additional conditions after the ID
                    if (idStr.contains(" ")) {
                        idStr = idStr.split(" ")[0];
                    }
                    return Long.parseLong(idStr);
                }
            }
            // If we can't extract from condition, try to find it in the current context
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error extracting patient ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * ENHANCED: Delete razorpay_payments by finding specific records
     */
    private int deleteRazorpayPaymentsByQuery(String deleteCondition) {
        try {
            // First, find all razorpay_payment IDs that need to be deleted
            String findSql = "SELECT id FROM razorpay_payments WHERE " + deleteCondition;
            List<Long> paymentIds = jdbcTemplate.queryForList(findSql, Long.class);
            
            System.out.println("🔍 Found " + paymentIds.size() + " razorpay_payments to delete: " + paymentIds);
            
            int totalDeleted = 0;
            for (Long paymentId : paymentIds) {
                try {
                    String deleteSql = "DELETE FROM razorpay_payments WHERE id = ?";
                    int deleted = jdbcTemplate.update(deleteSql, paymentId);
                    totalDeleted += deleted;
                    System.out.println("✅ Deleted razorpay_payment ID: " + paymentId);
                } catch (Exception e) {
                    System.err.println("❌ Failed to delete razorpay_payment ID " + paymentId + ": " + e.getMessage());
                }
            }
            
            return totalDeleted;
            
        } catch (Exception e) {
            System.err.println("❌ Query-based deletion failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * ENHANCED: Handle payment table deletion errors with alternative strategies
     */
    private int handlePaymentTableDeletionError(String tableName, Long patientId, 
                                              List<ForeignKeyRelation> foreignKeys, Exception originalError) {
        try {
            System.out.println("🔧 Attempting alternative deletion strategy for " + tableName);
            
            // Try to find the specific records causing issues
            String deleteCondition = findDeleteCondition(tableName, patientId, foreignKeys);
            if (deleteCondition == null) {
                System.out.println("⚠️ No delete condition available for alternative strategy");
                return 0;
            }
            
            // Try deleting records one by one
            String findSql = "SELECT id FROM " + tableName + " WHERE " + deleteCondition;
            List<Long> recordIds = jdbcTemplate.queryForList(findSql, Long.class);
            
            int successfulDeletes = 0;
            for (Long recordId : recordIds) {
                try {
                    String deleteSql = "DELETE FROM " + tableName + " WHERE id = ?";
                    int deleted = jdbcTemplate.update(deleteSql, recordId);
                    successfulDeletes += deleted;
                } catch (Exception e) {
                    System.err.println("❌ Could not delete " + tableName + " record ID " + recordId + ": " + e.getMessage());
                }
            }
            
            System.out.println("🔧 Alternative strategy deleted " + successfulDeletes + " records from " + tableName);
            return successfulDeletes;
            
        } catch (Exception e) {
            System.err.println("❌ Alternative deletion strategy failed for " + tableName + ": " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * ENHANCED: Find the correct WHERE condition to delete patient-related data from a table
     */
    private String findDeleteCondition(String tableName, Long patientId, List<ForeignKeyRelation> foreignKeys) {
        // Direct patient reference
        if (hasColumn(tableName, "patient_id")) {
            return "patient_id = " + patientId;
        }
        
        // For patients table itself
        if (tableName.equals("patients")) {
            return "id = " + patientId;
        }
        
        // ENHANCED: Special handling for razorpay_payments
        if (tableName.equals("razorpay_payments")) {
            return findRazorpayPaymentDeleteCondition(patientId);
        }
        
        // For user-related tables, find the user_id first
        if (tableName.equals("users")) {
            try {
                Long userId = jdbcTemplate.queryForObject(
                    "SELECT user_id FROM patients WHERE id = ?", 
                    Long.class, 
                    patientId
                );
                return userId != null ? "id = " + userId : null;
            } catch (Exception e) {
                return null;
            }
        }
        
        if (hasColumn(tableName, "user_id")) {
            try {
                Long userId = jdbcTemplate.queryForObject(
                    "SELECT user_id FROM patients WHERE id = ?", 
                    Long.class, 
                    patientId
                );
                return userId != null ? "user_id = " + userId : null;
            } catch (Exception e) {
                return null;
            }
        }
        
        // Find indirect relationships through foreign keys
        for (ForeignKeyRelation fk : foreignKeys) {
            if (fk.childTable.equals(tableName)) {
                if (fk.parentTable.equals("patients")) {
                    return fk.childColumn + " = " + patientId;
                }
            }
        }
        
        return null;
    }
    
    /**
     * ENHANCED: Find delete condition for razorpay_payments table
     */
    private String findRazorpayPaymentDeleteCondition(Long patientId) {
        try {
            // Always return the patient_id condition - we'll handle the FK logic in deleteRazorpayPaymentsSafely
            return "patient_id = " + patientId;
            
        } catch (Exception e) {
            System.err.println("❌ Error finding razorpay_payments delete condition: " + e.getMessage());
            return "patient_id = " + patientId; // Default condition
        }
    }
    
    /**
     * Check if a table has a specific column
     */
    private boolean hasColumn(String tableName, String columnName) {
        try {
            String sql = """
                SELECT COUNT(*) 
                FROM information_schema.columns 
                WHERE table_schema = DATABASE() 
                AND table_name = ? 
                AND column_name = ?
                """;
            
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get a preview of what would be deleted (without actually deleting)
     */
    public Map<String, Object> getDeletionPreview(Long patientId) {
        Map<String, Object> preview = new HashMap<>();
        
        try {
            List<ForeignKeyRelation> foreignKeys = getAllForeignKeyRelationships();
            Set<String> patientRelatedTables = findAllPatientRelatedTables(foreignKeys);
            List<String> deletionOrder = calculateEnhancedDeletionOrder(foreignKeys, patientRelatedTables, patientId);
            
            Map<String, Integer> tableRowCounts = new HashMap<>();
            int totalRows = 0;
            
            for (String tableName : deletionOrder) {
                int rowCount = getRowCountForDeletion(tableName, patientId, foreignKeys);
                tableRowCounts.put(tableName, rowCount);
                totalRows += rowCount;
            }
            
            preview.put("tablesAffected", tableRowCounts);
            preview.put("deletionOrder", deletionOrder);
            preview.put("totalRowsToDelete", totalRows);
            preview.put("patientId", patientId);
            preview.put("previewGeneratedAt", java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            preview.put("error", "Could not generate preview: " + e.getMessage());
        }
        
        return preview;
    }
    
    /**
     * Get count of rows that would be deleted from a table
     */
    private int getRowCountForDeletion(String tableName, Long patientId, List<ForeignKeyRelation> foreignKeys) {
        try {
            String deleteCondition = findDeleteCondition(tableName, patientId, foreignKeys);
            
            if (deleteCondition != null) {
                String countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + deleteCondition;
                Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
                return count != null ? count : 0;
            }
            
            return 0;
            
        } catch (Exception e) {
            System.out.println("⚠️ Could not count rows in " + tableName + ": " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * ENHANCED: Clean up orphaned records after deletion
     */
    @Transactional
    public void cleanupOrphanedRecords() {
        try {
            System.out.println("🧹 Starting orphaned records cleanup...");
            
            // Clean up orphaned payment records
            cleanupOrphanedPayments();
            
            // Clean up orphaned user records without patient profiles
            cleanupOrphanedUsers();
            
            System.out.println("✅ Orphaned records cleanup completed");
            
        } catch (Exception e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
        }
    }
    
	/**
     * QUICK FIX: Delete payment records first to avoid FK constraints
     */
    @Transactional
    public void deletePaymentRecordsFirst(Long patientId) {
        try {
            System.out.println("🏦 QUICK FIX: Deleting payment records first for patient ID: " + patientId);
            
            // Step 1: Get appointment IDs for this patient
            String getAppointmentIdsSql = "SELECT id FROM appointments WHERE patient_id = ?";
            List<Long> appointmentIds = jdbcTemplate.queryForList(getAppointmentIdsSql, Long.class, patientId);
            System.out.println("📋 Found appointment IDs: " + appointmentIds);
            
            // Step 2: Get lab test booking IDs for this patient  
            String getLabBookingIdsSql = "SELECT id FROM lab_test_bookings WHERE patient_id = ?";
            List<Long> labBookingIds = jdbcTemplate.queryForList(getLabBookingIdsSql, Long.class, patientId);
            System.out.println("📋 Found lab booking IDs: " + labBookingIds);
            
            // Step 3: Delete razorpay_payments for appointments
            int deletedAppointmentPayments = 0;
            for (Long appointmentId : appointmentIds) {
                try {
                    String deleteAppointmentPaymentsSql = "DELETE FROM razorpay_payments WHERE appointment_id = ?";
                    int deleted = jdbcTemplate.update(deleteAppointmentPaymentsSql, appointmentId);
                    deletedAppointmentPayments += deleted;
                    if (deleted > 0) {
                        System.out.println("✅ Deleted " + deleted + " payment(s) for appointment ID: " + appointmentId);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error deleting payments for appointment " + appointmentId + ": " + e.getMessage());
                }
            }
            
            // Step 4: Delete razorpay_payments for lab bookings
            int deletedLabPayments = 0;
            for (Long labBookingId : labBookingIds) {
                try {
                    String deleteLabPaymentsSql = "DELETE FROM razorpay_payments WHERE lab_test_booking_id = ?";
                    int deleted = jdbcTemplate.update(deleteLabPaymentsSql, labBookingId);
                    deletedLabPayments += deleted;
                    if (deleted > 0) {
                        System.out.println("✅ Deleted " + deleted + " payment(s) for lab booking ID: " + labBookingId);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error deleting payments for lab booking " + labBookingId + ": " + e.getMessage());
                }
            }
            
            System.out.println("✅ QUICK FIX COMPLETED: Deleted " + deletedAppointmentPayments + " appointment payments and " + deletedLabPayments + " lab payments");
            
        } catch (Exception e) {
            System.err.println("❌ QUICK FIX ERROR: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - let the deletion continue
        }
    }
	
    /**
     * Clean up orphaned payment records
     */
    private void cleanupOrphanedPayments() {
        try {
            // Clean up razorpay_payments with null appointment_id and lab_test_booking_id
            String cleanupSql = """
                DELETE FROM razorpay_payments 
                WHERE appointment_id IS NULL 
                AND lab_test_booking_id IS NULL
                """;
            
            int cleaned = jdbcTemplate.update(cleanupSql);
            if (cleaned > 0) {
                System.out.println("🧹 Cleaned up " + cleaned + " orphaned razorpay_payments");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error cleaning orphaned payments: " + e.getMessage());
        }
    }
    
    /**
     * Clean up orphaned user records
     */
    private void cleanupOrphanedUsers() {
        try {
            // This is optional - only clean users that were created for patients but patient was deleted
            String findOrphanedUsersSql = """
                SELECT u.id FROM users u
                LEFT JOIN patients p ON u.id = p.user_id
                WHERE p.id IS NULL
                AND EXISTS (SELECT 1 FROM user_roles ur JOIN roles r ON ur.role_id = r.id 
                           WHERE ur.user_id = u.id AND r.name = 'ROLE_PATIENT')
                """;
            
            List<Long> orphanedUserIds = jdbcTemplate.queryForList(findOrphanedUsersSql, Long.class);
            
            for (Long userId : orphanedUserIds) {
                try {
                    // Clean up user roles first
                    jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId);
                    // Clean up user
                    jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
                    System.out.println("🧹 Cleaned up orphaned user ID: " + userId);
                } catch (Exception e) {
                    System.err.println("❌ Error cleaning user " + userId + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error cleaning orphaned users: " + e.getMessage());
        }
    }
    
    /**
     * Inner class to represent foreign key relationships
     */
    private static class ForeignKeyRelation {
        final String childTable;
        final String childColumn;
        final String parentTable;
        final String parentColumn;
        
        ForeignKeyRelation(String childTable, String childColumn, String parentTable, String parentColumn) {
            this.childTable = childTable;
            this.childColumn = childColumn;
            this.parentTable = parentTable;
            this.parentColumn = parentColumn;
        }
        
        @Override
        public String toString() {
            return childTable + "." + childColumn + " -> " + parentTable + "." + parentColumn;
        }
    }
    /**
     * FIXED: Calculate deletion order that ensures user is deleted
     */
    private List<String> calculateFixedDeletionOrderWithUser(List<ForeignKeyRelation> foreignKeys, 
                                                           Set<String> relevantTables, 
                                                           Long patientId, Long userId) {
        
        // Find tables with actual data to delete
        Set<String> tablesWithData = new HashSet<>();
        for (String tableName : relevantTables) {
            if (tableName.equals("razorpay_payments")) {
                continue; // Skip as handled separately
            }
            
            try {
                String deleteCondition = findDeleteConditionForTable(tableName, patientId, userId, foreignKeys);
                if (deleteCondition != null) {
                    String countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + deleteCondition;
                    Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
                    if (count != null && count > 0) {
                        tablesWithData.add(tableName);
                        System.out.println("📊 Table " + tableName + " has " + count + " rows to delete");
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not check data in table " + tableName + ": " + e.getMessage());
            }
        }
        
        // FORCE include users table even if no data detected
        tablesWithData.add("users");
        
        List<String> deletionOrder = new ArrayList<>();
        
        // Detail/child tables first
        String[] detailTables = {
            "prescription_items", "health_risk_assessments", "video_sessions", 
            "treatment_recommendations", "diagnosis_predictions", "image_analysis_results",
            "device_readings", "ai_diagnosis_results", "genomic_risk_factors", 
            "genomic_data", "medical_record_blockchain", "vital_signs", "alert_rules",
            "triage_records", "medical_documents", "reviews", "chat_sessions"
        };
        for (String table : detailTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // Core patient data tables
        String[] coreDataTables = {
            "medical_records", "prescriptions", "lab_test_bookings", 
            "appointments", "monitoring_devices"
        };
        for (String table : coreDataTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // User-related tables (delete BEFORE users)
        String[] userTables = {"sessions", "user_roles"};
        for (String table : userTables) {
            if (tablesWithData.contains(table)) {
                deletionOrder.add(table);
                tablesWithData.remove(table);
            }
        }
        
        // Patient table (delete BEFORE users)
        if (tablesWithData.contains("patients")) {
            deletionOrder.add("patients");
            tablesWithData.remove("patients");
        }
        
        // ALWAYS delete users table LAST
        if (tablesWithData.contains("users")) {
            deletionOrder.add("users");
            tablesWithData.remove("users");
        }
        
        // Add any remaining tables
        deletionOrder.addAll(tablesWithData);
        
        return deletionOrder;
    }

    /**
     * ENHANCED: Find delete condition for any table (patient or user based)
     */
    private String findDeleteConditionForTable(String tableName, Long patientId, Long userId, List<ForeignKeyRelation> foreignKeys) {
        // Direct patient reference
        if (hasColumn(tableName, "patient_id")) {
            return "patient_id = " + patientId;
        }
        
        // For patients table itself
        if (tableName.equals("patients")) {
            return "id = " + patientId;
        }
        
        // For users table itself
        if (tableName.equals("users")) {
            return "id = " + userId;
        }
        
        // For user-related tables
        if (hasColumn(tableName, "user_id")) {
            return "user_id = " + userId;
        }
        
        // Find indirect relationships through foreign keys
        for (ForeignKeyRelation fk : foreignKeys) {
            if (fk.childTable.equals(tableName)) {
                if (fk.parentTable.equals("patients")) {
                    return fk.childColumn + " = " + patientId;
                }
                if (fk.parentTable.equals("users")) {
                    return fk.childColumn + " = " + userId;
                }
            }
        }
        
        return null;
    }

    /**
     * FORCE delete user if it still exists
     */
    private void forceDeleteUser(Long userId, Map<String, Integer> deletionResults) {
        try {
            // Check if user still exists
            String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
            Integer userExists = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
            
            if (userExists != null && userExists > 0) {
                System.out.println("🔧 User " + userId + " still exists, force deleting...");
                
                // Force delete any remaining user_roles
                try {
                    int deletedRoles = jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId);
                    System.out.println("🗑️ Force deleted " + deletedRoles + " user_roles for user " + userId);
                } catch (Exception e) {
                    System.err.println("⚠️ Could not force delete user_roles: " + e.getMessage());
                }
                
                // Force delete any remaining sessions
                try {
                    int deletedSessions = jdbcTemplate.update("DELETE FROM sessions WHERE user_id = ?", userId);
                    System.out.println("🗑️ Force deleted " + deletedSessions + " sessions for user " + userId);
                } catch (Exception e) {
                    System.err.println("⚠️ Could not force delete sessions: " + e.getMessage());
                }
                
                // Now force delete the user
                int deletedUsers = jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
                System.out.println("🗑️ FORCE deleted " + deletedUsers + " user(s) with ID " + userId);
                
                deletionResults.put("users_forced", deletedUsers);
            } else {
                System.out.println("✅ User " + userId + " already deleted");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in force user deletion: " + e.getMessage());
            throw new RuntimeException("Failed to force delete user: " + e.getMessage(), e);
        }
    }

    /**
     * Verify that deletion was complete
     */
    private void verifyCompleteDeleteion(Long patientId, Long userId) {
        try {
            // Check patient
            String checkPatientSql = "SELECT COUNT(*) FROM patients WHERE id = ?";
            Integer patientExists = jdbcTemplate.queryForObject(checkPatientSql, Integer.class, patientId);
            
            // Check user
            String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
            Integer userExists = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
            
            if (patientExists != null && patientExists > 0) {
                throw new RuntimeException("Verification FAILED: Patient " + patientId + " still exists!");
            }
            
            if (userExists != null && userExists > 0) {
                throw new RuntimeException("Verification FAILED: User " + userId + " still exists!");
            }
            
            System.out.println("✅ VERIFICATION PASSED: Both patient and user completely deleted");
            
        } catch (Exception e) {
            System.err.println("❌ Verification error: " + e.getMessage());
            throw new RuntimeException("Deletion verification failed: " + e.getMessage(), e);
        }
    }
}