package com.famcare.model;

import java.time.LocalDateTime;

public class FamilyDoctor {
    private Integer id;
    private String name;
    private String specialization;
    private String mobileNumber;
    private String email;
    private String address;
    private Integer familyId; // Parent ID - all children in family can see
    private String addedBy; // Username of parent who added
    private LocalDateTime createdAt;

    // Constructors
    public FamilyDoctor() {
    }

    public FamilyDoctor(String name, String specialization, String mobileNumber, 
                       String email, String address, Integer familyId, String addedBy) {
        this.name = name;
        this.specialization = specialization;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.address = address;
        this.familyId = familyId;
        this.addedBy = addedBy;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Integer familyId) {
        this.familyId = familyId;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "FamilyDoctor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", specialization='" + specialization + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", familyId=" + familyId +
                '}';
    }
}