package com.mediconnect.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mediconnect.model.ERole;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.RoleRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.service.PatientService;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private PatientService patientService;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userList = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("firstName", user.getFirstName());
                userData.put("lastName", user.getLastName());
                userData.put("email", user.getEmail());
                userData.put("phoneNumber", user.getPhoneNumber());
                userData.put("enabled", user.isEnabled());
                userData.put("createdAt", user.getCreatedAt());
                userData.put("updatedAt", user.getUpdatedAt());
                
                // Get primary role
                String primaryRole = "ROLE_PATIENT";
                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    primaryRole = user.getRoles().iterator().next().getName().name();
                }
                userData.put("role", primaryRole);
                
                userList.add(userData);
            }
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching users: " + e.getMessage());
        }
    }

    /**
     * Get all patients with COMPLETE data
     */
    @GetMapping("/patients")
    public ResponseEntity<?> getAllPatients() {
        try {
            List<Patient> patients = patientRepository.findAll();
            List<Map<String, Object>> patientList = new ArrayList<>();
            
            for (Patient patient : patients) {
                Map<String, Object> patientData = new HashMap<>();
                
                // Basic patient info
                patientData.put("id", patient.getId());
                patientData.put("userId", patient.getUser().getId());
                
                // User data
                User user = patient.getUser();
                patientData.put("firstName", user.getFirstName());
                patientData.put("lastName", user.getLastName());
                patientData.put("email", user.getEmail());
                patientData.put("phoneNumber", user.getPhoneNumber());
                patientData.put("enabled", user.isEnabled());
                
                // Medical data
                patientData.put("dateOfBirth", patient.getDateOfBirth());
                patientData.put("gender", patient.getGender() != null ? patient.getGender().name() : "MALE");
                patientData.put("bloodGroup", patient.getBloodGroup());
                patientData.put("allergies", patient.getAllergies());
                patientData.put("chronicDiseases", patient.getChronicDiseases());
                patientData.put("emergencyContactName", patient.getEmergencyContactName());
                patientData.put("emergencyContactNumber", patient.getEmergencyContactNumber());
                patientData.put("emergencyContactRelation", patient.getEmergencyContactRelation());
                patientData.put("insuranceProvider", patient.getInsuranceProvider());
                patientData.put("insurancePolicyNumber", patient.getInsurancePolicyNumber());
                patientData.put("height", patient.getHeight());
                patientData.put("weight", patient.getWeight());
                patientData.put("preferredLanguage", patient.getPreferredLanguage());
                patientData.put("profileImage", patient.getProfileImage());
                
                // Calculate age if DOB exists
                if (patient.getDateOfBirth() != null) {
                    int age = Period.between(patient.getDateOfBirth(), java.time.LocalDate.now()).getYears();
                    patientData.put("age", age);
                } else {
                    patientData.put("age", null);
                }
                
                // Medical condition
                String medicalCondition = "General checkup";
                if (patient.getAllergies() != null && !patient.getAllergies().isEmpty()) {
                    medicalCondition = "Has allergies";
                } else if (patient.getChronicDiseases() != null && !patient.getChronicDiseases().isEmpty()) {
                    medicalCondition = "Chronic condition";
                }
                patientData.put("medicalCondition", medicalCondition);
                
                patientData.put("createdAt", patient.getCreatedAt());
                patientData.put("updatedAt", patient.getUpdatedAt());
                
                patientList.add(patientData);
            }
            
            return ResponseEntity.ok(patientList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching patients: " + e.getMessage());
        }
    }

    /**
     * Get all admins
     */
    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins() {
        try {
            List<User> admins = userRepository.findUsersByRole(ERole.ROLE_ADMIN);
            List<Map<String, Object>> adminList = new ArrayList<>();
            
            for (User admin : admins) {
                Map<String, Object> adminData = new HashMap<>();
                adminData.put("id", admin.getId());
                adminData.put("firstName", admin.getFirstName());
                adminData.put("lastName", admin.getLastName());
                adminData.put("email", admin.getEmail());
                adminData.put("phoneNumber", admin.getPhoneNumber());
                adminData.put("enabled", admin.isEnabled());
                adminData.put("role", "ROLE_ADMIN");
                adminData.put("createdAt", admin.getCreatedAt());
                adminData.put("updatedAt", admin.getUpdatedAt());
                
                adminList.add(adminData);
            }
            
            return ResponseEntity.ok(adminList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching admins: " + e.getMessage());
        }
    }

    /**
     * Get admin by ID
     */
    @GetMapping("/admins/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        try {
            User admin = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + id));
            
            // Check if user is actually an admin
            boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
            
            if (!isAdmin) {
                return ResponseEntity.badRequest().body("User is not an admin");
            }
            
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("id", admin.getId());
            adminData.put("firstName", admin.getFirstName());
            adminData.put("lastName", admin.getLastName());
            adminData.put("email", admin.getEmail());
            adminData.put("phoneNumber", admin.getPhoneNumber());
            adminData.put("enabled", admin.isEnabled());
            adminData.put("role", "ROLE_ADMIN");
            adminData.put("createdAt", admin.getCreatedAt());
            adminData.put("updatedAt", admin.getUpdatedAt());
            
            return ResponseEntity.ok(adminData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching admin: " + e.getMessage());
        }
    }

    /**
     * Create new admin
     */
    @PostMapping("/admins")
    public ResponseEntity<?> createAdmin(@RequestBody Map<String, String> adminData) {
        try {
            System.out.println("📝 Creating new admin: " + adminData.get("firstName") + " " + adminData.get("lastName"));
            
            // Validate required fields
            String firstName = adminData.get("firstName");
            String lastName = adminData.get("lastName");
            String email = adminData.get("email");
            String password = adminData.get("password");
            String phoneNumber = adminData.get("phoneNumber");
            
            if (firstName == null || lastName == null || email == null || password == null) {
                return ResponseEntity.badRequest().body("Missing required fields: firstName, lastName, email, password");
            }
            
            // Check if email already exists
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Email already exists: " + email);
            }
            
            // Create new admin user
            User admin = new User();
            admin.setFirstName(firstName);
            admin.setLastName(lastName);
            admin.setEmail(email);
            admin.setPhoneNumber(phoneNumber);
            admin.setPassword(passwordEncoder.encode(password)); // Hash the password
            admin.setEnabled(true);
            
            // Assign ADMIN role
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
            admin.setRoles(Set.of(adminRole));
            
            // Save admin
            admin = userRepository.save(admin);
            System.out.println("✅ Admin created successfully with ID: " + admin.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin created successfully");
            response.put("id", admin.getId());
            response.put("firstName", admin.getFirstName());
            response.put("lastName", admin.getLastName());
            response.put("email", admin.getEmail());
            response.put("phoneNumber", admin.getPhoneNumber());
            response.put("enabled", admin.isEnabled());
            response.put("role", "ROLE_ADMIN");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error creating admin: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error creating admin: " + e.getMessage());
        }
    }

    /**
     * Update admin
     */
    @PutMapping("/admins/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @RequestBody Map<String, String> adminData) {
        try {
            System.out.println("📝 Updating admin ID: " + id);
            
            User admin = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + id));
            
            // Check if user is actually an admin
            boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
            
            if (!isAdmin) {
                return ResponseEntity.badRequest().body("User is not an admin");
            }
            
            // Update admin data
            String firstName = adminData.get("firstName");
            String lastName = adminData.get("lastName");
            String email = adminData.get("email");
            String phoneNumber = adminData.get("phoneNumber");
            
            if (firstName != null) admin.setFirstName(firstName);
            if (lastName != null) admin.setLastName(lastName);
            if (phoneNumber != null) admin.setPhoneNumber(phoneNumber);
            
            // Check if email is being changed and if it already exists
            if (email != null && !email.equals(admin.getEmail())) {
                if (userRepository.findByEmail(email).isPresent()) {
                    return ResponseEntity.badRequest().body("Email already exists: " + email);
                }
                admin.setEmail(email);
            }
            
            // Save updated admin
            admin = userRepository.save(admin);
            System.out.println("✅ Admin updated successfully");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin updated successfully");
            response.put("id", admin.getId());
            response.put("firstName", admin.getFirstName());
            response.put("lastName", admin.getLastName());
            response.put("email", admin.getEmail());
            response.put("phoneNumber", admin.getPhoneNumber());
            response.put("enabled", admin.isEnabled());
            response.put("role", "ROLE_ADMIN");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error updating admin: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error updating admin: " + e.getMessage());
        }
    }

    /**
 * Delete admin with comprehensive safety checks
 */
@DeleteMapping("/admins/{id}")
@Transactional
public ResponseEntity<?> deleteAdmin(@PathVariable Long id, HttpServletRequest request) {
    try {
        System.out.println("🗑️ Attempting to delete admin ID: " + id);
        
        // Get current user ID from header (sent by frontend)
        String currentUserIdStr = request.getHeader("X-Current-User-Id");
        Long currentUserId = null;
        if (currentUserIdStr != null) {
            try {
                currentUserId = Long.parseLong(currentUserIdStr);
                System.out.println("🔍 Current user ID from header: " + currentUserId);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid current user ID format: " + currentUserIdStr);
            }
        } else {
            System.out.println("⚠️ No current user ID found in headers");
        }
        
        User adminToDelete = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + id));
        
        // Check if user is actually an admin
        boolean isAdmin = adminToDelete.getRoles().stream()
            .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
        
        if (!isAdmin) {
            return ResponseEntity.badRequest().body("User is not an admin");
        }
        
        // SAFETY CHECK 1: Don't allow self-deletion
        if (currentUserId != null && currentUserId.equals(id)) {
            System.out.println("⚠️ BLOCKED: Admin ID " + currentUserId + " trying to delete themselves (target ID: " + id + ")");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "CANNOT_DELETE_SELF");
            errorResponse.put("message", "You cannot delete your own admin account");
            errorResponse.put("adminId", id);
            errorResponse.put("currentUserId", currentUserId);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // SAFETY CHECK 2: Don't delete if this is the last admin
        List<User> allAdmins = userRepository.findUsersByRole(ERole.ROLE_ADMIN);
        if (allAdmins.size() <= 1) {
            System.out.println("⚠️ BLOCKED: Cannot delete last admin - Only " + allAdmins.size() + " admin(s) remaining");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "CANNOT_DELETE_LAST_ADMIN");
            errorResponse.put("message", "Cannot delete the last admin. At least one admin must remain in the system.");
            errorResponse.put("remainingAdmins", allAdmins.size());
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        String adminName = adminToDelete.getFirstName() + " " + adminToDelete.getLastName();
        
        // All checks passed - proceed with deletion
        userRepository.delete(adminToDelete);
        System.out.println("✅ Admin deleted successfully: " + adminName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin deleted successfully");
        response.put("deletedAdminId", id);
        response.put("adminName", adminName);
        response.put("remainingAdmins", allAdmins.size() - 1);
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("❌ Error deleting admin: " + e.getMessage());
        return ResponseEntity.badRequest().body("Error deleting admin: " + e.getMessage());
    }
}
    
    /**
     * Check if admin can be deleted (helper endpoint)
     */
    @GetMapping("/admins/{id}/can-delete")
    public ResponseEntity<?> canDeleteAdmin(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Get current user ID from header
            String currentUserIdStr = request.getHeader("X-Current-User-Id");
            Long currentUserId = null;
            if (currentUserIdStr != null) {
                try {
                    currentUserId = Long.parseLong(currentUserIdStr);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid current user ID format: " + currentUserIdStr);
                }
            }
            
            User admin = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + id));
            
            // Check if user is actually an admin
            boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
            
            if (!isAdmin) {
                return ResponseEntity.badRequest().body("User is not an admin");
            }
            
            List<User> allAdmins = userRepository.findUsersByRole(ERole.ROLE_ADMIN);
            
            Map<String, Object> response = new HashMap<>();
            response.put("canDelete", true);
            response.put("reason", null);
            
            // Check if trying to delete self
            if (currentUserId != null && currentUserId.equals(id)) {
                response.put("canDelete", false);
                response.put("reason", "You cannot delete your own admin account");
                response.put("errorCode", "CANNOT_DELETE_SELF");
            }
            // Check if last admin
            else if (allAdmins.size() <= 1) {
                response.put("canDelete", false);
                response.put("reason", "Cannot delete the last admin in the system");
                response.put("errorCode", "CANNOT_DELETE_LAST_ADMIN");
            }
            
            response.put("adminCount", allAdmins.size());
            response.put("adminId", id);
            response.put("currentUserId", currentUserId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("canDelete", false);
            errorResponse.put("reason", "Error checking admin deletion permissions: " + e.getMessage());
            errorResponse.put("errorCode", "SYSTEM_ERROR");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            long totalUsers = userRepository.count();
            long totalPatients = patientRepository.count();
            long totalAdmins = userRepository.findUsersByRole(ERole.ROLE_ADMIN).size();
            long totalDoctors = userRepository.findUsersByRole(ERole.ROLE_DOCTOR).size();
            long activeUsers = userRepository.findByStatus(true).size();
            long inactiveUsers = userRepository.findByStatus(false).size();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalPatients", totalPatients);
            stats.put("totalAdmins", totalAdmins);
            stats.put("totalDoctors", totalDoctors);
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", inactiveUsers);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching stats: " + e.getMessage());
        }
    }

    /**
     * Get users by role
     */
    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            ERole eRole = ERole.valueOf("ROLE_" + role.toUpperCase());
            List<User> users = userRepository.findUsersByRole(eRole);
            
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("firstName", user.getFirstName());
                userData.put("lastName", user.getLastName());
                userData.put("email", user.getEmail());
                userData.put("phoneNumber", user.getPhoneNumber());
                userData.put("enabled", user.isEnabled());
                userData.put("role", eRole.name());
                userData.put("createdAt", user.getCreatedAt());
                userData.put("updatedAt", user.getUpdatedAt());
                
                userList.add(userData);
            }
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching users by role: " + e.getMessage());
        }
    }
    
    /**
     * Create a new patient with profile image upload and password
     * Profile image is saved as firstname.ext (e.g., prachi.jpg)
     */
    @PostMapping("/patients")
    public ResponseEntity<?> createPatient(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "bloodGroup", required = false) String bloodGroup,
            @RequestParam(value = "allergies", required = false) String allergies,
            @RequestParam(value = "chronicDiseases", required = false) String chronicDiseases,
            @RequestParam(value = "emergencyContactName", required = false) String emergencyContactName,
            @RequestParam(value = "emergencyContactNumber", required = false) String emergencyContactNumber,
            @RequestParam(value = "emergencyContactRelation", required = false) String emergencyContactRelation,
            @RequestParam(value = "insuranceProvider", required = false) String insuranceProvider,
            @RequestParam(value = "insurancePolicyNumber", required = false) String insurancePolicyNumber,
            @RequestParam(value = "height", required = false) String height,
            @RequestParam(value = "weight", required = false) String weight,
            @RequestParam(value = "preferredLanguage", required = false) String preferredLanguage,
            @RequestParam(value = "medicalCondition", required = false) String medicalCondition,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        
        try {
            System.out.println("📝 Creating new patient: " + firstName + " " + lastName);
            
            // Validate required fields
            if (firstName == null || lastName == null || email == null || password == null) {
                return ResponseEntity.badRequest().body("Missing required fields: firstName, lastName, email, password");
            }
            
            // Check if email already exists
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Email already exists: " + email);
            }
            
            // Create User first
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            user.setPassword(passwordEncoder.encode(password)); // Hash the password
            user.setEnabled(true);
            
            // Assign PATIENT role
            Role patientRole = roleRepository.findByName(ERole.ROLE_PATIENT)
                .orElseThrow(() -> new RuntimeException("Patient role not found"));
            user.setRoles(Set.of(patientRole));
            
            // Save user first
            user = userRepository.save(user);
            System.out.println("✅ User created with ID: " + user.getId());
            
            // Create Patient
            Patient patient = new Patient();
            patient.setUser(user);
            
            // Handle gender
            if (gender != null && !gender.isEmpty()) {
                try {
                    patient.setGender(Patient.Gender.valueOf(gender));
                } catch (Exception e) {
                    patient.setGender(Patient.Gender.MALE);
                }
            } else {
                patient.setGender(Patient.Gender.MALE);
            }
            
            // Handle date of birth
            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                try {
                    patient.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                } catch (Exception e) {
                    System.out.println("⚠️ Invalid date format: " + dateOfBirth);
                }
            }
            
            // Set other patient data
            patient.setBloodGroup(bloodGroup);
            patient.setAllergies(allergies);
            patient.setChronicDiseases(chronicDiseases);
            patient.setEmergencyContactName(emergencyContactName);
            patient.setEmergencyContactNumber(emergencyContactNumber);
            patient.setEmergencyContactRelation(emergencyContactRelation);
            patient.setInsuranceProvider(insuranceProvider);
            patient.setInsurancePolicyNumber(insurancePolicyNumber);
            patient.setPreferredLanguage(preferredLanguage);
            
            // Handle numeric fields
            if (height != null && !height.isEmpty()) {
                try {
                    patient.setHeight(Double.valueOf(height));
                } catch (Exception e) {
                    System.out.println("⚠️ Invalid height: " + height);
                }
            }
            
            if (weight != null && !weight.isEmpty()) {
                try {
                    patient.setWeight(Double.valueOf(weight));
                } catch (Exception e) {
                    System.out.println("⚠️ Invalid weight: " + weight);
                }
            }
            
            // Handle profile image upload - save as firstname.ext
            String savedImageName = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                savedImageName = saveProfileImage(profileImage, firstName);
                patient.setProfileImage(savedImageName);
                System.out.println("✅ Profile image saved as: " + savedImageName);
            }
            
            // Save patient
            patient = patientRepository.save(patient);
            System.out.println("✅ Patient created successfully with ID: " + patient.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient created successfully");
            response.put("id", patient.getId());
            response.put("userId", user.getId());
            if (savedImageName != null) {
                response.put("profileImage", savedImageName);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error creating patient: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error creating patient: " + e.getMessage());
        }
    }

    /**
     * Update patient with profile image upload
     */
    @PutMapping("/patients/{id}")
    public ResponseEntity<?> updatePatient(
            @PathVariable Long id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "bloodGroup", required = false) String bloodGroup,
            @RequestParam(value = "allergies", required = false) String allergies,
            @RequestParam(value = "chronicDiseases", required = false) String chronicDiseases,
            @RequestParam(value = "emergencyContactName", required = false) String emergencyContactName,
            @RequestParam(value = "emergencyContactNumber", required = false) String emergencyContactNumber,
            @RequestParam(value = "emergencyContactRelation", required = false) String emergencyContactRelation,
            @RequestParam(value = "insuranceProvider", required = false) String insuranceProvider,
            @RequestParam(value = "insurancePolicyNumber", required = false) String insurancePolicyNumber,
            @RequestParam(value = "height", required = false) String height,
            @RequestParam(value = "weight", required = false) String weight,
            @RequestParam(value = "preferredLanguage", required = false) String preferredLanguage,
            @RequestParam(value = "medicalCondition", required = false) String medicalCondition,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        
        try {
            System.out.println("📝 Updating patient ID: " + id);
            
            Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + id));
            
            String oldImageName = patient.getProfileImage();
            
            // Update User data
            User user = patient.getUser();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            userRepository.save(user);
            
            // Update Patient data
            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                try {
                    patient.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                } catch (Exception e) {
                    System.out.println("⚠️ Invalid date format: " + dateOfBirth);
                }
            }
            
            if (gender != null && !gender.isEmpty()) {
                try {
                    patient.setGender(Patient.Gender.valueOf(gender));
                } catch (Exception e) {
                    System.out.println("⚠️ Invalid gender: " + gender);
                }
            }
            
            patient.setBloodGroup(bloodGroup);
            patient.setAllergies(allergies);
            patient.setChronicDiseases(chronicDiseases);
            patient.setEmergencyContactName(emergencyContactName);
            patient.setEmergencyContactNumber(emergencyContactNumber);
            patient.setEmergencyContactRelation(emergencyContactRelation);
            patient.setInsuranceProvider(insuranceProvider);
            patient.setInsurancePolicyNumber(insurancePolicyNumber);
            patient.setPreferredLanguage(preferredLanguage);
            
            // Handle numeric fields
            if (height != null && !height.isEmpty()) {
                try {
                    patient.setHeight(Double.valueOf(height));
                } catch (Exception e) {
                    System.out.println("⚠️ Invalid height: " + height);
                }
            }
            
            if (weight != null && !weight.isEmpty()) {
                try {
                    patient.setWeight(Double.valueOf(weight));
                } catch (Exception e) {
                    System.out.println("⚠️ Invalid weight: " + weight);
                }
            }
            
            // Handle profile image update - save as firstname.ext
            if (profileImage != null && !profileImage.isEmpty()) {
                // Delete old image if exists
                if (oldImageName != null && !oldImageName.isEmpty()) {
                    deleteProfileImage(oldImageName);
                }
                
                // Save new image
                String savedImageName = saveProfileImage(profileImage, firstName);
                patient.setProfileImage(savedImageName);
                System.out.println("✅ Profile image updated to: " + savedImageName);
            }
            
            patientRepository.save(patient);
            System.out.println("✅ Patient updated successfully");
            
            return ResponseEntity.ok(Map.of("message", "Patient updated successfully"));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error updating patient: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error updating patient: " + e.getMessage());
        }
    }

    /**
     * Delete patient with COMPLETE cascading delete using dynamic SQL queries
     * This method automatically finds and deletes ALL foreign key references
     */
    /**
     * ✅ FIXED: Delete patient using PatientService's advanced deletion method
     */
    @DeleteMapping("/patients/{id}")
    @Transactional
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        try {
            System.out.println("🚀 Admin Dashboard: Starting enhanced patient deletion for ID: " + id);
            
            // Verify patient exists
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + id));
            
            Long userId = patient.getUser().getId();
            String patientName = patient.getUser().getFirstName() + " " + patient.getUser().getLastName();
            String patientEmail = patient.getUser().getEmail();
            
            System.out.println("👤 Patient: " + patientName + " (User ID: " + userId + ")");
            
            // ✅ USE PATIENT SERVICE'S ADVANCED DELETION METHOD
            System.out.println("🔄 Using PatientService's enhanced deletion method...");
            patientService.deletePatientWithAllRelatedData(id);
            
            // ✅ RUN CLEANUP FOR ORPHANED RECORDS
            System.out.println("🧹 Running orphaned records cleanup...");
            patientService.cleanupOrphanedRecords();
            
            System.out.println("✅ Admin Dashboard: Successfully deleted patient " + id + " and all related data");
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patient and all related data deleted successfully");
            response.put("patientId", id);
            response.put("userId", userId);
            response.put("patientName", patientName);
            response.put("patientEmail", patientEmail);
            response.put("deletedAt", java.time.LocalDateTime.now());
            response.put("method", "Enhanced PatientService Deletion");
            response.put("cleanupPerformed", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Admin Dashboard: Error in enhanced patient deletion: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete patient: " + e.getMessage());
            errorResponse.put("patientId", id);
            errorResponse.put("method", "Enhanced PatientService Deletion");
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Save profile image with firstname as filename (e.g., prachi.jpg)
     */
    private String saveProfileImage(MultipartFile file, String firstName) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Get file extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // Create filename: firstname.extension (e.g., prachi.jpg)
        String fileName = firstName.toLowerCase() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("✅ Profile image saved as: " + fileName);
        return fileName;
    }
    
    /**
     * Delete profile image file from filesystem
     */
    private void deleteProfileImage(String fileName) {
        try {
            Path imagePath = Paths.get(uploadDir).resolve(fileName);
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
                System.out.println("✅ Deleted image file: " + fileName);
            } else {
                System.out.println("⚠️ Image file not found: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("❌ Error deleting image file: " + e.getMessage());
        }
    }
}