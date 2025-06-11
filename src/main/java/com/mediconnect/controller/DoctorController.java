package com.mediconnect.controller;

import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.dto.DoctorDTO;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.DoctorLanguage;
import com.mediconnect.model.User;
import com.mediconnect.model.Role;
import com.mediconnect.model.ERole;
import com.mediconnect.repository.DoctorLanguageRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.repository.RoleRepository;
import com.mediconnect.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.mediconnect.security.UserDetailsImpl;
import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DoctorLanguageRepository doctorLanguageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Phone number validation pattern
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    
    // Pincode validation pattern
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^[0-9]{5,6}$");
    
    // Time format validation pattern (HH:MM-HH:MM or "Closed")
    private static final Pattern TIME_PATTERN = Pattern.compile("^(([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]|Closed)$");
    
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<DoctorDTO> getDoctorByUserId(@PathVariable Long userId) {
        try {
            System.out.println("🔍 Fetching doctor by user ID: " + userId);
            
            Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found for user ID: " + userId));
            
            DoctorDTO doctorDTO = convertToDTO(doctor);
            System.out.println("✅ Doctor found: " + doctorDTO.getFirstName() + " " + doctorDTO.getLastName());
            return ResponseEntity.ok(doctorDTO);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching doctor by user ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null);
        }
    }
 // Create Doctor (Admin Only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> createDoctor(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam("specialization") String specialization,
            @RequestParam("licenseNumber") String licenseNumber,
            @RequestParam("education") String education,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "hospitalAffiliation", required = false) String hospitalAffiliation,
            @RequestParam("yearsOfExperience") Integer yearsOfExperience,
            @RequestParam("consultationFee") Double consultationFee,
            @RequestParam(value = "biography", required = false) String biography,
            @RequestParam(value = "averageRating", required = false) Integer averageRating,
            @RequestParam(value = "isAvailableForEmergency", defaultValue = "false") boolean isAvailableForEmergency,
            @RequestParam(value = "gender", defaultValue = "MALE") String gender,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "about", required = false) String about,
            @RequestParam(value = "patientCount", defaultValue = "500+") String patientCount,
            @RequestParam(value = "expertise", required = false) String expertise,
            @RequestParam(value = "services", required = false) String services,
            @RequestParam(value = "languages", defaultValue = "English,Hindi") String languages,
            // NEW: Clinic Information
            @RequestParam(value = "clinicName", required = false) String clinicName,
            @RequestParam(value = "clinicAddress", required = false) String clinicAddress,
            @RequestParam(value = "clinicCity", required = false) String clinicCity,
            @RequestParam(value = "clinicState", required = false) String clinicState,
            @RequestParam(value = "clinicPincode", required = false) String clinicPincode,
            @RequestParam(value = "clinicPhone", required = false) String clinicPhone,
            @RequestParam(value = "onlineConsultation", defaultValue = "false") boolean onlineConsultation) {

        try {
            System.out.println("🚀 Creating new doctor: " + firstName + " " + lastName);
            
            // Validate required fields
            if (firstName == null || firstName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "First name is required"));
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Last name is required"));
            }
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email is required"));
            }
            
            // Validate clinic phone if provided
            if (clinicPhone != null && !clinicPhone.trim().isEmpty() && 
                !PHONE_PATTERN.matcher(clinicPhone.trim()).matches()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid clinic phone number format"));
            }
            
            // Validate pincode if provided
            if (clinicPincode != null && !clinicPincode.trim().isEmpty() && 
                !PINCODE_PATTERN.matcher(clinicPincode.trim()).matches()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid pincode format (should be 5-6 digits)"));
            }
            
            // Check if user with email already exists
            if (userRepository.findByEmail(email.trim()).isPresent()) {
                System.out.println("❌ User with email already exists: " + email);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User with this email already exists"));
            }

            // Create User first
            User user = new User();
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);
            
            String doctorPassword = (password != null && !password.trim().isEmpty()) 
                ? password.trim() 
                : "doctor123";
            user.setPassword(passwordEncoder.encode(doctorPassword));
            user.setEnabled(true);
            
            Role doctorRole = roleRepository.findByName(ERole.ROLE_DOCTOR)
                .orElseThrow(() -> new RuntimeException("Doctor role not found"));
            user.setRoles(Set.of(doctorRole));

            User savedUser = userRepository.save(user);
            System.out.println("✅ User created with ID: " + savedUser.getId());

            // Create Doctor profile
            Doctor doctor = new Doctor();
            doctor.setUser(savedUser);
            doctor.setSpecialization(specialization.trim());
            doctor.setLicenseNumber(licenseNumber.trim());
            doctor.setEducation(education.trim());
            doctor.setExperience(experience != null ? experience.trim() : null);
            doctor.setHospitalAffiliation(hospitalAffiliation != null ? hospitalAffiliation.trim() : null);
            doctor.setYearsOfExperience(yearsOfExperience);
            doctor.setConsultationFee(consultationFee);
            doctor.setBiography(biography != null ? biography.trim() : null);
            doctor.setAverageRating(averageRating != null ? averageRating : 5);
            doctor.setAvailableForEmergency(isAvailableForEmergency);
            doctor.setGender(gender);

            // Set extended profile fields
            doctor.setAbout(about != null ? about.trim() : null);
            doctor.setPatientCount(patientCount != null ? patientCount.trim() : "500+");
            doctor.setExpertise(expertise != null ? expertise.trim() : null);
            doctor.setServices(services != null ? services.trim() : null);
            
            // NEW: Set clinic information
            doctor.setClinicName(clinicName != null ? clinicName.trim() : null);
            doctor.setClinicAddress(clinicAddress != null ? clinicAddress.trim() : null);
            doctor.setClinicCity(clinicCity != null ? clinicCity.trim() : null);
            doctor.setClinicState(clinicState != null ? clinicState.trim() : null);
            doctor.setClinicPincode(clinicPincode != null ? clinicPincode.trim() : null);
            doctor.setClinicPhone(clinicPhone != null ? clinicPhone.trim() : null);
            doctor.setOnlineConsultation(onlineConsultation);

            // Handle profile image upload
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    System.out.println("📷 Processing profile image: " + profileImage.getOriginalFilename());
                    String storedFileName = fileStorageService.storeFile(profileImage);
                    doctor.setProfileImage(storedFileName);
                    System.out.println("✅ Profile image saved: " + storedFileName);
                } catch (Exception e) {
                    System.err.println("⚠️ Error uploading profile image: " + e.getMessage());
                }
            }

            Doctor savedDoctor = doctorRepository.save(doctor);
            System.out.println("✅ Doctor created with ID: " + savedDoctor.getId());

            // Add languages
            if (languages != null && !languages.trim().isEmpty()) {
                String[] languageArray = languages.split(",");
                for (String language : languageArray) {
                    String trimmedLang = language.trim();
                    if (!trimmedLang.isEmpty()) {
                        DoctorLanguage doctorLanguage = new DoctorLanguage();
                        doctorLanguage.setDoctor(savedDoctor);
                        doctorLanguage.setLanguage(trimmedLang);
                        doctorLanguageRepository.save(doctorLanguage);
                        System.out.println("✅ Added language: " + trimmedLang);
                    }
                }
            }

            System.out.println("🎉 Doctor creation completed successfully!");
            
            return ResponseEntity.ok(Map.of(
                "message", "Doctor created successfully",
                "doctorId", savedDoctor.getId(),
                "userId", savedUser.getId(),
                "email", savedUser.getEmail()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error creating doctor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create doctor: " + e.getMessage()));
        }
    }
 // Get All Doctors (Public)
    @GetMapping("/public/all")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        try {
            System.out.println("📋 Fetching all doctors");
            List<Doctor> doctors = doctorRepository.findAll();
            List<DoctorDTO> doctorDTOs = doctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            System.out.println("✅ Found " + doctorDTOs.size() + " doctors");
            return ResponseEntity.ok(doctorDTOs);
        } catch (Exception e) {
            System.err.println("❌ Error fetching doctors: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get Doctor by ID (Public)
    @GetMapping("/public/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        try {
            System.out.println("🔍 Fetching doctor by ID: " + id);
            Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + id));
            DoctorDTO doctorDTO = convertToDTO(doctor);
            System.out.println("✅ Doctor found: " + doctorDTO.getFirstName() + " " + doctorDTO.getLastName());
            return ResponseEntity.ok(doctorDTO);
        } catch (Exception e) {
            System.err.println("❌ Error fetching doctor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null);
        }
    }

    // Search Doctors
    @GetMapping("/search")
    public ResponseEntity<List<DoctorDTO>> searchDoctors(
            @RequestParam(value = "specialization", required = false) String specialization,
            @RequestParam(value = "hospital", required = false) String hospital,
            @RequestParam(value = "emergency", required = false) Boolean emergency,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "onlineConsultation", required = false) Boolean onlineConsultation) {
        try {
            System.out.println("🔍 Searching doctors with criteria");
            
            List<Doctor> doctors = doctorRepository.findAll();
            
            // Apply filters
            if (specialization != null && !specialization.trim().isEmpty()) {
                doctors = doctors.stream()
                    .filter(d -> d.getSpecialization() != null && 
                               d.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if (hospital != null && !hospital.trim().isEmpty()) {
                doctors = doctors.stream()
                    .filter(d -> d.getHospitalAffiliation() != null && 
                               d.getHospitalAffiliation().toLowerCase().contains(hospital.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if (city != null && !city.trim().isEmpty()) {
                doctors = doctors.stream()
                    .filter(d -> d.getClinicCity() != null && 
                               d.getClinicCity().toLowerCase().contains(city.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if (emergency != null && emergency) {
                doctors = doctors.stream()
                    .filter(Doctor::isAvailableForEmergency)
                    .collect(Collectors.toList());
            }
            
            if (onlineConsultation != null && onlineConsultation) {
                doctors = doctors.stream()
                    .filter(d -> d.getOnlineConsultation() != null && d.getOnlineConsultation())
                    .collect(Collectors.toList());
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                doctors = doctors.stream()
                    .filter(d -> 
                        (d.getUser().getFirstName() != null && d.getUser().getFirstName().toLowerCase().contains(lowerKeyword)) ||
                        (d.getUser().getLastName() != null && d.getUser().getLastName().toLowerCase().contains(lowerKeyword)) ||
                        (d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(lowerKeyword)) ||
                        (d.getHospitalAffiliation() != null && d.getHospitalAffiliation().toLowerCase().contains(lowerKeyword))
                    )
                    .collect(Collectors.toList());
            }
            
            List<DoctorDTO> doctorDTOs = doctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
            System.out.println("✅ Found " + doctorDTOs.size() + " doctors matching criteria");
            return ResponseEntity.ok(doctorDTOs);
        } catch (Exception e) {
            System.err.println("❌ Error searching doctors: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
 // DOCTOR PROFILE MANAGEMENT ENDPOINTS

    // 1. Get Current Doctor Profile
    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getCurrentDoctorProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
            DoctorDTO doctorDTO = convertToDTO(doctor);
            return ResponseEntity.ok(doctorDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Doctor profile not found"));
        }
    }

    // 2. Update Own Profile
    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public ResponseEntity<?> updateOwnProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> profileData) {
        try {
            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

            User user = doctor.getUser();

            // Update User fields
            if (profileData.containsKey("firstName")) {
                user.setFirstName((String) profileData.get("firstName"));
            }
            if (profileData.containsKey("lastName")) {
                user.setLastName((String) profileData.get("lastName"));
            }
            if (profileData.containsKey("email")) {
                String newEmail = ((String) profileData.get("email")).trim().toLowerCase();
                if (!user.getEmail().equals(newEmail)) {
                    if (userRepository.findByEmail(newEmail).isPresent()) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "Email already exists"));
                    }
                    user.setEmail(newEmail);
                }
            }
            if (profileData.containsKey("phoneNumber")) {
                user.setPhoneNumber((String) profileData.get("phoneNumber"));
            }

            userRepository.save(user);

            // Update Doctor fields
            if (profileData.containsKey("specialization")) {
                doctor.setSpecialization((String) profileData.get("specialization"));
            }
            if (profileData.containsKey("education")) {
                doctor.setEducation((String) profileData.get("education"));
            }
            if (profileData.containsKey("experience")) {
                doctor.setExperience((String) profileData.get("experience"));
            }
            if (profileData.containsKey("hospitalAffiliation")) {
                doctor.setHospitalAffiliation((String) profileData.get("hospitalAffiliation"));
            }
            if (profileData.containsKey("consultationFee")) {
                Object feeObj = profileData.get("consultationFee");
                if (feeObj instanceof Number) {
                    doctor.setConsultationFee(((Number) feeObj).doubleValue());
                } else if (feeObj instanceof String && !((String) feeObj).isEmpty()) {
                    doctor.setConsultationFee(Double.parseDouble((String) feeObj));
                }
            }
            if (profileData.containsKey("biography")) {
                doctor.setBiography((String) profileData.get("biography"));
            }
            if (profileData.containsKey("about")) {
                doctor.setAbout((String) profileData.get("about"));
            }
            if (profileData.containsKey("yearsOfExperience")) {
                Object expObj = profileData.get("yearsOfExperience");
                if (expObj instanceof Number) {
                    doctor.setYearsOfExperience(((Number) expObj).intValue());
                } else if (expObj instanceof String && !((String) expObj).isEmpty()) {
                    doctor.setYearsOfExperience(Integer.parseInt((String) expObj));
                }
            }
            if (profileData.containsKey("isAvailableForEmergency")) {
                doctor.setAvailableForEmergency((Boolean) profileData.get("isAvailableForEmergency"));
            }

            // Update clinic information
            if (profileData.containsKey("clinicName")) {
                doctor.setClinicName((String) profileData.get("clinicName"));
            }
            if (profileData.containsKey("clinicAddress")) {
                doctor.setClinicAddress((String) profileData.get("clinicAddress"));
            }
            if (profileData.containsKey("clinicCity")) {
                doctor.setClinicCity((String) profileData.get("clinicCity"));
            }
            if (profileData.containsKey("clinicState")) {
                doctor.setClinicState((String) profileData.get("clinicState"));
            }
            if (profileData.containsKey("clinicPincode")) {
                doctor.setClinicPincode((String) profileData.get("clinicPincode"));
            }
            if (profileData.containsKey("clinicPhone")) {
                doctor.setClinicPhone((String) profileData.get("clinicPhone"));
            }
            if (profileData.containsKey("onlineConsultation")) {
                doctor.setOnlineConsultation((Boolean) profileData.get("onlineConsultation"));
            }

            // Update clinic timings
            if (profileData.containsKey("mondayTiming")) {
                doctor.setMondayTiming((String) profileData.get("mondayTiming"));
            }
            if (profileData.containsKey("tuesdayTiming")) {
                doctor.setTuesdayTiming((String) profileData.get("tuesdayTiming"));
            }
            if (profileData.containsKey("wednesdayTiming")) {
                doctor.setWednesdayTiming((String) profileData.get("wednesdayTiming"));
            }
            if (profileData.containsKey("thursdayTiming")) {
                doctor.setThursdayTiming((String) profileData.get("thursdayTiming"));
            }
            if (profileData.containsKey("fridayTiming")) {
                doctor.setFridayTiming((String) profileData.get("fridayTiming"));
            }
            if (profileData.containsKey("saturdayTiming")) {
                doctor.setSaturdayTiming((String) profileData.get("saturdayTiming"));
            }
            if (profileData.containsKey("sundayTiming")) {
                doctor.setSundayTiming((String) profileData.get("sundayTiming"));
            }

            doctorRepository.save(doctor);

            return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "doctor", convertToDTO(doctor)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }

    // 3. Upload Profile Image
    @PostMapping("/{id}/upload-profile")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> uploadDoctorProfile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a file"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a valid image file"));
            }

            Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

            String storedFileName = fileStorageService.storeFile(file);
            doctor.setProfileImage(storedFileName);
            doctorRepository.save(doctor);

            return ResponseEntity.ok(Map.of(
                "message", "Profile image updated successfully",
                "profileImage", storedFileName
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
 // 4. Change Password for Doctor
    @PutMapping("/profile/password")
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public ResponseEntity<?> changeDoctorPassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, String> passwordData) {
        try {
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");

            System.out.println("🔐 Password change request for user ID: " + userDetails.getId());

            // Input validation
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Current password is required"));
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "New password is required"));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "New password must be at least 6 characters long"));
            }

            if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "New passwords do not match"));
            }

            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

            User user = doctor.getUser();
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "User account not found"));
            }

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                System.out.println("❌ Current password verification failed");
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Current password is incorrect"));
            }

            // Check if new password is different from current
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "New password must be different from current password"));
            }

         // Hash and update the new password
            String hashedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedNewPassword);
            
            User savedUser = userRepository.save(user);
            
            System.out.println("✅ Password updated successfully for user: " + savedUser.getEmail());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully",
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error changing password: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to change password: " + e.getMessage()
                ));
        }
    }

    // 5. NEW: Update Clinic Information
    @PutMapping("/profile/clinic")
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public ResponseEntity<?> updateClinicInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> clinicData) {
        try {
            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

            // Validate clinic phone if provided
            if (clinicData.containsKey("clinicPhone")) {
                String phone = (String) clinicData.get("clinicPhone");
                if (phone != null && !phone.trim().isEmpty() && 
                    !PHONE_PATTERN.matcher(phone.trim()).matches()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid clinic phone number format"));
                }
                doctor.setClinicPhone(phone != null ? phone.trim() : null);
            }

            // Validate pincode if provided
            if (clinicData.containsKey("clinicPincode")) {
                String pincode = (String) clinicData.get("clinicPincode");
                if (pincode != null && !pincode.trim().isEmpty() && 
                    !PINCODE_PATTERN.matcher(pincode.trim()).matches()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid pincode format (should be 5-6 digits)"));
                }
                doctor.setClinicPincode(pincode != null ? pincode.trim() : null);
            }

            // Update clinic information
            if (clinicData.containsKey("clinicName")) {
                doctor.setClinicName((String) clinicData.get("clinicName"));
            }
            if (clinicData.containsKey("clinicAddress")) {
                doctor.setClinicAddress((String) clinicData.get("clinicAddress"));
            }
            if (clinicData.containsKey("clinicCity")) {
                doctor.setClinicCity((String) clinicData.get("clinicCity"));
            }
            if (clinicData.containsKey("clinicState")) {
                doctor.setClinicState((String) clinicData.get("clinicState"));
            }
            if (clinicData.containsKey("onlineConsultation")) {
                doctor.setOnlineConsultation((Boolean) clinicData.get("onlineConsultation"));
            }

            doctorRepository.save(doctor);

            return ResponseEntity.ok(Map.of(
                "message", "Clinic information updated successfully",
                "clinic", getClinicInfo(doctor)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update clinic information: " + e.getMessage()));
        }
    }

    // 6. NEW: Update Clinic Timings
    @PutMapping("/profile/timings")
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public ResponseEntity<?> updateClinicTimings(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, String> timings) {
        try {
            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

            // Validate timing formats
            for (Map.Entry<String, String> entry : timings.entrySet()) {
                String day = entry.getKey();
                String timing = entry.getValue();
                
                if (timing != null && !timing.trim().isEmpty() && 
                    !timing.equalsIgnoreCase("Closed") &&
                    !TIME_PATTERN.matcher(timing.trim()).matches()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid timing format for " + day + ". Use HH:MM-HH:MM or 'Closed'"));
                }
            }

            // Update timings
            if (timings.containsKey("mondayTiming")) {
                doctor.setMondayTiming(timings.get("mondayTiming"));
            }
            if (timings.containsKey("tuesdayTiming")) {
                doctor.setTuesdayTiming(timings.get("tuesdayTiming"));
            }
            if (timings.containsKey("wednesdayTiming")) {
                doctor.setWednesdayTiming(timings.get("wednesdayTiming"));
            }
            if (timings.containsKey("thursdayTiming")) {
                doctor.setThursdayTiming(timings.get("thursdayTiming"));
            }
            if (timings.containsKey("fridayTiming")) {
                doctor.setFridayTiming(timings.get("fridayTiming"));
            }
            if (timings.containsKey("saturdayTiming")) {
                doctor.setSaturdayTiming(timings.get("saturdayTiming"));
            }
            if (timings.containsKey("sundayTiming")) {
                doctor.setSundayTiming(timings.get("sundayTiming"));
            }

            doctorRepository.save(doctor);

            return ResponseEntity.ok(Map.of(
                "message", "Clinic timings updated successfully",
                "timings", getClinicTimings(doctor)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update clinic timings: " + e.getMessage()));
        }
    }

    // 7. NEW: Get Clinic Information
    @GetMapping("/profile/clinic")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getClinicInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

            return ResponseEntity.ok(getClinicInfo(doctor));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Doctor not found"));
        }
    }

    // 8. NEW: Get Clinic Timings
    @GetMapping("/profile/timings")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getClinicTimings(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

            return ResponseEntity.ok(getClinicTimings(doctor));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Doctor not found"));
        }
    }
 // 9. NEW: Update Professional Sections (Areas of Expertise, Services, etc.)
    @PutMapping("/profile/professional")
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public ResponseEntity<?> updateProfessionalInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> professionalData) {
        try {
            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

            // Update professional sections
            if (professionalData.containsKey("expertise")) {
                @SuppressWarnings("unchecked")
                List<String> expertiseList = (List<String>) professionalData.get("expertise");
                if (expertiseList != null) {
                    doctor.setExpertise(String.join(",", expertiseList));
                }
            }

            if (professionalData.containsKey("services")) {
                @SuppressWarnings("unchecked")
                List<String> servicesList = (List<String>) professionalData.get("services");
                if (servicesList != null) {
                    doctor.setServices(String.join(",", servicesList));
                }
            }

            if (professionalData.containsKey("experiences")) {
                String experiencesJson = objectMapper.writeValueAsString(professionalData.get("experiences"));
                doctor.setExperiences(experiencesJson);
            }

            if (professionalData.containsKey("educationDetails")) {
                String educationJson = objectMapper.writeValueAsString(professionalData.get("educationDetails"));
                doctor.setEducationDetails(educationJson);
            }

            if (professionalData.containsKey("awards")) {
                String awardsJson = objectMapper.writeValueAsString(professionalData.get("awards"));
                doctor.setAwards(awardsJson);
            }

            doctorRepository.save(doctor);

            return ResponseEntity.ok(Map.of(
                "message", "Professional information updated successfully",
                "doctor", convertToDTO(doctor)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update professional information: " + e.getMessage()));
        }
    }

    // 10. NEW: Hard Delete Account (Permanent)
 // Hard Delete Account (Permanent) - ENHANCED
    @DeleteMapping("/profile/delete-account")
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public ResponseEntity<?> deleteAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, String> confirmationData) {
        try {
            String currentPassword = confirmationData.get("currentPassword");
            String confirmation = confirmationData.get("confirmation");

            System.out.println("🗑️ Doctor self-deletion request for user ID: " + userDetails.getId());

            // Validation
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Current password is required for account deletion"));
            }

            if (!"DELETE MY ACCOUNT".equals(confirmation)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please type 'DELETE MY ACCOUNT' to confirm deletion"));
            }

            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

            User user = doctor.getUser();

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Current password is incorrect"));
            }

            String doctorName = user.getFirstName() + " " + user.getLastName();
            String doctorEmail = user.getEmail();
            Long doctorId = doctor.getId();
            Long userId = user.getId();

            System.out.println("🔄 Starting enhanced cascade deletion for doctor: " + doctorName);

            // **FIXED ORDER: Delete payments FIRST, then appointments**
            try {
                // Step 1: Delete razorpay_payments first (child table)
                Query paymentsQuery = entityManager.createNativeQuery(
                    "DELETE rp FROM razorpay_payments rp " +
                    "JOIN appointments a ON rp.appointment_id = a.id " +
                    "WHERE a.doctor_id = ?1");
                paymentsQuery.setParameter(1, doctorId);
                int deletedPayments = paymentsQuery.executeUpdate();
                System.out.println("✅ Deleted " + deletedPayments + " payments");

                // Step 2: Delete video_sessions (if they reference appointments)
                entityManager.createNativeQuery(
                    "DELETE vs FROM video_sessions vs " +
                    "JOIN appointments a ON vs.appointment_id = a.id " +
                    "WHERE a.doctor_id = ?1")
                    .setParameter(1, doctorId).executeUpdate();
                System.out.println("✅ Deleted video sessions");

            } catch (Exception e) {
                System.out.println("⚠️ Error deleting payments/sessions: " + e.getMessage());
            }

            // Step 3: Now delete appointments (parent table)
            try {
                Query appointmentQuery = entityManager.createNativeQuery(
                    "DELETE FROM appointments WHERE doctor_id = ?1");
                appointmentQuery.setParameter(1, doctorId);
                int deletedAppointments = appointmentQuery.executeUpdate();
                System.out.println("✅ Deleted " + deletedAppointments + " appointments");
            } catch (Exception e) {
                System.out.println("⚠️ Error deleting appointments: " + e.getMessage());
            }

            // Step 4: Delete other related tables
            try {
                entityManager.createNativeQuery("DELETE FROM vital_signs WHERE doctor_id = ?1")
                    .setParameter(1, doctorId).executeUpdate();
                System.out.println("✅ Deleted vital signs");
            } catch (Exception e) {
                System.out.println("⚠️ No vital_signs table");
            }

            try {
                entityManager.createNativeQuery("DELETE FROM prescriptions WHERE doctor_id = ?1")
                    .setParameter(1, doctorId).executeUpdate();
                System.out.println("✅ Deleted prescriptions");
            } catch (Exception e) {
                System.out.println("⚠️ No prescriptions table");
            }

            try {
                entityManager.createNativeQuery("DELETE FROM medical_records WHERE doctor_id = ?1")
                    .setParameter(1, doctorId).executeUpdate();
                System.out.println("✅ Deleted medical records");
            } catch (Exception e) {
                System.out.println("⚠️ No medical_records table");
            }

            // Step 5: Delete doctor languages
            List<DoctorLanguage> languages = doctorLanguageRepository.findByDoctorId(doctorId);
            if (!languages.isEmpty()) {
                doctorLanguageRepository.deleteAll(languages);
                System.out.println("✅ Deleted " + languages.size() + " doctor languages");
            }

            // Step 6: Delete user roles
            try {
                entityManager.createNativeQuery("DELETE FROM user_roles WHERE user_id = ?1")
                    .setParameter(1, userId).executeUpdate();
                System.out.println("✅ Deleted user roles");
            } catch (Exception e) {
                System.out.println("⚠️ Error deleting user roles: " + e.getMessage());
            }

            // Step 7: Delete doctor profile
            doctorRepository.delete(doctor);
            System.out.println("✅ Deleted doctor profile");

            // Step 8: Delete user account
            userRepository.delete(user);
            System.out.println("✅ Deleted user account");

            System.out.println("🎉 Self-deletion completed successfully for: " + doctorName);

            return ResponseEntity.ok(Map.of(
                "message", "Account deleted successfully",
                "doctorName", doctorName,
                "email", doctorEmail,
                "deletedAt", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error in self-deletion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete account: " + e.getMessage()));
        }
    }

    // 11. NEW: Soft Delete (Deactivate Account)
    @PostMapping("/profile/deactivate")
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public ResponseEntity<?> deactivateAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, String> confirmationData) {
        try {
            String currentPassword = confirmationData.get("currentPassword");
            String reason = confirmationData.get("reason");

            System.out.println("⏸️ Account deactivation request for user ID: " + userDetails.getId());

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Current password is required for account deactivation"));
            }

            Doctor doctor = doctorRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

            User user = doctor.getUser();

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Current password is incorrect"));
            }

            // Deactivate user account
            user.setEnabled(false);
            userRepository.save(user);

            String doctorName = user.getFirstName() + " " + user.getLastName();

            System.out.println("✅ Account deactivated for: " + doctorName);

            return ResponseEntity.ok(Map.of(
                "message", "Account deactivated successfully",
                "doctorName", doctorName,
                "reason", reason != null ? reason : "No reason provided",
                "deactivatedAt", System.currentTimeMillis(),
                "note", "Account can be reactivated by contacting support"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error deactivating account: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to deactivate account: " + e.getMessage()));
        }
    }

    // 12. NEW: Password Strength Validation
    @PostMapping("/profile/validate-password")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> validatePasswordStrength(@RequestBody Map<String, String> data) {
        try {
            String password = data.get("password");
            
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "message", "Password is required"));
            }
            
            List<String> issues = new ArrayList<>();
            
            if (password.length() < 8) {
                issues.add("At least 8 characters");
            }
            
            if (!password.matches(".*[A-Z].*")) {
                issues.add("One uppercase letter");
            }
            
            if (!password.matches(".*[a-z].*")) {
                issues.add("One lowercase letter");
            }
            
            if (!password.matches(".*[0-9].*")) {
                issues.add("One number");
            }
            
            if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>\\/?].*")) {
                issues.add("One special character");
            }
            
            boolean isStrong = issues.isEmpty() && password.length() >= 8;
            String strength = password.length() < 6 ? "Weak" : 
                             password.length() < 8 ? "Fair" : 
                             issues.size() > 2 ? "Good" : "Strong";
            
            return ResponseEntity.ok(Map.of(
                "valid", issues.isEmpty(),
                "strength", strength,
                "issues", issues,
                "score", Math.max(0, 100 - (issues.size() * 20))
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("valid", false, "message", "Validation failed"));
        }
    }
 // ADMIN ENDPOINTS

    // Update Doctor (Admin Only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateDoctor(
            @PathVariable Long id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam("specialization") String specialization,
            @RequestParam("licenseNumber") String licenseNumber,
            @RequestParam("education") String education,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "hospitalAffiliation", required = false) String hospitalAffiliation,
            @RequestParam("yearsOfExperience") Integer yearsOfExperience,
            @RequestParam("consultationFee") Double consultationFee,
            @RequestParam(value = "biography", required = false) String biography,
            @RequestParam(value = "averageRating", required = false) Integer averageRating,
            @RequestParam(value = "isAvailableForEmergency", defaultValue = "false") boolean isAvailableForEmergency,
            @RequestParam(value = "gender", defaultValue = "MALE") String gender,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "about", required = false) String about,
            @RequestParam(value = "patientCount", defaultValue = "500+") String patientCount,
            @RequestParam(value = "expertise", required = false) String expertise,
            @RequestParam(value = "services", required = false) String services,
            @RequestParam(value = "languages", defaultValue = "English,Hindi") String languages,
            // NEW: Clinic Information
            @RequestParam(value = "clinicName", required = false) String clinicName,
            @RequestParam(value = "clinicAddress", required = false) String clinicAddress,
            @RequestParam(value = "clinicCity", required = false) String clinicCity,
            @RequestParam(value = "clinicState", required = false) String clinicState,
            @RequestParam(value = "clinicPincode", required = false) String clinicPincode,
            @RequestParam(value = "clinicPhone", required = false) String clinicPhone,
            @RequestParam(value = "onlineConsultation", defaultValue = "false") boolean onlineConsultation) {

        try {
            System.out.println("🔄 Updating doctor ID: " + id);
            
            Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + id));

            // Check if email is being changed and if new email already exists
            String newEmail = email.trim().toLowerCase();
            if (!doctor.getUser().getEmail().equals(newEmail)) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Another user with this email already exists"));
                }
            }

            // Update User information
            User user = doctor.getUser();
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setEmail(newEmail);
            user.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);
            userRepository.save(user);

            // Update Doctor information
            doctor.setSpecialization(specialization.trim());
            doctor.setLicenseNumber(licenseNumber.trim());
            doctor.setEducation(education.trim());
            doctor.setExperience(experience != null ? experience.trim() : null);
            doctor.setHospitalAffiliation(hospitalAffiliation != null ? hospitalAffiliation.trim() : null);
            doctor.setYearsOfExperience(yearsOfExperience);
            doctor.setConsultationFee(consultationFee);
            doctor.setBiography(biography != null ? biography.trim() : null);
            doctor.setAverageRating(averageRating != null ? averageRating : doctor.getAverageRating());
            doctor.setAvailableForEmergency(isAvailableForEmergency);
            doctor.setGender(gender);

            // Update extended profile fields
            doctor.setAbout(about != null ? about.trim() : null);
            doctor.setPatientCount(patientCount != null ? patientCount.trim() : doctor.getPatientCount());
            doctor.setExpertise(expertise != null ? expertise.trim() : null);
            doctor.setServices(services != null ? services.trim() : null);

            // Update clinic information
            doctor.setClinicName(clinicName != null ? clinicName.trim() : null);
            doctor.setClinicAddress(clinicAddress != null ? clinicAddress.trim() : null);
            doctor.setClinicCity(clinicCity != null ? clinicCity.trim() : null);
            doctor.setClinicState(clinicState != null ? clinicState.trim() : null);
            doctor.setClinicPincode(clinicPincode != null ? clinicPincode.trim() : null);
            doctor.setClinicPhone(clinicPhone != null ? clinicPhone.trim() : null);
            doctor.setOnlineConsultation(onlineConsultation);

            // Handle profile image upload
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    String storedFileName = fileStorageService.storeFile(profileImage);
                    doctor.setProfileImage(storedFileName);
                } catch (Exception e) {
                    System.err.println("⚠️ Error updating profile image: " + e.getMessage());
                }
            }

            doctorRepository.save(doctor);

            // Update languages
            try {
                List<DoctorLanguage> existingLanguages = doctorLanguageRepository.findByDoctorId(id);
                if (!existingLanguages.isEmpty()) {
                    doctorLanguageRepository.deleteAll(existingLanguages);
                }
                
                if (languages != null && !languages.trim().isEmpty()) {
                    String[] languageArray = languages.split(",");
                    for (String language : languageArray) {
                        String trimmedLang = language.trim();
                        if (!trimmedLang.isEmpty()) {
                            DoctorLanguage doctorLanguage = new DoctorLanguage();
                            doctorLanguage.setDoctor(doctor);
                            doctorLanguage.setLanguage(trimmedLang);
                            doctorLanguageRepository.save(doctorLanguage);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error updating languages: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of(
                "message", "Doctor updated successfully",
                "doctorId", doctor.getId()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error updating doctor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update doctor: " + e.getMessage()));
        }
    }

 // Delete Doctor (Admin Only) - ENHANCED WITH CASCADE DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            System.out.println("🗑️ Starting enhanced doctor deletion for ID: " + id);
            
            Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + id));

            String doctorName = doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName();
            Long userId = doctor.getUser().getId();

            System.out.println("👤 Deleting doctor: " + doctorName + " (User ID: " + userId + ")");

            // **FIXED ORDER: Delete payments FIRST, then appointments**
            try {
                // Step 1: Delete razorpay_payments first (child table)
                Query paymentsQuery = entityManager.createNativeQuery(
                    "DELETE rp FROM razorpay_payments rp " +
                    "JOIN appointments a ON rp.appointment_id = a.id " +
                    "WHERE a.doctor_id = ?1");
                paymentsQuery.setParameter(1, id);
                int deletedPayments = paymentsQuery.executeUpdate();
                System.out.println("✅ Deleted " + deletedPayments + " payments");

                // Step 2: Delete video_sessions
                entityManager.createNativeQuery(
                    "DELETE vs FROM video_sessions vs " +
                    "JOIN appointments a ON vs.appointment_id = a.id " +
                    "WHERE a.doctor_id = ?1")
                    .setParameter(1, id).executeUpdate();
                System.out.println("✅ Deleted video sessions");

            } catch (Exception e) {
                System.out.println("⚠️ Error deleting payments/sessions: " + e.getMessage());
            }

            // Step 3: Now delete appointments
            try {
                Query appointmentQuery = entityManager.createNativeQuery(
                    "DELETE FROM appointments WHERE doctor_id = ?1");
                appointmentQuery.setParameter(1, id);
                int deletedAppointments = appointmentQuery.executeUpdate();
                System.out.println("✅ Deleted " + deletedAppointments + " appointments");
            } catch (Exception e) {
                System.out.println("⚠️ No appointments to delete: " + e.getMessage());
            }

            // Step 4: Delete other related data
            try {
                entityManager.createNativeQuery("DELETE FROM vital_signs WHERE doctor_id = ?1")
                    .setParameter(1, id).executeUpdate();
            } catch (Exception e) {
                System.out.println("⚠️ No vital_signs table");
            }

            try {
                entityManager.createNativeQuery("DELETE FROM prescriptions WHERE doctor_id = ?1")
                    .setParameter(1, id).executeUpdate();
            } catch (Exception e) {
                System.out.println("⚠️ No prescriptions table");
            }

            try {
                entityManager.createNativeQuery("DELETE FROM medical_records WHERE doctor_id = ?1")
                    .setParameter(1, id).executeUpdate();
            } catch (Exception e) {
                System.out.println("⚠️ No medical_records table");
            }

            // Step 5: Delete doctor languages
            List<DoctorLanguage> languages = doctorLanguageRepository.findByDoctorId(id);
            if (!languages.isEmpty()) {
                doctorLanguageRepository.deleteAll(languages);
                System.out.println("✅ Deleted " + languages.size() + " doctor languages");
            }

            // Step 6: Delete user roles
            try {
                entityManager.createNativeQuery("DELETE FROM user_roles WHERE user_id = ?1")
                    .setParameter(1, userId).executeUpdate();
                System.out.println("✅ Deleted user roles");
            } catch (Exception e) {
                System.out.println("⚠️ Error deleting user roles: " + e.getMessage());
            }

            // Step 7: Delete doctor profile
            doctorRepository.delete(doctor);
            System.out.println("✅ Deleted doctor profile");

            // Step 8: Delete user account
            userRepository.deleteById(userId);
            System.out.println("✅ Deleted user account");

            System.out.println("🎉 Successfully deleted doctor: " + doctorName);

            return ResponseEntity.ok(Map.of(
                "message", "Doctor and all related data deleted successfully",
                "doctorName", doctorName
            ));

        } catch (Exception e) {
            System.err.println("❌ Error deleting doctor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete doctor: " + e.getMessage()));
        }
    }
 // UTILITY METHODS

    // Helper method to get clinic information
    private Map<String, Object> getClinicInfo(Doctor doctor) {
        Map<String, Object> clinicInfo = new HashMap<>();
        clinicInfo.put("clinicName", doctor.getClinicName());
        clinicInfo.put("clinicAddress", doctor.getClinicAddress());
        clinicInfo.put("clinicCity", doctor.getClinicCity());
        clinicInfo.put("clinicState", doctor.getClinicState());
        clinicInfo.put("clinicPincode", doctor.getClinicPincode());
        clinicInfo.put("clinicPhone", doctor.getClinicPhone());
        clinicInfo.put("onlineConsultation", doctor.getOnlineConsultation());
        return clinicInfo;
    }

    // Helper method to get clinic timings
    private Map<String, String> getClinicTimings(Doctor doctor) {
        Map<String, String> timings = new HashMap<>();
        timings.put("mondayTiming", doctor.getMondayTiming());
        timings.put("tuesdayTiming", doctor.getTuesdayTiming());
        timings.put("wednesdayTiming", doctor.getWednesdayTiming());
        timings.put("thursdayTiming", doctor.getThursdayTiming());
        timings.put("fridayTiming", doctor.getFridayTiming());
        timings.put("saturdayTiming", doctor.getSaturdayTiming());
        timings.put("sundayTiming", doctor.getSundayTiming());
        return timings;
    }

    // Convert Doctor Entity to DTO
    private DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        
        try {
            // Basic Information
            dto.setId(doctor.getId());
            if (doctor.getUser() != null) {
                dto.setUserId(doctor.getUser().getId());
                dto.setFirstName(doctor.getUser().getFirstName());
                dto.setLastName(doctor.getUser().getLastName());
                dto.setEmail(doctor.getUser().getEmail());
                dto.setPhoneNumber(doctor.getUser().getPhoneNumber());
            }
            
            // Professional Information
            dto.setSpecialization(doctor.getSpecialization());
            dto.setLicenseNumber(doctor.getLicenseNumber());
            dto.setEducation(doctor.getEducation());
            dto.setExperience(doctor.getExperience());
            dto.setHospitalAffiliation(doctor.getHospitalAffiliation());
            dto.setYearsOfExperience(doctor.getYearsOfExperience());
            dto.setConsultationFee(doctor.getConsultationFee());
            dto.setBiography(doctor.getBiography());
            dto.setAverageRating(doctor.getAverageRating());
            dto.setAvailableForEmergency(doctor.isAvailableForEmergency());
            dto.setGender(doctor.getGender());
            dto.setProfileImage(doctor.getProfileImage());
            
            // Extended Profile Information
            dto.setAbout(doctor.getAbout());
            dto.setPatientCount(doctor.getPatientCount());
            dto.setReviewCount(doctor.getReviewCount());
            
            // NEW: Clinic Information
            dto.setClinicName(doctor.getClinicName());
            dto.setClinicAddress(doctor.getClinicAddress());
            dto.setClinicCity(doctor.getClinicCity());
            dto.setClinicState(doctor.getClinicState());
            dto.setClinicPincode(doctor.getClinicPincode());
            dto.setClinicPhone(doctor.getClinicPhone());
            dto.setOnlineConsultation(doctor.getOnlineConsultation());
            
            // NEW: Clinic Timings
            dto.setMondayTiming(doctor.getMondayTiming());
            dto.setTuesdayTiming(doctor.getTuesdayTiming());
            dto.setWednesdayTiming(doctor.getWednesdayTiming());
            dto.setThursdayTiming(doctor.getThursdayTiming());
            dto.setFridayTiming(doctor.getFridayTiming());
            dto.setSaturdayTiming(doctor.getSaturdayTiming());
            dto.setSundayTiming(doctor.getSundayTiming());
            
            // Parse expertise and services
            if (doctor.getExpertise() != null && !doctor.getExpertise().isEmpty()) {
                String[] expertiseArray = doctor.getExpertise().split(",");
                List<String> expertiseList = Arrays.stream(expertiseArray)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
                dto.setExpertise(expertiseList);
                dto.setAreasOfExpertise(expertiseList); // For frontend compatibility
            } else {
                dto.setExpertise(generateDefaultExpertise(doctor.getSpecialization()));
                dto.setAreasOfExpertise(generateDefaultExpertise(doctor.getSpecialization()));
            }
            
            if (doctor.getServices() != null && !doctor.getServices().isEmpty()) {
                String[] servicesArray = doctor.getServices().split(",");
                List<String> servicesList = Arrays.stream(servicesArray)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
                dto.setServices(servicesList);
                dto.setServicesOffered(servicesList); // For frontend compatibility
            } else {
                dto.setServices(generateDefaultServices());
                dto.setServicesOffered(generateDefaultServices());
            }
            
            // Parse complex JSON fields with enhanced data
            dto.setExperiences(parseJsonToList(doctor.getExperiences(), generateDefaultExperiences(doctor)));
            dto.setProfessionalExperience(parseJsonToList(doctor.getExperiences(), generateDefaultExperiences(doctor)));
            
            dto.setEducationDetails(parseJsonToList(doctor.getEducationDetails(), generateDefaultEducation(doctor)));
            dto.setEducationTraining(parseJsonToList(doctor.getEducationDetails(), generateDefaultEducation(doctor)));
            
            dto.setAwards(parseJsonToList(doctor.getAwards(), generateDefaultAwards(doctor)));
            dto.setAwardsRecognitions(parseJsonToList(doctor.getAwards(), generateDefaultAwards(doctor)));
            
            dto.setClinics(parseJsonToList(doctor.getClinics(), generateDefaultClinics(doctor)));
            
            // Set default reviews
            dto.setReviews(generateDefaultReviews());
            
            // Get languages safely
            try {
                List<DoctorLanguage> doctorLanguages = doctorLanguageRepository.findByDoctorId(doctor.getId());
                if (!doctorLanguages.isEmpty()) {
                    List<String> languages = doctorLanguages.stream()
                        .map(DoctorLanguage::getLanguage)
                        .collect(Collectors.toList());
                    dto.setLanguages(languages);
                } else {
                    dto.setLanguages(Arrays.asList("English", "Hindi"));
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error fetching languages for doctor " + doctor.getId() + ": " + e.getMessage());
                dto.setLanguages(Arrays.asList("English", "Hindi"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error converting doctor to DTO for ID " + doctor.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return dto;
    }

    // Helper method to parse JSON to List
    private List<Map<String, Object>> parseJsonToList(String jsonString, List<Map<String, Object>> defaultValue) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            if (jsonNode.isArray()) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (JsonNode item : jsonNode) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = objectMapper.convertValue(item, Map.class);
                    result.add(map);
                }
                return result;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing JSON: " + e.getMessage());
        }
        
        return defaultValue;
    }
 // Generate default data methods
    private List<String> generateDefaultExpertise(String specialization) {
        List<String> expertise = new ArrayList<>();
        if (specialization != null) {
            expertise.add("General " + specialization + " consultations");
            expertise.add(specialization + " disorder diagnosis");
            expertise.add("Treatment planning and management");
            expertise.add("Preventive healthcare");
        } else {
            expertise.add("General medical consultations");
            expertise.add("Health assessment");
            expertise.add("Treatment planning");
            expertise.add("Preventive care");
        }
        return expertise;
    }

    private List<String> generateDefaultServices() {
        return Arrays.asList(
            "Online consultations",
            "In-person visits",
            "Follow-up consultations",
            "Health check-ups",
            "Medical certificates",
            "Prescription services"
        );
    }

    private List<Map<String, Object>> generateDefaultExperiences(Doctor doctor) {
        List<Map<String, Object>> experiences = new ArrayList<>();
        
        Map<String, Object> exp1 = new HashMap<>();
        exp1.put("hospital", doctor.getHospitalAffiliation() != null ? doctor.getHospitalAffiliation() : "City Hospital");
        exp1.put("role", "Senior " + (doctor.getSpecialization() != null ? doctor.getSpecialization() : "Medical") + " Specialist");
        exp1.put("period", "2015 - Present");
        exp1.put("description", "Providing comprehensive medical care and consultations to patients.");
        experiences.add(exp1);
        
        Map<String, Object> exp2 = new HashMap<>();
        exp2.put("hospital", "Apollo Hospital");
        exp2.put("role", (doctor.getSpecialization() != null ? doctor.getSpecialization() : "Medical") + " Specialist");
        exp2.put("period", "2010 - 2015");
        exp2.put("description", "Managed outpatient department and emergency cases.");
        experiences.add(exp2);
        
        return experiences;
    }

    private List<Map<String, Object>> generateDefaultEducation(Doctor doctor) {
        List<Map<String, Object>> education = new ArrayList<>();
        
        Map<String, Object> edu1 = new HashMap<>();
        edu1.put("degree", doctor.getEducation() != null ? doctor.getEducation() : "MD");
        edu1.put("institution", "Harvard Medical School");
        edu1.put("year", "2010");
        education.add(edu1);
        
        Map<String, Object> edu2 = new HashMap<>();
        edu2.put("degree", "MBBS");
        edu2.put("institution", "All India Institute of Medical Sciences");
        edu2.put("year", "2006");
        education.add(edu2);
        
        return education;
    }

    private List<Map<String, Object>> generateDefaultAwards(Doctor doctor) {
        List<Map<String, Object>> awards = new ArrayList<>();
        
        Map<String, Object> award1 = new HashMap<>();
        award1.put("title", "Excellence in Patient Care");
        award1.put("year", "2018");
        award1.put("organization", "Indian Medical Association");
        awards.add(award1);
        
        Map<String, Object> award2 = new HashMap<>();
        award2.put("title", "Best " + (doctor.getSpecialization() != null ? doctor.getSpecialization() : "Medical") + " Specialist");
        award2.put("year", "2016");
        award2.put("organization", "Healthcare Excellence Awards");
        awards.add(award2);
        
        return awards;
    }

    private List<Map<String, Object>> generateDefaultClinics(Doctor doctor) {
        List<Map<String, Object>> clinics = new ArrayList<>();
        
        Map<String, Object> clinic1 = new HashMap<>();
        clinic1.put("name", doctor.getClinicName() != null ? doctor.getClinicName() : 
                   (doctor.getHospitalAffiliation() != null ? doctor.getHospitalAffiliation() : "City Hospital"));
        clinic1.put("address", doctor.getClinicAddress() != null ? doctor.getClinicAddress() : 
                   "123 Main Street, " + (doctor.getClinicCity() != null ? doctor.getClinicCity() : "City Center") + 
                   " - " + (doctor.getClinicPincode() != null ? doctor.getClinicPincode() : "560001"));
        clinic1.put("phone", doctor.getClinicPhone() != null ? doctor.getClinicPhone() : "+91 9876543210");
        
        List<Map<String, String>> timings1 = new ArrayList<>();
        Map<String, String> timing1 = new HashMap<>();
        timing1.put("day", "Monday - Friday");
        timing1.put("time", doctor.getMondayTiming() != null ? doctor.getMondayTiming() : "09:00 AM - 01:00 PM");
        timings1.add(timing1);
        
        Map<String, String> timing2 = new HashMap<>();
        timing2.put("day", "Saturday");
        timing2.put("time", doctor.getSaturdayTiming() != null ? doctor.getSaturdayTiming() : "10:00 AM - 02:00 PM");
        timings1.add(timing2);
        
        clinic1.put("timings", timings1);
        clinics.add(clinic1);
        
        return clinics;
    }

    private List<Map<String, Object>> generateDefaultReviews() {
        List<Map<String, Object>> reviews = new ArrayList<>();
        
        Map<String, Object> review1 = new HashMap<>();
        review1.put("name", "Rahul Sharma");
        review1.put("rating", 5);
        review1.put("date", "2 months ago");
        review1.put("comment", "Excellent doctor! Very attentive and knowledgeable. I felt very comfortable during the consultation and all my concerns were addressed.");
        reviews.add(review1);
        
        Map<String, Object> review2 = new HashMap<>();
        review2.put("name", "Priya Patel");
        review2.put("rating", 4);
        review2.put("date", "3 months ago");
        review2.put("comment", "Good experience overall. The doctor was professional and gave me proper medical advice. The only downside was the waiting time.");
        reviews.add(review2);
        
        Map<String, Object> review3 = new HashMap<>();
        review3.put("name", "Amit Kumar");
        review3.put("rating", 5);
        review3.put("date", "5 months ago");
        review3.put("comment", "Very happy with the treatment. The doctor explained everything clearly and the prescribed medication worked well.");
        reviews.add(review3);
        
        return reviews;
    }

    // Utility method to get file extension
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "jpg";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "jpg";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "Doctor service is running",
            "timestamp", System.currentTimeMillis(),
            "features", Arrays.asList(
                "Profile Management",
                "Clinic Information",
                "Timings Management", 
                "Professional Sections",
                "Account Deletion",
                "Password Management",
                "Enhanced Search",
                "Admin Operations"
            )
        ));
    }
}