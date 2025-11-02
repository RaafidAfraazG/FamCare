package com.famcare.repository;

import com.famcare.model.FamilyDoctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class FamilyDoctorRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper to convert database row to FamilyDoctor object
    private RowMapper<FamilyDoctor> doctorRowMapper = (rs, rowNum) -> {
        FamilyDoctor doctor = new FamilyDoctor();
        doctor.setId(rs.getInt("id"));
        doctor.setName(rs.getString("name"));
        doctor.setSpecialization(rs.getString("specialization"));
        doctor.setMobileNumber(rs.getString("mobile_number"));
        doctor.setEmail(rs.getString("email"));
        doctor.setAddress(rs.getString("address"));
        doctor.setFamilyId(rs.getInt("family_id"));
        doctor.setAddedBy(rs.getString("added_by"));
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            doctor.setCreatedAt(ts.toLocalDateTime());
        }
        
        return doctor;
    };

    /**
     * Save a new family doctor
     */
    public void save(FamilyDoctor doctor) {
        String sql = "INSERT INTO family_doctors (name, specialization, mobile_number, email, address, family_id, added_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getMobileNumber(),
                doctor.getEmail(),
                doctor.getAddress(),
                doctor.getFamilyId(),
                doctor.getAddedBy()
        );
    }

    /**
     * Find all doctors for a family
     */
    public List<FamilyDoctor> findByFamilyId(Integer familyId) {
        String sql = "SELECT * FROM family_doctors WHERE family_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, doctorRowMapper, familyId);
    }

    /**
     * Find doctor by ID
     */
    public Optional<FamilyDoctor> findById(Integer id) {
        String sql = "SELECT * FROM family_doctors WHERE id = ?";
        try {
            FamilyDoctor doctor = jdbcTemplate.queryForObject(sql, doctorRowMapper, id);
            return Optional.of(doctor);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Update a family doctor
     */
    public void update(FamilyDoctor doctor) {
        String sql = "UPDATE family_doctors SET name = ?, specialization = ?, mobile_number = ?, " +
                    "email = ?, address = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getMobileNumber(),
                doctor.getEmail(),
                doctor.getAddress(),
                doctor.getId()
        );
    }

    /**
     * Delete a family doctor by ID
     */
    public void deleteById(Integer id) {
        String sql = "DELETE FROM family_doctors WHERE id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, id);
            if (rowsAffected > 0) {
                System.out.println("✅ Family doctor deleted from database: ID " + id);
            } else {
                System.out.println("⚠️ Family doctor not found: ID " + id);
            }
        } catch (Exception e) {
            System.err.println("❌ Error deleting family doctor: " + e.getMessage());
            throw new RuntimeException("Failed to delete family doctor: " + e.getMessage());
        }
    }

    /**
     * Count doctors for a family
     */
    public int countByFamilyId(Integer familyId) {
        String sql = "SELECT COUNT(*) FROM family_doctors WHERE family_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, familyId);
        return count != null ? count : 0;
    }
}