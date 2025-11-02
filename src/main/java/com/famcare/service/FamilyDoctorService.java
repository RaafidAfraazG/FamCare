package com.famcare.service;

import com.famcare.model.FamilyDoctor;
import com.famcare.repository.FamilyDoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FamilyDoctorService {

    @Autowired
    private FamilyDoctorRepository familyDoctorRepository;

    /**
     * Add a new family doctor
     * 
     * @param name - Doctor's name
     * @param specialization - Doctor's specialization
     * @param mobileNumber - Doctor's mobile number
     * @param email - Doctor's email
     * @param address - Doctor's address
     * @param familyId - Parent ID (family ID)
     * @param addedBy - Username of parent who added
     */
    public void addDoctor(String name, String specialization, String mobileNumber, 
                         String email, String address, Integer familyId, String addedBy) {
        // Basic validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Doctor name is required");
        }
        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required");
        }

        FamilyDoctor doctor = new FamilyDoctor(
            name.trim(), 
            specialization != null ? specialization.trim() : "", 
            mobileNumber.trim(), 
            email != null ? email.trim() : "", 
            address != null ? address.trim() : "", 
            familyId, 
            addedBy
        );
        
        familyDoctorRepository.save(doctor);
    }

    /**
     * Get all doctors for a family
     */
    public List<FamilyDoctor> getFamilyDoctors(Integer familyId) {
        return familyDoctorRepository.findByFamilyId(familyId);
    }

    /**
     * Get a specific doctor by ID
     */
    public Optional<FamilyDoctor> getDoctorById(Integer id) {
        return familyDoctorRepository.findById(id);
    }

    /**
     * Update doctor information
     */
    public void updateDoctor(Integer id, String name, String specialization, 
                           String mobileNumber, String email, String address) {
        Optional<FamilyDoctor> doctorOpt = familyDoctorRepository.findById(id);
        
        if (doctorOpt.isEmpty()) {
            throw new IllegalArgumentException("Doctor not found");
        }

        FamilyDoctor doctor = doctorOpt.get();
        doctor.setName(name.trim());
        doctor.setSpecialization(specialization != null ? specialization.trim() : "");
        doctor.setMobileNumber(mobileNumber.trim());
        doctor.setEmail(email != null ? email.trim() : "");
        doctor.setAddress(address != null ? address.trim() : "");

        familyDoctorRepository.update(doctor);
    }

    /**
     * Delete a doctor
     */
    public void deleteDoctor(Integer id) {
        familyDoctorRepository.deleteById(id);
    }

    /**
     * Get doctor count for a family
     */
    public int getDoctorCount(Integer familyId) {
        return familyDoctorRepository.countByFamilyId(familyId);
    }

    /**
     * Check if doctor belongs to family
     */
    public boolean isDoctorInFamily(Integer doctorId, Integer familyId) {
        Optional<FamilyDoctor> doctor = familyDoctorRepository.findById(doctorId);
        return doctor.isPresent() && doctor.get().getFamilyId().equals(familyId);
    }
}